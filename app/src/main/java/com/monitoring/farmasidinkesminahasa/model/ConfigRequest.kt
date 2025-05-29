package com.monitoring.farmasidinkesminahasa.model

data class ConfigRequest(
    val period: Int?,
    val ssid: String?,
    val password: String?,
    val is_periodic_sensor: Boolean?,
    val manual_fan: Boolean?,
    val manual_heater: Boolean?,
    val manual_humidifier: Boolean?,
    val manual_humidity: Int?,
    val manual_temp: Int?,
)
