package com.calendariomagico.app.data.local

import com.calendariomagico.app.data.model.CalendarGroup
import kotlinx.coroutines.flow.Flow

/**
 * On-device state: which shared calendar this phone currently belongs to,
 * and the name this family member picked. Kept separate from [CalendarBackend][
 * com.calendariomagico.app.data.remote.CalendarBackend] so the repository can
 * be unit tested without a real DataStore/Context.
 */
interface ProfileStore {
    val activeCalendar: Flow<CalendarGroup?>
    val displayName: Flow<String>
    suspend fun setActiveCalendar(group: CalendarGroup)
    suspend fun clearActiveCalendar()
    suspend fun setDisplayName(name: String)
}
