package com.personal.sidebar.model

import com.personal.sidebar.Edge
import kotlinx.serialization.SerialName
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
    /** Emoji shown in a folder circle or a link tile (legacy / fallback). */
    val emoji: String? = null,
    /** Outline-icon key (see FolderIcons) for a folder; takes precedence over emoji. */
    val iconKey: String? = null,
    /** Packed ARGB tint for the folder icon; null = colour to the system theme. */
    val colorArgb: Int? = null,
    /** URL opened for [ItemType.LINK] (launches a PWA/WebAPK if installed). */
    val url: String? = null,
    /** For [ItemType.LINK]: force the URL to open in this app (e.g. the browser
     *  that installed the PWA), so it launches standalone. Null = system default. */
    val targetPackage: String? = null,
    /** Member package names for [ItemType.FOLDER] / [ItemType.GROUP]. */
    val packages: List<String> = emptyList(),
    /** Link members (each an [ItemType.LINK]) for a [ItemType.FOLDER] / [ItemType.GROUP]. */
    val links: List<SidebarItem> = emptyList(),
) {
    companion object {
        fun app(pkg: String) = SidebarItem(ItemType.APP, packageName = pkg)
        fun link(name: String, url: String, emoji: String? = null, targetPackage: String? = null) =
            SidebarItem(ItemType.LINK, name = name, emoji = emoji, url = url, targetPackage = targetPackage)
        fun folder(name: String, packages: List<String>, emoji: String? = null, iconKey: String? = null, colorArgb: Int? = null) =
            SidebarItem(ItemType.FOLDER, name = name, emoji = emoji, iconKey = iconKey, colorArgb = colorArgb, packages = packages)
        fun group(name: String, packages: List<String>) =
            SidebarItem(ItemType.GROUP, name = name, packages = packages)
    }
}

/** Appearance + placement of the edge handle. */
@Serializable
data class HandleConfig(
    /** Show the handle. Off = open the sidebar only via the "Open sidebar"
     *  entry (gesture apps, shortcuts). */
    val visible: Boolean = true,
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
    /** Show app/link names under their icons in the panel. (New key so existing
     *  installs pick up the new default of off.) */
    @SerialName("labels")
    val showLabels: Boolean = false,
    /** App/link icon size in the panel, in dp. */
    val iconDp: Int = 42,
    /** Recolor app icons to match the system theme (Android 13+ themed icons). */
    val themedIcons: Boolean = false,
    /** Look used instead of [blurDp] on devices without hardware blur (e.g. Samsung). */
    val soft: SoftFrostConfig = SoftFrostConfig(),
)

/**
 * Software "frosted glass" for devices where the system blur is unavailable.
 * Every value is an opacity, 0 = off.
 */
@Serializable
data class SoftFrostConfig(
    /** Extra panel opacity added on top of the tint, to mute sharp content behind. */
    val mute: Float = 0.15f,
    /** Milky white haze. */
    val haze: Float = 0.08f,
    /** Wallpaper-colour tint, so the panel picks up the colour behind it. */
    val wallpaperTint: Float = 0.3f,
    /** Top-to-bottom light sheen. */
    val sheen: Float = 0.1f,
    /** Soft inner glow along the edges. */
    val glow: Float = 0.1f,
    /** Fine grain texture. */
    val grain: Float = 0.06f,
)

