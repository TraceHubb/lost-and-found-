package com.lostandfound.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lostandfound.data.models.ClaimStatus
import com.lostandfound.data.models.SimpleClaim
import com.lostandfound.data.models.SimpleFoundItem
import com.lostandfound.data.models.SimpleLostItem
import com.lostandfound.data.repositories.AuthRepository
import com.lostandfound.data.repositories.SimpleItemsRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.collectLatest

private val HistoryBackground = Color(0xFFF7F7F9)
private val CardWhite = Color(0xFFFFFFFF)
private val PrimaryPurple = Color(0xFF7C4DFF)
private val TextDark = Color(0xFF1B1B1F)
private val TextGray = Color(0xFF7A7A7E)
private val TabGray = Color(0xFFA6A6AB)

data class ClaimedItemRow(
    val claim: SimpleClaim,
    val title: String,
    val subtitle: String
)

private data class ReportRow(
    val type: String, // LOST or FOUND
    val title: String,
    val category: String,
    val locationText: String,
    val createdAt: Long
)

private enum class HistoryTab {
    REPORTS,
    CLAIMS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit
) {
    val userId = AuthRepository.currentUser?.uid.orEmpty()

    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    var reportedFound by remember { mutableStateOf<List<SimpleFoundItem>>(emptyList()) }
    var reportedLost by remember { mutableStateOf<List<SimpleLostItem>>(emptyList()) }
    var claimedRows by remember { mutableStateOf<List<ClaimedItemRow>>(emptyList()) }
    var selectedTab by remember { mutableStateOf(HistoryTab.REPORTS) }

    LaunchedEffect(userId) {
        if (userId.isBlank()) {
            error = "Please login to view history"
            isLoading = false
            return@LaunchedEffect
        }

        isLoading = true
        error = null

        SimpleItemsRepository.getReportedFoundItems(userId).collectLatest { items ->
            reportedFound = items
            if (isLoading) isLoading = false
        }
    }

    LaunchedEffect(userId) {
        if (userId.isBlank()) return@LaunchedEffect
        SimpleItemsRepository.getReportedLostItems(userId).collectLatest { items ->
            reportedLost = items
            if (isLoading) isLoading = false
        }
    }

    LaunchedEffect(userId) {
        if (userId.isBlank()) return@LaunchedEffect
        SimpleItemsRepository.getClaimsByClaimer(userId).collectLatest { claims ->
            val approvedOrPending = claims
                .filter { it.status == ClaimStatus.APPROVED || it.status == ClaimStatus.PENDING }

            claimedRows = approvedOrPending.map { claim ->
                val title = "Claim: ${claim.itemId}"
                val subtitle = "Status: ${claim.status.name}"
                ClaimedItemRow(claim = claim, title = title, subtitle = subtitle)
            }
            if (isLoading) isLoading = false
        }
    }

    val reports = remember(reportedFound, reportedLost) {
        val lostRows = reportedLost.map {
            ReportRow(
                type = "LOST",
                title = it.itemName.ifBlank { "Lost item" },
                category = it.category.ifBlank { "Other" },
                locationText = "Lost at: ${it.locationLost.ifBlank { "unknown location" }}",
                createdAt = it.createdAt
            )
        }
        val foundRows = reportedFound.map {
            ReportRow(
                type = "FOUND",
                title = it.itemName.ifBlank { "Found item" },
                category = it.category.ifBlank { "Other" },
                locationText = "Found at: ${it.locationFound.ifBlank { "unknown location" }}",
                createdAt = it.createdAt
            )
        }
        (lostRows + foundRows).sortedByDescending { it.createdAt }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HistoryBackground)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CardWhite,
            shadowElevation = 1.dp
        ) {
            Column {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = PrimaryPurple
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = PrimaryPurple,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "F",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                    Text(
                        text = "History",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    HistoryTabLabel(
                        title = "My Reports",
                        isSelected = selectedTab == HistoryTab.REPORTS,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedTab = HistoryTab.REPORTS }
                    )
                    HistoryTabLabel(
                        title = "My Claims",
                        isSelected = selectedTab == HistoryTab.CLAIMS,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedTab = HistoryTab.CLAIMS }
                    )
                }
            }
        }

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryPurple)
                }
            }

            error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = error ?: "Something went wrong", color = Color.Red)
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (selectedTab == HistoryTab.REPORTS) {
                        if (reports.isEmpty()) {
                            item { EmptyRow("No reports yet") }
                        } else {
                            items(reports) { report ->
                                ReportCard(report)
                            }
                        }
                    } else {
                        if (claimedRows.isEmpty()) {
                            item { EmptyRow("No claims yet") }
                        } else {
                            items(claimedRows) { claim ->
                                ClaimCard(claim)
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun HistoryTabLabel(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = if (isSelected) PrimaryPurple else TabGray,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(if (isSelected) PrimaryPurple else Color.Transparent)
        )
    }
}

@Composable
private fun EmptyRow(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = TextGray,
            fontStyle = FontStyle.Italic
        )
    }
}

@Composable
private fun ReportCard(row: ReportRow) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChip(row.type)
                Text(
                    text = formatDate(row.createdAt),
                    color = TextGray,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color(0xFFF0F0F2), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (row.type == "FOUND") Icons.Default.Check else Icons.Default.Search,
                        contentDescription = null,
                        tint = Color(0xFF8D8D93)
                    )
                }
                Spacer(modifier = Modifier.padding(horizontal = 10.dp))
                Column {
                    Text(row.title, color = TextDark, fontWeight = FontWeight.Bold, fontSize = 28.sp / 2)
                    Text(row.category, color = PrimaryPurple, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(row.locationText, color = TextGray, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun ClaimCard(row: ClaimedItemRow) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChip("CLAIM")
                Text(
                    text = formatDate(row.claim.createdAt),
                    color = TextGray,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color(0xFFF0F0F2), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF7BC47F)
                    )
                }
                Spacer(modifier = Modifier.padding(horizontal = 10.dp))
                Column {
                    Text("Claim ${row.claim.itemId.take(8)}", color = TextDark, fontWeight = FontWeight.Bold)
                    Text("Status: ${row.claim.status.name}", color = PrimaryPurple, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Submitted by ${row.claim.claimer_email}", color = TextGray, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun StatusChip(type: String) {
    val bg = when (type) {
        "FOUND" -> Color(0xFFE6F8EE)
        "LOST" -> Color(0xFFFDECEE)
        else -> Color(0xFFEDEBFF)
    }
    val fg = when (type) {
        "FOUND" -> Color(0xFF0F9D58)
        "LOST" -> Color(0xFFD84C6F)
        else -> PrimaryPurple
    }

    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(50))
            .border(1.dp, bg, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(type, color = fg, fontWeight = FontWeight.Bold, fontSize = 11.sp)
    }
}

private fun formatDate(ts: Long): String {
    return try {
        SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(ts))
    } catch (_: Exception) {
        ""
    }
}

@Composable
private fun _LegacyRowUnused(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String
) {
    Text(
        text = "$title $subtitle",
        color = iconTint
    )
}
