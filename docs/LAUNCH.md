# RollerSkater 1.0.0 — Launch & Release Guide

This document outlines the official release procedure, signing protocols, verification checksums, and distribution pipelines for **RollerSkater** (`com.rollerskater.app`).

---

## 1. Release Overview

- **Application Name**: RollerSkater
- **Application ID**: `com.rollerskater.app`
- **Release Version**: `1.0.0`
- **Version Code**: `1`
- **Target SDK**: Android 15 / API 35 (`compileSdk 37`, `minSdk 30`)
- **License**: GNU General Public License v3.0 (GPL-3.0)

---

## 2. Release Candidate Verification Checklist

Before publishing any build to distribution channels, ensure every quality gate is satisfied:

- [x] **Gate 1: Scope & Rebrand Complete**
  - Zero references to upstream cloud services (Navidrome, Jellyfin streaming, Daily Mix, cloud AI).
  - Clean package namespace: `com.rollerskater.app`.
  - 16 synchronized locales with 0 missing strings.
- [x] **Gate 2: Clean Builds & Dependency Audit**
  - Deprecated dependencies purged (`kuromoji`, `pinyin4j`, `wavy-slider`, `capturable`, `accompanist-permissions`).
  - Room database schema upgraded to Version 8 with migration `MIGRATION_7_8`.
  - Zero machine-specific absolute paths baked into output artifacts.
- [x] **Gate 3: Automated Test Suite**
  - 100% of unit tests migrated to JUnit 5 (Jupiter).
  - Deprecated JUnit 4 vintage runner eliminated.
  - 1,480 unit tests executed: **0 failures, 0 errors, 100% pass rate**.
- [x] **Gate 4: Runtime Performance & Battery**
  - StrictMode disk and network violation detection enabled in debug.
  - Startup baseline documented in `docs/perf/baseline.md`.
  - Hardware-accelerated smooth corner geometries via `AbsoluteSmoothCornerShape`.
- [x] **Gate 5: Platform Compliance**
  - Edge-to-edge layout enabled (`enableEdgeToEdge()`).
  - Predictive back navigation enabled (`android:enableOnBackInvokedCallback="true"`).
  - Foreground service type declared as `mediaPlayback`.
  - Dynamic Material You and themed monochrome icons verified.
- [x] **Gate 6: Security & Privacy**
  - Cleartext network traffic explicitly prohibited (`android:usesCleartextTraffic="false"`).
  - FileProvider paths strictly scoped to internal storage (`file_paths.xml`).
  - Production logging sanitized with regex filtering in `ReleaseTree`.
  - R8 full mode shrinking and resource optimization enabled.
- [x] **Gate 7: Release Engineering**
  - Keystore configuration template provided (`keystore.properties.example`).
  - GitHub Actions CI release workflow verified.
- [x] **Gate 8: Documentation, Legal & Metadata**
  - `CHANGELOG.md` adhering to Keep a Changelog specification.
  - Fastlane store metadata configured in `fastlane/metadata/android/en-US/`.
  - F-Droid build recipe defined in `metadata/com.rollerskater.app.yml`.
  - Legal notices preserved in `NOTICE.md`, `THIRD_PARTY_NOTICES.md`, and `LICENSE`.
- [x] **Gate 9: Release Artifacts & Checksums**
  - Release APK and Android App Bundle generated and verified.

---

## 3. Release Artifacts & Verification Checksums

All official release artifacts must match the following SHA-256 checksums:

```
f3507ea981f0cfbfa658bd9673acc4cf52e6bc4d48664db600a468d030a3e03e  RollerSkater-v1.0.0-arm64-v8a.apk
d703f765194628f0c9d012771b21b442ab215c081c64a9ea4bd9cfed01dd51b8  RollerSkater-v1.0.0-armeabi-v7a.apk
907084e3d33a0ab67246517488e6a940110e771201d2ef67ed529c1e8d600e2d  RollerSkater-v1.0.0-universal.apk
5f4510250ec620d81dbad70f62138e8a131642e70a34094282611a00688f4a27  RollerSkater-v1.0.0.aab
```

### Verification Command

To verify the integrity of downloaded release artifacts:

