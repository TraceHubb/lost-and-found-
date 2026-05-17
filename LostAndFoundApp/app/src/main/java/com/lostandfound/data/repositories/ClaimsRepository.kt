package com.lostandfound.data.repositories

import com.lostandfound.data.firebase.FirebaseProviders
import com.lostandfound.data.models.ClaimResult
import com.lostandfound.data.models.ClaimErrorType
import com.lostandfound.data.models.ContactInfo
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

/**
 * Repository for handling item claim operations with OTP verification.
 */
object ClaimsRepository {
    
    /**
     * Data class for OTP session information.
     */
    data class OtpSession(
        val otpCode: String,
        val showOtpInApp: Boolean = true,
        val emailNote: String? = null
    )
    
    /**
     * Create an OTP session for claiming an item.
     */
    suspend fun createOtpSession(
        userId: String,
        matchId: String,
        lostItemId: String,
        foundItemId: String
    ): ClaimResult<OtpSession> {
        return try {
            // Generate a 6-digit OTP
            val otpCode = generateOtp()
            
            // Store OTP session in Firestore
            val otpData = mapOf(
                "userId" to userId,
                "matchId" to matchId,
                "lostItemId" to lostItemId,
                "foundItemId" to foundItemId,
                "otpCode" to otpCode,
                "createdAt" to System.currentTimeMillis(),
                "expiresAt" to (System.currentTimeMillis() + (5 * 60 * 1000)), // 5 minutes
                "verified" to false
            )
            
            FirebaseProviders.firestore
                .collection("otp_sessions")
                .document(matchId)
                .set(otpData)
                .await()
            
            ClaimResult.Success(
                OtpSession(
                    otpCode = otpCode,
                    showOtpInApp = true,
                    emailNote = "Check your email for the verification code."
                )
            )
        } catch (e: Exception) {
            ClaimResult.Error(
                "Failed to create OTP session: ${e.message}",
                ClaimErrorType.NETWORK_ERROR
            )
        }
    }
    
    /**
     * Verify the OTP code and return contact information.
     */
    suspend fun verifyOtp(
        userId: String,
        matchId: String,
        otpCode: String
    ): ClaimResult<ContactInfo> {
        return try {
            // Get OTP session from Firestore
            val otpDoc = FirebaseProviders.firestore
                .collection("otp_sessions")
                .document(matchId)
                .get()
                .await()
            
            if (!otpDoc.exists()) {
                return ClaimResult.Error(
                    "OTP session not found",
                    ClaimErrorType.INVALID_OTP
                )
            }
            
            val otpData = otpDoc.data!!
            val storedOtp = otpData["otpCode"] as String
            val expiresAt = otpData["expiresAt"] as Long
            val verified = otpData["verified"] as Boolean
            
            // Check if OTP is expired
            if (System.currentTimeMillis() > expiresAt) {
                return ClaimResult.Error(
                    "OTP has expired",
                    ClaimErrorType.EXPIRED_OTP
                )
            }
            
            // Check if OTP is already verified
            if (verified) {
                return ClaimResult.Error(
                    "OTP has already been used",
                    ClaimErrorType.INVALID_OTP
                )
            }
            
            // Verify OTP code
            if (storedOtp != otpCode) {
                return ClaimResult.Error(
                    "Invalid OTP code",
                    ClaimErrorType.INVALID_OTP
                )
            }
            
            // Mark OTP as verified
            FirebaseProviders.firestore
                .collection("otp_sessions")
                .document(matchId)
                .update("verified", true)
                .await()
            
            // Get user information
            val userDoc = FirebaseProviders.firestore
                .collection("users")
                .document(userId)
                .get()
                .await()
            
            val userData = userDoc.data ?: mapOf()
            val userName = userData["name"] as? String ?: "Unknown User"
            val userEmail = userData["email"] as? String ?: AuthRepository.currentUser?.email ?: "No email"
            val userPhone = userData["phone"] as? String
            
            val contactInfo = ContactInfo(
                finderEmail = userEmail,
                finderPhone = userPhone,
                foundItemId = otpData["foundItemId"] as String,
                lostItemId = otpData["lostItemId"] as String
            )
            
            ClaimResult.Success(contactInfo)
        } catch (e: Exception) {
            ClaimResult.Error(
                "Failed to verify OTP: ${e.message}",
                ClaimErrorType.NETWORK_ERROR
            )
        }
    }
    
    /**
     * Resend OTP code.
     */
    suspend fun resendOtp(
        userId: String,
        matchId: String,
        lostItemId: String,
        foundItemId: String
    ): ClaimResult<OtpSession> {
        return try {
            // Delete existing OTP session
            FirebaseProviders.firestore
                .collection("otp_sessions")
                .document(matchId)
                .delete()
                .await()
            
            // Create new OTP session
            createOtpSession(userId, matchId, lostItemId, foundItemId)
        } catch (e: Exception) {
            ClaimResult.Error(
                "Failed to resend OTP: ${e.message}",
                ClaimErrorType.NETWORK_ERROR
            )
        }
    }
    
    /**
     * Generate a 6-digit OTP code.
     */
    private fun generateOtp(): String {
        return Random.nextInt(100000, 999999).toString()
    }
}