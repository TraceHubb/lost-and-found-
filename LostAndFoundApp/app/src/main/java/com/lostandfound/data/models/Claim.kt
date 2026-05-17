package com.lostandfound.data.models

/**
 * Represents a claim attempt on a found item using security questions verification.
 * Replaces the OtpVerificationSession model with a more comprehensive claim tracking system.
 */
data class Claim(
    val id: String = "",
    val claimerId: String = "",
    val itemId: String = "",
    val itemReporterId: String = "",
    val status: ClaimStatus = ClaimStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    // Security questions and answers
    val securityQuestions: List<SecurityQuestion> = emptyList(),
    val claimerAnswers: List<String> = emptyList(),

    // Review process
    val reviewedAt: Long? = null,
    val rejectionReason: String? = null,

    // Contact unlock
    val contactUnlockedAt: Long? = null,

    // Audit and tracking
    val attemptCount: Int = 0,
    val lastAttemptAt: Long? = null
) {
    /**
     * Checks if the claim has expired based on creation time.
     */
    fun isExpired(): Boolean {
        val expirationTime = createdAt + CLAIM_EXPIRY_HOURS * 60 * 60 * 1000
        return System.currentTimeMillis() > expirationTime
    }

    /**
     * Checks if the claim can be reviewed (has answers and is pending).
     */
    fun canBeReviewed(): Boolean {
        return status == ClaimStatus.PENDING &&
               claimerAnswers.isNotEmpty() &&
               claimerAnswers.size == securityQuestions.size
    }

    /**
     * Checks if contact information should be unlocked.
     */
    fun shouldUnlockContact(): Boolean {
        return status == ClaimStatus.APPROVED && contactUnlockedAt == null
    }

    companion object {
        const val CLAIM_EXPIRY_HOURS = 24
        const val MAX_ATTEMPTS = 3
    }
}

/**
 * Represents a security question generated from hidden item details.
 */
data class SecurityQuestion(
    val id: String,
    val question: String,
    val category: String,
    val hiddenDetailKey: String, // References the hidden detail this question tests
    val questionType: QuestionType = QuestionType.TEXT
) {
    /**
     * Creates a display-friendly version of the question with proper formatting.
     */
    fun getFormattedQuestion(): String {
        return when (questionType) {
            QuestionType.TEXT -> question
            QuestionType.MULTIPLE_CHOICE -> "$question (Choose the best answer)"
            QuestionType.NUMERIC -> "$question (Enter a number)"
        }
    }
}

/**
 * Types of security questions that can be generated.
 */
enum class QuestionType {
    TEXT,           // Free text answer
    MULTIPLE_CHOICE, // Select from options (future enhancement)
    NUMERIC         // Numeric answer (amounts, counts, etc.)
}

/**
 * Status of a claim in the verification process.
 */
enum class ClaimStatus {
    PENDING,    // Waiting for item reporter review
    APPROVED,   // Approved by item reporter
    REJECTED,   // Rejected by item reporter
    EXPIRED     // Claim expired without review
}

/**
 * Result of claim operations.
 */
sealed class ClaimResult<out T> {
    data class Success<T>(val data: T) : ClaimResult<T>()
    data class Error(val message: String, val errorType: ClaimErrorType) : ClaimResult<Nothing>()
}

/**
 * Types of errors that can occur during claim processing.
 */
enum class ClaimErrorType {
    UNAUTHORIZED,           // User not authorized to perform action
    ITEM_NOT_FOUND,        // Referenced item doesn't exist
    ITEM_ALREADY_CLAIMED,  // Item has already been claimed
    INVALID_ANSWERS,       // Provided answers are invalid
    CLAIM_EXPIRED,         // Claim has expired
    TOO_MANY_ATTEMPTS,     // Exceeded maximum attempts
    INSUFFICIENT_DETAILS,  // Not enough hidden details for questions
    NETWORK_ERROR,         // Database or network error
    VALIDATION_ERROR,      // Input validation failed
    UNKNOWN,               // Unknown error occurred

