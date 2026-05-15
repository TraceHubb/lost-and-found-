package com.lostandfound.data.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.lostandfound.data.firebase.FirebaseProviders
import com.lostandfound.data.models.ClaimResult
import com.lostandfound.data.models.ClaimErrorType
import com.lostandfound.data.models.ContactInfo
import com.lostandfound.data.models.OtpSessionCreated
import com.lostandfound.data.models.OtpVerificationSession
import com.lostandfound.data.services.EmailService
import kotlinx.coroutines.tasks.await
import java.security.SecureRandom

/**
 * Repository for managing secure item claiming with OTP verification.
 */
object ClaimsRepository {
    private const val OTP_SESSIONS_COLLECTION = "otpVerificationSessions"
    private const val AUDIT_LOGS_COLLECTION = "auditLogs"
    private const val ITEMS_COLLECTION = "items"
    private const val OTP_EXPIRY_MINUTES = 5
    private const val MAX_VERIFICATION_ATTEMPTS = 5
    private const val MAX_RESEND_PER_HOUR = 3

    private val firestore: FirebaseFirestore
        get() = FirebaseProviders.firestore

    private fun generateOtp(): String {
        val secureRandom = SecureRandom()
        val otpValue = secureRandom.nextInt(1000000)
        return otpValue.toString().padStart(6, '0')
    }

    /**
     * Checks no other *active* session uses this OTP.
     * Uses a single-field query (otpCode) to avoid requiring a composite Firestore index.
     */
    private suspend fun isOtpUnique(otpCode: String): Boolean {
        return try {
            val currentTime = System.currentTimeMillis()
            val snapshot = firestore.collection(OTP_SESSIONS_COLLECTION)
                .whereEqualTo("otpCode", otpCode)
                .limit(10)
                .get()
                .await()
            snapshot.documents.none { doc ->
                val expiresAt = doc.getLong("expiresAt") ?: 0L
                expiresAt > currentTime
            }
        } catch (e: Exception) {
            android.util.Log.e("ClaimsRepository", "isOtpUnique failed; allowing OTP", e)
            // If Firestore fails, don't spin generating 10 codes — treat as unique.
            true
        }
    }

