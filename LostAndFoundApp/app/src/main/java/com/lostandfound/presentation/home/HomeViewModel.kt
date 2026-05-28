package com.lostandfound.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lostandfound.data.firebase.FirebaseProviders
import com.lostandfound.data.models.ItemStatus
import com.lostandfound.data.models.ItemType
import com.lostandfound.data.models.SimpleFoundItem
import com.lostandfound.data.models.SimpleLostItem
import com.lostandfound.data.models.SimpleItemStatus
import com.lostandfound.data.repositories.AuthRepository
import com.lostandfound.data.repositories.SimpleItemsRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
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
        observeHomeCounts()
    }
    
    private fun observeHomeCounts() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val currentUserId = AuthRepository.currentUser?.uid
            if (currentUserId == null) {
                _state.update { it.copy(notificationCounts = NotificationCounts(), isLoading = false) }
                return@launch
            }

            // Active users can be fetched once (the main refresh requirement is graph counts).
            val activeUsersFromProfiles = runCatching {
                FirebaseProviders.firestore.collection("users").get().await().size()
            }.getOrDefault(0)

            // Observe simple collections (your claim flow updates these in real time).
            combine(
                SimpleItemsRepository.observeLostItems(),
                SimpleItemsRepository.observeFoundItems()
            ) { lostItems, foundItems ->
                val userIdsFromItems = buildSet {
                    lostItems.mapNotNullTo(this) { it.reporterId.takeIf { id -> id.isNotBlank() } }
                    foundItems.mapNotNullTo(this) { it.reporterId.takeIf { id -> id.isNotBlank() } }
                    add(currentUserId)
                }
                val activeUsersCount = when {
                    activeUsersFromProfiles > 0 -> maxOf(activeUsersFromProfiles, userIdsFromItems.size)
                    userIdsFromItems.isNotEmpty() -> userIdsFromItems.size
                    else -> 1 // at least current signed-in user
                }

                val overviewLostItems = lostItems.size
                val overviewFoundItems = foundItems.size

                val returnedLost = lostItems.count { it.status == SimpleItemStatus.CLAIMED }
                val returnedFound = foundItems.count { it.status == SimpleItemStatus.CLAIMED }
                val overviewReturnedItems = returnedLost + returnedFound

                val reunitedByMonth = computeReunitedByMonthSimple(lostItems, foundItems)

                // Keep notificationCounts compatible, but focus on overview + graph.
                HomeScreenState(
                    notificationCounts = NotificationCounts(),
                    overviewLostItems = overviewLostItems,
                    overviewFoundItems = overviewFoundItems,
                    overviewReturnedItems = overviewReturnedItems,
                    overviewActiveUsers = activeUsersCount,
                    reunitedTotal = overviewReturnedItems,
                    reunitedByMonth = reunitedByMonth,
                    dismissedNotifications = emptySet(),
                    isLoading = false
                )
            }.collectLatest { newState ->
                // Preserve dismissedNotifications (if any) by merging.
                _state.update { old ->
                    newState.copy(dismissedNotifications = old.dismissedNotifications)
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

    private fun computeReunitedByMonthSimple(
        lostItems: List<SimpleLostItem>,
        foundItems: List<SimpleFoundItem>
    ): List<Pair<String, Int>> {
        val now = Calendar.getInstance()
        val monthBuckets = (4 downTo 0).map { offset ->
            val cal = (now.clone() as Calendar).apply { add(Calendar.MONTH, -offset) }
            Pair(
                SimpleDateFormat("MMM", Locale.getDefault()).format(cal.time),
                cal.get(Calendar.MONTH) to cal.get(Calendar.YEAR)
            )
        }

        return monthBuckets.map { (label, monthYear) ->
            val countLost = lostItems.count { lost ->
                if (lost.status != SimpleItemStatus.CLAIMED) return@count false
                val cal = Calendar.getInstance().apply { timeInMillis = lost.createdAt }
                cal.get(Calendar.MONTH) == monthYear.first && cal.get(Calendar.YEAR) == monthYear.second
            }
            val countFound = foundItems.count { found ->
                if (found.status != SimpleItemStatus.CLAIMED) return@count false
                val cal = Calendar.getInstance().apply { timeInMillis = found.createdAt }
                cal.get(Calendar.MONTH) == monthYear.first && cal.get(Calendar.YEAR) == monthYear.second
            }
            val count = countLost + countFound
            label to count
        }
    }
}
