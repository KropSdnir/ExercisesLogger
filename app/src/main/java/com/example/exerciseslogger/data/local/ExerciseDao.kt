// File: app/src/main/java/com/example/exerciseslogger/data/local/ExerciseDao.kt
// Timestamp: Updated on 2025-08-20 23:12:19
// Scope: Adds a new query to fetch a distinct list of all categories from the database for the filter dropdown.

package com.example.exerciseslogger.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: Exercise)

    @Query("SELECT * FROM exercises ORDER BY name ASC")
    fun getAllExercises(): Flow<List<Exercise>>

    @Query("SELECT DISTINCT category FROM exercises WHERE category != '' ORDER BY category ASC")
    fun getUniqueCategories(): Flow<List<String>>
}