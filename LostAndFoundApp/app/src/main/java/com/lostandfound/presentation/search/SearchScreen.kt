package com.lostandfound.presentation.search

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedLocation by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Search & Filter",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search items") },
            placeholder = { Text("phone, wallet, keys...") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = selectedLocation,
            onValueChange = { selectedLocation = it },
            label = { Text("Location") },
            placeholder = { Text("City, campus, area...") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Category", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(modifier = Modifier.fillMaxWidth()) {
            FilterChip(
                selected = selectedCategory == "All",
                onClick = { selectedCategory = "All" },
                label = { Text("All") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(
                selected = selectedCategory == "Electronics",
                onClick = { selectedCategory = "Electronics" },
                label = { Text("Electronics") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(
                selected = selectedCategory == "Documents",
                onClick = { selectedCategory = "Documents" },
                label = { Text("Documents") }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { /* TODO: Apply filters */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Search")
        }
    }
}
