package com.lostandfound.data.services

import com.lostandfound.data.models.AnswerValidationResult
import com.lostandfound.data.models.SecurityQuestion
import kotlin.math.max

/**
 * Service for validating and normalizing security question answers.
 * Handles fuzzy matching and category-specific normalization.
 */
class AnswerValidationService {
    
    /**
     * Validates a single answer against the correct answer with fuzzy matching.
     */
    fun validateAnswer(
        question: SecurityQuestion,
        userAnswer: String,
        correctAnswer: String
    ): AnswerValidationResult {
        val normalizedUser = normalizeAnswer(userAnswer, question.category, question.hiddenDetailKey)
        val normalizedCorrect = normalizeAnswer(correctAnswer, question.category, question.hiddenDetailKey)
        
        return when (question.category.uppercase()) {
            "PHONE" -> validatePhoneAnswer(normalizedUser, normalizedCorrect, question.hiddenDetailKey)
            "WALLET" -> validateWalletAnswer(normalizedUser, normalizedCorrect, question.hiddenDetailKey)
            "KEYS" -> validateKeysAnswer(normalizedUser, normalizedCorrect, question.hiddenDetailKey)
            "BAG", "BACKPACK", "BAG/BACKPACK" -> validateBagAnswer(normalizedUser, normalizedCorrect, question.hiddenDetailKey)
            "LAPTOP" -> validateLaptopAnswer(normalizedUser, normalizedCorrect, question.hiddenDetailKey)
            "TABLET" -> validateTabletAnswer(normalizedUser, normalizedCorrect, question.hiddenDetailKey)
            "HEADPHONES" -> validateHeadphonesAnswer(normalizedUser, normalizedCorrect, question.hiddenDetailKey)
            "WATCH" -> validateWatchAnswer(normalizedUser, normalizedCorrect, question.hiddenDetailKey)
            "JEWELRY" -> validateJewelryAnswer(normalizedUser, normalizedCorrect, question.hiddenDetailKey)
            "CLOTHING" -> validateClothingAnswer(normalizedUser, normalizedCorrect, question.hiddenDetailKey)
            "BOOKS" -> validateBooksAnswer(normalizedUser, normalizedCorrect, question.hiddenDetailKey)
            "ID", "DOCUMENTS", "ID/DOCUMENTS" -> validateIdDocumentsAnswer(normalizedUser, normalizedCorrect, question.hiddenDetailKey)
            "GLASSES" -> validateGlassesAnswer(normalizedUser, normalizedCorrect, question.hiddenDetailKey)
            "UMBRELLA" -> validateUmbrellaAnswer(normalizedUser, normalizedCorrect, question.hiddenDetailKey)
            "WATER BOTTLE", "WATER_BOTTLE" -> validateWaterBottleAnswer(normalizedUser, normalizedCorrect, question.hiddenDetailKey)
            else -> validateGenericAnswer(normalizedUser, normalizedCorrect)
        }
    }
    
    /**
     * Normalizes an answer based on category and field type.
     */
    private fun normalizeAnswer(answer: String, category: String, fieldKey: String): String {
        var normalized = answer.trim().lowercase()
        
        // Category-specific normalization
        normalized = when (category.uppercase()) {
            "PHONE" -> normalizePhoneAnswer(normalized, fieldKey)
            "WALLET" -> normalizeWalletAnswer(normalized, fieldKey)
            "KEYS" -> normalizeKeysAnswer(normalized, fieldKey)
            "LAPTOP", "TABLET" -> normalizeTechAnswer(normalized, fieldKey)
            else -> normalized
        }
        
        // General normalization
        normalized = normalized
            .replace(Regex("\\s+"), " ") // Multiple spaces to single space
            .replace(Regex("[^a-z0-9\\s]"), "") // Remove special characters except spaces
        
        return normalized
    }
    
    /**
     * Phone-specific answer normalization.
     */
    private fun normalizePhoneAnswer(answer: String, fieldKey: String): String {
        return when (fieldKey) {
            "brand_model" -> {
                answer
                    .replace("iphone", "apple iphone")
                    .replace("samsung galaxy", "samsung")
                    .replace(Regex("\\b(thirteen|14|fifteen)\\b")) { 
                        when (it.value) {
                            "thirteen" -> "13"
                            "fourteen" -> "14" 
                            "fifteen" -> "15"
                            else -> it.value
                        }
                    }
                    .replace("pro max", "promax")
                    .replace("plus", "+")
            }
            "carrier" -> {
                answer
                    .replace("verizon wireless", "verizon")
                    .replace("at&t", "att")
                    .replace("t-mobile", "tmobile")
                    .replace("sprint", "tmobile") // Sprint merged with T-Mobile
            }
            "phone_digits" -> {
                answer.replace(Regex("[^0-9]"), "") // Keep only digits
            }
            else -> answer
        }
    }
    
