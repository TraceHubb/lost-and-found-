package com.lostandfound.data.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.lostandfound.data.firebase.FirebaseProviders
import com.lostandfound.data.models.*
import com.lostandfound.data.services.ClaimsRepository
import kotlinx.coroutines.tasks.await

/**
 * Repository implementation for managing claims with security questions verification.
 * Replaces the OTP-based ClaimsRepository with security questions workflow.
 */
object SecurityQuestionsClaimsRepository : ClaimsRepository {
    private const val CLAIMS_COLLECTION = "claims"
    private const val ITEMS_COLLECTION = "items"
    private const val NOTIFICATIONS_COLLECTION = "notifications"
    private const val AUDIT_LOGS_COLLECTION = "auditLogs"

    private val firestore: FirebaseFirestore
        get() = FirebaseProviders.firestore

    override suspend fun getItemById(itemId: String): EnhancedItem? {
        return EnhancedItemsRepository.getEnhancedItemById(itemId)
    }

    override suspend fun createClaim(claim: Claim) {
        try {
            val claimMap = claimToMap(claim)
            firestore.collection(CLAIMS_COLLECTION)
                .document(claim.id)
                .set(claimMap)
                .await()
                
            // Log audit event
            logAuditEvent(
                "CLAIM_CREATED",
                claim.claimerId,
                claim.id,
                mapOf(
                    "itemId" to claim.itemId,
                    "questionCount" to claim.securityQuestions.size
                )
            )
        } catch (e: Exception) {
            android.util.Log.e("ClaimsRepository", "Failed to create claim", e)
            throw e
        }
    }

    override suspend fun getClaimById(claimId: String): Claim? {
        return try {
            val doc = firestore.collection(CLAIMS_COLLECTION)
                .document(claimId)
                .get()
                .await()
            
            if (!doc.exists()) return null
            parseClaimFromDocument(doc)
        } catch (e: Exception) {
            android.util.Log.e("ClaimsRepository", "Failed to get claim by ID", e)
            null
        }
    }

    override suspend fun getActiveClaimByUser(userId: String, itemId: String): Claim? {
        return try {
            val snapshot = firestore.collection(CLAIMS_COLLECTION)
                .whereEqualTo("claimerId", userId)
                .whereEqualTo("itemId", itemId)
                .whereEqualTo("status", ClaimStatus.PENDING.name)
                .limit(1)
                .get()
                .await()
            
            snapshot.documents.firstOrNull()?.let { doc ->
                parseClaimFromDocument(doc)
            }
        } catch (e: Exception) {
            android.util.Log.e("ClaimsRepository", "Failed to get active claim", e)
            null
        }
    }

