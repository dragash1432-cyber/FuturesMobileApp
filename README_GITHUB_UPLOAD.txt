# FuturesFlow Android source project

## GitHub upload instructions (important)

1. Extract this ZIP on your computer.
2. In the GitHub repository `FuturesMobileApp`, remove the old workflow file(s) under `.github/workflows/` that run `gradle assembleDebug` (without `:app:`). In particular, delete the previous `android.yml` if it is still present.
3. Upload the **contents** of this extracted folder to the repository root. Do not upload the containing `FuturesFlowAndroid_CORRECTED` folder itself. The repository root must directly contain `settings.gradle.kts`, `build.gradle.kts`, `app/`, and `.github/`.
4. Commit the changes.
5. Open Actions and run **FuturesFlow Android APK (corrected)**. Do not run an older workflow.
6. If successful, download `futuresflow-debug-apk` from the run's Artifacts section.

The workflow uses the explicit Gradle project path and task `:app:assembleDebug`. It also checks the root project files and APK output.

## Current functional scope and limitations

This is a source project, not a prebuilt APK. A successful GitHub Actions run is required to produce the APK. The current signal engine deliberately returns no signal until a validated signal/target methodology is implemented; therefore it must not be treated as a live trading signal product. The initial data connection watches BTCUSDT only.
