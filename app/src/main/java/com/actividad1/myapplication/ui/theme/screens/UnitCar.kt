package com.actividad1.myapplication.ui.theme.screens

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.actividad1.myapplication.api.models.Car
import com.actividad1.myapplication.api.ApiClient
import com.actividad1.myapplication.api.models.CarWithImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.awaitResponse

@Composable
fun CarStockScreen(navController: NavController) {
    var cars by remember { mutableStateOf<List<Car>>(emptyList()) }
    var showAddEditModal by remember { mutableStateOf(false) }
    var selectedCar by remember { mutableStateOf<Car?>(null) }

    LaunchedEffect(Unit) {
        loadCars { fetchedCars ->
            cars = fetchedCars
        }
    }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Text(
            text = "Stock de Carros",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = { showAddEditModal = true },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Agregar Carro")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(cars.size) { index ->
                val car = cars[index]
                CarItem(
                    car = car,
                    onEdit = { selectedCar = car; showAddEditModal = true },
                    onDelete = {
                        deleteCar(car) { loadCars { cars = it } }
                    }
                )
            }
        }

        if (showAddEditModal) {
            AddEditCarModal(
                car = selectedCar,
                onDismiss = { showAddEditModal = false; selectedCar = null },
                onSave = {
                    loadCars { cars = it }
                    showAddEditModal = false
                },
                onOpenCamera = { navController.navigate("camera") }
            )
        }
    }
}
@Composable
fun CarItem(car: Car, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.elevatedCardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Placa: ${car.placa}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Modelo: ${car.modelo}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Chofer: ${car.chofer}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Activo: ${if (car.activo) "Sí" else "No"}", style = MaterialTheme.typography.bodyMedium)

            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = onEdit, modifier = Modifier.padding(end = 8.dp)) {
                    Text("Editar")
                }
                Button(onClick = onDelete, colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)) {
                    Text("Eliminar")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCarModal(
    car: Car?,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onOpenCamera:() -> Unit
) {
    var placa by remember { mutableStateOf(car?.placa ?: "") }
    var originalPlaca by remember { mutableStateOf(car?.placa ?: "") }
    var modelo by remember { mutableStateOf(car?.modelo ?: "") }
    var chofer by remember { mutableStateOf(car?.chofer ?: "") }
    var activo by remember { mutableStateOf(car?.activo ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (car == null) "Agregar Carro" else "Editar Carro") },
        text = {
            Column {
                OutlinedTextField(
                    value = placa,
                    onValueChange = { placa = it },
                    label = { Text("Placa") },
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = modelo,
                    onValueChange = { modelo = it },
                    label = { Text("Modelo") },
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = chofer,
                    onValueChange = { chofer = it },
                    label = { Text("Chofer") },
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = onOpenCamera) {
                    Text("Abrir Cámara")
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Activo:")
                    Checkbox(checked = activo, onCheckedChange = { activo = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newCar = Car(placa, modelo, chofer, activo, car?._idKit ?: "")
                    saveCarWithImage(newCar, originalPlaca) { onSave() }
                },
                enabled = placa.isNotBlank() && modelo.isNotBlank() && chofer.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

fun loadCars(onSuccess: (List<Car>) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        val response = ApiClient.apiService.getCars().awaitResponse()
        if (response.isSuccessful) {
            response.body()?.let { onSuccess(it) }
        }
    }
}
fun saveCarWithImage(car: Car, imageBase64: String, onComplete: () -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val carWithImage = CarWithImage(car, imageBase64)
            val response = ApiClient.apiService.saveCarWithImage(carWithImage).awaitResponse()
            if (response.isSuccessful) {
                withContext(Dispatchers.Main) { onComplete() }
            } else {
                println("Error al guardar imagen: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            println("Exception: ${e.message}")
        }
    }
}


fun deleteCar(car: Car, onComplete: () -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        val response = ApiClient.apiService.deleteCar(car._idKit).awaitResponse()
        if (response.isSuccessful) {
            onComplete()
        }
    }
}
