// package com.lostandfound.presentation.items

// import android.net.Uri
// import androidx.activity.compose.rememberLauncherForActivityResult
// import androidx.activity.result.contract.ActivityResultContracts
// import androidx.compose.foundation.Image
// import androidx.compose.foundation.background
// import androidx.compose.foundation.border
// import androidx.compose.foundation.clickable
// import androidx.compose.foundation.layout.*
// import androidx.compose.foundation.lazy.LazyColumn
// import androidx.compose.foundation.shape.CircleShape
// import androidx.compose.foundation.shape.RoundedCornerShape
// import androidx.compose.material.icons.Icons
// import androidx.compose.material.icons.filled.*
// import androidx.compose.material3.*
// import androidx.compose.runtime.*
// import androidx.compose.ui.Alignment
// import androidx.compose.ui.Modifier
// import androidx.compose.ui.draw.clip
// import androidx.compose.ui.graphics.Brush
// import androidx.compose.ui.graphics.Color
// import androidx.compose.ui.graphics.vector.ImageVector
// import androidx.compose.ui.layout.ContentScale
// import androidx.compose.ui.text.font.FontWeight
// import androidx.compose.ui.text.style.TextAlign
// import androidx.compose.ui.unit.dp
// import androidx.compose.ui.unit.sp
// import coil.compose.AsyncImage
// import com.lostandfound.data.models.Item
// import com.lostandfound.data.models.ItemType
// import com.lostandfound.data.repositories.ItemsRepository
// import kotlinx.coroutines.launch

// // Modern color palette
// private val PrimaryPurple = Color(0xFF8B5CF6)
// private val SecondaryPink = Color(0xFFEC4899)
// private val LightPurple = Color(0xFFF3E8FF)
// private val BackgroundWhite = Color(0xFFFAFAFA)
// private val CardWhite = Color(0xFFFFFFFF)
// private val TextDark = Color(0xFF1F2937)
// private val TextGray = Color(0xFF6B7280)

// @OptIn(ExperimentalMaterial3Api::class)
// @Composable
// fun ReportLostItemScreen(
//     onBack: () -> Unit,
//     onSubmit: () -> Unit
// ) {
//     var currentStep by remember { mutableStateOf(1) }
//     var reportType by remember { mutableStateOf<ItemType?>(null) }
    
//     // Form fields
//     var itemName by remember { mutableStateOf("") }
//     var category by remember { mutableStateOf("") }
//     var color by remember { mutableStateOf("") }
//     var brand by remember { mutableStateOf("") }
//     var additionalDetails by remember { mutableStateOf("") }
//     var location by remember { mutableStateOf("") }
//     var dateTime by remember { mutableStateOf("") }
//     var contactEmail by remember { mutableStateOf("") }
//     var contactPhone by remember { mutableStateOf("") }
//     var additionalNotes by remember { mutableStateOf("") }
//     var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
//     var isLoading by remember { mutableStateOf(false) }
//     var errorMessage by remember { mutableStateOf<String?>(null) }
//     var showCategoryDropdown by remember { mutableStateOf(false) }
    
//     val scope = rememberCoroutineScope()
//     val context = androidx.compose.ui.platform.LocalContext.current
    
//     // Image picker launcher
//     val imagePickerLauncher = rememberLauncherForActivityResult(
//         contract = ActivityResultContracts.GetContent()
//     ) { uri: Uri? ->
//         selectedImageUri = uri
//     }
    
//     Column(
//         modifier = Modifier
//             .fillMaxSize()
//             .background(BackgroundWhite)
//     ) {
//         // Header
//         Surface(
//             modifier = Modifier.fillMaxWidth(),
//             color = CardWhite,
//             shadowElevation = 1.dp
//         ) {
//             Row(
//                 modifier = Modifier
//                     .fillMaxWidth()
//                     .padding(16.dp),
//                 horizontalArrangement = Arrangement.SpaceBetween,
//                 verticalAlignment = Alignment.CenterVertically
//             ) {
//                 IconButton(onClick = {
//                     if (currentStep > 1) {
//                         currentStep--
//                     } else {
//                         onBack()
//                     }
//                 }) {
//                     Icon(
//                         Icons.Default.ArrowBack,
//                         contentDescription = "Back",
//                         tint = TextDark
//                     )
//                 }
                
//                 Column(
//                     horizontalAlignment = Alignment.CenterHorizontally,
//                     modifier = Modifier.weight(1f)
//                 ) {
//                     Text(
//                         text = when (currentStep) {
//                             1 -> "Report Item"
//                             2 -> "Report ${if (reportType == ItemType.LOST) "Lost" else "Found"} Item"
//                             else -> "Report ${if (reportType == ItemType.LOST) "Lost" else "Found"} Item"
//                         },
//                         style = MaterialTheme.typography.titleLarge,
//                         color = TextDark,
//                         fontWeight = FontWeight.Bold
//                     )
//                     Text(
//                         text = when (currentStep) {
//                             1 -> "Help us find your item by providing details"
//                             2 -> "Please provide accurate details"
//                             else -> "Please provide accurate details"
//                         },
//                         style = MaterialTheme.typography.bodySmall,
//                         color = TextGray,
//                         fontSize = 12.sp
//                     )
//                 }
                
