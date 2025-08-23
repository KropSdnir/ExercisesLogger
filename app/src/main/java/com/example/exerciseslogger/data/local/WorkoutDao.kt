// File: app/src/main/java/com/example/exerciseslogger/data/local/WorkoutDao.kt
// Timestamp: Updated on 2025-08-22 13:54:23
// Scope: The complete and correct DAO with all necessary functions.

package com.example.exerciseslogger.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Transaction
    @Query("SELECT * FROM workouts WHERE date = :date")
    fun getWorkoutForDate(date: Long): Flow<WorkoutWithExercises?>

    @Query("SELECT * FROM workouts WHERE date = :date LIMIT 1")
    suspend fun getWorkoutByDate(date: Long): Workout?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: Workout): Long

    @Update
    suspend fun updateWorkout(workout: Workout)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: WorkoutSetEntry)

    @Query("SELECT * FROM workout_sets WHERE workoutId = :workoutId AND exerciseId = :exerciseId ORDER BY setNumber ASC")
    fun getSetsForExercise(workoutId: Int, exerciseId: Int): Flow<List<WorkoutSetEntry>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWorkoutExercises(entries: List<WorkoutExercise>)

    @Query("DELETE FROM workout_exercises WHERE workoutId = :workoutId AND exerciseId IN (:exerciseIds)")
    suspend fun deleteWorkoutExercises(workoutId: Int, exerciseIds: List<Int>)

    @Query("DELETE FROM workout_sets WHERE id IN (:setIds)")
    suspend fun deleteSetsByIds(setIds: List<Int>)

    @Update
    suspend fun updateSets(sets: List<WorkoutSetEntry>)

    @Update
    suspend fun updateSet(set: WorkoutSetEntry)

    @Query("SELECT * FROM workouts WHERE date = :date LIMIT 1")
    fun getWorkoutFlowByDate(date: Long): Flow<Workout?>

    @Query("SELECT * FROM workout_exercises WHERE workoutId = :workoutId AND exerciseId = :exerciseId LIMIT 1")
    fun getWorkoutExercise(workoutId: Int, exerciseId: Int): Flow<WorkoutExercise?>

    @Update
    suspend fun updateWorkoutExercise(entry: WorkoutExercise)
}