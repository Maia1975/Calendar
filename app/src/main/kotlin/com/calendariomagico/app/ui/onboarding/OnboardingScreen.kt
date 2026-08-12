package com.calendariomagico.app.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.calendariomagico.app.data.repository.CalendarRepository
import com.calendariomagico.app.ui.theme.CreamBackground
import com.calendariomagico.app.ui.theme.Lavender
import com.calendariomagico.app.ui.theme.Mint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(repository: CalendarRepository) {
    val viewModel: OnboardingViewModel = viewModel(factory = OnboardingViewModel.factory(repository))
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🗓️✨", fontSize = 52.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                "Calendário Mágico",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "O calendário da família, sempre sincronizado 💛",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(28.dp))

            ModeSwitch(mode = state.mode, onModeChange = viewModel::onModeChange)
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = state.yourName,
                onValueChange = viewModel::onYourNameChange,
                label = { Text("O teu nome") },
                placeholder = { Text("Ex: Maria") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words
                ),
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(14.dp))

            AnimatedContent(targetState = state.mode, label = "onboarding-mode") { mode ->
                if (mode == OnboardingMode.CREATE) {
                    OutlinedTextField(
                        value = state.calendarName,
                        onValueChange = viewModel::onCalendarNameChange,
                        label = { Text("Nome do calendário") },
                        placeholder = { Text("Ex: Família Silva") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    OutlinedTextField(
                        value = state.pinInput,
                        onValueChange = viewModel::onPinChange,
                        label = { Text("Código de convite") },
                        placeholder = { Text("Ex: G7K2QM") },
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            state.errorMessage?.let { error ->
                Spacer(Modifier.height(12.dp))
                Text(
                    error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = viewModel::submit,
                enabled = state.canSubmit && !state.isLoading,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.height(22.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        if (state.mode == OnboardingMode.CREATE) "Criar calendário 🎉" else "Entrar no calendário 🚀",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "Cria um calendário e recebes um código para partilhares com a família. " +
                    "Ou usa um código que já recebeste para entrares no calendário deles.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ModeSwitch(mode: OnboardingMode, onModeChange: (OnboardingMode) -> Unit) {
    Surface(
        color = CreamBackground,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ModeChip(
                label = "Criar novo",
                selected = mode == OnboardingMode.CREATE,
                color = Lavender,
                modifier = Modifier.weight(1f)
            ) { onModeChange(OnboardingMode.CREATE) }
            ModeChip(
                label = "Já tenho código",
                selected = mode == OnboardingMode.JOIN,
                color = Mint,
                modifier = Modifier.weight(1f)
            ) { onModeChange(OnboardingMode.JOIN) }
        }
    }
}

@Composable
private fun ModeChip(
    label: String,
    selected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (selected) color else Color.Transparent,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Text(
            label,
            modifier = Modifier
                .padding(vertical = 12.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
