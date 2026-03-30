# MaidFinder Android App

A Kotlin Android application built with Jetpack Compose that connects clients seeking domestic help with maids seeking employment.

## Architecture

This project follows **MVVM (Model-View-ViewModel)** architecture with clean separation of concerns:

```
com.maidfinder.app/
├── data/
│   ├── model/          # Data classes
│   ├── repository/     # Repository interfaces & implementations
│   └── source/         # Data sources (in-memory, API, etc.)
├── navigation/         # Navigation graph & route definitions
├── ui/
│   ├── screens/        # Composable screen functions
│   └── theme/          # Material 3 theme, colors, typography
├── MainActivity.kt     # Entry point
└── MaidFinderApp.kt    # Root composable
```

## Tech Stack

- **Language:** Kotlin 2.1.10
- **UI:** Jetpack Compose with Material 3
- **Navigation:** Jetpack Navigation Compose
- **Architecture:** MVVM with Repository pattern
- **Min SDK:** 21 (Android 5.0)
- **Target SDK:** 35 (Android 15)
- **AGP:** 8.7.3

## Screens

1. **Role Selection** — Two clear options: "I Need a Maid" (client) or "I Am a Maid" (worker)
2. **Client Dashboard** — Placeholder for client features
3. **Maid Dashboard** — Placeholder for maid features

## Building

```bash
./gradlew assembleDebug
```

## License

Proprietary — All rights reserved.
