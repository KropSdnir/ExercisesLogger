// File: app/src/main/java/com/example/exerciseslogger/ui/screens/exercises/ExercisesUiState.kt
// Timestamp: Updated on 2025-08-21 21:00:58
// Scope: The UI state is updated to hold the current search query text.

package com.example.exerciseslogger.ui.screens.exercises

import com.example.exerciseslogger.data.local.Exercise

data class ExercisesUiState(
    val exercises: List<Exercise> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategoryFilter: String? = null,
    val searchQuery: String = "", // New property for search
    val isSelectionMode: Boolean = false,
    val selectedExerciseIds: Set<Int> = emptySet()
)