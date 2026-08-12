package com.calendariomagico.app.data.repository

import app.cash.turbine.test
import com.calendariomagico.app.data.model.CalendarItem
import com.calendariomagico.app.data.model.ItemType
import com.calendariomagico.app.testutil.FakeAuthGateway
import com.calendariomagico.app.testutil.FakeCalendarBackend
import com.calendariomagico.app.testutil.FakeProfileStore
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CalendarRepositoryTest {

    private fun buildRepository(scope: TestScope): CalendarRepository = CalendarRepository(
        backend = FakeCalendarBackend(),
        authGateway = FakeAuthGateway(),
        profileStore = FakeProfileStore(),
        externalScope = scope.backgroundScope
    )

    @Test
    fun `createCalendar succeeds and stores the new group`() = runTest {
        val repository = buildRepository(this)

        val result = repository.createCalendar("Família Silva", "Maria")
        advanceUntilIdle()

        assertThat(result.isSuccess).isTrue()
        val group = result.getOrThrow()
        assertThat(group.name).isEqualTo("Família Silva")
        assertThat(group.pinCode).isNotEmpty()

        repository.activeCalendar.test {
            assertThat(awaitItem()?.id).isEqualTo(group.id)
        }
    }

    @Test
    fun `createCalendar fails with a blank calendar name`() = runTest {
        val repository = buildRepository(this)
        val result = repository.createCalendar("   ", "Maria")
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `joinCalendar fails when the pin does not exist`() = runTest {
        val repository = buildRepository(this)
        val result = repository.joinCalendar("ZZZZZZ", "João")
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `joinCalendar succeeds with a valid pin and adds the member`() = runTest {
        // Owner and guest are two different devices that must share the same
        // backend (their common Firestore project) but have their own auth
        // and on-device profile, same as in real life.
        val sharedBackend = FakeCalendarBackend()
        val owner = CalendarRepository(sharedBackend, FakeAuthGateway("uid-owner"), FakeProfileStore(), backgroundScope)
        val guest = CalendarRepository(sharedBackend, FakeAuthGateway("uid-guest"), FakeProfileStore(), backgroundScope)

        val ownerGroup = owner.createCalendar("Família Silva", "Maria").getOrThrow()
        advanceUntilIdle()

        val result = guest.joinCalendar(ownerGroup.pinCode, "João")
        advanceUntilIdle()

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow().id).isEqualTo(ownerGroup.id)
    }

    @Test
    fun `saveItem fails when there is no active calendar`() = runTest {
        val repository = buildRepository(this)
        val result = repository.saveItem(CalendarItem(title = "Consulta"))
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("calendário")
    }

    @Test
    fun `saveItem succeeds after joining a calendar and is reflected in items`() = runTest {
        val repository = buildRepository(this)
        repository.createCalendar("Família Silva", "Maria")
        advanceUntilIdle()

        val saveResult = repository.saveItem(
            CalendarItem(type = ItemType.TAREFA, title = "Arrumar o quarto", dateTimeMillis = 1_000L)
        )
        advanceUntilIdle()

        assertThat(saveResult.isSuccess).isTrue()
        assertThat(repository.items.value.map { it.title }).contains("Arrumar o quarto")
    }

    @Test
    fun `toggleDone flips the isDone flag`() = runTest {
        val repository = buildRepository(this)
        repository.createCalendar("Família Silva", "Maria")
        advanceUntilIdle()
        val saved = repository.saveItem(
            CalendarItem(type = ItemType.TAREFA, title = "Lição de casa", dateTimeMillis = 1_000L)
        ).getOrThrow()
        advanceUntilIdle()

        repository.toggleDone(saved)
        advanceUntilIdle()

        assertThat(repository.items.value.first { it.id == saved.id }.isDone).isTrue()
    }

    @Test
    fun `deleteItem removes it from the items list`() = runTest {
        val repository = buildRepository(this)
        repository.createCalendar("Família Silva", "Maria")
        advanceUntilIdle()
        val saved = repository.saveItem(
            CalendarItem(title = "Aniversário", dateTimeMillis = 1_000L)
        ).getOrThrow()
        advanceUntilIdle()

        repository.deleteItem(saved.id)
        advanceUntilIdle()

        assertThat(repository.items.value.any { it.id == saved.id }).isFalse()
    }

    @Test
    fun `leaveCalendar clears the active calendar`() = runTest {
        val repository = buildRepository(this)
        repository.createCalendar("Família Silva", "Maria")
        advanceUntilIdle()

        repository.leaveCalendar()
        advanceUntilIdle()

        repository.activeCalendar.test {
            assertThat(awaitItem()).isNull()
        }
    }
}
