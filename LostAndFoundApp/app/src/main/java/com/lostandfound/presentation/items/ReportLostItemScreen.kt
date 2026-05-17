package com.lostandfound.presentation.items

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.lostandfound.data.models.*
import com.lostandfound.data.repositories.SimpleItemsRepository
import com.lostandfound.data.repositories.AuthRepository
import com.lostandfound.presentation.components.CampusFindScreenHeader
import com.lostandfound.presentation.components.ReportYesNoAnswerRow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportLostItemScreen(
    onBack: () -> Unit,
    onSubmit: () -> Unit
) {
    // Public details state
    var itemName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var generalDescription by remember { mutableStateOf("") }
    var locationLost by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    // Verification questions and answers state
    var question1 by remember { mutableStateOf("") }
    var answer1 by remember { mutableStateOf<Boolean?>(null) }
    var question2 by remember { mutableStateOf("") }
    var answer2 by remember { mutableStateOf<Boolean?>(null) }
    var question3 by remember { mutableStateOf("") }
    var answer3 by remember { mutableStateOf<Boolean?>(null) }
    var question4 by remember { mutableStateOf("") }
    var answer4 by remember { mutableStateOf<Boolean?>(null) }
    
    // UI state
    var isLoading by remember { mutableStateOf(false) }
    var isUploadingImage by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val PrimaryPurple = Color(0xFF8B5CF6)
    val SecondaryPink = Color(0xFFEC4899)
    val LightPurple = Color(0xFFF3E8FF)
    val BackgroundWhite = Color(0xFFFAFAFA)
    
    // Categories for lost items
    val categories = listOf(
        "Electronics", "Clothing", "Accessories", "Books", "Keys", 
        "Bags", "Documents", "Jewelry", "Sports Equipment", "Other"
    )
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        CampusFindScreenHeader(
            title = "Report Lost Item",
            onBack = onBack,
            trailing = {
                IconButton(onClick = {}) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = PrimaryPurple
                    )
                }
            }
        )
        
        Text(
            text = "Item details",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            item {
                // Error message
                errorMessage?.let { error ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                    ) {
                        Text(
                            text = error,
                            color = Color.Red,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                
                // PUBLIC DETAILS SECTION
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Item Details",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryPurple
                            )
                        }
                        
                        Text(
                            text = "Provide details about your lost item",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        // Item Name
                        OutlinedTextField(
                            value = itemName,
                            onValueChange = { itemName = it },
                            placeholder = { Text("Item name (e.g., iPhone 13 Pro)") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF6B4FA0)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = LightPurple,
                                focusedContainerColor = LightPurple,
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color(0xFF6B4FA0)
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Category Dropdown
                        Box {
                            OutlinedTextField(
                                value = category,
                                onValueChange = {},
                                readOnly = true,
                                placeholder = { Text("Item Category *") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.List,
                                        contentDescription = null,
                                        tint = Color(0xFF6B4FA0)
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { showCategoryDropdown = !showCategoryDropdown }) {
                                        Icon(
                                            if (showCategoryDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Dropdown"
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showCategoryDropdown = !showCategoryDropdown },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = LightPurple,
                                    focusedContainerColor = LightPurple,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = Color(0xFF6B4FA0)
                                )
                            )
                            
                            DropdownMenu(
                                expanded = showCategoryDropdown,
                                onDismissRequest = { showCategoryDropdown = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat) },
                                        onClick = {
                                            category = cat
                                            showCategoryDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // General Description
                        OutlinedTextField(
                            value = generalDescription,
                            onValueChange = { generalDescription = it },
                            placeholder = { Text("General description (e.g., Black smartphone with cracked screen)") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF6B4FA0)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = LightPurple,
                                focusedContainerColor = LightPurple,
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color(0xFF6B4FA0)
                            ),
                            minLines = 2
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Location Lost
                        OutlinedTextField(
                            value = locationLost,
                            onValueChange = { locationLost = it },
                            placeholder = { Text("Location where lost (e.g., Library 2nd floor)") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color(0xFF6B4FA0)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = LightPurple,
                                focusedContainerColor = LightPurple,
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color(0xFF6B4FA0)
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Contact Email
                        OutlinedTextField(
                            value = contactEmail,
                            onValueChange = { contactEmail = it },
                            placeholder = { Text("Contact email *") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Email,
                                    contentDescription = null,
                                    tint = Color(0xFF6B4FA0)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = LightPurple,
                                focusedContainerColor = LightPurple,
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color(0xFF6B4FA0)
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Contact Phone
                        OutlinedTextField(
                            value = contactPhone,
                            onValueChange = { contactPhone = it },
                            placeholder = { Text("Contact phone *") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = Color(0xFF6B4FA0)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = LightPurple,
                                focusedContainerColor = LightPurple,
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color(0xFF6B4FA0)
                            )
                        )
                    }
                }
                
                // VERIFICATION QUESTIONS SECTION
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(20.dp)
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
                            text = "Create 4 yes/no questions about your lost item (e.g., \"Is the case black?\"). Each answer must be Yes or No. Finders must answer all correctly to get your contact info.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF92400E),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        // Question 1 with Answer
                        OutlinedTextField(
                            value = question1,
                            onValueChange = { question1 = it },
                            placeholder = { Text("Question 1: e.g., Is the case black?") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF6B4FA0)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White.copy(alpha = 0.7f),
                                focusedContainerColor = Color.White.copy(alpha = 0.7f),
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color(0xFF6B4FA0)
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ReportYesNoAnswerRow(1, answer1) { answer1 = it }
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = question2,
                            onValueChange = { question2 = it },
                            placeholder = { Text("Question 2: e.g., Does it have a logo?") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF6B4FA0)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White.copy(alpha = 0.7f),
                                focusedContainerColor = Color.White.copy(alpha = 0.7f),
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color(0xFF6B4FA0)
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ReportYesNoAnswerRow(2, answer2) { answer2 = it }
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = question3,
                            onValueChange = { question3 = it },
                            placeholder = { Text("Question 3: e.g., Was it lost in the library?") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF6B4FA0)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White.copy(alpha = 0.7f),
                                focusedContainerColor = Color.White.copy(alpha = 0.7f),
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color(0xFF6B4FA0)
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ReportYesNoAnswerRow(3, answer3) { answer3 = it }
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = question4,
                            onValueChange = { question4 = it },
                            placeholder = { Text("Question 4: e.g., Are there keys inside?") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF6B4FA0)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White.copy(alpha = 0.7f),
                                focusedContainerColor = Color.White.copy(alpha = 0.7f),
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color(0xFF6B4FA0)
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ReportYesNoAnswerRow(4, answer4) { answer4 = it }
                        
                        val filledQuestions = listOf(question1, question2, question3, question4).count { it.isNotBlank() }
                        val filledAnswers = listOf(answer1, answer2, answer3, answer4).count { YesNoAnswer.isValid(it) }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Icon(
                                if (filledQuestions >= 4 && filledAnswers >= 4) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (filledQuestions >= 4 && filledAnswers >= 4) Color(0xFF059669) else Color(0xFFD97706),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$filledQuestions of 4 questions and $filledAnswers of 4 answers provided",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (filledQuestions >= 4 && filledAnswers >= 4) Color(0xFF059669) else Color(0xFFD97706),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                // IMAGE UPLOAD SECTION
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (selectedImageUri != null) 200.dp else 100.dp)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (selectedImageUri != null) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Selected image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { selectedImageUri = null },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(50))
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove image",
                                    tint = Color.Red
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add photo",
                                modifier = Modifier.size(40.dp),
                                tint = PrimaryPurple
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Upload Item Photo (Optional)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tap to select from gallery",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
                
                if (isUploadingImage) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = PrimaryPurple
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Uploading image...",
                            style = MaterialTheme.typography.bodySmall,
                            color = PrimaryPurple
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // POST BUTTON
                Button(
                    onClick = {
                        // Validation
                        when {
                            itemName.isBlank() -> {
                                errorMessage = "Please enter item name"
                                return@Button
                            }
                            category.isBlank() -> {
                                errorMessage = "Please select item category"
                                return@Button
                            }
                            locationLost.isBlank() -> {
                                errorMessage = "Please enter location where lost"
                                return@Button
                            }
                            contactEmail.isBlank() -> {
                                errorMessage = "Please enter contact email"
                                return@Button
                            }
                            contactPhone.isBlank() -> {
                                errorMessage = "Please enter contact phone"
                                return@Button
                            }
                            question1.isBlank() || question2.isBlank() || question3.isBlank() || question4.isBlank() -> {
                                errorMessage = "Please fill all 4 verification questions"
                                return@Button
                            }
                            !listOf(answer1, answer2, answer3, answer4).all { YesNoAnswer.isValid(it) } -> {
                                errorMessage = "Please select Yes or No for all 4 verification answers"
                                return@Button
                            }
                        }
                        
                        isLoading = true
                        errorMessage = null
                        
                        scope.launch {
                            try {
                                // Show uploading indicator if image is selected
                                if (selectedImageUri != null) {
                                    isUploadingImage = true
                                }
                                
                                // Create SimpleLostItem
                                val lostItem = SimpleLostItem(
                                    reporterId = AuthRepository.currentUser?.uid ?: "",
                                    itemName = itemName,
                                    category = category,
                                    generalDescription = generalDescription,
                                    locationLost = locationLost,
                                    contactEmail = contactEmail,
                                    contactPhone = contactPhone,
                                    question1 = question1,
                                    answer1 = answer1!!,
                                    question2 = question2,
                                    answer2 = answer2!!,
                                    question3 = question3,
                                    answer3 = answer3!!,
                                    question4 = question4,
                                    answer4 = answer4!!
                                )
                                
                                SimpleItemsRepository.addLostItem(lostItem, selectedImageUri, context)
                                
                                isUploadingImage = false
                                onSubmit()
                            } catch (e: Exception) {
                                errorMessage = "Failed to post item: ${e.message}"
                                isLoading = false
                                isUploadingImage = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryPurple
                    ),
                    shape = RoundedCornerShape(28.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text("Post Lost Item Report", style = MaterialTheme.typography.titleMedium)
                    }
                }
                
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}