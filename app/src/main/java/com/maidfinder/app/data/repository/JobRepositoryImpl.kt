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
class JobRepositoryImpl @Inject constructor(
    private val jobDao: JobDao,
    private val jobApi: JobApiService
) : JobRepository {

    override fun getJobs(lat: Double?, lng: Double?, filters: JobFilters): Flow<Resource<List<Job>>> = flow {
        emit(Resource.Loading)
        jobDao.getActiveJobs().first().let { cached ->
            emit(Resource.Success(cached.map { it.toDomain() }, DataSource.LOCAL))
        }
        try {
            val response = jobApi.getJobs(lat, lng, filters.jobType?.name, filters.minBudget, filters.maxBudget, filters.radiusKm.toInt())
            if (response.success && response.data != null) {
                val jobs = response.data.items.map { it.toEntity() }
                jobDao.insertAll(jobs)
                emit(Resource.Success(jobs.map { it.toDomain() }, DataSource.REMOTE))
            }
        } catch (_: Exception) { }
    }

    override suspend fun getJobById(id: String): Resource<Job> {
        val cached = jobDao.getJobById(id)
        if (cached != null) return Resource.Success(cached.toDomain(), DataSource.LOCAL)
        return try {
            val response = jobApi.getJobById(id)
            if (response.success && response.data != null) Resource.Success(response.data.toEntity().toDomain())
            else Resource.Error("Not found")
        } catch (_: Exception) { Resource.Error("Not found") }
    }

    override suspend fun createJob(job: Job): Result<Job> = try {
        val entity = JobEntity(
            id = "", clientId = job.clientId, clientName = job.clientName, clientRating = job.clientRating,
            jobType = job.jobType.name, title = job.title, description = job.description,
            locationLat = job.location.latitude, locationLng = job.location.longitude, locationAddress = job.location.address,
            dateStart = job.dateStart, dateEnd = job.dateEnd, budgetMin = job.budgetMin, budgetMax = job.budgetMax,
            budgetType = job.budgetType.name, status = "ACTIVE", applicantCount = 0, distanceKm = null,
            createdAt = System.currentTimeMillis()
        )
        jobDao.insert(entity)
        Result.success(entity.toDomain())
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun applyToJob(jobId: String, message: String?): Result<Unit> = Result.success(Unit)

    override fun getMyJobs(clientId: String): Flow<List<Job>> =
        jobDao.getMyJobs(clientId).map { list -> list.map { it.toDomain() } }
}

private fun JobDto.toEntity() = JobEntity(
    id = id, clientId = clientId, clientName = clientName, clientRating = clientRating,
    jobType = jobType, title = title, description = description,
    locationLat = locationLat, locationLng = locationLng, locationAddress = locationAddress,
    dateStart = dateStart, dateEnd = dateEnd, budgetMin = budgetMin, budgetMax = budgetMax,
    budgetType = budgetType, status = status, applicantCount = applicantCount,
    distanceKm = distanceKm, createdAt = System.currentTimeMillis()
)

private fun JobEntity.toDomain() = Job(
    id = id, clientId = clientId, clientName = clientName, clientRating = clientRating,
    jobType = runCatching { JobType.valueOf(jobType) }.getOrDefault(JobType.PART_TIME),
    title = title, description = description,
    location = Location(locationLat, locationLng, locationAddress),
    dateStart = dateStart, dateEnd = dateEnd,
    budgetMin = budgetMin, budgetMax = budgetMax,
    budgetType = runCatching { BudgetType.valueOf(budgetType) }.getOrDefault(BudgetType.HOURLY),
    status = runCatching { JobStatus.valueOf(status) }.getOrDefault(JobStatus.ACTIVE),
    applicantCount = applicantCount, distanceKm = distanceKm, createdAt = createdAt
)
