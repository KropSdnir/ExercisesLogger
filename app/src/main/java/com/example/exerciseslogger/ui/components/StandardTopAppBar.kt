// File: app/src/main/java/com/example/exerciseslogger/ui/components/StandardTopAppBar.kt
// Timestamp: Updated on 2025-08-20 23:48:00
// Scope: A reusable TopAppBar for all non-home screens.

package com.example.exerciseslogger.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.exerciseslogger.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardTopAppBar(
    title: String,
    onMenuClick: () -> Unit,
    onNavigateUp: () -> Unit
) {
    TopAppBar(
        title = { Text(title) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        navigationIcon = {
            Row {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.Menu, contentDescription = "Toggle Menu")
                }
                IconButton(onClick = onNavigateUp) {
                    Icon(
                        painter = painterResource(id = R.drawable.home_back_icon),
                        contentDescription = "Back",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    )
}