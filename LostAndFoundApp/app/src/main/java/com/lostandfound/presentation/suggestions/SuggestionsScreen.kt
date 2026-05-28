package com.lostandfound.presentation.suggestions

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.lostandfound.presentation.components.CampusFindLogo

private val PrimaryPurple = Color(0xFF8B5CF6)
private val LightPurple = Color(0xFFF3E8FF)
private val BackgroundWhite = Color(0xFFFAFAFA)
private val CardWhite = Color(0xFFFFFFFF)
private val TextDark = Color(0xFF1F2937)
private val TextGray = Color(0xFF6B7280)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestionsScreen(
    onBack: () -> Unit,
    onClaimFoundItem: (foundItemId: String) -> Unit,
    viewModel: SuggestionsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CardWhite,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
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

                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = viewModel::updateSearchQuery,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("Search suggestions", color = TextGray, fontSize = 14.sp)
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

        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryPurple)
                }
            }

            state.error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.error ?: "Error", color = Color.Red)
                }
            }

            state.pairs.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No suggestions yet", color = TextGray)
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.pairs) { pair ->
                        SuggestionCard(
                            pair = pair,
                            onClaim = { onClaimFoundItem(pair.foundItem.id) }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun SuggestionCard(
    pair: SimpleSuggestionPair,
    onClaim: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(
                color = when {
                    pair.matchScore >= 90 -> Color(0xFF10B981)
                    pair.matchScore >= 70 -> Color(0xFFFBBF24)
                    else -> Color(0xFFEF4444)
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
                    title = pair.lostItem.itemName,
                    subtitle = pair.lostItem.locationLost
                )

                Surface(color = LightPurple, shape = CircleShape) {
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
                    title = pair.foundItem.itemName,
                    subtitle = pair.foundItem.locationFound
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onClaim,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
            ) {
                Text("Claim", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun VsItemColumn(
    modifier: Modifier,
    label: String,
    labelColor: Color,
    imageUrl: String?,
    title: String,
    subtitle: String
) {
    Column(modifier = modifier) {
        Surface(color = labelColor, shape = RoundedCornerShape(6.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(Color(0xFFF0F0F2), RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(Color(0xFFF0F0F2), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color(0xFF8D8D93)
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
            text = subtitle.ifBlank { "—" },
            style = MaterialTheme.typography.bodySmall,
            color = TextGray,
            maxLines = 2
        )
    }
}

