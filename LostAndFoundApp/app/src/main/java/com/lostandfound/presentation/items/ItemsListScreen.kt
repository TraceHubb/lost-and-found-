// package com.lostandfound.presentation.items

// import androidx.compose.foundation.Image
// import androidx.compose.foundation.background
// import androidx.compose.foundation.clickable
// import androidx.compose.foundation.layout.*
// import androidx.compose.foundation.lazy.LazyColumn
// import androidx.compose.foundation.lazy.LazyRow
// import androidx.compose.foundation.lazy.items
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
// import androidx.compose.ui.res.painterResource
// import androidx.compose.ui.text.font.FontWeight
// import androidx.compose.ui.unit.dp
// import androidx.compose.ui.unit.sp
// import coil.compose.AsyncImage
// import com.lostandfound.data.models.Item
// import com.lostandfound.data.models.ItemType
// import com.lostandfound.data.repositories.ItemsRepository
// import java.text.SimpleDateFormat
// import java.util.*

// // Modern color palette
// private val PrimaryPurple = Color(0xFF8B5CF6)
// private val SecondaryPink = Color(0xFFEC4899)
// private val LightPurple = Color(0xFFF3E8FF)
// private val BackgroundWhite = Color(0xFFFAFAFA)
// private val CardWhite = Color(0xFFFFFFFF)
// private val GreenSuccess = Color(0xFF10B981)
// private val TextDark = Color(0xFF1F2937)
// private val TextGray = Color(0xFF6B7280)

// @OptIn(ExperimentalMaterial3Api::class)
// @Composable
// fun ItemsListScreen(
//     type: ItemType,
//     onBack: () -> Unit
// ) {
//     var searchQuery by remember { mutableStateOf("") }
//     var selectedCategory by remember { mutableStateOf("All") }
//     val items by ItemsRepository.observeItems(type = type, searchQuery = searchQuery)
//         .collectAsState(initial = emptyList())
    
//     val categories = listOf("All", "Electronics", "Documents", "Bags", "Keys", "Clothing")
    
//     val filteredItems = if (selectedCategory == "All") {
//         items
//     } else {
//         items.filter { it.category.equals(selectedCategory, ignoreCase = true) }
//     }

//     Column(
//         modifier = Modifier
//             .fillMaxSize()
//             .background(BackgroundWhite)
//     ) {
//         // Modern Header
//         Surface(
//             modifier = Modifier.fillMaxWidth(),
//             color = CardWhite,
//             shadowElevation = 1.dp
//         ) {
//             Column(
//                 modifier = Modifier.padding(20.dp)
//             ) {
//                 // Top Bar
//                 Row(
//                     modifier = Modifier.fillMaxWidth(),
//                     horizontalArrangement = Arrangement.SpaceBetween,
//                     verticalAlignment = Alignment.CenterVertically
//                 ) {
//                     Row(verticalAlignment = Alignment.CenterVertically) {
//                         Box(
//                             modifier = Modifier
//                                 .size(40.dp)
//                                 .clip(RoundedCornerShape(12.dp))
//                                 .background(
//                                     Brush.linearGradient(
//                                         colors = listOf(PrimaryPurple, SecondaryPink)
//                                     )
//                                 ),
//                             contentAlignment = Alignment.Center
//                         ) {
//                             Text(
//                                 text = "F",
//                                 color = Color.White,
//                                 fontSize = 20.sp,
//                                 fontWeight = FontWeight.Bold
//                             )
//                         }
//                     }
                    
//                     IconButton(onClick = {}) {
//                         Icon(
//                             Icons.Default.Notifications,
//                             contentDescription = "Notifications",
//                             tint = TextDark
//                         )
//                     }
//                 }
                
//                 Spacer(modifier = Modifier.height(16.dp))
                
//                 // Title
//                 Text(
//                     text = "Browse Items",
//                     style = MaterialTheme.typography.headlineMedium,
//                     color = TextDark,
//                     fontWeight = FontWeight.Bold
//                 )
//                 Text(
//                     text = "Find lost and found items",
//                     style = MaterialTheme.typography.bodyMedium,
//                     color = TextGray
//                 )
                
//                 Spacer(modifier = Modifier.height(16.dp))
                
//                 // Search Bar with Gradient
//                 Box(
//                     modifier = Modifier
//                         .fillMaxWidth()
//                         .clip(RoundedCornerShape(16.dp))
//                         .background(
//                             Brush.horizontalGradient(
//                                 colors = listOf(
//                                     LightPurple,
//                                     Color.White
//                                 )
//                             )
//                         )
//                 ) {
//                     Row(
//                         modifier = Modifier
//                             .fillMaxWidth()
//                             .padding(horizontal = 16.dp, vertical = 14.dp),
//                         verticalAlignment = Alignment.CenterVertically
//                     ) {
//                         Icon(
//                             imageVector = Icons.Default.Search,
//                             contentDescription = "Search",
//                             tint = PrimaryPurple,
//                             modifier = Modifier.size(24.dp)
//                         )
                        
