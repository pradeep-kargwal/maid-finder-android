package com.maidfinder.app.data.source

import com.maidfinder.app.data.model.BudgetType
import com.maidfinder.app.data.model.Job
import com.maidfinder.app.data.model.JobStatus
import com.maidfinder.app.data.model.JobType
import com.maidfinder.app.data.model.Location
import com.maidfinder.app.data.model.Shift
import com.maidfinder.app.data.model.ShiftType

/**
 * In-memory data source providing sample job data.
 */
object InMemoryJobDataSource {

    private val sampleJobs = mutableListOf(
        Job(
            id = "job_001",
            clientId = "client_001",
            clientName = "Rajesh V.",
            clientRating = 4.2,
            clientRatingCount = 5,
            jobType = JobType.PART_TIME,
            title = "Daily house cleaning",
            description = "Need someone for daily cleaning, 2 hours per day",
            location = Location(17.3850, 78.4867, "Madhapur", "Hyderabad"),
            dateStart = "2026-04-01",
            dateEnd = "2026-04-30",
            shifts = listOf(Shift(ShiftType.MORNING, "08:00", "10:00")),
            budgetMin = 100.0,
            budgetMax = 200.0,
            budgetType = BudgetType.HOURLY,
            status = JobStatus.ACTIVE,
            applicantCount = 3,
            distanceKm = 1.5
        ),
        Job(
            id = "job_002",
            clientId = "client_002",
            clientName = "Meera P.",
            clientRating = 4.5,
            clientRatingCount = 12,
            jobType = JobType.ONE_TIME,
            title = "Deep cleaning needed",
            description = "Full house deep cleaning for a 3BHK apartment",
            location = Location(17.4435, 78.3772, "Kondapur", "Hyderabad"),
            dateStart = "2026-04-05",
            shifts = listOf(Shift(ShiftType.MORNING, "09:00", "17:00")),
            budgetMin = 1500.0,
            budgetMax = 2000.0,
            budgetType = BudgetType.FIXED,
            status = JobStatus.ACTIVE,
            applicantCount = 5,
            distanceKm = 3.2
        ),
        Job(
            id = "job_003",
            clientId = "client_003",
            clientName = "Arun K.",
            clientRating = 3.8,
            clientRatingCount = 2,
            jobType = JobType.FULL_TIME,
            title = "Full-time house help needed",
            description = "Looking for a reliable full-time maid for cooking and cleaning",
            location = Location(17.4065, 78.4691, "Ameerpet", "Hyderabad"),
            dateStart = "2026-04-10",
            dateEnd = "2026-10-10",
            shifts = listOf(
                Shift(ShiftType.MORNING, "07:00", "12:00"),
                Shift(ShiftType.EVENING, "16:00", "19:00")
            ),
            budgetMin = 8000.0,
            budgetMax = 12000.0,
            budgetType = BudgetType.MONTHLY,
            status = JobStatus.ACTIVE,
            applicantCount = 8,
            distanceKm = 4.0
        ),
        Job(
            id = "job_004",
            clientId = "client_004",
            clientName = "Deepa S.",
            clientRating = 4.7,
            clientRatingCount = 18,
            jobType = JobType.PART_TIME,
            title = "Morning cooking and cleaning",
            description = "Need help with morning cooking and light cleaning",
            location = Location(17.3616, 78.4747, "Secunderabad", "Hyderabad"),
            dateStart = "2026-04-01",
            dateEnd = "2026-06-30",
            shifts = listOf(Shift(ShiftType.MORNING, "06:00", "10:00")),
            budgetMin = 120.0,
            budgetMax = 180.0,
            budgetType = BudgetType.HOURLY,
            status = JobStatus.ACTIVE,
            applicantCount = 2,
            distanceKm = 2.8
        ),
        Job(
            id = "job_005",
            clientId = "client_005",
            clientName = "Vikram N.",
            clientRating = 4.0,
            clientRatingCount = 3,
            jobType = JobType.ONE_TIME,
            title = "Party cleanup assistance",
            description = "Need help cleaning up after a house party",
            location = Location(17.4933, 78.3947, "Kukatpally", "Hyderabad"),
            dateStart = "2026-04-12",
            shifts = listOf(Shift(ShiftType.EVENING, "18:00", "22:00")),
            budgetMin = 800.0,
            budgetMax = 1000.0,
            budgetType = BudgetType.FIXED,
            status = JobStatus.ACTIVE,
            applicantCount = 1,
            distanceKm = 6.1
        )
    )

    fun getAllJobs(): List<Job> = sampleJobs.toList()

    fun getJobById(jobId: String): Job? =
        sampleJobs.find { it.id == jobId }

    fun addJob(job: Job) {
        sampleJobs.add(0, job)
    }

    fun updateJob(job: Job) {
        val index = sampleJobs.indexOfFirst { it.id == job.id }
        if (index >= 0) {
            sampleJobs[index] = job
        }
    }
}