    override suspend fun updateClaimStatus(claimId: String, status: ClaimStatus) {
        try {
            firestore.collection(CLAIMS_COLLECTION)
                .document(claimId)
                .update(
                    mapOf(
                        "status" to status.name,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
                
            // Log audit event
            logAuditEvent(
                "CLAIM_STATUS_UPDATED",
                "", // Will be filled by caller
                claimId,
                mapOf("newStatus" to status.name)
            )
        } catch (e: Exception) {
            android.util.Log.e("ClaimsRepository", "Failed to update claim status", e)
            throw e
        }
    }

    override suspend fun updateClaimAnswers(claimId: String, answers: List<String>, attemptCount: Int) {
        try {
            firestore.collection(CLAIMS_COLLECTION)
                .document(claimId)
                .update(
                    mapOf(
                        "claimerAnswers" to answers,
                        "attemptCount" to attemptCount,
                        "lastAttemptAt" to System.currentTimeMillis(),
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
                
            // Log audit event
            logAuditEvent(
                "CLAIM_ANSWERS_SUBMITTED",
                "", // Will be filled by caller
                claimId,
                mapOf(
                    "answerCount" to answers.size,
                    "attemptNumber" to attemptCount
                )
            )
        } catch (e: Exception) {
            android.util.Log.e("ClaimsRepository", "Failed to update claim answers", e)
            throw e
        }
    }

    override suspend fun updateClaimReview(
        claimId: String,
        status: ClaimStatus,
        rejectionReason: String?,
        reviewedAt: Long
    ) {
        try {
            val updates = mutableMapOf<String, Any?>(
                "status" to status.name,
                "reviewedAt" to reviewedAt,
                "updatedAt" to System.currentTimeMillis()
            )
            
            if (rejectionReason != null) {
                updates["rejectionReason"] = rejectionReason
            }
            
            firestore.collection(CLAIMS_COLLECTION)
                .document(claimId)
                .update(updates)
                .await()
                
            // Log audit event
            logAuditEvent(
                "CLAIM_REVIEWED",
                "", // Will be filled by caller
                claimId,
                mapOf(
                    "decision" to status.name,
                    "hasRejectionReason" to (rejectionReason != null)
                )
            )
        } catch (e: Exception) {
            android.util.Log.e("ClaimsRepository", "Failed to update claim review", e)
            throw e
        }
    }

    override suspend fun updateContactUnlocked(claimId: String, unlockedAt: Long) {
        try {
            firestore.collection(CLAIMS_COLLECTION)
                .document(claimId)
                .update(
                    mapOf(
                        "contactUnlockedAt" to unlockedAt,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
                
            // Log audit event
            logAuditEvent(
                "CONTACT_UNLOCKED",
                "", // Will be filled by caller
                claimId,
                mapOf("unlockedAt" to unlockedAt)
            )
        } catch (e: Exception) {
            android.util.Log.e("ClaimsRepository", "Failed to update contact unlocked", e)
            throw e
        }
    }

    override suspend fun markItemsAsClaimed(itemId: String, claimedBy: String, claimedAt: Long) {
        try {
            EnhancedItemsRepository.updateItemStatus(
                itemId = itemId,
                status = ItemStatus.CLAIMED,
                claimedBy = claimedBy,
                claimedAt = claimedAt
            )
            
            // Log audit event
            logAuditEvent(
                "ITEM_CLAIMED",
                claimedBy,
                "", // No claim ID in this context
                mapOf(
                    "itemId" to itemId,
                    "claimedAt" to claimedAt
                )
            )
        } catch (e: Exception) {
            android.util.Log.e("ClaimsRepository", "Failed to mark items as claimed", e)
            throw e
        }
    }

    override suspend fun getClaimsByUser(userId: String): List<Claim> {
        return try {
            val snapshot = firestore.collection(CLAIMS_COLLECTION)
                .whereEqualTo("claimerId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                parseClaimFromDocument(doc)
            }
        } catch (e: Exception) {
            android.util.Log.e("ClaimsRepository", "Failed to get claims by user", e)
            emptyList()
        }
    }

    override suspend fun getPendingClaimsByReporter(reporterId: String): List<Claim> {
        return try {
            val snapshot = firestore.collection(CLAIMS_COLLECTION)
                .whereEqualTo("itemReporterId", reporterId)
                .whereEqualTo("status", ClaimStatus.PENDING.name)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                parseClaimFromDocument(doc)
            }
        } catch (e: Exception) {
            android.util.Log.e("ClaimsRepository", "Failed to get pending claims", e)
            emptyList()
        }
    }

    override suspend fun markExpiredClaims(): Int {
        return try {
            val currentTime = System.currentTimeMillis()
            val expirationTime = currentTime - (Claim.CLAIM_EXPIRY_HOURS * 60 * 60 * 1000)
            
            val snapshot = firestore.collection(CLAIMS_COLLECTION)
                .whereEqualTo("status", ClaimStatus.PENDING.name)
                .whereLessThan("createdAt", expirationTime)
                .get()
                .await()
            
            val batch = firestore.batch()
            var expiredCount = 0
            
            for (doc in snapshot.documents) {
                batch.update(
                    doc.reference,
                    mapOf(
                        "status" to ClaimStatus.EXPIRED.name,
                        "updatedAt" to currentTime
                    )
                )
                expiredCount++
            }
            
            if (expiredCount > 0) {
                batch.commit().await()
                
                // Log audit event
                logAuditEvent(
                    "CLAIMS_EXPIRED_BATCH",
                    "system",
                    "",
                    mapOf("expiredCount" to expiredCount)
                )
            }
            
            expiredCount
        } catch (e: Exception) {
            android.util.Log.e("ClaimsRepository", "Failed to mark expired claims", e)
            0
        }
    }

    /**
     * Gets claims with answers ready for review.
     */
    suspend fun getClaimsReadyForReview(reporterId: String): List<Claim> {
        return try {
            val snapshot = firestore.collection(CLAIMS_COLLECTION)
                .whereEqualTo("itemReporterId", reporterId)
                .whereEqualTo("status", ClaimStatus.PENDING.name)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                parseClaimFromDocument(doc)
            }.filter { claim ->
                claim.canBeReviewed()
            }
        } catch (e: Exception) {
            android.util.Log.e("ClaimsRepository", "Failed to get claims ready for review", e)
            emptyList()
        }
    }

    /**
     * Gets claim statistics for a user.
     */
    suspend fun getClaimStatistics(userId: String): ClaimStatistics {
        return try {
            val snapshot = firestore.collection(CLAIMS_COLLECTION)
                .whereEqualTo("claimerId", userId)
                .get()
                .await()
            
            val claims = snapshot.documents.mapNotNull { doc ->
                parseClaimFromDocument(doc)
            }
            
            ClaimStatistics(
                totalClaims = claims.size,
                pendingClaims = claims.count { it.status == ClaimStatus.PENDING },
                approvedClaims = claims.count { it.status == ClaimStatus.APPROVED },
                rejectedClaims = claims.count { it.status == ClaimStatus.REJECTED },
                expiredClaims = claims.count { it.status == ClaimStatus.EXPIRED }
            )
        } catch (e: Exception) {
            android.util.Log.e("ClaimsRepository", "Failed to get claim statistics", e)
            ClaimStatistics()
        }
    }

    /**
     * Converts a Claim object to a Firestore map.
     */
    private fun claimToMap(claim: Claim): Map<String, Any?> {
        return mapOf(
            "id" to claim.id,
            "claimerId" to claim.claimerId,
            "itemId" to claim.itemId,
            "itemReporterId" to claim.itemReporterId,
            "status" to claim.status.name,
            "createdAt" to claim.createdAt,
            "updatedAt" to claim.updatedAt,
            "securityQuestions" to claim.securityQuestions.map { question ->
                mapOf(
                    "id" to question.id,
                    "question" to question.question,
                    "category" to question.category,
                    "hiddenDetailKey" to question.hiddenDetailKey,
                    "questionType" to question.questionType.name
                )
            },
            "claimerAnswers" to claim.claimerAnswers,
            "reviewedAt" to claim.reviewedAt,
            "rejectionReason" to claim.rejectionReason,
            "contactUnlockedAt" to claim.contactUnlockedAt,
            "attemptCount" to claim.attemptCount,
            "lastAttemptAt" to claim.lastAttemptAt
        )
    }

    /**
     * Parses a Claim object from a Firestore document.
     */
    private fun parseClaimFromDocument(doc: com.google.firebase.firestore.DocumentSnapshot): Claim? {
        if (!doc.exists()) return null
        
        return try {
            val statusStr = doc.getString("status") ?: ClaimStatus.PENDING.name
            val status = runCatching { ClaimStatus.valueOf(statusStr) }.getOrDefault(ClaimStatus.PENDING)
            
            val questionsData = doc.get("securityQuestions") as? List<Map<String, Any>> ?: emptyList()
            val securityQuestions = questionsData.map { questionMap ->
                val questionTypeStr = questionMap["questionType"] as? String ?: QuestionType.TEXT.name
                val questionType = runCatching { QuestionType.valueOf(questionTypeStr) }.getOrDefault(QuestionType.TEXT)
                
                SecurityQuestion(
                    id = questionMap["id"] as? String ?: "",
                    question = questionMap["question"] as? String ?: "",
                    category = questionMap["category"] as? String ?: "",
                    hiddenDetailKey = questionMap["hiddenDetailKey"] as? String ?: "",
                    questionType = questionType
                )
            }
            
            val answersData = doc.get("claimerAnswers") as? List<String> ?: emptyList()
            
            Claim(
                id = doc.id,
                claimerId = doc.getString("claimerId") ?: "",
                itemId = doc.getString("itemId") ?: "",
                itemReporterId = doc.getString("itemReporterId") ?: "",
                status = status,
                createdAt = doc.getLong("createdAt") ?: 0L,
                updatedAt = doc.getLong("updatedAt") ?: 0L,
                securityQuestions = securityQuestions,
                claimerAnswers = answersData,
                reviewedAt = doc.getLong("reviewedAt"),
                rejectionReason = doc.getString("rejectionReason"),
                contactUnlockedAt = doc.getLong("contactUnlockedAt"),
                attemptCount = doc.getLong("attemptCount")?.toInt() ?: 0,
                lastAttemptAt = doc.getLong("lastAttemptAt")
            )
        } catch (e: Exception) {
            android.util.Log.e("ClaimsRepository", "Failed to parse claim document", e)
            null
        }
    }

    /**
     * Logs an audit event for claim operations.
     */
    private suspend fun logAuditEvent(
        eventType: String,
        userId: String,
        claimId: String,
        details: Map<String, Any>
    ) {
        try {
            val auditLog = hashMapOf(
                "eventType" to eventType,
                "userId" to userId,
                "claimId" to claimId,
                "timestamp" to System.currentTimeMillis(),
                "details" to details
            )
            firestore.collection(AUDIT_LOGS_COLLECTION).add(auditLog).await()
        } catch (e: Exception) {
            android.util.Log.e("ClaimsRepository", "Failed to log audit event", e)
            // Don't throw - audit logging failure shouldn't break the main operation
        }
    }
}

/**
 * Statistics about claims for a user.
 */
data class ClaimStatistics(
    val totalClaims: Int = 0,
    val pendingClaims: Int = 0,
    val approvedClaims: Int = 0,
    val rejectedClaims: Int = 0,
    val expiredClaims: Int = 0
) {
    val successRate: Double
        get() = if (totalClaims > 0) approvedClaims.toDouble() / totalClaims else 0.0
}