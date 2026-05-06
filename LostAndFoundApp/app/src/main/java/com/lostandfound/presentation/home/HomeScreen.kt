package com.lostandfound.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lostandfound.data.repositories.AuthRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onReportLost: () -> Unit,
    onReportFound: () -> Unit,
    onBrowseLost: () -> Unit,
    onBrowseFound: () -> Unit,
    onLogout: () -> Unit
) {
    var showReportDialog by remember { mutableStateOf(false) }
    var showBrowseDialog by remember { mutableStateOf(false) }
    
    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report Item") },
            text = { Text("What would you like to report?") },
            confirmButton = {
                Button(onClick = {
                    showReportDialog = false
                    onReportLost()
                }) {
                    Text("Report Lost Item")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    showReportDialog = false
                    onReportFound()
                }) {
                    Text("Report Found Item")
                }
            }
        )
    }
    
    if (showBrowseDialog) {
        BrowseSelectionDialog(
            onDismiss = { showBrowseDialog = false },
            onBrowseLost = onBrowseLost,
            onBrowseFound = onBrowseFound
        )
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        item {
            // Header with profile
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Campus Lost & Found",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = AuthRepository.currentUser?.email.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
        
        item {
            // Quick action icons
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    QuickActionIcon(
                        icon = Icons.Default.Add,
                        label = "Report",
                        color = Color(0xFFFF6B6B),
                        onClick = { showReportDialog = true }
                    )
                    QuickActionIcon(
                        icon = Icons.Default.Star,
                        label = "Matching",
                        color = Color(0xFFFFA500),
                        onClick = {}
                    )
                    QuickActionIcon(
                        icon = Icons.Default.Email,
                        label = "Chat",
                        color = Color(0xFF4A90E2),
                        onClick = {}
                    )
                    QuickActionIcon(
                        icon = Icons.Default.Search,
                        label = "Browse",
                        color = Color(0xFF9E9E9E),
                        onClick = { showBrowseDialog = true }
                    )
                    QuickActionIcon(
                        icon = Icons.Default.Check,
                        label = "Tasks",
                        color = Color(0xFF4CAF50),
                        onClick = {}
                    )
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        item {
            // Notifications section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Notifications",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    NotificationItem(
                        icon = Icons.Default.Email,
                        title = "Unanswered Messages",
                        count = 2,
                        iconColor = Color(0xFF4A90E2)
                    )
                    NotificationItem(
                        icon = Icons.Default.ShoppingCart,
                        title = "Items Ready",
                        count = 3,
                        iconColor = Color(0xFF9E9E9E)
                    )
                    NotificationItem(
                        icon = Icons.Default.Star,
                        title = "Matching Results",
                        count = 24,
                        iconColor = Color(0xFFFFA500)
                    )
                    NotificationItem(
                        icon = Icons.Default.Check,
                        title = "Items in Review",
                        count = 2,
                        iconColor = Color(0xFF4CAF50)
                    )
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        item {
            // Statistics section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Statistics",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "View All",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text("Today", style = MaterialTheme.typography.bodyMedium)
                        Text("1W", style = MaterialTheme.typography.bodyMedium)
                        Text("4W", style = MaterialTheme.typography.bodyMedium)
                        Text("1Y", style = MaterialTheme.typography.bodyMedium)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatCard(title = "Found Items", value = "---")
                        StatCard(title = "Inquiries", value = "---")
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun QuickActionIcon(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(onClick = onClick) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun NotificationItem(
    icon: ImageVector,
    title: String,
    count: Int,
    iconColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = count.toString(),
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = "View",
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun StatCard(title: String, value: String) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .height(80.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun BrowseSelectionDialog(
    onDismiss: () -> Unit,
    onBrowseLost: () -> Unit,
    onBrowseFound: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Browse Items") },
        text = { Text("What would you like to browse?") },
        confirmButton = {
            Button(onClick = {
                onDismiss()
                onBrowseLost()
            }) {
                Text("Browse Lost Items")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = {
                onDismiss()
                onBrowseFound()
            }) {
                Text("Browse Found Items")
            }
        }
    )
}
