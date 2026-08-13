package com.calendariomagico.app.data.repository

import com.calendariomagico.app.data.local.ProfileStore
import com.calendariomagico.app.data.model.CalendarGroup
import com.calendariomagico.app.data.model.CalendarItem
import com.calendariomagico.app.data.model.Member
import com.calendariomagico.app.data.remote.AuthGateway
import com.calendariomagico.app.data.remote.CalendarBackend
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

/**
 * Single source of truth for the UI. Firestore (via [CalendarBackend]) already
 * keeps its own offline cache and resyncs automatically when connectivity
 * returns, so this class does not duplicate storage — it just exposes live
 * state derived from whichever shared calendar this device currently belongs
 * to, switching automatically whenever that changes.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CalendarRepository(
    private val backend: CalendarBackend,
    private val authGateway: AuthGateway,
    private val profileStore: ProfileStore,
    private val externalScope: CoroutineScope
) {
    val activeCalendar: Flow<CalendarGroup?> = profileStore.activeCalendar
    val displayName: Flow<String> = profileStore.displayName

    val items: StateFlow<List<CalendarItem>> = activeCalendar
        .flatMapLatest { group -> group?.let { backend.observeItems(it.id) } ?: flowOf(emptyList()) }
        .stateIn(externalScope, SharingStarted.Eagerly, emptyList())

    val members: StateFlow<List<Member>> = activeCalendar
        .flatMapLatest { group -> group?.let { backend.observeMembers(it.id) } ?: flowOf(emptyList()) }
        .stateIn(externalScope, SharingStarted.Eagerly, emptyList())

    suspend fun createCalendar(calendarName: String, memberName: String): Result<CalendarGroup> =
        runCatching {
            require(calendarName.isNotBlank()) { "Dá um nome ao teu calendário." }
            require(memberName.isNotBlank()) { "Como te chamas?" }
            val uid = authGateway.ensureSignedIn()
            val group = backend.createCalendar(calendarName.trim(), uid, memberName.trim())
            profileStore.setActiveCalendar(group)
            profileStore.setDisplayName(memberName.trim())
            group
        }

    suspend fun joinCalendar(pin: String, memberName: String): Result<CalendarGroup> =
        runCatching {
            require(memberName.isNotBlank()) { "Como te chamas?" }
            val uid = authGateway.ensureSignedIn()
            val group = backend.findCalendarByPin(pin)
                ?: error("Não encontrámos nenhum calendário com esse código. Confirma e tenta outra vez.")
            backend.joinCalendar(group.id, uid, memberName.trim())
            profileStore.setActiveCalendar(group)
            profileStore.setDisplayName(memberName.trim())
            group
        }

    suspend fun leaveCalendar() {
        val group = profileStore.activeCalendar.first() ?: return
        authGateway.currentUid?.let { uid ->
            runCatching { backend.leaveCalendar(group.id, uid) }
        }
        profileStore.clearActiveCalendar()
    }

    suspend fun saveItem(item: CalendarItem): Result<CalendarItem> = runCatching {
        val calendarId = profileStore.activeCalendar.first()?.id ?: error("Sem calendário ativo.")
        require(item.title.isNotBlank()) { "Dá um título a este item." }
        val uid = authGateway.ensureSignedIn()
        val name = profileStore.displayName.first()
        val toSave = item.copy(
            createdBy = item.createdBy.ifBlank { uid },
            authorName = item.authorName.ifBlank { name }
        )
        backend.upsertItem(calendarId, toSave)
    }

    suspend fun deleteItem(itemId: String) {
        val calendarId = profileStore.activeCalendar.first()?.id ?: return
        runCatching { backend.deleteItem(calendarId, itemId) }
    }

    suspend fun toggleDone(item: CalendarItem) {
        saveItem(item.copy(isDone = !item.isDone))
    }
}