//                 // Step indicators
//                 if (currentStep > 1) {
//                     Row(
//                         horizontalArrangement = Arrangement.spacedBy(4.dp),
//                         verticalAlignment = Alignment.CenterVertically
//                     ) {
//                         StepIndicator(1, currentStep >= 1, currentStep == 1)
//                         StepIndicator(2, currentStep >= 2, currentStep == 2)
//                         StepIndicator(3, currentStep >= 3, currentStep == 3)
//                     }
//                 } else {
//                     Spacer(modifier = Modifier.width(48.dp))
//                 }
//             }
//         }
        
//         // Content based on current step
//         when (currentStep) {
//             1 -> Step1SelectType(
//                 onTypeSelected = { type ->
//                     reportType = type
//                     currentStep = 2
//                 }
//             )
//             2 -> Step2ItemDetails(
//                 itemName = itemName,
//                 onItemNameChange = { itemName = it },
//                 category = category,
//                 onCategoryChange = { category = it },
//                 color = color,
//                 onColorChange = { color = it },
//                 brand = brand,
//                 onBrandChange = { brand = it },
//                 additionalDetails = additionalDetails,
//                 onAdditionalDetailsChange = { additionalDetails = it },
//                 location = location,
//                 onLocationChange = { location = it },
//                 dateTime = dateTime,
//                 onDateTimeChange = { dateTime = it },
//                 selectedImageUri = selectedImageUri,
//                 onImageSelected = { imagePickerLauncher.launch("image/*") },
//                 onImageRemoved = { selectedImageUri = null },
//                 showCategoryDropdown = showCategoryDropdown,
//                 onCategoryDropdownChange = { showCategoryDropdown = it },
//                 onContinue = {
//                     if (itemName.isBlank()) {
//                         errorMessage = "Please enter item name"
//                         return@Step2ItemDetails
//                     }
//                     if (category.isBlank()) {
//                         errorMessage = "Please select category"
//                         return@Step2ItemDetails
//                     }
//                     if (location.isBlank()) {
//                         errorMessage = "Please enter location"
//                         return@Step2ItemDetails
//                     }
//                     errorMessage = null
//                     currentStep = 3
//                 },
//                 errorMessage = errorMessage
//             )
//             3 -> Step3ContactAndReview(
//                 reportType = reportType ?: ItemType.LOST,
//                 itemName = itemName,
//                 category = category,
//                 color = color,
//                 brand = brand,
//                 location = location,
//                 dateTime = dateTime,
//                 contactEmail = contactEmail,
//                 onContactEmailChange = { contactEmail = it },
//                 contactPhone = contactPhone,
//                 onContactPhoneChange = { contactPhone = it },
//                 additionalNotes = additionalNotes,
//                 onAdditionalNotesChange = { additionalNotes = it },
//                 isLoading = isLoading,
//                 onSubmit = {
//                     isLoading = true
//                     scope.launch {
//                         try {
//                             val description = buildString {
//                                 if (category.isNotBlank()) append("Category: $category\n")
//                                 if (color.isNotBlank()) append("Color: $color\n")
//                                 if (brand.isNotBlank()) append("Brand: $brand\n")
//                                 if (additionalDetails.isNotBlank()) append("Details: $additionalDetails\n")
//                                 if (additionalNotes.isNotBlank()) append("Notes: $additionalNotes")
//                             }
                            
//                             val item = Item(
//                                 itemName = itemName,
//                                 description = description,
//                                 location = location,
//                                 contactEmail = contactEmail,
//                                 contactPhone = contactPhone,
//                                 category = category,
//                                 color = color,
//                                 brand = brand,
//                                 additionalDetails = additionalDetails,
//                                 type = reportType ?: ItemType.LOST,
//                                 date = System.currentTimeMillis()
//                             )
//                             ItemsRepository.addItem(item, selectedImageUri, context)
//                             onSubmit()
//                         } catch (e: Exception) {
//                             errorMessage = "Failed to submit: ${e.message}"
//                             isLoading = false
//                         }
//                     }
//                 }
//             )
//         }
//     }
// }

// @Composable
// private fun StepIndicator(step: Int, isCompleted: Boolean, isCurrent: Boolean) {
//     Surface(
//         shape = CircleShape,
//         color = when {
//             isCurrent -> PrimaryPurple
//             isCompleted -> PrimaryPurple
//             else -> Color(0xFFE5E7EB)
//         },
//         modifier = Modifier.size(32.dp)
//     ) {
//         Box(contentAlignment = Alignment.Center) {
//             Text(
//                 text = step.toString(),
//                 color = if (isCompleted || isCurrent) Color.White else TextGray,
//                 fontWeight = FontWeight.Bold,
//                 fontSize = 14.sp
//             )
//         }
//     }
// }

