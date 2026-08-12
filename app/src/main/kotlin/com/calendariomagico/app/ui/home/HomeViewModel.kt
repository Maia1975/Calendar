package com.calendariomagico.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.calendariomagico.app.data.model.CalendarItem
import com.calendariomagico.app.data.repository.CalendarRepository
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val month: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val items: List<CalendarItem> = emptyList(),
    val calendarName: String = "",
    val pinCode: String = ""
) {
    val itemsForSelectedDate: List<CalendarItem>
        get() = itemsOn(selectedDate)

    fun itemsOn(date: LocalDate): List<CalendarItem> =
        items
            .filter { com.calendariomagico.app.util.DateUtils.millisToLocalDate(it.dateTimeMillis) == date }
            .sortedBy { it.dateTimeMillis }
}

class HomeViewModel(private val repository: CalendarRepository) : ViewModel() {
    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val month = MutableStateFlow(YearMonth.now())

    val uiState: StateFlow<HomeUiState> = combine(
        repository.items,
        selectedDate,
        month,
        repository.activeCalendar
    ) { items, date, currentMonth, group ->
        HomeUiState(
            month = currentMonth,
            selectedDate = date,
            items = items,
            calendarName = group?.name.orEmpty(),
            pinCode = group?.pinCode.orEmpty()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
        month.value = YearMonth.from(date)
    }

    fun goToNextMonth() {
        month.value = month.value.plusMonths(1)
    }

    fun goToPreviousMonth() {
        month.value = month.value.minusMonths(1)
    }

    fun goToToday() {
        selectedDate.value = LocalDate.now()
        month.value = YearMonth.now()
    }

    fun toggleDone(item: CalendarItem) {
        viewModelScope.launch { repository.toggleDone(item) }
    }

    fun deleteItem(item: CalendarItem) {
        viewModelScope.launch { repository.deleteItem(item.id) }
    }

    fun saveItem(item: CalendarItem, onResult: (success: Boolean, error: String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.saveItem(item)
            onResult(result.isSuccess, result.exceptionOrNull()?.message)
        }
    }

    companion object {
        fun factory(repository: CalendarRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeViewModel(repository) as T
        }
    }
}