```bash
sha256sum -c <<EOF
f3507ea981f0cfbfa658bd9673acc4cf52e6bc4d48664db600a468d030a3e03e  RollerSkater-v1.0.0-arm64-v8a.apk
d703f765194628f0c9d012771b21b442ab215c081c64a9ea4bd9cfed01dd51b8  RollerSkater-v1.0.0-armeabi-v7a.apk
907084e3d33a0ab67246517488e6a940110e771201d2ef67ed529c1e8d600e2d  RollerSkater-v1.0.0-universal.apk
5f4510250ec620d81dbad70f62138e8a131642e70a34094282611a00688f4a27  RollerSkater-v1.0.0.aab
EOF
```

---

## 4. Keystore Generation & Signing Setup

Release builds are signed using a standard Android keystore.

### Generating a Production Keystore

```bash
keytool -genkeypair -v \
  -keystore rollerskater-release.jks \
  -alias rollerskater \
  -keyalg RSA \
  -keysize 4096 \
  -validity 10000 \
  -storetype PKCS12
```

### Configuring `keystore.properties`

Create a local `keystore.properties` file in the repository root (this file is excluded by `.gitignore`):

```properties
storeFile=/path/to/rollerskater-release.jks
storePassword=YOUR_SECURE_STORE_PASSWORD
keyAlias=rollerskater
keyPassword=YOUR_SECURE_KEY_PASSWORD
```

When building in environments without a release keystore (e.g., F-Droid build servers), release signing can be explicitly disabled:

```bash
./gradlew :app:assembleRelease -Prollerskater.disableReleaseSigning=true
```

---

## 5. F-Droid Submission Procedure

RollerSkater is 100% free software (GPL-3.0) and eligible for inclusion in the official F-Droid main repository.

### Build Recipe

The build recipe is prepared in `metadata/com.rollerskater.app.yml`:

```yaml
Categories:
  - Multimedia
License: GPL-3.0-only
AuthorName: RollerSkater Contributors
SourceCode: https://github.com/dhyandevp/RollerSkater
IssueTracker: https://github.com/dhyandevp/RollerSkater/issues
Changelog: https://github.com/dhyandevp/RollerSkater/blob/master/CHANGELOG.md

AutoName: RollerSkater
Summary: Fast, private, offline-first music player with Material 3 design

RepoType: git
Repo: https://github.com/dhyandevp/RollerSkater.git

Builds:
  - versionName: 1.0.0
    versionCode: 1
    commit: v1.0.0
    subdir: app
    gradle:
      - yes
```

### Submission Steps

1. Fork the [`fdroiddata`](https://gitlab.com/fdroid/fdroiddata) repository on GitLab.
2. Copy `metadata/com.rollerskater.app.yml` into `metadata/com.rollerskater.app.yml` in your `fdroiddata` branch.
3. Test locally with the F-Droid server tools:
   ```bash
   fdroid checkupdates com.rollerskater.app
   fdroid lint com.rollerskater.app
   fdroid build --stop com.rollerskater.app
   ```
4. Open a Merge Request against `fdroid/fdroiddata`.

---

## 6. GitHub Releases Publication

1. Tag the release commit:
   ```bash
   git tag -a v1.0.0 -m "RollerSkater 1.0.0"
   git push origin v1.0.0
   ```
2. Create a new GitHub Release matching tag `v1.0.0`.
3. Use the release notes from [CHANGELOG.md](../CHANGELOG.md).
4. Attach the signed release artifacts:
   - `app-release.apk` (universal APK)
   - `app-release.aab` (Android App Bundle)
5. Include the SHA-256 checksum block in the release description.

---

## 7. Google Play Store Publication

1. Build the production App Bundle:
   ```bash
   ./gradlew :app:bundleRelease
   ```
2. In Google Play Console, navigate to **Production > Create new release**.
3. Upload `app/build/outputs/bundle/release/app-release.aab`.
4. Copy the release notes from `fastlane/metadata/android/en-US/changelogs/1.txt`.
5. Upload store graphics:
   - App Icon: 512x512 PNG (`app/src/main/ic_launcher-playstore.png`)
   - Feature Graphic: 1024x500 PNG
   - Store screenshots: Phone, 7-inch tablet, and 10-inch tablet
6. Complete content rating and Data Safety declaration:
   - Data collection: **None** (zero data shared, zero data collected).
7. Submit the release for review.
