package com.calendariomagico.app.testutil

import com.calendariomagico.app.data.local.ProfileStore
import com.calendariomagico.app.data.model.CalendarGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeProfileStore : ProfileStore {
    private val calendarFlow = MutableStateFlow<CalendarGroup?>(null)
    private val nameFlow = MutableStateFlow("")

    override val activeCalendar = calendarFlow.asStateFlow()
    override val displayName = nameFlow.asStateFlow()

    override suspend fun setActiveCalendar(group: CalendarGroup) {
        calendarFlow.value = group
    }

    override suspend fun clearActiveCalendar() {
        calendarFlow.value = null
    }

    override suspend fun setDisplayName(name: String) {
        nameFlow.value = name
    }
}