    suspend fun isAuthorizedToClaim(userId: String, lostItemId: String): Boolean {
        return try {
            val itemDoc = firestore.collection(ITEMS_COLLECTION)
                .document(lostItemId)
                .get()
                .await()
            val reporterId = itemDoc.getString("userId") ?: ""
            reporterId == userId
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun sendOtpEmail(email: String, otpCode: String): EmailService.EmailResult {
        val userName = AuthRepository.currentUser?.displayName ?: "User"
        return EmailService.sendOtpEmail(
            recipientEmail = email,
            otpCode = otpCode,
            recipientName = userName
        )
    }

    private fun buildOtpSessionResponse(
        sessionId: String,
        otpCode: String,
        emailResult: EmailService.EmailResult
    ): OtpSessionCreated {
        return when (emailResult) {
            is EmailService.EmailResult.Success -> {
                val queued = emailResult.delivery == EmailService.DeliveryType.QUEUED_FIRESTORE
                OtpSessionCreated(
                    sessionId = sessionId,
                    otpCode = otpCode,
                    showOtpInApp = queued,
                    emailNote = if (queued) {
                        "Email is queued. Install Firebase Trigger Email extension, or use the code below."
                    } else {
                        null
                    }
                )
            }
            is EmailService.EmailResult.Failure -> {
                android.util.Log.e("ClaimsRepository", "OTP email failed: ${emailResult.debugMessage}")
                OtpSessionCreated(
                    sessionId = sessionId,
                    otpCode = otpCode,
                    showOtpInApp = true,
                    emailNote = "Email could not be sent. Use the verification code below."
                )
            }
        }
    }

    private fun sessionToMap(session: OtpVerificationSession): Map<String, Any?> = mapOf(
        "id" to session.id,
        "otpCode" to session.otpCode,
        "userId" to session.userId,
        "matchId" to session.matchId,
        "lostItemId" to session.lostItemId,
        "foundItemId" to session.foundItemId,
        "createdAt" to session.createdAt,
        "expiresAt" to session.expiresAt,
        "isVerified" to session.isVerified,
        "verifiedAt" to session.verifiedAt,
        "attemptCount" to session.attemptCount,
        "resendCount" to session.resendCount,
        "lastResendAt" to session.lastResendAt
    )

    private fun parseOtpSession(doc: com.google.firebase.firestore.DocumentSnapshot): OtpVerificationSession? {
        if (!doc.exists()) return null
        return OtpVerificationSession(
            id = doc.id,
            otpCode = doc.getString("otpCode") ?: "",
            userId = doc.getString("userId") ?: "",
            matchId = doc.getString("matchId") ?: "",
            lostItemId = doc.getString("lostItemId") ?: "",
            foundItemId = doc.getString("foundItemId") ?: "",
            createdAt = doc.getLong("createdAt") ?: 0L,
            expiresAt = doc.getLong("expiresAt") ?: 0L,
            isVerified = doc.getBoolean("isVerified")
                ?: doc.getBoolean("verified")
                ?: false,
            verifiedAt = doc.getLong("verifiedAt"),
            attemptCount = doc.getLong("attemptCount")?.toInt() ?: 0,
            resendCount = doc.getLong("resendCount")?.toInt() ?: 0,
            lastResendAt = doc.getLong("lastResendAt")
        )
    }

    private suspend fun logAuditEvent(
        eventType: String,
        userId: String,
        matchId: String,
        details: Map<String, Any>
    ) {
        try {
            val auditLog = hashMapOf(
                "eventType" to eventType,
                "userId" to userId,
                "matchId" to matchId,
                "timestamp" to System.currentTimeMillis(),
                "details" to details
            )
            firestore.collection(AUDIT_LOGS_COLLECTION).add(auditLog).await()
        } catch (e: Exception) {
            android.util.Log.e("ClaimsRepository", "Failed to log audit event", e)
        }
    }

    suspend fun createOtpSession(
        userId: String,
        matchId: String,
        lostItemId: String,
        foundItemId: String
    ): ClaimResult<OtpSessionCreated> {
        return try {
            if (!isAuthorizedToClaim(userId, lostItemId)) {
                logAuditEvent(
                    "OTP_SESSION_UNAUTHORIZED",
                    userId,
                    matchId,
                    mapOf("lostItemId" to lostItemId, "foundItemId" to foundItemId)
                )
                return ClaimResult.Error(
                    "Only the lost reporter can claim this item.",
                    ClaimErrorType.UNAUTHORIZED
                )
            }

            var otpCode: String
            var attempts = 0
            do {
                otpCode = generateOtp()
                attempts++
                if (attempts > 10) {
                    return ClaimResult.Error(
                        "Failed to generate unique code. Please try again.",
                        ClaimErrorType.UNKNOWN
                    )
                }
            } while (!isOtpUnique(otpCode))

            val userEmail = AuthRepository.currentUser?.email ?: return ClaimResult.Error(
                "User email not found. Please ensure you're logged in.",
                ClaimErrorType.UNAUTHORIZED
            )

            val currentTime = System.currentTimeMillis()
            val session = OtpVerificationSession(
                id = firestore.collection(OTP_SESSIONS_COLLECTION).document().id,
                otpCode = otpCode,
                userId = userId,
                matchId = matchId,
                lostItemId = lostItemId,
                foundItemId = foundItemId,
                createdAt = currentTime,
                expiresAt = currentTime + (OTP_EXPIRY_MINUTES * 60 * 1000),
                isVerified = false,
                verifiedAt = null,
                attemptCount = 0,
                resendCount = 0,
                lastResendAt = null
            )

            firestore.collection(OTP_SESSIONS_COLLECTION)
                .document(session.id)
                .set(sessionToMap(session))
                .await()

            val emailResult = sendOtpEmail(userEmail, otpCode)
            val response = buildOtpSessionResponse(session.id, otpCode, emailResult)

            logAuditEvent(
                "OTP_SESSION_CREATED",
                userId,
                matchId,
                mapOf(
                    "sessionId" to session.id,
                    "lostItemId" to lostItemId,
                    "foundItemId" to foundItemId,
                    "emailDelivery" to emailResult.toString()
                )
            )
            ClaimResult.Success(response)
        } catch (e: Exception) {
            ClaimResult.Error(
                "Failed to create claim session. Please try again.",
                ClaimErrorType.NETWORK_ERROR
            )
        }
    }

    private suspend fun getActiveSession(userId: String, matchId: String): OtpVerificationSession? {
        return try {
            val now = System.currentTimeMillis()
            val snapshot = firestore.collection(OTP_SESSIONS_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            snapshot.documents
                .mapNotNull { doc -> parseOtpSession(doc) }
                .filter { session ->
                    session.matchId == matchId &&
                        !session.isVerified &&
                        session.expiresAt > now
                }
                .maxByOrNull { it.createdAt }
        } catch (e: Exception) {
            android.util.Log.e("ClaimsRepository", "getActiveSession failed", e)
            null
        }
    }

    private suspend fun incrementAttemptCount(sessionId: String) {
        try {
            firestore.collection(OTP_SESSIONS_COLLECTION).document(sessionId)
                .update("attemptCount", com.google.firebase.firestore.FieldValue.increment(1))
                .await()
        } catch (e: Exception) {
            android.util.Log.e("ClaimsRepository", "Failed to increment attempt count", e)
        }
    }

    private suspend fun markSessionAsVerified(sessionId: String) {
        try {
            firestore.collection(OTP_SESSIONS_COLLECTION).document(sessionId)
                .update(mapOf("isVerified" to true, "verifiedAt" to System.currentTimeMillis()))
                .await()
        } catch (e: Exception) {
            android.util.Log.e("ClaimsRepository", "Failed to mark session as verified", e)
        }
    }

    private fun isValidOtpFormat(otpCode: String): Boolean {
        return otpCode.length == 6 && otpCode.all { it.isDigit() }
    }

    private fun normalizeOtpInput(input: String): String {
        val trimmed = input.trim()
        return if (trimmed.all { it.isDigit() }) {
            trimmed.padStart(6, '0').take(6)
        } else {
            trimmed
        }
    }

    private suspend fun retrieveContactInfo(foundItemId: String, lostItemId: String): ContactInfo? {
        return try {
            val itemDoc = firestore.collection(ITEMS_COLLECTION).document(foundItemId).get().await()
            ContactInfo(
                finderEmail = itemDoc.getString("contactEmail") ?: "",
                finderPhone = itemDoc.getString("contactPhone"),
                foundItemId = foundItemId,
                lostItemId = lostItemId
            )
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun markItemsAsClaimed(
        lostItemId: String,
        foundItemId: String,
        claimedBy: String
    ) {
        try {
            val currentTime = System.currentTimeMillis()
            val batch = firestore.batch()
            batch.update(
                firestore.collection(ITEMS_COLLECTION).document(lostItemId), mapOf(
                    "status" to "CLAIMED", "claimedBy" to claimedBy, "claimedAt" to currentTime
                )
            )
            batch.update(
                firestore.collection(ITEMS_COLLECTION).document(foundItemId), mapOf(
                    "status" to "CLAIMED", "claimedBy" to claimedBy, "claimedAt" to currentTime
                )
            )
            batch.commit().await()
        } catch (e: Exception) {
            android.util.Log.e("ClaimsRepository", "Failed to mark items as claimed", e)
        }
    }

    suspend fun verifyOtp(
        userId: String,
        matchId: String,
        otpCode: String
    ): ClaimResult<ContactInfo> {
        return try {
            val normalizedOtp = normalizeOtpInput(otpCode)
            if (!isValidOtpFormat(normalizedOtp)) {
                return ClaimResult.Error(
                    "Invalid OTP format. Expected 6 digits.",
                    ClaimErrorType.INVALID_OTP
                )
            }

            val session = getActiveSession(userId, matchId)
                ?: return ClaimResult.Error(
                    "No active session found. Please request a new code.",
                    ClaimErrorType.EXPIRED_OTP
                )

            if (session.isVerified) {
                val contactInfo = retrieveContactInfo(session.foundItemId, session.lostItemId)
                    ?: return ClaimResult.Error(
                        "Failed to retrieve contact information.",
                        ClaimErrorType.UNKNOWN
                    )
                return ClaimResult.Success(contactInfo)
            }

            if (System.currentTimeMillis() > session.expiresAt) {
                logAuditEvent(
                    "OTP_VERIFICATION_EXPIRED",
                    userId,
                    matchId,
                    mapOf("sessionId" to session.id)
                )
                return ClaimResult.Error(
                    "Code has expired. Please request a new code.",
                    ClaimErrorType.EXPIRED_OTP
                )
            }

            if (session.attemptCount >= MAX_VERIFICATION_ATTEMPTS) {
                logAuditEvent(
                    "OTP_VERIFICATION_TOO_MANY_ATTEMPTS",
                    userId,
                    matchId,
                    mapOf("sessionId" to session.id)
                )
                return ClaimResult.Error(
                    "Too many failed attempts. Please request a new code.",
                    ClaimErrorType.TOO_MANY_ATTEMPTS
                )
            }

            if (session.otpCode != normalizedOtp) {
                incrementAttemptCount(session.id)
                logAuditEvent(
                    "OTP_VERIFICATION_INCORRECT",
                    userId,
                    matchId,
                    mapOf("sessionId" to session.id)
                )
                return ClaimResult.Error("Incorrect verification code.", ClaimErrorType.INVALID_OTP)
            }

            markSessionAsVerified(session.id)
            val contactInfo = retrieveContactInfo(session.foundItemId, session.lostItemId)
                ?: return ClaimResult.Error(
                    "Failed to retrieve contact information.",
                    ClaimErrorType.UNKNOWN
                )

            markItemsAsClaimed(session.lostItemId, session.foundItemId, userId)
            
            // Send success notification email
            try {
                val userEmail = AuthRepository.currentUser?.email
                val itemDoc = firestore.collection(ITEMS_COLLECTION).document(session.lostItemId).get().await()
                val itemName = itemDoc.getString("itemName") ?: "your item"
                
                if (userEmail != null) {
                    com.lostandfound.data.services.EmailService.sendClaimSuccessEmail(
                        recipientEmail = userEmail,
                        itemName = itemName,
                        finderContact = contactInfo.finderEmail
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("ClaimsRepository", "Failed to send success email", e)
                // Don't fail the claim if email fails
            }
            
            logAuditEvent(
                "OTP_VERIFICATION_SUCCESS",
                userId,
                matchId,
                mapOf("sessionId" to session.id)
            )

            ClaimResult.Success(contactInfo)
        } catch (e: Exception) {
            ClaimResult.Error(
                "An unexpected error occurred. Please try again.",
                ClaimErrorType.NETWORK_ERROR
            )
        }
    }

    suspend fun resendOtp(
        userId: String,
        matchId: String,
        lostItemId: String,
        foundItemId: String
    ): ClaimResult<OtpSessionCreated> {
        return try {
            val oneHourAgo = System.currentTimeMillis() - (60 * 60 * 1000)
            val userSessions = firestore.collection(OTP_SESSIONS_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val recentResendCount = userSessions.documents.count { doc ->
                doc.getString("matchId") == matchId &&
                    (doc.getLong("createdAt") ?: 0L) > oneHourAgo
            }

            if (recentResendCount >= MAX_RESEND_PER_HOUR) {
                return ClaimResult.Error(
                    "Too many requests. Please try again later.",
                    ClaimErrorType.RATE_LIMIT_EXCEEDED
                )
            }

            val oldSession = getActiveSession(userId, matchId)
            oldSession?.let {
                firestore.collection(OTP_SESSIONS_COLLECTION).document(it.id).delete().await()
            }

            createOtpSession(userId, matchId, lostItemId, foundItemId)
        } catch (e: Exception) {
            ClaimResult.Error(
                "Failed to resend code. Please try again.",
                ClaimErrorType.NETWORK_ERROR
            )
        }
    }
}
