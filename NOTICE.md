# Notice

RollerSkater is a modified fork of the GPL-3.0-or-later Android music player published as
**PixelPlayerOSS** (https://github.com/PixelPlayerHQ/PixelPlayerOSS).

## Fork Metadata
- **Upstream Source**: https://github.com/PixelPlayerHQ/PixelPlayerOSS
- **Upstream License**: GNU General Public License v3.0 (GPL-3.0)
- **Fork Commit**: `eed81d927c4e9ba6f64d370fbf061a888c4feefb` (`eed81d9`)
- **Modification Date**: 2026-11-21
- **Modified By**: Dhyandev

## Summary of Modifications
- **Rebranding & Identity**: Renamed to RollerSkater, changed Android application namespace and package ID to `com.rollerskater.app`, created new vector icon, splash screens, and green color palette.
- **Offline-First Streamlining**: Completely removed remote and self-hosted cloud music streaming integrations (Navidrome, Subsonic, Jellyfin, Telegram streaming, and cloud sync workers).
- **Recommendation & NLP Removal**: Removed Daily Mix, Smart Mix, and offline NLP playlist generator concept tables and features in favor of pure local library management.
- **App Widgets Optimization**: Re-architected and enhanced AndroidX Glance home-screen widgets for Android 11+ (API 30+) compatibility with antialiased corner smoothing fallback and memory-cached album art bitmaps.
- **Performance & Cold Startup**: Eliminated artificial coroutine delays and optimized cold startup, media query caching, and Jetpack Compose recomposition overhead.
- **Attribution & Copyleft**: Preserved historical upstream licenses, author credits, and MIT-era predecessor notices intact in `LICENSE`, `THIRD_PARTY_NOTICES.md`, and `app/src/main/assets/licenses/`.

Under GPL-3.0 Section 5, this notice and all associated license files must accompany every copy distributed, and the complete source code of any build distributed must remain available under the GNU General Public License v3.0.