    /**
     * Wallet-specific answer normalization.
     */
    private fun normalizeWalletAnswer(answer: String, fieldKey: String): String {
        return when (fieldKey) {
            "cash_amount" -> {
                // Normalize currency amounts
                answer
                    .replace("$", "")
                    .replace("dollars", "")
                    .replace("dollar", "")
                    .replace("bucks", "")
                    .replace("about", "")
                    .replace("around", "")
                    .replace("approximately", "")
            }
            "card_types" -> {
                answer
                    .replace("credit card", "credit")
                    .replace("debit card", "debit")
                    .replace("driver's license", "drivers license")
                    .replace("id card", "id")
            }
            else -> answer
        }
    }
    
    /**
     * Keys-specific answer normalization.
     */
    private fun normalizeKeysAnswer(answer: String, fieldKey: String): String {
        return when (fieldKey) {
            "key_count" -> {
                // Convert written numbers to digits
                answer
                    .replace("one", "1")
                    .replace("two", "2")
                    .replace("three", "3")
                    .replace("four", "4")
                    .replace("five", "5")
                    .replace("six", "6")
                    .replace("seven", "7")
                    .replace("eight", "8")
                    .replace("nine", "9")
                    .replace("ten", "10")
            }
            "key_types" -> {
                answer
                    .replace("house key", "house")
                    .replace("car key", "car")
                    .replace("office key", "office")
                    .replace("apartment key", "apartment")
            }
            else -> answer
        }
    }
    
    /**
     * Tech device answer normalization (laptop, tablet).
     */
    private fun normalizeTechAnswer(answer: String, fieldKey: String): String {
        return when (fieldKey) {
            "brand_model" -> {
                answer
                    .replace("macbook pro", "macbook")
                    .replace("macbook air", "macbook")
                    .replace("surface pro", "surface")
                    .replace("thinkpad", "lenovo thinkpad")
            }
            "screen_size" -> {
                answer
                    .replace("inch", "")
                    .replace("inches", "")
                    .replace("\"", "")
                    .replace("'", "")
            }
            else -> answer
        }
    }
    
    /**
     * Phone-specific validation with category context.
     */
    private fun validatePhoneAnswer(userAnswer: String, correctAnswer: String, fieldKey: String): AnswerValidationResult {
        return when (fieldKey) {
            "phone_digits" -> {
                // Exact match required for phone digits
                val score = if (userAnswer == correctAnswer) 1.0 else 0.0
                AnswerValidationResult(score >= AnswerValidationResult.MINIMUM_SCORE, score)
            }
            "brand_model" -> {
                // More lenient matching for brand/model
                val similarity = calculateSimilarity(userAnswer, correctAnswer)
                val score = if (similarity >= 0.6) similarity else 0.0
                AnswerValidationResult(score >= AnswerValidationResult.MINIMUM_SCORE, score)
            }
            else -> validateGenericAnswer(userAnswer, correctAnswer)
        }
    }
    
    /**
     * Wallet-specific validation.
     */
    private fun validateWalletAnswer(userAnswer: String, correctAnswer: String, fieldKey: String): AnswerValidationResult {
        return when (fieldKey) {
            "cash_amount" -> {
                // Extract numeric values and compare ranges
                val userAmount = extractAmount(userAnswer)
                val correctAmount = extractAmount(correctAnswer)
                
                if (userAmount != null && correctAmount != null) {
                    val difference = kotlin.math.abs(userAmount - correctAmount)
                    val tolerance = maxOf(correctAmount * 0.3, 10.0) // 30% tolerance or $10
                    val score = if (difference <= tolerance) 0.9 else 0.0
                    AnswerValidationResult(score >= AnswerValidationResult.MINIMUM_SCORE, score)
                } else {
                    validateGenericAnswer(userAnswer, correctAnswer)
                }
            }
            else -> validateGenericAnswer(userAnswer, correctAnswer)
        }
    }
    
    /**
     * Keys-specific validation.
     */
    private fun validateKeysAnswer(userAnswer: String, correctAnswer: String, fieldKey: String): AnswerValidationResult {
        return when (fieldKey) {
            "key_count" -> {
                // Extract numbers and compare
                val userCount = extractNumber(userAnswer)
                val correctCount = extractNumber(correctAnswer)
                
                val score = if (userCount == correctCount) 1.0 else 0.0
                AnswerValidationResult(score >= AnswerValidationResult.MINIMUM_SCORE, score)
            }
            else -> validateGenericAnswer(userAnswer, correctAnswer)
        }
    }
    
