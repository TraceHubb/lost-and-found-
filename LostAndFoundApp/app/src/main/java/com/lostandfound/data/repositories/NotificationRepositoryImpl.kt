package com.lostandfound.data.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.lostandfound.data.firebase.FirebaseProviders
import com.lostandfound.data.models.ClaimNotification
import com.lostandfound.data.models.NotificationType
import com.lostandfound.data.services.NotificationRepository
import kotlinx.coroutines.tasks.await

/**
 * Firestore implementation of NotificationRepository.
 */
object NotificationRepositoryImpl : NotificationRepository {
    private const val NOTIFICATIONS_COLLECTION = "notifications"
    
    private val firestore: FirebaseFirestore
        get() = FirebaseProviders.firestore

    override suspend fun createNotification(notification: ClaimNotification) {
        try {
            val notificationMap = notificationToMap(notification)
            firestore.collection(NOTIFICATIONS_COLLECTION)
                .document(notification.id)
                .set(notificationMap)
                .await()
        } catch (e: Exception) {
            android.util.Log.e("NotificationRepository", "Failed to create notification", e)
            throw e
        }
    }

    override suspend fun getNotificationsByUser(userId: String): List<ClaimNotification> {
        return try {
            val snapshot = firestore.collection(NOTIFICATIONS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(50) // Limit to recent 50 notifications
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                parseNotificationFromDocument(doc)
            }
        } catch (e: Exception) {
            android.util.Log.e("NotificationRepository", "Failed to get notifications by user", e)
            emptyList()
        }
    }

    override suspend fun markAsRead(notificationId: String) {
        try {
            firestore.collection(NOTIFICATIONS_COLLECTION)
                .document(notificationId)
                .update("read", true)
                .await()
        } catch (e: Exception) {
            android.util.Log.e("NotificationRepository", "Failed to mark notification as read", e)
            throw e
        }
    }

    override suspend fun getUnreadCount(userId: String): Int {
        return try {
            val snapshot = firestore.collection(NOTIFICATIONS_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("read", false)
                .get()
                .await()
            
            snapshot.size()
        } catch (e: Exception) {
            android.util.Log.e("NotificationRepository", "Failed to get unread count", e)
            0
        }
    }

    override suspend fun deleteNotification(notificationId: String) {
        try {
            firestore.collection(NOTIFICATIONS_COLLECTION)
                .document(notificationId)
                .delete()
                .await()
        } catch (e: Exception) {
            android.util.Log.e("NotificationRepository", "Failed to delete notification", e)
            throw e
        }
    }

    override suspend fun deleteOldNotifications(olderThanDays: Int): Int {
        return try {
            val cutoffTime = System.currentTimeMillis() - (olderThanDays * 24 * 60 * 60 * 1000L)
            
            val snapshot = firestore.collection(NOTIFICATIONS_COLLECTION)
                .whereLessThan("createdAt", cutoffTime)
                .get()
                .await()
            
            val batch = firestore.batch()
            var deletedCount = 0
            
            for (doc in snapshot.documents) {
                batch.delete(doc.reference)
                deletedCount++
            }
            
            if (deletedCount > 0) {
                batch.commit().await()
            }
            
            deletedCount
        } catch (e: Exception) {
            android.util.Log.e("NotificationRepository", "Failed to delete old notifications", e)
            0
        }
    }

    /**
     * Gets notifications by type for a user.
     */
    suspend fun getNotificationsByType(userId: String, type: NotificationType): List<ClaimNotification> {
        return try {
            val snapshot = firestore.collection(NOTIFICATIONS_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("type", type.name)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                parseNotificationFromDocument(doc)
            }
        } catch (e: Exception) {
            android.util.Log.e("NotificationRepository", "Failed to get notifications by type", e)
            emptyList()
        }
    }

    /**
     * Marks all notifications as read for a user.
     */
    suspend fun markAllAsRead(userId: String): Int {
        return try {
            val snapshot = firestore.collection(NOTIFICATIONS_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("read", false)
                .get()
                .await()
            
            val batch = firestore.batch()
            var updatedCount = 0
            
            for (doc in snapshot.documents) {
                batch.update(doc.reference, "read", true)
                updatedCount++
            }
            
            if (updatedCount > 0) {
                batch.commit().await()
            }
            
            updatedCount
        } catch (e: Exception) {
            android.util.Log.e("NotificationRepository", "Failed to mark all as read", e)
            0
        }
    }

    /**
     * Converts a ClaimNotification to a Firestore map.
     */
    private fun notificationToMap(notification: ClaimNotification): Map<String, Any> {
        return mapOf(
            "id" to notification.id,
            "userId" to notification.userId,
            "type" to notification.type.name,
            "claimId" to notification.claimId,
            "itemId" to notification.itemId,
            "message" to notification.message,
            "read" to notification.read,
            "createdAt" to notification.createdAt
        )
    }

    /**
     * Parses a ClaimNotification from a Firestore document.
     */
    private fun parseNotificationFromDocument(doc: com.google.firebase.firestore.DocumentSnapshot): ClaimNotification? {
        if (!doc.exists()) return null
        
        return try {
            val typeStr = doc.getString("type") ?: NotificationType.CLAIM_SUBMITTED.name
            val type = runCatching { NotificationType.valueOf(typeStr) }.getOrDefault(NotificationType.CLAIM_SUBMITTED)
            
            ClaimNotification(
                id = doc.id,
                userId = doc.getString("userId") ?: "",
                type = type,
                claimId = doc.getString("claimId") ?: "",
                itemId = doc.getString("itemId") ?: "",
                message = doc.getString("message") ?: "",
                read = doc.getBoolean("read") ?: false,
                createdAt = doc.getLong("createdAt") ?: 0L
            )
        } catch (e: Exception) {
            android.util.Log.e("NotificationRepository", "Failed to parse notification document", e)
            null
        }
    }
}