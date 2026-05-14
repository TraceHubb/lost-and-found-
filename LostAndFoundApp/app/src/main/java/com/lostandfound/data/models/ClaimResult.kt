package com.lostandfound.data.models

/**
 * Represents the result of a claim operation.
 * 
 * This sealed class provides type-safe error handling for all claim-related
 * operations in the repository layer.
 */
sealed class ClaimResult<out T> {
    data class Success<T>(val data: T) : ClaimResult<T>()
    data class Error(val message: String, val errorType: ClaimErrorType) : ClaimResult<Nothing>()
}

/**
 * Categories of errors that can occur during claim operations.
 */
enum class ClaimErrorType {
    /** User is not authenticated */
    UNAUTHORIZED,
    
    /** OTP format is invalid or OTP code is incorrect */
    INVALID_OTP,
    
    /** OTP has expired (past 5 minutes) */
    EXPIRED_OTP,
    
    /** OTP has already been used */
    USED_OTP,
    
    /** Too many verification attempts (max 5) */
    TOO_MANY_ATTEMPTS,
    
    /** Rate limit exceeded for resend requests (max 3 per hour) */
    RATE_LIMIT_EXCEEDED,
    
    /** Network or Firestore connection error */
    NETWORK_ERROR,
    
    /** Unknown or unexpected error */
    UNKNOWN
}

/**
 * Contact information for the finder of an item.
 * 
 * This data is only accessible after successful OTP verification.
 */
data class ContactInfo(
    val finderEmail: String,
    val finderPhone: String?,
    val foundItemId: String,
    val lostItemId: String
)
