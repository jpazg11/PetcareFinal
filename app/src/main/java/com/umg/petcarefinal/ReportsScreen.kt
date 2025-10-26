// En el nuevo archivo: ReportsScreen.kt
package com.umg.petcarefinal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReportsScreen(appointmentViewModel: AppointmentViewModel = viewModel()) {
    // Obtenemos la lista específica de citas semanales desde el ViewModel
    val weeklyAppointments by appointmentViewModel.weeklyAppointments.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Reporte Semanal",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tienes ${weeklyAppointments.size} citas en los próximos 7 días.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Detalle de Citas",
            style = MaterialTheme.typography.headlineSmall
        )

        if (weeklyAppointments.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("¡Ninguna cita programada para esta semana!")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(weeklyAppointments) { appointment ->
                    // Reutilizamos el AppointmentCard que ya teníamos
                    AppointmentCard(appointment = appointment)
                }
            }
        }
    }
}
