package com.lostandfound.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lostandfound.data.firebase.FirebaseProviders
import com.lostandfound.data.models.ItemStatus
import com.lostandfound.data.models.ItemType
import com.lostandfound.data.repositories.AuthRepository
import com.lostandfound.data.repositories.ItemsRepository
import com.lostandfound.data.repositories.MatchingRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class NotificationCounts(
    val itemsReady: Int = 0,
    val matchingResults: Int = 0,
    val itemsInReview: Int = 0
)

data class HomeScreenState(
    val notificationCounts: NotificationCounts = NotificationCounts(),
    val overviewLostItems: Int = 0,
    val overviewFoundItems: Int = 0,
    val overviewReturnedItems: Int = 0,
    val overviewActiveUsers: Int = 0,
    val reunitedTotal: Int = 0,
    val reunitedByMonth: List<Pair<String, Int>> = emptyList(),
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
                    val activeUsersCount = runCatching {
                        FirebaseProviders.firestore.collection("users").get().await().size()
                    }.getOrDefault(0)
                    
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

                    // Real overview numbers (global)
                    val overviewLostItems = allItems.count { it.type == ItemType.LOST }
                    val overviewFoundItems = allItems.count { it.type == ItemType.FOUND }
                    val overviewReturnedItems = allItems.count {
                        it.status == ItemStatus.RECOVERED || it.status == ItemStatus.CLAIMED
                    }
                    val reunitedByMonth = computeReunitedByMonth(allItems)
                    
                    _state.update { 
                        it.copy(
                            notificationCounts = NotificationCounts(
                                itemsReady = itemsReady,
                                matchingResults = totalMatches,
                                itemsInReview = itemsInReview
                            ),
                            overviewLostItems = overviewLostItems,
                            overviewFoundItems = overviewFoundItems,
                            overviewReturnedItems = overviewReturnedItems,
                            overviewActiveUsers = activeUsersCount,
                            reunitedTotal = overviewReturnedItems,
                            reunitedByMonth = reunitedByMonth,
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

    private fun computeReunitedByMonth(allItems: List<com.lostandfound.data.models.Item>): List<Pair<String, Int>> {
        val now = Calendar.getInstance()
        val monthBuckets = (4 downTo 0).map { offset ->
            val cal = (now.clone() as Calendar).apply { add(Calendar.MONTH, -offset) }
            Pair(
                SimpleDateFormat("MMM", Locale.getDefault()).format(cal.time),
                cal.get(Calendar.MONTH) to cal.get(Calendar.YEAR)
            )
        }

        return monthBuckets.map { (label, monthYear) ->
            val count = allItems.count { item ->
                val cal = Calendar.getInstance().apply {
                    timeInMillis = if (item.datePosted > 0) item.datePosted else item.date
                }
                (item.status == ItemStatus.RECOVERED || item.status == ItemStatus.CLAIMED) &&
                    cal.get(Calendar.MONTH) == monthYear.first &&
                    cal.get(Calendar.YEAR) == monthYear.second
            }
            label to count
        }
    }
}