// @Composable
// private fun Step1SelectType(
//     onTypeSelected: (ItemType) -> Unit
// ) {
//     Column(
//         modifier = Modifier
//             .fillMaxSize()
//             .padding(24.dp),
//         verticalArrangement = Arrangement.spacedBy(16.dp)
//     ) {
//         Text(
//             text = "What are you reporting?",
//             style = MaterialTheme.typography.headlineSmall,
//             color = TextDark,
//             fontWeight = FontWeight.Bold
//         )
//         Text(
//             text = "Choose the best option that matches your situation",
//             style = MaterialTheme.typography.bodyMedium,
//             color = TextGray
//         )
        
//         Spacer(modifier = Modifier.height(16.dp))
        
//         // I Lost an Item Card
//         Card(
//             modifier = Modifier
//                 .fillMaxWidth()
//                 .clickable { onTypeSelected(ItemType.LOST) },
//             colors = CardDefaults.cardColors(containerColor = CardWhite),
//             elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
//             shape = RoundedCornerShape(16.dp)
//         ) {
//             Row(
//                 modifier = Modifier.padding(20.dp),
//                 verticalAlignment = Alignment.CenterVertically
//             ) {
//                 Box(
//                     modifier = Modifier
//                         .size(56.dp)
//                         .clip(RoundedCornerShape(12.dp))
//                         .background(LightPurple),
//                     contentAlignment = Alignment.Center
//                 ) {
//                     Icon(
//                         Icons.Default.Lock,
//                         contentDescription = null,
//                         tint = PrimaryPurple,
//                         modifier = Modifier.size(28.dp)
//                     )
//                 }
                
//                 Spacer(modifier = Modifier.width(16.dp))
                
//                 Column(modifier = Modifier.weight(1f)) {
//                     Text(
//                         text = "I Lost an Item",
//                         style = MaterialTheme.typography.titleMedium,
//                         color = TextDark,
//                         fontWeight = FontWeight.Bold
//                     )
//                     Spacer(modifier = Modifier.height(4.dp))
//                     Text(
//                         text = "Report an item you have lost so others can help you find it.",
//                         style = MaterialTheme.typography.bodySmall,
//                         color = TextGray
//                     )
//                 }
                
//                 Icon(
//                     Icons.Default.KeyboardArrowRight,
//                     contentDescription = null,
//                     tint = TextGray
//                 )
//             }
//         }
        
//         // I Found an Item Card
//         Card(
//             modifier = Modifier
//                 .fillMaxWidth()
//                 .clickable { onTypeSelected(ItemType.FOUND) },
//             colors = CardDefaults.cardColors(containerColor = CardWhite),
//             elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
//             shape = RoundedCornerShape(16.dp)
//         ) {
//             Row(
//                 modifier = Modifier.padding(20.dp),
//                 verticalAlignment = Alignment.CenterVertically
//             ) {
//                 Box(
//                     modifier = Modifier
//                         .size(56.dp)
//                         .clip(RoundedCornerShape(12.dp))
//                         .background(Color(0xFFDCFCE7)),
//                     contentAlignment = Alignment.Center
//                 ) {
//                     Icon(
//                         Icons.Default.CheckCircle,
//                         contentDescription = null,
//                         tint = Color(0xFF10B981),
//                         modifier = Modifier.size(28.dp)
//                     )
//                 }
                
//                 Spacer(modifier = Modifier.width(16.dp))
                
//                 Column(modifier = Modifier.weight(1f)) {
//                     Text(
//                         text = "I Found an Item",
//                         style = MaterialTheme.typography.titleMedium,
//                         color = TextDark,
//                         fontWeight = FontWeight.Bold
//                     )
//                     Spacer(modifier = Modifier.height(4.dp))
//                     Text(
//                         text = "Report an item you have found so we can reunite it with the owner.",
//                         style = MaterialTheme.typography.bodySmall,
//                         color = TextGray
//                     )
//                 }
                
//                 Icon(
//                     Icons.Default.KeyboardArrowRight,
//                     contentDescription = null,
//                     tint = TextGray
//                 )
//             }
//         }
        
//         Spacer(modifier = Modifier.weight(1f))
        
//         // Privacy Notice
//         Card(
//             colors = CardDefaults.cardColors(containerColor = LightPurple),
//             shape = RoundedCornerShape(12.dp)
//         ) {
//             Row(
//                 modifier = Modifier.padding(16.dp),
//                 verticalAlignment = Alignment.CenterVertically
//             ) {
//                 Icon(
//                     Icons.Default.Lock,
//                     contentDescription = null,
//                     tint = PrimaryPurple,
//                     modifier = Modifier.size(20.dp)
//                 )
//                 Spacer(modifier = Modifier.width(12.dp))
//                 Column {
//                     Text(
//                         text = "Your information is safe",
//                         style = MaterialTheme.typography.labelMedium,
//                         color = TextDark,
//                         fontWeight = FontWeight.Bold
//                     )
//                     Text(
//                         text = "We prioritize your privacy and only share information when necessary to return items.",
//                         style = MaterialTheme.typography.bodySmall,
//                         color = TextGray,
//                         fontSize = 11.sp
//                     )
//                 }
//             }
//         }
//     }
// }

