package com.lostandfound.data.repositories

import android.net.Uri
import com.google.firebase.firestore.ListenerRegistration
import com.lostandfound.data.firebase.FirebaseProviders
import com.lostandfound.data.models.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Enhanced repository for managing items with public/hidden details separation.
 * Supports both legacy Item model and new EnhancedItem model for backward compatibility.
 */
object EnhancedItemsRepository {
    private const val ITEMS_COLLECTION = "items"

    /**
     * Observes enhanced items with public/hidden details structure.
     */
    fun observeEnhancedItems(
        type: ItemType? = null,
        searchQuery: String = ""
    ): Flow<List<EnhancedItem>> = callbackFlow {
        var registration: ListenerRegistration? = null

        val baseQuery = FirebaseProviders.firestore
            .collection(ITEMS_COLLECTION)

        val q = if (type != null) {
            baseQuery.whereEqualTo("type", type.name)
        } else {
            baseQuery
        }

        registration = q.addSnapshotListener { snap, err ->
            if (err != null) {
                close(err)
                return@addSnapshotListener
            }
            
            val all = snap?.documents.orEmpty().mapNotNull { doc ->
                parseEnhancedItemFromDocument(doc)
            }
            
            // Sort by datePosted in memory (descending - newest first)
            val sorted = all.sortedByDescending { it.datePosted }

            val needle = searchQuery.trim().lowercase()
            val filtered = if (needle.isEmpty()) {
                sorted
            } else {
                sorted.filter { item ->
                    item.publicDetails.itemName.lowercase().contains(needle) ||
                        item.publicDetails.generalDescription.lowercase().contains(needle) ||
                        item.publicDetails.locationFound.lowercase().contains(needle)
                }
            }

            trySend(filtered)
        }

        awaitClose { registration?.remove() }
    }

