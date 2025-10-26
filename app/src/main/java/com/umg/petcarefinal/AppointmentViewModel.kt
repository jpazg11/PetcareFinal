// En el archivo: AppointmentViewModel.kt
package com.umg.petcarefinal

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar
import java.util.Date

class AppointmentViewModel(application: Application) : AndroidViewModel(application) {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val alarmManager = application.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private val _pendingAppointments = MutableStateFlow<List<Appointment>>(emptyList())
    val pendingAppointments = _pendingAppointments.asStateFlow()

    private val _completedAppointments = MutableStateFlow<List<Appointment>>(emptyList())
    val completedAppointments = _completedAppointments.asStateFlow()

    private val _weeklyAppointments = MutableStateFlow<List<Appointment>>(emptyList())
    val weeklyAppointments = _weeklyAppointments.asStateFlow()

    private val _pets = MutableStateFlow<List<Pet>>(emptyList())
    val pets = _pets.asStateFlow()

    init {
        loadAppointments()
        loadPetsForUser()
        loadWeeklyAppointments()
    }

    private fun loadAppointments() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("appointments").whereEqualTo("userId", userId).whereEqualTo("status", "Pendiente").orderBy("date", Query.Direction.ASCENDING).addSnapshotListener { snapshots, e ->
            if (e != null) { Log.w("AppointmentVM", "Listen failed for pending.", e); return@addSnapshotListener }
            snapshots?.let { _pendingAppointments.value = it.toObjects(Appointment::class.java) }
        }
        db.collection("appointments").whereEqualTo("userId", userId).whereEqualTo("status", "Completada").orderBy("date", Query.Direction.DESCENDING).addSnapshotListener { snapshots, e ->
            if (e != null) { Log.w("AppointmentVM", "Listen failed for completed.", e); return@addSnapshotListener }
            snapshots?.let { _completedAppointments.value = it.toObjects(Appointment::class.java) }
        }
    }

    private fun loadPetsForUser() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("pets").whereEqualTo("userId", userId).addSnapshotListener { snapshots, e ->
            if (e != null) { Log.w("AppointmentVM", "Listen failed for pets.", e); return@addSnapshotListener }
            snapshots?.let { _pets.value = it.toObjects(Pet::class.java) }
        }
    }

    private fun loadWeeklyAppointments() {
        val userId = auth.currentUser?.uid ?: return
        val calendar = Calendar.getInstance()
        val today = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, 7)
        val sevenDaysLater = calendar.time

        db.collection("appointments").whereEqualTo("userId", userId).whereEqualTo("status", "Pendiente").whereGreaterThanOrEqualTo("date", Timestamp(today)).whereLessThanOrEqualTo("date", Timestamp(sevenDaysLater)).orderBy("date", Query.Direction.ASCENDING).addSnapshotListener { snapshots, e ->
            if (e != null) { Log.w("AppointmentVM", "Listen failed for weekly report.", e); return@addSnapshotListener }
            snapshots?.let { _weeklyAppointments.value = it.toObjects(Appointment::class.java) }
        }
    }

    fun addAppointment(pet: Pet, vetName: String, reason: String, date: Date) {
        val userId = auth.currentUser?.uid ?: return
        val newAppointment = Appointment(userId = userId, petId = pet.id, petName = pet.name, vetName = vetName.trim(), reason = reason.trim(), date = Timestamp(date), status = "Pendiente")

        db.collection("appointments").add(newAppointment).addOnSuccessListener { docRef ->
            Log.d("AppointmentVM", "Cita añadida con éxito: ${docRef.id}")
            scheduleNotification(docRef.id, pet.name, reason.trim(), date)
        }.addOnFailureListener { e -> Log.w("AppointmentVM", "Error al añadir la cita", e) }
    }

    fun completeAppointment(appointmentId: String) {
        if (appointmentId.isBlank()) return
        db.collection("appointments").document(appointmentId).update("status", "Completada").addOnSuccessListener {
            Log.d("AppointmentVM", "Cita marcada como completada: $appointmentId")
            cancelNotification(appointmentId)
        }.addOnFailureListener { e -> Log.w("AppointmentVM", "Error al completar la cita", e) }
    }

    private fun scheduleNotification(appointmentId: String, petName: String, reason: String, appointmentDate: Date) {
        val intent = Intent(getApplication(), NotificationReceiver::class.java).apply {
            putExtra("petName", petName)
            putExtra("reason", reason)
            putExtra("notificationId", appointmentId.hashCode())
        }
        val pendingIntent = PendingIntent.getBroadcast(getApplication(), appointmentId.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notificationTime = appointmentDate.time - (2 * 60 * 60 * 1000)
        if (notificationTime > System.currentTimeMillis()) {
            try {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent)
                Log.d("AppointmentVM", "Alarma programada para la cita $appointmentId")
            } catch (e: SecurityException) {
                Log.e("AppointmentVM", "No se pudo programar la alarma exacta. ¿Falta el permiso?", e)
            }
        }
    }

    private fun cancelNotification(appointmentId: String) {
        val intent = Intent(getApplication(), NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(getApplication(), appointmentId.hashCode(), intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            Log.d("AppointmentVM", "Alarma cancelada para la cita $appointmentId")
        }
    }
}
