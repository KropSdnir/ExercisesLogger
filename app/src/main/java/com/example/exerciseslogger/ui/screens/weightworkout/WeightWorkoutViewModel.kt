// File: app/src/main/java/com/example/exerciseslogger/ui/screens/weightworkout/WeightWorkoutViewModel.kt
// Timestamp: Updated on 2025-08-22 13:54:23
// Scope: The complete and correct version of the ViewModel.

package com.example.exerciseslogger.ui.screens.weightworkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exerciseslogger.data.local.WorkoutDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WeightWorkoutViewModel @Inject constructor(
    private val workoutDao: WorkoutDao
) : ViewModel() {

    private val _dateState = MutableStateFlow(DateState())
    private val _selectionState = MutableStateFlow(SelectionState())
    private val _dialogState = MutableStateFlow(DialogState())

    private data class DateState(
        val selectedDate: LocalDate = LocalDate.now(),
        val isCalendarExpanded: Boolean = false
    )

    private data class SelectionState(
        val isSelectionMode: Boolean = false,
        val selectedIds: Set<Int> = emptySet()
    )

    private data class DialogState(
        val activeDialog: WorkoutDialogType? = null,
        val timeInputHours: String = "",
        val timeInputMinutes: String = ""
    )

    val uiState: StateFlow<WeightWorkoutUiState> = _dateState
        .flatMapLatest { dateState ->
            val dateInMillis = dateState.selectedDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
            combine(
                workoutDao.getWorkoutForDate(dateInMillis),
                _selectionState,
                _dialogState
            ) { workoutWithExercises, selectionState, dialogState ->
                WeightWorkoutUiState(
                    selectedDate = dateState.selectedDate,
                    isCalendarExpanded = dateState.isCalendarExpanded,
                    workout = workoutWithExercises?.workout,
                    exercisesForDate = workoutWithExercises?.exercises ?: emptyList(),
                    isSelectionMode = selectionState.isSelectionMode,
                    selectedExerciseIds = selectionState.selectedIds,
                    activeDialog = dialogState.activeDialog,
                    timeInputHours = dialogState.timeInputHours,
                    timeInputMinutes = dialogState.timeInputMinutes
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WeightWorkoutUiState()
        )

    fun showDialog(dialogType: WorkoutDialogType) {
        val workout = uiState.value.workout
        var hours = ""
        var minutes = ""

        if (dialogType == WorkoutDialogType.EDIT_START_TIME && workout?.workoutStartTime != null) {
            val localDateTime = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(workout.workoutStartTime), ZoneOffset.UTC)
            hours = localDateTime.format(DateTimeFormatter.ofPattern("HH"))
            minutes = localDateTime.format(DateTimeFormatter.ofPattern("mm"))
        } else if (dialogType == WorkoutDialogType.EDIT_END_TIME && workout?.workoutEndTime != null) {
            val localDateTime = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(workout.workoutEndTime), ZoneOffset.UTC)
            hours = localDateTime.format(DateTimeFormatter.ofPattern("HH"))
            minutes = localDateTime.format(DateTimeFormatter.ofPattern("mm"))
        }

        _dialogState.update { it.copy(activeDialog = dialogType, timeInputHours = hours, timeInputMinutes = minutes) }
    }

    fun dismissDialog() {
        _dialogState.update { it.copy(activeDialog = null, timeInputHours = "", timeInputMinutes = "") }
    }

    fun onTimeInputHoursChange(hours: String) {
        if (hours.length <= 2 && hours.all { it.isDigit() }) {
            _dialogState.update { it.copy(timeInputHours = hours) }
        }
    }

    fun onTimeInputMinutesChange(minutes: String) {
        if (minutes.length <= 2 && minutes.all { it.isDigit() }) {
            _dialogState.update { it.copy(timeInputMinutes = minutes) }
        }
    }

    fun onStartWorkoutClicked() {
        viewModelScope.launch {
            val workout = uiState.value.workout ?: return@launch
            if (workout.workoutStartTime == null) {
                val updatedWorkout = workout.copy(workoutStartTime = System.currentTimeMillis())
                workoutDao.updateWorkout(updatedWorkout)
            }
        }
    }

    fun onEndWorkoutClicked() {
        viewModelScope.launch {
            val workout = uiState.value.workout ?: return@launch
            if (workout.workoutEndTime == null) {
                val updatedWorkout = workout.copy(workoutEndTime = System.currentTimeMillis())
                workoutDao.updateWorkout(updatedWorkout)
            }
        }
    }

    fun onResetStartTimeConfirm() {
        viewModelScope.launch {
            val workout = uiState.value.workout ?: return@launch
            val updatedWorkout = workout.copy(workoutStartTime = null)
            workoutDao.updateWorkout(updatedWorkout)
            dismissDialog()
        }
    }

    fun onResetEndTimeConfirm() {
        viewModelScope.launch {
            val workout = uiState.value.workout ?: return@launch
            val updatedWorkout = workout.copy(workoutEndTime = null)
            workoutDao.updateWorkout(updatedWorkout)
            dismissDialog()
        }
    }

    fun onSaveEditedTime() {
        viewModelScope.launch {
            val workout = uiState.value.workout ?: return@launch
            val hours = _dialogState.value.timeInputHours.toIntOrNull()?.coerceIn(0, 23) ?: 0
            val minutes = _dialogState.value.timeInputMinutes.toIntOrNull()?.coerceIn(0, 59) ?: 0

            val newDateTime = uiState.value.selectedDate.atTime(hours, minutes)
            val newTimestamp = newDateTime.toInstant(ZoneOffset.UTC).toEpochMilli()

            val updatedWorkout = when (_dialogState.value.activeDialog) {
                WorkoutDialogType.EDIT_START_TIME -> workout.copy(workoutStartTime = newTimestamp)
                WorkoutDialogType.EDIT_END_TIME -> workout.copy(workoutEndTime = newTimestamp)
                else -> workout
            }
            workoutDao.updateWorkout(updatedWorkout)
            dismissDialog()
        }
    }

    fun toggleCalendar() {
        _dateState.update { it.copy(isCalendarExpanded = !it.isCalendarExpanded) }
    }

    fun selectDate(date: LocalDate) {
        _dateState.update { it.copy(selectedDate = date, isCalendarExpanded = false) }
    }

    fun toggleExerciseSelection(exerciseId: Int) {
        _selectionState.update { currentState ->
            val newSelectedIds = currentState.selectedIds.toMutableSet()
            if (newSelectedIds.contains(exerciseId)) {
                newSelectedIds.remove(exerciseId)
            } else {
                newSelectedIds.add(exerciseId)
            }

            if (newSelectedIds.isEmpty()) {
                SelectionState()
            } else {
                currentState.copy(isSelectionMode = true, selectedIds = newSelectedIds)
            }
        }
    }

    fun clearSelection() {
        _selectionState.value = SelectionState()
    }

    fun deleteSelectedExercises() {
        viewModelScope.launch {
            val workoutId = uiState.value.workout?.id ?: return@launch
            val idsToDelete = _selectionState.value.selectedIds.toList()

            if (idsToDelete.isNotEmpty()) {
                workoutDao.deleteWorkoutExercises(workoutId, idsToDelete)
            }
            clearSelection()
        }
    }
}