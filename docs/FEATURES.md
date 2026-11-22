# RollerSkater — Verified Feature Reference

This document catalogs every user-facing feature in **RollerSkater** (`com.rollerskater.app`), verified directly against the Kotlin implementation in `app/src/main/java/com/rollerskater/app/`.

---

## 1. Precision Playback & Audio Engine

| Feature | Primary Code Location | Description |
|---|---|---|
| **Dual-Player Engine** | `data/service/player/DualPlayerEngine.kt` | Orchestrates two independent ExoPlayer instances (`Player A` master and `Player B` auxiliary) to allow simultaneous decoding and gapless transitions across differing sample rates and formats without audio dropouts. |
| **Parametric Crossfading** | `presentation/screens/EditTransitionScreen.kt`<br>`data/repository/TransitionRepositoryImpl.kt` | Configurable crossfade duration (0.0s to 15.0s) utilizing an equal-power trigonometric curve ($\text{Vol}_{\text{in}} = \sin(\frac{\pi}{2}t)$, $\text{Vol}_{\text{out}} = \cos(\frac{\pi}{2}t)$) to maintain constant sound energy. |
| **Custom Transition Rules** | `data/database/TransitionRuleEntity.kt`<br>`data/database/TransitionDao.kt` | Define specific transition rules between distinct tracks, artists, or albums that override the global crossfade setting. |
| **Extended Codec Support** | `data/service/player/AudioDecoderPolicy.kt` | FFmpeg audio decoder extension (`org.jellyfin.media3:media3-ffmpeg-decoder`) alongside Android platform decoders. Direct decoding for FLAC (32-bit float PCM direct), ALAC, APE, WMA, AC-3, E-AC-3, DTS, DTS-HD, and Opus. |
| **Audio Offload & Fallback** | `data/service/player/DualPlayerEngine.kt` | Low-power DSP audio offload for battery preservation during screen-off listening. Features an automated 1,500ms stall detector that gracefully falls back to PCM rendering if device HAL drivers freeze. |
| **Graphic Equalizer** | `presentation/screens/EqualizerScreen.kt`<br>`data/equalizer/EqualizerManager.kt` | Hardware-backed graphic equalizer (5 to 10 bands depending on device HAL), Bass Boost, Audio Virtualizer, Reverb presets, and custom user-defined EQ curves. |
| **ReplayGain & Volume Normalization** | `data/service/player/ReplayGainAudioProcessor.kt` | Parses track and album ReplayGain tags (`REPLAYGAIN_TRACK_GAIN`, `REPLAYGAIN_ALBUM_GAIN`) to prevent sudden volume spikes between tracks. |
| **Surround Downmixing** | `data/service/player/SurroundDownmixProcessor.kt` | Downmixes 5.1 multi-channel audio tracks into balanced stereo audio for headphones. |
| **Interactive DJ Mashup Deck** | `presentation/screens/MashupScreen.kt` | Dual-deck interactive crossfader and BPM-style tempo mixer for blending two tracks live. |
| **Sleep Timer** | `data/service/SleepTimerAlarmScheduler.kt`<br>`data/service/SleepTimerReceiver.kt` | System-level timer with gradual volume fade-out. Employs `AlarmManager.setExactAndAllowWhileIdle` to fire reliably even when device is in deep Doze mode. |
| **Playback Speed & Pitch** | `presentation/components/UnifiedPlayerSheetV2.kt` | Fine-tune playback speed (0.5x to 2.0x) and pitch controls. |

---

## 2. Library Organization & Navigation

