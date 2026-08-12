package com.calendariomagico.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.calendariomagico.app.data.repository.CalendarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class OnboardingMode { CREATE, JOIN }

data class OnboardingUiState(
    val mode: OnboardingMode = OnboardingMode.CREATE,
    val yourName: String = "",
    val calendarName: String = "",
    val pinInput: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val canSubmit: Boolean
        get() = yourName.isNotBlank() &&
            if (mode == OnboardingMode.CREATE) calendarName.isNotBlank() else pinInput.length == 6
}

class OnboardingViewModel(private val repository: CalendarRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onModeChange(mode: OnboardingMode) = _uiState.update { it.copy(mode = mode, errorMessage = null) }
    fun onYourNameChange(value: String) = _uiState.update { it.copy(yourName = value, errorMessage = null) }
    fun onCalendarNameChange(value: String) = _uiState.update { it.copy(calendarName = value, errorMessage = null) }
    fun onPinChange(value: String) {
        val cleaned = value.uppercase().filter { it.isLetterOrDigit() }.take(6)
        _uiState.update { it.copy(pinInput = cleaned, errorMessage = null) }
    }

    fun submit() {
        val state = _uiState.value
        if (!state.canSubmit || state.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = if (state.mode == OnboardingMode.CREATE) {
                repository.createCalendar(state.calendarName, state.yourName)
            } else {
                repository.joinCalendar(state.pinInput, state.yourName)
            }
            result.fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false) } },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Algo correu mal. Tenta outra vez.")
                    }
                }
            )
        }
    }

    companion object {
        fun factory(repository: CalendarRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                OnboardingViewModel(repository) as T
        }
    }
}
