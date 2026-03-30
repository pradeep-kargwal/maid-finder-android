package com.maidfinder.app.data

import com.maidfinder.app.data.repository.BookingRepository
import com.maidfinder.app.data.repository.InMemoryBookingRepository
import com.maidfinder.app.data.repository.InMemoryJobRepository
import com.maidfinder.app.data.repository.InMemoryMaidRepository
import com.maidfinder.app.data.repository.JobRepository
import com.maidfinder.app.data.repository.MaidRepository

/**
 * Simple service locator for dependency injection.
 * Provides singleton instances of repositories.
 */
object ServiceLocator {

    private var _maidRepository: MaidRepository? = null
    private var _jobRepository: JobRepository? = null
    private var _bookingRepository: BookingRepository? = null

    val maidRepository: MaidRepository
        get() = _maidRepository ?: InMemoryMaidRepository().also { _maidRepository = it }

    val jobRepository: JobRepository
        get() = _jobRepository ?: InMemoryJobRepository().also { _jobRepository = it }

    val bookingRepository: BookingRepository
        get() = _bookingRepository ?: InMemoryBookingRepository().also { _bookingRepository = it }

    fun reset() {
        _maidRepository = null
        _jobRepository = null
        _bookingRepository = null
    }
}
