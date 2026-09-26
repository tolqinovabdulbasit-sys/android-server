package com.androidserver.server.model

import kotlinx.serialization.Serializable

@Serializable
data class ServerStatusResponse(
    val status: String,
    val device: String,
    val androidVersion: String,
    val uptimeSeconds: Long,
    val battery: String,
    val ip: String,
    val port: Int
)

@Serializable
data class SimpleMessage(
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
