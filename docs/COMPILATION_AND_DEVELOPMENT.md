# RollerSkater — Compilation & Development Manual

This guide provides step-by-step instructions for importing, building, running, and contributing to **RollerSkater** using Android Studio and the command line.

---

## 1. Prerequisites & Environment Setup

Before opening the project, ensure your environment meets these requirements:

### Required Tools
| Component | Minimum Version | Recommended / Current |
|---|---|---|
| **Android Studio** | Ladybug (2024.2.1+) | Ladybug Feature Drop / Koala or newer |
| **JDK (Java Development Kit)** | JDK 21 | Eclipse Temurin 21 or Android Studio Bundled JDK 21 |
| **Android SDK Platform** | API 30 (Android 11) | API 35 / API 37 |
| **Android SDK Build-Tools** | 35.0.0+ | 35.0.0 / 37.0.0 |
| **Git** | 2.30+ | Latest stable |

> [!IMPORTANT]
> RollerSkater requires **JDK 21**. If your system or Android Studio defaults to JDK 17 or JDK 11, Gradle sync will fail.

---

## 2. Importing Project into Android Studio

1. **Launch Android Studio**.
2. On the Welcome screen, click **Open** (or go to **File > Open** if an existing project is open).
3. Navigate to the repository root directory (`rollerskater`) and click **OK**.
4. Allow Android Studio to discover the Gradle configuration and initiate the initial sync.

### Configuring the Gradle JDK in Android Studio
If Android Studio does not automatically select JDK 21:
1. Open **Settings** (or **Preferences** on macOS): `Ctrl + Alt + S` (Linux/Windows) or `Cmd + ,` (macOS).
2. Navigate to **Build, Execution, Deployment > Build Tools > Gradle**.
3. Under **Gradle Projects**, find **Gradle JDK**.
4. Choose **Java 21** (or click *Add JDK...* and select your local JDK 21 path, or select *Download JDK... > Version 21*).
5. Click **Apply** and then **OK**.
6. Click **Sync Project with Gradle Files** (elephant icon in the top-right toolbar).

---

## 3. Running & Debugging the App

### Running on a Connected Device or Emulator
1. Connect a physical Android device with **USB Debugging** enabled (Android 11 / API 30 or newer), or launch an Android Virtual Device (AVD).
2. In the top toolbar, ensure the **Run Configuration** dropdown is set to **`app`**.
3. Select your device or emulator in the device selector dropdown.
4. Click the green **Run** button (`Shift + F10`) or **Debug** button (`Shift + F9`).
5. Gradle will assemble the debug APK and install it on the target device.

---

## 4. Building APKs

### Method A: Using Android Studio GUI
- **Debug APK**:
  - Go to **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
  - When the build finishes, click the notification popup **locate** link to open `app/build/outputs/apk/debug/`.
- **Signed Release APK**:
  - Go to **Build > Generate Signed Bundle / APK...**.
  - Choose **APK** or **Android App Bundle**.
  - Specify your keystore file, key alias, and passwords.

### Method B: Using Gradle Terminal Commands
Open the **Terminal** tab at the bottom of Android Studio (or your system terminal):

```bash
# 1. Compile Kotlin debug sources (fast verification)
./gradlew :app:compileDebugKotlin

# 2. Assemble Debug APK
./gradlew :app:assembleDebug
# Output APK: app/build/outputs/apk/debug/app-debug.apk

# 3. Assemble Release APK (unsigned verification)
./gradlew :app:assembleRelease -Prollerskater.disableReleaseSigning=true
# Output APK: app/build/outputs/apk/release/app-*-release-unsigned.apk
```

---

## 5. Configuring Release Signing (Optional)

To automatically generate signed release APKs or bundles locally, create a `keystore.properties` file in the repository root (this file is ignored by `.gitignore`):

```properties
storeFile=rollerskater.jks
storePassword=YourKeystorePassword
keyAlias=YourKeyAlias
keyPassword=YourKeyPassword
```

When `keystore.properties` is present, running `./gradlew :app:assembleRelease` will produce signed APKs in `app/build/outputs/apk/release/`.

---

## 6. Running Unit & Integration Tests

### In Android Studio
- In the **Project** view, navigate to `app/src/test/java/com/rollerskater/app/`.
- Right-click the folder and choose **Run 'Tests in 'rollerskater.app''** (`Ctrl + Shift + F10`).

### In the Terminal
To avoid JVM memory exhaustion during large multi-threaded test runs, always specify worker limits:

```bash
# Run all unit tests
./gradlew :app:testDebugUnitTest --max-workers=2

# Run a specific test class
./gradlew :app:testDebugUnitTest --tests "com.rollerskater.app.ui.glancewidget.AlbumArtBitmapCacheTest"
```

---

## 7. Pushing Changes to GitHub

### 1. Verify Git Status & Remote
Ensure that your working tree is clean and that the only configured remote is your repository:

```bash
# Check working tree
git status

# Verify git remote (should point to https://github.com/dhyandevp/RollerSkater.git)
git remote -v
```

### 2. Push to GitHub
Push your local commits on the `main` branch:

```bash
git push origin main
```

> [!TIP]
> If prompted for credentials when pushing via HTTPS:
> - **Username**: Your GitHub username.
> - **Password**: A GitHub **Personal Access Token (Classic)** with the `repo` scope enabled (generate one at *GitHub > Settings > Developer Settings > Personal access tokens*).

### 3. Verify GitHub Actions
Once pushed, open your repository on GitHub and click the **Actions** tab to view the automated workflows:
- **Nightly APK (`nightly-apk.yml`)**: Builds nightly debug and release packages.
- **PR Build (`pr-build.yml`)**: Validates pull requests with compilation and lint checks.
- **Alpha Release (`alpha-release.yml`)**: Generates automated alpha releases when version tags are pushed.

---

## 8. Common Troubleshooting

### 1. "Unsupported class file major version 65 (Java 21)"
- **Cause**: Android Studio or your system is using an older Gradle version or a JDK mismatched with Gradle 8.x.
- **Solution**: Ensure Gradle JDK in Android Studio is set to **JDK 21** under *Settings > Build, Execution, Deployment > Build Tools > Gradle*.

### 2. "Out of Memory (OOM) / Metaspace error during tests"
- **Cause**: Forking too many test workers on machines with limited RAM.
- **Solution**: Append `--max-workers=2` to test commands (configured in `gradle.properties`).

### 3. "Cannot set the rounded corner of views before Api 31"
- **Context**: Jetpack Glance warning on Android 11.
- **Resolution**: RollerSkater includes an automatic antialiased rounded bitmap fallback layer (`AlbumArtBitmapCache` and `toRoundedBitmap`) so widgets maintain rounded artwork, buttons, and backgrounds on Android 11 without relying on API 31+ outlines.
