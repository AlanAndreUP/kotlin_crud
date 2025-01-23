package com.actividad1.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.actividad1.myapplication.ui.theme.MyApplicationTheme
import com.actividad1.myapplication.ui.theme.screens.LoginScreen
import com.actividad1.myapplication.ui.theme.screens.CarStockScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                AppContent()
            }
        }
    }
}

@Composable
fun AppContent() {
    var currentScreen by remember { mutableStateOf("login") }

    when (currentScreen) {
        "login" -> LoginScreen(
            onLoginSuccess = { successMessage ->
                currentScreen = "carStock"
            },
            onLoginError = { errorMessage ->
                println("Error: $errorMessage")
            }
        )
        "carStock" -> CarStockScreen()
    }
}
