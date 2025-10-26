// En el archivo: Notification.kt
package com.umg.petcarefinal

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

/**
 * Este BroadcastReceiver se activa por el AlarmManager para mostrar la notificación de la cita.
 */
class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Obtenemos los datos extra que enviamos con la alarma
        val petName = intent.getStringExtra("petName") ?: "tu mascota"
        val reason = intent.getStringExtra("reason") ?: "una cita"

        // Usamos el hash del ID de la cita como ID de la notificación para poder cancelarla si es necesario
        val notificationId = intent.getIntExtra("notificationId", 0)

        // Llamamos a la función que construye y muestra la notificación
        showNotification(context, petName, reason, notificationId)
    }

    /**
     * Construye y muestra la notificación en la barra de estado del dispositivo.
     */
    private fun showNotification(context: Context, petName: String, reason: String, notificationId: Int) {
        // Obtenemos el servicio de notificaciones del sistema
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // El texto que aparecerá en la notificación
        val contentText = "Recuerda que hoy tienes una cita para $petName por: $reason."

        // Construimos la notificación usando NotificationCompat para máxima compatibilidad
        val notification = NotificationCompat.Builder(context, "petcare_channel_id") // El ID del canal debe coincidir con el creado en PetCareApp
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Asegúrate de que este drawable existe
            .setContentTitle("Recordatorio de Cita PetCare")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText)) // Permite que el texto se expanda
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Prioridad alta para que aparezca como notificación emergente
            .setAutoCancel(true) // La notificación se cierra automáticamente cuando el usuario la toca
            .build()

        // Mostramos la notificación
        notificationManager.notify(notificationId, notification)
    }
}
