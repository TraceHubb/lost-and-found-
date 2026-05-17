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
//import kotlinx.coroutines.launch
//
//@Composable
//fun LoginScreen(
//    onLoginSuccess: () -> Unit,
//    onNavigateToRegister: () -> Unit
//) {
//    var email by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
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
//        Text(
//            text = "Lost and Found",
//            style = MaterialTheme.typography.headlineLarge
//        )
//        Spacer(modifier = Modifier.height(32.dp))
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
//                        if (email.isBlank() || password.isBlank()) {
//                            errorMessage = "Email and password are required."
//                        } else {
//                            AuthRepository.login(email, password)
//                            onLoginSuccess()
//                        }
//                    } catch (e: Exception) {
//                        errorMessage = e.message ?: "Login failed."
//                    } finally {
//                        isLoading = false
//                    }
//                }
//            },
//            enabled = !isLoading,
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text(if (isLoading) "Logging in..." else "Login")
//        }
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        TextButton(onClick = onNavigateToRegister) {
//            Text("Don't have an account? Register")
//        }
//
//        TextButton(onClick = { /* TODO: Forgot password */ }) {
//            Text("Forgot Password?")
//        }
//    }
//}
