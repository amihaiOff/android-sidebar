package com.personal.sidebar.model

import com.personal.sidebar.Edge
import kotlinx.serialization.Serializable

/** An entry shown in the panel: either a single app or a folder of apps. */
@Serializable
enum class ItemType { APP, FOLDER }

@Serializable
data class SidebarItem(
    val type: ItemType,
    /** Package name for [ItemType.APP]. */
    val packageName: String? = null,
    /** Display name for [ItemType.FOLDER]. */
    val name: String? = null,
    /** Member package names for [ItemType.FOLDER]. */
    val packages: List<String> = emptyList(),
) {
    companion object {
        fun app(pkg: String) = SidebarItem(ItemType.APP, packageName = pkg)
        fun folder(name: String, packages: List<String>) =
            SidebarItem(ItemType.FOLDER, name = name, packages = packages)
    }
}

/** Appearance + placement of the edge handle. */
@Serializable
data class HandleConfig(
    val edge: Edge = Edge.RIGHT,
    /** Packed ARGB color of the handle pill. */
    val colorArgb: Int = DEFAULT_COLOR,
    val widthDp: Int = 22,
    val lengthDp: Int = 150,
    /** Vertical placement along the edge: 0f = top, 0.5f = center, 1f = bottom. */
    val verticalBias: Float = 0.5f,
) {
    companion object {
        // Translucent indigo (alpha 0x8C).
        const val DEFAULT_COLOR: Int = 0x8C4C5BD4.toInt()
    }
}

/** Appearance of the slide-out panel itself. */
@Serializable
data class PanelConfig(
    /** Background opacity of the panel: 0.35 = very see-through, 1 = solid.
     *  Kept low by default so the backdrop blur shows as frosted glass. */
    val opacity: Float = 0.6f,
    /** Panel tint lightness: 0 = near-black, 1 = light grey. */
    val brightness: Float = 0.3f,
    /** Backdrop blur (frost) radius in dp; 0 = off. Needs Android 12+. */
    val blurDp: Int = 48,
)

/** The whole persisted sidebar configuration. */
@Serializable
data class SidebarConfig(
    val handle: HandleConfig = HandleConfig(),
    val panel: PanelConfig = PanelConfig(),
    val items: List<SidebarItem> = emptyList(),
)