// @Composable
// private fun Step2ItemDetails(
//     itemName: String,
//     onItemNameChange: (String) -> Unit,
//     category: String,
//     onCategoryChange: (String) -> Unit,
//     color: String,
//     onColorChange: (String) -> Unit,
//     brand: String,
//     onBrandChange: (String) -> Unit,
//     additionalDetails: String,
//     onAdditionalDetailsChange: (String) -> Unit,
//     location: String,
//     onLocationChange: (String) -> Unit,
//     dateTime: String,
//     onDateTimeChange: (String) -> Unit,
//     selectedImageUri: Uri?,
//     onImageSelected: () -> Unit,
//     onImageRemoved: () -> Unit,
//     showCategoryDropdown: Boolean,
//     onCategoryDropdownChange: (Boolean) -> Unit,
//     onContinue: () -> Unit,
//     errorMessage: String?
// ) {
//     LazyColumn(
//         modifier = Modifier
//             .fillMaxSize()
//             .padding(horizontal = 20.dp),
//         verticalArrangement = Arrangement.spacedBy(12.dp)
//     ) {
//         item {
//             Spacer(modifier = Modifier.height(8.dp))
            
//             // Section Header
//             Row(verticalAlignment = Alignment.CenterVertically) {
//                 Icon(
//                     Icons.Default.List,
//                     contentDescription = null,
//                     tint = PrimaryPurple,
//                     modifier = Modifier.size(20.dp)
//                 )
//                 Spacer(modifier = Modifier.width(8.dp))
//                 Text(
//                     text = "Item Details",
//                     style = MaterialTheme.typography.titleMedium,
//                     color = TextDark,
//                     fontWeight = FontWeight.Bold
//                 )
//             }
            
//             Spacer(modifier = Modifier.height(8.dp))
            
//             // Error message
//             errorMessage?.let { error ->
//                 Card(
//                     colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
//                     shape = RoundedCornerShape(8.dp)
//                 ) {
//                     Text(
//                         text = error,
//                         color = Color(0xFFEF4444),
//                         modifier = Modifier.padding(12.dp),
//                         fontSize = 13.sp
//                     )
//                 }
//             }
            
//             // Item Name
//             Text(
//                 text = "Item Name *",
//                 style = MaterialTheme.typography.labelMedium,
//                 color = TextDark,
//                 fontWeight = FontWeight.SemiBold
//             )
//             OutlinedTextField(
//                 value = itemName,
//                 onValueChange = onItemNameChange,
//                 placeholder = { Text("e.g., iPhone 13, Black Wallet, Car Keys", fontSize = 14.sp) },
//                 modifier = Modifier.fillMaxWidth(),
//                 shape = RoundedCornerShape(12.dp),
//                 colors = OutlinedTextFieldDefaults.colors(
//                     unfocusedContainerColor = Color.White,
//                     focusedContainerColor = Color.White,
//                     unfocusedBorderColor = Color(0xFFE5E7EB),
//                     focusedBorderColor = PrimaryPurple
//                 )
//             )
            
//             // Category
//             Text(
//                 text = "Category *",
//                 style = MaterialTheme.typography.labelMedium,
//                 color = TextDark,
//                 fontWeight = FontWeight.SemiBold
//             )
//             Box {
//                 OutlinedTextField(
//                     value = category,
//                     onValueChange = {},
//                     readOnly = true,
//                     placeholder = { Text("Select Category", fontSize = 14.sp) },
//                     trailingIcon = {
//                         IconButton(onClick = { onCategoryDropdownChange(!showCategoryDropdown) }) {
//                             Icon(
//                                 if (showCategoryDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
//                                 contentDescription = "Dropdown"
//                             )
//                         }
//                     },
//                     modifier = Modifier
//                         .fillMaxWidth()
//                         .clickable { onCategoryDropdownChange(!showCategoryDropdown) },
//                     shape = RoundedCornerShape(12.dp),
//                     colors = OutlinedTextFieldDefaults.colors(
//                         unfocusedContainerColor = Color.White,
//                         focusedContainerColor = Color.White,
//                         unfocusedBorderColor = Color(0xFFE5E7EB),
//                         focusedBorderColor = PrimaryPurple
//                     )
//                 )
                
//                 DropdownMenu(
//                     expanded = showCategoryDropdown,
//                     onDismissRequest = { onCategoryDropdownChange(false) },
//                     modifier = Modifier.fillMaxWidth(0.9f)
//                 ) {
//                     com.lostandfound.data.models.ItemCategories.categories.forEach { cat ->
//                         DropdownMenuItem(
//                             text = { Text(cat) },
//                             onClick = {
//                                 onCategoryChange(cat)
//                                 onCategoryDropdownChange(false)
//                             }
//                         )
//                     }
//                 }
//             }
            
