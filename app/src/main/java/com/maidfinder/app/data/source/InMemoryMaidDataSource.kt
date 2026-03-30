package com.maidfinder.app.data.source

import com.maidfinder.app.data.model.DayAvailability
import com.maidfinder.app.data.model.Location
import com.maidfinder.app.data.model.MaidProfile
import com.maidfinder.app.data.model.Skill
import com.maidfinder.app.data.model.WorkType

/**
 * In-memory data source providing sample maid data.
 * Replaces a remote API for the initial milestone.
 */
object InMemoryMaidDataSource {

    private val sampleMaids = listOf(
        MaidProfile(
            userId = "maid_001",
            displayName = "Lakshmi R.",
            skills = listOf(Skill.CLEANING, Skill.COOKING),
            experienceYears = 3,
            hourlyRate = 150.0,
            monthlyRate = 8000.0,
            languages = listOf("Telugu", "Hindi"),
            workType = WorkType.BOTH,
            availability = mapOf(
                "Mon" to DayAvailability(morning = true, afternoon = false),
                "Tue" to DayAvailability(morning = true, afternoon = true),
                "Wed" to DayAvailability(morning = true, afternoon = true),
                "Thu" to DayAvailability(morning = true, afternoon = false),
                "Fri" to DayAvailability(morning = true, afternoon = true)
            ),
            ratingAvg = 4.5,
            ratingCount = 23,
            isVerifiedPhoto = true,
            isVerifiedId = true,
            location = Location(17.3850, 78.4867, "Madhapur", "Hyderabad"),
            isOnline = true,
            distanceKm = 2.3
        ),
        MaidProfile(
            userId = "maid_002",
            displayName = "Sunita K.",
            skills = listOf(Skill.CLEANING, Skill.LAUNDRY, Skill.IRONING),
            experienceYears = 5,
            hourlyRate = 120.0,
            languages = listOf("Hindi"),
            workType = WorkType.PART_TIME,
            availability = mapOf(
                "Mon" to DayAvailability(morning = true, afternoon = true),
                "Tue" to DayAvailability(morning = true, afternoon = true),
                "Wed" to DayAvailability(morning = true, afternoon = false),
                "Thu" to DayAvailability(morning = true, afternoon = true),
                "Fri" to DayAvailability(morning = true, afternoon = true),
                "Sat" to DayAvailability(morning = true)
            ),
            ratingAvg = 4.2,
            ratingCount = 15,
            isVerifiedPhoto = true,
            isVerifiedId = true,
            isVerifiedBackground = true,
            location = Location(17.4435, 78.3772, "Kondapur", "Hyderabad"),
            isOnline = false,
            distanceKm = 3.1
        ),
        MaidProfile(
            userId = "maid_003",
            displayName = "Priya M.",
            skills = listOf(Skill.COOKING, Skill.CLEANING),
            experienceYears = 2,
            hourlyRate = 180.0,
            monthlyRate = 10000.0,
            languages = listOf("Telugu", "English"),
            workType = WorkType.FULL_TIME,
            availability = mapOf(
                "Mon" to DayAvailability(morning = true, afternoon = true, evening = true),
                "Tue" to DayAvailability(morning = true, afternoon = true, evening = true),
                "Wed" to DayAvailability(morning = true, afternoon = true, evening = true),
                "Thu" to DayAvailability(morning = true, afternoon = true, evening = true),
                "Fri" to DayAvailability(morning = true, afternoon = true, evening = true)
            ),
            ratingAvg = 4.8,
            ratingCount = 8,
            isVerifiedPhoto = true,
            location = Location(17.4933, 78.3947, "Kukatpally", "Hyderabad"),
            isOnline = true,
            distanceKm = 5.7
        ),
        MaidProfile(
            userId = "maid_004",
            displayName = "Anita D.",
            skills = listOf(Skill.CLEANING, Skill.ELDERLY_CARE),
            experienceYears = 8,
            hourlyRate = 200.0,
            languages = listOf("Hindi", "Bengali", "English"),
            workType = WorkType.BOTH,
            availability = mapOf(
                "Mon" to DayAvailability(morning = true),
                "Wed" to DayAvailability(morning = true),
                "Fri" to DayAvailability(morning = true)
            ),
            ratingAvg = 4.9,
            ratingCount = 42,
            isVerifiedPhoto = true,
            isVerifiedId = true,
            isVerifiedBackground = true,
            location = Location(17.3616, 78.4747, "Secunderabad", "Hyderabad"),
            isOnline = true,
            distanceKm = 1.8
        ),
        MaidProfile(
            userId = "maid_005",
            displayName = "Kavitha S.",
            skills = listOf(Skill.CHILDCARE, Skill.CLEANING),
            experienceYears = 4,
            hourlyRate = 160.0,
            languages = listOf("Telugu", "Tamil"),
            workType = WorkType.PART_TIME,
            availability = mapOf(
                "Mon" to DayAvailability(afternoon = true, evening = true),
                "Tue" to DayAvailability(afternoon = true, evening = true),
                "Wed" to DayAvailability(afternoon = true, evening = true),
                "Thu" to DayAvailability(afternoon = true, evening = true),
                "Fri" to DayAvailability(afternoon = true, evening = true)
            ),
            ratingAvg = 4.3,
            ratingCount = 11,
            isVerifiedPhoto = true,
            location = Location(17.4065, 78.4691, "Ameerpet", "Hyderabad"),
            isOnline = false,
            distanceKm = 4.2
        )
    )

    fun getAllMaids(): List<MaidProfile> = sampleMaids

    fun getMaidById(maidId: String): MaidProfile? =
        sampleMaids.find { it.userId == maidId }
}
