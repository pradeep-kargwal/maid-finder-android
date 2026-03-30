# Changelog

All notable changes to MaidFinder will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

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
