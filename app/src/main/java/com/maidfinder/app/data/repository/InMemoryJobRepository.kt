package com.maidfinder.app.data.repository

import com.maidfinder.app.data.model.Job
import com.maidfinder.app.data.model.JobStatus
import com.maidfinder.app.data.model.JobType
import com.maidfinder.app.data.source.InMemoryJobDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * In-memory implementation of [JobRepository].
 */
class InMemoryJobRepository : JobRepository {

    override suspend fun getJobs(
        latitude: Double?,
        longitude: Double?,
        jobType: JobType?,
        minBudget: Double?,
        maxBudget: Double?,
        radiusKm: Double,
        myJobsOnly: Boolean,
        clientId: String?
    ): List<Job> = withContext(Dispatchers.IO) {
        InMemoryJobDataSource.getAllJobs()
            .filter { job ->
                job.status == JobStatus.ACTIVE &&
                (jobType == null || job.jobType == jobType) &&
                (minBudget == null || job.budgetMax >= minBudget) &&
                (maxBudget == null || job.budgetMin <= maxBudget) &&
                (!myJobsOnly || clientId == null || job.clientId == clientId) &&
                (job.distanceKm == null || job.distanceKm <= radiusKm)
            }
            .sortedByDescending { it.createdAt }
    }

    override suspend fun getJobById(jobId: String): Job? =
        withContext(Dispatchers.IO) {
            InMemoryJobDataSource.getJobById(jobId)
        }

    override suspend fun createJob(job: Job): Job = withContext(Dispatchers.IO) {
        val newJob = job.copy(id = "job_${UUID.randomUUID().toString().take(8)}")
        InMemoryJobDataSource.addJob(newJob)
        newJob
    }

    override suspend fun updateJob(job: Job): Job = withContext(Dispatchers.IO) {
        InMemoryJobDataSource.updateJob(job)
        job
    }

    override suspend fun cancelJob(jobId: String) = withContext(Dispatchers.IO) {
        val job = InMemoryJobDataSource.getJobById(jobId)
        if (job != null) {
            InMemoryJobDataSource.updateJob(job.copy(status = JobStatus.CANCELLED))
        }
    }

    override suspend fun getMyJobs(clientId: String): List<Job> = withContext(Dispatchers.IO) {
        InMemoryJobDataSource.getAllJobs()
            .filter { it.clientId == clientId }
            .sortedByDescending { it.createdAt }
    }
}
