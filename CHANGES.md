# Premium redesign + player upgrade

## Design
- New palette (ink black base, crimson for live/primary, warm gold for premium) - same color names, so every screen picks it up.
- Full typography scale and rounded shape system (Theme.kt, Type.kt, Color.kt).
- Slim bottom bar: icons only, the selected tab expands into a labelled pill (MainActivity.kt).
- Shared components in `ui/components/PremiumComponents.kt`: LiveBadge (pulsing), PillTabs, FilterPill, GradientButton, GlassPill, GoldTag, ShimmerBox, SectionTitle.

## Sports (SportsScreen.kt, rewritten)
- Featured match hero card, sport filter chips, Live now / Upcoming sections, local start time.
- Server buttons on every match; shimmer loading; clean empty states.

## Players
- `VideoPlayerView.kt`: tries ExoPlayer first, then forced HLS, then the in-app web player, so m3u8 / mp4 / mkv / dash / embed pages all open from one place.
- Double tap left/right = -10s/+10s, speed menu (hidden for live), fit/fill, rotate, auto landscape, immersive mode, keeps screen on, pauses when app goes to background.
- Sports servers can be switched inside the player and on the error card.
- `WebStreamPlayer.kt`: embed player with working fullscreen, autoplay, popup/ad-redirect blocking.
- `YouTubePlayerScreen.kt`: replaces the old dialog; adds "Open in YouTube" fallback.
- Music pauses automatically when a video starts.
- Added `media3-exoplayer-dash` dependency.

Not compiled here (no Android SDK in this environment): open in Android Studio and sync Gradle once.

# Build fixes
- Added missing `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar` (Gradle 9.3.1, needed by AGP 9.1.1).
- Added `debug.keystore` (it was git-ignored, so debug builds failed with "keystore not found"); removed it from app/.gitignore.
- GitHub workflow now uses `./gradlew`, creates the keystore if absent, and fails loudly if no APK is produced.
