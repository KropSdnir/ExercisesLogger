// File: app/src/main/java/com/example/exerciseslogger/ui/screens/home/HomeScreen.kt
// Timestamp: Updated on 2025-08-20 22:17:57
// Scope: Updated to handle navigation events. The options menu icon is removed, and the "Weight" dropdown item now triggers a navigation callback.

package com.example.exerciseslogger.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.temporal.ChronoUnit

@Composable
fun HomeScreen(
    onMenuClick: () -> Unit,
    onNavigateToWeightWorkout: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val tabTitles = listOf("Statistics", "History")

    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val currentYearMonth = YearMonth.now()
    val months = remember { List(12) { currentYearMonth.minusMonths(6).plusMonths(it.toLong()) } }

    LaunchedEffect(uiState.isCalendarExpanded) {
        if (uiState.isCalendarExpanded) {
            val selectedMonth = YearMonth.from(uiState.selectedDate)
            val index = ChronoUnit.MONTHS.between(months.first(), selectedMonth).toInt()
            if (index in months.indices) {
                lazyListState.scrollToItem(index)
            }
        }
    }

    Scaffold(
        topBar = {
            HomeTopAppBar(
                onMenuClick = onMenuClick,
                onCalendarClick = { scope.launch { viewModel.toggleCalendar() } },
                onNavigateToWeightWorkout = onNavigateToWeightWorkout
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            AnimatedVisibility(visible = !uiState.isCalendarExpanded) {
                SimpleCalendarView(selectedDate = uiState.selectedDate)
            }
            AnimatedVisibility(visible = uiState.isCalendarExpanded) {
                FullCalendarView(
                    lazyListState = lazyListState,
                    months = months,
                    selectedDate = uiState.selectedDate,
                    onDateSelected = viewModel::selectDate
                )
            }

            if (!uiState.isCalendarExpanded) {
                TabRow(selectedTabIndex = uiState.selectedTabIndex) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = uiState.selectedTabIndex == index,
                            onClick = { viewModel.selectTab(index) },
                            text = { Text(text = title) }
                        )
                    }
                }
                when (uiState.selectedTabIndex) {
                    0 -> StatisticsContent()
                    1 -> HistoryContent()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopAppBar(
    onMenuClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onNavigateToWeightWorkout: () -> Unit
) {
    var showAddMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text("Home") },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Toggle Menu")
            }
        },
        actions = {
            IconButton(onClick = onCalendarClick) {
                Icon(Icons.Default.CalendarMonth, contentDescription = "Toggle Calendar")
            }
            Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                IconButton(onClick = { showAddMenu = !showAddMenu }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Workout")
                }
                DropdownMenu(
                    expanded = showAddMenu,
                    onDismissRequest = { showAddMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Weight") },
                        onClick = onNavigateToWeightWorkout
                    )
                    DropdownMenuItem(text = { Text("Cardio") }, onClick = { /* TODO: Navigate to Cardio Detail */ })
                }
            }
            // Corrected: The three-dot options menu icon has been removed.
        }
    )
}

@Composable
private fun StatisticsContent() {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Text("Statistics Card (to be implemented)", textAlign = TextAlign.Center)
    }
}

@Composable
private fun HistoryContent() {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Text("History Card (to be implemented)", textAlign = TextAlign.Center)
    }
}