// File: app/src/main/java/com/example/exerciseslogger/ui/screens/home/HomeUiState.kt
// Timestamp: Updated on 2025-08-20 21:57:48
// Scope: Defines the data structure for the Home Screen's UI state. It now includes the currently selected date.

package com.example.exerciseslogger.ui.screens.home

import java.time.LocalDate

data class HomeUiState(
    val selectedTabIndex: Int = 0,
    val isCalendarExpanded: Boolean = false,
    val selectedDate: LocalDate = LocalDate.now()
)