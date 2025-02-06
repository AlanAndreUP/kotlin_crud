package com.actividad1.myapplication.api.models


data class LoginRequest(val correo: String, val password: String)
data class LoginImageRequest(val base64: String)
data class LoginResponse(val message: String)
