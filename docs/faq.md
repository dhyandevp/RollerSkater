# Frequently Asked Questions (FAQ)

Frequently asked questions regarding **RollerSkater**'s architecture, offline privacy, audio engine, format support, and features.

---

## 1. General & Privacy

### What is RollerSkater?
RollerSkater is an independent, offline-first Android music player built with modern Android engineering standards: Kotlin 2.x, Jetpack Compose, Material 3 Expressive theming, AndroidX Media3 / ExoPlayer, Room SQLite database, and Dagger Hilt.

### Does RollerSkater collect my listening data, track analytics, or upload crash logs?
**No. Absolutely zero.** RollerSkater operates 100% on-device by default. There are no tracking SDKs, no advertising libraries, no telemetry, no Google Analytics, and no Firebase Crashlytics. All listening statistics, playlists, favorites, and metadata reside exclusively in your device's local SQLite database.

### Is RollerSkater open source?
Yes. RollerSkater is free software licensed under the **GNU General Public License v3.0 (GPL-3.0)**. The complete source code is public, and all bundled third-party dependencies are documented in [`docs/DEPENDENCY_LICENSES.md`](DEPENDENCY_LICENSES.md).

---

## 2. Playback & Audio Formats

### What audio formats and codecs are supported?
RollerSkater decodes standard platform formats alongside extended codecs via the bundled Jellyfin Media3 FFmpeg software decoder:
- **Lossless**: **FLAC** (including 32-bit float direct PCM), **ALAC** (Apple Lossless in `.m4a`), **WAV** (PCM 8/16/24/32-bit, IEEE Float), **AIFF**, and **APE** (Monkey's Audio).
- **Lossy**: **MP3**, **AAC** (AAC-LC, HE-AAC v1/v2), **Ogg Vorbis**, **Opus**, **WMA** (Windows Media Audio 1/2/Pro/Lossless), **AC-3** (Dolby Digital), and **DTS**.

### What is the `DualPlayerEngine`?
Standard single-player audio engines often encounter audio dropouts or sample rate mismatches when transitioning between different audio files. RollerSkater's [`DualPlayerEngine.kt`](../app/src/main/java/com/rollerskater/app/data/service/player/DualPlayerEngine.kt) maintains two synchronized ExoPlayer instances (`Player A` master and `Player B` auxiliary) to achieve seamless, sample-exact equal-power crossfading and gapless transitions across mixed sample rates and bit depths.

### Does RollerSkater support gapless playback?
Yes. Gapless playback is enabled by default for all supported lossless formats (FLAC, ALAC, WAV) and lossy formats with encoder delay and padding tags (properly encoded MP3 and AAC).

### What is ReplayGain and how does it work?
ReplayGain normalizes playback volume across albums and tracks so you don't have to adjust your volume between songs with differing master loudness. RollerSkater reads embedded `REPLAYGAIN_TRACK_GAIN` and `REPLAYGAIN_ALBUM_GAIN` metadata tags in real time through its custom [`ReplayGainProcessor.kt`](../app/src/main/java/com/rollerskater/app/data/service/ReplayGainProcessor.kt).

---

## 3. Lyrics & Metadata

### Where does RollerSkater get lyrics?
1. **Local Files First**: RollerSkater prioritizes embedded ID3 USLT/SYLT tags, Vorbis comments, or standalone `.lrc` and `.ttml` files located in the same folder as the song with matching filename.
2. **Opt-In Online Lyrics (LRCLIB)**: If local lyrics are missing and you have explicitly enabled **LRCLIB Resolver** in Settings, the app fetches missing lyrics on demand from `https://lrclib.net/`.

### Can I edit tags and embed album covers directly in the app?
Yes. RollerSkater includes a built-in Tag Editor powered by native C++ TagLib bindings (`io.github.kyant0:taglib`) and JAudioTagger. You can modify Title, Artist, Album Artist, Album, Year, Track, Genre, Composer, and embed custom album artwork directly into the file.

### Can I export and import my playlists?
Yes. Playlists can be exported to standard `.m3u` / `.m3u8` playlist files via the playlist screen options menu. You can also import existing M3U playlists from other players.

---

## 4. Hardware & Battery

### Why does RollerSkater recommend exempting itself from battery optimization?
Modern Android operating systems (especially Samsung OneUI, Xiaomi HyperOS, and OnePlus OxygenOS) aggressively terminate background audio services when the screen locks. Exempting RollerSkater ensures your music continues playing smoothly when the screen turns off and that the Sleep Timer fires accurately during deep Doze states.

### Does RollerSkater support Android Auto?
Yes. RollerSkater integrates with AndroidX Media3's `MediaSessionService`, making your local library, queue, playlists, and playback controls available via Android Auto and Bluetooth media metadata displays.

### How do I move my playlists and listening statistics to a new phone?
1. On your old device: Go to **Settings > Backup and Restore > Export Backup** and save the `.rsk` JSON archive.
2. Transfer the `.rsk` file to your new device.
3. On your new device: Install RollerSkater, scan your music library, and select **Settings > Backup and Restore > Import Backup**. RollerSkater will automatically reconcile your playlists, favorites, listening stats, and custom EQ presets with your new file paths.
