//package com.lostandfound.presentation.auth
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.input.PasswordVisualTransformation
//import androidx.compose.ui.unit.dp
//import com.lostandfound.data.repositories.AuthRepository
//import com.lostandfound.presentation.components.CampusFindBrandRow
//import kotlinx.coroutines.launch
//
//@Composable
//fun RegisterScreen(
//    onRegisterSuccess: () -> Unit,
//    onNavigateToLogin: () -> Unit
//) {
//    var email by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//    var confirmPassword by remember { mutableStateOf("") }
//    var errorMessage by remember { mutableStateOf("") }
//    var isLoading by remember { mutableStateOf(false) }
//    val scope = rememberCoroutineScope()
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
//    ) {
//        CampusFindBrandRow(logoSize = 48.dp)
//        Spacer(modifier = Modifier.height(8.dp))
//        Text(
//            text = "Create Account",
//            style = MaterialTheme.typography.titleLarge
//        )
//        Spacer(modifier = Modifier.height(24.dp))
//
//        OutlinedTextField(
//            value = email,
//            onValueChange = { email = it },
//            label = { Text("Email") },
//            modifier = Modifier.fillMaxWidth()
//        )
//        Spacer(modifier = Modifier.height(16.dp))
//
//        OutlinedTextField(
//            value = password,
//            onValueChange = { password = it },
//            label = { Text("Password") },
//            visualTransformation = PasswordVisualTransformation(),
//            modifier = Modifier.fillMaxWidth()
//        )
//        Spacer(modifier = Modifier.height(16.dp))
//
//        OutlinedTextField(
//            value = confirmPassword,
//            onValueChange = { confirmPassword = it },
//            label = { Text("Confirm Password") },
//            visualTransformation = PasswordVisualTransformation(),
//            modifier = Modifier.fillMaxWidth()
//        )
//
//        if (errorMessage.isNotEmpty()) {
//            Spacer(modifier = Modifier.height(8.dp))
//            Text(
//                text = errorMessage,
//                color = MaterialTheme.colorScheme.error
//            )
//        }
//
//        Spacer(modifier = Modifier.height(24.dp))
//
//        Button(
//            onClick = {
//                errorMessage = ""
//                isLoading = true
//                scope.launch {
//                    try {
//                        when {
//                            email.isBlank() || password.isBlank() -> {
//                                errorMessage = "Email and password are required."
//                            }
//                            password.length < 6 -> {
//                                errorMessage = "Password must be at least 6 characters."
//                            }
//                            password != confirmPassword -> {
//                                errorMessage = "Passwords do not match."
//                            }
//                            else -> {
//                                AuthRepository.register(email, password)
//                                onRegisterSuccess()
//                            }
//                        }
//                    } catch (e: Exception) {
//                        errorMessage = e.message ?: "Registration failed."
//                    } finally {
//                        isLoading = false
//                    }
//                }
//            },
//            enabled = !isLoading,
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text(if (isLoading) "Creating account..." else "Register")
//        }
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        TextButton(onClick = onNavigateToLogin) {
//            Text("Already have an account? Login")
//        }
//    }
//}
