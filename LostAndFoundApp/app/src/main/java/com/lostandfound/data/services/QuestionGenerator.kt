package com.lostandfound.data.services

import com.lostandfound.data.models.*
import java.util.UUID

/**
 * Interface for generating security questions from hidden item details.
 */
interface QuestionGenerator {
    /**
     * Generates 2-3 security questions based on item category and hidden details.
     */
    fun generateQuestions(
        category: String,
        hiddenDetails: Map<String, String>
    ): QuestionGenerationResult
    
    /**
     * Validates answers against the original hidden details.
     */
    fun validateAnswers(
        questions: List<SecurityQuestion>,
        answers: List<String>,
        hiddenDetails: Map<String, String>
    ): ClaimValidationResult
}

/**
 * Result of question generation operation.
 */
sealed class QuestionGenerationResult {
    data class Success(val questions: List<SecurityQuestion>) : QuestionGenerationResult()
    data class Error(val error: QuestionGenerationError) : QuestionGenerationResult()
}

/**
 * Errors that can occur during question generation.
 */
sealed class QuestionGenerationError {
    object InsufficientDetails : QuestionGenerationError()
    object UnsupportedCategory : QuestionGenerationError()
    object InvalidHiddenDetails : QuestionGenerationError()
    data class CategorySpecificError(val category: String, val details: String) : QuestionGenerationError()
}

/**
 * Main implementation of the question generation system.
 * Routes to category-specific generators and handles validation.
 */
class CategorySpecificQuestionGenerator : QuestionGenerator {
    
    private val generators = mapOf(
        "PHONE" to PhoneQuestionGenerator(),
        "WALLET" to WalletQuestionGenerator(),
        "KEYS" to KeysQuestionGenerator(),
        "BAG/BACKPACK" to BagQuestionGenerator(),
        "BAG" to BagQuestionGenerator(),
        "BACKPACK" to BagQuestionGenerator(),
        "LAPTOP" to LaptopQuestionGenerator(),
        "TABLET" to TabletQuestionGenerator(),
        "HEADPHONES" to HeadphonesQuestionGenerator(),
        "WATCH" to WatchQuestionGenerator(),
        "JEWELRY" to JewelryQuestionGenerator(),
        "CLOTHING" to ClothingQuestionGenerator(),
        "BOOKS" to BooksQuestionGenerator(),
        "ID/DOCUMENTS" to IdDocumentsQuestionGenerator(),
        "ID" to IdDocumentsQuestionGenerator(),
        "DOCUMENTS" to IdDocumentsQuestionGenerator(),
        "GLASSES" to GlassesQuestionGenerator(),
        "UMBRELLA" to UmbrellaQuestionGenerator(),
        "WATER BOTTLE" to WaterBottleQuestionGenerator(),
        "WATER_BOTTLE" to WaterBottleQuestionGenerator()
    )
    
    private val genericGenerator = GenericQuestionGenerator()
    private val answerValidator = AnswerValidationService()
    
    override fun generateQuestions(
        category: String,
        hiddenDetails: Map<String, String>
    ): QuestionGenerationResult {
        // Validate input
        val validationResult = CategoryHiddenDetailsSchema.validateHiddenDetails(category, hiddenDetails)
        if (validationResult is ValidationResult.Invalid) {
            return QuestionGenerationResult.Error(
                QuestionGenerationError.InvalidHiddenDetails
            )
        }
        
        // Filter out empty details
        val nonEmptyDetails = hiddenDetails.filterValues { it.isNotBlank() }
        if (nonEmptyDetails.size < HiddenItemDetails.MINIMUM_REQUIRED_DETAILS) {
            return QuestionGenerationResult.Error(
                QuestionGenerationError.InsufficientDetails
            )
        }
        
        // Get appropriate generator
        val generator = generators[category.uppercase()] ?: genericGenerator
        
        try {
            val questions = generator.createQuestions(nonEmptyDetails, category)
            
            // Ensure we have 2-3 questions
            val finalQuestions = when {
                questions.size < 2 -> {
                    // Fall back to generic generator if category-specific didn't produce enough
                    val additionalQuestions = genericGenerator.createQuestions(nonEmptyDetails, category)
                    (questions + additionalQuestions).take(3)
                }
                questions.size > 3 -> questions.take(3)
                else -> questions
            }
            
            return if (finalQuestions.size >= 2) {
                QuestionGenerationResult.Success(finalQuestions)
            } else {
                QuestionGenerationResult.Error(
                    QuestionGenerationError.CategorySpecificError(
                        category, 
                        "Could not generate sufficient questions from provided details"
                    )
                )
            }
            
        } catch (e: Exception) {
            return QuestionGenerationResult.Error(
                QuestionGenerationError.CategorySpecificError(category, e.message ?: "Unknown error")
            )
        }
    }
    
    override fun validateAnswers(
        questions: List<SecurityQuestion>,
        answers: List<String>,
        hiddenDetails: Map<String, String>
    ): ClaimValidationResult {
        if (questions.size != answers.size) {
            return ClaimValidationResult(
                overallValid = false,
                individualResults = emptyList(),
                averageScore = 0.0,
                feedback = listOf("Number of answers doesn't match number of questions")
            )
        }
        
        val individualResults = questions.zip(answers) { question, answer ->
            val correctAnswer = hiddenDetails[question.hiddenDetailKey] ?: ""
            answerValidator.validateAnswer(question, answer, correctAnswer)
        }
        
        return ClaimValidationResult.fromIndividualResults(individualResults)
    }
}

/**
 * Base interface for category-specific question generators.
 */
interface CategoryQuestionGenerator {
    fun createQuestions(hiddenDetails: Map<String, String>, category: String): List<SecurityQuestion>
}

/**
 * Generic question generator for unsupported categories or fallback.
 */
class GenericQuestionGenerator : CategoryQuestionGenerator {
    
    override fun createQuestions(hiddenDetails: Map<String, String>, category: String): List<SecurityQuestion> {
        val questions = mutableListOf<SecurityQuestion>()
        
        // Generic questions based on common fields
        hiddenDetails["brand"]?.let { brand ->
            if (brand.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What is the brand or maker of this item?",
                    category = category,
                    hiddenDetailKey = "brand"
                ))
            }
        }
        
        hiddenDetails["color"]?.let { color ->
            if (color.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What is the primary color of this item?",
                    category = category,
                    hiddenDetailKey = "color"
                ))
            }
        }
        
        hiddenDetails["unique_markings"]?.let { markings ->
            if (markings.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "Describe any unique markings or features on this item.",
                    category = category,
                    hiddenDetailKey = "unique_markings"
                ))
            }
        }
        
        hiddenDetails["contents"]?.let { contents ->
            if (contents.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What are the main contents or components of this item?",
                    category = category,
                    hiddenDetailKey = "contents"
                ))
            }
        }
        
        hiddenDetails["material"]?.let { material ->
            if (material.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What material is this item made of?",
                    category = category,
                    hiddenDetailKey = "material"
                ))
            }
        }
        
        hiddenDetails["size_dimensions"]?.let { size ->
            if (size.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What is the size or dimensions of this item?",
                    category = category,
                    hiddenDetailKey = "size_dimensions"
                ))
            }
        }
        
        return questions.take(3)
    }
}

/**
 * Generates unique IDs for security questions.
 */
private fun generateId(): String = UUID.randomUUID().toString()