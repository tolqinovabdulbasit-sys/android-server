package com.androidserver.service

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import com.androidserver.server.logger.LogType
import com.androidserver.server.logger.ServerLogManager

class PowerManagerHelper(private val context: Context) {

    private var wakeLock: PowerManager.WakeLock? = null
    private var wifiLock: WifiManager.WifiLock? = null

    fun acquireLocks() {
        try {
            // CPU uyg'oq ushlab turish (Samsung OneUI Deep Sleep himoyasi)
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            wakeLock = powerManager?.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "AndroidServer::WakeLock"
            )?.apply {
                setReferenceCounted(false)
                acquire(24 * 60 * 60 * 1000L) // 24 soat xavfsiz muddat
            }

            // Wi-Fi aloqasi uxlamasligi uchun WifiLock
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            val wifiMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                WifiManager.WIFI_MODE_FULL_LOW_LATENCY
            } else {
                @Suppress("DEPRECATION")
                WifiManager.WIFI_MODE_FULL_HIGH_PERF
            }

            wifiLock = wifiManager?.createWifiLock(wifiMode, "AndroidServer::WifiLock")?.apply {
                setReferenceCounted(false)
                acquire()
            }

            ServerLogManager.log("WakeLock va WifiLock muvaffaqiyatli faollashtirildi", LogType.SUCCESS)
        } catch (e: Exception) {
            ServerLogManager.log("WakeLock olishda xatolik: ${e.message}", LogType.WARNING)
        }
    }

    fun releaseLocks() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
            if (wifiLock?.isHeld == true) {
                wifiLock?.release()
            }
            ServerLogManager.log("WakeLock va WifiLock bo'shatildi", LogType.INFO)
        } catch (e: Exception) {
            ServerLogManager.log("WakeLock bo'shatishda xatolik: ${e.message}", LogType.WARNING)
        }
    }

    fun getBatteryLevel(): String {
        return try {
            val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
                context.registerReceiver(null, filter)
            }
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            val batteryPct = if (level != -1 && scale != -1) {
                (level * 100 / scale.toFloat()).toInt()
            } else -1

            val chargingSuffix = if (isCharging) " (Zaryad olmoqda ⚡)" else ""
            if (batteryPct != -1) "$batteryPct%$chargingSuffix" else "Noma'lum"
        } catch (e: Exception) {
            "Noma'lum"
        }
    }
}
