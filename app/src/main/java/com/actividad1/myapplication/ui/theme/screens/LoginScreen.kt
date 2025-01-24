package com.actividad1.myapplication.ui.theme.screens
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.actividad1.myapplication.R
import com.actividad1.myapplication.api.ApiClient
import com.actividad1.myapplication.api.models.LoginRequest
import com.actividad1.myapplication.api.models.LoginResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun LoginScreen(
    onLoginSuccess:  (String) -> Unit,
    onLoginError: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.img),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Inicio de sesión",
            fontFamily = FontFamily.Serif,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (email.isEmpty() || password.isEmpty()) {
                    errorMessage = "Por favor rellena los campos."
                } else {
                    errorMessage = ""
                    onLoginClick(
                        email,
                        password,
                        onLoginSuccess,
                        onLoginError,
                        setErrorMessage = { errorMessage = it },
                        setSuccessMessage = { successMessage = it }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Login")
        }
        if (successMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = successMessage, color = MaterialTheme.colorScheme.primary)
        }

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}

fun onLoginClick(
    email: String,
    password: String,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit,
    setErrorMessage: (String) -> Unit,
    setSuccessMessage: (String) -> Unit
) {
    val request = LoginRequest(correo = email, password = password)

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = ApiClient.apiService.login(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body()?.message ?: "Respuesta vacía"
                setSuccessMessage("Respuesta exitosa del servidor: $responseBody")
                onSuccess(responseBody)
            } else {
                setErrorMessage("Error en la solicitud. Código de error: ${response.code()}")
                onError("Error al iniciar sesión: ${response.code()}")
            }
        } catch (e: Exception) {
            setErrorMessage("Excepción en la solicitud: ${e.message}")
            onError("Error al conectar con el servidor: ${e.message}")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(
        onLoginSuccess = { },
        onLoginError = { }
    )
}
