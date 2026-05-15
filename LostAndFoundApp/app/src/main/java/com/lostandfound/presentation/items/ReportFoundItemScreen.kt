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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.lostandfound.data.models.Item
import com.lostandfound.data.models.ItemType
import com.lostandfound.data.repositories.ItemsRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportFoundItemScreen(
    onBack: () -> Unit,
    onSubmit: () -> Unit
) {
    var itemName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var additionalDetails by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
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
                    Column {
                        Text(
                            text = "Campus Lost & Found",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF1F2937),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                IconButton(onClick = {}) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = PrimaryPurple
                    )
                }
            }
        }
        
        // Title
        Text(
            text = "Report Found Item",
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
                
                // Item Name
                FormField(
                    icon = Icons.Default.Phone,
                    placeholder = "Item name (e.g., iPhone 13)",
                    value = itemName,
                    onValueChange = { itemName = it },
                    backgroundColor = LightPurple
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
                        com.lostandfound.data.models.ItemCategories.categories.forEach { cat ->
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
                
                // Color
                FormField(
                    icon = Icons.Default.Star,
                    placeholder = "Color (e.g., Black, Blue)",
                    value = color,
                    onValueChange = { color = it },
                    backgroundColor = LightPurple
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                // Brand
                FormField(
                    icon = Icons.Default.Info,
                    placeholder = "Brand (e.g., Apple, Samsung)",
                    value = brand,
                    onValueChange = { brand = it },
                    backgroundColor = LightPurple
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                // Additional Details
                FormField(
                    icon = Icons.Default.List,
                    placeholder = "Additional Details (e.g., scratches, stickers, unique features)",
                    value = additionalDetails,
                    onValueChange = { additionalDetails = it },
                    backgroundColor = LightPurple,
                    minLines = 3
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                // Location
                FormField(
                    icon = Icons.Default.LocationOn,
                    placeholder = "Specific Location where found (e.g., Library, Cafeteria)",
                    value = location,
                    onValueChange = { location = it },
                    backgroundColor = LightPurple
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                // Contact Email
                FormField(
                    icon = Icons.Default.Email,
                    placeholder = "Contact email *",
                    value = contactEmail,
                    onValueChange = { contactEmail = it },
                    backgroundColor = LightPurple
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                // Contact Phone
                FormField(
                    icon = Icons.Default.Phone,
                    placeholder = "Contact phone *",
                    value = contactPhone,
                    onValueChange = { contactPhone = it },
                    backgroundColor = LightPurple
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Image Upload Section
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
                
                // Post button
                Button(
                    onClick = {
                        if (itemName.isBlank()) {
                            errorMessage = "Please enter item name"
                            return@Button
                        }
                        if (category.isBlank()) {
                            errorMessage = "Please select item category"
                            return@Button
                        }
                        if (location.isBlank()) {
                            errorMessage = "Please enter location"
                            return@Button
                        }
                        if (contactEmail.isBlank() && contactPhone.isBlank()) {
                            errorMessage = "Please provide at least one contact method (email or phone)"
                            return@Button
                        }
                        if (contactEmail.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(contactEmail).matches()) {
                            errorMessage = "Please enter a valid email address"
                            return@Button
                        }
                        
                        isLoading = true
                        errorMessage = null
                        
                        scope.launch {
                            try {
                                // Show uploading indicator if image is selected
                                if (selectedImageUri != null) {
                                    isUploadingImage = true
                                }
                                
                                // Build description from structured fields
                                val description = buildString {
                                    if (category.isNotBlank()) append("Category: $category\n")
                                    if (color.isNotBlank()) append("Color: $color\n")
                                    if (brand.isNotBlank()) append("Brand: $brand\n")
                                    if (additionalDetails.isNotBlank()) append("Details: $additionalDetails")
                                }
                                
                                val item = Item(
                                    itemName = itemName,
                                    description = description,
                                    location = location,
                                    contactEmail = contactEmail,
                                    contactPhone = contactPhone,
                                    category = category,
                                    color = color,
                                    brand = brand,
                                    additionalDetails = additionalDetails,
                                    type = ItemType.FOUND,
                                    date = System.currentTimeMillis()
                                )
                                ItemsRepository.addItem(item, selectedImageUri, context)
                                
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
                        Text("Post Found Item Report", style = MaterialTheme.typography.titleMedium)
                    }
                }
                
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
