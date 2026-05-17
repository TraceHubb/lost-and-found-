//package com.lostandfound.presentation.claim
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ClaimSuccessScreen(
//    contactEmail: String,
//    contactPhone: String,
//    onBack: () -> Unit,
//    onHome: () -> Unit
//) {
//    val PrimaryPurple = Color(0xFF8B5CF6)
//    val BackgroundWhite = Color(0xFFFAFAFA)
//    val SuccessGreen = Color(0xFF10B981)
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(BackgroundWhite)
//    ) {
//        // Header
//        Surface(
//            modifier = Modifier.fillMaxWidth(),
//            color = Color.White,
//            shadowElevation = 2.dp
//        ) {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    IconButton(onClick = onBack) {
//                        Icon(
//                            Icons.Default.ArrowBack,
//                            contentDescription = "Back",
//                            tint = PrimaryPurple
//                        )
//                    }
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Text(
//                        text = "Claim Successful",
//                        style = MaterialTheme.typography.titleMedium,
//                        color = Color(0xFF1F2937),
//                        fontWeight = FontWeight.Bold
//                    )
//                }
//            }
//        }
//
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(16.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//            // Success Icon
//            Card(
//                modifier = Modifier.size(120.dp),
//                colors = CardDefaults.cardColors(containerColor = SuccessGreen),
//                shape = RoundedCornerShape(60.dp)
//            ) {
//                Box(
//                    modifier = Modifier.fillMaxSize(),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Icon(
//                        Icons.Default.CheckCircle,
//                        contentDescription = "Success",
//                        tint = Color.White,
//                        modifier = Modifier.size(60.dp)
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.height(32.dp))
//
//            Text(
//                text = "Congratulations!",
//                style = MaterialTheme.typography.headlineMedium,
//                fontWeight = FontWeight.Bold,
//                color = Color(0xFF1F2937)
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            Text(
//                text = "You answered all questions correctly!",
//                style = MaterialTheme.typography.bodyLarge,
//                color = Color.Gray
//            )
//
//            Spacer(modifier = Modifier.height(32.dp))
//
//            // Contact Information Card
//            Card(
//                modifier = Modifier.fillMaxWidth(),
//                colors = CardDefaults.cardColors(containerColor = Color.White),
//                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//            ) {
//                Column(
//                    modifier = Modifier.padding(24.dp)
//                ) {
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        modifier = Modifier.padding(bottom = 16.dp)
//                    ) {
//                        Icon(
//                            Icons.Default.Person,
//                            contentDescription = null,
//                            tint = PrimaryPurple,
//                            modifier = Modifier.size(24.dp)
//                        )
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text(
//                            text = "Finder's Contact Information",
//                            style = MaterialTheme.typography.titleMedium,
//                            fontWeight = FontWeight.Bold,
//                            color = PrimaryPurple
//                        )
//                    }
//
//                    // Email
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        modifier = Modifier.padding(bottom = 12.dp)
//                    ) {
//                        Icon(
//                            Icons.Default.Email,
//                            contentDescription = "Email",
//                            tint = Color.Gray,
//                            modifier = Modifier.size(20.dp)
//                        )
//                        Spacer(modifier = Modifier.width(12.dp))
//                        Column {
//                            Text(
//                                text = "Email",
//                                style = MaterialTheme.typography.labelMedium,
//                                color = Color.Gray
//                            )
//                            Text(
//                                text = contactEmail,
//                                style = MaterialTheme.typography.bodyLarge,
//                                fontWeight = FontWeight.Medium,
//                                color = Color(0xFF1F2937)
//                            )
//                        }
//                    }
//
//                    // Phone
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Icon(
//                            Icons.Default.Phone,
//                            contentDescription = "Phone",
//                            tint = Color.Gray,
//                            modifier = Modifier.size(20.dp)
//                        )
//                        Spacer(modifier = Modifier.width(12.dp))
//                        Column {
//                            Text(
//                                text = "Phone",
//                                style = MaterialTheme.typography.labelMedium,
//                                color = Color.Gray
//                            )
//                            Text(
//                                text = contactPhone,
//                                style = MaterialTheme.typography.bodyLarge,
//                                fontWeight = FontWeight.Medium,
//                                color = Color(0xFF1F2937)
//                            )
//                        }
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(24.dp))
//
//            Text(
//                text = "Please contact the finder to arrange pickup of your item.",
//                style = MaterialTheme.typography.bodyMedium,
//                color = Color.Gray,
//                modifier = Modifier.padding(horizontal = 16.dp)
//            )
//
//            Spacer(modifier = Modifier.height(32.dp))
//
//            // Action Buttons
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                OutlinedButton(
//                    onClick = onBack,
//                    modifier = Modifier.weight(1f),
//                    colors = ButtonDefaults.outlinedButtonColors(
//                        contentColor = PrimaryPurple
//                    )
//                ) {
//                    Text("Back to Item")
//                }
//
//                Button(
//                    onClick = onHome,
//                    modifier = Modifier.weight(1f),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = PrimaryPurple
//                    )
//                ) {
//                    Text("Go to Home")
//                }
//            }
//        }
//    }
//}