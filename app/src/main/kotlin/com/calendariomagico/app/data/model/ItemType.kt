package com.calendariomagico.app.data.model

enum class ItemType(val firestoreValue: String, val emoji: String, val label: String) {
    EVENTO("evento", "🎉", "Evento"),
    TAREFA("tarefa", "✅", "Tarefa"),
    LEMBRETE("lembrete", "⏰", "Lembrete");

    companion object {
        fun fromFirestore(value: String?): ItemType =
            entries.firstOrNull { it.firestoreValue == value } ?: EVENTO
    }
}
