package com.actividad1.myapplication.api.models

data class CarWithImage(
    val car: Car,
    val imageBase64: String
)

data class Car(
    val placa: String,
    val modelo: String,
    val chofer: String,
    val activo: Boolean,
    val _idKit: String,
    val imagenBase64: String? = null
)