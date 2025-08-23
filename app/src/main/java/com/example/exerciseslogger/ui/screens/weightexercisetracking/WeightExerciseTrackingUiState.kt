// File: app/src/main/java/com/example/exerciseslogger/ui/screens/weightexercisetracking/WeightExerciseTrackingUiState.kt
// Timestamp: Updated on 2025-08-22 16:15:33
// Scope: Restores state properties for the exercise timer reset dialogs.

package com.example.exerciseslogger.ui.screens.weightexercisetracking

import com.example.exerciseslogger.data.local.Workout
import com.example.exerciseslogger.data.local.WorkoutExercise
import com.example.exerciseslogger.data.local.WorkoutSetEntry

enum class WeightUnit { KGS, LBS }

data class WeightExerciseTrackingUiState(
    val currentWorkout: Workout? = null,
    val workoutExercise: WorkoutExercise? = null,
    val selectedTabIndex: Int = 0,
    val weight: String = "",
    val reps: String = "",
    val sets: String = "1",
    val weightUnit: WeightUnit = WeightUnit.KGS,
    val loggedSets: List<WorkoutSetEntry> = emptyList(),
    val isSelectionMode: Boolean = false,
    val selectedSetIds: Set<Int> = emptySet(),
    val editingSetId: Int? = null,
    val editingNotesText: String = "",
    val editingNotesSetId: Int? = null,
    val showUnlockRpeDialogForSetId: Int? = null,
    val showUncheckDialogForSetId: Int? = null,
    val currentSetStartTime: Long? = null,
    val showResetSetStartTimeDialog: Boolean = false,
    val showResetExerciseStartTimeDialog: Boolean = false,
    val showResetExerciseEndTimeDialog: Boolean = false,
    val countdownDurationSeconds: Int = 60, // Default to 60 seconds
    val countdownRemainingSeconds: Int = 60, // Default to 60 seconds
    val isCountdownRunning: Boolean = false,
    val isAutoStartTimerOn: Boolean = false,
    val vibrateTrigger: Boolean = false
)