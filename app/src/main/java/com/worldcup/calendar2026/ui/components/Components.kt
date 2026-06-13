package com.worldcup.calendar2026.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.worldcup.calendar2026.domain.model.Match
import com.worldcup.calendar2026.domain.model.MatchState
import com.worldcup.calendar2026.domain.model.StandingRow
import com.worldcup.calendar2026.ui.UiState
import java.time.format.DateTimeFormatter
import java.util.Locale

private val timeFmt: DateTimeFormatter =
    DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())

/** Renders loading / error / empty / content for any list-bearing UiState. */
@Composable
fun <T> StateContainer(
    state: UiState<List<T>>,
    onRetry: () -> Unit,
    emptyMessage: String = "Nothing to show yet",
    content: @Composable (List<T>) -> Unit
) {
    when (state) {
        is UiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }
        is UiState.Error -> Box(Modifier.fillMaxSize().padding(24.dp), Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(state.message, textAlign = TextAlign.Center)
                Spacer(Modifier.size(12.dp))
                Button(onClick = onRetry) { Text("Retry") }
            }
        }
        is UiState.Success -> if (state.data.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(24.dp), Alignment.Center) {
                Text(emptyMessage, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            content(state.data)
        }
    }
}

@Composable
private fun StatusBadge(match: Match) {
    val (label, bg, fg) = when (match.state) {
        MatchState.LIVE -> Triple(
            match.elapsed?.let { "$it'" } ?: "LIVE",
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.onError
        )
        MatchState.FINISHED -> Triple("FT", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
        MatchState.POSTPONED -> Triple(match.statusShort, MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
        else -> Triple(match.kickoff.format(timeFmt), MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
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
private fun TeamRow(name: String, logoUrl: String?, goals: Int?, bold: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        AsyncImage(
            model = logoUrl,
            contentDescription = null,
            modifier = Modifier.size(24.dp).clip(CircleShape)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = name,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = goals?.toString() ?: "–",
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun MatchCard(match: Match, modifier: Modifier = Modifier) {
    val homeWon = match.isFinished && (match.homeGoals ?: 0) > (match.awayGoals ?: 0)
    val awayWon = match.isFinished && (match.awayGoals ?: 0) > (match.homeGoals ?: 0)

    Card(
        modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 5.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = match.round,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(match)
            }
            Spacer(Modifier.size(10.dp))
            TeamRow(match.home.name, match.home.logoUrl, match.homeGoals, bold = homeWon || match.isLive)
            Spacer(Modifier.size(6.dp))
            TeamRow(match.away.name, match.away.logoUrl, match.awayGoals, bold = awayWon || match.isLive)
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
fun DateHeader(text: String) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

// ---- Standings table --------------------------------------------------------

@Composable
fun StandingsHeaderRow() {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("#", Modifier.width(22.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Text("Team", Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        StatCell("P"); StatCell("W"); StatCell("D"); StatCell("L"); StatCell("GD"); StatCell("Pts", bold = true)
    }
}

@Composable
private fun StatCell(text: String, bold: Boolean = false) {
    Text(
        text = text,
        modifier = Modifier.width(26.dp),
        textAlign = TextAlign.Center,
        fontSize = 12.sp,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
    )
}

@Composable
fun StandingRowItem(row: StandingRow, qualifiesTop2: Boolean) {
    val accent = if (qualifiesTop2)
        MaterialTheme.colorScheme.primary.copy(alpha = 0.10f) else Color.Transparent
    Row(
        Modifier.fillMaxWidth().background(accent).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("${row.rank}", Modifier.width(22.dp), fontWeight = FontWeight.SemiBold)
        AsyncImage(
            model = row.team.logoUrl,
            contentDescription = null,
            modifier = Modifier.size(20.dp).clip(CircleShape)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            row.team.name,
            Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        StatCell("${row.played}")
        StatCell("${row.win}")
        StatCell("${row.draw}")
        StatCell("${row.lose}")
        StatCell(if (row.goalsDiff > 0) "+${row.goalsDiff}" else "${row.goalsDiff}")
        StatCell("${row.points}", bold = true)
    }
}
