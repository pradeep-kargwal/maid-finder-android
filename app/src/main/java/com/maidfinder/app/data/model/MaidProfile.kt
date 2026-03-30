package com.maidfinder.app.data.model

/**
 * Extended profile for a maid with skills, rates, and availability.
 */
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
    val location: Location? = null,
    val isOnline: Boolean = false,
    val distanceKm: Double? = null
)

enum class Skill(val label: String) {
    CLEANING("Cleaning"),
    COOKING("Cooking"),
    LAUNDRY("Laundry"),
    IRONING("Ironing"),
    CHILDCARE("Childcare"),
    ELDERLY_CARE("Elderly Care")
}

enum class WorkType {
    PART_TIME, FULL_TIME, BOTH
}

data class DayAvailability(
    val morning: Boolean = false,
    val afternoon: Boolean = false,
    val evening: Boolean = false
)

data class Location(
    val latitude: Double,
    val longitude: Double,
    val address: String = "",
    val city: String = ""
)
