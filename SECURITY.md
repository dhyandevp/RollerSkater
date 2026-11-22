# Security Policy

RollerSkater treats security and user privacy as core architectural invariants. Because RollerSkater is a 100% offline-first application designed to process personal audio files and user metadata exclusively on-device, our threat model centers on storage isolation, IPC integrity, and dependency supply chain security.

---

## 1. Supported Versions

Security patches and dependency updates are actively maintained on the following versions:

| Version | Supported | Security Maintenance Status |
|---|---|---|
| `1.0.x` | :white_check_mark: | Current active release branch |
| `main` | :white_check_mark: | Development trunk |
| `< 1.0.0` | :x: | Unsupported legacy versions |

---

## 2. Reporting a Vulnerability

We appreciate the responsible disclosure of security vulnerabilities by security researchers, contributors, and users.

### How to Report Privately
**Do NOT submit public GitHub issues for sensitive security vulnerabilities.**

1. **GitHub Private Vulnerability Reporting (Preferred)**:
   Navigate to the [Security Advisories](https://github.com/dhyandevp/RollerSkater/security/advisories) tab and click **"Report a vulnerability"**. This creates an encrypted, private advisory draft visible only to maintainers.
2. **Alternative Contact**:
   If GitHub Advisories are unavailable, open a regular GitHub issue titled `[SECURITY INQUIRY]` containing only a brief request for a secure communication channel. **Do not include reproduction steps, proofs of concept, or vulnerability details in the public issue.**

### What to Include in Your Report
To help us triage and resolve the issue quickly, please provide:
- A clear description of the vulnerability and its potential impact.
- Affected components (e.g. MediaStore indexing, IPC Service, Room Database, TagLib JNI, or Backup module).
- Step-by-step reproduction instructions or a minimal proof of concept (PoC).
- Device model, Android OS version, and RollerSkater version/commit tested.
- Any suggested remediations or mitigations.

### Response Timeline & Safe Harbor
- **Acknowledgment**: We aim to acknowledge receipt of reports within **48 hours**.
- **Assessment**: We will evaluate severity and coordinate a fix within **7 to 14 days**.
- **Public Disclosure**: Coordinated disclosure occurs only after a verified patch has been tagged and released.
- **Safe Harbor**: If you conduct vulnerability research in good faith in accordance with this policy (without accessing unauthorized user data, degrading service availability, or violating applicable laws), we will not pursue legal action.

---

## 3. Security Architecture & Threat Model

RollerSkater enforces strict security boundaries across the entire codebase:

### On-Device Data Residency & Network Restrictions
- **Zero Background Network Activity**: The app performs zero background telemetry, analytics, user tracking, or automatic remote logging.
- **Prohibited Cleartext Traffic**: `android:usesCleartextTraffic="false"` is strictly enforced in `AndroidManifest.xml` with a restrictive `network_security_config.xml`.
- **Opt-In Public APIs**: Any optional network calls (LRCLIB, Deezer, MusicBrainz) require explicit user consent and transmit only public track metadata (artist name, track title). No user identifiers, device IDs, or location data are ever transmitted.

### Storage & Android Scoped Storage Isolation
- **Storage Access Framework (SAF)**: Backup exports, imports, and custom playlist files are written exclusively through Android's system file picker via scoped content URIs (`content://`).
- **FileProvider Hardening**: Android `FileProvider` paths are restricted to internal cache and files directories (`res/xml/file_paths.xml`). External storage root sharing is blocked.
- **No Path Traversal**: Lyrics and playlist parsers sanitize file paths to prevent directory traversal attacks (`../`).

### Inter-Process Communication (IPC) & Services
- **Foreground Playback Service**: `MusicService` is an AndroidX `MediaSessionService` with explicit intent filters and permission constraints.
- **Non-Exported Components**: Internal broadcast receivers and background services are marked `android:exported="false"` unless explicitly required by system contracts (such as MediaButtonReceiver or Quick Settings TileServices).
- **Protected Permissions**: Privileged actions (e.g. setting system ringtones via `WRITE_SETTINGS`) prompt the user with system permission dialogues.

### Supply Chain & Native Code Security
- **Native JNI Boundaries**: Native TagLib bindings (`io.github.kyant0:taglib`) execute within the app's sandboxed process with memory-safe buffer constraints.
- **Dependency Auditing**: Dependencies are pinned in `gradle/libs.versions.toml` and monitored via Dependabot and automated CI workflows.
- **Sanitized Release Logging**: Debug logging trees (`Timber.DebugTree`) and LeakCanary are completely stripped in release builds (`ReleaseTree`), preventing sensitive path or track names from leaking into system logcat.

---

## 4. Keystore & Credential Hygiene

- **Untracked Release Keystores**: Production keystores (`*.jks`, `*.keystore`) and `keystore.properties` are strictly excluded from source control by `.gitignore` and `.gitattributes`.
- **CI Secrets**: Official release workflows sign artifacts using encrypted GitHub repository secrets (`CI_KEYSTORE_B64`, `CI_KEYSTORE_PASSWORD`). Pull requests from forks cannot access release secrets and automatically fall back to throwaway developer signatures.
