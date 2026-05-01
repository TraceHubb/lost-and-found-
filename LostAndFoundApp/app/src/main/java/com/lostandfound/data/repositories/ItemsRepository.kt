package com.lostandfound.data.repositories

import android.net.Uri
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.lostandfound.data.firebase.FirebaseProviders
import com.lostandfound.data.models.Item
import com.lostandfound.data.models.ItemStatus
import com.lostandfound.data.models.ItemType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

object ItemsRepository {
    private const val ITEMS_COLLECTION = "items"

    fun observeItems(
        type: ItemType? = null,
        searchQuery: String = ""
    ): Flow<List<Item>> = callbackFlow {
        var registration: ListenerRegistration? = null

        val baseQuery = FirebaseProviders.firestore
            .collection(ITEMS_COLLECTION)
            .orderBy("datePosted", Query.Direction.DESCENDING)

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
                val typeStr = (doc.getString("type") ?: ItemType.LOST.name)
                val statusStr = (doc.getString("status") ?: ItemStatus.ACTIVE.name)
                val parsedType = runCatching { ItemType.valueOf(typeStr) }.getOrDefault(ItemType.LOST)
                val parsedStatus =
                    runCatching { ItemStatus.valueOf(statusStr) }.getOrDefault(ItemStatus.ACTIVE)

                Item(
                    id = doc.id,
                    userId = doc.getString("userId") ?: "",
                    itemName = doc.getString("itemName") ?: "",
                    description = doc.getString("description") ?: "",
                    location = doc.getString("location") ?: "",
                    date = doc.getLong("date") ?: 0L,
                    imageUrl = doc.getString("imageUrl") ?: "",
                    type = parsedType,
                    status = parsedStatus,
                    contactEmail = doc.getString("contactEmail") ?: "",
                    contactPhone = doc.getString("contactPhone") ?: "",
                    datePosted = doc.getLong("datePosted") ?: 0L
                )
            }

            val needle = searchQuery.trim().lowercase()
            val filtered = if (needle.isEmpty()) {
                all
            } else {
                all.filter { item ->
                    item.itemName.lowercase().contains(needle) ||
                        item.description.lowercase().contains(needle) ||
                        item.location.lowercase().contains(needle)
                }
            }

            trySend(filtered)
        }

        awaitClose { registration?.remove() }
    }

    suspend fun addItem(
        item: Item,
        imageUri: Uri?
    ) {
        val userId = AuthRepository.currentUser?.uid ?: throw IllegalStateException("Not logged in")
        val docRef = FirebaseProviders.firestore.collection(ITEMS_COLLECTION).document()

        val finalImageUrl = if (imageUri != null) {
            val storageRef = FirebaseProviders.storage.reference
                .child("items")
                .child("${docRef.id}.jpg")
            storageRef.putFile(imageUri).await()
            storageRef.downloadUrl.await().toString()
        } else {
            ""
        }

        val toSave = item.copy(
            id = docRef.id,
            userId = userId,
            imageUrl = finalImageUrl
        )

        // Store enums as strings for easy queries.
        val map = hashMapOf(
            "id" to toSave.id,
            "userId" to toSave.userId,
            "itemName" to toSave.itemName,
            "description" to toSave.description,
            "location" to toSave.location,
            "date" to toSave.date,
            "imageUrl" to toSave.imageUrl,
            "type" to toSave.type.name,
            "status" to toSave.status.name,
            "contactEmail" to toSave.contactEmail,
            "contactPhone" to toSave.contactPhone,
            "datePosted" to toSave.datePosted
        )

        docRef.set(map).await()
    }
}

