package com.monitoring.farmasidinkesminahasa.service

import com.monitoring.farmasidinkesminahasa.model.PeriodConfigRequest
import com.monitoring.farmasidinkesminahasa.model.HistoryItemResponse
import com.monitoring.farmasidinkesminahasa.model.Tool
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
//    @GET("/read_sensor")
//    fun getSensorData(): Call<SensorResponse>
//
    @GET("/sensor/tools")
    fun getTools(): Call<List<Tool>>

    @GET("/sensor/firebase/latest")
    fun getLatestSensorData(): Call<HistoryItemResponse>

    @POST("/sensor/firebase/period-config")
    fun postPeriodConfig(@Body periodConfigRequest: PeriodConfigRequest): Call<Void>

    @GET("/sensor/firebase/history")
    fun getHistorySensorData(
        @Query("start") start: String,  // format: YYYY-MM-DD
        @Query("end") end: String       // format: YYYY-MM-DD
    ): Call<List<HistoryItemResponse>>
}