package com.worldcup.calendar2026.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.worldcup.calendar2026.domain.model.Match
import com.worldcup.calendar2026.domain.model.MatchState
import com.worldcup.calendar2026.ui.UiState
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateFmt: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM · HH:mm", Locale.getDefault())

@Composable
fun KnockoutScreen(
    onMatchClick: (Int) -> Unit = {},
    viewModel: KnockoutViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val s = state) {
        is UiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }
        is UiState.Error -> Box(Modifier.fillMaxSize().padding(24.dp), Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(s.message, textAlign = TextAlign.Center)
                Spacer(Modifier.size(12.dp))
                Button(onClick = viewModel::refresh) { Text("Retry") }
            }
        }
        is UiState.Success -> {
            val rounds = s.data
            if (rounds.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(24.dp), Alignment.Center) {
                    Text(
                        "No knockout matches available yet",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    rounds.forEach { (round, matches) ->
                        item(key = "round-$round") {
                            RoundHeader(round)
                        }
                        items(matches, key = { it.id }) { match ->
                            KnockoutMatchCard(match, onClick = { onMatchClick(match.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoundHeader(round: String) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = round,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun KnockoutMatchCard(match: Match, onClick: () -> Unit) {
    val homeWon = match.isFinished && (match.homeGoals ?: 0) > (match.awayGoals ?: 0)
    val awayWon = match.isFinished && (match.awayGoals ?: 0) > (match.homeGoals ?: 0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 5.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick
    ) {
        Column(Modifier.padding(14.dp)) {
            // Status row
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = match.kickoff.format(dateFmt),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                KnockoutStatusBadge(match)
            }
            Spacer(Modifier.size(10.dp))

            // Home team
            KnockoutTeamRow(
                name = match.home.name,
                logoUrl = match.home.logoUrl,
                goals = match.homeGoals,
                isWinner = homeWon
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            // Away team
            KnockoutTeamRow(
                name = match.away.name,
                logoUrl = match.away.logoUrl,
                goals = match.awayGoals,
                isWinner = awayWon
            )

            // Venue
            match.venue?.let {
                Spacer(Modifier.size(8.dp))
                Text(
                    text = listOfNotNull(it, match.city).joinToString(" · "),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun KnockoutStatusBadge(match: Match) {
    val (label, bg, fg) = when (match.state) {
        MatchState.LIVE -> Triple(
            match.elapsed?.let { "$it'" } ?: "LIVE",
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.onError
        )
        MatchState.FINISHED -> Triple(
            "FT",
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
        MatchState.POSTPONED -> Triple(
            match.statusShort,
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
        else -> Triple(
            "Scheduled",
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
    Surface(color = bg, shape = RoundedCornerShape(6.dp)) {
        Text(
            text = label,
            color = fg,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun KnockoutTeamRow(
    name: String,
    logoUrl: String?,
    goals: Int?,
    isWinner: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        AsyncImage(
            model = logoUrl,
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = name,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = goals?.toString() ?: "–",
            fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Medium
        )
    }
}
