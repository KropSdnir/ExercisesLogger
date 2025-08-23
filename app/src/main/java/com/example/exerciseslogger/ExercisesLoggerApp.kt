// File: app/src/main/java/com/example/exerciseslogger/ExercisesLoggerApp.kt
// Timestamp: Updated on 2025-08-20 21:22:11
// Scope: This is the main Application class for the app. The @HiltAndroidApp annotation initializes Hilt for dependency injection, making it the entry point for the entire application's lifecycle and dependency graph.

package com.example.exerciseslogger

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ExercisesLoggerApp : Application()