package com.monitoring.farmasidinkesminahasa.model

data class ToolsResponse(
    val tools: List<Tool>
)

data class Tool(
    val name: String,
    val description: String
)