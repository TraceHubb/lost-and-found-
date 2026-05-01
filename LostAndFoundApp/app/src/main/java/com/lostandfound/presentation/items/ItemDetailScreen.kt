package com.lostandfound.presentation.items

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ItemDetailScreen(itemId: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Item Details",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        // Placeholder for item image
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Text("Item Photo", modifier = Modifier.padding(16.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Item Name", style = MaterialTheme.typography.titleLarge)
        Text("Description goes here...", style = MaterialTheme.typography.bodyMedium)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Location: Campus Building A", style = MaterialTheme.typography.bodyMedium)
        Text("Date: January 15, 2024", style = MaterialTheme.typography.bodyMedium)
        Text("Status: Active", style = MaterialTheme.typography.bodyMedium)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { /* TODO: Claim item */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("This is Mine")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedButton(
            onClick = { /* TODO: Contact finder */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Contact Finder")
        }
    }
}
