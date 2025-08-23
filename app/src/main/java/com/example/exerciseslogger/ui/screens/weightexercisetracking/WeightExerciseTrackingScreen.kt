// File: app/src/main/java/com/example/exerciseslogger/ui/screens/weightexercisetracking/WeightExerciseTrackingScreen.kt
// Timestamp: Updated on 2025-08-22 21:05:00 (CEST)
// Scope: Implements long-press for Start/End Exercise buttons.

package com.example.exerciseslogger.ui.screens.weightexercisetracking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border // Added this import
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.exerciseslogger.R
import com.example.exerciseslogger.data.local.Workout
import com.example.exerciseslogger.data.local.WorkoutExercise
import com.example.exerciseslogger.data.local.WorkoutSetEntry
import com.example.exerciseslogger.ui.components.StandardTopAppBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun WeightExerciseTrackingScreen(
    widthSizeClass: WindowWidthSizeClass,
    exerciseName: String,
    onMenuClick: () -> Unit,
    onNavigateUp: () -> Unit,
    viewModel: WeightExerciseTrackingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val tabTitles = listOf("Track", "History", "Stats")
    val focusManager = LocalFocusManager.current

    if (uiState.showResetSetStartTimeDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onResetSetStartTimeDismiss,
            title = { Text("Reset Set Start Time") },
            text = { Text("Are you sure you want to reset the current set\'s start time?") },
            confirmButton = { Button(onClick = viewModel::onResetSetStartTimeConfirm) { Text("Reset") } },
            dismissButton = { TextButton(onClick = viewModel::onResetSetStartTimeDismiss) { Text("Cancel") } }
        )
    }

    if (uiState.showResetExerciseStartTimeDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onResetExerciseStartTimeDismiss,
            title = { Text("Reset Exercise Start Time") },
            text = { Text("Are you sure you want to reset the start time for this exercise? This will also reset the end time.") },
            confirmButton = { Button(onClick = viewModel::onResetExerciseStartTimeConfirm) { Text("Reset") } },
            dismissButton = { TextButton(onClick = viewModel::onResetExerciseStartTimeDismiss) { Text("Cancel") } }
        )
    }

    if (uiState.showResetExerciseEndTimeDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onResetExerciseEndTimeDismiss,
            title = { Text("Reset Exercise End Time") },
            text = { Text("Are you sure you want to reset the end time for this exercise?") },
            confirmButton = { Button(onClick = viewModel::onResetExerciseEndTimeConfirm) { Text("Reset") } },
            dismissButton = { TextButton(onClick = viewModel::onResetExerciseEndTimeDismiss) { Text("Cancel") } }
        )
    }

    if (uiState.showUncheckDialogForSetId != null) {
        AlertDialog(
            onDismissRequest = viewModel::onUncheckDismiss,
            title = { Text("Confirm Uncheck") },
            text = { Text("Are you sure you want to mark this set as incomplete?") },
            confirmButton = { Button(onClick = viewModel::onUncheckConfirm) { Text("Confirm") } },
            dismissButton = { TextButton(onClick = viewModel::onUncheckDismiss) { Text("Cancel") } }
        )
    }

    if (uiState.showUnlockRpeDialogForSetId != null) {
        AlertDialog(
            onDismissRequest = viewModel::onUnlockRpeDismiss,
            title = { Text("Unlock RPE") },
            text = { Text("Are you sure you want to unlock and edit the RPE for this set?") },
            confirmButton = { Button(onClick = viewModel::onUnlockRpeConfirm) { Text("Unlock") } },
            dismissButton = { TextButton(onClick = viewModel::onUnlockRpeDismiss) { Text("Cancel") } }
        )
    }

    Scaffold(
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) {
            focusManager.clearFocus()
        },
        topBar = {
            TrackingTopAppBar(
                title = exerciseName,
                isSelectionMode = uiState.isSelectionMode,
                selectedCount = uiState.selectedSetIds.size,
                onMenuClick = onMenuClick,
                onNavigateUp = onNavigateUp,
                onClearSelection = viewModel::clearSelection,
                onDeleteSelected = viewModel::deleteSelectedSets
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            TabRow(selectedTabIndex = uiState.selectedTabIndex) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = uiState.selectedTabIndex == index,
                        onClick = { viewModel.onTabSelected(index) },
                        text = { Text(title) }
                    )
                }
            }
            when (uiState.selectedTabIndex) {
                0 -> TrackTabContent(widthSizeClass, uiState, viewModel)
                1 -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("History (Not Implemented") }
                2 -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Stats (Not Implemented") }
            }
        }
    }
}

