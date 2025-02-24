package com.actividad1.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.actividad1.myapplication.ui.theme.MyApplicationTheme
import com.actividad1.myapplication.ui.theme.screens.CameraScreen
import com.actividad1.myapplication.ui.theme.screens.LoginScreen
import com.actividad1.myapplication.ui.theme.screens.CarStockScreen
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.Manifest

import android.content.pm.PackageManager

import androidx.core.content.ContextCompat

fun showNotification(context: Context, title: String, message: String) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            Log.e("Notification", "POST_NOTIFICATIONS permission is not granted.")
            return
        }
    }

    val channelId = "my_channel_id"
    val notificationId = System.currentTimeMillis().toInt()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId, "Notificaciones",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Canal para recibir notificaciones push"
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }


    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .build()


    NotificationManagerCompat.from(context).notify(notificationId, notification)
}

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Nuevo token: $token")

    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("FCM", "Mensaje recibido de: ${remoteMessage.from}")

        remoteMessage.notification?.let {
            Log.d("FCM", "Título: ${it.title}, Mensaje: ${it.body}")
            showNotification(applicationContext, it.title ?: "Sin título", it.body ?: "Sin mensaje")
        }

        remoteMessage.data.let {
            Log.d("FCM", "Datos recibidos: $it")
        }
    }

}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                AppNavigation()
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
        }

        getFirebaseToken()
    }

    private fun getFirebaseToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.e("FCM", "No se pudo obtener el token", task.exception)
                    return@addOnCompleteListener
                }

                val token = task.result
                Log.d("FCM", "Token actual: $token")

            }
    }
}


@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("carStock")
                },
                onLoginError = { errorMessage ->
                    Log.e("Login", "Error: $errorMessage")
                }
            )
        }
        composable("carStock") { CarStockScreen(navController) }
        composable("camera") {
            CameraScreen(
                onImageCaptured = { imageBase64 ->
                    navController.navigate("carStock")
                }
            )
        }
    }
}
