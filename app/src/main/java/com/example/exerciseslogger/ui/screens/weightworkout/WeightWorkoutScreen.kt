// File: app/src/main/java/com/example/exerciseslogger/ui/screens/weightworkout/WeightWorkoutScreen.kt
// Timestamp: Updated on 2025-08-22 13:51:14
// Scope: Replaces the dropdown menu with a single "choice" dialog that leads to either the Edit or Reset dialogs.

package com.example.exerciseslogger.ui.screens.weightworkout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.exerciseslogger.R
import com.example.exerciseslogger.data.local.Exercise
import com.example.exerciseslogger.data.local.Workout
import com.example.exerciseslogger.ui.screens.home.FullCalendarView
import com.example.exerciseslogger.ui.screens.home.SimpleCalendarView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightWorkoutScreen(
    onMenuClick: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateToExercises: () -> Unit,
    onNavigateToTracking: (exerciseId: Int, exerciseName: String) -> Unit,
    viewModel: WeightWorkoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle which dialog to show
    when (uiState.activeDialog) {
        WorkoutDialogType.CHOOSE_START_ACTION -> {
            ActionChoiceDialog(
                onDismiss = viewModel::dismissDialog,
                onEditClick = { viewModel.showDialog(WorkoutDialogType.EDIT_START_TIME) },
                onResetClick = { viewModel.showDialog(WorkoutDialogType.RESET_START_TIME) }
            )
        }
        WorkoutDialogType.CHOOSE_END_ACTION -> {
            ActionChoiceDialog(
                onDismiss = viewModel::dismissDialog,
                onEditClick = { viewModel.showDialog(WorkoutDialogType.EDIT_END_TIME) },
                onResetClick = { viewModel.showDialog(WorkoutDialogType.RESET_END_TIME) }
            )
        }
        WorkoutDialogType.RESET_START_TIME -> {
            ResetConfirmationDialog(
                onDismiss = viewModel::dismissDialog,
                onConfirm = viewModel::onResetStartTimeConfirm,
                title = "Reset Start Time",
                text = "Are you sure you want to reset the workout start time?"
            )
        }
        WorkoutDialogType.RESET_END_TIME -> {
            ResetConfirmationDialog(
                onDismiss = viewModel::dismissDialog,
                onConfirm = viewModel::onResetEndTimeConfirm,
                title = "Reset End Time",
                text = "Are you sure you want to reset the workout end time?"
            )
        }
        WorkoutDialogType.EDIT_START_TIME, WorkoutDialogType.EDIT_END_TIME -> {
            EditTimeDialog(
                hours = uiState.timeInputHours,
                minutes = uiState.timeInputMinutes,
                onHoursChange = viewModel::onTimeInputHoursChange,
                onMinutesChange = viewModel::onTimeInputMinutesChange,
                onDismiss = viewModel::dismissDialog,
                onConfirm = viewModel::onSaveEditedTime
            )
        }
        null -> {}
    }

    Scaffold(
        topBar = {
            WorkoutTopAppBar(
                isSelectionMode = uiState.isSelectionMode,
                selectedCount = uiState.selectedExerciseIds.size,
                onMenuClick = onMenuClick,
                onNavigateHome = onNavigateHome,
                onCalendarClick = viewModel::toggleCalendar,
                onAddExerciseClick = onNavigateToExercises,
                onClearSelection = viewModel::clearSelection
            )
        },
        floatingActionButton = {
            if (uiState.isSelectionMode) {
                FloatingActionButton(onClick = viewModel::deleteSelectedExercises) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete selected exercises")
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            val lazyListState = rememberLazyListState()
            val currentYearMonth = YearMonth.now()
            val months = remember { List(12) { currentYearMonth.minusMonths(6).plusMonths(it.toLong()) } }

            LaunchedEffect(uiState.isCalendarExpanded) {
                if (uiState.isCalendarExpanded) {
                    val selectedMonth = YearMonth.from(uiState.selectedDate)
                    val index = ChronoUnit.MONTHS.between(months.first(), selectedMonth).toInt()
                    if (index in months.indices) lazyListState.scrollToItem(index)
                }
            }

            AnimatedVisibility(visible = !uiState.isCalendarExpanded) {
                SimpleCalendarView(selectedDate = uiState.selectedDate)
            }
            AnimatedVisibility(visible = uiState.isCalendarExpanded) {
                FullCalendarView(lazyListState, months, uiState.selectedDate, viewModel::selectDate)
            }
            if (!uiState.isCalendarExpanded) {
                WorkoutTimerHeader(
                    workout = uiState.workout,
                    onStartClick = viewModel::onStartWorkoutClicked,
                    onStartTimeLongPress = { viewModel.showDialog(WorkoutDialogType.CHOOSE_START_ACTION) },
                    onEndClick = viewModel::onEndWorkoutClicked,
                    onEndTimeLongPress = { viewModel.showDialog(WorkoutDialogType.CHOOSE_END_ACTION) },
                    modifier = Modifier.padding(16.dp)
                )

                WorkoutExerciseList(
                    exercises = uiState.exercisesForDate,
                    isSelectionMode = uiState.isSelectionMode,
                    selectedIds = uiState.selectedExerciseIds,
                    onItemClick = { exerciseId ->
                        if (uiState.isSelectionMode) {
                            viewModel.toggleExerciseSelection(exerciseId)
                        } else {
                            val exercise = uiState.exercisesForDate.find { it.id == exerciseId }
                            exercise?.let {
                                onNavigateToTracking(it.id, it.name)
                            }
                        }
                    },
                    onItemLongClick = viewModel::toggleExerciseSelection
                )
            }
        }
    }
}

@Composable
private fun ActionChoiceDialog(
    onDismiss: () -> Unit,
    onEditClick: () -> Unit,
    onResetClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Action") },
        text = {
            Column {
                TextButton(onClick = onEditClick, modifier = Modifier.fillMaxWidth()) { Text("Edit Time") }
                TextButton(onClick = onResetClick, modifier = Modifier.fillMaxWidth()) { Text("Reset Time") }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun ResetConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    text: String
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = { Button(onClick = onConfirm) { Text("Reset") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun EditTimeDialog(
    hours: String,
    minutes: String,
    onHoursChange: (String) -> Unit,
    onMinutesChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Time (24h format)") },
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OutlinedTextField(
                    value = hours,
                    onValueChange = onHoursChange,
                    label = { Text("HH") },
                    modifier = Modifier.width(60.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Text(":", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = minutes,
                    onValueChange = onMinutesChange,
                    label = { Text("MM") },
                    modifier = Modifier.width(60.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = { Button(onClick = onConfirm) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WorkoutTimerHeader(
    workout: Workout?,
    onStartClick: () -> Unit,
    onStartTimeLongPress: () -> Unit,
    onEndClick: () -> Unit,
    onEndTimeLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (workout?.workoutStartTime == null) {
            Button(onClick = onStartClick) {
                Text("Start Workout", fontSize = 12.sp)
            }
        } else {
            Text(
                text = "WS: ${formatTimestamp(workout.workoutStartTime, "HH:mm:ss")}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.combinedClickable(onClick = {}, onLongClick = onStartTimeLongPress)
            )
        }

        if (workout?.workoutEndTime == null) {
            Button(onClick = onEndClick, enabled = workout?.workoutStartTime != null) {
                Text("End Workout", fontSize = 12.sp)
            }
        } else {
            val durationText = workout.workoutStartTime?.let { startTime ->
                val durationSeconds = (workout.workoutEndTime - startTime) / 1000
                "(${formatDuration(durationSeconds)})"
            } ?: ""
            Text(
                text = "WE: ${formatTimestamp(workout.workoutEndTime, "HH:mm:ss")} $durationText",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.combinedClickable(onClick = {}, onLongClick = onEndTimeLongPress)
            )
        }
    }
}

private fun formatTimestamp(millis: Long, pattern: String): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(Date(millis))
}

private fun formatDuration(totalSeconds: Long): String {
    if (totalSeconds < 0) return "00:00:00"
    val hours = TimeUnit.SECONDS.toHours(totalSeconds)
    val minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) % 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

@Composable
private fun WorkoutExerciseList(
    exercises: List<Exercise>,
    isSelectionMode: Boolean,
    selectedIds: Set<Int>,
    onItemClick: (Int) -> Unit,
    onItemLongClick: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (exercises.isEmpty()) {
            item {
                Text(
                    text = "No exercises logged for this day. Tap '+' to add some.",
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            items(exercises, key = { it.id }) { exercise ->
                val isSelected = selectedIds.contains(exercise.id)
                WorkoutExerciseListItem(
                    exercise = exercise,
                    isSelected = isSelected,
                    onClick = { onItemClick(exercise.id) },
                    onLongClick = { onItemLongClick(exercise.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WorkoutExerciseListItem(
    exercise: Exercise,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (exercise.category.isNotEmpty()) "${exercise.name} (${exercise.category})" else exercise.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutTopAppBar(
    isSelectionMode: Boolean,
    selectedCount: Int,
    onMenuClick: () -> Unit,
    onNavigateHome: () -> Unit,
    onCalendarClick: () -> Unit,
    onAddExerciseClick: () -> Unit,
    onClearSelection: () -> Unit
) {
    TopAppBar(
        title = {
            if (isSelectionMode) Text("$selectedCount Selected") else Text("Weight Workout")
        },
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
                    IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, "Menu") }
                    IconButton(onClick = onNavigateHome) {
                        Icon(painterResource(R.drawable.home_back_icon), "Home", Modifier.size(24.dp))
                    }
                }
            }
        },
        actions = {
            if (!isSelectionMode) {
                IconButton(onClick = onCalendarClick) { Icon(Icons.Default.CalendarMonth, "Calendar") }
                IconButton(onClick = onAddExerciseClick) { Icon(Icons.Default.Add, "Add Exercise") }
            }
        }
    )
}