// File: app/src/main/java/com/example/exerciseslogger/ui/screens/exercisedetails/ExerciseDetailsUiState.kt
// Timestamp: Updated on 2025-08-21 21:31:13
// Scope: Defines the data structure for the Exercise Details screen's UI state.

package com.example.exerciseslogger.ui.screens.exercisedetails

data class ExerciseDetailsUiState(
    val selectedTabIndex: Int = 0,
    val exerciseName: String = ""
    // We will add lists for history and stats data here later
)