//             // Color
//             Text(
//                 text = "Color (e.g., Black, Blue)",
//                 style = MaterialTheme.typography.labelMedium,
//                 color = TextDark,
//                 fontWeight = FontWeight.SemiBold
//             )
//             OutlinedTextField(
//                 value = color,
//                 onValueChange = onColorChange,
//                 placeholder = { Text("e.g., Black", fontSize = 14.sp) },
//                 modifier = Modifier.fillMaxWidth(),
//                 shape = RoundedCornerShape(12.dp),
//                 colors = OutlinedTextFieldDefaults.colors(
//                     unfocusedContainerColor = Color.White,
//                     focusedContainerColor = Color.White,
//                     unfocusedBorderColor = Color(0xFFE5E7EB),
//                     focusedBorderColor = PrimaryPurple
//                 )
//             )
            
//             // Brand
//             Text(
//                 text = "Brand (e.g., Apple, Samsung)",
//                 style = MaterialTheme.typography.labelMedium,
//                 color = TextDark,
//                 fontWeight = FontWeight.SemiBold
//             )
//             OutlinedTextField(
//                 value = brand,
//                 onValueChange = onBrandChange,
//                 placeholder = { Text("e.g., Apple", fontSize = 14.sp) },
//                 modifier = Modifier.fillMaxWidth(),
//                 shape = RoundedCornerShape(12.dp),
//                 colors = OutlinedTextFieldDefaults.colors(
//                     unfocusedContainerColor = Color.White,
//                     focusedContainerColor = Color.White,
//                     unfocusedBorderColor = Color(0xFFE5E7EB),
//                     focusedBorderColor = PrimaryPurple
//                 )
//             )
            
//             // Additional Details
//             Text(
//                 text = "Additional Details (e.g., scratches, stickers, unique features)",
//                 style = MaterialTheme.typography.labelMedium,
//                 color = TextDark,
//                 fontWeight = FontWeight.SemiBold
//             )
//             OutlinedTextField(
//                 value = additionalDetails,
//                 onValueChange = onAdditionalDetailsChange,
//                 placeholder = { Text("Describe unique features, marks, or anything that can help identify the item", fontSize = 14.sp) },
//                 modifier = Modifier.fillMaxWidth(),
//                 shape = RoundedCornerShape(12.dp),
//                 minLines = 3,
//                 colors = OutlinedTextFieldDefaults.colors(
//                     unfocusedContainerColor = Color.White,
//                     focusedContainerColor = Color.White,
//                     unfocusedBorderColor = Color(0xFFE5E7EB),
//                     focusedBorderColor = PrimaryPurple
//                 )
//             )
            
//             Spacer(modifier = Modifier.height(8.dp))
            
//             // Image Upload Section
//             Row(verticalAlignment = Alignment.CenterVertically) {
//                 Icon(
//                     Icons.Default.Info,
//                     contentDescription = null,
//                     tint = PrimaryPurple,
//                     modifier = Modifier.size(20.dp)
//                 )
//                 Spacer(modifier = Modifier.width(8.dp))
//                 Text(
//                     text = "Item Photo (Optional)",
//                     style = MaterialTheme.typography.titleMedium,
//                     color = TextDark,
//                     fontWeight = FontWeight.Bold
//                 )
//             }
            
//             Text(
//                 text = "Upload a photo to help identify your item",
//                 style = MaterialTheme.typography.bodySmall,
//                 color = TextGray,
//                 fontSize = 12.sp
//             )
            
//             Spacer(modifier = Modifier.height(8.dp))
            
//             // Image Upload Card
//             if (selectedImageUri != null) {
//                 // Show selected image
//                 Card(
//                     modifier = Modifier
//                         .fillMaxWidth()
//                         .height(200.dp),
//                     shape = RoundedCornerShape(12.dp),
//                     colors = CardDefaults.cardColors(containerColor = Color.White),
//                     elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//                 ) {
//                     Box(modifier = Modifier.fillMaxSize()) {
//                         AsyncImage(
//                             model = selectedImageUri,
//                             contentDescription = "Selected item image",
//                             modifier = Modifier.fillMaxSize(),
//                             contentScale = ContentScale.Crop
//                         )
                        
//                         // Remove button
//                         IconButton(
//                             onClick = onImageRemoved,
//                             modifier = Modifier
//                                 .align(Alignment.TopEnd)
//                                 .padding(8.dp)
//                                 .background(Color.Black.copy(alpha = 0.6f), CircleShape)
//                         ) {
//                             Icon(
//                                 Icons.Default.Close,
//                                 contentDescription = "Remove image",
//                                 tint = Color.White
//                             )
//                         }
//                     }
//                 }
//             } else {
//                 // Show upload button
//                 Card(
//                     modifier = Modifier
//                         .fillMaxWidth()
//                         .height(160.dp)
//                         .clickable { onImageSelected() },
//                     shape = RoundedCornerShape(12.dp),
//                     colors = CardDefaults.cardColors(containerColor = LightPurple),
//                     elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
//                 ) {
//                     Column(
//                         modifier = Modifier.fillMaxSize(),
//                         horizontalAlignment = Alignment.CenterHorizontally,
//                         verticalArrangement = Arrangement.Center
//                     ) {
//                         Box(
//                             modifier = Modifier
//                                 .size(56.dp)
//                                 .clip(CircleShape)
//                                 .background(PrimaryPurple),
//                             contentAlignment = Alignment.Center
//                         ) {
//                             Icon(
//                                 Icons.Default.Add,
//                                 contentDescription = null,
//                                 tint = Color.White,
//                                 modifier = Modifier.size(28.dp)
//                             )
//                         }
                        
