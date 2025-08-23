// File: app/src/main/java/com/example/exerciseslogger/ui/screens/exercises/ExercisesScreen.kt
// Timestamp: Updated on 2025-08-21 22:04:31
// Scope: Implements a more robust fix for the swipe-to-add navigation and a definitive fix for the inconsistent item height.

package com.example.exerciseslogger.ui.screens.exercises

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.exerciseslogger.R
import com.example.exerciseslogger.data.local.Exercise
import com.example.exerciseslogger.navigation.ExercisesScreenmode
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesScreen(
    workoutDate: LocalDate,
    mode: ExercisesScreenmode,
    onMenuClick: () -> Unit,
    onNavigateUp: () -> Unit,
    onNavigateToExerciseAddition: () -> Unit,
    onNavigateToExerciseDetails: (Int, String) -> Unit,
    viewModel: ExercisesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when(event) {
                is ExercisesViewModel.UiEvent.ShowSnackbar -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = event.message,
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ExercisesTopAppBar(
                mode = mode,
                isSelectionMode = uiState.isSelectionMode,
                selectedCount = uiState.selectedExerciseIds.size,
                onMenuClick = onMenuClick,
                onNavigateUp = onNavigateUp,
                onNavigateToExerciseAddition = onNavigateToExerciseAddition,
                onClearSelection = viewModel::clearExerciseSelection
            )
        },
        floatingActionButton = {
            if (mode == ExercisesScreenmode.SELECTION && uiState.isSelectionMode) {
                ExtendedFloatingActionButton(
                    onClick = {
                        viewModel.addSelectedExercisesToWorkout(workoutDate)
                        onNavigateUp()
                    },
                    icon = { Icon(Icons.Default.Add, "Add to workout.") },
                    text = { Text(text = "Add to Workout") },
                )
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                label = { Text("Search Exercises") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear Search")
                        }
                    }
                },
                singleLine = true,
                enabled = !uiState.isSelectionMode
            )

            CategoryFilter(
                selectedCategory = uiState.selectedCategoryFilter,
                categories = uiState.categories,
                onCategorySelected = viewModel::selectCategoryFilter,
                isEnabled = !uiState.isSelectionMode
            )

            ExerciseList(
                exercises = uiState.exercises,
                mode = mode,
                isSelectionMode = uiState.isSelectionMode,
                selectedIds = uiState.selectedExerciseIds,
                onItemClick = viewModel::toggleExerciseSelection,
                onViewDetailsClick = onNavigateToExerciseDetails,
                onSwipeToAdd = { exerciseId ->
                    viewModel.addSingleExerciseToWorkout(exerciseId, workoutDate)
                    onNavigateUp()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExercisesTopAppBar(
    mode: ExercisesScreenmode,
    isSelectionMode: Boolean,
    selectedCount: Int,
    onMenuClick: () -> Unit,
    onNavigateUp: () -> Unit,
    onNavigateToExerciseAddition: () -> Unit,
    onClearSelection: () -> Unit
) {
    TopAppBar(
        title = { if (isSelectionMode) Text("$selectedCount Selected") else Text("Exercises") },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = if (isSelectionMode) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer
        ),
        navigationIcon = {
            if (isSelectionMode) {
                IconButton(onClick = onClearSelection) { Icon(Icons.Default.Close, "Clear Selection") }
            } else {
                Row {
                    IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, "Menu") }
                    IconButton(onClick = onNavigateUp) {
                        Icon(painterResource(R.drawable.home_back_icon), "Back", Modifier.size(24.dp))
                    }
                }
            }
        },
        actions = {
            if (!isSelectionMode && mode == ExercisesScreenmode.SELECTION) {
                IconButton(onClick = onNavigateToExerciseAddition) {
                    Icon(Icons.Default.Add, "Add Exercise")
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFilter(
    selectedCategory: String?,
    categories: List<String>,
    onCategorySelected: (String?) -> Unit,
    isEnabled: Boolean
) {
    var isFilterExpanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = isFilterExpanded,
        onExpandedChange = { if (isEnabled) isFilterExpanded = !isFilterExpanded },
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = selectedCategory ?: "All Categories",
            onValueChange = {},
            readOnly = true,
            label = { Text("Filter by Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isFilterExpanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            enabled = isEnabled
        )
        ExposedDropdownMenu(
            expanded = isFilterExpanded,
            onDismissRequest = { isFilterExpanded = false }
        ) {
            DropdownMenuItem(text = { Text("All Categories") }, onClick = { onCategorySelected(null); isFilterExpanded = false })
            categories.forEach { category ->
                DropdownMenuItem(text = { Text(category) }, onClick = { onCategorySelected(category); isFilterExpanded = false })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseList(
    exercises: List<Exercise>,
    mode: ExercisesScreenmode,
    isSelectionMode: Boolean,
    selectedIds: Set<Int>,
    onItemClick: (Int) -> Unit,
    onViewDetailsClick: (Int, String) -> Unit,
    onSwipeToAdd: (Int) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (exercises.isEmpty()) {
            item { Text("No exercises found.", modifier = Modifier.padding(32.dp)) }
        } else {
            items(exercises, key = { it.id }) { exercise ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = {
                        if (it == SwipeToDismissBoxValue.StartToEnd) {
                            onSwipeToAdd(exercise.id)
                            return@rememberSwipeToDismissBoxState false // Prevents visual dismiss
                        }
                        return@rememberSwipeToDismissBoxState false
                    },
                    positionalThreshold = { it * .25f }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = mode == ExercisesScreenmode.SELECTION && !isSelectionMode,
                    enableDismissFromEndToStart = false,
                    backgroundContent = {
                        val color by animateColorAsState(
                            targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.background
                            },
                            label = "Swipe Background Color"
                        )
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(color)
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add exercise")
                        }
                    }
                ) {
                    val isSelected = selectedIds.contains(exercise.id)
                    ExerciseListItem(
                        exercise = exercise,
                        isSelected = isSelected,
                        isSelectionMode = isSelectionMode,
                        onClick = {
                            if (isSelectionMode) {
                                onItemClick(exercise.id)
                            } else if (mode == ExercisesScreenmode.MANAGEMENT) {
                                onViewDetailsClick(exercise.id, exercise.name)
                            }
                        },
                        onLongClick = { onItemClick(exercise.id) },
                        onViewDetailsClick = { onViewDetailsClick(exercise.id, exercise.name) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExerciseListItem(
    exercise: Exercise,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onViewDetailsClick: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp) // Corrected: Set a fixed height
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 16.dp), // Corrected: Remove vertical padding
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (exercise.category.isNotEmpty()) "${exercise.name} (${exercise.category})" else exercise.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        if (!isSelectionMode) {
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Exercise Options")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Details") },
                        onClick = {
                            showMenu = false
                            onViewDetailsClick()
                        }
                    )
                }
            }
        }
    }
}