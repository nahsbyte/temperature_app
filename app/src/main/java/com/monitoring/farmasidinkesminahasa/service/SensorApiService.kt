package com.monitoring.farmasidinkesminahasa.service

import com.monitoring.farmasidinkesminahasa.model.SensorResponse
import retrofit2.Call
import retrofit2.http.GET

interface SensorApiService {
    @GET("/sensor")
    fun getSensorData(): Call<SensorResponse>
}