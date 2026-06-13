package com.worldcup.calendar2026.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.worldcup.calendar2026.ui.components.StandingRowItem
import com.worldcup.calendar2026.ui.components.StandingsHeaderRow
import com.worldcup.calendar2026.ui.components.StateContainer

@Composable
fun StandingsScreen(viewModel: StandingsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    StateContainer(
        state = state,
        onRetry = viewModel::refresh,
        emptyMessage = "Standings appear once the group stage begins"
    ) { groups ->
        LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
            groups.forEach { group ->
                item(key = "title-${group.groupName}") {
                    Text(
                        text = group.groupName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 12.dp, top = 18.dp, bottom = 4.dp)
                    )
                }
                item(key = "head-${group.groupName}") { StandingsHeaderRow() }
                item(key = "div-${group.groupName}") { HorizontalDivider() }
                group.rows.forEach { row ->
                    item(key = "${group.groupName}-${row.team.id}") {
                        // Top two of each group advance.
                        StandingRowItem(row = row, qualifiesTop2 = row.rank <= 2)
                    }
                }
                item { Spacer(Modifier.size(4.dp)) }
            }
        }
    }
}
