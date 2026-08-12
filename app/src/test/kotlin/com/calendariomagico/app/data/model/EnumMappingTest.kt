package com.calendariomagico.app.data.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class EnumMappingTest {

    @Test
    fun `every ItemType round trips through its firestore value`() {
        ItemType.entries.forEach { type ->
            assertThat(ItemType.fromFirestore(type.firestoreValue)).isEqualTo(type)
        }
    }

    @Test
    fun `unknown firestore value for ItemType falls back to EVENTO`() {
        assertThat(ItemType.fromFirestore("nao-existe")).isEqualTo(ItemType.EVENTO)
        assertThat(ItemType.fromFirestore(null)).isEqualTo(ItemType.EVENTO)
    }

    @Test
    fun `every ItemColor round trips through its firestore value`() {
        ItemColor.entries.forEach { color ->
            assertThat(ItemColor.fromFirestore(color.firestoreValue)).isEqualTo(color)
        }
    }

    @Test
    fun `unknown firestore value for ItemColor falls back to LAVANDA`() {
        assertThat(ItemColor.fromFirestore("nao-existe")).isEqualTo(ItemColor.LAVANDA)
        assertThat(ItemColor.fromFirestore(null)).isEqualTo(ItemColor.LAVANDA)
    }
}
