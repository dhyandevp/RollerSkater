# Changelog

All notable changes to **RollerSkater** will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.0] - 2026-11-22

### Added
- **Offline-First Core**: Fully independent, standalone on-device music player with zero telemetry, zero analytics, zero crash uploaders, and zero proprietary advertising SDKs.
- **Audio Codec Engine**: AndroidX Media3 / ExoPlayer with custom `DualPlayerEngine` providing gapless transitions, crossfade curves, and sleep timers.
- **Extended Codecs**: Integrated Jellyfin Media3 FFmpeg decoder (`org.jellyfin.media3:media3-ffmpeg-decoder`) alongside Android platform decoders for direct playback of FLAC (32-bit float PCM direct), ALAC, APE, WMA, AC-3, E-AC-3, DTS, and Opus.
- **Modern Jetpack Compose UI**: Material 3 Expressive theming, dynamic color roles, AMOLED pure black theme, and adaptive window width size class support.
- **Predictive Back Navigation**: Native Android 14+ Predictive Back gestures across player sheets, queue panes, lyrics panels, and sidebar drawers (`android:enableOnBackInvokedCallback="true"`).
- **Edge-to-Edge Experience**: Full immersive edge-to-edge layout handling system bars, display cutouts, and IME keyboard insets.
- **Home Screen Widgets**: AndroidX Glance AppWidgets with responsive layouts and on-device album art bitmap caches.
- **Listening Statistics & Recap**: Private, on-device listening history tracking top artists, albums, songs, listening habits, and annual recap story generator.
- **Hardware-Accelerated Smooth Shapes**: Native GPU-accelerated smooth corner rect geometry (`AbsoluteSmoothCornerShape`).
- **Comprehensive Localization**: Full string resources synchronized across 16 supported locales (`ar`, `cs`, `de`, `en`, `es`, `fr`, `in`, `it`, `ko`, `nb`, `pt`, `pt-BR`, `ru`, `sk`, `tr`, `zh-CN`, `vi`).
- **Backup & Restore v3**: Modular on-device backup and restore engine with atomic export/import across playlists, settings, bookmarks, and listening stats.
- **Tag & Metadata Editing**: High-performance metadata reader and tag editor backed by native TagLib bindings (`io.github.kyant0:taglib`).
- **Lyrics Engine**: Synchronized word-by-word and line-by-line lyrics rendering with TTML/LRC support and offline storage.

### Changed
- Rebranded application package namespace strictly to `com.rollerskater.app` with standalone repository structure.
- Migrated all unit test suites (1,480 tests) to JUnit 5 (JUnit Jupiter) with complete elimination of legacy JUnit 4 vintage runners.
- Upgraded Room database schema to version 8 with automated migrations (`MIGRATION_7_8`) dropping legacy pending listen tables.
- Refactored repository architecture by flattening interface indirections into unified concrete implementations.
- Enforced strict release build security with sanitized release logging (`ReleaseTree`), scoped internal storage FileProvider paths, and R8 full mode shrinking.

### Removed
- Removed all remote cloud streaming backends (Navidrome, Subsonic, Jellyfin, Telegram audio, QQ Music).
- Removed external scrobbling integrations (Last.fm, ListenBrainz).
- Removed all cloud AI, NLP smart mix, and remote recommendation services.
- Removed unused permissions (`ACCESS_LOCAL_NETWORK`, `ACCESS_FINE_LOCATION`) and set `android:usesCleartextTraffic="false"`.
- Removed dead dependencies (`kuromoji`, `pinyin4j`, `wavy-slider`, `capturable`, `accompanist-permissions`).
- Removed 274 obsolete string resources and unreferenced preview drawable assets.