/** Appearance of a folder card (a "nested glass" layer inside the panel). */
@Serializable
data class FolderConfig(
    /** Show the styled tile (square) behind each folder icon. Off = bare icon. */
    val iconBackground: Boolean = false,
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

/** Appearance of an inline titled group's container (a subtle framed section). */
@Serializable
data class GroupConfig(
    /** Border stroke width in dp; 0 = none. */
    val borderDp: Float = 1f,
    /** Border visibility: a white stroke with a dark outline, at this opacity
     *  (0 = invisible, 1 = bright). New key so existing installs get the bolder default. */
    @SerialName("borderAlpha")
    val borderBrightness: Float = 0.35f,
    /** Drop-shadow elevation in dp; 0 = flat. */
    val shadowDp: Float = 0f,
    /** Inner shadow depth in dp; makes the group look sunken into the panel. 0 = flat. */
    val insetDp: Float = 0f,
    /** Corner radius in dp. */
    val cornerDp: Int = 16,
    /** Center the group title instead of left-aligning it. */
    val titleCenter: Boolean = false,
    /** Group title text opacity (matches the panel's Settings link by default). */
    val titleAlpha: Float = 0.55f,
)

/**
 * Which panel design the sidebar uses. Each design has its own panel window,
 * content and settings; the edge handle is shared.
 */
@Serializable
enum class Design {
    /** The original frosted-glass panel (needs hardware blur to look its best). */
    GLASS,
    /** The opaque "thumb rail": a folder rail with a drawer that grows out of it. */
    RAIL,
}

/** The fixed folders on the rail, top to bottom. */
@Serializable
enum class RailFolder(val title: String) {
    RECENT("Recent"),
    MEDIA("Media"),
    PRODUCTIVITY("Productivity"),
    AI("AI"),
    TOOLS("Tools"),
}

/** A titled set of apps inside a rail folder. */
@Serializable
data class RailGroup(
    val id: String,
    val title: String,
    val packages: List<String> = emptyList(),
)

/** Settings + content of the [Design.RAIL] panel. */
@Serializable
data class RailConfig(
    /** Opacity of the whole rail + drawer, 0.4..1. */
    val opacity: Float = 0.88f,
    /** Warm monochrome icons on a tinted tile instead of each app's own colours. */
    val themedIcons: Boolean = true,
    /** Folder the drawer opens on; remembers the last one picked. */
    val selected: RailFolder = RailFolder.AI,
    /** Groups per folder. [RailFolder.RECENT] uses only its first group's title;
     *  its apps come from recents. */
    val groups: Map<RailFolder, List<RailGroup>> = defaultRailGroups(),
) {
    fun groupsOf(folder: RailFolder): List<RailGroup> = groups[folder].orEmpty()

    fun withGroups(folder: RailFolder, list: List<RailGroup>): RailConfig =
        copy(groups = groups + (folder to list))
}

/** Starter groups from the design hand-off. Packages that aren't installed are
 *  simply not shown until they are. */
fun defaultRailGroups(): Map<RailFolder, List<RailGroup>> = mapOf(
    RailFolder.RECENT to listOf(RailGroup("recent-today", "Today")),
    RailFolder.MEDIA to listOf(
        RailGroup("media-watch", "Watch", listOf("com.google.android.youtube")),
        RailGroup("media-browse", "Browse & chat", listOf("com.vivaldi.browser", "com.whatsapp")),
    ),
    RailFolder.PRODUCTIVITY to listOf(
        RailGroup(
            "prod-work", "Work",
            listOf("com.Slack", "com.google.android.gm", "com.google.android.calendar", "com.samsung.android.calendar"),
        ),
        RailGroup("prod-docs", "Docs", listOf("com.google.android.apps.docs", "notion.id")),
    ),
    RailFolder.AI to listOf(
        RailGroup(
            "ai-assistants", "Assistants",
            listOf("com.openai.chatgpt", "com.google.android.apps.bard", "com.anthropic.claude"),
        ),
        RailGroup("ai-search", "Search", listOf("ai.perplexity.app.android")),
    ),
    RailFolder.TOOLS to listOf(
        RailGroup("tools-system", "System", listOf("com.android.settings", "com.android.vending")),
        RailGroup("tools-install", "Install", listOf("dev.imranr.obtainium")),
    ),
)

/** The whole persisted sidebar configuration. */
@Serializable
data class SidebarConfig(
    val design: Design = Design.GLASS,
    val rail: RailConfig = RailConfig(),
    val handle: HandleConfig = HandleConfig(),
    val panel: PanelConfig = PanelConfig(),
    val folder: FolderConfig = FolderConfig(),
    val group: GroupConfig = GroupConfig(),
    val items: List<SidebarItem> = emptyList(),
)
