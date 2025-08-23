// File: app/src/main/java/com/example/exerciseslogger/MainActivity.kt
// Timestamp: Updated on 2025-08-22 01:03:12
// Scope: The complete and correct version that calculates and passes the window size class.

package com.example.exerciseslogger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.exerciseslogger.navigation.ExercisesScreenmode
import com.example.exerciseslogger.navigation.Screen
import com.example.exerciseslogger.ui.components.AppDrawer
import com.example.exerciseslogger.ui.screens.exerciseaddition.ExerciseAdditionScreen
import com.example.exerciseslogger.ui.screens.exercisedetails.ExerciseDetailsScreen
import com.example.exerciseslogger.ui.screens.exercises.ExercisesScreen
import com.example.exerciseslogger.ui.screens.home.HomeScreen
import com.example.exerciseslogger.ui.screens.weightexercisetracking.WeightExerciseTrackingScreen
import com.example.exerciseslogger.ui.screens.weightworkout.WeightWorkoutScreen
import com.example.exerciseslogger.ui.screens.weightworkout.WeightWorkoutViewModel
import com.example.exerciseslogger.ui.theme.ExercisesLoggerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExercisesLoggerTheme {
                val windowSizeClass = calculateWindowSizeClass(this)
                AppShell(
                    widthSizeClass = windowSizeClass.widthSizeClass
                )
            }
        }
    }
}

@Composable
fun AppShell(widthSizeClass: WindowWidthSizeClass) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { AppDrawer(navController = navController) { scope.launch { drawerState.close() } } }
    ) {
        NavHost(navController = navController, startDestination = Screen.Home.route) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onNavigateToWeightWorkout = { navController.navigate(Screen.WeightWorkout.route) }
                )
            }
            composable(Screen.WeightWorkout.route) {
                val viewModel: WeightWorkoutViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsState()

                WeightWorkoutScreen(
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onNavigateHome = { navController.navigateUp() },
                    onNavigateToExercises = {
                        navController.navigate(Screen.Exercises.createRoute(uiState.selectedDate, ExercisesScreenmode.SELECTION))
                    },
                    onNavigateToTracking = { exerciseId, exerciseName ->
                        navController.navigate(
                            Screen.WeightExerciseTracking.createRoute(exerciseId, exerciseName, uiState.selectedDate)
                        )
                    },
                    viewModel = viewModel
                )
            }
            composable(
                route = Screen.Exercises.route,
                arguments = listOf(
                    navArgument("date") { type = NavType.LongType },
                    navArgument("mode") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val dateEpochDay = backStackEntry.arguments?.getLong("date") ?: LocalDate.now().toEpochDay()
                val date = LocalDate.ofEpochDay(dateEpochDay)
                val mode = backStackEntry.arguments?.getString("mode")?.let { ExercisesScreenmode.valueOf(it) } ?: ExercisesScreenmode.SELECTION

                ExercisesScreen(
                    workoutDate = date,
                    mode = mode,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onNavigateUp = { navController.navigateUp() },
                    onNavigateToExerciseAddition = { navController.navigate(Screen.ExerciseAddition.route) },
                    onNavigateToExerciseDetails = { exerciseId, exerciseName ->
                        navController.navigate(Screen.ExerciseDetails.createRoute(exerciseId, exerciseName))
                    }
                )
            }
            composable(Screen.ExerciseAddition.route) {
                ExerciseAdditionScreen(
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onNavigateUp = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.ExerciseDetails.route,
                arguments = listOf(
                    navArgument("exerciseId") { type = NavType.IntType },
                    navArgument("exerciseName") { type = NavType.StringType }
                )
            ) {
                ExerciseDetailsScreen(
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onNavigateUp = { navController.navigateUp() }
                )
            }
            composable(
                route = Screen.WeightExerciseTracking.route,
                arguments = listOf(
                    navArgument("exerciseId") { type = NavType.IntType },
                    navArgument("exerciseName") { type = NavType.StringType },
                    navArgument("date") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val exerciseName = backStackEntry.arguments?.getString("exerciseName") ?: "Track Exercise"
                WeightExerciseTrackingScreen(
                    widthSizeClass = widthSizeClass,
                    exerciseName = exerciseName,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onNavigateUp = { navController.navigateUp() }
                )
            }
        }
    }
}