package com.actividad1.myapplication.api.models


data class Car(
    val placa: String,
    val modelo: String,
    val chofer: String,
    val activo: Boolean,
    val _idKit: String
)