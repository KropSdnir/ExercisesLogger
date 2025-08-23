// File: app/src/main/java/com/example/exerciseslogger/ui/screens/exercisedetails/ExerciseDetailsViewModel.kt
// Timestamp: Updated on 2025-08-21 21:31:13
// Scope: Manages the UI state and logic for the Exercise Details screen.

package com.example.exerciseslogger.ui.screens.exercisedetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ExerciseDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val exerciseId: Int = savedStateHandle.get<Int>("exerciseId")!!
    private val exerciseName: String = savedStateHandle.get<String>("exerciseName")!!

    private val _uiState = MutableStateFlow(ExerciseDetailsUiState(exerciseName = exerciseName))
    val uiState: StateFlow<ExerciseDetailsUiState> = _uiState.asStateFlow()

    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTabIndex = index) }
    }
}