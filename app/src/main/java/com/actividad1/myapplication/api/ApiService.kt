package com.actividad1.myapplication.api

import com.actividad1.myapplication.api.models.Car
import com.actividad1.myapplication.api.models.LoginRequest
import com.actividad1.myapplication.api.models.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @POST("users/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>
    @GET("unidades")
    fun getCars(): Call<List<Car>>

    @POST("unidades")
    fun createCar(@Body car: Car): Call<Car>

    @PUT("unidad/:{id}")
    fun updateCar(@Path("id") id: String, @Body car: Car): Call<Car>

    @DELETE("unidades/{id}")
    fun deleteCar(@Path("id") id: String): Call<Void>
}

