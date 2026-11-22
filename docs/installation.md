# RollerSkater — Installation & Setup Guide

This guide covers all official distribution channels, installation procedures, integrity verification, and initial permission configurations for **RollerSkater**.

---

## 1. System Requirements

| Requirement | Minimum | Recommended |
|---|---|---|
| **Operating System** | Android 11 (API level 30) | Android 13–15+ (API 33–35+) |
| **CPU Architecture** | `arm64-v8a` (64-bit ARM) or `armeabi-v7a` (32-bit ARM) | `arm64-v8a` |
| **Free Storage** | ~35 MB for installation | ~100 MB (includes artwork cache) |
| **RAM** | 2 GB | 4 GB or higher |
| **Google Play Services** | **Not Required** (100% Google-free) | Not Required |

---

## 2. Official Download Channels

RollerSkater is distributed exclusively through open-source and direct release channels.

### Channel A: GitHub Releases (Recommended for Immediate Updates)
Download the latest signed release APK from the official repository:
- **Repository**: [https://github.com/dhyandevp/RollerSkater/releases](https://github.com/dhyandevp/RollerSkater/releases)
- **Artifacts Provided per Release**:
  - `RollerSkater-vX.Y.Z-arm64-v8a.apk`: Optimized for modern 64-bit Android smartphones and tablets (smaller footprint, faster native TagLib performance).
  - `RollerSkater-vX.Y.Z-armeabi-v7a.apk`: Compatible with older 32-bit Android devices.
  - `RollerSkater-vX.Y.Z-universal.apk`: Contains all native architectures in a single package.
  - `checksums.txt`: SHA-256 hashes for cryptographic verification.

### Channel B: F-Droid (Open Source Catalog)
RollerSkater's metadata recipe is maintained in `metadata/com.rollerskater.app.yml`. Once the package review is indexed on F-Droid, builds are compiled directly from source tags in clean, reproducible environments.

### Channel C: Building from Source
If you prefer building your own APK:
```bash
git clone https://github.com/dhyandevp/RollerSkater.git
cd RollerSkater
./gradlew :app:assembleRelease -Prollerskater.disableReleaseSigning=true
# Generated APK: app/build/outputs/apk/release/app-release-unsigned.apk
```

---

## 3. Sideloading Instructions

### Sideloading via Android Browser or File Manager
1. Download the appropriate `.apk` file (typically `arm64-v8a`) to your device.
2. Tap the downloaded file in your browser or notification drawer.
3. If prompted by Android, grant **"Install unknown apps"** to your browser or file manager:
   - Go to **Settings > Apps > Special app access > Install unknown apps**.
   - Enable **Allow from this source**.
4. Tap **Install** and wait for the system package manager to complete.

### Installing via Android Debug Bridge (ADB)
Connect your device to a computer with USB debugging enabled:

```bash
# Verify your device is connected
adb devices

# Install modern 64-bit build (replaces existing install if signatures match)
adb install -r RollerSkater-v1.0.0-arm64-v8a.apk

# For older 32-bit devices
adb install -r RollerSkater-v1.0.0-armeabi-v7a.apk
```

---

## 4. Cryptographic Verification & Integrity Checks

Always verify that your downloaded binary has not been tampered with in transit.

### Checking SHA-256 Checksum
On Linux / macOS:
```bash
# Verify against published checksums.txt
sha256sum -c checksums.txt

# Or compute manual checksum
sha256sum RollerSkater-v1.0.0-arm64-v8a.apk
```
On Windows PowerShell:
```powershell
Get-FileHash -Algorithm SHA256 .\RollerSkater-v1.0.0-arm64-v8a.apk
```

### Verifying APK Signature with `apksigner`
The official Android SDK `apksigner` tool verifies APK v2/v3 signatures:
```bash
apksigner verify --verbose --print-certs RollerSkater-v1.0.0-arm64-v8a.apk
```
Expected output:
```text
Verifies
Verified using v1 scheme (JAR signing): false
Verified using v2 scheme (APK Signature Scheme v2): true
Verified using v3 scheme (APK Signature Scheme v3): true
Number of signers: 1
```

---

## 5. Post-Installation Permissions & First Launch

When you first launch RollerSkater, the app will request runtime permissions necessary for local playback:

1. **Audio Storage Access**:
   - **Android 13+ (API 33+)**: Prompts for `READ_MEDIA_AUDIO`. This gives RollerSkater permission to index songs, artists, and albums from Android's MediaStore.
   - **Android 11–12 (API 30–32)**: Prompts for `READ_EXTERNAL_STORAGE`.
2. **Notifications (Android 13+)**:
   - Prompts for `POST_NOTIFICATIONS` so that playback controls and album art display in the lock screen and notification shade.
3. **Battery Optimization Exemption (Recommended)**:
   - Many OEMs (Samsung OneUI, Xiaomi MIUI/HyperOS, OnePlus OxygenOS) aggressively terminate background audio services.
   - During initial setup, RollerSkater offers a prompt to exempt the app from battery killing. You can also configure this manually via **Settings > Apps > RollerSkater > Battery > Unrestricted**.

---

## 6. Upgrades & Release Channels

- **Upgrading Over Existing Installs**: New releases can be installed directly over existing versions without data loss, provided the APK signature matches.
- **Signature Conflicts**: If you switch from a GitHub release to an F-Droid build (or a locally compiled self-signed build), Android's package manager will block installation due to differing signing keys. In this situation:
  1. Back up your playlists, favorites, and listening stats via **Settings > Backup and Restore > Export Backup**.
  2. Uninstall the old version.
  3. Install the new build.
  4. Restore your data via **Settings > Backup and Restore > Import Backup**.
