package com.calendariomagico.app.data.repository

import com.calendariomagico.app.data.local.ProfileStore
import com.calendariomagico.app.data.model.CalendarGroup
import com.calendariomagico.app.data.model.CalendarItem
import com.calendariomagico.app.data.model.Member
import com.calendariomagico.app.data.remote.AuthGateway
import com.calendariomagico.app.data.remote.CalendarBackend
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Single source of truth for the UI. Firestore (via [CalendarBackend]) already
 * keeps its own offline cache and resyncs automatically when connectivity
 * returns, so this class does not duplicate storage — it just exposes live
 * state and coordinates which shared calendar this device is currently in.
 */
class CalendarRepository(
    private val backend: CalendarBackend,
    private val authGateway: AuthGateway,
    private val profileStore: ProfileStore,
    private val externalScope: CoroutineScope
) {
    private val _items = MutableStateFlow<List<CalendarItem>>(emptyList())
    val items: StateFlow<List<CalendarItem>> = _items.asStateFlow()

    private val _members = MutableStateFlow<List<Member>>(emptyList())
    val members: StateFlow<List<Member>> = _members.asStateFlow()

    val activeCalendar: Flow<CalendarGroup?> = profileStore.activeCalendar
    val displayName: Flow<String> = profileStore.displayName

    private var itemsJob: Job? = null
    private var membersJob: Job? = null
    private var currentCalendarId: String? = null

    suspend fun start() {
        profileStore.activeCalendar.first()?.let { attachListeners(it.id) }
    }

    suspend fun createCalendar(calendarName: String, memberName: String): Result<CalendarGroup> =
        runCatching {
            require(calendarName.isNotBlank()) { "Dá um nome ao teu calendário." }
            require(memberName.isNotBlank()) { "Como te chamas?" }
            val uid = authGateway.ensureSignedIn()
            val group = backend.createCalendar(calendarName.trim(), uid, memberName.trim())
            profileStore.setActiveCalendar(group)
            profileStore.setDisplayName(memberName.trim())
            attachListeners(group.id)
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
            attachListeners(group.id)
            group
        }

    suspend fun leaveCalendar() {
        val group = profileStore.activeCalendar.first() ?: return
        authGateway.currentUid?.let { uid ->
            runCatching { backend.leaveCalendar(group.id, uid) }
        }
        detachListeners()
        profileStore.clearActiveCalendar()
    }

    suspend fun saveItem(item: CalendarItem): Result<CalendarItem> = runCatching {
        val calendarId = currentCalendarId ?: error("Sem calendário ativo.")
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
        val calendarId = currentCalendarId ?: return
        runCatching { backend.deleteItem(calendarId, itemId) }
    }

    suspend fun toggleDone(item: CalendarItem) {
        saveItem(item.copy(isDone = !item.isDone))
    }

    private fun attachListeners(calendarId: String) {
        if (currentCalendarId == calendarId && itemsJob?.isActive == true) return
        currentCalendarId = calendarId
        itemsJob?.cancel()
        membersJob?.cancel()
        itemsJob = externalScope.launch {
            backend.observeItems(calendarId).collect { _items.value = it }
        }
        membersJob = externalScope.launch {
            backend.observeMembers(calendarId).collect { _members.value = it }
        }
    }

    private fun detachListeners() {
        itemsJob?.cancel()
        membersJob?.cancel()
        currentCalendarId = null
        _items.value = emptyList()
        _members.value = emptyList()
    }
}
