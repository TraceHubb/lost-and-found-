package com.lostandfound.data.models

/**
 * Enhanced Item model supporting the Security Questions Verification System.
 * Separates item details into public (visible to all) and hidden (verification only) information.
 */
data class EnhancedItem(
    val id: String = "",
    val userId: String = "",
    val type: ItemType = ItemType.LOST,
    val status: ItemStatus = ItemStatus.ACTIVE,
    val datePosted: Long = System.currentTimeMillis(),
    
    // Public details - visible to all users
    val publicDetails: PublicItemDetails,
    
    // Hidden details - used only for security questions
    val hiddenDetails: HiddenItemDetails,
    
    // Claim tracking
    val claimedBy: String? = null,
    val claimedAt: Long? = null
)

/**
 * Public item information visible to all users browsing lost and found items.
 */
data class PublicItemDetails(
    val itemName: String,
    val category: String,
    val generalDescription: String,
    val locationFound: String,
    val dateFound: Long,
    val imageUrl: String = "",
    val contactEmail: String,
    val contactPhone: String
)

/**
 * Hidden item information used exclusively for generating security questions.
 * Only accessible to the Question Generator and item reporter.
 */
data class HiddenItemDetails(
    val category: String,
    val details: Map<String, String> // Category-specific hidden details
) {
    /**
     * Validates that sufficient hidden details are provided for question generation.
     */
    fun hasMinimumDetails(): Boolean {
        val nonEmptyDetails = details.values.count { it.isNotBlank() }
        return nonEmptyDetails >= MINIMUM_REQUIRED_DETAILS
    }
    
    /**
     * Gets the count of non-empty hidden details.
     */
    fun getDetailCount(): Int {
        return details.values.count { it.isNotBlank() }
    }
    
    companion object {
        const val MINIMUM_REQUIRED_DETAILS = 3
    }
}

/**
 * Schema definitions for category-specific hidden details.
 * Each category defines the expected hidden detail fields for security question generation.
 */
object CategoryHiddenDetailsSchema {
    
    val PHONE = mapOf(
        "brand_model" to "Brand and model (e.g., iPhone 13 Pro)",
        "color" to "Exact color of the phone",
        "case_type" to "Case type and color",
        "screen_condition" to "Screen condition/cracks/protector",
        "wallpaper" to "Lock screen wallpaper description",
        "home_apps" to "Apps visible on home screen",
        "phone_digits" to "Last 4 digits of phone number",
        "carrier" to "Mobile carrier (Verizon, AT&T, etc.)"
    )
    
    val WALLET = mapOf(
        "color_material" to "Color and material (leather, fabric, etc.)",
        "brand" to "Brand or maker",
        "card_types" to "Types of cards inside (credit, debit, ID, etc.)",
        "cash_amount" to "Approximate cash amount",
        "id_type" to "Type of ID (driver's license, student ID, etc.)",
        "unique_features" to "Unique features, wear patterns, or damage"
    )
    
    val KEYS = mapOf(
        "keychain_details" to "Keychain description (material, color, shape)",
        "key_count" to "Number of keys",
        "key_types" to "Types of keys (house, car, office, etc.)",
        "unique_features" to "Unique key features or markings",
        "attached_items" to "Items attached to keychain (fobs, tags, etc.)"
    )
    
    val BAG_BACKPACK = mapOf(
        "brand" to "Brand or maker",
        "color_material" to "Color and material",
        "contents" to "Main contents inside",
        "pockets_compartments" to "Number and types of pockets/compartments",
        "wear_patterns" to "Wear patterns, stains, or damage",
        "zippers_closures" to "Zipper colors, closure types, or unique features"
    )
    
    val LAPTOP = mapOf(
        "brand_model" to "Brand and model",
        "color" to "Color of the laptop",
        "stickers_decorations" to "Stickers, decorations, or markings",
        "screen_size" to "Screen size (13-inch, 15-inch, etc.)",
        "keyboard_layout" to "Keyboard layout or language",
        "ports_connections" to "Visible ports or connections",
        "case_sleeve" to "Case or sleeve description"
    )
    
    val TABLET = mapOf(
        "brand_model" to "Brand and model (iPad, Samsung Galaxy, etc.)",
        "color" to "Color of the device",
        "case_cover" to "Case or cover description",
        "screen_size" to "Screen size",
        "accessories" to "Accessories (stylus, keyboard, etc.)",
        "wallpaper" to "Wallpaper or background image",
        "apps" to "Visible apps on home screen"
    )
    
    val HEADPHONES = mapOf(
        "brand_model" to "Brand and model",
        "color" to "Color of headphones",
        "type" to "Type (over-ear, in-ear, wireless, wired)",
        "case_accessories" to "Case or accessories included",
        "wear_damage" to "Wear patterns or damage",
        "unique_features" to "Unique features or customizations"
    )
    
    val WATCH = mapOf(
        "brand_model" to "Brand and model",
        "band_material" to "Band material and color",
        "face_design" to "Watch face design or digital display",
        "size" to "Watch size or fit",
        "unique_features" to "Unique features, engravings, or damage",
        "accessories" to "Accessories or attachments"
    )
    