| Feature | Primary Code Location | Description |
|---|---|---|
| **Media Tabs** | `presentation/screens/LibraryScreen.kt`<br>`presentation/screens/LibraryMediaTabs.kt` | Seven top-level library sections: **Songs**, **Albums**, **Artists**, **Genres**, **Playlists**, **Folders**, and **Favorites**. Backed by AndroidX Paging 3 for instant virtualized scrolling. |
| **Folder Browser** | `data/repository/FolderTreeBuilder.kt` | Real-time filesystem directory tree navigation. Automatically respects `.nomedia` markers and directory exclusions. |
| **Directory Exclusion** | `presentation/screens/ExcludedDirectoriesScreen.kt` | Blacklist specific directories or system audio folders (e.g. WhatsApp audio, ringtones) from scanning. |
| **Multi-Artist Delimiter Parsing** | `presentation/screens/DelimiterConfigScreen.kt`<br>`presentation/screens/WordDelimiterConfigScreen.kt` | Configurable tag delimiters (`;`, `/`, `&`, `feat.`, `ft.`, `,`) to properly isolate collaborating artists into individual library profiles. |
| **Duplicate Song Detector** | `presentation/screens/DuplicateSongsScreen.kt` | Identifies duplicate audio files across local storage by duration, title matching, and file size to help free up storage space. |
| **Recently Played History** | `presentation/screens/RecentlyPlayedScreen.kt` | Chronological listening ledger recording every track played on-device. |
| **Favorites Management** | `data/database/FavoritesDao.kt`<br>`data/database/RollerSkaterDatabase.kt` | One-tap track favoriting with automated SQLite triggers synchronizing `is_favorite` flags across the database. |

---

## 3. Search & Fast Discovery

| Feature | Primary Code Location | Description |
|---|---|---|
| **Full-Text Search (FTS4)** | `data/database/SongSearchFtsEntity.kt`<br>`data/database/MusicDao.kt` | High-performance full-text search indexing title, artist, and album tags using SQLite FTS4 with the `unicode61` tokenizer. |
| **Search UI** | `presentation/screens/SearchScreen.kt` | Instant search results with highlighted matches across songs, albums, and artists. |
| **Genre Categories Grid** | `presentation/screens/search/components/GenreCategoriesGrid.kt` | Visual discovery tiles categorizing local music by genre with dynamic typography. |
| **Search History** | `data/database/SearchHistoryDao.kt` | Persistent recent search queries with individual and bulk removal. |

---

## 4. Metadata Inspection & Tag Editing

| Feature | Primary Code Location | Description |
|---|---|---|
| **Direct File Tag Editing** | `data/media/SongMetadataEditor.kt` | File-level metadata editing backed by native TagLib (`libtaglib.so`), JAudioTagger, and Vorbis-Java. Supports editing Title, Artist, Album Artist, Album, Year, Track Number, Disc Number, Genre, Composer, and Lyrics. |
| **Technical Track Inspector** | `presentation/viewmodel/SongInfoBottomSheetViewModel.kt` | Detailed audio metrics sheet displaying bitrate, sample rate, bit depth, codec MIME type, filesystem path, file size, and modified date. |
| **MusicBrainz Lookup** | `data/musicbrainz/MusicBrainzRepository.kt`<br>`data/musicbrainz/MusicBrainzApiService.kt` | Search MusicBrainz (`musicbrainz.org`) directly from the track info sheet to retrieve verified MBIDs, track numbers, and release dates. |
| **Set as Ringtone / Alarm** | `presentation/viewmodel/SongInfoBottomSheetViewModel.kt` | One-tap shortcut to assign any local track as the system ringtone, alarm, or notification tone via `Settings.ACTION_MANAGE_WRITE_SETTINGS`. |

---

## 5. Synchronized Lyrics Engine

| Feature | Primary Code Location | Description |
|---|---|---|
| **Embedded & External Lyrics** | `data/repository/LyricsRepositoryImpl.kt` | Reads embedded ID3 USLT/SYLT tags, Vorbis comment lyrics, local `.lrc` files, and `.ttml` timed text files. |
| **Word-Level Highlights** | `presentation/screens/LyricsSettingsScreen.kt`<br>`presentation/components/player/FullPlayerContent.kt` | Karaoke-style word-by-word active highlighting with fluid auto-scroll synchronization. |
| **Manual Timing Offset** | `presentation/components/player/FullPlayerContent.kt` | Millisecond-level manual timing offset adjustment (+/- buttons) to perfectly align lyrics with audio. |
| **Online LRCLIB Sync (Opt-In)** | `data/network/lyrics/LrcLibApiService.kt` | Optional network provider fetching missing lyrics from `https://lrclib.net/` (disabled by default; 100% opt-in). |

---

## 6. Playlists & Smart Management

