package com.salesflo.snackflo

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.WindowManager
import org.json.JSONObject
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Locale


//actual object DeviceInfo {
//    actual val model: String
//        get() = android.os.Build.MODEL
//
//    actual val osVersion: String
//        get() = android.os.Build.VERSION.RELEASE
//
//    actual val platform: String
//        get() = "Android"
//}
lateinit var applicationContext: Context

fun initializeContext(context: Context) {
    applicationContext = context
}

// androidMain
actual suspend fun getFullDeviceInfo(): FullDeviceInfo {

    val wm = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val metrics = DisplayMetrics().also { wm.defaultDisplay.getMetrics(it) }

    val ip = try {
        val interfaces = NetworkInterface.getNetworkInterfaces().toList()
        interfaces.flatMap { it.inetAddresses.toList() }
            .firstOrNull { !it.isLoopbackAddress && it is Inet4Address }
            ?.hostAddress ?: "N/A"
    } catch (e: Exception) {
        "N/A"
    }

    val packageInfo =
        applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0)
    val isEmulator = Build.FINGERPRINT.contains("generic") || Build.PRODUCT.contains("sdk")

    return FullDeviceInfo(
        model = Build.MODEL ?: "Unknown",
        osVersion = Build.VERSION.RELEASE ?: "Unknown",
        platform = "Android",
        manufacturer = Build.MANUFACTURER ?: "Unknown",
        deviceName = Build.DEVICE ?: "Unknown",
        sdkVersion = Build.VERSION.SDK_INT,
        ipAddress = ip,
        appVersion = packageInfo.versionName ?: "N/A",
        locale = Locale.getDefault().toString(),
        isEmulator = isEmulator,
        screenDensity = metrics.density,
        screenWidth = metrics.widthPixels,
        screenHeight = metrics.heightPixels
    )
}


fun getDeviceInfoJson(context: Context): String {
    val info = JSONObject()
    try {
        info.put("manufacturer", Build.MANUFACTURER)
        info.put("model", Build.MODEL)
        info.put("brand", Build.BRAND)
        info.put("device", Build.DEVICE)
        info.put("product", Build.PRODUCT)
        info.put("hardware", Build.HARDWARE)
        info.put("sdk_int", Build.VERSION.SDK_INT)
        info.put("release", Build.VERSION.RELEASE)
        info.put(
            "android_id",
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        )
    } catch (e: Exception) {
        info.put("error", e.localizedMessage)
    }
    return info.toString()
}