//                         Spacer(modifier = Modifier.height(12.dp))
                        
//                         Text(
//                             text = "Upload Photo",
//                             style = MaterialTheme.typography.titleMedium,
//                             color = PrimaryPurple,
//                             fontWeight = FontWeight.Bold
//                         )
                        
//                         Spacer(modifier = Modifier.height(4.dp))
                        
//                         Text(
//                             text = "Tap to select an image from your gallery",
//                             style = MaterialTheme.typography.bodySmall,
//                             color = TextGray,
//                             fontSize = 12.sp,
//                             textAlign = TextAlign.Center,
//                             modifier = Modifier.padding(horizontal = 32.dp)
//                         )
//                     }
//                 }
//             }
            
//             Spacer(modifier = Modifier.height(8.dp))
            
//             // When & Where Lost Section
//             Row(verticalAlignment = Alignment.CenterVertically) {
//                 Icon(
//                     Icons.Default.LocationOn,
//                     contentDescription = null,
//                     tint = PrimaryPurple,
//                     modifier = Modifier.size(20.dp)
//                 )
//                 Spacer(modifier = Modifier.width(8.dp))
//                 Text(
//                     text = "When & Where Lost",
//                     style = MaterialTheme.typography.titleMedium,
//                     color = TextDark,
//                     fontWeight = FontWeight.Bold
//                 )
//             }
            
//             Spacer(modifier = Modifier.height(8.dp))
            
//             // Lost Date & Time
//             Text(
//                 text = "Lost Date & Time *",
//                 style = MaterialTheme.typography.labelMedium,
//                 color = TextDark,
//                 fontWeight = FontWeight.SemiBold
//             )
//             OutlinedTextField(
//                 value = dateTime,
//                 onValueChange = onDateTimeChange,
//                 placeholder = { Text("Select date and time", fontSize = 14.sp) },
//                 trailingIcon = {
//                     Icon(Icons.Default.DateRange, contentDescription = null)
//                 },
//                 modifier = Modifier.fillMaxWidth(),
//                 shape = RoundedCornerShape(12.dp),
//                 colors = OutlinedTextFieldDefaults.colors(
//                     unfocusedContainerColor = Color.White,
//                     focusedContainerColor = Color.White,
//                     unfocusedBorderColor = Color(0xFFE5E7EB),
//                     focusedBorderColor = PrimaryPurple
//                 )
//             )
            
//             // Specific Location
//             Text(
//                 text = "Specific Location where lost *",
//                 style = MaterialTheme.typography.labelMedium,
//                 color = TextDark,
//                 fontWeight = FontWeight.SemiBold
//             )
//             OutlinedTextField(
//                 value = location,
//                 onValueChange = onLocationChange,
//                 placeholder = { Text("e.g., Library, Cafeteria, Classroom", fontSize = 14.sp) },
//                 modifier = Modifier.fillMaxWidth(),
//                 shape = RoundedCornerShape(12.dp),
//                 colors = OutlinedTextFieldDefaults.colors(
//                     unfocusedContainerColor = Color.White,
//                     focusedContainerColor = Color.White,
//                     unfocusedBorderColor = Color(0xFFE5E7EB),
//                     focusedBorderColor = PrimaryPurple
//                 )
//             )
            
//             Text(
//                 text = "Please enter location manually",
//                 style = MaterialTheme.typography.bodySmall,
//                 color = PrimaryPurple,
//                 fontSize = 11.sp
//             )
            
//             Spacer(modifier = Modifier.height(16.dp))
            
//             // Continue Button
//             Button(
//                 onClick = onContinue,
//                 modifier = Modifier
//                     .fillMaxWidth()
//                     .height(52.dp),
//                 colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
//                 shape = RoundedCornerShape(12.dp)
//             ) {
//                 Text(
//                     text = "Continue",
//                     fontSize = 16.sp,
//                     fontWeight = FontWeight.SemiBold
//                 )
//             }
            
//             Spacer(modifier = Modifier.height(80.dp))
//         }
//     }
// }

// @Composable
// private fun Step3ContactAndReview(
//     reportType: ItemType,
//     itemName: String,
//     category: String,
//     color: String,
//     brand: String,
//     location: String,
//     dateTime: String,
//     contactEmail: String,
//     onContactEmailChange: (String) -> Unit,
//     contactPhone: String,
//     onContactPhoneChange: (String) -> Unit,
//     additionalNotes: String,
//     onAdditionalNotesChange: (String) -> Unit,
//     isLoading: Boolean,
//     onSubmit: () -> Unit
// ) {
//     var localErrorMessage by remember { mutableStateOf<String?>(null) }
    
//     LazyColumn(
//         modifier = Modifier
//             .fillMaxSize()
//             .padding(horizontal = 20.dp),
//         verticalArrangement = Arrangement.spacedBy(12.dp)
//     ) {
//         item {
//             Spacer(modifier = Modifier.height(8.dp))
            
