package com.worldcup.calendar2026.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.worldcup.calendar2026.domain.model.Match
import com.worldcup.calendar2026.ui.components.DateHeader
import com.worldcup.calendar2026.ui.components.MatchCard
import com.worldcup.calendar2026.ui.components.StateContainer
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val headerFmt: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault())

/** Full tournament calendar, grouped by date with sticky headers. */
@Composable
fun CalendarScreen(viewModel: CalendarViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    StateContainer(state = state, onRetry = viewModel::refresh, emptyMessage = "No fixtures published yet") { matches ->
        val grouped = matches.groupBy { it.localDate }.toSortedMap()
        LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
            grouped.forEach { (date, dayMatches) ->
                item(key = "header-$date") { DateHeader(date.format(headerFmt)) }
                items(dayMatches, key = { it.id }) { MatchCard(it) }
            }
        }
    }
}

@Composable
fun LiveScreen(viewModel: LiveViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    StateContainer(
        state = state,
        onRetry = viewModel::refresh,
        emptyMessage = "No matches are live right now"
    ) { matches ->
        MatchList(matches)
    }
}

@Composable
fun NextDayScreen(viewModel: NextDayViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    StateContainer(
        state = state,
        onRetry = viewModel::refresh,
        emptyMessage = "No matches scheduled for ${viewModel.date.format(headerFmt)}"
    ) { matches ->
        LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
            item { DateHeader(viewModel.date.format(headerFmt)) }
            items(matches, key = { it.id }) { MatchCard(it) }
        }
    }
}

@Composable
private fun MatchList(matches: List<Match>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(vertical = 8.dp)) {
        items(matches, key = { it.id }) { MatchCard(it) }
    }
}
