package com.monitoring.farmasidinkesminahasa.service

import com.monitoring.farmasidinkesminahasa.model.ConfigRequest
import com.monitoring.farmasidinkesminahasa.model.PeriodConfigRequest
import com.monitoring.farmasidinkesminahasa.model.HistoryItemResponse
import com.monitoring.farmasidinkesminahasa.model.NtfyMessage
import com.monitoring.farmasidinkesminahasa.model.Tool
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("/sensor/tools")
    fun getTools(): Call<List<Tool>>

    @GET("/sensor/firebase/latest")
    fun getLatestSensorData(): Call<HistoryItemResponse>

    @POST("/sensor/firebase/period-config")
    fun postPeriodConfig(@Body periodConfigRequest: PeriodConfigRequest): Call<Void>

    @POST("/sensor/firebase/config")
    fun postConfig(@Body configRequest: ConfigRequest): Call<Void>

    @GET("/sensor/firebase/history")
    fun getHistorySensorData(
        @Query("start") start: String,  // format: YYYY-MM-DD
        @Query("end") end: String       // format: YYYY-MM-DD
    ): Call<List<HistoryItemResponse>>

    @POST("/sensor/wifi-config")
    fun updateWifiConfig(
        @Query("ssid") ssid: String,
        @Query("password") password: String
    ): Call<Void>

    @POST("/wifi")
    fun updateWifiLocal(
        @Query("ssid") ssid: String,
        @Query("password") password: String
    ): Call<Void>
}