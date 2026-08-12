package com.calendariomagico.app.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.calendariomagico.app.data.model.CalendarItem
import com.calendariomagico.app.data.model.ItemType
import com.calendariomagico.app.util.DateUtils

@Composable
fun ItemCard(
    item: CalendarItem,
    onClick: () -> Unit,
    onToggleDone: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = item.color.light.copy(alpha = if (item.isDone) 0.35f else 0.55f),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item.type == ItemType.TAREFA) {
                Checkbox(checked = item.isDone, onCheckedChange = { onToggleDone() })
            } else {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(50))
                        .background(item.color.dark)
                )
                androidx.compose.foundation.layout.Spacer(Modifier.size(12.dp))
            }

            Column(modifier = Modifier.weight(1f).padding(start = 6.dp)) {
                Text(
                    "${item.type.emoji} ${item.title}",
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (item.isDone) TextDecoration.LineThrough else null,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val subtitle = buildString {
                    append(DateUtils.timeLabel(item.dateTimeMillis))
                    if (item.hasReminder) append("  ⏰ lembrete")
                    if (item.authorName.isNotBlank()) append("  · ${item.authorName}")
                }
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (item.notes.isNotBlank()) {
                    Text(
                        item.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Apagar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
