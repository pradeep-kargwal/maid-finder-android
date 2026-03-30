# Changelog

All notable changes to MaidFinder will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [1.0.0] - 2026-03-31

### Added - Feature Parity & Auth Release
- **Authentication**: OTP login screen with phone verification, demo mode toggle (Client/Maid)
- **Bookings Screen**: Full bookings list for both user types with Active/Completed/Cancelled tabs
- **Messages Screen**: Conversation list with search, unread badges, online indicators
- **Chat Screen**: Real-time message bubbles, voice note placeholder, system messages
- **Profile Screen**: Role-aware profile with gradient header, settings menu, demo mode switcher
- **Design System**: Reusable components (AvatarInitials, StatusBadge, GradientButton, EmptyState, ShimmerEffect)
- **Animations**: Pulsing online dots, animated tab transitions, shimmer loading effects
- **Data Models**: AuthSession, Conversation, Message, MessageType
- **Repositories**: AuthRepository (OTP + demo), MessageRepository (conversations + messages)
- Sample bookings data (6 bookings across multiple statuses)
- Sample conversations with messages
- Badge indicators on Messages tab
- Online/offline status indicators

### Changed
- MainScreen now shows 4 tabs for both roles (Home, Bookings, Messages, Profile)
- MaidMainScreen uses authSession for user-aware screens
- Navigation graph expanded to 8 routes with auth flow
- MaidFinderApp now creates AuthViewModel and manages auth state

### Feature Parity Matrix
| Feature | Client | Maid |
|---------|--------|------|
| Browse/Search | ✅ | ✅ |
| Bookings List | ✅ | ✅ |
| Messages | ✅ | ✅ |
| Chat | ✅ | ✅ |
| Profile | ✅ | ✅ |
| Auth/Login | ✅ | ✅ |
| Demo Mode | ✅ | ✅ |

## [Unreleased]

## [0.5.0] - 2026-03-31

### Added
- Maid profile detail screen with skills, availability, verification badges
- Job posting form for clients (job type, dates, shifts, budget)
- Bottom navigation bar with 5 tabs for both client and maid roles
- Job detail screen with one-tap apply for maids
- Booking flow with date selection, cost breakdown, confirmation
- MaidProfileViewModel for profile detail data
- PostJobViewModel for job form state management
- JobDetailViewModel with apply and express interest actions
- BookingViewModel with booking creation logic
- FAB on client dashboard for quick job posting
- Navigation routes for all new screens

### Changed
- ClientDashboardScreen now navigates to profile detail on card tap
- MaidDashboardScreen now navigates to job detail on card tap
- Navigation graph updated with 7 routes total

## [0.4.0] - 2026-03-30

### Added
- Maid profile detail screen with full information display
- MaidProfileViewModel for detail data loading
- Navigation route with maidId argument
- Save/bookmark toggle on profile detail

## [0.3.0] - 2026-03-30

### Added
- Job posting form for clients
- PostJobViewModel with form state management
- SegmentedButtonRow for job type selection
- FilterChip selectors for shift and budget type
- Success screen on job submission
- FAB on client dashboard

## [0.2.0] - 2026-03-30

### Added
- Data layer with Repository pattern (MaidRepository, JobRepository, BookingRepository)
- Data models: User, MaidProfile, Job, Booking, Application, Review
- In-memory data sources with sample data (5 maids, 5 jobs)
- ServiceLocator for dependency injection
- MaidListViewModel with filtering (work type, verified, radius)
- JobFeedViewModel with job type filtering
- Client dashboard with maid cards showing name, rating, rate, distance, skills
- Maid dashboard with job feed showing type badge, budget, location, shifts
- FilterChip-based UI for search/filter controls

## [0.1.0] - 2026-03-30

### Added
- Initial Android project scaffold with Jetpack Compose
- Material 3 theme with light/dark color schemes
- MVVM architecture structure (data, navigation, ui packages)
- Role selection screen with "I Need a Maid" and "I Am a Maid" options
- Navigation Compose with routes for Client and Maid dashboards
- Gradle wrapper (8.11.1) for reproducible builds
- Project documentation (README)