    /**
     * Gets all enhanced items.
     */
    suspend fun getAllEnhancedItems(): List<EnhancedItem> {
        return try {
            val snapshot = FirebaseProviders.firestore
                .collection(ITEMS_COLLECTION)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                parseEnhancedItemFromDocument(doc)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Gets an enhanced item by ID.
     */
    suspend fun getEnhancedItemById(itemId: String): EnhancedItem? {
        return try {
            val doc = FirebaseProviders.firestore
                .collection(ITEMS_COLLECTION)
                .document(itemId)
                .get()
                .await()
            
            if (!doc.exists()) return null
            parseEnhancedItemFromDocument(doc)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Adds an enhanced item with public/hidden details separation.
     */
    suspend fun addEnhancedItem(
        item: EnhancedItem,
        imageUri: Uri?,
        context: android.content.Context
    ) {
        val userId = AuthRepository.currentUser?.uid ?: throw IllegalStateException("Not logged in")
        val docRef = FirebaseProviders.firestore.collection(ITEMS_COLLECTION).document()

        var finalImageUrl = ""
        
        // Upload image to Imgur if provided
        if (imageUri != null) {
            try {
                val result = com.lostandfound.data.services.ImageUploadService.uploadImage(context, imageUri)
                finalImageUrl = result.getOrElse { "" }
            } catch (e: Exception) {
                android.util.Log.e("EnhancedItemsRepository", "Failed to upload image: ${e.message}")
                // Continue without image
            }
        }

        val toSave = item.copy(
            id = docRef.id,
            userId = userId,
            publicDetails = item.publicDetails.copy(imageUrl = finalImageUrl)
        )

        // Validate hidden details before saving
        val validationResult = CategoryHiddenDetailsSchema.validateHiddenDetails(
            toSave.hiddenDetails.category,
            toSave.hiddenDetails.details
        )
        
        if (validationResult is ValidationResult.Invalid) {
            throw IllegalArgumentException(validationResult.message)
        }

        // Store enhanced item with new structure
        val map = hashMapOf(
            "id" to toSave.id,
            "userId" to toSave.userId,
            "type" to toSave.type.name,
            "status" to toSave.status.name,
            "datePosted" to toSave.datePosted,
            "claimedBy" to toSave.claimedBy,
            "claimedAt" to toSave.claimedAt,
            
            // Public details
            "publicDetails" to hashMapOf(
                "itemName" to toSave.publicDetails.itemName,
                "category" to toSave.publicDetails.category,
                "generalDescription" to toSave.publicDetails.generalDescription,
                "locationFound" to toSave.publicDetails.locationFound,
                "dateFound" to toSave.publicDetails.dateFound,
                "imageUrl" to toSave.publicDetails.imageUrl,
                "contactEmail" to toSave.publicDetails.contactEmail,
                "contactPhone" to toSave.publicDetails.contactPhone
            ),
            
            // Hidden details (secured)
            "hiddenDetails" to hashMapOf(
                "category" to toSave.hiddenDetails.category,
                "details" to toSave.hiddenDetails.details
            ),
            
            // Legacy fields for backward compatibility
            "itemName" to toSave.publicDetails.itemName,
            "description" to toSave.publicDetails.generalDescription,
            "location" to toSave.publicDetails.locationFound,
            "date" to toSave.publicDetails.dateFound,
            "imageUrl" to toSave.publicDetails.imageUrl,
            "contactEmail" to toSave.publicDetails.contactEmail,
            "contactPhone" to toSave.publicDetails.contactPhone,
            "category" to toSave.publicDetails.category,
            "color" to (toSave.hiddenDetails.details["color"] ?: ""),
            "brand" to (toSave.hiddenDetails.details["brand"] ?: ""),
            "additionalDetails" to (toSave.hiddenDetails.details["unique_features"] ?: "")
        )

        docRef.set(map).await()
    }

    /**
     * Updates item status (for claiming).
     */
    suspend fun updateItemStatus(
        itemId: String,
        status: ItemStatus,
        claimedBy: String? = null,
        claimedAt: Long? = null
    ) {
        try {
            val updates = mutableMapOf<String, Any?>(
                "status" to status.name
            )
            
            if (claimedBy != null) {
                updates["claimedBy"] = claimedBy
            }
            
            if (claimedAt != null) {
                updates["claimedAt"] = claimedAt
            }

            FirebaseProviders.firestore
                .collection(ITEMS_COLLECTION)
                .document(itemId)
                .update(updates)
                .await()
        } catch (e: Exception) {
            android.util.Log.e("EnhancedItemsRepository", "Failed to update item status", e)
            throw e
        }
    }

    /**
     * Migrates a legacy Item to EnhancedItem format.
     */
    suspend fun migrateLegacyItem(legacyItem: Item): EnhancedItem {
        return EnhancedItem(
            id = legacyItem.id,
            userId = legacyItem.userId,
            type = legacyItem.type,
            status = legacyItem.status,
            datePosted = legacyItem.datePosted,
            publicDetails = PublicItemDetails(
                itemName = legacyItem.itemName,
                category = legacyItem.category.ifEmpty { "Other" },
                generalDescription = legacyItem.description,
                locationFound = legacyItem.location,
                dateFound = legacyItem.date,
                imageUrl = legacyItem.imageUrl,
                contactEmail = legacyItem.contactEmail,
                contactPhone = legacyItem.contactPhone
            ),
            hiddenDetails = HiddenItemDetails(
                category = legacyItem.category.ifEmpty { "Other" },
                details = mapOf(
                    "brand" to legacyItem.brand,
                    "color" to legacyItem.color,
                    "unique_features" to legacyItem.additionalDetails
                ).filterValues { it.isNotBlank() }
            ),
            claimedBy = legacyItem.claimedBy,
            claimedAt = legacyItem.claimedAt
        )
    }

    /**
     * Parses an EnhancedItem from a Firestore document.
     * Handles both new enhanced format and legacy format for backward compatibility.
     */
    private fun parseEnhancedItemFromDocument(doc: com.google.firebase.firestore.DocumentSnapshot): EnhancedItem? {
        if (!doc.exists()) return null
        
        return try {
            val typeStr = doc.getString("type") ?: ItemType.LOST.name
            val statusStr = doc.getString("status") ?: ItemStatus.ACTIVE.name
            val parsedType = runCatching { ItemType.valueOf(typeStr) }.getOrDefault(ItemType.LOST)
            val parsedStatus = runCatching { ItemStatus.valueOf(statusStr) }.getOrDefault(ItemStatus.ACTIVE)
            
            // Check if this is the new enhanced format
            val publicDetailsMap = doc.get("publicDetails") as? Map<String, Any>
            val hiddenDetailsMap = doc.get("hiddenDetails") as? Map<String, Any>
            
            if (publicDetailsMap != null && hiddenDetailsMap != null) {
                // New enhanced format
                parseEnhancedFormat(doc, parsedType, parsedStatus, publicDetailsMap, hiddenDetailsMap)
            } else {
                // Legacy format - migrate on the fly
                parseLegacyFormat(doc, parsedType, parsedStatus)
            }
        } catch (e: Exception) {
            android.util.Log.e("EnhancedItemsRepository", "Failed to parse item document", e)
            null
        }
    }

    /**
     * Parses enhanced format document.
     */
    private fun parseEnhancedFormat(
        doc: com.google.firebase.firestore.DocumentSnapshot,
        type: ItemType,
        status: ItemStatus,
        publicDetailsMap: Map<String, Any>,
        hiddenDetailsMap: Map<String, Any>
    ): EnhancedItem {
        val publicDetails = PublicItemDetails(
            itemName = publicDetailsMap["itemName"] as? String ?: "",
            category = publicDetailsMap["category"] as? String ?: "",
            generalDescription = publicDetailsMap["generalDescription"] as? String ?: "",
            locationFound = publicDetailsMap["locationFound"] as? String ?: "",
            dateFound = publicDetailsMap["dateFound"] as? Long ?: 0L,
            imageUrl = publicDetailsMap["imageUrl"] as? String ?: "",
            contactEmail = publicDetailsMap["contactEmail"] as? String ?: "",
            contactPhone = publicDetailsMap["contactPhone"] as? String ?: ""
        )
        
        val hiddenDetails = HiddenItemDetails(
            category = hiddenDetailsMap["category"] as? String ?: "",
            details = (hiddenDetailsMap["details"] as? Map<String, String>) ?: emptyMap()
        )
        
        return EnhancedItem(
            id = doc.id,
            userId = doc.getString("userId") ?: "",
            type = type,
            status = status,
            datePosted = doc.getLong("datePosted") ?: 0L,
            publicDetails = publicDetails,
            hiddenDetails = hiddenDetails,
            claimedBy = doc.getString("claimedBy"),
            claimedAt = doc.getLong("claimedAt")
        )
    }

    /**
     * Parses legacy format document and converts to enhanced format.
     */
    private fun parseLegacyFormat(
        doc: com.google.firebase.firestore.DocumentSnapshot,
        type: ItemType,
        status: ItemStatus
    ): EnhancedItem {
        val publicDetails = PublicItemDetails(
            itemName = doc.getString("itemName") ?: "",
            category = doc.getString("category") ?: "Other",
            generalDescription = doc.getString("description") ?: "",
            locationFound = doc.getString("location") ?: "",
            dateFound = doc.getLong("date") ?: 0L,
            imageUrl = doc.getString("imageUrl") ?: "",
            contactEmail = doc.getString("contactEmail") ?: "",
            contactPhone = doc.getString("contactPhone") ?: ""
        )
        
        val hiddenDetails = HiddenItemDetails(
            category = doc.getString("category") ?: "Other",
            details = mapOf(
                "brand" to (doc.getString("brand") ?: ""),
                "color" to (doc.getString("color") ?: ""),
                "unique_features" to (doc.getString("additionalDetails") ?: "")
            ).filterValues { it.isNotBlank() }
        )
        
        return EnhancedItem(
            id = doc.id,
            userId = doc.getString("userId") ?: "",
            type = type,
            status = status,
            datePosted = doc.getLong("datePosted") ?: 0L,
            publicDetails = publicDetails,
            hiddenDetails = hiddenDetails,
            claimedBy = doc.getString("claimedBy"),
            claimedAt = doc.getLong("claimedAt")
        )
    }

    /**
     * Gets items by user ID.
     */
    suspend fun getItemsByUser(userId: String): List<EnhancedItem> {
        return try {
            val snapshot = FirebaseProviders.firestore
                .collection(ITEMS_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                parseEnhancedItemFromDocument(doc)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Searches items by category.
     */
    suspend fun getItemsByCategory(category: String, type: ItemType? = null): List<EnhancedItem> {
        return try {
            var query = FirebaseProviders.firestore
                .collection(ITEMS_COLLECTION)
                .whereEqualTo("category", category)
            
            if (type != null) {
                query = query.whereEqualTo("type", type.name)
            }
            
            val snapshot = query.get().await()
            
            snapshot.documents.mapNotNull { doc ->
                parseEnhancedItemFromDocument(doc)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}