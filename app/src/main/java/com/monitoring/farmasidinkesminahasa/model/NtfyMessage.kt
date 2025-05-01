package com.monitoring.farmasidinkesminahasa.model

data class NtfyMessage(
    val id: String?,
    val time: Long?,
    val event: String?,
    val topic: String?,
    val message: String?,
    val title: String?
)