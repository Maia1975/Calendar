package com.calendariomagico.app.ui.home

import com.calendariomagico.app.data.model.CalendarItem
import com.calendariomagico.app.data.model.ItemType
import com.calendariomagico.app.data.repository.CalendarRepository
import com.calendariomagico.app.testutil.FakeAuthGateway
import com.calendariomagico.app.testutil.FakeCalendarBackend
import com.calendariomagico.app.testutil.FakeProfileStore
import com.calendariomagico.app.util.DateUtils
import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildRepository(scope: kotlinx.coroutines.test.TestScope): CalendarRepository =
        CalendarRepository(
            backend = FakeCalendarBackend(),
            authGateway = FakeAuthGateway(),
            profileStore = FakeProfileStore(),
            externalScope = scope.backgroundScope
        )

    @Test
    fun `changing month updates the exposed state`() = runTest {
        val repository = buildRepository(this)
        val viewModel = HomeViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        val startMonth = viewModel.uiState.value.month
        viewModel.goToNextMonth()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.month).isEqualTo(startMonth.plusMonths(1))
    }

    @Test
    fun `goToToday resets month and selected date`() = runTest {
        val repository = buildRepository(this)
        val viewModel = HomeViewModel(repository)
        viewModel.selectDate(LocalDate.now().plusMonths(3))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.goToToday()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.selectedDate).isEqualTo(LocalDate.now())
        assertThat(viewModel.uiState.value.month).isEqualTo(YearMonth.now())
    }

    @Test
    fun `itemsForSelectedDate only returns items on that day`() = runTest {
        val repository = buildRepository(this)
        repository.createCalendar("Família Silva", "Maria")
        testDispatcher.scheduler.advanceUntilIdle()

        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)
        repository.saveItem(
            CalendarItem(type = ItemType.EVENTO, title = "Hoje", dateTimeMillis = DateUtils.combine(today, LocalTime.NOON))
        )
        repository.saveItem(
            CalendarItem(type = ItemType.EVENTO, title = "Amanhã", dateTimeMillis = DateUtils.combine(tomorrow, LocalTime.NOON))
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val viewModel = HomeViewModel(repository)
        viewModel.selectDate(today)
        testDispatcher.scheduler.advanceUntilIdle()

        val titles = viewModel.uiState.value.itemsForSelectedDate.map { it.title }
        assertThat(titles).containsExactly("Hoje")
    }

    @Test
    fun `toggleDone updates the underlying item`() = runTest {
        val repository = buildRepository(this)
        repository.createCalendar("Família Silva", "Maria")
        testDispatcher.scheduler.advanceUntilIdle()
        val saved = repository.saveItem(
            CalendarItem(type = ItemType.TAREFA, title = "Arrumar", dateTimeMillis = DateUtils.combine(LocalDate.now(), LocalTime.NOON))
        ).getOrThrow()
        testDispatcher.scheduler.advanceUntilIdle()

        val viewModel = HomeViewModel(repository)
        viewModel.toggleDone(saved)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.items.first { it.id == saved.id }.isDone).isTrue()
    }
}
