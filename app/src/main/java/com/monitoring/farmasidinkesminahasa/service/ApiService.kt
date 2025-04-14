package com.monitoring.farmasidinkesminahasa.service

import com.monitoring.farmasidinkesminahasa.model.SensorResponse
import com.monitoring.farmasidinkesminahasa.model.ToolsResponse
import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("/read_sensor")
    fun getSensorData(): Call<SensorResponse>
    @GET("/tools")
    fun getTools(): Call<ToolsResponse>

}