package com.maidfinder.app.data.local

import com.maidfinder.app.data.local.dao.*
import com.maidfinder.app.data.local.entity.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Seeds Room with sample data on first launch so the app works
 * fully offline until a backend is connected.
 */
@Singleton
class DataSeeder @Inject constructor(
    private val maidDao: MaidProfileDao,
    private val jobDao: JobDao,
    private val bookingDao: BookingDao,
    private val messageDao: MessageDao
) {
    suspend fun seedIfNeeded() {
        val existing = maidDao.getMaidById("maid_001")
        if (existing != null) return // Already seeded

        maidDao.insertAll(sampleMaids)
        jobDao.insertAll(sampleJobs)
        bookingDao.insertAll(sampleBookings)
        sampleConversations.forEach { messageDao.insertConversation(it) }
        sampleMessages.forEach { messageDao.insertMessage(it) }
    }

    companion object {
        val sampleMaids = listOf(
            MaidProfileEntity("maid_001", "Lakshmi R.", null,
                listOf("CLEANING", "COOKING"), 3, 150.0, listOf("Telugu", "Hindi"),
                "BOTH", 4.5, 23, true, true, 2.3, true),
            MaidProfileEntity("maid_002", "Sunita K.", null,
                listOf("CLEANING", "LAUNDRY", "IRONING"), 5, 120.0, listOf("Hindi"),
                "PART_TIME", 4.2, 15, true, true, 3.1, false),
            MaidProfileEntity("maid_003", "Priya M.", null,
                listOf("COOKING", "CLEANING"), 2, 180.0, listOf("Telugu", "English"),
                "FULL_TIME", 4.8, 8, true, false, 5.7, true),
            MaidProfileEntity("maid_004", "Anita D.", null,
                listOf("CLEANING", "ELDERLY_CARE"), 8, 200.0, listOf("Hindi", "Bengali", "English"),
                "BOTH", 4.9, 42, true, true, 1.8, true),
            MaidProfileEntity("maid_005", "Kavitha S.", null,
                listOf("CHILDCARE", "CLEANING"), 4, 160.0, listOf("Telugu", "Tamil"),
                "PART_TIME", 4.3, 11, true, false, 4.2, false)
        )

        val sampleJobs = listOf(
            JobEntity("job_001", "demo_client_001", "Rajesh V.", 4.2, "PART_TIME",
                "Daily house cleaning", "Need someone for daily cleaning, 2 hours per day",
                17.3850, 78.4867, "Madhapur, Hyderabad", "2026-04-01", "2026-04-30",
                100.0, 200.0, "HOURLY", "ACTIVE", 3, 1.5, System.currentTimeMillis()),
            JobEntity("job_002", "demo_client_001", "Meera P.", 4.5, "ONE_TIME",
                "Deep cleaning needed", "Full house deep cleaning for a 3BHK apartment",
                17.4435, 78.3772, "Kondapur, Hyderabad", "2026-04-05", null,
                1500.0, 2000.0, "FIXED", "ACTIVE", 5, 3.2, System.currentTimeMillis()),
            JobEntity("job_003", "demo_client_001", "Arun K.", 3.8, "FULL_TIME",
                "Full-time house help needed", "Looking for reliable full-time maid for cooking and cleaning",
                17.4065, 78.4691, "Ameerpet, Hyderabad", "2026-04-10", "2026-10-10",
                8000.0, 12000.0, "MONTHLY", "ACTIVE", 8, 4.0, System.currentTimeMillis()),
            JobEntity("job_004", "demo_client_001", "Deepa S.", 4.7, "PART_TIME",
                "Morning cooking and cleaning", "Need help with morning cooking and light cleaning",
                17.3616, 78.4747, "Secunderabad, Hyderabad", "2026-04-01", "2026-06-30",
                120.0, 180.0, "HOURLY", "ACTIVE", 2, 2.8, System.currentTimeMillis()),
            JobEntity("job_005", "demo_client_001", "Vikram N.", 4.0, "ONE_TIME",
                "Party cleanup assistance", "Need help cleaning up after a house party",
                17.4933, 78.3947, "Kukatpally, Hyderabad", "2026-04-12", null,
                800.0, 1000.0, "FIXED", "ACTIVE", 1, 6.1, System.currentTimeMillis())
        )

        val sampleBookings = listOf(
            BookingEntity("book_001", "demo_client_001", "Priya M.", "maid_001", "Lakshmi R.",
                null, "CONFIRMED", "2026-04-01", "2026-04-30", 150.0, "HOURLY", 6000.0,
                System.currentTimeMillis() - 86400000),
            BookingEntity("book_002", "demo_client_001", "Priya M.", "maid_002", "Sunita K.",
                null, "COMPLETED", "2026-03-15", "2026-03-20", 120.0, "HOURLY", 1200.0,
                System.currentTimeMillis() - 604800000),
            BookingEntity("book_003", "demo_client_001", "Priya M.", "maid_004", "Anita D.",
                null, "PENDING", "2026-04-05", "2026-04-10", 200.0, "HOURLY", 2000.0,
                System.currentTimeMillis() - 3600000),
            BookingEntity("book_004", "client_002", "Meera P.", "maid_001", "Lakshmi R.",
                null, "IN_PROGRESS", "2026-03-28", "2026-04-28", 150.0, "HOURLY", 8000.0,
                System.currentTimeMillis() - 172800000)
        )

        val sampleConversations = listOf(
            ConversationEntity("conv_001", "maid_001", "Lakshmi R.", true,
                "I'll be there at 8 AM tomorrow!", "TEXT", 2, System.currentTimeMillis() - 300000),
            ConversationEntity("conv_002", "maid_002", "Sunita K.", false,
                "Thank you, see you then", "TEXT", 0, System.currentTimeMillis() - 3600000),
            ConversationEntity("conv_003", "maid_004", "Anita D.", true,
                "Is the job still available?", "TEXT", 1, System.currentTimeMillis() - 7200000)
        )

        val sampleMessages = listOf(
            MessageEntity("msg_001", "conv_001", "demo_client_001", "maid_001", "TEXT",
                "Hi! Are you available for cleaning tomorrow?", true, true, System.currentTimeMillis() - 500000),
            MessageEntity("msg_002", "conv_001", "maid_001", "demo_client_001", "TEXT",
                "Yes, I'm available. What time works for you?", true, true, System.currentTimeMillis() - 400000),
            MessageEntity("msg_003", "conv_001", "demo_client_001", "maid_001", "TEXT",
                "How about 8 AM?", true, true, System.currentTimeMillis() - 350000),
            MessageEntity("msg_004", "conv_001", "maid_001", "demo_client_001", "TEXT",
                "I'll be there at 8 AM tomorrow!", false, true, System.currentTimeMillis() - 300000),
            MessageEntity("msg_005", "conv_001", "demo_client_001", "maid_001", "SYSTEM",
                "Booking confirmed for April 1, 8:00 AM", true, true, System.currentTimeMillis() - 250000)
        )
    }
}
