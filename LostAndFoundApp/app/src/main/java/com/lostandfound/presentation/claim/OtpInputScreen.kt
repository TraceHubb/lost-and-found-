package com.lostandfound.presentation.claim

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.lostandfound.data.models.ClaimErrorType
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpInputScreen(
    matchId: String,
    lostItemId: String,
    foundItemId: String,
    navController: NavController,
    viewModel: ClaimViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var otpInput by remember { mutableStateOf("") }
    var timeRemaining by remember { mutableStateOf(300) } // 5 minutes in seconds
    
    // Start countdown timer
    LaunchedEffect(uiState) {
        if (uiState is ClaimUiState.OtpSent) {
            val expiresAt = (uiState as ClaimUiState.OtpSent).expiresAt
            while (timeRemaining > 0) {
                val remaining = ((expiresAt - System.currentTimeMillis()) / 1000).toInt()
                if (remaining <= 0) {
                    timeRemaining = 0
                    break
                }
                timeRemaining = remaining
                delay(1000)
            }
        }
    }
    
    // Navigate to contact info on success
    LaunchedEffect(uiState) {
        if (uiState is ClaimUiState.VerificationSuccess) {
            navController.navigate("contact_info/$matchId") {
                popUpTo("otp_input/$matchId/$lostItemId/$foundItemId") { inclusive = true }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verify Your Claim") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Title
            Text(
                text = "Enter Verification Code",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            if (uiState is ClaimUiState.OtpSent) {
                val sent = uiState as ClaimUiState.OtpSent
                Text(
                    text = if (sent.showOtpInApp) {
                        "Use the verification code below (email delivery is not set up yet)"
                    } else {
                        "We've sent a 6-digit verification code to your email"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = maskEmail(sent.email),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                sent.emailNote?.let { note ->
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (sent.showOtpInApp) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Your verification code",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = sent.otpCode,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 4.sp
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "We've sent a 6-digit verification code to your email",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // OTP Input Field
            OutlinedTextField(
                value = otpInput,
                onValueChange = { 
                    if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                        otpInput = it
                    }
                },
                label = { Text("Verification Code") },
                placeholder = { Text("000000") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is ClaimUiState.Loading,
                isError = uiState is ClaimUiState.Error
            )
            
            // Countdown Timer
            if (timeRemaining > 0) {
                val minutes = timeRemaining / 60
                val seconds = timeRemaining % 60
                Text(
                    text = "Code expires in ${String.format("%d:%02d", minutes, seconds)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (timeRemaining < 60) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "Code has expired",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Error Message
            if (uiState is ClaimUiState.Error) {
                val error = uiState as ClaimUiState.Error
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = error.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        
                        // Recovery action based on error type
                        when (error.errorType) {
                            ClaimErrorType.EXPIRED_OTP -> {
                                Text(
                                    text = "Please request a new code using the button below.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            ClaimErrorType.TOO_MANY_ATTEMPTS -> {
                                Text(
                                    text = "Please request a new code to try again.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            ClaimErrorType.RATE_LIMIT_EXCEEDED -> {
                                Text(
                                    text = "Please wait before requesting another code.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            else -> {}
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Verify Button
            Button(
                onClick = { viewModel.verifyOtp(otpInput) },
                modifier = Modifier.fillMaxWidth(),
                enabled = otpInput.length == 6 && uiState !is ClaimUiState.Loading
            ) {
                if (uiState is ClaimUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Verify Code")
                }
            }
            
            // Resend Code Button
            TextButton(
                onClick = { 
                    otpInput = ""
                    viewModel.resendOtp()
                },
                enabled = uiState !is ClaimUiState.Loading
            ) {
                Text("Resend Code")
            }
            
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

/**
 * Mask email address for privacy.
 * Example: john.doe@example.com -> j***e@example.com
 */
private fun maskEmail(email: String): String {
    val parts = email.split("@")
    if (parts.size != 2) return email
    
    val localPart = parts[0]
    val domain = parts[1]
    
    if (localPart.length <= 2) {
        return "${localPart.first()}***@$domain"
    }
    
    val firstChar = localPart.first()
    val lastChar = localPart.last()
    return "$firstChar***$lastChar@$domain"
}
