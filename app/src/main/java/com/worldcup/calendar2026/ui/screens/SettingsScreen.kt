package com.worldcup.calendar2026.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.worldcup.calendar2026.data.remote.dto.StatusResponseDto
import com.worldcup.calendar2026.ui.UiState

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val savedKey by viewModel.apiKey.collectAsStateWithLifecycle()
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    var draftKey by rememberSaveable { mutableStateOf(savedKey) }
    var saved by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text("Configuration", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = draftKey,
            onValueChange = {
                draftKey = it
                saved = false
            },
            label = { Text("API Key") },
            placeholder = { Text("Enter your API-Football key") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
        ) {
            OutlinedButton(
                onClick = { viewModel.testConnection() },
                enabled = draftKey.isNotBlank() && connectionState !is UiState.Loading
            ) {
                Text("Test Connection")
            }
            Button(
                onClick = {
                    viewModel.saveApiKey(draftKey)
                    saved = true
                },
                enabled = draftKey.isNotBlank()
            ) {
                Text("Save")
            }
        }
        if (saved) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "API key saved. It will be used for all future requests.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(24.dp))
        connectionState?.let { state ->
            ConnectionStatusCard(state)
        }
    }
}

@Composable
private fun ConnectionStatusCard(state: UiState<StatusResponseDto>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (state) {
                is UiState.Success -> MaterialTheme.colorScheme.primaryContainer
                is UiState.Error -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        when (state) {
            is UiState.Loading -> Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Text("Testing connection…", style = MaterialTheme.typography.bodyMedium)
            }

            is UiState.Error -> Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Connection failed",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    state.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }

            is UiState.Success -> {
                val status = state.data
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "✓ Connected",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(8.dp))
                    StatusRow("Account", "${status.account.firstname} ${status.account.lastname}")
                    StatusRow("Plan", status.subscription.plan ?: "–")
                    StatusRow(
                        "Requests today",
                        "${status.requests.current} / ${status.requests.limitDay}"
                    )
                    StatusRow(
                        "Subscription active",
                        if (status.subscription.active) "Yes" else "No"
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
