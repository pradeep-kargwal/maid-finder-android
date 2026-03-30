package com.maidfinder.app.data.repository

import com.maidfinder.app.data.model.MaidProfile
import com.maidfinder.app.data.model.Skill
import com.maidfinder.app.data.model.WorkType
import com.maidfinder.app.data.source.InMemoryMaidDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * In-memory implementation of [MaidRepository].
 * Filters and returns sample maid data for the initial milestone.
 */
class InMemoryMaidRepository : MaidRepository {

    private val savedMaidIds = mutableSetOf<String>()

    override suspend fun getNearbyMaids(
        latitude: Double,
        longitude: Double,
        radiusKm: Double,
        workType: WorkType?,
        minRate: Double?,
        maxRate: Double?,
        minExperience: Int?,
        languages: List<String>?,
        minRating: Double?,
        skills: List<Skill>?,
        verifiedOnly: Boolean
    ): List<MaidProfile> = withContext(Dispatchers.IO) {
        InMemoryMaidDataSource.getAllMaids()
            .filter { maid ->
                (workType == null || maid.workType == workType || maid.workType == WorkType.BOTH) &&
                (minRate == null || maid.hourlyRate >= minRate) &&
                (maxRate == null || maid.hourlyRate <= maxRate) &&
                (minExperience == null || maid.experienceYears >= minExperience) &&
                (languages == null || maid.languages.any { it in languages }) &&
                (minRating == null || maid.ratingAvg >= minRating) &&
                (skills == null || maid.skills.any { it in skills }) &&
                (!verifiedOnly || maid.isVerifiedPhoto) &&
                (maid.distanceKm == null || maid.distanceKm <= radiusKm)
            }
            .sortedBy { it.distanceKm ?: Double.MAX_VALUE }
    }

    override suspend fun getMaidById(maidId: String): MaidProfile? =
        withContext(Dispatchers.IO) {
            InMemoryMaidDataSource.getMaidById(maidId)
        }

    override suspend fun saveMaid(maid: MaidProfile) {
        // In-memory: no-op for now
    }

    override suspend fun getSavedMaids(): List<MaidProfile> = withContext(Dispatchers.IO) {
        InMemoryMaidDataSource.getAllMaids()
            .filter { it.userId in savedMaidIds }
    }

    override suspend fun saveMaidToBookmarks(maidId: String) {
        savedMaidIds.add(maidId)
    }

    override suspend fun removeMaidFromBookmarks(maidId: String) {
        savedMaidIds.remove(maidId)
    }

    override suspend fun isMaidSaved(maidId: String): Boolean =
        maidId in savedMaidIds
}