@Composable
private fun TrackTabContent(
    widthSizeClass: WindowWidthSizeClass,
    uiState: WeightExerciseTrackingUiState,
    viewModel: WeightExerciseTrackingViewModel
) {
    val isCompact = widthSizeClass == WindowWidthSizeClass.Compact

    var paramsCardExpanded by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ExerciseTimerCard(
            workoutExercise = uiState.workoutExercise,
            onStartClick = viewModel::onStartExerciseClicked,
            onResetStartRequest = viewModel::onResetExerciseStartTimeRequest,
            onEndClick = viewModel::onEndExerciseClicked,
            onResetEndRequest = viewModel::onResetExerciseEndTimeRequest
        )
        SetParametersCard(
            uiState = uiState,
            viewModel = viewModel,
            isCompact = isCompact,
            expanded = paramsCardExpanded,
            onExpandToggle = { paramsCardExpanded = !paramsCardExpanded })
        TimerCard(
            remainingSeconds = uiState.countdownRemainingSeconds,
            isRunning = uiState.isCountdownRunning,
            onStartClick = viewModel::onTimerStart,
            onResetClick = viewModel::onTimerReset,
            onRepeatClick = viewModel::onTimerRepeat,
            onDurationChange = viewModel::onTimerDurationChange,
            isAutoStartTimerOn = uiState.isAutoStartTimerOn, 
            onToggleAutoStartTimer = viewModel::onToggleAutoStartTimer
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                WorkoutLogHeader(
                    setStartTime = uiState.currentSetStartTime,
                    onStartSetClick = viewModel::onStartSetClicked,
                    onResetSetRequest = viewModel::onResetSetStartTimeRequest
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.loggedSets.size, key = { uiState.loggedSets[it].id }) { index ->
                        val set = uiState.loggedSets[index]
                        val isSelectedForMultiSelect = uiState.selectedSetIds.contains(set.id)
                        val isSelectedForEditing = uiState.editingSetId == set.id
                        SetListItem(
                            set = set,
                            isSelected = isSelectedForMultiSelect || isSelectedForEditing,
                            isBeingEdited = uiState.editingNotesSetId == set.id,
                            isSelectionMode = uiState.isSelectionMode,
                            editingText = uiState.editingNotesText,
                            onEditNotesChange = viewModel::onEditNotesChange,
                            onSaveNote = viewModel::onSaveNote,
                            onClick = {
                                if (!paramsCardExpanded) paramsCardExpanded = true
                                if (uiState.isSelectionMode) {
                                    viewModel.toggleSetSelection(set.id)
                                } else {
                                    viewModel.selectSetForEditing(set)
                                }
                            },
                            onLongClick = {
                                viewModel.toggleSetSelection(set.id)
                            },
                            onBeginEdit = { viewModel.onBeginEditNote(set) },
                            onMoveUp = { viewModel.moveSet(index, index - 1) },
                            onMoveDown = { viewModel.moveSet(index, index + 1) },
                            isUpEnabled = index > 0,
                            isDownEnabled = index < uiState.loggedSets.size - 1,
                            onSetChecked = { viewModel.onSetChecked(set) },
                            onUncheckRequest = { viewModel.onUncheckRequest(set) },
                            onRpeChange = { newRpe -> viewModel.onRpeChange(set, newRpe) },
                            onLockRpe = { viewModel.onLockRpe(set) },
                            onUnlockRpeRequest = { viewModel.onUnlockRpeRequest(set) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TimerCard(
    remainingSeconds: Int,
    isRunning: Boolean,
    onStartClick: () -> Unit,
    onResetClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onDurationChange: (Int) -> Unit,
    isAutoStartTimerOn: Boolean, 
    onToggleAutoStartTimer: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var newTime by remember { mutableStateOf(remainingSeconds.toString()) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Set Timer Duration") },
            text = {
                OutlinedTextField(
                    value = newTime,
                    onValueChange = { newTime = it.filter { char -> char.isDigit() } },
                    label = { Text("Seconds") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
            confirmButton = {
                Button(onClick = {
                    onDurationChange(newTime.toIntOrNull() ?: 0)
                    showDialog = false
                }) {
                    Text("Set")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween 
        ) {
            Text(
                text = "$remainingSeconds s",
                fontSize = 20.sp,
                modifier = Modifier.combinedClickable( // Allows long press for editing duration
                    onClick = { /* Could be used for a quick action if needed */ },
                    onLongClick = {
                        newTime = remainingSeconds.toString()
                        showDialog = true
                    }
                )
            )
            
            Spacer(modifier = Modifier.weight(1f)) // Pushes "A" and buttons to the right if timer text is short

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .combinedClickable(
                         onClick = { /* Tap on A could perhaps also toggle, or do nothing */ },
                         onLongClick = onToggleAutoStartTimer // Changed from onLongPress
                    )
                    .then(
                        if (isAutoStartTimerOn) {
                            Modifier.border(BorderStroke(2.dp, MaterialTheme.colorScheme.primary), CircleShape)
                        } else {
                            Modifier
                        }
                    )
                    .padding(8.dp) // Padding inside the optional border
            ) {
                Text(
                    text = "A",
                    fontSize = 20.sp, // Match timer text size
                    color = if (isAutoStartTimerOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Spacer(modifier = Modifier.weight(1f)) // Pushes buttons to the right

            Row {
                IconButton(onClick = if (isRunning) onResetClick else onStartClick) {
                    Icon(
                        if (isRunning) Icons.Default.Close else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Reset" else "Start"
                    )
                }
                IconButton(onClick = onRepeatClick) {
                    Icon(Icons.Default.Refresh, contentDescription = "Repeat")
                }
            }
        }
    }
}


@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun ExerciseTimerCard(
    workoutExercise: WorkoutExercise?,
    onStartClick: () -> Unit,
    onResetStartRequest: () -> Unit,
    onEndClick: () -> Unit,
    onResetEndRequest: () -> Unit
) {
    if (workoutExercise?.exerciseEndTime != null && workoutExercise.exerciseStartTime != null) {
        // Exercise duration summary view
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {},
                    onLongClick = onResetEndRequest
                )
        ) {
            val startTime = formatTimestamp(workoutExercise.exerciseStartTime, "HH:mm:ss")
            val endTime = formatTimestamp(workoutExercise.exerciseEndTime, "HH:mm:ss")
            val duration = formatHms(workoutExercise.exerciseEndTime - workoutExercise.exerciseStartTime)
            Text(
                text = "Duration: $startTime - $endTime ($duration)",
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    } else {
        // Exercise duration tracking view
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (workoutExercise?.exerciseStartTime == null) {
                Card(
                    modifier = Modifier.combinedClickable(
                        onClick = { /* Consider short click for start too */ },
                        onLongClick = onStartClick // Keep long click for start
                    )
                ) {
                    Text("Start Exercise", modifier = Modifier.padding(8.dp))
                }
            } else {
                Text(
                    text = "Started: ${formatTimestamp(workoutExercise.exerciseStartTime, "HH:mm:ss")}",
                    modifier = Modifier.combinedClickable(onClick = {}, onLongClick = onResetStartRequest)
                )
            }

            Card(
                modifier = Modifier.combinedClickable(
                    enabled = workoutExercise?.exerciseStartTime != null,
                    onClick = { /* Consider short click for end too */ },
                    onLongClick = onEndClick // Keep long click for end
                )
            ) {
                Text("End Exercise", modifier = Modifier.padding(8.dp))
            }
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun WorkoutLogHeader(
    setStartTime: Long?,
    onStartSetClick: () -> Unit,
    onResetSetRequest: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text("Sets", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.weight(1f))

        if (setStartTime == null) {
            Button(
                onClick = onStartSetClick,
                contentPadding = PaddingValues(horizontal = 12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Start Set", fontSize = 12.sp)
            }
        } else {
            Text(
                text = "Set start time: ${formatTimestamp(setStartTime, "HH:mm:ss")}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.combinedClickable(
                    onClick = {},
                    onLongClick = onResetSetRequest
                )
            )
        }
    }
}

private fun formatTimestamp(millis: Long, pattern: String): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(Date(millis))
}

private fun formatHms(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

@Composable
private fun NumberPickerRow(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    increment: () -> Unit,
    decrement: () -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: (@Composable () -> Unit)? = null,
    labelColor: Color
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, color = labelColor) },
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, color = labelColor),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            trailingIcon = trailingIcon,
            colors = OutlinedTextFieldDefaults.colors(
                focusedLabelColor = labelColor,
                unfocusedLabelColor = labelColor,
                focusedTrailingIconColor = labelColor,
                unfocusedTrailingIconColor = labelColor,
                focusedTextColor = labelColor,
                unfocusedTextColor = labelColor
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 0.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = decrement, modifier = Modifier.size(28.dp)) {
                Icon(
                    Icons.Default.Remove,
                    contentDescription = "Decrement $label",
                    tint = labelColor
                )
            }
            IconButton(onClick = increment, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Increment $label", tint = labelColor)
            }
        }
    }
}

@Composable
private fun SetParametersCard(
    uiState: WeightExerciseTrackingUiState,
    viewModel: WeightExerciseTrackingViewModel,
    isCompact: Boolean,
    expanded: Boolean = true,
    onExpandToggle: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!expanded) {
                    Text(
                        "Weight",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 15.sp),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    Text(
                        "Reps",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 15.sp),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    Text(
                        "Sets",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 15.sp),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                } else {
                    Spacer(modifier = Modifier.weight(3f))
                }
                IconButton(
                    onClick = { onExpandToggle?.invoke() },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }
            }
            if (expanded) {
                if (isCompact) {
                    CompactInputLayout(uiState = uiState, viewModel = viewModel)
                } else {
                    ExpandedInputLayout(uiState = uiState, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
private fun CompactInputLayout(
    uiState: WeightExerciseTrackingUiState,
    viewModel: WeightExerciseTrackingViewModel
) {
    val fieldColors = OutlinedTextFieldDefaults.colors()
    val labelColor = Color.White
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        NumberPickerRow(
            value = uiState.weight,
            onValueChange = viewModel::onWeightChange,
            label = "Weight",
            increment = {
                val w = uiState.weight.toFloatOrNull() ?: 0f
                val inc = if (uiState.weightUnit == WeightUnit.LBS) 2.5f else 0.5f
                val newW = w + inc
                viewModel.onWeightChange(String.format("%.1f", newW))
            },
            decrement = {
                val w = uiState.weight.toFloatOrNull() ?: 0f
                val inc = if (uiState.weightUnit == WeightUnit.LBS) 2.5f else 0.5f
                val newW = w - inc
                viewModel.onWeightChange(if (newW <= 0f) "" else String.format("%.1f", newW))
            },
            modifier = Modifier.weight(1.5f),
            trailingIcon = {
                UnitToggleAllowNoFocus(
                    selectedUnit = uiState.weightUnit,
                    labelColor = labelColor,
                    onUnitChange = { newUnit ->
                        val weightVal = uiState.weight.toFloatOrNull()
                        if (weightVal != null) {
                            val kgs =
                                if (uiState.weightUnit == WeightUnit.KGS) weightVal else weightVal / 2.20462f
                            val lbs =
                                if (uiState.weightUnit == WeightUnit.LBS) weightVal else weightVal * 2.20462f
                            viewModel.onWeightChange(
                                String.format("%.1f", if (newUnit == WeightUnit.KGS) kgs else lbs)
                            )
                        }
                        viewModel.onUnitChange(newUnit)
                    })
            },
            labelColor = labelColor
        )
        NumberPickerRow(
            value = uiState.reps,
            onValueChange = viewModel::onRepsChange,
            label = "Reps",
            increment = {
                val r = uiState.reps.toIntOrNull() ?: 0
                viewModel.onRepsChange((r + 1).toString())
            },
            decrement = {
                val r = uiState.reps.toIntOrNull() ?: 0
                viewModel.onRepsChange(if ((r - 1) <= 0) "" else (r - 1).toString())
            },
            modifier = Modifier.weight(1f),
            labelColor = labelColor
        )
        NumberPickerRow(
            value = uiState.sets,
            onValueChange = viewModel::onSetsChange,
            label = "Sets",
            increment = {
                val s = uiState.sets.toIntOrNull() ?: 0
                viewModel.onSetsChange((s + 1).toString())
            },
            decrement = {
                val s = uiState.sets.toIntOrNull() ?: 0
                viewModel.onSetsChange(if ((s - 1) <= 0) "" else (s - 1).toString())
            },
            modifier = Modifier.weight(1f),
            labelColor = labelColor
        )
        IconButton(
            onClick = { if (uiState.editingSetId != null) viewModel.updateSelectedSet() else viewModel.addSets() },
            enabled = uiState.weight.isNotBlank() && uiState.reps.isNotBlank(),
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Icon(
                imageVector = if (uiState.editingSetId != null) Icons.Default.CheckCircle else Icons.Default.Add,
                contentDescription = if (uiState.editingSetId != null) "Update Set" else "Add Sets"
            )
        }
    }
}

@Composable
private fun ExpandedInputLayout(
    uiState: WeightExerciseTrackingUiState,
    viewModel: WeightExerciseTrackingViewModel
) {
    val fieldColors = OutlinedTextFieldDefaults.colors()
    val labelColor = Color.White
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        NumberPickerRow(
            value = uiState.weight,
            onValueChange = viewModel::onWeightChange,
            label = "Weight",
            increment = {
                val w = uiState.weight.toFloatOrNull() ?: 0f
                val inc = if (uiState.weightUnit == WeightUnit.LBS) 2.5f else 0.5f
                val newW = w + inc
                viewModel.onWeightChange(String.format("%.1f", newW))
            },
            decrement = {
                val w = uiState.weight.toFloatOrNull() ?: 0f
                val inc = if (uiState.weightUnit == WeightUnit.LBS) 2.5f else 0.5f
                val newW = w - inc
                viewModel.onWeightChange(if (newW <= 0f) "" else String.format("%.1f", newW))
            },
            modifier = Modifier.weight(1f),
            trailingIcon = {
                UnitToggleAllowNoFocus(
                    selectedUnit = uiState.weightUnit,
                    labelColor = labelColor,
                    onUnitChange = { newUnit ->
                        val weightVal = uiState.weight.toFloatOrNull()
                        if (weightVal != null) {
                            val kgs =
                                if (uiState.weightUnit == WeightUnit.KGS) weightVal else weightVal / 2.20462f
                            val lbs =
                                if (uiState.weightUnit == WeightUnit.LBS) weightVal else weightVal * 2.20462f
                            viewModel.onWeightChange(
                                String.format("%.1f", if (newUnit == WeightUnit.KGS) kgs else lbs)
                            )
                        }
                        viewModel.onUnitChange(newUnit)
                    })
            },
            labelColor = labelColor
        )
        NumberPickerRow(
            value = uiState.reps,
            onValueChange = viewModel::onRepsChange,
            label = "Reps",
            increment = {
                val r = uiState.reps.toIntOrNull() ?: 0
                viewModel.onRepsChange((r + 1).toString())
            },
            decrement = {
                val r = uiState.reps.toIntOrNull() ?: 0
                viewModel.onRepsChange(if ((r - 1) <= 0) "" else (r - 1).toString())
            },
            modifier = Modifier.weight(1f),
            labelColor = labelColor
        )
        NumberPickerRow(
            value = uiState.sets,
            onValueChange = viewModel::onSetsChange,
            label = "Sets",
            increment = {
                val s = uiState.sets.toIntOrNull() ?: 0
                viewModel.onSetsChange((s + 1).toString())
            },
            decrement = {
                val s = uiState.sets.toIntOrNull() ?: 0
                viewModel.onSetsChange(if ((s - 1) <= 0) "" else (s - 1).toString())
            },
            modifier = Modifier.weight(1f),
            labelColor = labelColor
        )
        IconButton(
            onClick = { if (uiState.editingSetId != null) viewModel.updateSelectedSet() else viewModel.addSets() },
            enabled = uiState.weight.isNotBlank() && uiState.reps.isNotBlank(),
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Icon(
                imageVector = if (uiState.editingSetId != null) Icons.Default.CheckCircle else Icons.Default.Add,
                contentDescription = if (uiState.editingSetId != null) "Update Set" else "Add Sets"
            )
        }
    }
}

@Composable
private fun UnitToggleAllowNoFocus(
    selectedUnit: WeightUnit,
    labelColor: Color,
    onUnitChange: (WeightUnit) -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(56.dp)
            .clip(RoundedCornerShape(4.dp))
            .pointerInput(selectedUnit) {
                detectTapGestures(
                    onLongPress = {
                        val newUnit =
                            if (selectedUnit == WeightUnit.KGS) WeightUnit.LBS else WeightUnit.KGS
                        onUnitChange(newUnit)
                    },
                    onTap = { /* no focus action at all, must tap actual text box */ }
                )
            }
            .padding(horizontal = 8.dp)
    ) {
        Text(
            text = if (selectedUnit == WeightUnit.KGS) "kg" else "lb",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = labelColor
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
private fun SetListItem(
    set: WorkoutSetEntry,
    isSelected: Boolean,
    isBeingEdited: Boolean,
    isSelectionMode: Boolean,
    editingText: String,
    onEditNotesChange: (String) -> Unit,
    onSaveNote: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onBeginEdit: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    isUpEnabled: Boolean,
    isDownEnabled: Boolean,
    onSetChecked: () -> Unit,
    onUncheckRequest: () -> Unit,
    onRpeChange: (Float) -> Unit,
    onLockRpe: () -> Unit,
    onUnlockRpeRequest: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant

    Column {
        // Add parent Card to group all 4 sub-cards with an outline border
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(2.dp)) {
                // Card 1: Set Parameter Logs
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { onClick() },
                                onLongPress = { onLongClick() })
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(containerColor = backgroundColor),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val weightInKgs = String.format("%.1f", set.weight / 2.20462)
                            Text(
                                text = "${set.setNumber}. ${set.weight} lbs | $weightInKgs kgs x ${set.reps} reps",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isSelectionMode) {
                                    IconButton(
                                        onClick = onMoveUp,
                                        enabled = isUpEnabled,
                                        modifier = Modifier.size(28.dp)
                                    ) { Icon(Icons.Default.ArrowUpward, "Move Up") }
                                    IconButton(
                                        onClick = onMoveDown,
                                        enabled = isDownEnabled,
                                        modifier = Modifier.size(28.dp)
                                    ) { Icon(Icons.Default.ArrowDownward, "Move Down") }
                                }
                                Box(modifier = Modifier.pointerInput(set.isCompleted) {
                                    detectTapGestures(
                                        onTap = { if (!set.isCompleted) onSetChecked() },
                                        onLongPress = { if (set.isCompleted) onUncheckRequest() }
                                    )
                                }) {
                                    Checkbox(
                                        checked = set.isCompleted,
                                        onCheckedChange = null,
                                        enabled = false,
                                        colors = CheckboxDefaults.colors(
                                            disabledCheckedColor = MaterialTheme.colorScheme.primary,
                                            disabledUncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(1.dp))
                // Card 2: Set RPE Logs
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                        RpeCard(
                            set = set,
                            onRpeChange = onRpeChange,
                            onLockRpe = onLockRpe,
                            onUnlockRpeRequest = onUnlockRpeRequest
                        )
                    }
                }
                Spacer(modifier = Modifier.height(1.dp))
                // Card 3: Set Summary Logs
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                        if (set.isCompleted) {
                            SetSummaryCard(set = set)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                // Card 4: Set Notes
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .combinedClickable(
                                onClick = {},
                                onLongClick = onBeginEdit
                            )
                    ) {
                        if (isBeingEdited) {
                            val focusRequester = remember { FocusRequester() }
                            var hasFocus by remember { mutableStateOf(false) }
                            OutlinedTextField(
                                value = editingText,
                                onValueChange = onEditNotesChange,
                                label = { Text("Notes") },
                                singleLine = false,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester)
                                    .onFocusChanged { focusState ->
                                        if (hasFocus && !focusState.isFocused) {
                                            onSaveNote()
                                        }
                                        hasFocus = focusState.isFocused
                                    }
                            )
                            LaunchedEffect(Unit) { focusRequester.requestFocus() }
                        } else if (set.notes.isNotBlank()) {
                            Text(
                                text = "Notes: ${set.notes}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Text(
                                text = "Add a note...",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SetSummaryCard(set: WorkoutSetEntry) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        FlowRow(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val duration = (set.exerciseTime ?: 0L) + (set.restTime ?: 0L)
            LogItem("R:", set.restTime?.let { "$it s" } ?: "-")
            LogItem("E:", set.exerciseTime?.let { "$it s" } ?: "-")
            LogItem("D:", "${duration / 60}m ${duration % 60}s")
            if (set.isRpeLocked && set.rpe != null) {
                LogItem("RPE:", String.format("%.1f", set.rpe))
            }
            val ct = set.completionTime?.let {
                formatTimestamp(it, "HH:mm:ss")
            } ?: "-"
            LogItem("CT:", ct)
        }
    }
}

@Composable
private fun LogItem(label: String, value: String) {
    Text(
        text = "$label $value", // Removed colon from here as it's in the label now
        style = MaterialTheme.typography.bodySmall,
        fontSize = 10.sp
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RpeCard(
    set: WorkoutSetEntry,
    onRpeChange: (Float) -> Unit,
    onLockRpe: () -> Unit,
    onUnlockRpeRequest: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        if (set.isRpeLocked) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(onClick = {}, onLongClick = onUnlockRpeRequest)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("RPE:", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Text(String.format("%.1f", set.rpe ?: 0.0f))
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.Lock, contentDescription = "RPE Locked", modifier = Modifier.size(16.dp))
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Slider(
                    value = set.rpe ?: 0f,
                    onValueChange = onRpeChange,
                    valueRange = 0f..10f,
                    steps = 19,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = String.format("%.1f", set.rpe ?: 0.0f),
                    onValueChange = { onRpeChange(it.toFloatOrNull() ?: 0f) },
                    modifier = Modifier.width(70.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                    label = { Text("RPE", fontSize = 10.sp) }
                )
                IconButton(onClick = onLockRpe, enabled = set.rpe != null) {
                    Icon(Icons.Default.Add, contentDescription = "Lock RPE")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrackingTopAppBar(
    title: String,
    isSelectionMode: Boolean,
    selectedCount: Int,
    onMenuClick: () -> Unit,
    onNavigateUp: () -> Unit,
    onClearSelection: () -> Unit,
    onDeleteSelected: () -> Unit
) {
    TopAppBar(
        title = { if (isSelectionMode) Text("$selectedCount Selected") else Text(title) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = if (isSelectionMode) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer
        ),
        navigationIcon = {
            if (isSelectionMode) {
                IconButton(onClick = onClearSelection) {
                    Icon(Icons.Default.Close, "Clear Selection")
                }
            } else {
                Row {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Toggle Menu")
                    }
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            painter = painterResource(id = R.drawable.home_back_icon),
                            contentDescription = "Back",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        },
        actions = {
            if (isSelectionMode) {
                IconButton(onClick = onDeleteSelected) {
                    Icon(Icons.Default.Delete, "Delete selected sets")
                }
            }
        }
    )
}
