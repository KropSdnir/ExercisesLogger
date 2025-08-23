// File: app/src/main/java/com/example/exerciseslogger/ui/screens/exerciseaddition/ExerciseAdditionUiState.kt
// Timestamp: Updated on 2025-08-21 18:45:13
// Scope: The UI state is updated to include a 'notes' field.

package com.example.exerciseslogger.ui.screens.exerciseaddition

data class ExerciseAdditionUiState(
    val name: String = "",
    val isCategoryDropdownExpanded: Boolean = false,
    val selectedCategory: String = "",
    val notes: String = "", // New property
    val categories: List<String> = listOf("Chest", "Back", "Legs", "Shoulders", "Arms", "Core", "Add"),
    val showAddCategoryDialog: Boolean = false,
    val newCategoryName: String = ""
)