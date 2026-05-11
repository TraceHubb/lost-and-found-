package com.lostandfound.presentation.matches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lostandfound.data.models.Item
import com.lostandfound.data.repositories.ItemMatch
import com.lostandfound.data.repositories.ItemsRepository
import com.lostandfound.data.repositories.MatchingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MatchResultsState(
    val sourceItem: Item? = null,
    val matches: List<ItemMatch> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class MatchResultsViewModel : ViewModel() {
    private val _state = MutableStateFlow(MatchResultsState())
    val state: StateFlow<MatchResultsState> = _state.asStateFlow()

    private var lastItemId: String? = null

    fun loadMatches(itemId: String) {
        lastItemId = itemId
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Retrieve the source item by ID
                val sourceItem = ItemsRepository.getItemById(itemId)
                
                if (sourceItem == null) {
                    _state.update { it.copy(
                        isLoading = false,
                        error = "Item not found"
                    )}
                    return@launch
                }
                
                // Retrieve all items
                val allItems = ItemsRepository.getAllItems()
                
                // Calculate matches using MatchingRepository
                val matches = MatchingRepository.findMatches(sourceItem, allItems)
                
                // Update state with source item and sorted matches
                _state.update { it.copy(
                    sourceItem = sourceItem,
                    matches = matches,
                    isLoading = false,
                    error = null
                )}
            } catch (e: Exception) {
                _state.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load matches"
                )}
            }
        }
    }

    fun retry() {
        lastItemId?.let { itemId ->
            loadMatches(itemId)
        }
    }
}