    // Legacy OTP error types for backward compatibility
    @Deprecated("Use INVALID_ANSWERS instead")
    INVALID_OTP,           // OTP format is invalid or OTP code is incorrect
    @Deprecated("Use CLAIM_EXPIRED instead")
    EXPIRED_OTP,           // OTP has expired (past 5 minutes)
    @Deprecated("Use TOO_MANY_ATTEMPTS instead")
    USED_OTP,              // OTP has already been used
    @Deprecated("Use TOO_MANY_ATTEMPTS instead")
    RATE_LIMIT_EXCEEDED    // Rate limit exceeded for resend requests
}

/**
 * Contact information unlocked after successful claim approval.
 */
data class ContactInfo(
    val finderEmail: String,
    val finderPhone: String? = null,
    val foundItemId: String,
    val lostItemId: String,
    val unlockedAt: Long = System.currentTimeMillis()
)

/**
 * Session information returned when a claim is initiated.
 */
data class ClaimSession(
    val claimId: String,
    val securityQuestions: List<SecurityQuestion>,
    val expiresAt: Long,
    val attemptsRemaining: Int
)

/**
 * Notification for claim-related events.
 */
data class ClaimNotification(
    val id: String = "",
    val userId: String,
    val type: NotificationType,
    val claimId: String,
    val itemId: String,
    val message: String,
    val read: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Types of notifications for claim events.
 */
enum class NotificationType {
    CLAIM_SUBMITTED,    // Someone submitted a claim on your found item
    CLAIM_APPROVED,     // Your claim was approved
    CLAIM_REJECTED,     // Your claim was rejected
    CLAIM_EXPIRED,      // Your claim expired without review
    CONTACT_UNLOCKED    // Contact information has been unlocked
}

/**
 * Answer validation result for security questions.
 */
data class AnswerValidationResult(
    val isValid: Boolean,
    val score: Double, // Similarity score (0.0 to 1.0)
    val feedback: String? = null
) {
    companion object {
        const val MINIMUM_SCORE = 0.7 // Minimum similarity score for acceptance

        fun valid(score: Double = 1.0): AnswerValidationResult {
            return AnswerValidationResult(true, score)
        }

        fun invalid(score: Double = 0.0, feedback: String? = null): AnswerValidationResult {
            return AnswerValidationResult(false, score, feedback)
        }
    }
}

/**
 * Comprehensive validation result for all answers in a claim.
 */
data class ClaimValidationResult(
    val overallValid: Boolean,
    val individualResults: List<AnswerValidationResult>,
    val averageScore: Double,
    val feedback: List<String> = emptyList()
) {
    companion object {
        fun fromIndividualResults(results: List<AnswerValidationResult>): ClaimValidationResult {
            val averageScore = if (results.isNotEmpty()) {
                results.map { it.score }.average()
            } else 0.0

            val overallValid = results.all { it.isValid } &&
                              averageScore >= AnswerValidationResult.MINIMUM_SCORE

            val feedback = results.mapNotNull { it.feedback }

            return ClaimValidationResult(overallValid, results, averageScore, feedback)
        }
    }
}

/**
 * Legacy OTP session data for backward compatibility.
 * @deprecated Use Claim model instead
 */
@Deprecated("Use Claim model instead")
data class OtpSessionCreated(
    val sessionId: String,
    val otpCode: String,
    val showOtpInApp: Boolean,
    val emailNote: String? = null
)

/**
 * Legacy OTP verification session for backward compatibility.
 * @deprecated Use Claim model instead
 */
@Deprecated("Use Claim model instead")
data class OtpVerificationSession(
    val id: String = "",
    val lostItemId: String = "",
    val foundItemId: String = "",
    val claimerUserId: String = "",
    val otpCode: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = createdAt + (5 * 60 * 1000), // 5 minutes
    val isUsed: Boolean = false,
    val attemptCount: Int = 0,
    val lastAttemptAt: Long? = null
)