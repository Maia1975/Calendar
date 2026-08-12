package com.calendariomagico.app.testutil

import com.calendariomagico.app.data.model.CalendarGroup
import com.calendariomagico.app.data.model.CalendarItem
import com.calendariomagico.app.data.model.Member
import com.calendariomagico.app.data.remote.CalendarBackend
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeCalendarBackend : CalendarBackend {
    private val calendars = mutableMapOf<String, CalendarGroup>()
    private val itemsByCalendar = mutableMapOf<String, MutableStateFlow<List<CalendarItem>>>()
    private val membersByCalendar = mutableMapOf<String, MutableStateFlow<List<Member>>>()
    var nextId = 0
    var failNextUpsert: Throwable? = null

    private fun itemsFlow(calendarId: String) =
        itemsByCalendar.getOrPut(calendarId) { MutableStateFlow(emptyList()) }

    private fun membersFlow(calendarId: String) =
        membersByCalendar.getOrPut(calendarId) { MutableStateFlow(emptyList()) }

    override suspend fun findCalendarByPin(pin: String): CalendarGroup? =
        calendars.values.firstOrNull { it.pinCode.equals(pin.trim().uppercase(), ignoreCase = true) }

    override suspend fun createCalendar(name: String, ownerUid: String, ownerDisplayName: String): CalendarGroup {
        val id = "cal-${nextId++}"
        val group = CalendarGroup(id = id, name = name, pinCode = "PIN$id".uppercase(), createdAt = 0L, ownerUid = ownerUid)
        calendars[id] = group
        joinCalendar(id, ownerUid, ownerDisplayName)
        return group
    }

    override suspend fun joinCalendar(calendarId: String, uid: String, displayName: String) {
        val flow = membersFlow(calendarId)
        val updated = flow.value.filterNot { it.uid == uid } + Member(uid = uid, displayName = displayName)
        flow.value = updated
    }

    override suspend fun leaveCalendar(calendarId: String, uid: String) {
        val flow = membersFlow(calendarId)
        flow.value = flow.value.filterNot { it.uid == uid }
    }

    override fun observeItems(calendarId: String): Flow<List<CalendarItem>> = itemsFlow(calendarId).asStateFlow()

    override fun observeMembers(calendarId: String): Flow<List<Member>> = membersFlow(calendarId).asStateFlow()

    override suspend fun upsertItem(calendarId: String, item: CalendarItem): CalendarItem {
        failNextUpsert?.let {
            failNextUpsert = null
            throw it
        }
        val id = item.id.ifBlank { "item-${nextId++}" }
        val saved = item.copy(id = id)
        val flow = itemsFlow(calendarId)
        flow.value = flow.value.filterNot { it.id == id } + saved
        return saved
    }

    override suspend fun deleteItem(calendarId: String, itemId: String) {
        val flow = itemsFlow(calendarId)
        flow.value = flow.value.filterNot { it.id == itemId }
    }

    override suspend fun fetchItemsOnce(calendarId: String): List<CalendarItem> = itemsFlow(calendarId).value
}
