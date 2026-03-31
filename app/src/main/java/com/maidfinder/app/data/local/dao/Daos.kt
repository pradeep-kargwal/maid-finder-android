package com.maidfinder.app.data.local.dao

import androidx.room.*
import com.maidfinder.app.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUser(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE role = 'MAID' ORDER BY lastSyncedAt DESC")
    fun getAllMaids(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUser(id: String)
}

@Dao
interface MaidProfileDao {
    @Query("SELECT * FROM maid_profiles ORDER BY distanceKm ASC")
    fun getAllMaids(): Flow<List<MaidProfileEntity>>

    @Query("SELECT * FROM maid_profiles WHERE userId = :id")
    suspend fun getMaidById(id: String): MaidProfileEntity?

    @Query("SELECT * FROM maid_profiles WHERE userId IN (SELECT maidId FROM saved_maids)")
    fun getSavedMaids(): Flow<List<MaidProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(maids: List<MaidProfileEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(maid: MaidProfileEntity)

    @Query("DELETE FROM maid_profiles WHERE lastSyncedAt < :threshold")
    suspend fun deleteStale(threshold: Long)
}

@Dao
interface JobDao {
    @Query("SELECT * FROM jobs WHERE status = 'ACTIVE' ORDER BY createdAt DESC")
    fun getActiveJobs(): Flow<List<JobEntity>>

    @Query("SELECT * FROM jobs WHERE id = :id")
    suspend fun getJobById(id: String): JobEntity?

    @Query("SELECT * FROM jobs WHERE clientId = :clientId ORDER BY createdAt DESC")
    fun getMyJobs(clientId: String): Flow<List<JobEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(jobs: List<JobEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(job: JobEntity)

    @Query("DELETE FROM jobs WHERE status != 'ACTIVE' AND lastSyncedAt < :threshold")
    suspend fun deleteStale(threshold: Long)
}

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings WHERE clientId = :userId OR maidId = :userId ORDER BY createdAt DESC")
    fun getBookings(userId: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE (clientId = :userId OR maidId = :userId) AND status = :status ORDER BY createdAt DESC")
    fun getBookingsByStatus(userId: String, status: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE id = :id")
    suspend fun getBookingById(id: String): BookingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bookings: List<BookingEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(booking: BookingEntity)

    @Query("UPDATE bookings SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM conversations ORDER BY updatedAt DESC")
    fun getConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY createdAt ASC")
    suspend fun getMessages(conversationId: String): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Query("UPDATE messages SET isRead = 1 WHERE conversationId = :conversationId AND isRead = 0")
    suspend fun markAsRead(conversationId: String)

    @Query("UPDATE conversations SET unreadCount = 0 WHERE id = :conversationId")
    suspend fun clearUnread(conversationId: String)
}

@Dao
interface SavedMaidDao {
    @Query("SELECT * FROM saved_maids")
    fun getAll(): Flow<List<SavedMaidEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_maids WHERE maidId = :maidId)")
    suspend fun isSaved(maidId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entity: SavedMaidEntity)

    @Query("DELETE FROM saved_maids WHERE maidId = :maidId")
    suspend fun remove(maidId: String)
}
