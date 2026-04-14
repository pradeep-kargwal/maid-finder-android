package com.maidfinder.app.data

import com.maidfinder.app.data.repository.AuthRepository
import com.maidfinder.app.data.repository.BookingRepository
import com.maidfinder.app.data.repository.InMemoryBookingRepository
import com.maidfinder.app.data.repository.InMemoryJobRepository
import com.maidfinder.app.data.repository.InMemoryMaidRepository
import com.maidfinder.app.data.repository.JobRepository
import com.maidfinder.app.data.repository.MaidRepository
import com.maidfinder.app.data.repository.MessageRepository

object ServiceLocator {

    private var _authRepository: AuthRepository? = null
    private var _maidRepository: MaidRepository? = null
    private var _jobRepository: JobRepository? = null
    private var _bookingRepository: BookingRepository? = null
    private var _messageRepository: MessageRepository? = null

    val authRepository: AuthRepository
        get() = _authRepository ?: AuthRepository().also { _authRepository = it }

    val maidRepository: MaidRepository
        get() = _maidRepository ?: InMemoryMaidRepository().also { _maidRepository = it }

    val jobRepository: JobRepository
        get() = _jobRepository ?: InMemoryJobRepository().also { _jobRepository = it }

    val bookingRepository: BookingRepository
        get() = _bookingRepository ?: InMemoryBookingRepository().also { _bookingRepository = it }

    val messageRepository: MessageRepository
        get() = _messageRepository ?: MessageRepository().also { _messageRepository = it }

    fun reset() {
        _authRepository = null
        _maidRepository = null
        _jobRepository = null
        _bookingRepository = null
        _messageRepository = null
    }
}
