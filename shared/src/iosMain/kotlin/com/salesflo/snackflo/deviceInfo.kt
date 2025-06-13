package com.salesflo.snackflo

import platform.Foundation.NSBundle
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.localeIdentifier
import platform.UIKit.UIDevice


//actual object DeviceInfo {
//    actual val model: String
//        get() = UIDevice.currentDevice.model
//
//    actual val osVersion: String
//        get() = UIDevice.currentDevice.systemVersion
//
//    actual val platform: String
//        get() = "iOS"
//}


actual suspend fun getFullDeviceInfo(): FullDeviceInfo {
    val device = UIDevice.currentDevice
    val locale = NSLocale.currentLocale.localeIdentifier ?: "en_US"
    val appVersion =
        NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString")?.toString() ?: "N/A"

    return FullDeviceInfo(
        model = device.model ?: "Unknown",
        osVersion = device.systemVersion ?: "Unknown",
        platform = "iOS",
        manufacturer = "Apple",
        deviceName = device.name ?: "Unknown",
        sdkVersion = 0, // Not directly accessible
        ipAddress = "Not Implemented", // Needs platform network access
        appVersion = appVersion,
        locale = locale,
        isEmulator = isIosSimulator(),
        screenDensity = null, // Needs UIScreen.scale
        screenWidth = null,
        screenHeight = null
    )
}

fun isIosSimulator(): Boolean {
    return UIDevice.currentDevice.model?.contains("Simulator") == true
}


