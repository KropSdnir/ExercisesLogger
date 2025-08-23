// File: app/src/main/java/com/example/exerciseslogger/ui/screens/exercisedetails/ExerciseDetailsScreen.kt
// Timestamp: Updated on 2025-08-21 21:31:13
// Scope: Implements the UI for the Exercise Details screen, including the History and Stats tabs.

package com.example.exerciseslogger.ui.screens.exercisedetails

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.exerciseslogger.ui.components.StandardTopAppBar

@Composable
fun ExerciseDetailsScreen(
    onMenuClick: () -> Unit,
    onNavigateUp: () -> Unit,
    viewModel: ExerciseDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val tabTitles = listOf("History", "Stats")

    Scaffold(
        topBar = {
            StandardTopAppBar(
                title = uiState.exerciseName,
                onMenuClick = onMenuClick,
                onNavigateUp = onNavigateUp
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
                0 -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("History Content (Not Implemented)") }
                1 -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Stats Content (Not Implemented)") }
            }
        }
    }
}