package com.actividad1.myapplication.ui.theme.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.camera.core.CameraX
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

class CameraActivity : ComponentActivity() {
    private lateinit var cameraPermissionRequest: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraPermissionRequest = registerForActivityResult(RequestPermission()) { granted ->
            if (granted) {
                CameraX.initialize(this)
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
            }
        }

        // Solicitar permiso en tiempo de ejecución
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            cameraPermissionRequest.launch(Manifest.permission.CAMERA)
        }
    }
}

@Composable
fun CameraScreen(onImageCaptured: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    AndroidView(
        factory = { ctx ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            val cameraView = PreviewView(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = androidx.camera.core.Preview.Builder().build()
                imageCapture = ImageCapture.Builder().build()

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                preview.setSurfaceProvider(cameraView.surfaceProvider)
                try {
                    cameraProvider.unbindAll() // Liberar cámaras previamente asignadas
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                } catch (e: Exception) {
                    Toast.makeText(context, "Error al iniciar la cámara", Toast.LENGTH_SHORT).show()
                }
            }, ContextCompat.getMainExecutor(ctx))

            cameraView
        },
        modifier = Modifier.fillMaxWidth()
    )

    Button(
        onClick = {
            if (imageCapture != null) {
                imageCapture?.takePicture(
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            val buffer: ByteBuffer = image.planes[0].buffer
                            val bytes = ByteArray(buffer.capacity())
                            buffer.get(bytes)
                            image.close()

                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            imageBitmap = bitmap
                            onImageCaptured(encodeImageToBase64(bitmap))
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Toast.makeText(context, "Error al capturar imagen", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            } else {
                Toast.makeText(context, "Error: Cámara no inicializada", Toast.LENGTH_SHORT).show()
            }
        }
    ) {
        Text("Capturar Foto")
    }

    imageBitmap?.let { bitmap ->
        Image(bitmap = bitmap.asImageBitmap(), contentDescription = null)
    }
}

fun encodeImageToBase64(bitmap: Bitmap): String {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
}
