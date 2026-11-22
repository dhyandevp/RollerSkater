# RollerSkater — Troubleshooting & Diagnostic Guide

This guide helps diagnose and resolve common issues related to playback, library scanning, hardware effects, permissions, and OEM battery optimizations.

---

## 1. Quick Diagnostic Checklist

If you encounter unexpected behavior:
1. Ensure you are running the latest version from [GitHub Releases](https://github.com/dhyandevp/RollerSkater/releases).
2. Check that **RollerSkater** has **Audio & Media** permissions granted (**Settings > Apps > RollerSkater > Permissions**).
3. Try triggering a manual library rescan (**Settings > Library > Rescan Library**).

---

## 2. Playback & Audio Engine Issues

### Playback Stops When Screen Turns Off or Device Enters Sleep
- **Root Cause**: Many Android device manufacturers (notably Samsung, Xiaomi, OnePlus, and Huawei) aggressively terminate foreground audio services when the screen locks to conserve battery.
- **Resolution**:
  1. Open Android **Settings > Apps > RollerSkater**.
  2. Tap **Battery** (or *App Battery Usage*).
  3. Select **Unrestricted** (or disable *MIUI Battery Saver* / *Put unused apps to sleep*).
  4. Ensure RollerSkater's notification is not dismissed from the notification tray.

### Audio Freezes or Stutters (Audio Offload DSP Stalls)
- **Root Cause**: Certain OEM audio DSP drivers exhibit Audio HAL stalls where hardware offload audio timestamps freeze during sample rate changes or high bit-depth files.
- **RollerSkater's Automatic Mitigation**: RollerSkater includes an automated stall detector in [`DualPlayerEngine.kt`](../app/src/main/java/com/rollerskater/app/data/service/player/DualPlayerEngine.kt) that detects audio driver freezes after 1,500 ms and automatically falls back to standard software PCM decoding.
- **Manual Override**: If your device continues to stutter, navigate to **Settings > Audio Engine** and toggle **Hardware Audio Offload** to **OFF**.

### Equalizer or Bass Boost Has No Audible Effect
- **Root Cause**: System-wide audio sound engines (such as Samsung SoundAlive / Dolby Atmos, Xiaomi Sound Enhancement, or Dirac HD Sound) often override third-party audio effect sessions.
- **Resolution**:
  1. In RollerSkater's Equalizer screen, verify that the master toggle is **ON**.
  2. Open Android **Settings > Sounds and vibration > Sound quality and effects** and set system Dolby Atmos to **Auto** or **Off** to prevent audio session hijacking.
  3. Note: Bit-perfect USB DAC output routes raw PCM directly to external hardware, bypassing Android's software equalizer pipeline.

---

## 3. Library & Metadata Indexing

### Audio Files Not Appearing in Library
- **Step 1: Check Permissions**:
  - Android 13+: Ensure `READ_MEDIA_AUDIO` is granted.
  - Android 11–12: Ensure `READ_EXTERNAL_STORAGE` is granted.
- **Step 2: Check for `.nomedia` Files**:
  - If a directory (or any parent folder) contains an invisible file named `.nomedia`, Android's MediaStore intentionally skips indexing all files in that directory.
  - Use a file manager to view hidden files and delete `.nomedia` from your music folders.
- **Step 3: Check Excluded Directories**:
  - Open **Settings > Excluded Folders** in RollerSkater and verify that your music directory was not accidentally blacklisted.
- **Step 4: DRM / Streaming Cache**:
  - RollerSkater plays standard local audio files. DRM-protected files (e.g. `.m4p` from Apple Music or encrypted cache files from Spotify/Tidal) cannot be decoded.

### Missing Album Artwork
- **Embedded Artwork**: Ensure tracks have valid APIC (ID3v2) or METADATA_BLOCK_PICTURE (FLAC) chunks. You can inspect or embed artwork using RollerSkater's built-in Tag Editor.
- **Folder Cover Images**: If your music uses external images (`cover.jpg`, `folder.jpg`, `albumart.jpg`), verify that **Folder Cover Images** is enabled under **Settings > Library > Album Artwork**. On Android 13+, this will request `READ_MEDIA_IMAGES` to read image files in the same directory.

### Multiple Artists Combined Into One
- **Resolution**: Navigate to **Settings > Delimiters & Tag Parsing** and enable your library's separator format (e.g. `;`, `/`, `&`, `feat.`, `ft.`, or `,`). RollerSkater will re-index collaborators into their own dedicated artist pages.

---

## 4. Installation & Upgrades

### "App Not Installed as Package Conflicts with an Existing Package"
- **Root Cause**: Android enforces signature parity. You cannot install an F-Droid build over a GitHub build (or a locally compiled self-signed debug build) without uninstalling first because their cryptographic signing keys differ.
- **Resolution**:
  1. Open RollerSkater, go to **Settings > Backup and Restore > Export Backup**, and save your `.rsk` file to storage.
  2. Uninstall the existing version.
  3. Install the new release.
  4. Restore your data via **Settings > Backup and Restore > Import Backup**.

---

## 5. Capturing Logs for Bug Reports

If you experience an unhandled crash or reproducible issue, capturing a sanitized logcat provides valuable diagnostic data:

```bash
# Connect device with USB debugging enabled
adb logcat -c  # Clear existing buffer
adb logcat -v time RollerSkater:V MusicService:V *:E > rollerskater-debug.log
```

Review the output to ensure no private folder names or personal credentials are included, then attach it to your [GitHub Issue](https://github.com/dhyandevp/RollerSkater/issues).
