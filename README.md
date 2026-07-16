# Sidebar Launcher

A personal-use Android edge sidebar. Swipe in from a thin handle on the screen
edge and a translucent panel springs out with a grid of your installed apps —
tap one to launch it. Native Kotlin + Jetpack Compose (Material 3, Material You
dynamic color). No Play Store, no Android Studio required.

## How it works

- **Trigger:** a tiny translucent *edge handle* — one small overlay window that
  renders once and sits idle until you touch it. A tap or an inward swipe opens
  the panel. Nothing polls, nothing runs on a timer.
- **Panel:** built on demand as a `TYPE_APPLICATION_OVERLAY` window hosting a
  `ComposeView` (with a custom `LifecycleOwner` / `SavedStateRegistryOwner` /
  `ViewModelStoreOwner`, since overlay windows don't get those for free). It
  animates in with a spring, and is **torn down completely on dismiss** — no
  Compose UI stays mounted while hidden.
- **App list:** `PackageManager.queryIntentActivities()` for everything with a
  launcher entry; launched via each app's launch intent.
- **Host:** a foreground service keeps the idle handle alive across OEM battery
  killers. It holds no wakelocks and does no background work — the foreground
  status is only so the window isn't reclaimed.

### Battery

Event-driven by construction: one idle handle window, panel inflated only on a
trigger and destroyed on dismiss, zero polling/timers.

## Customizing

Everything is configured from the app's home screen and applies live (the
running handle re-arms on each change):

- **Handle appearance** — side (left/right), color (swatches + opacity), width,
  length, and vertical position along the edge, with a live preview.
- **Curated apps** — the panel is **empty by default**; you choose which apps
  appear via **Add apps** (a searchable, multi-select list). It never dumps
  every installed app on you.
- **Folders** — group apps with **New folder**: give it a name, pick members.
  In the panel a folder is a nested-glass card (more opaque than the panel, with
  a bright edge + shadow for depth) that expands to a 3-column grid; opening one
  dims the rest of the panel to focus on it. Edit/delete folders from settings.
- **Reorder** — long-press the drag handle on any item in the settings list to
  drag it up or down; the new order is what the panel shows.
- **Panel & glass (the lab)** — all panel + folder look controls in one screen
  (Appearance → **Panel & glass…**), each wired to a live preview: panel tint
  opacity, brightness (near-black → white), edge stroke, background colour/dim;
  and folder opacity, brightness, edge, columns and corner radius. Changes apply
  on back. Effects stay **inside the panel** — a translucent frosted tint rather
  than a real backdrop blur, because a per-window blur can't be confined to an
  overlay's bounds on Android (the platform blur covers the whole screen).

Config is stored as JSON in SharedPreferences (`kotlinx.serialization`).

## Install on your phone (via Obtainium)

Every push builds an APK in GitHub Actions and publishes it as a new
versioned release (tag **`v1.0.<build>`**) marked as the repo's *latest*
release. Obtainium reads the tag as the version, so each build is a genuine
version bump it can detect. The "latest" flag also gives one stable URL:

```
https://github.com/amihaioff/android-sidebar/releases/latest/download/sidebar-latest.apk
```

1. Install [Obtainium](https://github.com/ImranR98/Obtainium).
2. In Android settings, allow Obtainium to **install unknown apps**.
3. In Obtainium, **Add App** → paste this repo URL:
   `https://github.com/amihaioff/android-sidebar`
   (Obtainium reads the GitHub releases and picks up `sidebar-latest.apk`.)
4. Tap **Install**. On later builds Obtainium shows an update — tap **Update**.

Because every APK is signed with the **same committed debug key**, new builds
install straight over the old one as updates, so permissions are granted **once,
ever** and persist across iterations.

## One-time permission setup

Launch the app once. The onboarding screen deep-links to each toggle and shows
live status:

1. **Draw over other apps** — *required*. Lets the handle and panel appear on
   top of everything.
2. **Notifications** — *optional* (Android 13+). Permits the silent ongoing
   notification the foreground service posts.
3. **Ignore battery optimization** — *recommended*. Stops aggressive OEMs from
   killing the handle.

Then pick the **handle side** (left/right) and flip **Enable sidebar** on. It
re-arms automatically after a reboot or an app update.

## Iteration loop

```
change code → push → CI builds (~1–2 min) → Obtainium shows update → tap Update
```

Permissions are already granted (stable signature), so it's about as close to
"click Run" as it gets without a computer.

## Build locally (optional)

Java 21 + the Android SDK (platform 35, build-tools 35) required. Point Gradle
at your SDK via `local.properties` (`sdk.dir=/path/to/android-sdk`) or the
`ANDROID_HOME` env var, then:

```bash
./gradlew :app:assembleDebug
# → app/build/outputs/apk/debug/app-debug.apk
```

## Signing key

`keystore/debug.keystore.base64` is a committed base64-encoded debug keystore.
`app/build.gradle.kts` decodes it to `keystore/debug.keystore` at configuration
time and signs every build type with it. This is intentional for a personal,
non-store app: one fixed signature means seamless in-place updates. Do not reuse
this key for anything you publish.

## Project layout

```
app/src/main/java/com/personal/sidebar/
├─ MainActivity.kt            # home: permissions, appearance controls, items, enable
├─ ManageScreens.kt          # app picker + folder editor (searchable multi-select)
├─ SidebarApp.kt              # Application; notification channel
├─ Settings.kt               # enabled flag + JSON-persisted SidebarConfig
├─ model/SidebarConfig.kt     # handle appearance + curated apps/folders model
├─ apps/AppRepository.kt      # query + launch installed apps (cached)
├─ overlay/
│  ├─ EdgeHandle.kt           # the idle trigger strip
│  ├─ PanelController.kt      # inflate/tear-down the panel window
│  └─ OverlayViewHost.kt      # Lifecycle/SavedState/ViewModelStore for overlays
├─ service/
│  ├─ SidebarService.kt       # foreground host for the handle + panel
│  └─ BootReceiver.kt         # re-arm after reboot / app update
├─ ui/
│  ├─ SidebarPanel.kt         # the Compose panel (scrim + spring card + grid)
│  └─ theme/Theme.kt          # Material 3 + dynamic color
└─ util/Permissions.kt        # permission checks + settings intents
```
