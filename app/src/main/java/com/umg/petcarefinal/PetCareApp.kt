// En el archivo: PetCareApp.kt
package com.umg.petcarefinal

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

/**
 * Esta clase se ejecuta al iniciar la aplicación.
 * Su propósito es configurar cosas que se necesitan una sola vez,
 * como el canal de notificaciones.
 */
class PetCareApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        // Los canales de notificación solo son necesarios para Android 8 (API 26) o superior.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Recordatorios de Citas"
            val descriptionText = "Notificaciones para recordar las citas de tus mascotas"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("petcare_channel_id", name, importance).apply {
                description = descriptionText
            }

            // Registrar el canal en el sistema
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
    