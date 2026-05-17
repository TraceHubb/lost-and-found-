package com.lostandfound.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YesNoAnswerSelector(
    answer: Boolean?,
    onAnswerSelected: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FilterChip(
            selected = answer == true,
            onClick = { onAnswerSelected(true) },
            label = { Text("Yes") }
        )
        FilterChip(
            selected = answer == false,
            onClick = { onAnswerSelected(false) },
            label = { Text("No") }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YesNoQuestionCard(
    questionNumber: Int,
    question: String,
    answer: Boolean?,
    onAnswerSelected: (Boolean) -> Unit,
    accentColor: Color = Color(0xFFD97706)
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Question $questionNumber:",
                style = MaterialTheme.typography.labelMedium,
                color = accentColor,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = question,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF1F2937)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Your answer (Yes or No only)",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF92400E)
            )
            Spacer(modifier = Modifier.height(8.dp))
            YesNoAnswerSelector(answer = answer, onAnswerSelected = onAnswerSelected)
        }
    }
}

@Composable
fun ReportYesNoAnswerRow(
    answerNumber: Int,
    answer: Boolean?,
    onAnswerSelected: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Answer $answerNumber (Yes or No only)",
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF92400E)
        )
        Spacer(modifier = Modifier.height(8.dp))
        YesNoAnswerSelector(answer = answer, onAnswerSelected = onAnswerSelected)
    }
}
