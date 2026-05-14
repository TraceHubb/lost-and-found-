package com.lostandfound.data.models

/**
 * Represents an OTP verification session for secure item claiming.
 * 
 * This session is created when a user initiates a claim on a matched item.
 * It contains a time-limited OTP code that must be verified before granting
 * access to finder contact information.
 */
data class OtpVerificationSession(
    val id: String = "",
    val otpCode: String = "",
    val userId: String = "",
    val matchId: String = "",
    val lostItemId: String = "",
    val foundItemId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (5 * 60 * 1000), // 5 minutes
    val isVerified: Boolean = false,
    val verifiedAt: Long? = null,
    val attemptCount: Int = 0,
    val resendCount: Int = 0,
    val lastResendAt: Long? = null
)