//                         TextField(
//                             value = searchQuery,
//                             onValueChange = { searchQuery = it },
//                             modifier = Modifier.weight(1f),
//                             placeholder = { 
//                                 Text(
//                                     "Search items (wallet, phone, keys...)",
//                                     color = TextGray,
//                                     fontSize = 14.sp
//                                 ) 
//                             },
//                             colors = TextFieldDefaults.colors(
//                                 focusedContainerColor = Color.Transparent,
//                                 unfocusedContainerColor = Color.Transparent,
//                                 disabledContainerColor = Color.Transparent,
//                                 focusedIndicatorColor = Color.Transparent,
//                                 unfocusedIndicatorColor = Color.Transparent,
//                                 focusedTextColor = TextDark,
//                                 unfocusedTextColor = TextDark
//                             ),
//                             singleLine = true
//                         )
                        
//                         Icon(
//                             Icons.Default.List,
//                             contentDescription = "Filter",
//                             tint = PrimaryPurple,
//                             modifier = Modifier.size(24.dp)
//                         )
//                     }
//                 }
//             }
//         }
        
//         // Category Filters
//         LazyRow(
//             modifier = Modifier
//                 .fillMaxWidth()
//                 .padding(vertical = 16.dp),
//             horizontalArrangement = Arrangement.spacedBy(12.dp),
//             contentPadding = PaddingValues(horizontal = 20.dp)
//         ) {
//             items(categories) { category ->
//                 CategoryChip(
//                     category = category,
//                     isSelected = selectedCategory == category,
//                     onClick = { selectedCategory = category },
//                     icon = when(category) {
//                         "All" -> Icons.Default.Star
//                         "Electronics" -> Icons.Default.Phone
//                         "Documents" -> Icons.Default.List
//                         "Bags" -> Icons.Default.ShoppingCart
//                         "Keys" -> Icons.Default.Lock
//                         "Clothing" -> Icons.Default.Person
//                         else -> Icons.Default.Star
//                     }
//                 )
//             }
//         }
        
//         // Section Header
//         Row(
//             modifier = Modifier
//                 .fillMaxWidth()
//                 .padding(horizontal = 20.dp),
//             horizontalArrangement = Arrangement.SpaceBetween,
//             verticalAlignment = Alignment.CenterVertically
//         ) {
//             Text(
//                 text = "Recent Items",
//                 style = MaterialTheme.typography.titleMedium,
//                 color = TextDark,
//                 fontWeight = FontWeight.Bold
//             )
//             Text(
//                 text = "Sort: Latest",
//                 style = MaterialTheme.typography.bodySmall,
//                 color = TextGray
//             )
//         }
        
//         Spacer(modifier = Modifier.height(12.dp))
        
//         // Items List
//         if (filteredItems.isEmpty()) {
//             Box(
//                 modifier = Modifier
//                     .fillMaxSize()
//                     .padding(16.dp),
//                 contentAlignment = Alignment.Center
//             ) {
//                 Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                     Icon(
//                         Icons.Default.Search,
//                         contentDescription = null,
//                         modifier = Modifier.size(64.dp),
//                         tint = TextGray
//                     )
//                     Spacer(modifier = Modifier.height(16.dp))
//                     Text(
//                         text = "No items found",
//                         style = MaterialTheme.typography.titleMedium,
//                         color = TextDark
//                     )
//                     Text(
//                         text = "Try adjusting your search or filters",
//                         style = MaterialTheme.typography.bodyMedium,
//                         color = TextGray
//                     )
//                 }
//             }
//         } else {
//             LazyColumn(
//                 modifier = Modifier
//                     .fillMaxSize()
//                     .padding(horizontal = 20.dp),
//                 verticalArrangement = Arrangement.spacedBy(12.dp)
//             ) {
//                 items(filteredItems) { item ->
//                     ModernItemCard(item = item)
//                 }
                
//                 item {
//                     Spacer(modifier = Modifier.height(80.dp))
//                 }
//             }
//         }
//     }
// }

// @Composable
// private fun CategoryChip(
//     category: String,
//     isSelected: Boolean,
//     onClick: () -> Unit,
//     icon: ImageVector
// ) {
//     Surface(
//         onClick = onClick,
//         shape = RoundedCornerShape(12.dp),
//         color = if (isSelected) PrimaryPurple else Color.White,
//         modifier = Modifier.height(48.dp),
//         shadowElevation = if (isSelected) 4.dp else 1.dp
//     ) {
//         Row(
//             modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
//             verticalAlignment = Alignment.CenterVertically,
//             horizontalArrangement = Arrangement.Center
//         ) {
//             Icon(
//                 icon,
//                 contentDescription = category,
//                 tint = if (isSelected) Color.White else TextDark,
//                 modifier = Modifier.size(20.dp)
//             )
//             Spacer(modifier = Modifier.width(8.dp))
//             Text(
//                 text = category,
//                 color = if (isSelected) Color.White else TextDark,
//                 fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
//                 fontSize = 14.sp
//             )
//         }
//     }
// }

