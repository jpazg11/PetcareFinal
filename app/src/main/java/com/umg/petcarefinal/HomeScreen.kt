// En el archivo: HomeScreen.kt
package com.umg.petcarefinal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow

// --- IMPORTACIONES QUE PUEDEN FALTAR ---
import com.umg.petcarefinal.PetScreen
import com.umg.petcarefinal.AppointmentScreen
import com.umg.petcarefinal.ReportsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Mascotas", "Citas", "Reportes")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PetCare") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Filled.ExitToApp, contentDescription = "Cerrar Sesión")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> PetScreen()
                1 -> AppointmentScreen()
                2 -> ReportsScreen()
            }
        }
    }
}
