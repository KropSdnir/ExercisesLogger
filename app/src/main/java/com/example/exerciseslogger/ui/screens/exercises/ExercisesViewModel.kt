// File: app/src/main/java/com/example/exerciseslogger/ui/screens/exercises/ExercisesViewModel.kt
// Timestamp: Updated on 2025-08-22 13:33:01
// Scope: Corrects references to the database entity from 'WorkoutExerciseCrossRef' to 'WorkoutExercise'.

package com.example.exerciseslogger.ui.screens.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exerciseslogger.data.local.ExerciseDao
import com.example.exerciseslogger.data.local.Workout
import com.example.exerciseslogger.data.local.WorkoutDao
import com.example.exerciseslogger.data.local.WorkoutExercise
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject

@HiltViewModel
class ExercisesViewModel @Inject constructor(
    private val exerciseDao: ExerciseDao,
    private val workoutDao: WorkoutDao
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _selectionState = MutableStateFlow(SelectionState())
    private val _searchQuery = MutableStateFlow("")

    private val _eventChannel = Channel<UiEvent>()
    val eventFlow = _eventChannel.receiveAsFlow()

    private data class SelectionState(
        val isSelectionMode: Boolean = false,
        val selectedIds: Set<Int> = emptySet()
    )

    private val _allExercises = exerciseDao.getAllExercises()
    private val _allCategories = exerciseDao.getUniqueCategories()

    val uiState = combine(
        _allExercises,
        _allCategories,
        _selectedCategory,
        _selectionState,
        _searchQuery
    ) { allExercises, categories, selectedCategory, selection, searchQuery ->
        val categoryFiltered = if (selectedCategory != null) {
            allExercises.filter { it.category == selectedCategory }
        } else {
            allExercises
        }
        val searchFiltered = if (searchQuery.isNotBlank()) {
            categoryFiltered.filter { it.name.contains(searchQuery, ignoreCase = true) }
        } else {
            categoryFiltered
        }
        ExercisesUiState(
            exercises = searchFiltered,
            categories = categories,
            selectedCategoryFilter = selectedCategory,
            searchQuery = searchQuery,
            isSelectionMode = selection.isSelectionMode,
            selectedExerciseIds = selection.selectedIds
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExercisesUiState()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun selectCategoryFilter(category: String?) {
        _selectedCategory.value = category
    }

    fun toggleExerciseSelection(exerciseId: Int) {
        _selectionState.update { currentState ->
            val newSelectedIds = currentState.selectedIds.toMutableSet().apply {
                if (contains(exerciseId)) remove(exerciseId) else add(exerciseId)
            }
            if (newSelectedIds.isEmpty()) SelectionState() else currentState.copy(isSelectionMode = true, selectedIds = newSelectedIds)
        }
    }

    fun clearExerciseSelection() {
        _selectionState.value = SelectionState()
    }

    fun addSelectedExercisesToWorkout(date: LocalDate) {
        viewModelScope.launch {
            val selectedIds = _selectionState.value.selectedIds
            if (selectedIds.isEmpty()) return@launch

            addExercisesToDate(selectedIds.toList(), date)
            _eventChannel.send(UiEvent.ShowSnackbar("${selectedIds.size} exercises added"))
            clearExerciseSelection()
        }
    }

    fun addSingleExerciseToWorkout(exerciseId: Int, date: LocalDate) {
        viewModelScope.launch {
            addExercisesToDate(listOf(exerciseId), date)
            _eventChannel.send(UiEvent.ShowSnackbar("Exercise added"))
        }
    }

    private suspend fun addExercisesToDate(exerciseIds: List<Int>, date: LocalDate) {
        val dateInMillis = date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

        var workout = workoutDao.getWorkoutByDate(dateInMillis)
        if (workout == null) {
            val newWorkoutId = workoutDao.insertWorkout(Workout(date = dateInMillis))
            workout = Workout(id = newWorkoutId.toInt(), date = dateInMillis)
        }

        val entries = exerciseIds.map { exerciseId ->
            WorkoutExercise(workoutId = workout.id, exerciseId = exerciseId)
        }
        workoutDao.insertWorkoutExercises(entries)
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
    }
}