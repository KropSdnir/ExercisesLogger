// File: app/src/main/java/com/example/exerciseslogger/data/local/AppDatabase.kt
// Timestamp: Updated on 2025-08-22 13:29:51
// Scope: The database version is incremented to 18 to trigger the migration.

package com.example.exerciseslogger.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Exercise::class, Workout::class, WorkoutSetEntry::class, WorkoutExercise::class],
    version = 18, // Incremented version
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
}