package com.lostandfound.presentation.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lostandfound.utils.DatabaseCleanup
import com.lostandfound.utils.CleanupResult
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseCleanupScreen(
    onBack: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }
    var recordCounts by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var cleanupResult by remember { mutableStateOf<CleanupResult?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    
    // Load record counts on screen load
    LaunchedEffect(Unit) {
        try {
            recordCounts = DatabaseCleanup.getRecordCounts()
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.Warning, contentDescription = "Back")
            }
            Text(
                text = "Database Cleanup",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Warning Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = Color.Red
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "WARNING",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This will permanently delete all existing lost and found items, OTP sessions, and related data. This action cannot be undone!",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Only proceed if you want to start fresh with the new Security Questions system.",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Current Record Counts
        Text(
            text = "Current Database Records",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        recordCounts.forEach { (collection, count) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (collection) {
                            "items" -> "Lost & Found Items"
                            "otpSessions" -> "OTP Verification Sessions"
                            "auditLogs" -> "Audit Logs"
                            else -> collection
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = if (count >= 0) "$count records" else "Error loading",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (count >= 0) MaterialTheme.colorScheme.primary else Color.Red
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Cleanup Button
        Button(
            onClick = { showConfirmDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            enabled = !isLoading && recordCounts.values.any { it > 0 }
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = if (isLoading) "Cleaning up..." else "Clean Database",
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        // Results
        cleanupResult?.let { result ->
            Spacer(modifier = Modifier.height(24.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (result.hasErrors()) Color(0xFFFFEBEE) else Color(0xFFE8F5E8)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Cleanup Results",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = result.getSummary(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
    
    // Confirmation Dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Database Cleanup") },
            text = { 
                Text("Are you absolutely sure you want to delete all existing data? This cannot be undone!")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        isLoading = true
                        scope.launch {
                            try {
                                val result = DatabaseCleanup.performFullCleanup()
                                cleanupResult = result
                                // Refresh counts
                                recordCounts = DatabaseCleanup.getRecordCounts()
                            } catch (e: Exception) {
                                // Handle error
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                ) {
                    Text("DELETE ALL", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}