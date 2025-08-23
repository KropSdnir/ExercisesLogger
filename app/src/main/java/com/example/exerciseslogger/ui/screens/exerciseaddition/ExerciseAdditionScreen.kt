// File: app/src/main/java/com/example/exerciseslogger/ui/screens/exerciseaddition/ExerciseAdditionScreen.kt
// Timestamp: Updated on 2025-08-21 18:45:13
// Scope: The UI is updated to include a multi-line text field for exercise notes.

package com.example.exerciseslogger.ui.screens.exerciseaddition

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.exerciseslogger.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseAdditionScreen(
    onMenuClick: () -> Unit,
    onNavigateUp: () -> Unit,
    viewModel: ExerciseAdditionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.showAddCategoryDialog) {
        AddCategoryDialog(
            newCategoryName = uiState.newCategoryName,
            onNameChange = viewModel::onNewCategoryNameChange,
            onConfirm = viewModel::addNewCategory,
            onDismiss = viewModel::dismissAddCategoryDialog
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exercise Addition") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                navigationIcon = {
                    Row {
                        IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, "Menu") }
                        IconButton(onClick = onNavigateUp) {
                            Icon(painterResource(R.drawable.home_back_icon), "Back", Modifier.size(24.dp))
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            ExposedDropdownMenuBox(
                expanded = uiState.isCategoryDropdownExpanded,
                onExpandedChange = { viewModel.toggleCategoryDropdown() },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = uiState.selectedCategory,
                    onValueChange = {},
                    label = { Text("Category") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = uiState.isCategoryDropdownExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = uiState.isCategoryDropdownExpanded,
                    onDismissRequest = { viewModel.toggleCategoryDropdown() }
                ) {
                    uiState.categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = { viewModel.onCategorySelected(category) }
                        )
                    }
                }
            }

            // New "Notes" text field
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::onNotesChange,
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 4
            )

            Spacer(modifier = Modifier.weight(1f)) // Pushes the button to the bottom

            Button(
                onClick = {
                    viewModel.onAddClicked()
                    onNavigateUp()
                },
                enabled = uiState.name.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add")
            }
        }
    }
}

@Composable
private fun AddCategoryDialog(
    newCategoryName: String,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Category") },
        text = {
            OutlinedTextField(
                value = newCategoryName,
                onValueChange = onNameChange,
                label = { Text("Category Name") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}