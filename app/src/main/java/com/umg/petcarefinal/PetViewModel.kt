// En el archivo: PetViewModel.kt
package com.umg.petcarefinal

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class PetViewModel : ViewModel() {

    // Inicialización explícita de los servicios de Firebase
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    // Flujo para mantener la lista de mascotas actualizada
    private val _pets = MutableStateFlow<List<Pet>>(emptyList())
    val pets = _pets.asStateFlow()

    init {
        loadPets()
    }

    private fun loadPets() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("pets")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("PetViewModel", "Error al escuchar cambios en mascotas.", e)
                    return@addSnapshotListener
                }
                snapshots?.let {
                    _pets.value = it.toObjects(Pet::class.java)
                    Log.d("PetViewModel", "Mascotas cargadas/actualizadas: ${it.size()} mascotas.")
                }
            }
    }

    /**
     * Añade una nueva mascota. Sube la foto si existe y luego guarda los datos en Firestore.
     */
    fun addPet(name: String, type: String, age: String, imageUri: Uri?) {
        val userId = auth.currentUser?.uid ?: return
        val ageInt = age.toIntOrNull()
        if (ageInt == null) {
            Log.e("PetViewModel", "La edad introducida no es un número válido: $age")
            return
        }

        // Si el usuario seleccionó una imagen, la subimos primero.
        if (imageUri != null) {
            uploadPhoto(imageUri) { photoUrl ->
                // Este bloque se ejecuta SOLO si la foto se subió con éxito.
                // Con la URL de la foto, creamos el documento en Firestore.
                val newPet = Pet(userId = userId, name = name.trim(), type = type.trim(), age = ageInt, photoUrl = photoUrl)
                savePetToFirestore(newPet)
            }
        } else {
            // Si no hay imagen, creamos el documento directamente con la URL vacía.
            val newPet = Pet(userId = userId, name = name.trim(), type = type.trim(), age = ageInt, photoUrl = "")
            savePetToFirestore(newPet)
        }
    }

    /**
     * Función auxiliar que guarda el objeto Pet en la colección "pets" de Firestore.
     */
    private fun savePetToFirestore(pet: Pet) {
        db.collection("pets")
            .add(pet)
            .addOnSuccessListener { documentReference ->
                Log.d("PetViewModel", "ÉXITO: Mascota guardada en Firestore con ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("PetViewModel", "FALLO: Error al guardar la mascota en Firestore.", e)
            }
    }

    /**
     * Actualiza una mascota existente.
     */
    fun updatePet(pet: Pet, newImageUri: Uri?) {
        if (pet.id.isBlank()) {
            Log.e("PetViewModel", "ID de mascota vacío, no se puede actualizar.")
            return
        }

        // Preparamos los datos a actualizar (sin la URL de la foto todavía)
        var petToUpdate = pet.copy(
            name = pet.name.trim(),
            type = pet.type.trim()
        )

        // Si se seleccionó una nueva imagen...
        if (newImageUri != null) {
            uploadPhoto(newImageUri) { photoUrl ->
                // ...la subimos, y cuando tenemos la URL, la añadimos al objeto y guardamos.
                petToUpdate = petToUpdate.copy(photoUrl = photoUrl)
                db.collection("pets").document(pet.id).set(petToUpdate)
                    .addOnSuccessListener { Log.d("PetViewModel", "Mascota actualizada con nueva foto: ${pet.id}") }
                    .addOnFailureListener { e -> Log.w("PetViewModel", "Error al actualizar mascota con foto", e) }
            }
        } else {
            // Si no hay imagen nueva, simplemente guardamos los otros cambios.
            db.collection("pets").document(pet.id).set(petToUpdate)
                .addOnSuccessListener { Log.d("PetViewModel", "Mascota actualizada (sin foto nueva): ${pet.id}") }
                .addOnFailureListener { e -> Log.w("PetViewModel", "Error al actualizar mascota sin foto", e) }
        }
    }

    /**
     * Sube una foto a Firebase Storage y devuelve la URL de descarga.
     */
    private fun uploadPhoto(imageUri: Uri, onSuccess: (String) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        // Creamos un nombre de archivo único para evitar sobreescribir imágenes
        val fileName = "${userId}_${UUID.randomUUID()}.jpg"
        val photoRef = storage.reference.child("pet_photos/$fileName")

        Log.d("PetViewModel", "Iniciando subida de foto: $fileName")
        photoRef.putFile(imageUri)
            .addOnSuccessListener {
                // Una vez subida, obtenemos la URL de descarga
                photoRef.downloadUrl
                    .addOnSuccessListener { uri ->
                        Log.d("PetViewModel", "Foto subida con éxito. URL: $uri")
                        onSuccess(uri.toString()) // Llamamos al callback con la URL
                    }
                    .addOnFailureListener { e ->
                        Log.w("PetViewModel", "Error al obtener la URL de descarga de la foto.", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.w("PetViewModel", "Error al subir la foto a Storage.", e)
            }
    }

    /**
     * Borra una mascota de Firestore.
     */
    fun deletePet(petId: String) {
        if (petId.isBlank()) return
        db.collection("pets").document(petId)
            .delete()
            .addOnSuccessListener { Log.d("PetViewModel", "Mascota borrada con éxito: $petId") }
            .addOnFailureListener { e -> Log.w("PetViewModel", "Error al borrar mascota", e) }
    }
}
