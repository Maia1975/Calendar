package com.calendariomagico.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.calendariomagico.app.data.model.CalendarGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.deviceProfileDataStore by preferencesDataStore(name = "device_profile")

/**
 * Small piece of state that lives only on this device: which shared calendar
 * it currently belongs to, and the name this family member picked. The
 * calendar's actual content always lives in Firestore, never here.
 */
class DeviceProfileStore(context: Context) : ProfileStore {
    private val dataStore = context.deviceProfileDataStore

    private object Keys {
        val CALENDAR_ID = stringPreferencesKey("calendar_id")
        val CALENDAR_NAME = stringPreferencesKey("calendar_name")
        val CALENDAR_PIN = stringPreferencesKey("calendar_pin")
        val CALENDAR_OWNER = stringPreferencesKey("calendar_owner")
        val CALENDAR_CREATED_AT = longPreferencesKey("calendar_created_at")
        val DISPLAY_NAME = stringPreferencesKey("display_name")
    }

    override val activeCalendar: Flow<CalendarGroup?> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            val id = prefs[Keys.CALENDAR_ID]
            if (id.isNullOrBlank()) return@map null
            CalendarGroup(
                id = id,
                name = prefs[Keys.CALENDAR_NAME].orEmpty(),
                pinCode = prefs[Keys.CALENDAR_PIN].orEmpty(),
                createdAt = prefs[Keys.CALENDAR_CREATED_AT] ?: 0L,
                ownerUid = prefs[Keys.CALENDAR_OWNER].orEmpty()
            )
        }

    override val displayName: Flow<String> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.DISPLAY_NAME].orEmpty() }

    override suspend fun setActiveCalendar(group: CalendarGroup) {
        dataStore.edit { prefs ->
            prefs[Keys.CALENDAR_ID] = group.id
            prefs[Keys.CALENDAR_NAME] = group.name
            prefs[Keys.CALENDAR_PIN] = group.pinCode
            prefs[Keys.CALENDAR_OWNER] = group.ownerUid
            prefs[Keys.CALENDAR_CREATED_AT] = group.createdAt
        }
    }

    override suspend fun clearActiveCalendar() {
        dataStore.edit { prefs ->
            prefs.remove(Keys.CALENDAR_ID)
            prefs.remove(Keys.CALENDAR_NAME)
            prefs.remove(Keys.CALENDAR_PIN)
            prefs.remove(Keys.CALENDAR_OWNER)
            prefs.remove(Keys.CALENDAR_CREATED_AT)
        }
    }

    override suspend fun setDisplayName(name: String) {
        dataStore.edit { prefs -> prefs[Keys.DISPLAY_NAME] = name }
    }
}
