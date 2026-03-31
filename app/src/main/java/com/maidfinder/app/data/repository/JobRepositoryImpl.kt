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
            if (cached.isNotEmpty()) emit(Resource.Success(cached.map { it.toDomain() }, DataSource.LOCAL))
        }
        try {
            val response = jobApi.getJobs(lat, lng, filters.jobType?.name, filters.minBudget, filters.maxBudget, filters.radiusKm.toInt())
            if (response.success && response.data != null) {
                val jobs = response.data.items.map { it.toEntity() }
                jobDao.insertAll(jobs)
                emit(Resource.Success(jobs.map { it.toDomain() }, DataSource.REMOTE))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }

    override suspend fun getJobById(id: String): Resource<Job> {
        return try {
            val response = jobApi.getJobById(id)
            if (response.success && response.data != null) {
                Resource.Success(response.data.toEntity().toDomain())
            } else {
                jobDao.getJobById(id)?.let { Resource.Success(it.toDomain(), DataSource.LOCAL) }
                    ?: Resource.Error("Not found")
            }
        } catch (e: Exception) {
            jobDao.getJobById(id)?.let { Resource.Success(it.toDomain(), DataSource.LOCAL) }
                ?: Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun createJob(job: Job): Result<Job> = try {
        val request = CreateJobRequest(
            job.jobType.name, job.title, job.description,
            job.location.latitude, job.location.longitude, job.location.address,
            job.dateStart, job.dateEnd, job.budgetMin, job.budgetMax, job.budgetType.name
        )
        val response = jobApi.createJob(request)
        if (response.success && response.data != null) {
            val entity = response.data.toEntity()
            jobDao.insert(entity)
            Result.success(entity.toDomain())
        } else Result.failure(Exception(response.error?.message ?: "Failed"))
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun applyToJob(jobId: String, message: String?): Result<Unit> = try {
        val response = jobApi.applyToJob(jobId, ApplyRequest(message))
        if (response.success) Result.success(Unit) else Result.failure(Exception(response.error?.message))
    } catch (e: Exception) { Result.failure(e) }

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
