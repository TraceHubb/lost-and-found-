package com.lostandfound.presentation.items

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportLostItemScreen(
    onBack: () -> Unit,
    onSubmit: () -> Unit
) {
    var itemName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    
    val purpleColor = Color(0xFF6B4FA0)
    val lightPurple = Color(0xFFE8E0F5)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = purpleColor
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
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Campus Lost & Found",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "dagmawitadeferes@gmail.com",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
                IconButton(onClick = {}) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color.White
                    )
                }
            }
        }
        
        // Title
        Text(
            text = "Report Lost Item",
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
                // Item Name
                FormField(
                    icon = Icons.Default.Phone,
                    placeholder = "Item name (e.g., iPhone 13)",
                    value = itemName,
                    onValueChange = { itemName = it },
                    backgroundColor = lightPurple
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                // Description
                FormField(
                    icon = Icons.Default.List,
                    placeholder = "Item Description (e.g., color, distinguishing marks)",
                    value = description,
                    onValueChange = { description = it },
                    backgroundColor = lightPurple,
                    minLines = 4
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                // Location
                FormField(
                    icon = Icons.Default.LocationOn,
                    placeholder = "Specific Location found (e.g., Library, Cafeteria)",
                    value = location,
                    onValueChange = { location = it },
                    backgroundColor = lightPurple
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                // Contact Email
                FormField(
                    icon = Icons.Default.Email,
                    placeholder = "Contact email (optional)",
                    value = contactEmail,
                    onValueChange = { contactEmail = it },
                    backgroundColor = lightPurple
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                // Contact Phone
                FormField(
                    icon = Icons.Default.Phone,
                    placeholder = "Contact phone (optional)",
                    value = contactPhone,
                    onValueChange = { contactPhone = it },
                    backgroundColor = lightPurple
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Upload Photo
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Camera",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Gallery",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Upload Item Photo(s) (Optional)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Save for later button
                Button(
                    onClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFA500)
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save for later", style = MaterialTheme.typography.titleMedium)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Post button
                Button(
                    onClick = onSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = purpleColor
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("Post Lost Item Report", style = MaterialTheme.typography.titleMedium)
                }
                
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun FormField(
    icon: ImageVector,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    backgroundColor: Color,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                icon,
                contentDescription = null,
                tint = Color(0xFF6B4FA0)
            )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = backgroundColor,
            focusedContainerColor = backgroundColor,
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Color(0xFF6B4FA0)
        ),
        minLines = minLines
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportItemScreen(
    type: com.lostandfound.data.models.ItemType,
    onDone: () -> Unit
) {
    if (type == com.lostandfound.data.models.ItemType.FOUND) {
        ReportFoundItemScreen(
            onBack = onDone,
            onSubmit = onDone
        )
    } else {
        ReportLostItemScreen(
            onBack = onDone,
            onSubmit = onDone
        )
    }
}
