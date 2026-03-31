package com.maidfinder.app.domain.model

data class UserProfile(
    val id: String,
    val phone: String,
    val role: UserRole,
    val displayName: String,
    val photoUrl: String? = null,
    val isVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class UserRole { CLIENT, MAID }

data class MaidProfile(
    val userId: String,
    val displayName: String,
    val photoUrl: String? = null,
    val skills: List<Skill> = emptyList(),
    val experienceYears: Int = 0,
    val hourlyRate: Double = 0.0,
    val monthlyRate: Double? = null,
    val languages: List<String> = emptyList(),
    val workType: WorkType = WorkType.PART_TIME,
    val availability: Map<String, DayAvailability> = emptyMap(),
    val ratingAvg: Double = 0.0,
    val ratingCount: Int = 0,
    val isVerifiedPhoto: Boolean = false,
    val isVerifiedId: Boolean = false,
    val isVerifiedBackground: Boolean = false,
    val distanceKm: Double? = null,
    val isOnline: Boolean = false
)

data class DayAvailability(
    val morning: Boolean = false,
    val afternoon: Boolean = false,
    val evening: Boolean = false
)

enum class Skill(val label: String) {
    CLEANING("Cleaning"), COOKING("Cooking"), LAUNDRY("Laundry"),
    IRONING("Ironing"), CHILDCARE("Childcare"), ELDERLY_CARE("Elderly Care")
}

enum class WorkType { PART_TIME, FULL_TIME, BOTH }

data class Location(
    val latitude: Double,
    val longitude: Double,
    val address: String = "",
    val city: String = ""
)

data class AuthSession(
    val userId: String,
    val phone: String,
    val role: UserRole,
    val displayName: String,
    val accessToken: String = "",
    val refreshToken: String = "",
    val isDemo: Boolean = false,
    val isLoggedIn: Boolean = true
)
