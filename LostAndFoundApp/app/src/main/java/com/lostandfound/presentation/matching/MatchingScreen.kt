package com.lostandfound.presentation.matching

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.lostandfound.data.models.Item
import com.lostandfound.presentation.components.CampusFindLogo

// Modern color palette
private val PrimaryPurple = Color(0xFF8B5CF6)
private val SecondaryPink = Color(0xFFEC4899)
private val LightPurple = Color(0xFFF3E8FF)
private val BackgroundWhite = Color(0xFFFAFAFA)
private val CardWhite = Color(0xFFFFFFFF)
private val TextDark = Color(0xFF1F2937)
private val TextGray = Color(0xFF6B7280)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchingScreen(
    onBack: () -> Unit,
    onFindMatches: (itemId: String) -> Unit,
    viewModel: MatchingScreenViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        // Modern Header Section
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CardWhite,
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Back button and title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = PrimaryPurple
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    CampusFindLogo()
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Suggestions",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextDark,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Search Bar
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { 
                        Text(
                            "Search suggestions",
                            color = TextGray,
                            fontSize = 14.sp
                        ) 
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = PrimaryPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = LightPurple,
                        focusedContainerColor = LightPurple,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = PrimaryPurple,
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark
                    ),
                    singleLine = true
                )
            }
        }
        
        // Content Area
        when {
            state.isLoading -> {
                // Loading State
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = PrimaryPurple
                    )
                }
            }
            
            state.error != null -> {
                // Error State
                ErrorState(
                    message = state.error ?: "An error occurred",
                    onRetry = { viewModel.loadMatchedPairs() }
                )
            }
            
            state.matchedPairs.isEmpty() -> {
                // Empty State
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No suggestions yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            }
            
            else -> {
                // Items List - Show matched pairs
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.matchedPairs.size) { index ->
                        val pair = state.matchedPairs[index]
                        MatchedPairCard(
                            pair = pair,
                            onFindMatches = { onFindMatches(pair.lostItem.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemCardWithMatchButton(
    item: Item,
    onFindMatches: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Item Image
                if (item.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.itemName,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(android.R.drawable.ic_menu_report_image),
                        contentDescription = null,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Item Details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.itemName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 2
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Find Matches Button
            Button(
                onClick = onFindMatches,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Find Matches",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
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
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}


@Composable
private fun MatchedPairCard(
    pair: ItemPair,
    onFindMatches: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Match score badge at top
            Surface(
                color = when {
                    pair.matchScore >= 90 -> Color(0xFF10B981) // Green
                    pair.matchScore >= 70 -> Color(0xFFFBBF24) // Yellow
                    else -> Color(0xFFEF4444) // Red
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "✓ ${pair.matchScore}% Match",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // VS mode: Lost vs Found (side-by-side)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                VsItemColumn(
                    modifier = Modifier.weight(1f),
                    label = "LOST",
                    labelColor = Color(0xFFEF4444),
                    imageUrl = pair.lostItem.imageUrl,
                    fallbackBg = LightPurple,
                    fallbackTint = PrimaryPurple,
                    title = pair.lostItem.itemName,
                    subtitle = pair.lostItem.location
                )

                Surface(
                    color = LightPurple,
                    shape = CircleShape
                ) {
                    Text(
                        text = "VS",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        color = PrimaryPurple,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                VsItemColumn(
                    modifier = Modifier.weight(1f),
                    label = "FOUND",
                    labelColor = Color(0xFF10B981),
                    imageUrl = pair.foundItem.imageUrl,
                    fallbackBg = Color(0xFFDCFCE7),
                    fallbackTint = Color(0xFF10B981),
                    title = pair.foundItem.itemName,
                    subtitle = pair.foundItem.location
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Match Reasons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Match
                if (pair.lostItem.category == pair.foundItem.category && pair.lostItem.category.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(android.R.drawable.checkbox_on_background),
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Category Match",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDark,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                }
                
                // Color Match
                if (pair.lostItem.color == pair.foundItem.color && pair.lostItem.color.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(android.R.drawable.checkbox_on_background),
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Color Match",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDark,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                }
                
                // Model/Brand Match
                if (pair.lostItem.brand == pair.foundItem.brand && pair.lostItem.brand.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(android.R.drawable.checkbox_on_background),
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Model Match",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDark,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Claim Button
            Button(
                onClick = onFindMatches,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
            ) {
                Text(
                    text = "Claim",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun VsItemColumn(
    modifier: Modifier,
    label: String,
    labelColor: Color,
    imageUrl: String,
    fallbackBg: Color,
    fallbackTint: Color,
    title: String,
    subtitle: String
) {
    Column(
        modifier = modifier
    ) {
        Surface(
            color = labelColor,
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (imageUrl.isNotBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(fallbackBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_menu_report_image),
                    contentDescription = null,
                    tint = fallbackTint,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            maxLines = 2
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = TextGray,
            maxLines = 2
        )
    }
}
