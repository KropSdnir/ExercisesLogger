// File: app/src/main/java/com/example/exerciseslogger/ui/screens/home/CalendarView.kt
// Timestamp: Updated on 2025-08-22 00:03:19
// Scope: Adds a missing import for RowScope to resolve the build error.

package com.example.exerciseslogger.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun SimpleCalendarView(selectedDate: LocalDate) {
    val today = LocalDate.now()
    val displayText = if (selectedDate == today) {
        "Today ${selectedDate.format(DateTimeFormatter.ofPattern("M/d/yy"))}"
    } else {
        selectedDate.format(DateTimeFormatter.ofPattern("EEEE M/d/yy"))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Text(
            text = displayText,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun FullCalendarView(
    lazyListState: LazyListState,
    months: List<YearMonth>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
) {
    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        items(months.size) { index ->
            MonthView(
                yearMonth = months[index],
                selectedDate = selectedDate,
                onDateSelected = onDateSelected
            )
            if (index < months.size - 1) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun MonthView(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val daysInMonth = (1..yearMonth.lengthOfMonth()).map { it.toString() }
    val firstDayOfWeek = yearMonth.atDay(1).dayOfWeek.value % 7
    val emptyCells = List(firstDayOfWeek) { "" }
    val allCells = (emptyCells + daysInMonth).chunked(7)

    Card(elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                DayOfWeek.values().forEach { day ->
                    Text(
                        text = day.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))

            Column {
                allCells.forEach { week ->
                    Row {
                        week.forEach { day ->
                            DayCell(day = day, yearMonth = yearMonth, selectedDate = selectedDate, onDateSelected = onDateSelected)
                        }
                        if (week.size < 7) {
                            for (i in 1..(7 - week.size)) {
                                Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.DayCell(
    day: String,
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
    ) {
        if (day.isNotEmpty()) {
            val date = yearMonth.atDay(day.toInt())
            val isSelected = date == selectedDate
            Text(
                text = day,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                    .clickable { onDateSelected(date) }
                    .padding(8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}