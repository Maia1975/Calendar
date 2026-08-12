package com.calendariomagico.app.data.model

data class CalendarGroup(
    val id: String = "",
    val name: String = "",
    val pinCode: String = "",
    val createdAt: Long = 0L,
    val ownerUid: String = ""
)

data class Member(
    val uid: String = "",
    val displayName: String = "",
    val joinedAt: Long = 0L,
    val colorKey: ItemColor = ItemColor.LAVANDA
)
