package com.maidfinder.app.data.repository

import com.maidfinder.app.data.model.MaidProfile
import com.maidfinder.app.data.model.Skill
import com.maidfinder.app.data.model.WorkType

/**
 * Repository interface for maid-related operations.
 */
interface MaidRepository {
    suspend fun getNearbyMaids(
        latitude: Double,
        longitude: Double,
        radiusKm: Double = 5.0,
        workType: WorkType? = null,
        minRate: Double? = null,
        maxRate: Double? = null,
        minExperience: Int? = null,
        languages: List<String>? = null,
        minRating: Double? = null,
        skills: List<Skill>? = null,
        verifiedOnly: Boolean = false
    ): List<MaidProfile>

    suspend fun getMaidById(maidId: String): MaidProfile?

    suspend fun saveMaid(maid: MaidProfile)

    suspend fun getSavedMaids(): List<MaidProfile>

    suspend fun saveMaidToBookmarks(maidId: String)

    suspend fun removeMaidFromBookmarks(maidId: String)

    suspend fun isMaidSaved(maidId: String): Boolean
}
