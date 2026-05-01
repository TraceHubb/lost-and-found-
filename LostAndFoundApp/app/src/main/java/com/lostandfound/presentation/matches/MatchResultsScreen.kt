package com.lostandfound.presentation.matches

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MatchResultsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Possible Matches",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn {
            items(5) { index ->
                MatchCard(matchScore = 85 - (index * 10))
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun MatchCard(matchScore: Int) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Possible Match", style = MaterialTheme.typography.titleMedium)
                Text("$matchScore% match", color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Item description matches your lost item")
            Text("Location: Similar area", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { /* TODO: View details */ }) {
                Text("View Details")
            }
        }
    }
}
