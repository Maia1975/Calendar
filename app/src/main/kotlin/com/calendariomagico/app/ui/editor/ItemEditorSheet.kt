package com.calendariomagico.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.calendariomagico.app.data.model.CalendarItem
import com.calendariomagico.app.data.model.ItemColor
import com.calendariomagico.app.data.model.ItemType
import com.calendariomagico.app.util.DateUtils
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

private val reminderOptions = listOf(0, 10, 30, 60, 24 * 60)
private fun reminderLabel(minutes: Int) = when (minutes) {
    0 -> "Na hora"
    in 1..59 -> "$minutes min antes"
    in 60..1439 -> "${minutes / 60} h antes"
    else -> "1 dia antes"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemEditorSheet(
    initialItem: CalendarItem?,
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onSave: (CalendarItem) -> Unit,
    onDelete: (() -> Unit)?
) {
    var type by rememberSaveable { mutableStateOf(initialItem?.type ?: ItemType.EVENTO) }
    var title by rememberSaveable { mutableStateOf(initialItem?.title ?: "") }
    var notes by rememberSaveable { mutableStateOf(initialItem?.notes ?: "") }
    var color by rememberSaveable { mutableStateOf(initialItem?.color ?: ItemColor.entries.random()) }
    var date by rememberSaveable {
        mutableStateOf(initialItem?.let { DateUtils.millisToLocalDate(it.dateTimeMillis) } ?: initialDate)
    }
    var time by rememberSaveable {
        mutableStateOf(initialItem?.let { DateUtils.millisToLocalDateTime(it.dateTimeMillis).toLocalTime() } ?: LocalTime.of(9, 0))
    }
    var reminderEnabled by rememberSaveable { mutableStateOf(initialItem?.hasReminder ?: false) }
    var reminderMinutes by rememberSaveable { mutableStateOf(initialItem?.reminderMinutesBefore ?: 30) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                if (initialItem == null) "Novo item ✨" else "Editar item",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ItemType.entries.forEach { option ->
                    FilterChip(
                        selected = type == option,
                        onClick = { type = option },
                        label = { Text("${option.emoji} ${option.label}") }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notas (opcional)") },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.weight(1f)) {
                    Text("📅 " + DateUtils.shortDateLabel(DateUtils.combine(date, time)))
                }
                OutlinedButton(onClick = { showTimePicker = true }, modifier = Modifier.weight(1f)) {
                    Text("🕒 " + time.toString())
                }
            }
            Spacer(Modifier.height(20.dp))

            Text("Cor", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ItemColor.entries.forEach { option ->
                    ColorDot(
                        color = option,
                        selected = color == option,
                        onClick = { color = option }
                    )
                }
            }
            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Lembrete ⏰", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
            }
            if (reminderEnabled) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    reminderOptions.forEach { minutes ->
                        FilterChip(
                            selected = reminderMinutes == minutes,
                            onClick = { reminderMinutes = minutes },
                            label = { Text(reminderLabel(minutes)) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (onDelete != null) {
                    TextButton(onClick = onDelete, modifier = Modifier.weight(1f)) {
                        Text("Apagar", color = MaterialTheme.colorScheme.error)
                    }
                }
                Button(
                    onClick = {
                        val saved = (initialItem ?: CalendarItem()).copy(
                            type = type,
                            title = title,
                            notes = notes,
                            dateTimeMillis = DateUtils.combine(date, time),
                            color = color,
                            reminderMinutesBefore = if (reminderEnabled) reminderMinutes else null
                        )
                        onSave(saved)
                    },
                    enabled = title.isNotBlank(),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(2f)
                ) {
                    Text("Guardar")
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        date = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } }
        ) {
            DatePicker(state = state)
        }
    }

    if (showTimePicker) {
        val state = rememberTimePickerState(initialHour = time.hour, initialMinute = time.minute, is24Hour = true)
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(28.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(state = state)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") }
                    Button(onClick = {
                        time = LocalTime.of(state.hour, state.minute)
                        showTimePicker = false
                    }) { Text("OK") }
                }
            }
        }
    }
}

@Composable
private fun ColorDot(color: ItemColor, selected: Boolean, onClick: () -> Unit) {
    androidx.compose.material3.Surface(
        onClick = onClick,
        shape = CircleShape,
        color = color.light,
        border = if (selected) androidx.compose.foundation.BorderStroke(3.dp, color.dark) else null,
        modifier = Modifier.size(if (selected) 40.dp else 32.dp)
    ) {}
}
