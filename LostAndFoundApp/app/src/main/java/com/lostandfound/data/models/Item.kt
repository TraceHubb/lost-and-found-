package com.lostandfound.data.models

data class Item(
    val id: String = "",
    val userId: String = "",
    val itemName: String = "",
    val description: String = "",
    val location: String = "",
    val date: Long = 0,
    val imageUrl: String = "",
    val type: ItemType = ItemType.LOST,
    val status: ItemStatus = ItemStatus.ACTIVE,
    val contactEmail: String = "",
    val contactPhone: String = "",
    val datePosted: Long = System.currentTimeMillis()
)

enum class ItemType {
    LOST, FOUND
}

enum class ItemStatus {
    ACTIVE, RECOVERED, CLAIMED
}
