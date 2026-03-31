package com.maidfinder.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.maidfinder.app.data.local.dao.*
import com.maidfinder.app.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        MaidProfileEntity::class,
        JobEntity::class,
        BookingEntity::class,
        MessageEntity::class,
        ConversationEntity::class,
        SavedMaidEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(ListStringConverter::class)
abstract class MaidFinderDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun maidProfileDao(): MaidProfileDao
    abstract fun jobDao(): JobDao
    abstract fun bookingDao(): BookingDao
    abstract fun messageDao(): MessageDao
    abstract fun savedMaidDao(): SavedMaidDao
}
