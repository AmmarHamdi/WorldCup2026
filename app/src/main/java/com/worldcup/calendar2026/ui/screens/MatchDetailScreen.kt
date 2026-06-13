package com.worldcup.calendar2026.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Rectangle
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.worldcup.calendar2026.domain.model.EventType
import com.worldcup.calendar2026.domain.model.Lineup
import com.worldcup.calendar2026.domain.model.Match
import com.worldcup.calendar2026.domain.model.MatchEvent
import com.worldcup.calendar2026.domain.model.MatchState
import com.worldcup.calendar2026.domain.model.MatchStatistic
import com.worldcup.calendar2026.ui.UiState
import java.time.format.DateTimeFormatter
import java.util.Locale

private val timeFmt: DateTimeFormatter =
    DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailScreen(
    onBack: () -> Unit,
    viewModel: MatchDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Match Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        when (val s = state) {
            is UiState.Loading -> Box(
                Modifier.fillMaxSize().padding(innerPadding),
                Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            is UiState.Error -> Box(
                Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
                Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(s.message, textAlign = TextAlign.Center)
                    Spacer(Modifier.size(12.dp))
                    Button(onClick = viewModel::refresh) { Text("Retry") }
                }
            }

            is UiState.Success -> {
                val data = s.data
                MatchDetailContent(
                    match = data.match,
                    events = data.events,
                    homeLineup = data.homeLineup,
                    awayLineup = data.awayLineup,
                    statistics = data.statistics,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun MatchDetailContent(
    match: Match,
    events: List<MatchEvent>,
    homeLineup: Lineup?,
    awayLineup: Lineup?,
    statistics: List<MatchStatistic>,
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("Events", "Lineups", "Stats")

    Column(modifier.fillMaxSize()) {
        ScoreHeader(match)
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        when (selectedTab) {
            0 -> EventsTab(events, match)
            1 -> LineupsTab(homeLineup, awayLineup)
            2 -> StatsTab(statistics, match)
        }
    }
}

// ---- Score Header -----------------------------------------------------------

@Composable
private fun ScoreHeader(match: Match) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = match.round,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home team
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    AsyncImage(
                        model = match.home.logoUrl,
                        contentDescription = match.home.name,
                        modifier = Modifier.size(48.dp).clip(CircleShape)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = match.home.name,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }

                // Score or time
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(80.dp)
                ) {
                    if (match.state == MatchState.SCHEDULED) {
                        Text(
                            text = match.kickoff.format(timeFmt),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "${match.homeGoals ?: 0} – ${match.awayGoals ?: 0}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (match.isLive) {
                        Text(
                            text = match.elapsed?.let { "$it'" } ?: "LIVE",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    } else if (match.isFinished) {
                        Text(
                            text = match.statusShort,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Away team
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    AsyncImage(
                        model = match.away.logoUrl,
                        contentDescription = match.away.name,
                        modifier = Modifier.size(48.dp).clip(CircleShape)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = match.away.name,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
            match.venue?.let {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = listOfNotNull(it, match.city).joinToString(" · "),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

// ---- Events Tab -------------------------------------------------------------

@Composable
private fun EventsTab(events: List<MatchEvent>, match: Match) {
    if (events.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(24.dp), Alignment.Center) {
            Text("No events available", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
    ) {
        items(events) { event ->
            EventRow(event, isHomeTeam = event.teamId == match.home.id)
        }
    }
}

@Composable
private fun EventRow(event: MatchEvent, isHomeTeam: Boolean) {
    val minuteText = if (event.extraMinute != null) "${event.minute}+${event.extraMinute}'" else "${event.minute}'"

    val icon = when (event.type) {
        EventType.GOAL -> Icons.Filled.SportsSoccer
        EventType.CARD -> Icons.Filled.Rectangle
        EventType.SUBSTITUTION -> Icons.Filled.CompareArrows
        EventType.VAR -> Icons.Filled.Visibility
    }

    val iconTint = when (event.type) {
        EventType.GOAL -> MaterialTheme.colorScheme.primary
        EventType.CARD -> if (event.detail?.contains("Yellow", ignoreCase = true) == true)
            Color(0xFFFFD600) else MaterialTheme.colorScheme.error
        EventType.SUBSTITUTION -> MaterialTheme.colorScheme.tertiary
        EventType.VAR -> MaterialTheme.colorScheme.secondary
    }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Minute column
        Text(
            text = minuteText,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(48.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Divider line
        Box(
            Modifier
                .width(2.dp)
                .height(36.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )

        Spacer(Modifier.width(12.dp))

        // Icon
        Icon(icon, contentDescription = event.type.name, tint = iconTint, modifier = Modifier.size(20.dp))

        Spacer(Modifier.width(10.dp))

        // Player info
        Column(Modifier.weight(1f)) {
            Text(
                text = event.playerName,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            event.assistName?.let { assist ->
                Text(
                    text = assist,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            event.detail?.let { detail ->
                Text(
                    text = detail,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Team side indicator
        Text(
            text = if (isHomeTeam) "H" else "A",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
}

// ---- Lineups Tab ------------------------------------------------------------

@Composable
private fun LineupsTab(homeLineup: Lineup?, awayLineup: Lineup?) {
    if (homeLineup == null && awayLineup == null) {
        Box(Modifier.fillMaxSize().padding(24.dp), Alignment.Center) {
            Text("Lineups not available yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
    ) {
        // Starting XI header
        item {
            Text(
                "Starting XI",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Team name headers
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Text(
                    text = homeLineup?.teamName ?: "",
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    fontSize = 14.sp
                )
                Text(
                    text = awayLineup?.teamName ?: "",
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End,
                    fontSize = 14.sp
                )
            }
        }

        // Starting XI rows side by side
        val maxRows = maxOf(
            homeLineup?.startingXI?.size ?: 0,
            awayLineup?.startingXI?.size ?: 0
        )
        items(maxRows) { index ->
            val homePlayer = homeLineup?.startingXI?.getOrNull(index)
            val awayPlayer = awayLineup?.startingXI?.getOrNull(index)
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 3.dp)
            ) {
                // Home player
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    if (homePlayer != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "${homePlayer.number}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = homePlayer.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 13.sp
                        )
                    }
                }

                // Away player
                Row(
                    Modifier.weight(1f),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (awayPlayer != null) {
                        Text(
                            text = awayPlayer.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 13.sp
                        )
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "${awayPlayer.number}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Coaches
        if (homeLineup?.coach != null || awayLineup?.coach != null) {
            item { Spacer(Modifier.height(12.dp)) }
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = homeLineup?.coach?.let { "Coach: $it" } ?: "",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = awayLineup?.coach?.let { "Coach: $it" } ?: "",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

// ---- Stats Tab --------------------------------------------------------------

@Composable
private fun StatsTab(statistics: List<MatchStatistic>, match: Match) {
    val keyStats = listOf(
        "Ball Possession", "Total Shots", "Shots on Goal",
        "Fouls", "Corner Kicks", "Offsides", "Yellow Cards", "Red Cards"
    )

    val filtered = if (statistics.isNotEmpty()) {
        val byType = statistics.associateBy { it.type }
        keyStats.mapNotNull { byType[it] } + statistics.filter { it.type !in keyStats }
    } else {
        emptyList()
    }

    if (filtered.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(24.dp), Alignment.Center) {
            Text("Statistics not available yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
    ) {
        // Team name headers
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = match.home.name,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    fontSize = 13.sp
                )
                Text(
                    text = match.away.name,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End,
                    fontSize = 13.sp
                )
            }
        }

        items(filtered) { stat ->
            StatBarRow(stat)
        }
    }
}

@Composable
private fun StatBarRow(stat: MatchStatistic) {
    val homeValue = stat.home?.replace("%", "")?.trim()?.toFloatOrNull() ?: 0f
    val awayValue = stat.away?.replace("%", "")?.trim()?.toFloatOrNull() ?: 0f
    val total = homeValue + awayValue
    val homeRatio = if (total > 0f) homeValue / total else 0.5f

    Column(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
        Text(
            text = stat.type,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stat.home ?: "0",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.width(40.dp)
            )
            Box(Modifier.weight(1f).height(8.dp)) {
                // Background
                LinearProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                // Home bar (left to right)
                LinearProgressIndicator(
                    progress = { homeRatio },
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                )
            }
            Text(
                text = stat.away ?: "0",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.width(40.dp),
                textAlign = TextAlign.End
            )
        }
    }
}
