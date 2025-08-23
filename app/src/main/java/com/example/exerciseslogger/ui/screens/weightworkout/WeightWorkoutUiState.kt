// File: app/src/main/java/com/example/exerciseslogger/ui/screens/weightworkout/WeightWorkoutUiState.kt
// Timestamp: Updated on 2025-08-22 13:51:14
// Scope: The UI state is refactored to manage a new "choice" dialog for the timer actions.

package com.example.exerciseslogger.ui.screens.weightworkout

import com.example.exerciseslogger.data.local.Exercise
import com.example.exerciseslogger.data.local.Workout
import java.time.LocalDate

// An enum to represent all possible dialogs on this screen
enum class WorkoutDialogType {
    CHOOSE_START_ACTION,
    CHOOSE_END_ACTION,
    RESET_START_TIME,
    RESET_END_TIME,
    EDIT_START_TIME,
    EDIT_END_TIME
}

data class WeightWorkoutUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val isCalendarExpanded: Boolean = false,
    val workout: Workout? = null,
    val exercisesForDate: List<Exercise> = emptyList(),
    val isSelectionMode: Boolean = false,
    val selectedExerciseIds: Set<Int> = emptySet(),
    val activeDialog: WorkoutDialogType? = null,
    val timeInputHours: String = "",
    val timeInputMinutes: String = ""
)