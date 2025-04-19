package com.monitoring.farmasidinkesminahasa.model

data class SensorResponse(
    val Kelembaban: Int,
    val Suhu: Int,
    val Kontrol: Double,
    val History: List<HistoryItemResponse>
)
