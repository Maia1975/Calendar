package com.calendariomagico.app.data.model

import androidx.compose.ui.graphics.Color

enum class ItemColor(val firestoreValue: String, val light: Color, val dark: Color) {
    LAVANDA("lavanda", Color(0xFFD8C4F7), Color(0xFF6A4FA0)),
    MENTA("menta", Color(0xFFB6EBD3), Color(0xFF2E8B63)),
    PESSEGO("pessego", Color(0xFFFFD3B6), Color(0xFFC97A46)),
    CEU("ceu", Color(0xFFB9E4FB), Color(0xFF3E7EA6)),
    SOL("sol", Color(0xFFFFF0A8), Color(0xFFB89A1E));

    companion object {
        fun fromFirestore(value: String?): ItemColor =
            entries.firstOrNull { it.firestoreValue == value } ?: LAVANDA
    }
}
