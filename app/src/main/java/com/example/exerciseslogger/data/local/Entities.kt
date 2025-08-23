// File: app/src/main/java/com/example/exerciseslogger/data/local/Entities.kt
// Timestamp: Updated on 2025-08-22 13:29:51
// Scope: The WorkoutExerciseCrossRef is converted to a full entity to store exercise-specific start and end times.

package com.example.exerciseslogger.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val category: String,
    val notes: String = ""
)

@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: Long,
    val workoutStartTime: Long? = null,
    val workoutEndTime: Long? = null
)

@Entity(
    tableName = "workout_exercises",
    primaryKeys = ["workoutId", "exerciseId"],
    indices = [Index(value = ["exerciseId"])]
)
data class WorkoutExercise(
    val workoutId: Int,
    val exerciseId: Int,
    val exerciseStartTime: Long? = null,
    val exerciseEndTime: Long? = null
)

@Entity(tableName = "workout_sets")
data class WorkoutSetEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val workoutId: Int,
    val exerciseId: Int,
    val setNumber: Int,
    val weight: Double,
    val reps: Int,
    val notes: String = "",
    val isCompleted: Boolean = false,
    val rpe: Float? = null,
    val isRpeLocked: Boolean = false,
    val startTime: Long? = null,
    val exerciseTime: Long? = null,
    val restTime: Long? = null,
    val completionTime: Long? = null
)

data class WorkoutWithExercises(
    @Embedded val workout: Workout,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(WorkoutExercise::class, parentColumn = "workoutId", entityColumn = "exerciseId")
    )
    val exercises: List<Exercise>
)