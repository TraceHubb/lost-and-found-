package com.lostandfound.presentation.matches

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.lostandfound.data.repositories.ItemMatch
import com.lostandfound.presentation.claim.ClaimViewModel
import com.lostandfound.presentation.components.CampusFindLogo

// Modern color palette
private val PrimaryPurple = Color(0xFF8B5CF6)
private val SecondaryPink = Color(0xFFEC4899)
private val LightPurple = Color(0xFFF3E8FF)
private val BackgroundWhite = Color(0xFFFAFAFA)
private val CardWhite = Color(0xFFFFFFFF)
private val GreenSuccess = Color(0xFF10B981)
private val TextDark = Color(0xFF1F2937)
private val TextGray = Color(0xFF6B7280)

@Composable
fun MatchResultsScreen(
    itemId: String,
    onBack: () -> Unit,
    onViewDetails: (itemId: String) -> Unit,
    navController: NavController,
    viewModel: MatchResultsViewModel = viewModel(),
    claimViewModel: ClaimViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedFilter by remember { mutableStateOf("All Matches") }
    
    // Load matches when screen is first displayed
    LaunchedEffect(itemId) {
        viewModel.loadMatches(itemId)
    }
    
    // Filter matches based on selected filter
    val filteredMatches = when (selectedFilter) {
        "High (90%+)" -> state.matches.filter { it.matchScore >= 90 }
        "Medium (60-90%)" -> state.matches.filter { it.matchScore in 60..89 }
        "Low (<60%)" -> state.matches.filter { it.matchScore < 60 }
        else -> state.matches
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        // Modern Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CardWhite,
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextDark
                        )
                    }
                    
                    CampusFindLogo()
                    
                    IconButton(onClick = {}) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = TextDark
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Title
                Text(
                    text = "Matching Results",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextDark,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "We found possible matches for your item",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray
                )
            }
        }
        
        // Filter Tabs
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 20.dp)
        ) {
            items(listOf("All Matches", "High (90%+)", "Medium (60-90%)", "Low (<60%)")) { filter ->
                FilterChip(
                    filter = filter,
                    isSelected = selectedFilter == filter,
                    onClick = { selectedFilter = filter }
                )
            }
        }
        
        // Content area
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryPurple)
                }
            }
            
            state.error != null -> {
                ErrorState(
                    message = state.error!!,
                    onRetry = { viewModel.retry() }
                )
            }
            
            filteredMatches.isEmpty() -> {
                EmptyMatchesState()
            }
            
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredMatches) { match ->
                        ModernMatchCard(
                            match = match,
                            sourceItem = state.sourceItem,
                            onViewDetails = { onViewDetails(match.item.id) },
                            onClaimItem = {
                                // Navigate to OTP input screen
                                // matchId will be constructed from sourceItem.id and match.item.id
                                val matchId = "${state.sourceItem?.id}_${match.item.id}"
                                val lostItemId = state.sourceItem?.id ?: ""
                                val foundItemId = match.item.id
                                
                                // Initiate claim process
                                claimViewModel.initiateClaim(matchId, lostItemId, foundItemId)
                                
                                // Navigate to OTP input screen
                                navController.navigate("otp_input/$matchId/$lostItemId/$foundItemId")
                            },
                            onNotMine = { /* TODO */ }
                        )
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChip(
    filter: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) PrimaryPurple else Color.White,
        modifier = Modifier.height(40.dp),
        shadowElevation = if (isSelected) 4.dp else 1.dp
    ) {
        Text(
            text = filter,
            color = if (isSelected) Color.White else TextDark,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun ModernMatchCard(
    match: ItemMatch,
    sourceItem: com.lostandfound.data.models.Item?,
    onViewDetails: () -> Unit,
    onClaimItem: () -> Unit,
    onNotMine: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Best Match Badge (if score >= 90)
            if (match.matchScore >= 90) {
                Surface(
                    color = PrimaryPurple,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "Best Match",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
            
            // Side-by-side comparison
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Your Lost Item
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Your Lost Item",
                        fontSize = 12.sp,
                        color = TextGray,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = sourceItem?.itemName ?: "bag",
                        fontSize = 16.sp,
                        color = TextDark,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Item Image
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF3F4F6))
                    ) {
                        if (!sourceItem?.imageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = sourceItem?.imageUrl,
                                contentDescription = sourceItem?.itemName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(48.dp)
                                    .align(Alignment.Center),
                                tint = TextGray
                            )
                        }
                    }
                }
                
                // VS Divider
                Box(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFF3F4F6),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "VS",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                        }
                    }
                }
                
                // Found Item
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Found Item",
                        fontSize = 12.sp,
                        color = TextGray,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = match.item.itemName,
                        fontSize = 16.sp,
                        color = TextDark,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Item Image
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF3F4F6))
                    ) {
                        if (match.item.imageUrl.isNotBlank()) {
                            AsyncImage(
                                model = match.item.imageUrl,
                                contentDescription = match.item.itemName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(48.dp)
                                    .align(Alignment.Center),
                                tint = TextGray
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Details Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Details",
                    fontSize = 20.sp,
                    color = TextDark,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                // Category
                DetailRow(
                    label = "Category:",
                    value = match.item.category.ifBlank { "Bag/Backpack" }
                )
                
                // Color
                DetailRow(
                    label = "Color:",
                    value = match.item.color.ifBlank { "black" }
                )
                
                // Brand
                DetailRow(
                    label = "Brand:",
                    value = match.item.brand.ifBlank { "no brand" }
                )
                
                // Location
                DetailRow(
                    label = "Location:",
                    value = match.item.location
                )
                
                // Additional Details
                DetailRow(
                    label = "Additional Details:",
                    value = match.item.additionalDetails.ifBlank { "big" }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Match indicators - simplified reasons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Show top 3 match reasons
                val matchReasons = mutableListOf<String>()
                
                if (match.matchReasons.any { it.contains("category", ignoreCase = true) }) {
                    matchReasons.add("Category Match")
                }
                if (match.matchReasons.any { it.contains("color", ignoreCase = true) }) {
                    matchReasons.add("Color Match")
                }
                if (match.matchReasons.any { it.contains("brand", ignoreCase = true) || it.contains("model", ignoreCase = true) }) {
                    matchReasons.add("Model Match")
                }
                
                matchReasons.take(3).forEach { reason ->
                    MatchIndicator(reason)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // View Details Button (Purple)
                Button(
                    onClick = onViewDetails,
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryPurple
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "View\nDetails",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
                
                // Claim Item Button (Outlined)
                OutlinedButton(
                    onClick = onClaimItem,
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryPurple
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryPurple),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Claim\nItem",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
                
                // Not Mine Button (Outlined)
                OutlinedButton(
                    onClick = onNotMine,
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextDark
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE5E7EB)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Not\nMine",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Match Score at bottom
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "${match.matchScore}%",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenSuccess
                    )
                    Text(
                        text = "Match Score",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                }
            }
        }
    }
}

@Composable
private fun MatchIndicator(reason: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = GreenSuccess,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = reason,
            fontSize = 13.sp,
            color = TextDark,
            fontWeight = FontWeight.Medium
        )
    }
}
@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color(0xFFEF4444)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = TextDark
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
        ) {
            Text("Retry")
        }
    }
}

@Composable
private fun EmptyMatchesState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = TextGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No matches found",
            style = MaterialTheme.typography.titleLarge,
            color = TextDark,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "We couldn't find any items matching your criteria. Check back later!",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = TextGray
        )
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            color = TextDark,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(150.dp)
        )
        Text(
            text = value,
            fontSize = 15.sp,
            color = TextGray,
            modifier = Modifier.weight(1f)
        )
    }
}
