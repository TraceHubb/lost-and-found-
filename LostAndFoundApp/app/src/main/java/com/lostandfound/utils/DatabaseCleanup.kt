package com.lostandfound.utils

import com.google.firebase.firestore.FirebaseFirestore
import com.lostandfound.data.firebase.FirebaseProviders
import kotlinx.coroutines.tasks.await

/**
 * Utility class for cleaning up old database records before implementing
 * the new Security Questions Verification System.
 * 
 * WARNING: This will permanently delete data. Use with caution!
 */
object DatabaseCleanup {
    
    private val firestore: FirebaseFirestore
        get() = FirebaseProviders.firestore
    
    /**
     * Deletes all items from the items collection
     */
    suspend fun clearAllItems(): Result<Int> {
        return try {
            val itemsSnapshot = firestore.collection("items").get().await()
            val batch = firestore.batch()
            var deleteCount = 0
            
            for (document in itemsSnapshot.documents) {
                batch.delete(document.reference)
                deleteCount++
            }
            
            if (deleteCount > 0) {
                batch.commit().await()
            }
            
            Result.success(deleteCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Deletes all OTP verification sessions
     */
    suspend fun clearOtpSessions(): Result<Int> {
        return try {
            val otpSnapshot = firestore.collection("otpVerificationSessions").get().await()
            val batch = firestore.batch()
            var deleteCount = 0
            
            for (document in otpSnapshot.documents) {
                batch.delete(document.reference)
                deleteCount++
            }
            
            if (deleteCount > 0) {
                batch.commit().await()
            }
            
            Result.success(deleteCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Deletes all audit logs (optional - you may want to keep these)
     */
    suspend fun clearAuditLogs(): Result<Int> {
        return try {
            val auditSnapshot = firestore.collection("auditLogs").get().await()
            val batch = firestore.batch()
            var deleteCount = 0
            
            for (document in auditSnapshot.documents) {
                batch.delete(document.reference)
                deleteCount++
            }
            
            if (deleteCount > 0) {
                batch.commit().await()
            }
            
            Result.success(deleteCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Comprehensive cleanup - removes all old data
     * Call this before implementing the new security questions system
     */
    suspend fun performFullCleanup(): CleanupResult {
        val results = mutableMapOf<String, Result<Int>>()
        
        // Clear items
        results["items"] = clearAllItems()
        
        // Clear OTP sessions
        results["otpSessions"] = clearOtpSessions()
        
        // Optionally clear audit logs (uncomment if you want to remove them)
        // results["auditLogs"] = clearAuditLogs()
        
        return CleanupResult(results)
    }
    
    /**
     * Get counts of existing records before cleanup
     */
    suspend fun getRecordCounts(): Map<String, Int> {
        val counts = mutableMapOf<String, Int>()
        
        try {
            val itemsCount = firestore.collection("items").get().await().size()
            counts["items"] = itemsCount
        } catch (e: Exception) {
            counts["items"] = -1
        }
        
        try {
            val otpCount = firestore.collection("otpVerificationSessions").get().await().size()
            counts["otpSessions"] = otpCount
        } catch (e: Exception) {
            counts["otpSessions"] = -1
        }
        
        try {
            val auditCount = firestore.collection("auditLogs").get().await().size()
            counts["auditLogs"] = auditCount
        } catch (e: Exception) {
            counts["auditLogs"] = -1
        }
        
        return counts
    }
}

/**
 * Result of cleanup operations
 */
data class CleanupResult(
    val results: Map<String, Result<Int>>
) {
    fun getTotalDeleted(): Int {
        return results.values.sumOf { result ->
            result.getOrElse { 0 }
        }
    }
    
    fun hasErrors(): Boolean {
        return results.values.any { it.isFailure }
    }
    
    fun getErrorMessages(): List<String> {
        return results.entries.mapNotNull { (collection, result) ->
            result.exceptionOrNull()?.let { exception ->
                "Failed to clear $collection: ${exception.message}"
            }
        }
    }
    
    fun getSummary(): String {
        val deleted = getTotalDeleted()
        val errors = getErrorMessages()
        
        return buildString {
            appendLine("Cleanup Summary:")
            appendLine("- Total records deleted: $deleted")
            
            results.forEach { (collection, result) ->
                val count = result.getOrElse { 0 }
                val status = if (result.isSuccess) "✓" else "✗"
                appendLine("- $collection: $count records $status")
            }
            
            if (errors.isNotEmpty()) {
                appendLine("\nErrors:")
                errors.forEach { error ->
                    appendLine("- $error")
                }
            }
        }
    }
}