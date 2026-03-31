package com.maidfinder.app.data.repository

import com.maidfinder.app.data.local.dao.*
import com.maidfinder.app.data.local.entity.*
import com.maidfinder.app.data.remote.api.*
import com.maidfinder.app.data.remote.dto.*
import com.maidfinder.app.domain.model.*
import com.maidfinder.app.domain.repository.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MaidRepositoryImpl @Inject constructor(
    private val maidDao: MaidProfileDao,
    private val savedMaidDao: SavedMaidDao,
    private val userApi: UserApiService
) : MaidRepository {

    override fun getNearbyMaids(lat: Double, lng: Double, radiusKm: Double, filters: MaidFilters): Flow<Resource<List<MaidProfile>>> = flow {
        emit(Resource.Loading)
        // Load from Room (seeded on first launch)
        maidDao.getAllMaids().first().let { cached ->
            emit(Resource.Success(cached.map { it.toDomain() }, DataSource.LOCAL))
        }
        // Network sync (best-effort, swallow errors)
        try {
            val response = userApi.searchMaids(lat, lng, radiusKm.toInt())
            if (response.success && response.data != null) {
                val maids = response.data.items.map { it.toEntity() }
                maidDao.insertAll(maids)
                emit(Resource.Success(maids.map { it.toDomain() }, DataSource.REMOTE))
            }
        } catch (_: Exception) {
            // Network unavailable - Room data is sufficient
        }
    }

    override suspend fun getMaidById(id: String): Resource<MaidProfile> {
        // Try Room first
        val cached = maidDao.getMaidById(id)
        if (cached != null) return Resource.Success(cached.toDomain(), DataSource.LOCAL)
        // Try network as fallback
        return try {
            val response = userApi.getMaidById(id)
            if (response.success && response.data != null) {
                Resource.Success(response.data.toEntity().toDomain())
            } else Resource.Error("Not found")
        } catch (_: Exception) { Resource.Error("Not found") }
    }

    override suspend fun saveMaid(maidId: String) = savedMaidDao.save(SavedMaidEntity(maidId))
    override suspend fun removeMaid(maidId: String) = savedMaidDao.remove(maidId)
    override suspend fun isMaidSaved(maidId: String): Boolean = savedMaidDao.isSaved(maidId)

    override fun getSavedMaids(): Flow<List<MaidProfile>> =
        maidDao.getSavedMaids().map { list -> list.map { it.toDomain() } }
}

private fun MaidProfileDto.toEntity() = MaidProfileEntity(
    userId = id, displayName = displayName, photoUrl = photoUrl, skills = skills,
    experienceYears = experienceYears, hourlyRate = hourlyRate, languages = languages,
    workType = workType, ratingAvg = ratingAvg, ratingCount = ratingCount,
    isVerifiedPhoto = isVerifiedPhoto, isVerifiedId = isVerifiedId, distanceKm = distanceKm, isOnline = isOnline
)

private fun MaidProfileEntity.toDomain() = MaidProfile(
    userId = userId, displayName = displayName, photoUrl = photoUrl,
    skills = skills.mapNotNull { runCatching { Skill.valueOf(it) }.getOrNull() },
    experienceYears = experienceYears, hourlyRate = hourlyRate, languages = languages,
    workType = runCatching { WorkType.valueOf(workType) }.getOrDefault(WorkType.PART_TIME),
    ratingAvg = ratingAvg, ratingCount = ratingCount, isVerifiedPhoto = isVerifiedPhoto,
    isVerifiedId = isVerifiedId, distanceKm = distanceKm, isOnline = isOnline
)
