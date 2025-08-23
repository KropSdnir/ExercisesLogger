// File: app/src/main/java/com/example/exerciseslogger/di/DatabaseModule.kt
// Timestamp: Updated on 2025-08-21 00:20:38
// Scope: Updated to include a RoomDatabase.Callback that pre-populates the database with a default list of exercises upon creation.

package com.example.exerciseslogger.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.exerciseslogger.data.local.AppDatabase
import com.example.exerciseslogger.data.local.Exercise
import com.example.exerciseslogger.data.local.ExerciseDao
import com.example.exerciseslogger.data.local.WorkoutDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        // Hilt provides a Provider<> to break a dependency cycle
        exerciseDaoProvider: Provider<ExerciseDao>
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "exercises_logger_db"
        )
            .fallbackToDestructiveMigration()
            .addCallback(AppDatabaseCallback(exerciseDaoProvider)) // Add the callback here
            .build()
    }

    @Provides
    @Singleton
    fun provideExerciseDao(appDatabase: AppDatabase): ExerciseDao {
        return appDatabase.exerciseDao()
    }

    @Provides
    @Singleton
    fun provideWorkoutDao(appDatabase: AppDatabase): WorkoutDao {
        return appDatabase.workoutDao()
    }
}

// This class contains the logic to run when the database is first created.
private class AppDatabaseCallback(
    private val exerciseDaoProvider: Provider<ExerciseDao>
) : RoomDatabase.Callback() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        applicationScope.launch {
            populateDatabase()
        }
    }

    private suspend fun populateDatabase() {
        val startingExercises = listOf(
            Exercise(name = "Squat", category = "Legs"),
            Exercise(name = "Bench", category = "Chest"),
            Exercise(name = "Deadlift", category = "Legs")
        )

        startingExercises.forEach {
            exerciseDaoProvider.get().insertExercise(it)
        }
    }
}