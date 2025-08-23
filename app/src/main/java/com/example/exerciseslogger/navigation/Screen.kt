// File: app/src/main/java/com/example/exerciseslogger/navigation/Screen.kt
// Timestamp: Updated on 2025-08-21 21:31:13
// Scope: The navigation graph is updated to include the new ExerciseDetails route.

package com.example.exerciseslogger.navigation

import java.time.LocalDate

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object WeightWorkout : Screen("weight_workout")
    object Exercises : Screen("exercises/{date}/{mode}") {
        fun createRoute(date: LocalDate, mode: ExercisesScreenmode) = "exercises/${date.toEpochDay()}/${mode.name}"
    }
    object ExerciseAddition : Screen("exercise_addition")
    object ExerciseDetails : Screen("exercise_details/{exerciseId}/{exerciseName}") {
        fun createRoute(exerciseId: Int, exerciseName: String) = "exercise_details/$exerciseId/$exerciseName"
    }
    object WeightExerciseTracking : Screen("weight_exercise_tracking/{exerciseId}/{exerciseName}/{date}") {
        fun createRoute(exerciseId: Int, exerciseName: String, date: LocalDate) =
            "weight_exercise_tracking/$exerciseId/$exerciseName/${date.toEpochDay()}"
    }
}