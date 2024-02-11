package com.athallah.newsapp.data

sealed class ResultState<out T> {
    data class Success<T>(val data: T) : ResultState<T>()
    data class Error(val e: Throwable) : ResultState<Nothing>()
    data object Loading : ResultState<Nothing>()
}