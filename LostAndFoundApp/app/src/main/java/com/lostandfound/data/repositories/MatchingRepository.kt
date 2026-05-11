package com.lostandfound.data.repositories

import com.lostandfound.data.models.Item
import com.lostandfound.data.models.ItemType

object MatchingRepository {
    
    /**
     * Find potential matches for a given item
     * Matches lost items with found items and vice versa
     * ONLY matches items with the exact same category
     */
    suspend fun findMatches(item: Item, allItems: List<Item>): List<ItemMatch> {
        // Get opposite type items (if lost, find found items; if found, find lost items)
        val oppositeType = if (item.type == ItemType.LOST) ItemType.FOUND else ItemType.LOST
        
        // Filter by opposite type, different ID, AND same category (case-insensitive)
        val candidateItems = allItems.filter { candidate ->
            candidate.type == oppositeType && 
            candidate.id != item.id &&
            candidate.category.isNotBlank() &&
            item.category.isNotBlank() &&
            candidate.category.equals(item.category, ignoreCase = true)
        }
        
        // Calculate match score for each candidate
        val matches = candidateItems.mapNotNull { candidate ->
            val score = calculateMatchScore(item, candidate)
            if (score > 0) {
                ItemMatch(
                    item = candidate,
                    matchScore = score,
                    matchReasons = getMatchReasons(item, candidate)
                )
            } else null
        }
        
        // Sort by match score (highest first)
        return matches.sortedByDescending { it.matchScore }
    }
    
    /**
     * Calculate match score between two items (0-100)
     */
    private fun calculateMatchScore(item1: Item, item2: Item): Int {
        var score = 0
        
        // Category match (40 points) - Most important
        if (item1.category.isNotBlank() && item2.category.isNotBlank()) {
            if (item1.category.equals(item2.category, ignoreCase = true)) {
                score += 40
            }
        }
        
        // Color match (20 points)
        if (item1.color.isNotBlank() && item2.color.isNotBlank()) {
            if (item1.color.equals(item2.color, ignoreCase = true)) {
                score += 20
            }
        }
        
        // Brand match (20 points)
        if (item1.brand.isNotBlank() && item2.brand.isNotBlank()) {
            if (item1.brand.equals(item2.brand, ignoreCase = true)) {
                score += 20
            }
        }
        
        // Location match (10 points)
        if (item1.location.isNotBlank() && item2.location.isNotBlank()) {
            if (item1.location.equals(item2.location, ignoreCase = true)) {
                score += 10
            }
        }
        
        // Item name similarity (10 points)
        if (item1.itemName.isNotBlank() && item2.itemName.isNotBlank()) {
            val similarity = calculateStringSimilarity(item1.itemName, item2.itemName)
            score += (similarity * 10).toInt()
        }
        
        return score
    }
    
    /**
     * Get human-readable match reasons
     */
    private fun getMatchReasons(item1: Item, item2: Item): List<String> {
        val reasons = mutableListOf<String>()
        
        if (item1.category.equals(item2.category, ignoreCase = true)) {
            reasons.add("Same category: ${item1.category}")
        }
        
        if (item1.color.equals(item2.color, ignoreCase = true)) {
            reasons.add("Same color: ${item1.color}")
        }
        
        if (item1.brand.equals(item2.brand, ignoreCase = true)) {
            reasons.add("Same brand: ${item1.brand}")
        }
        
        if (item1.location.equals(item2.location, ignoreCase = true)) {
            reasons.add("Same location: ${item1.location}")
        }
        
        return reasons
    }
    
    /**
     * Calculate string similarity (0.0 to 1.0)
     * Uses simple word overlap
     */
    private fun calculateStringSimilarity(str1: String, str2: String): Double {
        val words1 = str1.lowercase().split(" ", ",", "-").filter { it.isNotBlank() }
        val words2 = str2.lowercase().split(" ", ",", "-").filter { it.isNotBlank() }
        
        if (words1.isEmpty() || words2.isEmpty()) return 0.0
        
        val commonWords = words1.intersect(words2.toSet()).size
        val totalWords = maxOf(words1.size, words2.size)
        
        return commonWords.toDouble() / totalWords
    }
}

/**
 * Represents a matched item with score and reasons
 */
data class ItemMatch(
    val item: Item,
    val matchScore: Int,
    val matchReasons: List<String>
)