// @Composable
// private fun ModernItemCard(item: Item) {
//     Card(
//         modifier = Modifier.fillMaxWidth(),
//         colors = CardDefaults.cardColors(containerColor = CardWhite),
//         elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
//         shape = RoundedCornerShape(16.dp)
//     ) {
//         Row(
//             modifier = Modifier.padding(12.dp),
//             verticalAlignment = Alignment.Top
//         ) {
//             // Item Image
//             Box(
//                 modifier = Modifier
//                     .size(80.dp)
//                     .clip(RoundedCornerShape(12.dp))
//                     .background(Color(0xFFF3F4F6))
//             ) {
//                 if (item.imageUrl.isNotBlank()) {
//                     AsyncImage(
//                         model = item.imageUrl,
//                         contentDescription = item.itemName,
//                         modifier = Modifier.fillMaxSize(),
//                         contentScale = ContentScale.Crop
//                     )
//                 } else {
//                     Icon(
//                         Icons.Default.Info,
//                         contentDescription = null,
//                         modifier = Modifier
//                             .size(40.dp)
//                             .align(Alignment.Center),
//                         tint = TextGray
//                     )
//                 }
//             }

//             Spacer(modifier = Modifier.width(12.dp))

//             // Item Details
//             Column(modifier = Modifier.weight(1f)) {
//                 // Status Badge
//                 Surface(
//                     color = if (item.type == ItemType.FOUND) 
//                         Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
//                     shape = RoundedCornerShape(6.dp)
//                 ) {
//                     Text(
//                         text = if (item.type == ItemType.FOUND) "Found" else "Lost",
//                         color = if (item.type == ItemType.FOUND) 
//                             GreenSuccess else Color(0xFFEF4444),
//                         fontSize = 12.sp,
//                         fontWeight = FontWeight.SemiBold,
//                         modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
//                     )
//                 }
                
//                 Spacer(modifier = Modifier.height(6.dp))
                
//                 Text(
//                     text = item.itemName,
//                     style = MaterialTheme.typography.titleMedium,
//                     color = TextDark,
//                     fontWeight = FontWeight.Bold
//                 )
                
//                 Spacer(modifier = Modifier.height(4.dp))
                
//                 Row(verticalAlignment = Alignment.CenterVertically) {
//                     Icon(
//                         Icons.Default.LocationOn,
//                         contentDescription = null,
//                         tint = PrimaryPurple,
//                         modifier = Modifier.size(16.dp)
//                     )
//                     Spacer(modifier = Modifier.width(4.dp))
//                     Text(
//                         text = item.location,
//                         style = MaterialTheme.typography.bodySmall,
//                         color = TextGray
//                     )
//                 }
                
//                 Spacer(modifier = Modifier.height(4.dp))
                
//                 Row(verticalAlignment = Alignment.CenterVertically) {
//                     Icon(
//                         Icons.Default.DateRange,
//                         contentDescription = null,
//                         tint = TextGray,
//                         modifier = Modifier.size(14.dp)
//                     )
//                     Spacer(modifier = Modifier.width(4.dp))
//                     Text(
//                         text = getTimeAgo(item.datePosted),
//                         style = MaterialTheme.typography.bodySmall,
//                         color = TextGray,
//                         fontSize = 12.sp
//                     )
//                 }
//             }
            
//             // Bookmark Icon
//             IconButton(onClick = {}) {
//                 Icon(
//                     Icons.Default.Star,
//                     contentDescription = "Bookmark",
//                     tint = TextGray
//                 )
//             }
//         }
        
//         // Match Percentage (if applicable - you can calculate this based on your logic)
//         if (item.type == ItemType.FOUND) {
//             Divider(color = Color(0xFFE5E7EB), thickness = 1.dp)
//             Row(
//                 modifier = Modifier
//                     .fillMaxWidth()
//                     .padding(12.dp),
//                 horizontalArrangement = Arrangement.End,
//                 verticalAlignment = Alignment.CenterVertically
//             ) {
//                 Text(
//                     text = "92% Match",
//                     color = PrimaryPurple,
//                     fontWeight = FontWeight.Bold,
//                     fontSize = 14.sp
//                 )
//             }
//         }
//     }
// }

// private fun getTimeAgo(timestamp: Long): String {
//     val now = System.currentTimeMillis()
//     val diff = now - timestamp
    
//     return when {
//         diff < 60000 -> "Just now"
//         diff < 3600000 -> "${diff / 60000} minutes ago"
//         diff < 86400000 -> "${diff / 3600000} hours ago"
//         diff < 604800000 -> "${diff / 86400000} days ago"
//         else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
//     }
// }
