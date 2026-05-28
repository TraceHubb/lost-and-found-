package com.lostandfound.presentation.matching

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lostandfound.data.models.Item
import com.lostandfound.data.models.ItemType
import com.lostandfound.data.repositories.ItemsRepository
import com.lostandfound.data.repositories.MatchingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Represents a matched pair of items (one lost, one found)
data class ItemPair(
    val lostItem: Item,
    val foundItem: Item,
    val matchScore: Int,
    val matchReasons: List<String>
)

data class MatchingScreenState(
    val matchedPairs: List<ItemPair> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val searchQuery: String = ""
)

class MatchingScreenViewModel : ViewModel() {
    private val _state = MutableStateFlow(MatchingScreenState())
    val state: StateFlow<MatchingScreenState> = _state.asStateFlow()

    init {
        loadMatchedPairs()
    }

    fun loadMatchedPairs() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            ItemsRepository.observeItems(searchQuery = _state.value.searchQuery)
                .catch { exception ->
                    _state.update { it.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load items"
                    )}
                }
                .collect { allItems ->
                    // Separate lost and found items
                    val lostItems = allItems.filter { it.type == ItemType.LOST }
                    val foundItems = allItems.filter { it.type == ItemType.FOUND }
                    
                    // Find matched pairs
                    val pairs = mutableListOf<ItemPair>()
                    
                    for (lostItem in lostItems) {
                        // Find matches for this lost item (only show suggestions > 50%)
                        val matches = MatchingRepository.findMatches(lostItem, foundItems)
                            .filter { it.matchScore > 50 }
                            .take(3) // limit suggestions per lost item

                        matches.forEach { match ->
                            pairs.add(
                                ItemPair(
                                    lostItem = lostItem,
                                    foundItem = match.item,
                                    matchScore = match.matchScore,
                                    matchReasons = match.matchReasons
                                )
                            )
                        }
                    }
                    
                    // Sort pairs by match score (highest first)
                    val sortedPairs = pairs.sortedByDescending { it.matchScore }
                    
                    _state.update { it.copy(
                        matchedPairs = sortedPairs,
                        isLoading = false,
                        error = null
                    )}
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
        // Reload pairs with new search query
        loadMatchedPairs()
    }
}
