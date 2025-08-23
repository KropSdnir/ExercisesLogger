// File: app/src/main/java/com/example/exerciseslogger/ui/screens/exerciseaddition/ExerciseAdditionViewModel.kt
// Timestamp: Updated on 2025-08-21 18:45:13
// Scope: The ViewModel is updated to handle the logic for the new 'notes' field.

package com.example.exerciseslogger.ui.screens.exerciseaddition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exerciseslogger.data.local.Exercise
import com.example.exerciseslogger.data.local.ExerciseDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExerciseAdditionViewModel @Inject constructor(
    private val exerciseDao: ExerciseDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExerciseAdditionUiState())
    val uiState: StateFlow<ExerciseAdditionUiState> = _uiState.asStateFlow()

    fun onNameChange(newName: String) { _uiState.update { it.copy(name = newName) } }
    fun onNotesChange(newNotes: String) { _uiState.update { it.copy(notes = newNotes) } } // New function
    fun onNewCategoryNameChange(name: String) { _uiState.update { it.copy(newCategoryName = name) } }
    fun dismissAddCategoryDialog() { _uiState.update { it.copy(showAddCategoryDialog = false, newCategoryName = "") } }
    fun toggleCategoryDropdown() { _uiState.update { it.copy(isCategoryDropdownExpanded = !it.isCategoryDropdownExpanded) } }

    fun onCategorySelected(category: String) {
        if (category == "Add") {
            _uiState.update { it.copy(showAddCategoryDialog = true, isCategoryDropdownExpanded = false) }
        } else {
            _uiState.update {
                it.copy(
                    selectedCategory = category,
                    isCategoryDropdownExpanded = false
                )
            }
        }
    }

    fun addNewCategory() {
        _uiState.update {
            val newCategory = it.newCategoryName.trim()
            if (newCategory.isNotBlank() && !it.categories.contains(newCategory)) {
                val updatedCategories = it.categories.toMutableList().apply {
                    add(it.categories.size - 1, newCategory)
                }
                it.copy(categories = updatedCategories, selectedCategory = newCategory, newCategoryName = "", showAddCategoryDialog = false)
            } else {
                it.copy(newCategoryName = "", showAddCategoryDialog = false)
            }
        }
    }

    fun onAddClicked() {
        viewModelScope.launch {
            val newExercise = Exercise(
                name = uiState.value.name,
                category = uiState.value.selectedCategory,
                notes = uiState.value.notes // Save notes
            )
            exerciseDao.insertExercise(newExercise)
        }
    }
}