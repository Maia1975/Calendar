package com.calendariomagico.app.data.model

data class CalendarItem(
    val id: String = "",
    val type: ItemType = ItemType.EVENTO,
    val title: String = "",
    val notes: String = "",
    val dateTimeMillis: Long = 0L,
    val color: ItemColor = ItemColor.LAVANDA,
    val isDone: Boolean = false,
    val reminderMinutesBefore: Int? = null,
    val createdBy: String = "",
    val authorName: String = "",
    val updatedAt: Long = 0L
) {
    val hasReminder: Boolean get() = reminderMinutesBefore != null
}
