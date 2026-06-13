package com.worldcup.calendar2026.ui

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T, val cacheWarning: String? = null) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
