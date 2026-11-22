# RollerSkater — Release Engineering & Deployment Manual

This document provides complete instructions for release management, cryptographic signing, ABI split packaging, CI/CD pipeline automation, and multi-channel distribution.

---

## 1. Versioning Protocol

RollerSkater follows [Semantic Versioning 2.0.0](https://semver.org/): `MAJOR.MINOR.PATCH` (e.g. `1.0.0`).

### Single Source of Truth
The version name and code are maintained exclusively in [`gradle.properties`](../gradle.properties):
```properties
APP_VERSION_NAME=1.0.0
APP_VERSION_CODE=1
```
`app/build.gradle.kts` automatically reads these properties:
```kotlin
defaultConfig {
    versionCode = (project.findProperty("APP_VERSION_CODE") as? String)?.toInt() ?: 1
    versionName = (project.findProperty("APP_VERSION_NAME") as? String) ?: "1.0.0"
}
```

---

## 2. Release Signing Strategy

### Local Keystore Setup
To generate a local RSA 4096-bit release keystore:
```bash
keytool -genkey -v \
  -keystore rollerskater.jks \
  -alias rollerskater-key \
  -keyalg RSA \
  -keysize 4096 \
  -validity 10000 \
  -dname "CN=RollerSkater Release, OU=Mobile, O=RollerSkater, L=World, S=World, C=US"
```

Configure `keystore.properties` at the repository root:
```properties
storeFile=rollerskater.jks
storePassword=YourKeystorePassword
keyAlias=rollerskater-key
keyPassword=YourKeyPassword
```

> [!CAUTION]
> Never commit `keystore.properties` or `rollerskater.jks` to version control. Both are excluded via `.gitignore` and `.gitattributes`.

### CI/CD Signing Secrets
Official GitHub Actions workflows read credentials from repository secrets:
- `CI_KEYSTORE_B64`: Base64-encoded binary keystore (`base64 -w 0 rollerskater.jks`).
- `CI_KEYSTORE_PASSWORD`: Keystore and key password.
- If secrets are absent (such as in PRs from external forks), the build gracefully falls back to a throwaway developer keystore so pull requests can still produce testable binaries.

---

## 3. Assembling Release Binaries

### Option A: Architecture-Specific Split APKs (Recommended for Sideloading)
Generates lightweight APKs tailored to specific CPU architectures:
```bash
./gradlew :app:assembleRelease -Prollerskater.enableAbiSplits=true
```
Outputs in `app/build/outputs/apk/release/`:
- `app-arm64-v8a-release.apk` (~21 MB): 64-bit ARM devices (most modern phones).
- `app-armeabi-v7a-release.apk` (~20 MB): 32-bit legacy ARM devices.
- `app-universal-release.apk` (~28 MB): Universal fallback.

### Option B: Android App Bundle (AAB for Google Play)
Generates an optimized App Bundle allowing Google Play to serve dynamic feature splits:
```bash
./gradlew :app:bundleRelease
```
Output: `app/build/outputs/bundle/release/app-release.aab` (~27 MB).

### Option C: Unsigned Release (for F-Droid / Verification)
```bash
./gradlew :app:assembleRelease -Prollerskater.disableReleaseSigning=true
```

---

## 4. Verification & Checksum Generation

### Verifying APK Signature with `apksigner`
```bash
BUILD_TOOLS="$(ls "$ANDROID_HOME/build-tools" | sort -V | tail -n 1)"
"$ANDROID_HOME/build-tools/$BUILD_TOOLS/apksigner" verify --verbose \
  app/build/outputs/apk/release/app-arm64-v8a-release.apk
```

### Validating Package Manifest & Badging with `aapt2`
```bash
"$ANDROID_HOME/build-tools/$BUILD_TOOLS/aapt2" dump badging \
  app/build/outputs/apk/release/app-arm64-v8a-release.apk | grep -E "package|launchable-activity"
```

### Computing SHA-256 Checksums
```bash
cd app/build/outputs/apk/release/
sha256sum app-arm64-v8a-release.apk app-armeabi-v7a-release.apk > checksums.txt
cat checksums.txt
```

---

## 5. Automated CI/CD Workflows (`.github/workflows/`)

| Workflow File | Trigger | Purpose & Output |
|---|---|---|
| **[`ci.yml`](../.github/workflows/ci.yml)** | Push / PR to `main` | Compiles Kotlin debug, executes 197 unit test classes, runs Android Lint, and builds test release APK/AAB. |
| **[`pr-build.yml`](../.github/workflows/pr-build.yml)** | Pull Requests | Assembles PR test split APKs with PR version naming, verifies with `aapt2`/`apksigner`, and publishes downloadable artifact. |
| **[`release.yml`](../.github/workflows/release.yml)** | Tag push (`v*.*.*`) | Assembles signed split APKs, computes SHA-256 checksums, and publishes an official GitHub Release. |
| **[`alpha-release.yml`](../.github/workflows/alpha-release.yml)** | Manual / Alpha branches | Builds alpha prerelease APKs and archives compressed R8 mappings (`mapping.txt.gz`) for crash retracing. |
| **[`nightly-apk.yml`](../.github/workflows/nightly-apk.yml)** | Scheduled Nightly | Evaluates commits on `main` and publishes nightly APKs with 14-day retention. |

---

## 6. Distribution Channels

### 1. GitHub Releases
- Official tags follow format `vX.Y.Z` (e.g. `v1.0.0`).
- Releases bundle `arm64-v8a`, `armeabi-v7a`, `universal` APKs and `checksums.txt`.

### 2. F-Droid Repository
- Configured in [`metadata/com.rollerskater.app.yml`](../metadata/com.rollerskater.app.yml).
- Built from clean git release tags.
- Disables proprietary Google Play billing and signing.

### 3. Google Play Store
- Upload `RollerSkater-vX.Y.Z.aab` directly to Google Play Console.
- Uses Google Play App Signing with enrolled upload key.
