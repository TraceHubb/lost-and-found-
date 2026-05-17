package com.lostandfound.presentation.claim

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.lostandfound.data.models.SimpleLostItem
import com.lostandfound.data.models.SimpleClaim
import com.lostandfound.data.repositories.SimpleItemsRepository
import com.lostandfound.data.repositories.AuthRepository
import com.lostandfound.data.models.YesNoAnswer
import com.lostandfound.presentation.components.CampusFindScreenHeader
import com.lostandfound.presentation.components.YesNoQuestionCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleLostClaimScreen(
    itemId: String,
    onBack: () -> Unit,
    onClaimSuccess: (String, String) -> Unit // (contactEmail, contactPhone)
) {
    var item by remember { mutableStateOf<SimpleLostItem?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    
    var answer1 by remember { mutableStateOf<Boolean?>(null) }
    var answer2 by remember { mutableStateOf<Boolean?>(null) }
    var answer3 by remember { mutableStateOf<Boolean?>(null) }
    var answer4 by remember { mutableStateOf<Boolean?>(null) }
    
    val scope = rememberCoroutineScope()
    val PrimaryPurple = Color(0xFF8B5CF6)
    val BackgroundWhite = Color(0xFFFAFAFA)
    
    // Load item details
    LaunchedEffect(itemId) {
        try {
            item = SimpleItemsRepository.getLostItemById(itemId)
            isLoading = false
        } catch (e: Exception) {
            errorMessage = "Failed to load item details"
            isLoading = false
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = PrimaryPurple
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "I Found This Item",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF1F2937),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryPurple)
            }
        } else if (item == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = Color.Red,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = errorMessage ?: "Item not found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Red
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    // Item Details Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row {
                                // Item Image
                                if (item!!.imageUrl != null) {
                                    AsyncImage(
                                        model = item!!.imageUrl,
                                        contentDescription = "Item image",
                                        modifier = Modifier
                                            .size(100.dp)
                                            .background(Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .background(Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Search,
                                            contentDescription = "No image",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(40.dp)
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                // Item Info
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item!!.itemName,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item!!.category,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFFEF4444),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = item!!.generalDescription,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = "Location",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Lost at: ${item!!.locationLost}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                    
                    // Verification Questions Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Verification Questions",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD97706)
                                )
                            }
                            
                            Text(
                                text = "Answer all 4 questions correctly to get the owner's contact information.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF92400E),
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            YesNoQuestionCard(
                                questionNumber = 1,
                                question = item!!.question1,
                                answer = answer1,
                                onAnswerSelected = { answer1 = it }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            YesNoQuestionCard(
                                questionNumber = 2,
                                question = item!!.question2,
                                answer = answer2,
                                onAnswerSelected = { answer2 = it }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            YesNoQuestionCard(
                                questionNumber = 3,
                                question = item!!.question3,
                                answer = answer3,
                                onAnswerSelected = { answer3 = it }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            YesNoQuestionCard(
                                questionNumber = 4,
                                question = item!!.question4,
                                answer = answer4,
                                onAnswerSelected = { answer4 = it }
                            )
                        }
                    }
                    
                    // Error message
                    errorMessage?.let { error ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                        ) {
                            Text(
                                text = error,
                                color = Color.Red,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    
                    // Submit Button
                    Button(
                        onClick = {
                            // Validate all questions are answered
                            val answers = listOf(answer1, answer2, answer3, answer4)
                            if (answers.any { !YesNoAnswer.isValid(it) }) {
                                errorMessage = "Please select Yes or No for all 4 questions"
                                return@Button
                            }
                            
                            isSubmitting = true
                            errorMessage = null
                            
                            scope.launch {
                                try {
                                    val claim = SimpleClaim(
                                        itemId = itemId,
                                        claimerId = AuthRepository.currentUser?.uid ?: "",
                                        claimer_email = AuthRepository.currentUser?.email ?: "",
                                        answer1 = answer1!!,
                                        answer2 = answer2!!,
                                        answer3 = answer3!!,
                                        answer4 = answer4!!
                                    )
                                    
                                    SimpleItemsRepository.submitLostClaim(claim)
                                    
                                    // Get correct answers to check
                                    val correctAnswers = listOf(item!!.answer1, item!!.answer2, item!!.answer3, item!!.answer4)
                                    
                                    // Check if all answers are correct
                                    if (claim.isAllCorrect(correctAnswers)) {
                                        onClaimSuccess(item!!.contactEmail, item!!.contactPhone)
                                    } else {
                                        errorMessage = "Incorrect answers. You got ${claim.getCorrectCount(correctAnswers)} out of 4 questions right. Please try again."
                                        isSubmitting = false
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Failed to submit claim: ${e.message}"
                                    isSubmitting = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981)
                        ),
                        shape = RoundedCornerShape(28.dp),
                        enabled = !isSubmitting && listOf(answer1, answer2, answer3, answer4).all { YesNoAnswer.isValid(it) }
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                "I Found This Item",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
