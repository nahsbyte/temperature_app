package com.monitoring.farmasidinkesminahasa.service

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.logging.HttpLoggingInterceptor


object RetrofitClient {
    private const val BASE_URL = "https://api.nahsbyte.my.id/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Untuk mengonversi JSON ke Kotlin Object
            .client(client)
            .build()
            .create(ApiService::class.java)
    }
}
