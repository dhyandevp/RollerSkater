# RollerSkater — Build & Compilation Manual

This guide covers building **RollerSkater** from source, configuring local signing, understanding Gradle build flags, and troubleshooting common build or environment errors.

---

## 1. System Requirements & Environment

| Component | Minimum Version | Verified Working Version | Notes |
|---|---|---|---|
| **Operating System** | Linux, macOS, or Windows | Linux x86_64 / arm64 | Tested on Linux with bash shell |
| **JDK** | JDK 21 | Eclipse Temurin 21 (`21.0.x`) or OpenJDK 21 | **Mandatory**: Kotlin 2.4 and AGP 9.3 target JVM 21 |
| **Android SDK Platform** | API 30 (Android 11) | API 37 | Target & Compile SDK is API 37 |
| **Android Build-Tools** | 35.0.0 | 35.0.0 / 37.0.0 | Included with Android SDK command-line tools |
| **Gradle** | 9.6.1 | Handled via `./gradlew` wrapper | Uses configuration cache & parallel execution |

---

## 2. Standard Build Commands

Always run build commands from the repository root:

```bash
# 1. Compile Kotlin debug sources (fastest incremental check)
./gradlew :app:compileDebugKotlin

# 2. Assemble Debug APK (includes debug logging & LeakCanary)
./gradlew :app:assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk

# 3. Assemble Release APKs with ABI splits (arm64-v8a and armeabi-v7a)
./gradlew :app:assembleRelease -Prollerskater.enableAbiSplits=true

# 4. Assemble Release APK without signing (for local verification or F-Droid)
./gradlew :app:assembleRelease -Prollerskater.disableReleaseSigning=true

# 5. Assemble Android App Bundle (AAB for Google Play)
./gradlew :app:bundleRelease
# Output: app/build/outputs/bundle/release/app-release.aab
```

---

## 3. Gradle Properties & Build Configuration Flags

These flags can be passed via command line (`-Pkey=value`) or configured in `gradle.properties`:

| Flag | Default | Description |
|---|---|---|
| `rollerskater.enableAbiSplits` | `true` (for APK) / `false` (for AAB) | Generates architecture-specific APKs (`arm64-v8a`, `armeabi-v7a`). Automatically disabled during bundle tasks. |
| `rollerskater.disableReleaseSigning` | `false` | When `true`, skips signing configurations and produces unsigned release APKs even if keystore properties exist. |
| `rollerskater.enableComposeCompilerReports` | `false` | When `true`, exports stability and metrics reports to `app/build/compose_compiler_reports` and `compose_compiler_metrics`. |
| `APP_VERSION_NAME` | Value in `gradle.properties` (`1.0.0`) | Overrides the user-visible version string (e.g. for PR builds or CI releases). |
| `APP_VERSION_CODE` | Value in `gradle.properties` (`1`) | Overrides the internal integer versionCode. |

---

## 4. Setting Up Release Signing

To sign release builds locally, create a `keystore.properties` file at the repository root (this file is excluded by `.gitignore`):

```properties
storeFile=rollerskater.jks
storePassword=YourStorePassword
keyAlias=YourKeyAlias
keyPassword=YourKeyPassword
```

### Generating a New Release Key
```bash
keytool -genkey -v -keystore rollerskater.jks -alias rollerskater-key \
  -keyalg RSA -keysize 4096 -validity 10000 \
  -dname "CN=RollerSkater, OU=Mobile, O=RollerSkater, L=World, S=World, C=US"
```

> [!CAUTION]
> Never commit `keystore.properties` or `.jks` files containing real production signing keys to Git.

---

## 5. Running Tests & Baseline Profiles

```bash
# Run all JVM unit tests
./gradlew :app:testDebugUnitTest

# Run specific unit test class
./gradlew :app:testDebugUnitTest --tests "com.rollerskater.app.data.database.MigrationsTest"

# Run Android Lint checks
./gradlew :app:lintDebug

# Generate Baseline Profiles (requires connected device or emulator with API 30+)
./gradlew :baselineprofile:generateBaselineProfile
```

---

## 6. Common Build Errors & Troubleshooting

### Issue 1: Unsupported Java Version or Class File Version Mismatch
**Symptom**: `Unsupported class file major version 65` or `Gradle was run with an incompatible Java version`.  
**Cause**: Gradle was invoked with JDK 17, 11, or 8 instead of JDK 21.  
**Solution**:
1. Check your active Java version: `java -version`.
2. Explicitly specify `JAVA_HOME`:
   ```bash
   JAVA_HOME=/path/to/jdk-21 ./gradlew :app:compileDebugKotlin
   ```
3. In Android Studio, go to **Settings > Build, Execution, Deployment > Build Tools > Gradle** and set **Gradle JDK** to **JDK 21**.

### Issue 2: Gradle Daemon Lock or Concurrent Daemon Collision
**Symptom**: `Storage[.../file-to-id.tab] registration stack trace` or `Failed to create parent directory`.  
**Cause**: Two or more Gradle daemons running concurrently or a previous daemon was killed mid-compilation leaving stale lock files in `.gradle/caches`.  
**Solution**:
```bash
./gradlew --stop
killall -9 java 2>/dev/null || true
./gradlew :app:compileDebugKotlin
```

### Issue 3: Missing Android SDK Platform 37
**Symptom**: `Failed to find target with hash string 'android-37' in: /path/to/Android/Sdk`.  
**Cause**: The Android SDK platform for API 37 is not installed.  
**Solution**:
Install the platform via SDK Manager or CLI:
```bash
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "platforms;android-37" "build-tools;35.0.0"
```

### Issue 4: Release Build Fails Due to Missing Signing Config
**Symptom**: Build fails during `:app:validateSigningRelease` or produces an unsigned APK when signed was expected.  
**Cause**: `keystore.properties` is missing or has incorrect passwords.  
**Solution**:
Either create `keystore.properties` with valid credentials, or pass `-Prollerskater.disableReleaseSigning=true` to produce unsigned binaries.

### Issue 5: Retracing Obfuscated R8 Crash Stacks
**Symptom**: Release crash reports display obfuscated class names (e.g., `a.b.c.d()`).  
**Cause**: R8 minification is enabled in release builds.  
**Solution**:
The R8 mapping file is located at `app/build/outputs/mapping/release/mapping.txt`. Retrace the stack trace:
```bash
$ANDROID_HOME/cmdline-tools/latest/bin/retrace app/build/outputs/mapping/release/mapping.txt crash_trace.txt
```
