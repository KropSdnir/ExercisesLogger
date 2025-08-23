// File: app/src/main/java/com/example/exerciseslogger/ui/screens/weightexercisetracking/WeightExerciseTrackingViewModel.kt
// Timestamp: Updated on 2025-08-22 21:05:00 (CEST)
// Scope: Implements new Rest Time logic for Set #1 and preserves input field data on deselection.

package com.example.exerciseslogger.ui.screens.weightexercisetracking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exerciseslogger.data.local.Workout
import com.example.exerciseslogger.data.local.WorkoutDao
import com.example.exerciseslogger.data.local.WorkoutSetEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Collections
import javax.inject.Inject
import kotlin.math.round
import kotlin.math.roundToInt

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WeightExerciseTrackingViewModel @Inject constructor(
    private val workoutDao: WorkoutDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val exerciseId: Int = savedStateHandle.get<Int>("exerciseId")!!
    private val dateEpochDay: Long = savedStateHandle.get<Long>("date")!!
    private val date = LocalDate.ofEpochDay(dateEpochDay)

    private val _transientUiState = MutableStateFlow(TransientUiState())
    private val _selectionState = MutableStateFlow(SelectionState())
    private var countdownJob: Job? = null


    private val workoutFlow = flow {
        val dateInMillis = date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        var workout = workoutDao.getWorkoutByDate(dateInMillis)
        if (workout == null) {
            val newWorkoutId = workoutDao.insertWorkout(Workout(date = dateInMillis))
            workout = Workout(id = newWorkoutId.toInt(), date = dateInMillis)
        }
        emit(workout)
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    private val workoutExerciseFlow = workoutFlow.flatMapLatest { workout ->
        workoutDao.getWorkoutExercise(workout.id, exerciseId)
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    private val loggedSetsFlow = workoutFlow.flatMapLatest { workout ->
        workoutDao.getSetsForExercise(workout.id, exerciseId)
    }

    private data class SelectionState(val isSelectionMode: Boolean = false, val selectedIds: Set<Int> = emptySet())
    // Make sure TransientUiState has the new timer properties
    private data class TransientUiState(
        val selectedTabIndex: Int = 0,
        val weight: String = "0",
        val reps: String = "1",
        val sets: String = "1",
        val weightUnit: WeightUnit = WeightUnit.KGS,
        val editingSetId: Int? = null,
        val editingNotesSetId: Int? = null,
        val editingNotesText: String = "",
        val showUnlockRpeDialogForSetId: Int? = null,
        val showUncheckDialogForSetId: Int? = null,
        val currentSetStartTime: Long? = null,
        val showResetSetStartTimeDialog: Boolean = false,
        val showResetExerciseStartTimeDialog: Boolean = false,
        val showResetExerciseEndTimeDialog: Boolean = false,
        val countdownDurationSeconds: Int = 60,
        val countdownRemainingSeconds: Int = 60,
        val isCountdownRunning: Boolean = false,
        val isAutoStartTimerOn: Boolean = false,
        val vibrateTrigger: Boolean = false
    )

    val uiState: StateFlow<WeightExerciseTrackingUiState> = combine(
        _transientUiState,
        loggedSetsFlow,
        _selectionState,
        workoutFlow,
        workoutExerciseFlow
    ) { transientState, setsFromDb, selection, workout, workoutExercise ->
        WeightExerciseTrackingUiState(
            currentWorkout = workout,
            workoutExercise = workoutExercise,
            selectedTabIndex = transientState.selectedTabIndex,
            weight = transientState.weight,
            reps = transientState.reps,
            sets = transientState.sets,
            weightUnit = transientState.weightUnit,
            editingSetId = transientState.editingSetId,
            editingNotesSetId = transientState.editingNotesSetId,
            editingNotesText = transientState.editingNotesText,
            showUnlockRpeDialogForSetId = transientState.showUnlockRpeDialogForSetId,
            showUncheckDialogForSetId = transientState.showUncheckDialogForSetId,
            currentSetStartTime = transientState.currentSetStartTime,
            showResetSetStartTimeDialog = transientState.showResetSetStartTimeDialog,
            showResetExerciseStartTimeDialog = transientState.showResetExerciseStartTimeDialog,
            showResetExerciseEndTimeDialog = transientState.showResetExerciseEndTimeDialog,
            loggedSets = setsFromDb,
            isSelectionMode = selection.isSelectionMode,
            selectedSetIds = selection.selectedIds,
            // Pass the new timer properties
            countdownDurationSeconds = transientState.countdownDurationSeconds,
            countdownRemainingSeconds = transientState.countdownRemainingSeconds,
            isCountdownRunning = transientState.isCountdownRunning,
            isAutoStartTimerOn = transientState.isAutoStartTimerOn,
            vibrateTrigger = transientState.vibrateTrigger
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        // Initialize with default timer values
        initialValue = WeightExerciseTrackingUiState(
            countdownDurationSeconds = 60,
            countdownRemainingSeconds = 60,
            isCountdownRunning = false,
            vibrateTrigger = false
        )
    )

    fun onTabSelected(index: Int) { _transientUiState.update { it.copy(selectedTabIndex = index) } }
    fun onWeightChange(value: String) { _transientUiState.update { it.copy(weight = value) } }
    fun onRepsChange(value: String) { _transientUiState.update { it.copy(reps = value) } }
    fun onUnitChange(unit: WeightUnit) { _transientUiState.update { it.copy(weightUnit = unit) } }
    fun onSetsChange(value: String) { _transientUiState.update { it.copy(sets = value) } }
    fun onEditNotesChange(newNotes: String) { _transientUiState.update { it.copy(editingNotesText = newNotes) } }

    fun onStartExerciseClicked() {
        viewModelScope.launch {
            val workoutExercise = workoutExerciseFlow.first() ?: return@launch
            val updated = workoutExercise.copy(exerciseStartTime = System.currentTimeMillis())
            workoutDao.updateWorkoutExercise(updated)
        }
    }

    fun onEndExerciseClicked() {
        viewModelScope.launch {
            val workoutExercise = workoutExerciseFlow.first() ?: return@launch
            val updated = workoutExercise.copy(exerciseEndTime = System.currentTimeMillis())
            workoutDao.updateWorkoutExercise(updated)
        }
    }

    fun onStartSetClicked() {
        resetTimerIfRunning()
        _transientUiState.update { it.copy(currentSetStartTime = System.currentTimeMillis()) }
    }

    fun onResetSetStartTimeRequest() {
        _transientUiState.update { it.copy(showResetSetStartTimeDialog = true) }
    }

    fun onResetSetStartTimeConfirm() {
        _transientUiState.update { it.copy(currentSetStartTime = null, showResetSetStartTimeDialog = false) }
    }

    fun onResetSetStartTimeDismiss() {
        _transientUiState.update { it.copy(showResetSetStartTimeDialog = false) }
    }

    fun onResetExerciseStartTimeRequest() {
        _transientUiState.update { it.copy(showResetExerciseStartTimeDialog = true) }
    }

    fun onResetExerciseStartTimeConfirm() {
        viewModelScope.launch {
            val workoutExercise = workoutExerciseFlow.first() ?: return@launch
            val updated = workoutExercise.copy(exerciseStartTime = null, exerciseEndTime = null)
            workoutDao.updateWorkoutExercise(updated)
            _transientUiState.update { it.copy(showResetExerciseStartTimeDialog = false) }
        }
    }

    fun onResetExerciseStartTimeDismiss() {
        _transientUiState.update { it.copy(showResetExerciseStartTimeDialog = false) }
    }

    fun onResetExerciseEndTimeRequest() {
        _transientUiState.update { it.copy(showResetExerciseEndTimeDialog = true) }
    }

    fun onResetExerciseEndTimeConfirm() {
        viewModelScope.launch {
            val workoutExercise = workoutExerciseFlow.first() ?: return@launch
            val updated = workoutExercise.copy(exerciseEndTime = null)
            workoutDao.updateWorkoutExercise(updated)
            _transientUiState.update { it.copy(showResetExerciseEndTimeDialog = false) }
        }
    }

    fun onResetExerciseEndTimeDismiss() {
        _transientUiState.update { it.copy(showResetExerciseEndTimeDialog = false) }
    }

    private fun resetTimerIfRunning() {
        if (_transientUiState.value.isCountdownRunning) {
            countdownJob?.cancel()
            countdownJob = null // Added this line
            _transientUiState.update {
                it.copy(
                    isCountdownRunning = false,
                    countdownRemainingSeconds = it.countdownDurationSeconds
                )
            }
        }
    }

    fun addSets() {
        resetTimerIfRunning()
        viewModelScope.launch {
            val currentState = _transientUiState.value
            val weightInput = currentState.weight.toDoubleOrNull() ?: return@launch
            val repsValue = currentState.reps.toIntOrNull() ?: return@launch
            val numSets = currentState.sets.toIntOrNull()?.coerceAtLeast(1) ?: 1
            val weightInLbs = if (currentState.weightUnit == WeightUnit.KGS) {
                round((weightInput * 2.20462) * 10) / 10.0
            } else {
                weightInput
            }
            val workout = workoutFlow.first()
            val startingSetNumber = loggedSetsFlow.first().size
            for (i in 1..numSets) {
                val newSet = WorkoutSetEntry(
                    workoutId = workout.id,
                    exerciseId = exerciseId,
                    setNumber = startingSetNumber + i,
                    weight = weightInLbs,
                    reps = repsValue,
                )
                workoutDao.insertSet(newSet)
            }
        }
    }

    fun onSetChecked(set: WorkoutSetEntry) {
        viewModelScope.launch {
            val startTime = _transientUiState.value.currentSetStartTime ?: System.currentTimeMillis()
            val completionTime = System.currentTimeMillis()
            val exerciseTime = (completionTime - startTime) / 1000

            val allSets = loggedSetsFlow.first()
            val exerciseStartTime = workoutExerciseFlow.first()?.exerciseStartTime

            val restTime = if (set.setNumber == 1) {
                if (exerciseStartTime != null) {
                    (startTime - exerciseStartTime) / 1000
                } else {
                    null // Can\'t calculate if exercise hasn\'t started
                }
            } else {
                val previousSet = allSets
                    .filter { it.isCompleted && it.completionTime != null }
                    .maxByOrNull { it.setNumber }

                if (previousSet?.completionTime != null) {
                    (startTime - previousSet.completionTime) / 1000
                } else {
                    null // Can\'t find a valid previous set
                }
            }

            val rpeToSave = if (set.isRpeLocked) set.rpe else null

            val updatedSet = set.copy(
                isCompleted = true,
                startTime = startTime,
                exerciseTime = exerciseTime,
                completionTime = completionTime,
                restTime = restTime,
                rpe = rpeToSave
            )
            workoutDao.updateSet(updatedSet)
            _transientUiState.update { it.copy(currentSetStartTime = null) }
            if (_transientUiState.value.isAutoStartTimerOn) {
                onTimerStart()
            }
        }
    }


    fun onUncheckConfirm() {
        viewModelScope.launch {
            val state = _transientUiState.value
            val setToUpdate = loggedSetsFlow.first().find { it.id == state.showUncheckDialogForSetId }
            if (setToUpdate != null) {
                workoutDao.updateSet(setToUpdate.copy(
                    isCompleted = false,
                    startTime = null,
                    exerciseTime = null,
                    restTime = null,
                    completionTime = null,
                    rpe = null,
                    isRpeLocked = false
                ))
            }
            _transientUiState.update { it.copy(showUncheckDialogForSetId = null) }
        }
    }

    fun selectSetForEditing(set: WorkoutSetEntry) {
        _transientUiState.update { currentState ->
            if (currentState.editingSetId == set.id) {
                currentState.copy(editingSetId = null) // Deselect but keep data
            } else {
                currentState.copy(
                    editingSetId = set.id,
                    weight = set.weight.toString(),
                    reps = set.reps.toString()
                )
            }
        }
    }


    fun updateSelectedSet() {
        resetTimerIfRunning()
        viewModelScope.launch {
            val currentState = _transientUiState.value
            val setToUpdateId = currentState.editingSetId ?: return@launch
            val originalSet = loggedSetsFlow.first().find { it.id == setToUpdateId } ?: return@launch
            val weightInput = currentState.weight.toDoubleOrNull() ?: return@launch
            val repsValue = currentState.reps.toIntOrNull() ?: return@launch
            val weightInLbs = if (currentState.weightUnit == WeightUnit.KGS) {
                round((weightInput * 2.20462) * 10) / 10.0
            } else {
                weightInput
            }
            val updatedSet = originalSet.copy(weight = weightInLbs, reps = repsValue)
            workoutDao.updateSet(updatedSet)
            _transientUiState.update { it.copy(editingSetId = null) } // Deselect but don\'t clear inputs
        }
    }

    fun incrementWeight() {
        val currentState = _transientUiState.value
        val currentWeight = currentState.weight.toFloatOrNull() ?: 0f
        val inc = if (currentState.weightUnit == WeightUnit.LBS) 2.5f else 0.5f
        val newW = currentWeight + inc
        _transientUiState.update { it.copy(weight = String.format("%.1f", newW)) }
    }

    fun decrementWeight() {
        val currentState = _transientUiState.value
        val currentWeight = currentState.weight.toFloatOrNull() ?: 0f
        val inc = if (currentState.weightUnit == WeightUnit.LBS) 2.5f else 0.5f
        val newW = currentWeight - inc
        _transientUiState.update { it.copy(weight = if (newW <= 0f) "" else String.format("%.1f", newW)) }
    }

    fun incrementReps() {
        val currentReps = _transientUiState.value.reps.toIntOrNull() ?: 0
        _transientUiState.update { it.copy(reps = (currentReps + 1).toString()) }
    }

    fun decrementReps() {
        val currentReps = _transientUiState.value.reps.toIntOrNull() ?: 0
        _transientUiState.update { it.copy(reps = if ((currentReps - 1) <= 0) "" else (currentReps - 1).toString()) }
    }

    fun incrementSets() {
        val currentSets = _transientUiState.value.sets.toIntOrNull() ?: 0
        _transientUiState.update { it.copy(sets = (currentSets + 1).toString()) }
    }

    fun decrementSets() {
        val currentSets = _transientUiState.value.sets.toIntOrNull() ?: 0
        _transientUiState.update { it.copy(sets = if ((currentSets - 1) <= 0) "" else (currentSets - 1).toString()) }
    }

    fun onUncheckRequest(set: WorkoutSetEntry) {
        _transientUiState.update { it.copy(showUncheckDialogForSetId = set.id) }
    }

    fun onUncheckDismiss() {
        _transientUiState.update { it.copy(showUncheckDialogForSetId = null) }
    }

    fun toggleSetSelection(setId: Int) {
        _selectionState.update { currentState ->
            val newSelectedIds = currentState.selectedIds.toMutableSet().apply {
                if (contains(setId)) remove(setId) else add(setId)
            }
            if (newSelectedIds.isEmpty()) SelectionState() else currentState.copy(isSelectionMode = true, selectedIds = newSelectedIds)
        }
    }

    fun clearSelection() {
        _selectionState.value = SelectionState()
    }

    fun deleteSelectedSets() {
        viewModelScope.launch {
            val idsToDelete = _selectionState.value.selectedIds.toList()
            if (idsToDelete.isNotEmpty()) {
                workoutDao.deleteSetsByIds(idsToDelete)
                renumberSets()
            }
            clearSelection()
        }
    }

    private suspend fun renumberSets() {
        val workout = workoutFlow.first()
        val remainingSets = workoutDao.getSetsForExercise(workout.id, exerciseId).first()
        val renumbered = remainingSets.mapIndexed { index, set ->
            set.copy(setNumber = index + 1)
        }
        workoutDao.updateSets(renumbered)
    }

    fun moveSet(from: Int, to: Int) {
        viewModelScope.launch {
            val sets = loggedSetsFlow.first().toMutableList()
            if (from < 0 || from >= sets.size || to < 0 || to >= sets.size) return@launch
            Collections.swap(sets, from, to)
            val renumberedSets = sets.mapIndexed { index, set ->
                set.copy(setNumber = index + 1)
            }
            workoutDao.updateSets(renumberedSets)
        }
    }

    fun onBeginEditNote(set: WorkoutSetEntry) {
        _transientUiState.update { it.copy(editingNotesSetId = set.id, editingNotesText = set.notes) }
    }

    fun onSaveNote() {
        viewModelScope.launch {
            val state = _transientUiState.value
            val setToUpdate = loggedSetsFlow.first().find { it.id == state.editingNotesSetId }
            if (setToUpdate != null) {
                workoutDao.updateSet(setToUpdate.copy(notes = state.editingNotesText))
            }
            _transientUiState.update { it.copy(editingNotesSetId = null, editingNotesText = "") }
        }
    }

    fun onRpeChange(set: WorkoutSetEntry, newRpe: Float) {
        viewModelScope.launch {
            val roundedRpe = (newRpe * 2).roundToInt() / 2f
            workoutDao.updateSet(set.copy(rpe = roundedRpe))
        }
    }

    fun onLockRpe(set: WorkoutSetEntry) {
        viewModelScope.launch { workoutDao.updateSet(set.copy(isRpeLocked = true)) }
    }

    fun onUnlockRpeRequest(set: WorkoutSetEntry) {
        _transientUiState.update { it.copy(showUnlockRpeDialogForSetId = set.id) }
    }

    fun onUnlockRpeConfirm() {
        viewModelScope.launch {
            val state = _transientUiState.value
            val setToUpdate = loggedSetsFlow.first().find { it.id == state.showUnlockRpeDialogForSetId }
            if (setToUpdate != null) {
                workoutDao.updateSet(setToUpdate.copy(isRpeLocked = false))
            }
            _transientUiState.update { it.copy(showUnlockRpeDialogForSetId = null) }
        }
    }

    fun onUnlockRpeDismiss() {
        _transientUiState.update { it.copy(showUnlockRpeDialogForSetId = null) }
    }

    // Change parameter type to Int
    fun onTimerDurationChange(newDuration: Int) {
        //val duration = newDuration.toIntOrNull() ?: 0 // No longer needed
        _transientUiState.update {
            it.copy(
                countdownDurationSeconds = newDuration, // Use newDuration directly
                countdownRemainingSeconds = newDuration // Use newDuration directly
            )
        }
    }
    
    fun onToggleAutoStartTimer() {
        _transientUiState.update { it.copy(isAutoStartTimerOn = !it.isAutoStartTimerOn) }
    }

    fun onTimerStart() {
        if (_transientUiState.value.isCountdownRunning) return
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            _transientUiState.update { it.copy(isCountdownRunning = true) }
            while (_transientUiState.value.countdownRemainingSeconds > 0) {
                delay(1000)
                _transientUiState.update { it.copy(countdownRemainingSeconds = it.countdownRemainingSeconds - 1) }
            }
            _transientUiState.update {
                it.copy(
                    isCountdownRunning = false,
                    vibrateTrigger = true,
                    countdownRemainingSeconds = it.countdownDurationSeconds // Reset to the set duration
                )
            }
        }
    }

    fun onTimerReset() {
        countdownJob?.cancel()
        _transientUiState.update {
            it.copy(
                isCountdownRunning = false,
                countdownRemainingSeconds = it.countdownDurationSeconds
            )
        }
    }

    fun onTimerRepeat() {
        onTimerReset()
        onTimerStart()
    }

    fun onVibrationHandled() {
        _transientUiState.update { it.copy(vibrateTrigger = false) }
    }
}
