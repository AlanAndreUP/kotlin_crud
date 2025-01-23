package com.actividad1.myapplication.ui.theme.screens

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.actividad1.myapplication.api.models.Car
import com.actividad1.myapplication.api.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.awaitResponse

@Composable
fun CarStockScreen() {
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
                }
            )
        }
    }
}

@Composable
fun CarItem(car: Car, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        elevation = CardDefaults.elevatedCardElevation()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Placa: ${car.placa}")
            Text(text = "Modelo: ${car.modelo}")
            Text(text = "Chofer: ${car.chofer}")
            Text(text = "Activo: ${if (car.activo) "Sí" else "No"}")

            Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.End) {
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
    onSave: () -> Unit
) {
    var placa by remember { mutableStateOf(car?.placa ?: "") }
    var modelo by remember { mutableStateOf(car?.modelo ?: "") }
    var chofer by remember { mutableStateOf(car?.chofer ?: "") }
    var activo by remember { mutableStateOf(car?.activo ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (car == null) "Agregar Carro" else "Editar Carro") },
        text = {
            Column {
                BasicTextField(
                    value = placa,
                    onValueChange = { placa = it },
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    decorationBox = { innerTextField ->
                        OutlinedTextFieldDefaults.DecorationBox(
                            value = placa,
                            innerTextField = innerTextField,
                            enabled = true,
                            singleLine = true,
                            visualTransformation = TODO(),
                            interactionSource = remember { MutableInteractionSource() },
                            isError = false,
                            label = TODO(),
                            placeholder = TODO(),
                            leadingIcon = TODO(),
                            trailingIcon = TODO(),
                            prefix = TODO(),
                            suffix = TODO(),
                            supportingText = TODO(),
                            colors = TextFieldDefaults.outlinedTextFieldColors(),
                            contentPadding = TODO(),
                            container = TODO(),
                        )
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))
                BasicTextField(
                    value = modelo,
                    onValueChange = { modelo = it },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                BasicTextField(
                    value = chofer,
                    onValueChange = { chofer = it },
                    modifier = Modifier.fillMaxWidth()
                )
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
                    saveCar(newCar) { onSave() }
                }
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

fun saveCar(car: Car, onComplete: () -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        val response = if (car._idKit.isEmpty()) {
            ApiClient.apiService.createCar(car).awaitResponse()
        } else {
            ApiClient.apiService.updateCar(car._idKit, car).awaitResponse()
        }
        if (response.isSuccessful) {
            onComplete()
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
