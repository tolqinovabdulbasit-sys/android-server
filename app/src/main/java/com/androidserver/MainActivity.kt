package com.androidserver

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import com.androidserver.server.logger.ServerLogManager
import com.androidserver.service.PowerManagerHelper
import com.androidserver.service.ServerForegroundService
import com.androidserver.ui.screens.MainScreen
import com.androidserver.ui.theme.AndroidServerTheme

class MainActivity : ComponentActivity() {

    private lateinit var powerManagerHelper: PowerManagerHelper

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            ServerLogManager.log("Bildirishnoma ruxsati berildi", com.androidserver.server.logger.LogType.SUCCESS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        powerManagerHelper = PowerManagerHelper(this)

        checkPermissions()

        setContent {
            AndroidServerTheme {
                val isRunning by ServerForegroundService.isServerRunning.collectAsState()
                val serverUrl by ServerForegroundService.serverUrl.collectAsState()
                val logs by ServerLogManager.logs.collectAsState()

                val deviceModel = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"
                var batteryInfo by remember { mutableStateOf(powerManagerHelper.getBatteryLevel()) }

                LaunchedEffect(Unit) {
                    while (true) {
                        batteryInfo = powerManagerHelper.getBatteryLevel()
                        kotlinx.coroutines.delay(5000)
                    }
                }

                MainScreen(
                    isRunning = isRunning,
                    serverUrl = serverUrl,
                    deviceModel = deviceModel,
                    batteryInfo = batteryInfo,
                    logs = logs,
                    onStartServer = { port ->
                        startServerService(port)
                    },
                    onStopServer = {
                        stopServerService()
                    },
                    onClearLogs = {
                        ServerLogManager.clear()
                    }
                )
            }
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun startServerService(port: Int) {
        val intent = Intent(this, ServerForegroundService::class.java).apply {
            action = ServerForegroundService.ACTION_START
            putExtra(ServerForegroundService.EXTRA_PORT, port)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopServerService() {
        val intent = Intent(this, ServerForegroundService::class.java).apply {
            action = ServerForegroundService.ACTION_STOP
        }
        startService(intent)
    }
}
