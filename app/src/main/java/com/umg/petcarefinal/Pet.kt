// En el nuevo archivo: Pet.kt
package com.umg.petcarefinal

import com.google.firebase.firestore.DocumentId

data class Pet(
    @DocumentId // Anotación para que Firestore mapee el ID del documento aquí
    val id: String = "",
    val userId: String = "", // ID del usuario dueño de la mascota
    val name: String = "",
    val type: String = "",
    val age: Int = 0,
    val photoUrl: String = "" // URL de la foto en Firebase Storage
)