    // Additional category-specific validation methods following the same pattern
    private fun validateBagAnswer(userAnswer: String, correctAnswer: String, fieldKey: String): AnswerValidationResult {
        return validateGenericAnswer(userAnswer, correctAnswer)
    }
    
    private fun validateLaptopAnswer(userAnswer: String, correctAnswer: String, fieldKey: String): AnswerValidationResult {
        return validateGenericAnswer(userAnswer, correctAnswer)
    }
    
    private fun validateTabletAnswer(userAnswer: String, correctAnswer: String, fieldKey: String): AnswerValidationResult {
        return validateGenericAnswer(userAnswer, correctAnswer)
    }
    
    private fun validateHeadphonesAnswer(userAnswer: String, correctAnswer: String, fieldKey: String): AnswerValidationResult {
        return validateGenericAnswer(userAnswer, correctAnswer)
    }
    
    private fun validateWatchAnswer(userAnswer: String, correctAnswer: String, fieldKey: String): AnswerValidationResult {
        return validateGenericAnswer(userAnswer, correctAnswer)
    }
    
    private fun validateJewelryAnswer(userAnswer: String, correctAnswer: String, fieldKey: String): AnswerValidationResult {
        return validateGenericAnswer(userAnswer, correctAnswer)
    }
    
    private fun validateClothingAnswer(userAnswer: String, correctAnswer: String, fieldKey: String): AnswerValidationResult {
        return validateGenericAnswer(userAnswer, correctAnswer)
    }
    
    private fun validateBooksAnswer(userAnswer: String, correctAnswer: String, fieldKey: String): AnswerValidationResult {
        return validateGenericAnswer(userAnswer, correctAnswer)
    }
    
    private fun validateIdDocumentsAnswer(userAnswer: String, correctAnswer: String, fieldKey: String): AnswerValidationResult {
        return validateGenericAnswer(userAnswer, correctAnswer)
    }
    
    private fun validateGlassesAnswer(userAnswer: String, correctAnswer: String, fieldKey: String): AnswerValidationResult {
        return validateGenericAnswer(userAnswer, correctAnswer)
    }
    
    private fun validateUmbrellaAnswer(userAnswer: String, correctAnswer: String, fieldKey: String): AnswerValidationResult {
        return validateGenericAnswer(userAnswer, correctAnswer)
    }
    
    private fun validateWaterBottleAnswer(userAnswer: String, correctAnswer: String, fieldKey: String): AnswerValidationResult {
        return validateGenericAnswer(userAnswer, correctAnswer)
    }
    
    /**
     * Generic answer validation using string similarity.
     */
    private fun validateGenericAnswer(userAnswer: String, correctAnswer: String): AnswerValidationResult {
        val similarity = calculateSimilarity(userAnswer, correctAnswer)
        return AnswerValidationResult(
            similarity >= AnswerValidationResult.MINIMUM_SCORE,
            similarity
        )
    }
    
    /**
     * Calculates similarity between two strings using Levenshtein distance.
     */
    private fun calculateSimilarity(str1: String, str2: String): Double {
        if (str1 == str2) return 1.0
        if (str1.isEmpty() || str2.isEmpty()) return 0.0
        
        val distance = levenshteinDistance(str1, str2)
        val maxLength = maxOf(str1.length, str2.length)
        
        return 1.0 - (distance.toDouble() / maxLength)
    }
    
    /**
     * Calculates Levenshtein distance between two strings.
     */
    private fun levenshteinDistance(str1: String, str2: String): Int {
        val dp = Array(str1.length + 1) { IntArray(str2.length + 1) }
        
        for (i in 0..str1.length) {
            dp[i][0] = i
        }
        
        for (j in 0..str2.length) {
            dp[0][j] = j
        }
        
        for (i in 1..str1.length) {
            for (j in 1..str2.length) {
                val cost = if (str1[i - 1] == str2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1,      // insertion
                    dp[i - 1][j - 1] + cost // substitution
                )
            }
        }
        
        return dp[str1.length][str2.length]
    }
    
    /**
     * Extracts numeric amount from text (for cash amounts).
     */
    private fun extractAmount(text: String): Double? {
        val regex = Regex("\\d+(?:\\.\\d{1,2})?")
        val match = regex.find(text)
        return match?.value?.toDoubleOrNull()
    }
    
    /**
     * Extracts number from text (for counts).
     */
    private fun extractNumber(text: String): Int? {
        val regex = Regex("\\d+")
        val match = regex.find(text)
        return match?.value?.toIntOrNull()
    }
}