package com.lostandfound.data.repositories

import android.content.Context
import android.net.Uri
import com.lostandfound.data.firebase.FirebaseProviders
import com.lostandfound.data.models.SimpleFoundItem
import com.lostandfound.data.models.SimpleLostItem
import com.lostandfound.data.models.SimpleClaim
import com.lostandfound.data.models.SimpleItemStatus
import com.lostandfound.data.models.ClaimStatus
import com.lostandfound.data.services.ImageUploadService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await

object SimpleItemsRepository {
    
    private val firestore = FirebaseProviders.firestore
    private val foundItemsCollection = firestore.collection("simple_found_items")
    private val lostItemsCollection = firestore.collection("simple_lost_items")
    private val claimsCollection = firestore.collection("simple_claims")
    
    fun getReportedFoundItems(userId: String): Flow<List<SimpleFoundItem>> = callbackFlow {
        val listener = foundItemsCollection
            .whereEqualTo("reporterId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val items = snapshot?.documents?.mapNotNull { doc ->
                    runCatching { doc.toObject(SimpleFoundItem::class.java)?.copy(id = doc.id) }.getOrNull()
                }.orEmpty()
                
                trySend(items.sortedByDescending { it.createdAt })
            }
        
        awaitClose { listener.remove() }
    }
    
    fun getReportedLostItems(userId: String): Flow<List<SimpleLostItem>> = callbackFlow {
        val listener = lostItemsCollection
            .whereEqualTo("reporterId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val items = snapshot?.documents?.mapNotNull { doc ->
                    runCatching { doc.toObject(SimpleLostItem::class.java)?.copy(id = doc.id) }.getOrNull()
                }.orEmpty()
                
                trySend(items.sortedByDescending { it.createdAt })
            }
        
        awaitClose { listener.remove() }
    }
    
    fun getClaimsByClaimer(userId: String): Flow<List<SimpleClaim>> = callbackFlow {
        val listener = claimsCollection
            .whereEqualTo("claimerId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val claims = snapshot?.documents?.mapNotNull { doc ->
                    runCatching {
                        doc.toObject(SimpleClaim::class.java)?.copy(
                            id = doc.id,
                            status = runCatching {
                                ClaimStatus.valueOf(doc.getString("status") ?: ClaimStatus.PENDING.name)
                            }.getOrDefault(ClaimStatus.PENDING)
                        )
                    }.getOrNull()
                }.orEmpty()
                
                trySend(claims.sortedByDescending { it.createdAt })
            }
        
        awaitClose { listener.remove() }
    }

    /**
     * Observe all lost items (any status) so UIs can react to claim status changes in real time.
     */
    fun observeLostItems(): Flow<List<SimpleLostItem>> = callbackFlow {
        val listener = lostItemsCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val items = snapshot?.documents?.mapNotNull { doc ->
                    runCatching { doc.toObject(SimpleLostItem::class.java)?.copy(id = doc.id) }.getOrNull()
                }.orEmpty()

                trySend(items.sortedByDescending { it.createdAt })
            }

        awaitClose { listener.remove() }
    }

    /**
     * Observe all found items (any status) so UIs can react to claim status changes in real time.
     */
    fun observeFoundItems(): Flow<List<SimpleFoundItem>> = callbackFlow {
        val listener = foundItemsCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val items = snapshot?.documents?.mapNotNull { doc ->
                    runCatching { doc.toObject(SimpleFoundItem::class.java)?.copy(id = doc.id) }.getOrNull()
                }.orEmpty()

                trySend(items.sortedByDescending { it.createdAt })
            }

        awaitClose { listener.remove() }
    }
    
    /**
     * Add a new found item with 4 verification questions
     */
    suspend fun addFoundItem(
        item: SimpleFoundItem,
        imageUri: Uri? = null,
        context: Context
    ): String {
        // Upload image if provided
        val imageUrl = imageUri?.let { uri ->
            ImageUploadService.uploadImage(context, uri)?.getOrNull()
        }
        
        // Create item with image URL
        val itemWithImage = item.copy(
            id = firestore.collection("temp").document().id,
            imageUrl = imageUrl
        )
        
        // Save to Firestore
        foundItemsCollection.document(itemWithImage.id).set(itemWithImage).await()
        
        return itemWithImage.id
    }
    
    /**
     * Add a new lost item with 4 verification questions
     */
    suspend fun addLostItem(
        item: SimpleLostItem,
        imageUri: Uri? = null,
        context: Context
    ): String {
        // Upload image if provided
        val imageUrl = imageUri?.let { uri ->
            ImageUploadService.uploadImage(context, uri)?.getOrNull()
        }
        
        // Create item with image URL
        val itemWithImage = item.copy(
            id = firestore.collection("temp").document().id,
            imageUrl = imageUrl
        )
        
        // Save to Firestore
        lostItemsCollection.document(itemWithImage.id).set(itemWithImage).await()
        
        return itemWithImage.id
    }
    
    /**
     * Get all active found items
     */
    fun getActiveFoundItems(): Flow<List<SimpleFoundItem>> {
        return callbackFlow {
            val listener = foundItemsCollection
                .whereEqualTo("status", SimpleItemStatus.ACTIVE.name)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    
                    val items = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            doc.toObject(SimpleFoundItem::class.java)?.copy(id = doc.id)
                        } catch (e: Exception) {
                            null
                        }
                    } ?: emptyList()
                    
                    trySend(items)
                }
            
            awaitClose { listener.remove() }
        }
    }
    
    /**
     * Get all active lost items
     */
    fun getActiveLostItems(): Flow<List<SimpleLostItem>> {
        return callbackFlow {
            val listener = lostItemsCollection
                .whereEqualTo("status", SimpleItemStatus.ACTIVE.name)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    
                    val items = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            doc.toObject(SimpleLostItem::class.java)?.copy(id = doc.id)
                        } catch (e: Exception) {
                            null
                        }
                    } ?: emptyList()
                    
                    trySend(items)
                }
            
            awaitClose { listener.remove() }
        }
    }
    
    /**
     * Get a specific found item by ID
     */
    suspend fun getFoundItemById(itemId: String): SimpleFoundItem? {
        return try {
            val doc = foundItemsCollection.document(itemId).get().await()
            doc.toObject(SimpleFoundItem::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Get a specific lost item by ID
     */
    suspend fun getLostItemById(itemId: String): SimpleLostItem? {
        return try {
            val doc = lostItemsCollection.document(itemId).get().await()
            doc.toObject(SimpleLostItem::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Submit a claim for a found item
     */
    suspend fun submitFoundClaim(claim: SimpleClaim): String {
        val claimWithId = claim.copy(
            id = firestore.collection("temp").document().id
        )
        
        claimsCollection.document(claimWithId.id).set(claimWithId).await()
        
        // Get the found item to check answers
        val foundItem = getFoundItemById(claimWithId.itemId)
        if (foundItem != null) {
            val correctAnswers = listOf(foundItem.answer1, foundItem.answer2, foundItem.answer3, foundItem.answer4)
            
            // If all answers are correct, approve the claim automatically
            if (claimWithId.isAllCorrect(correctAnswers)) {
                approveFoundClaim(claimWithId.id, claimWithId.itemId)
            }
        }
        
        return claimWithId.id
    }
    
    /**
     * Submit a claim for a lost item
     */
    suspend fun submitLostClaim(claim: SimpleClaim): String {
        val claimWithId = claim.copy(
            id = firestore.collection("temp").document().id
        )
        
        claimsCollection.document(claimWithId.id).set(claimWithId).await()
        
        // Get the lost item to check answers
        val lostItem = getLostItemById(claimWithId.itemId)
        if (lostItem != null) {
            val correctAnswers = listOf(lostItem.answer1, lostItem.answer2, lostItem.answer3, lostItem.answer4)
            
            // If all answers are correct, approve the claim automatically
            if (claimWithId.isAllCorrect(correctAnswers)) {
                approveLostClaim(claimWithId.id, claimWithId.itemId)
            }
        }
        
        return claimWithId.id
    }
    
    /**
     * Approve a found item claim and mark item as claimed
     */
    private suspend fun approveFoundClaim(claimId: String, itemId: String) {
        // Update claim status
        claimsCollection.document(claimId).update(
            mapOf(
                "status" to ClaimStatus.APPROVED.name,
                "reviewedAt" to System.currentTimeMillis()
            )
        ).await()
        
        // Update item status
        foundItemsCollection.document(itemId).update(
            "status", SimpleItemStatus.CLAIMED.name
        ).await()
    }
    
    /**
     * Approve a lost item claim and mark item as claimed
     */
    private suspend fun approveLostClaim(claimId: String, itemId: String) {
        // Update claim status
        claimsCollection.document(claimId).update(
            mapOf(
                "status" to ClaimStatus.APPROVED.name,
                "reviewedAt" to System.currentTimeMillis()
            )
        ).await()
        
        // Update item status
        lostItemsCollection.document(itemId).update(
            "status", SimpleItemStatus.CLAIMED.name
        ).await()
    }
    
    /**
     * Get claim by ID
     */
    suspend fun getClaimById(claimId: String): SimpleClaim? {
        return try {
            val doc = claimsCollection.document(claimId).get().await()
            doc.toObject(SimpleClaim::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Get claims for a specific item
     */
    suspend fun getClaimsForItem(itemId: String): List<SimpleClaim> {
        return try {
            val docs = claimsCollection.whereEqualTo("itemId", itemId).get().await()
            docs.mapNotNull { doc ->
                doc.toObject(SimpleClaim::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}