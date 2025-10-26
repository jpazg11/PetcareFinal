
// En MainActivity.kt
package com.umg.petcarefinal

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.umg.petcarefinal.ui.theme.PetcareFinalTheme

class MainActivity : ComponentActivity() {

    // --- INICIO DE CAMBIOS PARA NOTIFICACIONES ---

    // 1. Lanzador para solicitar el permiso de notificación
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permiso concedido. No es necesario hacer nada extra aquí.
        } else {
            // Permiso denegado. Podríamos mostrar un mensaje al usuario.
        }
    }

    // 2. Función para pedir el permiso
    private fun askNotificationPermission() {
        // Solo se necesita en Android 13 (TIRAMISU) o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // El permiso ya está concedido.
            } else {
                // El permiso no está concedido, lo solicitamos.
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // --- FIN DE CAMBIOS PARA NOTIFICACIONES ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        askNotificationPermission() // 3. Llamamos a la función al crear la actividad

        setContent {
            PetcareFinalTheme {
                // Simplemente llamamos a nuestro composable de navegación
                AppNavigation()
            }
        }
    }
}



