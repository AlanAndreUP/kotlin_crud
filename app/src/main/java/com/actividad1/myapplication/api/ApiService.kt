package com.actividad1.myapplication.api

import com.actividad1.myapplication.api.models.Car
import com.actividad1.myapplication.api.models.CarWithImage
import com.actividad1.myapplication.api.models.LoginImageRequest
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
    fun saveCarWithImage(@Body car: CarWithImage): Call<CarWithImage>

    @PUT("unidades/unidad/{placaId}")
    fun updateCar(@Path("placaId") placaId: String, @Body car: Car): Call<Car>

    @DELETE("unidades/unidad/{id}")
    fun deleteCar(@Path("id") id: String): Call<Void>

    @POST("users/image")
    fun loginByImage(@Body request: LoginImageRequest): Call<LoginResponse>
}

