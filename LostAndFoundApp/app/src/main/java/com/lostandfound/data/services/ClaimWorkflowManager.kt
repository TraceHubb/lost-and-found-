package com.lostandfound.data.services

import com.lostandfound.data.models.*
import com.lostandfound.data.repositories.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Orchestrates the complete claim process from initiation to contact unlock.
 * Manages claim lifecycle, security questions, and approval workflow.
 */
class ClaimWorkflowManager(
    private val questionGenerator: QuestionGenerator,
    private val answerValidator: AnswerValidationService,
    private val notificationService: NotificationService,
    private val claimsRepository: ClaimsRepository
) {
    
    /**
     * Initiates a new claim on a found item.
     * Generates security questions and creates claim session.
     */
    suspend fun initiateClaim(
        claimerId: String,
        itemId: String
    ): ClaimResult<ClaimSession> = withContext(Dispatchers.IO) {
        try {
            // 1. Validate claimer authorization
            if (claimerId != AuthRepository.currentUser?.uid) {
                return@withContext ClaimResult.Error(
                    "Unauthorized to create claim",
                    ClaimErrorType.UNAUTHORIZED
                )
            }
            
            // 2. Retrieve item and validate it exists and is claimable
            val item = claimsRepository.getItemById(itemId)
                ?: return@withContext ClaimResult.Error(
                    "Item not found",
                    ClaimErrorType.ITEM_NOT_FOUND
                )
            
            if (item.status != ItemStatus.ACTIVE) {
                return@withContext ClaimResult.Error(
                    "Item is no longer available for claiming",
                    ClaimErrorType.ITEM_ALREADY_CLAIMED
                )
            }
            
            // 3. Check if user already has an active claim on this item
            val existingClaim = claimsRepository.getActiveClaimByUser(claimerId, itemId)
            if (existingClaim != null) {
                if (existingClaim.isExpired()) {
                    // Mark expired claim and allow new one
                    claimsRepository.updateClaimStatus(existingClaim.id, ClaimStatus.EXPIRED)
                } else {
                    // Return existing active claim
                    return@withContext ClaimResult.Success(
                        ClaimSession(
                            claimId = existingClaim.id,
                            securityQuestions = existingClaim.securityQuestions,
                            expiresAt = existingClaim.createdAt + (Claim.CLAIM_EXPIRY_HOURS * 60 * 60 * 1000),
                            attemptsRemaining = Claim.MAX_ATTEMPTS - existingClaim.attemptCount
                        )
                    )
                }
            }
            
            // 4. Generate security questions from hidden details
            val questionResult = questionGenerator.generateQuestions(
                item.hiddenDetails.category,
                item.hiddenDetails.details
            )
            
            val questions = when (questionResult) {
                is QuestionGenerationResult.Success -> questionResult.questions
                is QuestionGenerationResult.Error -> {
                    return@withContext ClaimResult.Error(
                        "Unable to generate security questions for this item",
                        ClaimErrorType.INSUFFICIENT_DETAILS
                    )
                }
            }
            
            // 5. Create claim record
            val claim = Claim(
                id = generateClaimId(),
                claimerId = claimerId,
                itemId = itemId,
                itemReporterId = item.userId,
                securityQuestions = questions,
                createdAt = System.currentTimeMillis()
            )
            
            claimsRepository.createClaim(claim)
            
            // 6. Create claim session response
            val session = ClaimSession(
                claimId = claim.id,
                securityQuestions = questions,
                expiresAt = claim.createdAt + (Claim.CLAIM_EXPIRY_HOURS * 60 * 60 * 1000),
                attemptsRemaining = Claim.MAX_ATTEMPTS
            )
            
            ClaimResult.Success(session)
            
        } catch (e: Exception) {
            ClaimResult.Error(
                "Failed to initiate claim: ${e.message}",
                ClaimErrorType.NETWORK_ERROR
            )
        }
    }
    
    /**
     * Submits answers to security questions for a claim.
     */
    suspend fun submitAnswers(
        claimId: String,
        answers: List<String>
    ): ClaimResult<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Retrieve and validate claim
            val claim = claimsRepository.getClaimById(claimId)
                ?: return@withContext ClaimResult.Error(
                    "Claim not found",
                    ClaimErrorType.ITEM_NOT_FOUND
                )
            
            if (claim.status != ClaimStatus.PENDING) {
                return@withContext ClaimResult.Error(
                    "Claim is no longer pending",
                    ClaimErrorType.CLAIM_EXPIRED
                )
            }
            
            if (claim.isExpired()) {
                claimsRepository.updateClaimStatus(claimId, ClaimStatus.EXPIRED)
                return@withContext ClaimResult.Error(
                    "Claim has expired",
                    ClaimErrorType.CLAIM_EXPIRED
                )
            }
            
            if (claim.attemptCount >= Claim.MAX_ATTEMPTS) {
                return@withContext ClaimResult.Error(
                    "Maximum attempts exceeded",
                    ClaimErrorType.TOO_MANY_ATTEMPTS
                )
            }
            
            // 2. Validate answers count
            if (answers.size != claim.securityQuestions.size) {
                return@withContext ClaimResult.Error(
                    "Number of answers doesn't match number of questions",
                    ClaimErrorType.INVALID_ANSWERS
                )
            }
            
            // 3. Store answers and increment attempt count
            claimsRepository.updateClaimAnswers(
                claimId,
                answers,
                claim.attemptCount + 1
            )
            
            // 4. Notify item reporter of pending claim
            notificationService.sendClaimSubmittedNotification(
                claim.itemReporterId,
                claimId,
                claim.itemId
            )
            
            ClaimResult.Success(Unit)
            
        } catch (e: Exception) {
            ClaimResult.Error(
                "Failed to submit answers: ${e.message}",
                ClaimErrorType.NETWORK_ERROR
            )
        }
    }
    
    /**
     * Reviews a claim and approves or rejects it.
     */
    suspend fun reviewClaim(
        claimId: String,
        reporterId: String,
        approved: Boolean,
        rejectionReason: String? = null
    ): ClaimResult<ContactInfo?> = withContext(Dispatchers.IO) {
        try {
            // 1. Validate reporter authorization
            val claim = claimsRepository.getClaimById(claimId)
                ?: return@withContext ClaimResult.Error(
                    "Claim not found",
                    ClaimErrorType.ITEM_NOT_FOUND
                )
            
            if (claim.itemReporterId != reporterId) {
                return@withContext ClaimResult.Error(
                    "Unauthorized to review this claim",
                    ClaimErrorType.UNAUTHORIZED
                )
            }
            
            if (claim.status != ClaimStatus.PENDING) {
                return@withContext ClaimResult.Error(
                    "Claim is not pending review",
                    ClaimErrorType.VALIDATION_ERROR
                )
            }
            
            // 2. Update claim status
            val newStatus = if (approved) ClaimStatus.APPROVED else ClaimStatus.REJECTED
            claimsRepository.updateClaimReview(
                claimId,
                newStatus,
                rejectionReason,
                System.currentTimeMillis()
            )
            
            // 3. Handle approval - unlock contact info and mark items as claimed
            val contactInfo = if (approved) {
                val item = claimsRepository.getItemById(claim.itemId)
                if (item != null) {
                    // Mark items as claimed
                    claimsRepository.markItemsAsClaimed(
                        claim.itemId,
                        claim.claimerId,
                        System.currentTimeMillis()
                    )
                    
                    // Create contact info
                    val contact = ContactInfo(
                        finderEmail = item.publicDetails.contactEmail,
                        finderPhone = item.publicDetails.contactPhone,
                        foundItemId = claim.itemId,
                        lostItemId = claim.itemId // In this system, same item
                    )
                    
                    // Update claim with contact unlock timestamp
                    claimsRepository.updateContactUnlocked(claimId, System.currentTimeMillis())
                    
                    contact
                } else null
            } else null
            
            // 4. Notify claimer of decision
            val notificationType = if (approved) NotificationType.CLAIM_APPROVED else NotificationType.CLAIM_REJECTED
            notificationService.sendClaimDecisionNotification(
                claim.claimerId,
                claimId,
                claim.itemId,
                notificationType,
                rejectionReason
            )
            
            ClaimResult.Success(contactInfo)
            
        } catch (e: Exception) {
            ClaimResult.Error(
                "Failed to review claim: ${e.message}",
                ClaimErrorType.NETWORK_ERROR
            )
        }
    }
    
    /**
     * Gets claim details for review by item reporter.
     */
    suspend fun getClaimForReview(
        claimId: String,
        reporterId: String
    ): ClaimResult<ClaimReviewData> = withContext(Dispatchers.IO) {
        try {
            val claim = claimsRepository.getClaimById(claimId)
                ?: return@withContext ClaimResult.Error(
                    "Claim not found",
                    ClaimErrorType.ITEM_NOT_FOUND
                )
            
            if (claim.itemReporterId != reporterId) {
                return@withContext ClaimResult.Error(
                    "Unauthorized to view this claim",
                    ClaimErrorType.UNAUTHORIZED
                )
            }
            
            val item = claimsRepository.getItemById(claim.itemId)
                ?: return@withContext ClaimResult.Error(
                    "Item not found",
                    ClaimErrorType.ITEM_NOT_FOUND
                )
            
            val reviewData = ClaimReviewData(
                claim = claim,
                hiddenDetails = item.hiddenDetails.details,
                publicDetails = item.publicDetails
            )
            
            ClaimResult.Success(reviewData)
            
        } catch (e: Exception) {
            ClaimResult.Error(
                "Failed to get claim for review: ${e.message}",
                ClaimErrorType.NETWORK_ERROR
            )
        }
    }
    
    /**
     * Gets claims submitted by a user.
     */
    suspend fun getUserClaims(userId: String): ClaimResult<List<Claim>> = withContext(Dispatchers.IO) {
        try {
            val claims = claimsRepository.getClaimsByUser(userId)
            ClaimResult.Success(claims)
        } catch (e: Exception) {
            ClaimResult.Error(
                "Failed to get user claims: ${e.message}",
                ClaimErrorType.NETWORK_ERROR
            )
        }
    }
    
    /**
     * Gets pending claims for items reported by a user.
     */
    suspend fun getPendingClaimsForReporter(reporterId: String): ClaimResult<List<Claim>> = withContext(Dispatchers.IO) {
        try {
            val claims = claimsRepository.getPendingClaimsByReporter(reporterId)
            ClaimResult.Success(claims)
        } catch (e: Exception) {
            ClaimResult.Error(
                "Failed to get pending claims: ${e.message}",
                ClaimErrorType.NETWORK_ERROR
            )
        }
    }
    
    /**
     * Cleans up expired claims.
     */
    suspend fun cleanupExpiredClaims(): ClaimResult<Int> = withContext(Dispatchers.IO) {
        try {
            val expiredCount = claimsRepository.markExpiredClaims()
            ClaimResult.Success(expiredCount)
        } catch (e: Exception) {
            ClaimResult.Error(
                "Failed to cleanup expired claims: ${e.message}",
                ClaimErrorType.NETWORK_ERROR
            )
        }
    }
    
    private fun generateClaimId(): String {
        return "claim_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}

/**
 * Data structure for claim review containing all necessary information.
 */
data class ClaimReviewData(
    val claim: Claim,
    val hiddenDetails: Map<String, String>,
    val publicDetails: PublicItemDetails
)

/**
 * Interface for notification service.
 */
interface NotificationService {
    suspend fun sendClaimSubmittedNotification(
        reporterId: String,
        claimId: String,
        itemId: String
    )
    
    suspend fun sendClaimDecisionNotification(
        claimerId: String,
        claimId: String,
        itemId: String,
        decision: NotificationType,
        rejectionReason: String? = null
    )
}

/**
 * Interface for claims repository operations.
 */
interface ClaimsRepository {
    suspend fun getItemById(itemId: String): EnhancedItem?
    suspend fun createClaim(claim: Claim)
    suspend fun getClaimById(claimId: String): Claim?
    suspend fun getActiveClaimByUser(userId: String, itemId: String): Claim?
    suspend fun updateClaimStatus(claimId: String, status: ClaimStatus)
    suspend fun updateClaimAnswers(claimId: String, answers: List<String>, attemptCount: Int)
    suspend fun updateClaimReview(claimId: String, status: ClaimStatus, rejectionReason: String?, reviewedAt: Long)
    suspend fun updateContactUnlocked(claimId: String, unlockedAt: Long)
    suspend fun markItemsAsClaimed(itemId: String, claimedBy: String, claimedAt: Long)
    suspend fun getClaimsByUser(userId: String): List<Claim>
    suspend fun getPendingClaimsByReporter(reporterId: String): List<Claim>
    suspend fun markExpiredClaims(): Int
}