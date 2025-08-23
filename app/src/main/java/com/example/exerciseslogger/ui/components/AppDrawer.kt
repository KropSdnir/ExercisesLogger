// File: app/src/main/java/com/example/exerciseslogger/ui/components/AppDrawer.kt
// Timestamp: Updated on 2025-08-21 20:54:57
// Scope: The 'Exercises' item now navigates using the renamed ExercisesScreenmode enum.

package com.example.exerciseslogger.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.exerciseslogger.navigation.ExercisesScreenmode
import com.example.exerciseslogger.navigation.Screen
import java.time.LocalDate

@Composable
fun AppDrawer(
    navController: NavController,
    closeDrawer: () -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    ModalDrawerSheet {
        Spacer(Modifier.height(12.dp))
        // Home Item
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Home") },
            selected = currentRoute == Screen.Home.route,
            onClick = {
                navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } }
                closeDrawer()
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        // Weight Workout Item
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.FitnessCenter, contentDescription = null) },
            label = { Text("Weight Workout") },
            selected = currentRoute == Screen.WeightWorkout.route,
            onClick = {
                navController.navigate(Screen.WeightWorkout.route)
                closeDrawer()
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        // Exercises Item
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.List, contentDescription = null) },
            label = { Text("Exercises") },
            selected = currentRoute?.startsWith("exercises") == true,
            onClick = {
                navController.navigate(Screen.Exercises.createRoute(LocalDate.now(), ExercisesScreenmode.MANAGEMENT))
                closeDrawer()
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
    }
}