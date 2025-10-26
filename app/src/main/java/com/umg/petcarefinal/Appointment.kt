// En el nuevo archivo: Appointment.kt
package com.umg.petcarefinal

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Appointment(
    @DocumentId
    val id: String = "",
    val userId: String = "",       // ID del usuario dueño de la cita
    val petId: String = "",          // ID de la mascota a la que pertenece la cita
    val petName: String = "",        // Nombre de la mascota (para mostrarlo fácilmente)
    val vetName: String = "",        // Nombre del veterinario
    val reason: String = "",         // Motivo de la cita
    val date: Timestamp = Timestamp.now(), // Fecha y hora de la cita
    val status: String = "Pendiente" // Estado: "Pendiente" o "Completada"
)
