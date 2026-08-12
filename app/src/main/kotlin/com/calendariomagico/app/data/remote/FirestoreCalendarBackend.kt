package com.calendariomagico.app.data.remote

import com.calendariomagico.app.data.model.CalendarGroup
import com.calendariomagico.app.data.model.CalendarItem
import com.calendariomagico.app.data.model.ItemColor
import com.calendariomagico.app.data.model.ItemType
import com.calendariomagico.app.data.model.Member
import com.calendariomagico.app.util.PinCodeGenerator
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val CALENDARS = "calendars"
private const val ITEMS = "items"
private const val MEMBERS = "members"
private const val MAX_PIN_ATTEMPTS = 25

class FirestoreCalendarBackend(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : CalendarBackend {

    override suspend fun findCalendarByPin(pin: String): CalendarGroup? {
        val normalized = PinCodeGenerator.normalize(pin)
        val snapshot = db.collection(CALENDARS)
            .whereEqualTo("pinCode", normalized)
            .limit(1)
            .get()
            .await()
        return snapshot.documents.firstOrNull()?.toCalendarGroup()
    }

    override suspend fun createCalendar(
        name: String,
        ownerUid: String,
        ownerDisplayName: String
    ): CalendarGroup {
        var pin = PinCodeGenerator.generate()
        var attempts = 0
        while (findCalendarByPin(pin) != null) {
            attempts++
            if (attempts >= MAX_PIN_ATTEMPTS) {
                error("Não foi possível gerar um código único. Tenta novamente.")
            }
            pin = PinCodeGenerator.generate()
        }

        val docRef = db.collection(CALENDARS).document()
        val createdAt = System.currentTimeMillis()
        val data = mapOf(
            "name" to name,
            "pinCode" to pin,
            "createdAt" to createdAt,
            "ownerUid" to ownerUid
        )
        docRef.set(data).await()
        joinCalendar(docRef.id, ownerUid, ownerDisplayName)
        return CalendarGroup(id = docRef.id, name = name, pinCode = pin, createdAt = createdAt, ownerUid = ownerUid)
    }

    override suspend fun joinCalendar(calendarId: String, uid: String, displayName: String) {
        val data = mapOf(
            "displayName" to displayName,
            "joinedAt" to System.currentTimeMillis(),
            "colorKey" to ItemColor.entries.random().firestoreValue
        )
        db.collection(CALENDARS).document(calendarId)
            .collection(MEMBERS).document(uid)
            .set(data)
            .await()
    }

    override suspend fun leaveCalendar(calendarId: String, uid: String) {
        db.collection(CALENDARS).document(calendarId)
            .collection(MEMBERS).document(uid)
            .delete()
            .await()
    }

    override fun observeItems(calendarId: String): Flow<List<CalendarItem>> = callbackFlow {
        val registration = db.collection(CALENDARS).document(calendarId)
            .collection(ITEMS)
            .orderBy("dateTimeMillis", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents.orEmpty().mapNotNull { it.toCalendarItem() }
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    override fun observeMembers(calendarId: String): Flow<List<Member>> = callbackFlow {
        val registration = db.collection(CALENDARS).document(calendarId)
            .collection(MEMBERS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val members = snapshot?.documents.orEmpty().mapNotNull { it.toMember() }
                trySend(members)
            }
        awaitClose { registration.remove() }
    }

    override suspend fun upsertItem(calendarId: String, item: CalendarItem): CalendarItem {
        val itemsRef = db.collection(CALENDARS).document(calendarId).collection(ITEMS)
        val docRef = if (item.id.isBlank()) itemsRef.document() else itemsRef.document(item.id)
        val toSave = item.copy(id = docRef.id, updatedAt = System.currentTimeMillis())
        docRef.set(toSave.toFirestoreMap()).await()
        return toSave
    }

    override suspend fun deleteItem(calendarId: String, itemId: String) {
        db.collection(CALENDARS).document(calendarId)
            .collection(ITEMS).document(itemId)
            .delete()
            .await()
    }

    override suspend fun fetchItemsOnce(calendarId: String): List<CalendarItem> {
        val snapshot = db.collection(CALENDARS).document(calendarId)
            .collection(ITEMS)
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toCalendarItem() }
    }
}

private fun DocumentSnapshot.toCalendarGroup(): CalendarGroup? {
    if (!exists()) return null
    return CalendarGroup(
        id = id,
        name = getString("name").orEmpty(),
        pinCode = getString("pinCode").orEmpty(),
        createdAt = getLong("createdAt") ?: 0L,
        ownerUid = getString("ownerUid").orEmpty()
    )
}

private fun DocumentSnapshot.toCalendarItem(): CalendarItem? {
    if (!exists()) return null
    return CalendarItem(
        id = id,
        type = ItemType.fromFirestore(getString("type")),
        title = getString("title").orEmpty(),
        notes = getString("notes").orEmpty(),
        dateTimeMillis = getLong("dateTimeMillis") ?: 0L,
        color = ItemColor.fromFirestore(getString("color")),
        isDone = getBoolean("isDone") ?: false,
        reminderMinutesBefore = getLong("reminderMinutesBefore")?.toInt(),
        createdBy = getString("createdBy").orEmpty(),
        authorName = getString("authorName").orEmpty(),
        updatedAt = getLong("updatedAt") ?: 0L
    )
}

private fun DocumentSnapshot.toMember(): Member? {
    if (!exists()) return null
    return Member(
        uid = id,
        displayName = getString("displayName").orEmpty(),
        joinedAt = getLong("joinedAt") ?: 0L,
        colorKey = ItemColor.fromFirestore(getString("colorKey"))
    )
}

private fun CalendarItem.toFirestoreMap(): Map<String, Any?> = mapOf(
    "type" to type.firestoreValue,
    "title" to title,
    "notes" to notes,
    "dateTimeMillis" to dateTimeMillis,
    "color" to color.firestoreValue,
    "isDone" to isDone,
    "reminderMinutesBefore" to reminderMinutesBefore,
    "createdBy" to createdBy,
    "authorName" to authorName,
    "updatedAt" to updatedAt
)
