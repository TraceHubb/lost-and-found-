package com.lostandfound.presentation.claim

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lostandfound.data.models.ClaimResult
import com.lostandfound.data.models.ClaimErrorType
import com.lostandfound.data.models.ContactInfo
import com.lostandfound.data.repositories.AuthRepository
import com.lostandfound.data.repositories.ClaimsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state for the claim flow.
 */
sealed class ClaimUiState {
    object Idle : ClaimUiState()
    object Loading : ClaimUiState()
    data class OtpSent(
        val email: String,
        val expiresAt: Long,
        val otpCode: String,
        val showOtpInApp: Boolean,
        val emailNote: String? = null
    ) : ClaimUiState()
    data class VerificationSuccess(val contactInfo: ContactInfo) : ClaimUiState()
    data class Error(val message: String, val errorType: ClaimErrorType) : ClaimUiState()
}

/**
 * ViewModel for managing the secure item claiming flow.
 */
class ClaimViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<ClaimUiState>(ClaimUiState.Idle)
    val uiState: StateFlow<ClaimUiState> = _uiState.asStateFlow()
    
    private val _contactInfo = MutableStateFlow<ContactInfo?>(null)
    val contactInfo: StateFlow<ContactInfo?> = _contactInfo.asStateFlow()
    
    private var currentMatchId: String = ""
    private var currentLostItemId: String = ""
    private var currentFoundItemId: String = ""
    
    /**
     * Initiate the claim process by creating an OTP session.
     */
    fun initiateClaim(matchId: String, lostItemId: String, foundItemId: String) {
        viewModelScope.launch {
            _uiState.value = ClaimUiState.Loading
            
            currentMatchId = matchId
            currentLostItemId = lostItemId
            currentFoundItemId = foundItemId
            
            val userId = AuthRepository.currentUser?.uid ?: run {
                _uiState.value = ClaimUiState.Error(
                    "Please log in to claim items.",
                    ClaimErrorType.UNAUTHORIZED
                )
                return@launch
            }
            
            val result = ClaimsRepository.createOtpSession(userId, matchId, lostItemId, foundItemId)
            
            _uiState.value = when (result) {
                is ClaimResult.Success -> {
                    val userEmail = AuthRepository.currentUser?.email ?: "your email"
                    val expiresAt = System.currentTimeMillis() + (5 * 60 * 1000)
                    ClaimUiState.OtpSent(
                        email = userEmail,
                        expiresAt = expiresAt,
                        otpCode = result.data.otpCode,
                        showOtpInApp = result.data.showOtpInApp,
                        emailNote = result.data.emailNote
                    )
                }
                is ClaimResult.Error -> {
                    ClaimUiState.Error(result.message, result.errorType)
                }
            }
        }
    }
    
    /**
     * Verify the entered OTP code.
     */
    fun verifyOtp(otpCode: String) {
        viewModelScope.launch {
            _uiState.value = ClaimUiState.Loading
            
            val userId = AuthRepository.currentUser?.uid ?: run {
                _uiState.value = ClaimUiState.Error(
                    "Please log in to verify your claim.",
                    ClaimErrorType.UNAUTHORIZED
                )
                return@launch
            }
            
            val result = ClaimsRepository.verifyOtp(userId, currentMatchId, otpCode)
            
            _uiState.value = when (result) {
                is ClaimResult.Success -> {
                    _contactInfo.value = result.data
                    ClaimUiState.VerificationSuccess(result.data)
                }
                is ClaimResult.Error -> {
                    ClaimUiState.Error(result.message, result.errorType)
                }
            }
        }
    }
    
    /**
     * Resend the OTP code.
     */
    fun resendOtp() {
        viewModelScope.launch {
            _uiState.value = ClaimUiState.Loading
            
            val userId = AuthRepository.currentUser?.uid ?: run {
                _uiState.value = ClaimUiState.Error(
                    "Please log in to resend code.",
                    ClaimErrorType.UNAUTHORIZED
                )
                return@launch
            }
            
            val result = ClaimsRepository.resendOtp(userId, currentMatchId, currentLostItemId, currentFoundItemId)
            
            _uiState.value = when (result) {
                is ClaimResult.Success -> {
                    val userEmail = AuthRepository.currentUser?.email ?: "your email"
                    val expiresAt = System.currentTimeMillis() + (5 * 60 * 1000)
                    ClaimUiState.OtpSent(
                        email = userEmail,
                        expiresAt = expiresAt,
                        otpCode = result.data.otpCode,
                        showOtpInApp = result.data.showOtpInApp,
                        emailNote = result.data.emailNote
                    )
                }
                is ClaimResult.Error -> {
                    ClaimUiState.Error(result.message, result.errorType)
                }
            }
        }
    }
    
    /**
     * Reset the state to idle.
     */
    fun resetState() {
        _uiState.value = ClaimUiState.Idle
        currentMatchId = ""
        currentLostItemId = ""
        currentFoundItemId = ""
    }
}
