package com.maidfinder.app.domain.model

sealed class Resource<out T> {
    data class Success<T>(val data: T, val source: DataSource = DataSource.REMOTE) : Resource<T>()
    data class Error(val message: String, val code: Int? = null) : Resource<Nothing>()
    data object Loading : Resource<Nothing>()
}

enum class DataSource { LOCAL, REMOTE }

inline fun <T> Resource<T>.onSuccess(action: (T) -> Unit): Resource<T> {
    if (this is Resource.Success) action(data)
    return this
}

inline fun <T> Resource<T>.onError(action: (String, Int?) -> Unit): Resource<T> {
    if (this is Resource.Error) action(message, code)
    return this
}
