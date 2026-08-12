package com.calendariomagico.app.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.calendariomagico.app.data.model.CalendarItem
import com.calendariomagico.app.util.DateUtils
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun MonthGrid(
    month: YearMonth,
    selectedDate: LocalDate,
    itemsByDay: (LocalDate) -> List<CalendarItem>,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val days = DateUtils.monthGrid(month)
    val today = LocalDate.now()

    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            DateUtils.weekDayLabels().forEach { label ->
                Text(
                    label,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        for (week in days.chunked(7)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (day in week) {
                    DayCell(
                        date = day,
                        isCurrentMonth = day.month == month.month,
                        isSelected = day == selectedDate,
                        isToday = day == today,
                        dotColors = itemsByDay(day).map { it.color.light }.distinct().take(3),
                        onClick = { onDayClick(day) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    isCurrentMonth: Boolean,
    isSelected: Boolean,
    isToday: Boolean,
    dotColors: List<androidx.compose.ui.graphics.Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(3.dp)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(16.dp),
            color = when {
                isSelected -> MaterialTheme.colorScheme.primary
                isToday -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surface
            },
            modifier = Modifier.fillMaxWidth().aspectRatio(1f)
        ) {
            Column(
                modifier = Modifier.padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isSelected -> MaterialTheme.colorScheme.onPrimary
                        !isCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                if (dotColors.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        dotColors.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.onPrimary else color,
                                        CircleShape
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}