//             // Error message
//             localErrorMessage?.let { error ->
//                 Card(
//                     colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
//                     shape = RoundedCornerShape(8.dp)
//                 ) {
//                     Text(
//                         text = error,
//                         color = Color(0xFFEF4444),
//                         modifier = Modifier.padding(12.dp),
//                         fontSize = 13.sp
//                     )
//                 }
//             }
            
//             // Contact Information Section
//             Row(verticalAlignment = Alignment.CenterVertically) {
//                 Icon(
//                     Icons.Default.Email,
//                     contentDescription = null,
//                     tint = PrimaryPurple,
//                     modifier = Modifier.size(20.dp)
//                 )
//                 Spacer(modifier = Modifier.width(8.dp))
//                 Text(
//                     text = "Contact Information",
//                     style = MaterialTheme.typography.titleMedium,
//                     color = TextDark,
//                     fontWeight = FontWeight.Bold
//                 )
//             }
            
//             Text(
//                 text = "Provide at least one contact method so we can reach you",
//                 style = MaterialTheme.typography.bodySmall,
//                 color = TextGray,
//                 fontSize = 12.sp
//             )
            
//             Spacer(modifier = Modifier.height(8.dp))
            
//             // Contact Email
//             Text(
//                 text = "Contact Email *",
//                 style = MaterialTheme.typography.labelMedium,
//                 color = TextDark,
//                 fontWeight = FontWeight.SemiBold
//             )
//             OutlinedTextField(
//                 value = contactEmail,
//                 onValueChange = {
//                     onContactEmailChange(it)
//                     localErrorMessage = null
//                 },
//                 placeholder = { Text("Enter email address", fontSize = 14.sp) },
//                 modifier = Modifier.fillMaxWidth(),
//                 shape = RoundedCornerShape(12.dp),
//                 colors = OutlinedTextFieldDefaults.colors(
//                     unfocusedContainerColor = Color.White,
//                     focusedContainerColor = Color.White,
//                     unfocusedBorderColor = Color(0xFFE5E7EB),
//                     focusedBorderColor = PrimaryPurple
//                 )
//             )
            
//             // Contact Phone
//             Text(
//                 text = "Contact Phone Number *",
//                 style = MaterialTheme.typography.labelMedium,
//                 color = TextDark,
//                 fontWeight = FontWeight.SemiBold
//             )
//             OutlinedTextField(
//                 value = contactPhone,
//                 onValueChange = {
//                     onContactPhoneChange(it)
//                     localErrorMessage = null
//                 },
//                 placeholder = { Text("Enter phone number", fontSize = 14.sp) },
//                 modifier = Modifier.fillMaxWidth(),
//                 shape = RoundedCornerShape(12.dp),
//                 colors = OutlinedTextFieldDefaults.colors(
//                     unfocusedContainerColor = Color.White,
//                     focusedContainerColor = Color.White,
//                     unfocusedBorderColor = Color(0xFFE5E7EB),
//                     focusedBorderColor = PrimaryPurple
//                 )
//             )
            
//             Spacer(modifier = Modifier.height(8.dp))
            
//             // Additional Notes Section
//             Row(verticalAlignment = Alignment.CenterVertically) {
//                 Icon(
//                     Icons.Default.List,
//                     contentDescription = null,
//                     tint = PrimaryPurple,
//                     modifier = Modifier.size(20.dp)
//                 )
//                 Spacer(modifier = Modifier.width(8.dp))
//                 Text(
//                     text = "Additional Notes (Optional)",
//                     style = MaterialTheme.typography.titleMedium,
//                     color = TextDark,
//                     fontWeight = FontWeight.Bold
//                 )
//             }
            
//             Spacer(modifier = Modifier.height(8.dp))
            
//             OutlinedTextField(
//                 value = additionalNotes,
//                 onValueChange = onAdditionalNotesChange,
//                 placeholder = { Text("Add any other information that might help in finding your item", fontSize = 14.sp) },
//                 modifier = Modifier.fillMaxWidth(),
//                 shape = RoundedCornerShape(12.dp),
//                 minLines = 3,
//                 colors = OutlinedTextFieldDefaults.colors(
//                     unfocusedContainerColor = Color.White,
//                     focusedContainerColor = Color.White,
//                     unfocusedBorderColor = Color(0xFFE5E7EB),
//                     focusedBorderColor = PrimaryPurple
//                 )
//             )
            
//             Spacer(modifier = Modifier.height(8.dp))
            
//             // Review & Confirm Section
//             Row(verticalAlignment = Alignment.CenterVertically) {
//                 Icon(
//                     Icons.Default.CheckCircle,
//                     contentDescription = null,
//                     tint = PrimaryPurple,
//                     modifier = Modifier.size(20.dp)
//                 )
//                 Spacer(modifier = Modifier.width(8.dp))
//                 Text(
//                     text = "Review & Confirm",
//                     style = MaterialTheme.typography.titleMedium,
//                     color = TextDark,
//                     fontWeight = FontWeight.Bold
//                 )
//             }
            
