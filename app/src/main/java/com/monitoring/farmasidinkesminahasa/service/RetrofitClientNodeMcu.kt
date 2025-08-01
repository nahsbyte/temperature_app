package com.monitoring.farmasidinkesminahasa.service

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.logging.HttpLoggingInterceptor


object RetrofitClientNodeMcu {
    private const val BASE_URL = "http://192.168.4.1:1234/"

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
