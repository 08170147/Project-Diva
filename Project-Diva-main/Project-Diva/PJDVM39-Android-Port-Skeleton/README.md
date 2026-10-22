# PJDVM39 Android Port — Starter Shell

A minimal Kotlin Android shell and repeatable GitHub Actions build pipeline. This repository is a diagnostic starter project, **not a playable port of Project DIVA**.

## Current verified scope

- Landscape Android activity.
- Reads test assets from `app/src/main/assets`.
- Verifies SHA-256 hashes listed in `resource_manifest.txt`.
- Includes two non-game test resources: `port_test.txt` and `port_test_extra.txt`.
- Root-level GitHub Actions workflow builds the debug APK and packages source deliverables.

No proprietary game assets, executable, title keys, firmware, or game code are included. An iOS ARM64 Mach-O executable cannot be relinked as an Android native library. A real port would require substantial compatible runtime/API implementation or reimplementation.

## Build using GitHub Actions

1. Upload the **contents of the outer `Project-Diva-main` folder** to the root of a GitHub repository. The `.github/workflows/android-build.yml` file must be at the repository root.
2. Enable GitHub Actions if prompted.
3. Push to `main`/`master`, open a pull request to either branch, or run **Actions → Build Android Port Shell → Run workflow**.
4. Download the `PJDVM39-Android-Port-build` artifact from the completed workflow.
5. The artifact contains:
   - `PJDVM39-Android-Port-debug.apk` — installable debug build, if the workflow succeeds.
   - `PJDVM39-Android-Port-source.zip` — source project.
   - `PJDVM39-Android-Port-deliverables.zip` — APK and source ZIP together.
   - `SHA256SUMS.txt` — checksums for the APK and source ZIP.

## Build requirements

- JDK 17
- Gradle 8.9 (installed by GitHub Actions)
- Android Gradle Plugin 8.7.3
- Kotlin Android plugin 2.0.21
- Android compile SDK 35 / build tools 35.0.0
- Minimum Android API 26

## Local build

From the repository root:

```bash
gradle -p PJDVM39-Android-Port-Skeleton clean assembleDebug
```

The APK is written to:

```text
PJDVM39-Android-Port-Skeleton/app/build/outputs/apk/debug/app-debug.apk
```

## Next engineering milestones

1. Add unit tests for valid, missing, malformed, and hash-mismatched manifest entries.
2. Add a Vulkan surface plus touch/audio diagnostics.
3. Inventory native dependencies and imported frameworks from a legally obtained game package.
4. Research a compatible Horizon service layer and NVN-to-Vulkan translation. This starter shell does not implement those components.
