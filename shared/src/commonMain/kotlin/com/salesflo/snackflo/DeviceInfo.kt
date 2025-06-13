package com.salesflo.snackflo

import kotlinx.serialization.Serializable
@Serializable
data class FullDeviceInfo(
    val model: String,
    val osVersion: String,
    val platform: String,
    val manufacturer: String,
    val deviceName: String,
    val sdkVersion: Int,
    val ipAddress: String,
    val appVersion: String,
    val locale: String,
    val isEmulator: Boolean,
    val screenDensity: Float?,
    val screenWidth: Int?,
    val screenHeight: Int?
)

expect suspend fun getFullDeviceInfo(): FullDeviceInfo

