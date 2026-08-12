package com.calendariomagico.app.data.local

import androidx.test.core.app.ApplicationProvider
import com.calendariomagico.app.data.model.CalendarGroup
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DeviceProfileStoreTest {

    private fun newStore() = DeviceProfileStore(ApplicationProvider.getApplicationContext())

    @Test
    fun `starts with no active calendar`() = runTest {
        val store = newStore()
        assertThat(store.activeCalendar.first()).isNull()
    }

    @Test
    fun `setActiveCalendar persists the group`() = runTest {
        val store = newStore()
        val group = CalendarGroup(id = "cal-1", name = "Família Silva", pinCode = "AB12CD", createdAt = 123L, ownerUid = "uid-1")

        store.setActiveCalendar(group)

        assertThat(store.activeCalendar.first()).isEqualTo(group)
    }

    @Test
    fun `clearActiveCalendar removes it`() = runTest {
        val store = newStore()
        store.setActiveCalendar(CalendarGroup(id = "cal-1", name = "Família Silva", pinCode = "AB12CD"))

        store.clearActiveCalendar()

        assertThat(store.activeCalendar.first()).isNull()
    }

    @Test
    fun `display name is persisted independently of the calendar`() = runTest {
        val store = newStore()
        store.setDisplayName("Maria")
        assertThat(store.displayName.first()).isEqualTo("Maria")

        store.clearActiveCalendar()
        assertThat(store.displayName.first()).isEqualTo("Maria")
    }
}
