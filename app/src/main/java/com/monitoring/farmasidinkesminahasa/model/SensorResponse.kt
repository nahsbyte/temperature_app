package com.monitoring.farmasidinkesminahasa.model

data class SensorResponse(
    val Kelembaban: Int,
    val Suhu: Int,
    val Kontrol: Double,
    val History: List<HistoryItem>
)

data class HistoryItem(
    val Control: Double,
    val Kelembaban: Int,
    val Suhu: Int,
    val Timestamp: String
)
