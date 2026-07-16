package com.personal.sidebar.model

import com.personal.sidebar.Edge
import kotlinx.serialization.Serializable

/**
 * An entry shown in the panel: a single app, a web link/PWA (opens a URL), a
 * folder (emoji circle that expands), or a group (a titled inline section).
 */
@Serializable
enum class ItemType { APP, LINK, FOLDER, GROUP }

@Serializable
data class SidebarItem(
    val type: ItemType,
    /** Package name for [ItemType.APP]. */
    val packageName: String? = null,
    /** Display name for folders/groups/links. */
    val name: String? = null,
    /** Emoji shown in a folder circle or a link tile. */
    val emoji: String? = null,
    /** URL opened for [ItemType.LINK] (launches a PWA/WebAPK if installed). */
    val url: String? = null,
    /** Member package names for [ItemType.FOLDER] / [ItemType.GROUP]. */
    val packages: List<String> = emptyList(),
) {
    companion object {
        fun app(pkg: String) = SidebarItem(ItemType.APP, packageName = pkg)
        fun link(name: String, url: String, emoji: String? = null) =
            SidebarItem(ItemType.LINK, name = name, emoji = emoji, url = url)
        fun folder(name: String, packages: List<String>, emoji: String? = null) =
            SidebarItem(ItemType.FOLDER, name = name, emoji = emoji, packages = packages)
        fun group(name: String, packages: List<String>) =
            SidebarItem(ItemType.GROUP, name = name, packages = packages)
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
    /** Background scrim behind the panel: RGB color (alpha ignored here). */
    val scrimColor: Int = 0xFF000000.toInt(),
    /** Background scrim opacity: 0 = no dim (see through), 1 = solid. */
    val scrimAlpha: Float = 0.25f,
    /** White "glass edge" stroke width in dp; 0 = no edge. */
    val edgeDp: Float = 1f,
    /** Corner radius (dp) of the panel's inner edge. */
    val cornerDp: Int = 28,
    /** Show app/link names under their icons in the panel. */
    val showLabels: Boolean = true,
)

/** Appearance of a folder card (a "nested glass" layer inside the panel). */
@Serializable
data class FolderConfig(
    /** Folder tint opacity — usually higher than the panel so it reads closer. */
    val opacity: Float = 0.9f,
    /** Folder tint lightness: 0 = near-black, 1 = white. */
    val brightness: Float = 0.45f,
    /** Folder edge stroke width in dp; 0 = none. */
    val edgeDp: Float = 1.5f,
    /** Apps per row inside a folder. */
    val columns: Int = 3,
    /** Folder corner radius in dp. */
    val cornerDp: Int = 20,
    /** Drop-shadow extent (dp) on each side of the folder card. */
    val shadowTopDp: Float = 0f,
    val shadowBottomDp: Float = 12f,
    val shadowLeftDp: Float = 4f,
    val shadowRightDp: Float = 4f,
)

/** The whole persisted sidebar configuration. */
@Serializable
data class SidebarConfig(
    val handle: HandleConfig = HandleConfig(),
    val panel: PanelConfig = PanelConfig(),
    val folder: FolderConfig = FolderConfig(),
    val items: List<SidebarItem> = emptyList(),
)
