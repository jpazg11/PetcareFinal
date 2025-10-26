// En el archivo: PetScreen.kt
package com.umg.petcarefinal

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Pets // <-- 'P' mayúscula
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter

@Composable
fun PetScreen(petViewModel: PetViewModel = viewModel()) {
    val pets by petViewModel.pets.collectAsState()

    // Estados para controlar los diálogos
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Pet?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Pet?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Añadir Mascota")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (pets.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Aún no tienes mascotas registradas.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(pets) { pet ->
                        PetCard(
                            pet = pet,
                            onEditClick = { showEditDialog = pet },
                            onDeleteClick = { showDeleteDialog = pet }
                        )
                    }
                }
            }
        }
    }

    // --- DIÁLOGOS ---
    if (showAddDialog) {
        AddOrEditPetDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, type, age, imageUri ->
                petViewModel.addPet(name, type, age, imageUri)
                showAddDialog = false
            }
        )
    }

    showEditDialog?.let { petToEdit ->
        AddOrEditPetDialog(
            pet = petToEdit,
            onDismiss = { showEditDialog = null },
            onConfirm = { name, type, age, newImageUri ->
                val updatedPet = petToEdit.copy(
                    name = name,
                    type = type,
                    age = age.toIntOrNull() ?: petToEdit.age
                )
                petViewModel.updatePet(updatedPet, newImageUri)
                showEditDialog = null
            }
        )
    }

    showDeleteDialog?.let { petToDelete ->
        DeleteConfirmationDialog(
            petName = petToDelete.name,
            onDismiss = { showDeleteDialog = null },
            onConfirm = {
                petViewModel.deletePet(petToDelete.id)
                showDeleteDialog = null
            }
        )
    }
}


@Composable
fun PetCard(pet: Pet, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = pet.photoUrl.ifEmpty { R.drawable.ic_launcher_foreground }),
                contentDescription = "Foto de ${pet.name}",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = pet.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(text = "Tipo: ${pet.type}")
                Text(text = "Edad: ${pet.age} años")
            }
            IconButton(onClick = onEditClick) {
                Icon(Icons.Filled.Edit, contentDescription = "Editar")
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Filled.Delete, contentDescription = "Borrar")
            }
        }
    }
}


@Composable
fun AddOrEditPetDialog(
    pet: Pet? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Uri?) -> Unit
) {
    var name by remember { mutableStateOf(pet?.name ?: "") }
    var type by remember { mutableStateOf(pet?.type ?: "") }
    var age by remember { mutableStateOf(pet?.age?.toString() ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (pet == null) "Añadir Nueva Mascota" else "Editar Mascota") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        Image(painter = rememberAsyncImagePainter(model = imageUri), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else if (pet?.photoUrl?.isNotEmpty() == true) {
                        Image(painter = rememberAsyncImagePainter(model = pet.photoUrl), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        // --- ESTA ES LA LÍNEA CORREGIDA ---
                        Icon(Icons.Outlined.Pets, contentDescription = "Añadir foto", tint = Color.Gray) // <-- 'P' mayúscula
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Tipo (ej. Perro, Gato)") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text("Edad") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, type, age, imageUri) },
                enabled = name.isNotBlank() && type.isNotBlank() && age.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun DeleteConfirmationDialog(
    petName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar Borrado") },
        text = { Text("¿Estás seguro de que quieres eliminar a $petName? Esta acción no se puede deshacer.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
