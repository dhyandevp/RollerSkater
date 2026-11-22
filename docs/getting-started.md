# Getting Started with RollerSkater

Welcome to **RollerSkater**! This guide walks you through setting up your music library, organizing playlists, configuring audio transitions, and getting the most out of your offline listening experience.

---

## 1. First-Time Setup & Library Indexing

When you launch RollerSkater for the first time:

1. **Permission Prompts**:
   - Tap **Allow** when prompted for Audio Media permissions (`READ_MEDIA_AUDIO` on Android 13+, `READ_EXTERNAL_STORAGE` on Android 11–12).
   - Tap **Allow** for Notification permissions so playback controls display in your notification shade and lock screen.
2. **Automatic Library Scan**:
   - RollerSkater immediately performs an initial asynchronous scan of all local audio tracks cataloged by Android's MediaStore.
   - The scan runs entirely in the background via Kotlin Coroutines on `Dispatchers.IO` and batches inserts into the local Room database (`RollerSkaterDatabase`).
   - Track titles, artists, albums, durations, years, and bitrates will appear across the **Songs**, **Albums**, and **Artists** tabs.

---

## 2. Refining Your Library

### Excluding Non-Music Directories
Many apps download audio files (voice notes, podcast clips, navigation audio, ringtones) that you may not want in your music queue:
1. Open **Settings > Excluded Folders** (or navigate to **Library > Folders**).
2. Tap the **+** button or tap any folder with an eye/slash icon.
3. Select any directory (e.g. `WhatsApp/Media/WhatsApp Audio`, `Podcasts`, or `Notifications`) to exclude it.
4. Excluded folders and any directories containing a `.nomedia` file will be filtered out of all library queries and shuffle operations.

### Multi-Artist Delimiter Parsing
Collaborating artists often have combined tags (e.g. `Daft Punk; Pharrell Williams` or `Kendrick Lamar & SZA`):
1. Navigate to **Settings > Delimiters & Tag Parsing**.
2. Enable or customize your preferred separators:
   - Semicolon (`;`)
   - Forward slash (`/`)
   - Ampersand (`&`)
   - Featuring abbreviations (`feat.`, `ft.`, `featuring`)
   - Comma (`,`)
3. RollerSkater will index each collaborator independently under their own artist page, while preserving full song metadata.

---

## 3. Playback Controls & Gestures

### The Player Sheet
RollerSkater features a unified two-tier player sheet ([`UnifiedPlayerSheetV2.kt`](../app/src/main/java/com/rollerskater/app/presentation/components/UnifiedPlayerSheetV2.kt)):

| Surface | Gesture | Action |
|---|---|---|
| **Mini-Player Bar** | **Swipe Up** | Expands to full-screen player sheet. |
| **Mini-Player Bar** | **Swipe Left / Right** | Skips to next or previous track. |
| **Mini-Player Bar** | **Tap Play/Pause** | Toggles audio playback instantly. |
| **Full Player** | **Swipe Down** | Collapses back to the mini-player. |
| **Full Player** | **Swipe Left / Right** | Switches between artwork view, lyrics panel, and playback queue. |
| **Predictive Back** | **Edge Swipe Inward** | Native Android 14+ predictive back gesture collapses the player sheet with live scale preview. |

### Continuous Volume Fade & Sleep Timer
To fall asleep with music without battery drain:
1. Tap the **Three-Dot Menu (⋮)** in the top right of the player sheet and select **Sleep Timer**.
2. Select your desired duration (e.g. 15, 30, 45, 60 minutes) or set a custom time.
3. Choose **Fade Out at End** to gently decrease playback volume over the final 60 seconds.
4. RollerSkater uses `AlarmManager.setExactAndAllowWhileIdle` ([`SleepTimerAlarmScheduler.kt`](../app/src/main/java/com/rollerskater/app/data/service/SleepTimerAlarmScheduler.kt)) so the timer fires accurately even when your device enters Android Doze mode.

---

## 4. Equalizer & Audio Effects

Customize acoustic profiles across different headphones and speakers:
1. Open **Settings > Equalizer** (or tap the EQ icon in the player).
2. Toggle the hardware equalizer **ON**.
3. Select from built-in presets: *Flat*, *Acoustic*, *Bass Booster*, *Electronic*, *Rock*, *Vocal Boost*, or *Classical*.
4. Adjust individual frequency bands (5 to 10 bands depending on your device's DSP capabilities).
5. Fine-tune supplementary acoustic enhancements:
   - **Bass Boost**: Deepens low-end frequencies without audio clipping.
   - **Virtualizer**: Widens the stereo soundstage for headphone listening.
   - **Reverb Presets**: Simulates room acoustics (*Small Room*, *Medium Hall*, *Large Hall*).
   - **Custom Curves**: Save and name your own equalizer presets.

---

## 5. Synchronized Karaoke Lyrics

Enjoy line-by-line and word-level highlighted lyrics:

1. **Local Lyrics First**: RollerSkater automatically checks for embedded ID3 USLT/SYLT tags, Vorbis comments, or a `.lrc` / `.ttml` file in the same directory as the song with matching filename.
2. **Opt-In Online Lyrics (LRCLIB)**:
   - If local lyrics are unavailable, you can enable online fetching via **Settings > Lyrics Settings > LRCLIB Resolver**.
   - This feature is **100% opt-in** and contacts `https://lrclib.net/` using only the artist and track title.
3. **Manual Timing Offset**:
   - If lyrics run slightly ahead or behind the audio, tap the **clock icon** on the lyrics screen to adjust the synchronization offset in $\pm 100\text{ms}$ increments.

---

## 6. Managing Playlists & Bookmarks

- **Create a Playlist**: Tap the **+** button in the **Playlists** tab, name your playlist, and optionally assign a custom description and cover image.
- **Drag-and-Drop Reordering**: Long-press any track in a playlist to drag and reposition it.
- **M3U Export & Import**:
  - Export any playlist to standard `.m3u` / `.m3u8` format via the playlist menu.
  - Import external playlist files by opening them directly in RollerSkater or choosing **Import M3U** in the Playlists tab.
- **Audio Bookmarks**: Long audio files, DJ sets, and live recordings can be bookmarked by tapping **Add Bookmark** in track options to save your exact position and resume later.

---

## 7. Backing Up Your Data

Because RollerSkater keeps all records strictly on-device, regular backups ensure your playlists, listening stats, and equalizer curves are safe:

1. Navigate to **Settings > Backup and Restore**.
2. Select **Export Backup**.
3. Choose what to include:
   - :white_check_mark: Playlists & Playlist Tracks
   - :white_check_mark: Favorites
   - :white_check_mark: Custom Equalizer Presets
   - :white_check_mark: Listening Statistics & Play Counts
   - :white_check_mark: App Preferences & Layouts
4. Use Android's Storage Access Framework (SAF) to save the `.rsk` JSON archive to your internal storage, SD card, or external drive.
