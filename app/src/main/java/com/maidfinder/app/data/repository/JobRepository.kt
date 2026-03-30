package com.maidfinder.app.data.repository

import com.maidfinder.app.data.model.Job
import com.maidfinder.app.data.model.JobType

/**
 * Repository interface for job-related operations.
 */
interface JobRepository {
    suspend fun getJobs(
        latitude: Double? = null,
        longitude: Double? = null,
        jobType: JobType? = null,
        minBudget: Double? = null,
        maxBudget: Double? = null,
        radiusKm: Double = 5.0,
        myJobsOnly: Boolean = false,
        clientId: String? = null
    ): List<Job>

    suspend fun getJobById(jobId: String): Job?

    suspend fun createJob(job: Job): Job

    suspend fun updateJob(job: Job): Job

    suspend fun cancelJob(jobId: String)

    suspend fun getMyJobs(clientId: String): List<Job>
}
