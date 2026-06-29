package com.parentdashboard.presentation.ui.screens.consent

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.parentdashboard.R
import com.parentdashboard.presentation.viewmodel.ConsentViewModel

@Composable
fun ConsentScreen(onComplete: () -> Unit, viewModel: ConsentViewModel = hiltViewModel()) {
    val loading by viewModel.loading.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.consent_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.consent_message), style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(32.dp))
        if (loading) {
            CircularProgressIndicator()
        } else {
            Button(onClick = { viewModel.acceptAll(onComplete) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.consent_accept_all))
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { viewModel.acceptEssentialOnly(onComplete) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.consent_essential_only))
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { viewModel.denyAll(onComplete) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.consent_deny))
            }
        }
    }
}
