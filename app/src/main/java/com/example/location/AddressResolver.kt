package com.example.location

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.example.data.model.LocationData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

data class ResolvedAddress(
    val fullAddress: String,
    val locality: String = "",
    val subLocality: String = "",
    val adminArea: String = "",
    val country: String = "",
    val postalCode: String = ""
)

class AddressResolver(private val context: Context) {

    private val cache = ConcurrentHashMap<String, ResolvedAddress>()

    suspend fun resolveAddress(latitude: Double, longitude: Double): ResolvedAddress = withContext(Dispatchers.IO) {
        val cacheKey = "${String.format(Locale.US, "%.4f", latitude)},${String.format(Locale.US, "%.4f", longitude)}"
        cache[cacheKey]?.let { return@withContext it }

        if (!Geocoder.isPresent()) {
            val fallback = ResolvedAddress(fullAddress = "Coordinates ($cacheKey)")
            cache[cacheKey] = fallback
            return@withContext fallback
        }

        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val list = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(latitude, longitude, 1)
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(latitude, longitude, 1)
            }

            if (!list.isNullOrEmpty()) {
                val resolved = buildResolvedAddress(list[0])
                if (resolved.fullAddress.isNotBlank()) {
                    cache[cacheKey] = resolved
                    return@withContext resolved
                }
            }
        } catch (e: Exception) {
            // Geocoder service can sometimes fail or timeout on emulators/offline
        }

        val fallback = ResolvedAddress(
            fullAddress = "${String.format(Locale.US, "%.5f", latitude)}°, ${String.format(Locale.US, "%.5f", longitude)}°"
        )
        cache[cacheKey] = fallback
        return@withContext fallback
    }

    suspend fun getAddress(latitude: Double, longitude: Double): String {
        return resolveAddress(latitude, longitude).fullAddress
    }

    private fun buildResolvedAddress(address: Address): ResolvedAddress {
        val parts = mutableListOf<String>()

        // 1. Street / Thoroughfare
        val thoroughfare = address.thoroughfare
        val subThoroughfare = address.subThoroughfare
        if (!thoroughfare.isNullOrBlank() && !LocationData.isCodeOrPlusCode(thoroughfare)) {
            if (!subThoroughfare.isNullOrBlank() && !LocationData.isCodeOrPlusCode(subThoroughfare)) {
                parts.add("$subThoroughfare $thoroughfare")
            } else {
                parts.add(thoroughfare)
            }
        } else if (!address.featureName.isNullOrBlank() &&
            address.featureName != address.locality &&
            !LocationData.isCodeOrPlusCode(address.featureName)
        ) {
            // Only add featureName if it is NOT a Plus Code (skips "2HVR+R6Q")
            parts.add(address.featureName)
        }

        // 2. SubLocality (Neighborhood, Sector, Colony, Area)
        val subLocality = address.subLocality?.takeIf { !LocationData.isCodeOrPlusCode(it) }
        if (!subLocality.isNullOrBlank() && !parts.contains(subLocality)) {
            parts.add(subLocality)
        }

        // 3. Locality (City / Town)
        val locality = address.locality?.takeIf { !LocationData.isCodeOrPlusCode(it) }
        if (!locality.isNullOrBlank() && !parts.contains(locality)) {
            parts.add(locality)
        }

        // 4. SubAdminArea (District)
        val subAdminArea = address.subAdminArea?.takeIf { !LocationData.isCodeOrPlusCode(it) }
        if (!subAdminArea.isNullOrBlank() && subAdminArea != locality && !parts.contains(subAdminArea)) {
            parts.add(subAdminArea)
        }

        // 5. AdminArea (State / Province)
        val adminArea = address.adminArea?.takeIf { !LocationData.isCodeOrPlusCode(it) }
        if (!adminArea.isNullOrBlank() && !parts.contains(adminArea)) {
            parts.add(adminArea)
        }

        // 6. Country
        val country = address.countryName?.takeIf { !LocationData.isCodeOrPlusCode(it) }
        if (!country.isNullOrBlank() && !parts.contains(country)) {
            parts.add(country)
        }

        val fullAddress = if (parts.isNotEmpty()) {
            parts.joinToString(", ")
        } else {
            // Fallback to getAddressLine(0), but strip any leading plus code!
            val rawLine = address.getAddressLine(0) ?: "Location verified"
            cleanLeadingPlusCode(rawLine)
        }

        return ResolvedAddress(
            fullAddress = fullAddress,
            locality = locality ?: (subLocality ?: ""),
            subLocality = subLocality ?: "",
            adminArea = adminArea ?: "",
            country = country ?: "",
            postalCode = address.postalCode ?: ""
        )
    }

    private fun cleanLeadingPlusCode(addressLine: String): String {
        val cleaned = addressLine.replace(Regex("^[A-Z0-9]{2,8}\\+[A-Z0-9]{2,5},?\\s*", RegexOption.IGNORE_CASE), "").trim()
        return cleaned.ifBlank { addressLine }
    }
}
