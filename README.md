# Parent Dashboard

Android color-sort puzzle — campaign levels, daily challenges, AI practice, local Wi‑Fi / Bluetooth multiplayer, themes, and AdMob monetization.

## Stack

- Kotlin, Jetpack Compose, Hilt, Room
- Firebase Analytics, AdMob
- Google Nearby Connections (local P2P)
- English + 21 locales (same set as PuzzleVerse)

## Requirements

- Android Studio Ladybug or newer
- JDK 17
- Android SDK 35

## Setup

1. Open `D:\Work\Games\ParentDashboard` in Android Studio.
2. Add Firebase config at `app/google-services.json`.
3. Replace release AdMob IDs in `app/build.gradle.kts`.
4. Build:

```bash
./gradlew assembleDebug
```

## Features

- Color sort campaign with adaptive difficulty
- Play vs system / hint-assisted practice
- Daily / weekly / monthly challenges
- Same-device 2-player + local P2P (Wi‑Fi / Bluetooth via Nearby)
- 8 themes + accessibility options
- Rewarded ads for extra hints
- Play Store docs in `docs/`

## Next steps (game engine)

The project is scaffolded from PuzzleVerse. Replace `engine/ScrewPuzzleEngine.kt` with `ParentDashboardEngine.kt` (tube pour logic) and update the game board UI component.

## License

All rights reserved unless a LICENSE file is added.