| Feature | Primary Code Location | Description |
|---|---|---|
| **Custom Playlists** | `presentation/screens/PlaylistDetailScreen.kt`<br>`presentation/screens/CreatePlaylistScreen.kt` | Create and customize playlists with custom names, descriptions, and user-selected artwork. |
| **Drag-and-Drop Reordering** | `presentation/screens/PlaylistDetailScreen.kt` | Fluid touch drag-to-reorder track queues powered by `sh.calvin.reorderable`. |
| **M3U Export & Import** | `data/playlist/M3uManager.kt` | Export local playlists to standard `.m3u` / `.m3u8` playlist files; import external playlists with path resolution. |
| **Quick Fill Smart Playlists** | `presentation/screens/QuickFillScreen.kt` | Generate playlists automatically based on rules (top played, newly added, specific decades, or genres). |

---

## 7. Audio Bookmarks

| Feature | Primary Code Location | Description |
|---|---|---|
| **Timestamp Bookmarking** | `data/database/AudioBookmarkEntity.kt`<br>`presentation/screens/AudioBookmarksScreen.kt` | Save precise timestamp markers inside long audio files (DJ sets, live recordings, podcasts, audiobooks) with custom labels. |
| **Bookmark Jump & Resume** | `data/repository/AudioBookmarkRepositoryImpl.kt` | One-tap resumption directly from the Audio Bookmarks library screen. |

---

## 8. Listening Statistics & Recap

| Feature | Primary Code Location | Description |
|---|---|---|
| **Private On-Device Analytics** | `data/database/SongEngagementEntity.kt`<br>`presentation/screens/StatsScreen.kt` | Tracks play counts, skip counts, completion percentages, total hours listened, and favorite artists without external servers. |
| **Listening Recap** | `presentation/recap/` | Interactive summary cards celebrating top artists, top albums, and listening habits. |
| **Story Card Sharing** | `presentation/recap/` | Export high-resolution recap summary story cards to image files via `dev.shreyaspatil:capturable`. |

---

## 9. Personalization & Appearance

| Feature | Primary Code Location | Description |
|---|---|---|
| **Material You Dynamic Theming** | `presentation/screens/PaletteStyleSettingsScreen.kt`<br>`ui/theme/Theme.kt` | Extracts color palettes dynamically from album art via `androidx.palette` to style UI components. |
| **OLED Pure Black Mode** | `data/preferences/UserPreferencesRepository.kt` | Pure `#000000` AMOLED theme maximizing contrast and power efficiency. |
| **Navigation Bar Customization** | `presentation/screens/NavBarCornerRadiusScreen.kt` | Adjust bottom navigation bar rounding and pill corner radius. |
| **Wavy Seekbar** | `app/build.gradle.kts` (`ir.mahozad.multiplatform:wavy-slider`) | Animated wavy progress seekbar on the full-screen player. |

---

## 10. Android System Integration & Widgets

| Feature | Primary Code Location | Description |
|---|---|---|
| **Glance Home-Screen Widgets** | `ui/glancewidget/`<br>`docs/WIDGET_ARCHITECTURE.md` | 4 Material 3 widgets: 4×1 Bar (`BarWidget4x1`), 4×2 Controls (`ControlWidget4x2`), 2×2 Album Grid (`GridWidget2x2`), and Resizable Adaptive Glance. |
| **Android 11 Widget Fallback** | `ui/glancewidget/WidgetUtils.kt` | Antialiased corner smoothing fallback using `BitmapShader` and `LruCache` for Android 11 (API 30). |
| **Quick Settings Tiles** | `data/service/tile/ShuffleAllTileService.kt`<br>`data/service/tile/LastPlaylistTileService.kt` | System Quick Settings tiles to instantly shuffle all songs or resume the last playlist. |
| **System Media Controls** | `data/service/MusicNotificationProvider.kt` | Standard Media3 notification shade controls, lockscreen seekbars, and Bluetooth car integration. |

---

## 11. Backup & Data Portability

| Feature | Primary Code Location | Description |
|---|---|---|
| **JSON Export & Restore** | `data/backup/BackupCoordinator.kt`<br>`docs/BACKUP_AND_RESTORE.md` | Modular on-device backup of playlists, favorites, equalizer presets, listening stats, bookmarks, and preferences via Storage Access Framework (SAF). |
| **Fuzzy Relinking** | `data/backup/BackupCoordinator.kt` | Automatically relinks playlist tracks if file IDs changed following an SD card replacement or device transfer. |
