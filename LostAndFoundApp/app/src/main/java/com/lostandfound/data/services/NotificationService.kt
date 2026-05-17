package com.lostandfound.data.services

import com.lostandfound.data.models.ClaimNotification
import com.lostandfound.data.models.NotificationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service for managing claim-related notifications.
 * Handles push notifications and in-app notification storage.
 */
class NotificationServiceImpl(
    private val notificationRepository: NotificationRepository
) {
    
    suspend fun sendClaimSubmittedNotification(
        reporterId: String,
        claimId: String,
        itemId: String
    ) = withContext(Dispatchers.IO) {
        try {
            val notification = ClaimNotification(
                id = generateNotificationId(),
                userId = reporterId,
                type = NotificationType.CLAIM_SUBMITTED,
                claimId = claimId,
                itemId = itemId,
                message = "Someone has submitted a claim for your found item. Please review their answers."
            )
            
            // Store notification in database
            notificationRepository.createNotification(notification)
            
            // Send push notification (if implemented)
            sendPushNotification(
                userId = reporterId,
                title = "New Claim Submitted",
                message = notification.message,
                data = mapOf(
                    "type" to "claim_submitted",
                    "claimId" to claimId,
                    "itemId" to itemId
                )
            )
            
        } catch (e: Exception) {
            android.util.Log.e("NotificationService", "Failed to send claim submitted notification", e)
        }
    }
    
    suspend fun sendClaimDecisionNotification(
        claimerId: String,
        claimId: String,
        itemId: String,
        decision: NotificationType,
        rejectionReason: String?
    ) = withContext(Dispatchers.IO) {
        try {
            val message = when (decision) {
                NotificationType.CLAIM_APPROVED -> 
                    "Great news! Your claim has been approved. You can now contact the finder."
                NotificationType.CLAIM_REJECTED -> 
                    "Your claim has been rejected. ${rejectionReason ?: "Please try again with more accurate information."}"
                else -> "Your claim status has been updated."
            }
            
            val notification = ClaimNotification(
                id = generateNotificationId(),
                userId = claimerId,
                type = decision,
                claimId = claimId,
                itemId = itemId,
                message = message
            )
            
            // Store notification in database
            notificationRepository.createNotification(notification)
            
            // Send push notification
            val title = when (decision) {
                NotificationType.CLAIM_APPROVED -> "Claim Approved! 🎉"
                NotificationType.CLAIM_REJECTED -> "Claim Rejected"
                else -> "Claim Update"
            }
            
            sendPushNotification(
                userId = claimerId,
                title = title,
                message = message,
                data = mapOf(
                    "type" to decision.name.lowercase(),
                    "claimId" to claimId,
                    "itemId" to itemId
                )
            )
            
        } catch (e: Exception) {
            android.util.Log.e("NotificationService", "Failed to send claim decision notification", e)
        }
    }
    
    /**
     * Sends a notification when a claim expires.
     */
    suspend fun sendClaimExpiredNotification(
        claimerId: String,
        claimId: String,
        itemId: String
    ) = withContext(Dispatchers.IO) {
        try {
            val notification = ClaimNotification(
                id = generateNotificationId(),
                userId = claimerId,
                type = NotificationType.CLAIM_EXPIRED,
                claimId = claimId,
                itemId = itemId,
                message = "Your claim has expired. You can submit a new claim if the item is still available."
            )
            
            notificationRepository.createNotification(notification)
            
            sendPushNotification(
                userId = claimerId,
                title = "Claim Expired",
                message = notification.message,
                data = mapOf(
                    "type" to "claim_expired",
                    "claimId" to claimId,
                    "itemId" to itemId
                )
            )
            
        } catch (e: Exception) {
            android.util.Log.e("NotificationService", "Failed to send claim expired notification", e)
        }
    }
    
    /**
     * Sends a notification when contact information is unlocked.
     */
    suspend fun sendContactUnlockedNotification(
        claimerId: String,
        claimId: String,
        itemId: String,
        finderEmail: String
    ) = withContext(Dispatchers.IO) {
        try {
            val notification = ClaimNotification(
                id = generateNotificationId(),
                userId = claimerId,
                type = NotificationType.CONTACT_UNLOCKED,
                claimId = claimId,
                itemId = itemId,
                message = "Contact information unlocked! You can now reach the finder at $finderEmail"
            )
            
            notificationRepository.createNotification(notification)
            
            sendPushNotification(
                userId = claimerId,
                title = "Contact Info Available! 📞",
                message = notification.message,
                data = mapOf(
                    "type" to "contact_unlocked",
                    "claimId" to claimId,
                    "itemId" to itemId,
                    "finderEmail" to finderEmail
                )
            )
            
        } catch (e: Exception) {
            android.util.Log.e("NotificationService", "Failed to send contact unlocked notification", e)
        }
    }
    
    /**
     * Gets notifications for a user.
     */
    suspend fun getUserNotifications(userId: String): List<ClaimNotification> {
        return try {
            notificationRepository.getNotificationsByUser(userId)
        } catch (e: Exception) {
            android.util.Log.e("NotificationService", "Failed to get user notifications", e)
            emptyList()
        }
    }
    
    /**
     * Marks a notification as read.
     */
    suspend fun markNotificationAsRead(notificationId: String) {
        try {
            notificationRepository.markAsRead(notificationId)
        } catch (e: Exception) {
            android.util.Log.e("NotificationService", "Failed to mark notification as read", e)
        }
    }
    
    /**
     * Gets unread notification count for a user.
     */
    suspend fun getUnreadCount(userId: String): Int {
        return try {
            notificationRepository.getUnreadCount(userId)
        } catch (e: Exception) {
            android.util.Log.e("NotificationService", "Failed to get unread count", e)
            0
        }
    }
    
    /**
     * Sends push notification (placeholder implementation).
     * In a real app, this would integrate with Firebase Cloud Messaging or similar service.
     */
    private suspend fun sendPushNotification(
        userId: String,
        title: String,
        message: String,
        data: Map<String, String>
    ) {
        // TODO: Implement actual push notification sending
        // This would typically use Firebase Cloud Messaging (FCM)
        android.util.Log.d("NotificationService", "Push notification sent to $userId: $title - $message")
    }
    
    private fun generateNotificationId(): String {
        return "notification_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}

/**
 * Repository interface for notification storage operations.
 */
interface NotificationRepository {
    suspend fun createNotification(notification: ClaimNotification)
    suspend fun getNotificationsByUser(userId: String): List<ClaimNotification>
    suspend fun markAsRead(notificationId: String)
    suspend fun getUnreadCount(userId: String): Int
    suspend fun deleteNotification(notificationId: String)
    suspend fun deleteOldNotifications(olderThanDays: Int): Int
}