    val JEWELRY = mapOf(
        "metal_type" to "Metal type (gold, silver, platinum, etc.)",
        "stone_details" to "Stone types, colors, or settings",
        "engravings" to "Engravings or inscriptions",
        "size_fit" to "Size or fit information",
        "brand_maker" to "Brand or maker marks",
        "unique_design" to "Unique design features or damage"
    )
    
    val CLOTHING = mapOf(
        "brand" to "Brand or label",
        "size" to "Size (S, M, L, XL, etc.)",
        "color_pattern" to "Color and pattern",
        "material" to "Material or fabric type",
        "unique_features" to "Unique features, stains, or damage",
        "pockets_contents" to "Pockets and any contents"
    )
    
    val BOOKS = mapOf(
        "title_author" to "Title and author",
        "cover_type" to "Cover type (hardcover, paperback)",
        "condition" to "Condition (new, used, damaged)",
        "bookmarks_notes" to "Bookmarks, notes, or highlighting",
        "unique_features" to "Unique features or damage",
        "contents" to "Any items inside the book"
    )
    
    val ID_DOCUMENTS = mapOf(
        "document_type" to "Type of document (driver's license, passport, etc.)",
        "issuing_authority" to "Issuing authority or state",
        "expiration_info" to "Expiration year or month",
        "photo_description" to "Description of photo appearance",
        "unique_features" to "Unique features or damage",
        "accompanying_items" to "Other items with the document"
    )
    
    val GLASSES = mapOf(
        "frame_type" to "Frame type and material",
        "frame_color" to "Frame color",
        "lens_type" to "Lens type (prescription, sunglasses, etc.)",
        "brand" to "Brand or maker",
        "case_accessories" to "Case or cleaning cloth included",
        "unique_features" to "Unique features or damage"
    )
    
    val UMBRELLA = mapOf(
        "color_pattern" to "Color and pattern",
        "size_type" to "Size (compact, full-size) and type",
        "handle_material" to "Handle material and design",
        "brand" to "Brand or maker",
        "condition" to "Condition or damage",
        "unique_features" to "Unique features or markings"
    )
    
    val WATER_BOTTLE = mapOf(
        "brand" to "Brand or maker",
        "color" to "Color of the bottle",
        "material" to "Material (plastic, metal, glass)",
        "size_capacity" to "Size or capacity",
        "stickers_decorations" to "Stickers, decorations, or markings",
        "unique_features" to "Unique features or damage"
    )
    
    val OTHER = mapOf(
        "brand" to "Brand or maker",
        "color" to "Primary color",
        "material" to "Material or construction",
        "unique_markings" to "Unique markings or features",
        "contents" to "Contents or components",
        "size_dimensions" to "Size or dimensions"
    )
    
    /**
     * Gets the schema for a specific category.
     */
    fun getSchemaForCategory(category: String): Map<String, String> {
        return when (category.uppercase()) {
            "PHONE" -> PHONE
            "WALLET" -> WALLET
            "KEYS" -> KEYS
            "BAG/BACKPACK", "BAG", "BACKPACK" -> BAG_BACKPACK
            "LAPTOP" -> LAPTOP
            "TABLET" -> TABLET
            "HEADPHONES" -> HEADPHONES
            "WATCH" -> WATCH
            "JEWELRY" -> JEWELRY
            "CLOTHING" -> CLOTHING
            "BOOKS" -> BOOKS
            "ID/DOCUMENTS", "ID", "DOCUMENTS" -> ID_DOCUMENTS
            "GLASSES" -> GLASSES
            "UMBRELLA" -> UMBRELLA
            "WATER BOTTLE", "WATER_BOTTLE" -> WATER_BOTTLE
            else -> OTHER
        }
    }
    
    /**
     * Gets all supported categories.
     */
    fun getAllCategories(): List<String> {
        return listOf(
            "Phone", "Wallet", "Keys", "Bag/Backpack", "Laptop", "Tablet",
            "Headphones", "Watch", "Jewelry", "Clothing", "Books",
            "ID/Documents", "Glasses", "Umbrella", "Water Bottle", "Other"
        )
    }
    
    /**
     * Validates that a category has sufficient hidden details for question generation.
     */
    fun validateHiddenDetails(category: String, details: Map<String, String>): ValidationResult {
        val schema = getSchemaForCategory(category)
        val nonEmptyDetails = details.filterValues { it.isNotBlank() }
        
        return when {
            nonEmptyDetails.size < HiddenItemDetails.MINIMUM_REQUIRED_DETAILS -> {
                ValidationResult.Invalid(
                    "Please provide at least ${HiddenItemDetails.MINIMUM_REQUIRED_DETAILS} " +
                    "specific details about this $category to enable secure verification."
                )
            }
            nonEmptyDetails.keys.none { schema.containsKey(it) } -> {
                ValidationResult.Invalid(
                    "Please provide details that match the expected fields for $category items."
                )
            }
            else -> ValidationResult.Valid
        }
    }
}

/**
 * Result of validation operations.
 */
sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()
}