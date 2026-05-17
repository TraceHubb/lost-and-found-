package com.lostandfound.data.models

/**
 * Simple item model for found items with 4 verification questions
 */
data class SimpleFoundItem(
    val id: String = "",
    val reporterId: String = "",
    val itemName: String = "",
    val category: String = "",
    val generalDescription: String = "",
    val locationFound: String = "",
    val dateFound: Long = System.currentTimeMillis(),
    val contactEmail: String = "",
    val contactPhone: String = "",
    val imageUrl: String? = null,
    
    // 4 verification questions with expected yes/no answers
    val question1: String = "", // e.g., "Is the item blue?"
    val answer1: Boolean = true, // true = "yes", false = "no"
    val question2: String = "", // e.g., "Does it have a logo?"
    val answer2: Boolean = false, // true = "yes", false = "no"
    val question3: String = "", // e.g., "Is it damaged?"
    val answer3: Boolean = true, // true = "yes", false = "no"
    val question4: String = "", // e.g., "Does it have keys inside?"
    val answer4: Boolean = false, // true = "yes", false = "no"
    
    val status: SimpleItemStatus = SimpleItemStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Claim attempt for verification
 */
data class SimpleClaim(
    val id: String = "",
    val itemId: String = "",
    val claimerId: String = "",
    val claimer_email: String = "",
    
    // Answers to the 4 questions (true/false)
    val answer1: Boolean = false,
    val answer2: Boolean = false,
    val answer3: Boolean = false,
    val answer4: Boolean = false,
    
    val status: ClaimStatus = ClaimStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long? = null
) {
    /**
     * Check if all 4 answers match the expected answers
     */
    fun isAllCorrect(expectedAnswers: List<Boolean>): Boolean {
        if (expectedAnswers.size != 4) return false
        
        val userAnswers = listOf(answer1, answer2, answer3, answer4)
        return userAnswers.zip(expectedAnswers).all { (userAnswer, expectedAnswer) ->
            userAnswer == expectedAnswer
        }
    }
    
    /**
     * Get the number of correct answers
     */
    fun getCorrectCount(expectedAnswers: List<Boolean>): Int {
        if (expectedAnswers.size != 4) return 0
        
        val userAnswers = listOf(answer1, answer2, answer3, answer4)
        return userAnswers.zip(expectedAnswers).count { (userAnswer, expectedAnswer) ->
            userAnswer == expectedAnswer
        }
    }
}

enum class SimpleItemStatus {
    ACTIVE,     // Available for claiming
    CLAIMED,    // Successfully claimed
    EXPIRED     // No longer available
}

/**
 * Simple item model for lost items with 4 verification questions
 */
data class SimpleLostItem(
    val id: String = "",
    val reporterId: String = "",
    val itemName: String = "",
    val category: String = "",
    val generalDescription: String = "",
    val locationLost: String = "",
    val dateLost: Long = System.currentTimeMillis(),
    val contactEmail: String = "",
    val contactPhone: String = "",
    val imageUrl: String? = null,
    
    // 4 verification questions with expected yes/no answers
    val question1: String = "", // e.g., "Is the item blue?"
    val answer1: Boolean = true, // true = "yes", false = "no"
    val question2: String = "", // e.g., "Does it have a logo?"
    val answer2: Boolean = false, // true = "yes", false = "no"
    val question3: String = "", // e.g., "Is it damaged?"
    val answer3: Boolean = true, // true = "yes", false = "no"
    val question4: String = "", // e.g., "Does it have keys inside?"
    val answer4: Boolean = false, // true = "yes", false = "no"
    
    val status: SimpleItemStatus = SimpleItemStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis()
)