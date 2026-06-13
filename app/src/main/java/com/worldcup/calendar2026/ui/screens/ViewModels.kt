package com.worldcup.calendar2026.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worldcup.calendar2026.data.repository.WorldCupRepository
import com.worldcup.calendar2026.domain.model.GroupStanding
import com.worldcup.calendar2026.domain.model.Match
import com.worldcup.calendar2026.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay
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

/** Live matches — polls every 30 seconds while the screen is in the back stack. */
@HiltViewModel
class LiveViewModel @Inject constructor(
    private val repo: WorldCupRepository
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<List<Match>>>(UiState.Loading)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            while (isActive) {
                runCatching { repo.liveMatches() }
                    .onSuccess { _state.value = UiState.Success(it) }
                    .onFailure {
                        if (_state.value is UiState.Loading) {
                            _state.value = UiState.Error(it.message ?: "Could not load live matches")
                        }
                    }
                delay(POLL_MILLIS)
            }
        }
    }

    private companion object { const val POLL_MILLIS = 30_000L }
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
