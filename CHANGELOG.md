# Changelog

All notable changes to MaidFinder will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

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

### Changed
- ClientDashboardScreen now displays real data from MaidListViewModel
- MaidDashboardScreen now displays real data from JobFeedViewModel
- Navigation graph updated to provide ViewModels via ServiceLocator

## [0.1.0] - 2026-03-30

### Added
- Initial Android project scaffold with Jetpack Compose
- Material 3 theme with light/dark color schemes
- MVVM architecture structure (data, navigation, ui packages)
- Role selection screen with "I Need a Maid" and "I Am a Maid" options
- Navigation Compose with routes for Client and Maid dashboards
- Gradle wrapper (8.11.1) for reproducible builds
- Project documentation (README)
