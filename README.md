# MaidFinder Android App

A Kotlin Android application built with Jetpack Compose that connects clients seeking domestic help with maids seeking employment.

## Architecture

This project follows **MVVM (Model-View-ViewModel)** architecture with clean separation of concerns:

```
com.maidfinder.app/
├── data/
│   ├── model/              # Data classes (User, MaidProfile, Job, Booking, etc.)
│   ├── repository/         # Repository interfaces & in-memory implementations
│   └── source/             # In-memory data sources with sample data
├── navigation/             # Navigation graph & route definitions
├── ui/
│   ├── screens/            # Composable screen functions
│   ├── theme/              # Material 3 theme, colors
│   └── viewmodel/          # ViewModels (MaidListViewModel, JobFeedViewModel)
├── MainActivity.kt         # Entry point
├── MaidFinderApp.kt        # Root composable
└── ServiceLocator.kt       # Simple DI container
```

## Tech Stack

- **Language:** Kotlin 2.1.10
- **UI:** Jetpack Compose with Material 3
- **Navigation:** Jetpack Navigation Compose
- **Architecture:** MVVM with Repository pattern
- **State Management:** StateFlow
- **Min SDK:** 21 (Android 5.0)
- **Target SDK:** 35 (Android 15)
- **AGP:** 8.7.3
- **Gradle:** 8.11.1

## Data Layer

The app uses a Repository pattern with interfaces and in-memory implementations:

- **MaidRepository** - Browse, filter, and save maid profiles
- **JobRepository** - Browse, create, and manage job postings
- **BookingRepository** - Manage bookings between clients and maids

Sample data includes 5 maid profiles and 5 job postings for Hyderabad.

## Screens

1. **Role Selection** - Two clear options: "I Need a Maid" or "I Am a Maid"
2. **Client Dashboard** - Browse nearby maids with filter chips (work type, verified)
3. **Maid Dashboard** - Browse job feed with filter chips (part-time, full-time, one-time)

## Building

```bash
./gradlew assembleDebug
```

## License

Proprietary - All rights reserved.
