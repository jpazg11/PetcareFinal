// En el archivo: AppointmentScreen.kt
package com.umg.petcarefinal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AppointmentScreen(
    appointmentViewModel: AppointmentViewModel = viewModel(),
    petViewModel: PetViewModel = viewModel()
) {
    val pendingAppointments by appointmentViewModel.pendingAppointments.collectAsState()
    val completedAppointments by appointmentViewModel.completedAppointments.collectAsState()
    val userPets by petViewModel.pets.collectAsState()

    var showAddAppointmentDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddAppointmentDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Añadir Cita")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Text("Próximas Citas", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 8.dp)) }
            if (pendingAppointments.isEmpty()) {
                item { Text("No tienes citas pendientes.") }
            } else {
                items(pendingAppointments) { appointment ->
                    AppointmentCard(
                        appointment = appointment,
                        onCompleteClick = { appointmentViewModel.completeAppointment(appointment.id) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item { Text("Historial de Citas", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 8.dp)) }
            if (completedAppointments.isEmpty()) {
                item { Text("No tienes citas en tu historial.") }
            } else {
                items(completedAppointments) { appointment ->
                    AppointmentCard(appointment = appointment)
                }
            }
        }
    }

    if (showAddAppointmentDialog) {
        AddAppointmentDialog(
            userPets = userPets,
            onDismiss = { showAddAppointmentDialog = false },
            onConfirm = { pet, vetName, reason, date ->
                appointmentViewModel.addAppointment(pet, vetName, reason, date)
                showAddAppointmentDialog = false
            }
        )
    }
}

// --- INICIO DE LA SECCIÓN CORREGIDA ---

@OptIn(ExperimentalMaterial3Api::class) // Anotación necesaria
@Composable
fun AddAppointmentDialog(
    userPets: List<Pet>,
    onDismiss: () -> Unit,
    onConfirm: (Pet, String, String, Date) -> Unit
) {
    var selectedPet by remember { mutableStateOf(userPets.firstOrNull()) }
    var vetName by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    // 1. Estado para la fecha que se mostrará y se enviará
    var selectedDate by remember { mutableStateOf(Date()) }
    var showDatePicker by remember { mutableStateOf(false) }

    // El estado del DatePicker ahora se inicializa con nuestro estado
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.time,
        yearRange = (Calendar.getInstance().get(Calendar.YEAR))..(Calendar.getInstance().get(Calendar.YEAR) + 5)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agendar Nueva Cita") },
        text = {
            Column {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedPet?.name ?: "Selecciona una mascota",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Mascota") },
                        trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        userPets.forEach { pet ->
                            DropdownMenuItem(
                                text = { Text(pet.name) },
                                onClick = {
                                    selectedPet = pet
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = vetName, onValueChange = { vetName = it }, label = { Text("Nombre del Veterinario") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Motivo de la Cita") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    // 2. El valor ahora viene de nuestro estado 'selectedDate'
                    value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fecha") },
                    modifier = Modifier.clickable { showDatePicker = true }
                )
            }
        },
        confirmButton = {
            Button(
                // 3. Pasamos la 'selectedDate' que está en nuestro estado
                onClick = { selectedPet?.let { onConfirm(it, vetName, reason, selectedDate) } },
                enabled = selectedPet != null && vetName.isNotBlank() && reason.isNotBlank()
            ) {
                Text("Agendar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    // 4. Al presionar OK, actualizamos nuestro estado 'selectedDate'
                    datePickerState.selectedDateMillis?.let {
                        // Se usa un nuevo objeto Date para asegurar la actualización del estado
                        selectedDate = Date(it)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
// --- FIN DE LA SECCIÓN CORREGIDA ---


@Composable
fun AppointmentCard(
    appointment: Appointment,
    onCompleteClick: (() -> Unit)? = null
) {
    val sdf = SimpleDateFormat("dd/MM/yyyy 'a las' HH:mm", Locale.getDefault())
    val formattedDate = sdf.format(appointment.date.toDate())

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Mascota: ${appointment.petName}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Motivo: ${appointment.reason}")
            Text("Veterinario: ${appointment.vetName}")
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                if (onCompleteClick != null) {
                    IconButton(onClick = onCompleteClick) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Marcar como Completada",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}



