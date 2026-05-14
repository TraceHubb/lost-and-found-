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
    val datePosted: Long = System.currentTimeMillis(),
    // New structured fields
    val category: String = "",
    val color: String = "",
    val brand: String = "",
    val additionalDetails: String = "",
    // Claim tracking fields
    val claimedBy: String? = null,
    val claimedAt: Long? = null
)

enum class ItemType {
    LOST, FOUND
}

enum class ItemStatus {
    ACTIVE, RECOVERED, CLAIMED
}

// Item categories for dropdown
object ItemCategories {
    val categories = listOf(
        "Phone",
        "Wallet",
        "Keys",
        "Bag/Backpack",
        "Laptop",
        "Tablet",
        "Headphones",
        "Watch",
        "Jewelry",
        "Clothing",
        "Books",
        "ID/Documents",
        "Glasses",
        "Umbrella",
        "Water Bottle",
        "Other"
    )
}
