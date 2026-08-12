package com.calendariomagico.app.data.remote

import com.calendariomagico.app.data.model.CalendarGroup
import com.calendariomagico.app.data.model.CalendarItem
import com.calendariomagico.app.data.model.Member
import kotlinx.coroutines.flow.Flow

/**
 * Abstraction over the real-time backend so the repository and its tests
 * never need to depend on the concrete Firestore SDK.
 */
interface CalendarBackend {
    suspend fun findCalendarByPin(pin: String): CalendarGroup?
    suspend fun createCalendar(name: String, ownerUid: String, ownerDisplayName: String): CalendarGroup
    suspend fun joinCalendar(calendarId: String, uid: String, displayName: String)
    suspend fun leaveCalendar(calendarId: String, uid: String)
    fun observeItems(calendarId: String): Flow<List<CalendarItem>>
    fun observeMembers(calendarId: String): Flow<List<Member>>
    suspend fun upsertItem(calendarId: String, item: CalendarItem): CalendarItem
    suspend fun deleteItem(calendarId: String, itemId: String)
    suspend fun fetchItemsOnce(calendarId: String): List<CalendarItem>
}
