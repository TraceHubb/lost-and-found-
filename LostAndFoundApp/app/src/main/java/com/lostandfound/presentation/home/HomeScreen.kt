package com.lostandfound.presentation.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lostandfound.presentation.components.CampusFindBrandRow
import com.lostandfound.presentation.components.CampusFindProfileIcon

// Modern color palette
private val PrimaryPurple = Color(0xFF8B5CF6)
private val SecondaryPink = Color(0xFFEC4899)
private val LightPurple = Color(0xFFF3E8FF)
private val BackgroundWhite = Color(0xFFFAFAFA)
private val CardWhite = Color(0xFFFFFFFF)
private val GreenSuccess = Color(0xFF10B981)
private val TextDark = Color(0xFF1F2937)
private val TextGray = Color(0xFF6B7280)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onReportLost: () -> Unit,
    onReportFound: () -> Unit,
    onBrowseLost: () -> Unit,
    onBrowseFound: () -> Unit,
    onNavigateToMatching: () -> Unit,
    onNavigateToItemsReady: () -> Unit,
    onNavigateToItemsInReview: () -> Unit,
    onLogout: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    var showBrowseDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    
    if (showBrowseDialog) {
        BrowseSelectionDialog(
            onDismiss = { showBrowseDialog = false },
            onBrowseLost = onBrowseLost,
            onBrowseFound = onBrowseFound
        )
    }
    
    if (showReportDialog) {
        ReportSelectionDialog(
            onDismiss = { showReportDialog = false },
            onReportLost = onReportLost,
            onReportFound = onReportFound
        )
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        item {
            // Modern Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CardWhite
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CampusFindBrandRow()
                        CampusFindProfileIcon()
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        item {
            // Items Reunited Card with Graph
            ItemsReunitedCard(
                totalItems = state.notificationCounts.itemsReady + 
                            state.notificationCounts.matchingResults + 
                            state.notificationCounts.itemsInReview
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
        
        item {
            // Quick action buttons
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ModernQuickAction(
                        icon = Icons.Default.Add,
                        label = "Report",
                        gradient = listOf(Color(0xFFEF4444), Color(0xFFF97316)),
                        onClick = { showReportDialog = true }  // Show report selection dialog
                    )
                    ModernQuickAction(
                        icon = Icons.Default.Star,
                        label = "Matching",
                        gradient = listOf(Color(0xFFF59E0B), Color(0xFFFBBF24)),
                        onClick = onNavigateToMatching
                    )
                    ModernQuickAction(
                        icon = Icons.Default.Search,
                        label = "Browse",
                        gradient = listOf(PrimaryPurple, SecondaryPink),
                        onClick = { showBrowseDialog = true }
                    )
                    ModernQuickAction(
                        icon = Icons.Default.Check,
                        label = "Tasks",
                        gradient = listOf(Color(0xFF10B981), Color(0xFF34D399)),
                        onClick = {}
                    )
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        item {
            // Overview Section Title
            Text(
                text = "Overview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        item {
            // Statistics Cards Grid
            StatisticsGrid(
                lostItems = state.notificationCounts.itemsReady,
                foundItems = state.notificationCounts.matchingResults,
                returnedItems = state.notificationCounts.itemsInReview,
                activeUsers = 32841,
                onLostItemsClick = onNavigateToItemsReady,
                onFoundItemsClick = onNavigateToMatching,
                onReturnedItemsClick = onNavigateToItemsInReview,
                viewModel = viewModel
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        item {
            // Success Rate Card
            SuccessRateCard()
        }
        
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun ItemsReunitedCard(totalItems: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(8.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Items Reunited",
                style = MaterialTheme.typography.titleMedium,
                color = TextGray,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format("%,d", totalItems * 1000),
                    style = MaterialTheme.typography.headlineLarge,
                    fontSize = 36.sp,
                    color = TextDark,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Items",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextGray
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.KeyboardArrowUp,
                    contentDescription = null,
                    tint = GreenSuccess,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "12.5%",
                    color = GreenSuccess,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "vs last month",
                    color = TextGray,
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Simple Graph Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                LightPurple.copy(alpha = 0.3f),
                                LightPurple.copy(alpha = 0.1f)
                            )
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("Jan", "Feb", "Mar", "Apr", "May").forEach { month ->
                        Text(
                            text = month,
                            color = TextGray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernQuickAction(
    icon: ImageVector,
    label: String,
    gradient: List<Color>,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .shadow(4.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.linearGradient(gradient)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextDark,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun StatisticsGrid(
    lostItems: Int,
    foundItems: Int,
    returnedItems: Int,
    activeUsers: Int,
    onLostItemsClick: () -> Unit,
    onFoundItemsClick: () -> Unit,
    onReturnedItemsClick: () -> Unit,
    viewModel: HomeViewModel
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Search,
                title = "Lost Items",
                count = lostItems,
                percentage = "+3.2%",
                iconColor = PrimaryPurple,
                backgroundColor = LightPurple,
                onClick = {
                    viewModel.dismissNotification("itemsReady")
                    onLostItemsClick()
                },
                isHighlighted = false
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.CheckCircle,
                title = "Found Items",
                count = foundItems,
                percentage = "+7.1%",
                iconColor = Color.White,
                backgroundColor = PrimaryPurple,
                onClick = {
                    viewModel.dismissNotification("matchingResults")
                    onFoundItemsClick()
                },
                isHighlighted = true
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Refresh,
                title = "Returned Items",
                count = returnedItems,
                percentage = "+5.8%",
                iconColor = GreenSuccess,
                backgroundColor = GreenSuccess.copy(alpha = 0.1f),
                onClick = {
                    viewModel.dismissNotification("itemsInReview")
                    onReturnedItemsClick()
                },
                isHighlighted = false
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Person,
                title = "Active Users",
                count = activeUsers,
                percentage = "+2.4%",
                iconColor = SecondaryPink,
                backgroundColor = SecondaryPink.copy(alpha = 0.1f),
                onClick = {},
                isHighlighted = false
            )
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    count: Int,
    percentage: String,
    iconColor: Color,
    backgroundColor: Color,
    onClick: () -> Unit,
    isHighlighted: Boolean
) {
    Card(
        modifier = modifier
            .height(140.dp)
            .shadow(if (isHighlighted) 12.dp else 4.dp, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) PrimaryPurple else CardWhite
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(backgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        contentDescription = null,
                        tint = if (isHighlighted) Color.White else GreenSuccess,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = percentage,
                        color = if (isHighlighted) Color.White else GreenSuccess,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            Column {
                Text(
                    text = String.format("%,d", count),
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (isHighlighted) Color.White else TextDark,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isHighlighted) Color.White.copy(alpha = 0.8f) else TextGray
                )
            }
        }
    }
}

@Composable
fun SuccessRateCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(8.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Success Rate",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextGray,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "82.6%",
                    style = MaterialTheme.typography.headlineLarge,
                    fontSize = 40.sp,
                    color = PrimaryPurple,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "More than 82% of items are successfully returned",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray,
                    lineHeight = 20.sp
                )
            }
            
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(PrimaryPurple, SecondaryPink)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
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
            Button(
                onClick = {
                    onDismiss()
                    onBrowseLost()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryPurple
                )
            ) {
                Text("Browse Lost Items")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = {
                onDismiss()
                onBrowseFound()
            }) {
                Text("Browse Found Items", color = PrimaryPurple)
            }
        }
    )
}

@Composable
private fun ReportSelectionDialog(
    onDismiss: () -> Unit,
    onReportLost: () -> Unit,
    onReportFound: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Report Item") },
        text = { Text("What would you like to report?") },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    onReportLost()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF4444)
                )
            ) {
                Text("Report Lost Item")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = {
                onDismiss()
                onReportFound()
            }) {
                Text("Report Found Item", color = Color(0xFF10B981))
            }
        }
    )
}
