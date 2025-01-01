package com.monitoring.farmasidinkesminahasa.service

import com.monitoring.farmasidinkesminahasa.model.ToolsResponse
import retrofit2.Response
import retrofit2.http.GET

interface ToolService {
    @GET("sensor/tools")
    suspend fun getTools(): Response<ToolsResponse>
}
