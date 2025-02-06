package com.actividad1.myapplication.ui.theme.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.camera.core.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.actividad1.myapplication.api.ApiClient
import com.actividad1.myapplication.api.models.LoginImageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.Executor

@Composable
fun CameraScreen(onImageCaptured: (String) -> Unit) {
    val context = LocalContext.current

    // Launcher para solicitar el permiso de la cámara
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher para abrir la cámara y obtener un thumbnail (Bitmap)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val base64Image = encodeBitmapToBase64(bitmap)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Imagen capturada", Toast.LENGTH_SHORT).show()
                    }
                    val request = LoginImageRequest(base64Image)
                    val response = ApiClient.apiService.loginByImage(request).execute()
                    if (response.isSuccessful){
                        val responseBody = response.body()?.message ?: "Respuesta vacía"
                        // Usamos withContext para ejecutar las actualizaciones de UI en el hilo principal
                        withContext(Dispatchers.Main) {
                            onImageCaptured(responseBody)  // Llamada a onSuccess pasando el mensaje recibido
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Error al iniciar sesión: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception){
                    withContext(Dispatchers.Main){
                        Toast.makeText(context, "Error al conectar con el servidor: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(context, "No se capturó la imagen", Toast.LENGTH_SHORT).show()
        }
    }

    // Interfaz simple: un botón para tomar la foto.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            // Verifica si el permiso ya está concedido
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                cameraLauncher.launch()
            } else {
                // Si no está concedido, se solicita el permiso
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }) {
            Text(text = "Tomar Foto")
        }
    }
}

/**
 * Función de ayuda para codificar un Bitmap a una cadena Base64.
 */
fun encodeBitmapToBase64(bitmap: Bitmap): String {
    val outputStream = ByteArrayOutputStream()
    // Se comprime la imagen a JPEG (calidad 100, ajustar si es necesario)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    val byteArray = outputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}