//             Text(
//                 text = "Please review your details before submitting",
//                 style = MaterialTheme.typography.bodySmall,
//                 color = TextGray
//             )
            
//             Spacer(modifier = Modifier.height(8.dp))
            
//             // Review Card
//             Card(
//                 colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
//                 shape = RoundedCornerShape(12.dp)
//             ) {
//                 Column(modifier = Modifier.padding(16.dp)) {
//                     ReviewRow("Item Name", itemName)
//                     ReviewRow("Category", category)
//                     if (color.isNotBlank()) ReviewRow("Color", color)
//                     if (brand.isNotBlank()) ReviewRow("Brand", brand)
//                     if (dateTime.isNotBlank()) ReviewRow("Lost Date & Time", dateTime)
//                     ReviewRow("Location", location)
//                     if (contactEmail.isNotBlank()) ReviewRow("Contact", contactEmail)
//                 }
//             }
            
//             Spacer(modifier = Modifier.height(16.dp))
            
//             // Submit Button
//             Button(
//                 onClick = {
//                     // Validate contact information
//                     if (contactEmail.isBlank() && contactPhone.isBlank()) {
//                         localErrorMessage = "Please provide at least one contact method (email or phone)"
//                         return@Button
//                     }
//                     if (contactEmail.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(contactEmail).matches()) {
//                         localErrorMessage = "Please enter a valid email address"
//                         return@Button
//                     }
//                     localErrorMessage = null
//                     onSubmit()
//                 },
//                 modifier = Modifier
//                     .fillMaxWidth()
//                     .height(52.dp),
//                 colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
//                 shape = RoundedCornerShape(12.dp),
//                 enabled = !isLoading
//             ) {
//                 if (isLoading) {
//                     CircularProgressIndicator(
//                         color = Color.White,
//                         modifier = Modifier.size(24.dp)
//                     )
//                 } else {
//                     Text(
//                         text = "Submit Report",
//                         fontSize = 16.sp,
//                         fontWeight = FontWeight.SemiBold
//                     )
//                 }
//             }
            
//             // Privacy Notice
//             Row(
//                 modifier = Modifier.fillMaxWidth(),
//                 horizontalArrangement = Arrangement.Center,
//                 verticalAlignment = Alignment.CenterVertically
//             ) {
//                 Icon(
//                     Icons.Default.Lock,
//                     contentDescription = null,
//                     tint = TextGray,
//                     modifier = Modifier.size(14.dp)
//                 )
//                 Spacer(modifier = Modifier.width(4.dp))
//                 Text(
//                     text = "Your report is safe and private",
//                     style = MaterialTheme.typography.bodySmall,
//                     color = TextGray,
//                     fontSize = 11.sp
//                 )
//             }
            
//             Spacer(modifier = Modifier.height(80.dp))
//         }
//     }
// }

// @Composable
// private fun ReviewRow(label: String, value: String) {
//     if (value.isNotBlank()) {
//         Row(
//             modifier = Modifier
//                 .fillMaxWidth()
//                 .padding(vertical = 6.dp),
//             horizontalArrangement = Arrangement.SpaceBetween
//         ) {
//             Text(
//                 text = label,
//                 style = MaterialTheme.typography.bodySmall,
//                 color = TextGray,
//                 fontWeight = FontWeight.Medium
//             )
//             Text(
//                 text = value,
//                 style = MaterialTheme.typography.bodySmall,
//                 color = TextDark,
//                 fontWeight = FontWeight.SemiBold,
//                 textAlign = TextAlign.End,
//                 modifier = Modifier.weight(1f, fill = false)
//             )
//         }
//     }
// }

// @Composable
// fun FormField(
//     icon: ImageVector,
//     placeholder: String,
//     value: String,
//     onValueChange: (String) -> Unit,
//     backgroundColor: Color,
//     iconColor: Color = Color(0xFF8B5CF6),
//     focusedBorderColor: Color = Color(0xFF8B5CF6),
//     minLines: Int = 1
// ) {
//     OutlinedTextField(
//         value = value,
//         onValueChange = onValueChange,
//         placeholder = { Text(placeholder) },
//         leadingIcon = {
//             Icon(
//                 icon,
//                 contentDescription = null,
//                 tint = iconColor
//             )
//         },
//         modifier = Modifier.fillMaxWidth(),
//         shape = RoundedCornerShape(12.dp),
//         colors = OutlinedTextFieldDefaults.colors(
//             unfocusedContainerColor = backgroundColor,
//             focusedContainerColor = backgroundColor,
//             unfocusedBorderColor = Color.Transparent,
//             focusedBorderColor = focusedBorderColor
//         ),
//         minLines = minLines
//     )
// }


// @OptIn(ExperimentalMaterial3Api::class)
// @Composable
// fun ReportItemScreen(
//     type: com.lostandfound.data.models.ItemType,
//     onDone: () -> Unit
// ) {
//     ReportLostItemScreen(
//         onBack = onDone,
//         onSubmit = onDone
//     )
// }
