package com.example.data.model

data class LocationData(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val altitude: Double = 0.0,
    val accuracy: Float = 0f,
    val speed: Float = 0f,
    val bearing: Float = 0f,
    val address: String = "Detecting location...",
    val locality: String = "",
    val subLocality: String = "",
    val adminArea: String = "",
    val country: String = "",
    val postalCode: String = "",
    val isMock: Boolean = false,
    val provider: String = "GPS"
) {
    /**
     * Extracts a concise, human-readable place name for compact templates (e.g. Compact Pill, Modern Minimal),
     * strictly excluding raw Plus Codes (e.g. 2HVR+R6Q), postal codes, and GPS coordinate fragments.
     */
    fun getShortLocation(): String {
        // 1. If locality is present and clean (not a Plus Code or postal code)
        val cleanLocality = locality.trim().takeIf { it.isNotBlank() && !isCodeOrPlusCode(it) }
        val cleanSubLocality = subLocality.trim().takeIf { it.isNotBlank() && !isCodeOrPlusCode(it) }

        if (!cleanLocality.isNullOrBlank()) {
            if (!cleanSubLocality.isNullOrBlank() && !cleanLocality.equals(cleanSubLocality, ignoreCase = true) && cleanSubLocality.length <= 16) {
                return "$cleanSubLocality, $cleanLocality"
            }
            return cleanLocality
        }

        if (!cleanSubLocality.isNullOrBlank()) {
            return cleanSubLocality
        }

        // 2. Extract from full address, skipping Plus Codes, postal codes, and coordinates
        val cleanSegments = address.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() && !isCodeOrPlusCode(it) && !it.startsWith("Coordinates") && !it.startsWith("Resolving") && it != "Detecting location..." }

        if (cleanSegments.isNotEmpty()) {
            return cleanSegments[0]
        }

        // 3. Fallback: return first non-code piece or clean address without plus code
        val nonCode = address.split(",").map { it.trim() }.firstOrNull { !isCodeOrPlusCode(it) }
        if (!nonCode.isNullOrBlank()) {
            return nonCode
        }

        return if (address.isNotBlank() && address != "Detecting location..." && address != "Resolving...") {
            address.replace(Regex("^[A-Z0-9]{2,8}\\+[A-Z0-9]{2,5},?\\s*", RegexOption.IGNORE_CASE), "").trim()
        } else {
            ""
        }
    }

    companion object {
        fun isCodeOrPlusCode(text: String): Boolean {
            val clean = text.trim()
            if (clean.isEmpty()) return true

            // Google Open Location Code (Plus Code) e.g., "2HVR+R6Q", "8FVC9G8F+5W", "7MRX+W9"
            val plusCodePattern = Regex("^[A-Z0-9]{2,8}\\+[A-Z0-9]{2,5}$", RegexOption.IGNORE_CASE)
            if (plusCodePattern.matches(clean)) return true

            // Contains plus with short alphanumeric parts (e.g. "+R6Q", "2HVR+R6Q")
            if (clean.contains('+')) {
                val parts = clean.split("+")
                if (parts.size == 2 && parts[0].length <= 8 && parts[1].length <= 8 &&
                    parts[0].all { it.isLetterOrDigit() } && parts[1].take(3).all { it.isLetterOrDigit() }) {
                    return true
                }
            }

            // Pure numeric postal codes (e.g., "54000", "90210")
            if (clean.matches(Regex("^[0-9]{3,8}$"))) return true

            // Coordinate notation like "31.5204°, 74.3587°"
            if (clean.contains('°')) return true

            return false
        }
    }
}
