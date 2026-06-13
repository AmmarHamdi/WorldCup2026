package com.worldcup.calendar2026.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worldcup.calendar2026.data.ApiKeyStore
import com.worldcup.calendar2026.data.remote.dto.StatusResponseDto
import com.worldcup.calendar2026.data.repository.WorldCupRepository
import com.worldcup.calendar2026.domain.model.GroupStanding
import com.worldcup.calendar2026.domain.model.Match
import com.worldcup.calendar2026.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/** Full match calendar, grouped by date in the UI. */
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repo: WorldCupRepository
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<List<Match>>>(UiState.Loading)
    val state = _state.asStateFlow()

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        _state.value = UiState.Loading
        runCatching { repo.fixtures() }
            .onSuccess { _state.value = UiState.Success(it) }
            .onFailure { _state.value = UiState.Error(it.message ?: "Could not load fixtures") }
    }
}

/** Live matches — fetched on demand (no automatic polling). */
@HiltViewModel
class LiveViewModel @Inject constructor(
    private val repo: WorldCupRepository
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<List<Match>>>(UiState.Loading)
    val state = _state.asStateFlow()

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        _state.value = UiState.Loading
        runCatching { repo.liveMatches() }
            .onSuccess { _state.value = UiState.Success(it) }
            .onFailure { _state.value = UiState.Error(it.message ?: "Could not load live matches") }
    }
}

/** Tomorrow's fixtures. */
@HiltViewModel
class NextDayViewModel @Inject constructor(
    private val repo: WorldCupRepository
) : ViewModel() {
    val date: LocalDate = LocalDate.now().plusDays(1)

    private val _state = MutableStateFlow<UiState<List<Match>>>(UiState.Loading)
    val state = _state.asStateFlow()

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        _state.value = UiState.Loading
        runCatching { repo.fixturesOn(date) }
            .onSuccess { _state.value = UiState.Success(it) }
            .onFailure { _state.value = UiState.Error(it.message ?: "Could not load fixtures") }
    }
}

/** Group standings / classement. */
@HiltViewModel
class StandingsViewModel @Inject constructor(
    private val repo: WorldCupRepository
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<List<GroupStanding>>>(UiState.Loading)
    val state = _state.asStateFlow()

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        _state.value = UiState.Loading
        runCatching { repo.standings() }
            .onSuccess { _state.value = UiState.Success(it) }
            .onFailure { _state.value = UiState.Error(it.message ?: "Could not load standings") }
    }
}

/** Settings — manages the API key used for all network requests. */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val apiKeyStore: ApiKeyStore,
    private val repo: WorldCupRepository
) : ViewModel() {
    private val _apiKey = MutableStateFlow(apiKeyStore.getKey())
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private val _connectionState = MutableStateFlow<UiState<StatusResponseDto>?>(null)
    val connectionState: StateFlow<UiState<StatusResponseDto>?> = _connectionState.asStateFlow()

    fun saveApiKey(key: String) {
        apiKeyStore.setKey(key)
        _apiKey.value = apiKeyStore.getKey()
        _connectionState.value = null
    }

    fun testConnection() = viewModelScope.launch {
        _connectionState.value = UiState.Loading
        runCatching { repo.checkStatus() }
            .onSuccess { _connectionState.value = UiState.Success(it) }
            .onFailure { _connectionState.value = UiState.Error(it.message ?: "Connection failed") }
    }
}
