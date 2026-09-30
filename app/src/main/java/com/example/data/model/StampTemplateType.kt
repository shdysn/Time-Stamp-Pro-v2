package com.example.data.model

enum class StampTemplateType(
    val id: String,
    val displayName: String,
    val description: String
) {
    CLASSIC_CARD(
        id = "CLASSIC_CARD",
        displayName = "Classic Card",
        description = "Rounded dark card with prominent time, coordinates & sensor chips"
    ),
    MODERN_MINIMAL(
        id = "MODERN_MINIMAL",
        displayName = "Modern Minimal",
        description = "Clean borderless typography with vertical neon accent bar"
    ),
    CYBER_TECH_HUD(
        id = "CYBER_TECH_HUD",
        displayName = "Cyber Tech HUD",
        description = "Tactical corner reticle brackets with technical monospace telemetry"
    ),
    FRAMED_OUTLINE(
        id = "FRAMED_OUTLINE",
        displayName = "Framed Outline",
        description = "Centered uppercase inspection certificate style with thin outline"
    ),
    COMPACT_PILL(
        id = "COMPACT_PILL",
        displayName = "Compact Pill",
        description = "Ultra-compact rounded capsule showing time and location only"
    ),
    FULL_BANNER(
        id = "FULL_BANNER",
        displayName = "Full Banner",
        description = "Edge-to-edge docked bottom strip with multi-column data layout"
    );

    companion object {
        fun fromId(id: String?): StampTemplateType {
            if (id.isNullOrBlank()) return CLASSIC_CARD
            return entries.firstOrNull {
                it.id.equals(id, ignoreCase = true) || it.name.equals(id, ignoreCase = true)
            } ?: CLASSIC_CARD
        }
    }
}
