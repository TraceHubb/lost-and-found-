package com.lostandfound.presentation.suggestions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lostandfound.data.models.SimpleFoundItem
import com.lostandfound.data.models.SimpleLostItem
import com.lostandfound.data.repositories.SimpleItemsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SimpleSuggestionPair(
    val lostItem: SimpleLostItem,
    val foundItem: SimpleFoundItem,
    val matchScore: Int
)

data class SuggestionsState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val searchQuery: String = "",
    val pairs: List<SimpleSuggestionPair> = emptyList()
)

class SuggestionsViewModel : ViewModel() {
    private val _state = MutableStateFlow(SuggestionsState())
    val state: StateFlow<SuggestionsState> = _state.asStateFlow()

    init {
        observeSuggestions()
    }

    fun updateSearchQuery(q: String) {
        _state.update { it.copy(searchQuery = q) }
    }

    private fun observeSuggestions() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            combine(
                SimpleItemsRepository.getActiveLostItems(),
                SimpleItemsRepository.getActiveFoundItems()
            ) { lost, found ->
                val pairs = buildPairs(lost, found, _state.value.searchQuery)
                pairs
            }.collect { pairs ->
                _state.update { it.copy(isLoading = false, pairs = pairs, error = null) }
            }
        }
    }

    private fun buildPairs(
        lostItems: List<SimpleLostItem>,
        foundItems: List<SimpleFoundItem>,
        searchQuery: String
    ): List<SimpleSuggestionPair> {
        val needle = searchQuery.trim().lowercase()

        val pairs = mutableListOf<SimpleSuggestionPair>()
        for (lost in lostItems) {
            for (found in foundItems) {
                val score = calculateScore(lost, found)
                if (score > 50) {
                    val pair = SimpleSuggestionPair(lostItem = lost, foundItem = found, matchScore = score)
                    pairs.add(pair)
                }
            }
        }

        val filtered = if (needle.isBlank()) {
            pairs
        } else {
            pairs.filter { p ->
                p.lostItem.itemName.lowercase().contains(needle) ||
                    p.foundItem.itemName.lowercase().contains(needle) ||
                    p.lostItem.generalDescription.lowercase().contains(needle) ||
                    p.foundItem.generalDescription.lowercase().contains(needle)
            }
        }

        return filtered.sortedByDescending { it.matchScore }
    }

    private fun calculateScore(lost: SimpleLostItem, found: SimpleFoundItem): Int {
        var score = 0

        // Category (0 or 40)
        if (lost.category.isNotBlank() &&
            found.category.isNotBlank() &&
            lost.category.equals(found.category, ignoreCase = true)
        ) score += 40

        // Item name similarity (0..30)
        score += (stringSimilarity(lost.itemName, found.itemName) * 30).toInt()

        // Location similarity (0..20)
        score += (stringSimilarity(lost.locationLost, found.locationFound) * 20).toInt()

        // Description similarity (0..10)
        score += (stringSimilarity(lost.generalDescription, found.generalDescription) * 10).toInt()

        return score.coerceIn(0, 100)
    }

    private fun stringSimilarity(a: String, b: String): Double {
        val w1 = a.lowercase().split(" ", ",", "-", "_").filter { it.isNotBlank() }
        val w2 = b.lowercase().split(" ", ",", "-", "_").filter { it.isNotBlank() }
        if (w1.isEmpty() || w2.isEmpty()) return 0.0
        val common = w1.intersect(w2.toSet()).size
        val total = maxOf(w1.size, w2.size)
        return common.toDouble() / total.toDouble()
    }
}

