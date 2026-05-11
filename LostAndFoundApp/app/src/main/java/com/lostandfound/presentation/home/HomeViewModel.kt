package com.lostandfound.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lostandfound.data.models.ItemStatus
import com.lostandfound.data.models.ItemType
import com.lostandfound.data.repositories.AuthRepository
import com.lostandfound.data.repositories.ItemsRepository
import com.lostandfound.data.repositories.MatchingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationCounts(
    val itemsReady: Int = 0,
    val matchingResults: Int = 0,
    val itemsInReview: Int = 0
)

data class HomeScreenState(
    val notificationCounts: NotificationCounts = NotificationCounts(),
    val dismissedNotifications: Set<String> = emptySet(),
    val isLoading: Boolean = true
)

class HomeViewModel : ViewModel() {
    private val _state = MutableStateFlow(HomeScreenState())
    val state: StateFlow<HomeScreenState> = _state
    
    init {
        loadNotificationCounts()
    }
    
    fun loadNotificationCounts() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                val currentUserId = AuthRepository.currentUser?.uid
                
                if (currentUserId != null) {
                    // Get all items
                    val allItems = ItemsRepository.getAllItems()
                    
                    // Items Ready: User's items with ACTIVE status
                    val itemsReady = allItems.count { item ->
                        item.userId == currentUserId && item.status == ItemStatus.ACTIVE
                    }
                    
                    // Matching Results: Count matches for all user's lost items
                    val userLostItems = allItems.filter { 
                        it.userId == currentUserId && it.type == ItemType.LOST 
                    }
                    
                    var totalMatches = 0
                    userLostItems.forEach { lostItem ->
                        val matches = MatchingRepository.findMatches(lostItem, allItems)
                        totalMatches += matches.size
                    }
                    
                    // Items in Review: User's items with RECOVERED or CLAIMED status
                    val itemsInReview = allItems.count { item ->
                        item.userId == currentUserId && 
                        (item.status == ItemStatus.RECOVERED || item.status == ItemStatus.CLAIMED)
                    }
                    
                    _state.update { 
                        it.copy(
                            notificationCounts = NotificationCounts(
                                itemsReady = itemsReady,
                                matchingResults = totalMatches,
                                itemsInReview = itemsInReview
                            ),
                            isLoading = false
                        )
                    }
                } else {
                    // User not logged in
                    _state.update { 
                        it.copy(
                            notificationCounts = NotificationCounts(),
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                // On error, keep default counts
                _state.update { 
                    it.copy(
                        notificationCounts = NotificationCounts(),
                        isLoading = false
                    )
                }
            }
        }
    }
    
    fun dismissNotification(notificationType: String) {
        _state.update { currentState ->
            currentState.copy(
                dismissedNotifications = currentState.dismissedNotifications + notificationType
            )
        }
    }
}
