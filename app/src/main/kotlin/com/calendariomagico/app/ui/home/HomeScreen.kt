package com.calendariomagico.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.calendariomagico.app.data.model.CalendarItem
import com.calendariomagico.app.data.repository.CalendarRepository
import com.calendariomagico.app.ui.editor.ItemEditorSheet
import com.calendariomagico.app.ui.home.components.ItemCard
import com.calendariomagico.app.ui.home.components.MonthGrid
import com.calendariomagico.app.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(repository: CalendarRepository, onOpenSettings: () -> Unit) {
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory(repository))
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var editingItem by remember { mutableStateOf<CalendarItem?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        state.calendarName.ifBlank { "Calendário Mágico" },
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Definições")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { isCreatingNew = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Adicionar")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = viewModel::goToPreviousMonth) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Mês anterior")
                }
                Text(
                    DateUtils.monthYearLabel(state.month),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = viewModel::goToNextMonth) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Próximo mês")
                }
            }
            TextButton(
                onClick = viewModel::goToToday,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) { Text("Hoje") }

            MonthGrid(
                month = state.month,
                selectedDate = state.selectedDate,
                itemsByDay = viewModel::itemsOn,
                onDayClick = viewModel::selectDate,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(Modifier.height(8.dp))
            Text(
                DateUtils.dayHeaderLabel(state.selectedDate),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(8.dp))

            val dayItems = state.itemsForSelectedDate
            if (dayItems.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🌈", style = MaterialTheme.typography.headlineLarge)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Nada por aqui ainda. Toca em + para adicionar!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(dayItems, key = { it.id.ifBlank { it.hashCode().toString() } }) { item ->
                        ItemCard(
                            item = item,
                            onClick = { editingItem = item },
                            onToggleDone = { viewModel.toggleDone(item) },
                            onDelete = { viewModel.deleteItem(item) }
                        )
                    }
                }
            }
        }
    }

    if (isCreatingNew) {
        ItemEditorSheet(
            initialItem = null,
            initialDate = state.selectedDate,
            onDismiss = { isCreatingNew = false },
            onSave = { item ->
                viewModel.saveItem(item) { _, _ -> }
                isCreatingNew = false
            },
            onDelete = null
        )
    }

    editingItem?.let { item ->
        ItemEditorSheet(
            initialItem = item,
            initialDate = state.selectedDate,
            onDismiss = { editingItem = null },
            onSave = { updated ->
                viewModel.saveItem(updated) { _, _ -> }
                editingItem = null
            },
            onDelete = {
                viewModel.deleteItem(item)
                editingItem = null
            }
        )
    }
}
