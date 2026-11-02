# Futures Flow Android

Android Studio / GitHub Actions project for a Binance USDⓈ-M Futures flow-monitor prototype.

## Important status — not a production trading signal system

This source project is structured to build a debug APK. It is **not yet a validated production signal engine**.

Implemented prototype elements:
- Binance USDⓈ-M Futures `aggTrade` WebSocket connection for BTCUSDT only.
- Per aggregate-trade quote notional gate: `price × quantity >= 3,000 USDT`.
- Aggressor inference from Binance `m` field: `m=true` means buyer was maker, so the aggressor was the seller (SHORT); otherwise buyer aggressor (LONG).
- Open Interest REST sampling.
- In-memory CVD accumulator.
- Five timeframe candle data retrieval (5m, 15m, 30m, 1h, 4h).
- Telegram Bot API client and settings screen.
- Foreground service and GitHub Actions debug APK workflow.

## Deliberate signal safety gate

`SignalEngine.evaluate()` currently returns no signal. Therefore no Telegram trade alert is sent. This is intentional: the exact multi-timeframe confirmation rules and mathematically defined TP1, TP2, and invalidation method have not been specified and validated. The app must not invent target prices or send fabricated confirmations.

## Known functional limitations

1. Only BTCUSDT is monitored; a multi-symbol scanner is not implemented.
2. The 3,000 USDT filter applies to each Binance aggregate-trade event, not a grouped time-window volume.
3. `aggTrade` is Binance's aggregate-trade stream; it does not identify a named institution or prove a single institutional parent order.
4. OI is periodically sampled and is not yet a robust event-time aligned rolling series.
5. CVD is an in-memory session accumulator and is not persisted or calculated as independent rolling values for each timeframe.
6. Candle direction data is informational only; the current strategy gate is disabled.
7. Telegram delivery, background execution, reconnects, and installation still require real-device testing.

## Build with GitHub Actions

The repository root must contain `settings.gradle.kts`, `build.gradle.kts`, and `app/build.gradle.kts` directly. Do not upload the enclosing ZIP folder as an extra nested directory.

1. Upload/extract the **contents** of this ZIP into the root of your GitHub repository.
2. Open **Actions → Android APK**.
3. Select **Run workflow** or push to `main`.
4. Wait for the `build` job to finish successfully.
5. Download `futures-flow-debug-apk` from the run's **Artifacts** section.

Workflow uses JDK 17, Gradle 8.9, Android SDK platform 35 and Android build-tools 35.0.0.

## Local Android Studio build

Open this folder as the project. Use JDK 17 and Android SDK 35. Run:

```bash
gradle --no-daemon :app:assembleDebug
```

## Telegram setup

Create a bot with BotFather. Enter the bot token and chat ID in the app. Never commit real bot tokens to GitHub.

## Security

The prototype stores the Telegram token in app-private SharedPreferences. Before public release, migrate secrets to Android Keystore-backed encrypted storage and review logging and backup policy.
