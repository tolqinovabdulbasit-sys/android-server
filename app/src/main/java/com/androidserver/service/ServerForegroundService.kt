package com.androidserver.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.androidserver.MainActivity
import com.androidserver.R
import com.androidserver.server.KtorServerEngine
import com.androidserver.server.logger.LogType
import com.androidserver.server.logger.ServerLogManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ServerForegroundService : Service() {

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private lateinit var powerManagerHelper: PowerManagerHelper
    private lateinit var ktorServerEngine: KtorServerEngine

    inner class LocalBinder : Binder() {
        fun getService(): ServerForegroundService = this@ServerForegroundService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        powerManagerHelper = PowerManagerHelper(this)
        ktorServerEngine = KtorServerEngine(this, powerManagerHelper)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val port = intent?.getIntExtra(EXTRA_PORT, 8080) ?: 8080

        when (action) {
            ACTION_START -> startServer(port)
            ACTION_STOP -> stopServer()
        }

        return START_STICKY
    }

    fun startServer(port: Int = 8080) {
        serviceScope.launch {
            powerManagerHelper.acquireLocks()
            val started = ktorServerEngine.start(port)
            if (started) {
                val ip = ktorServerEngine.getLocalIpAddress()
                val notification = buildNotification("Server Faol: http://$ip:$port")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                    )
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }
                _isServerRunning.value = true
                _serverUrl.value = "http://$ip:$port"
            } else {
                stopSelf()
            }
        }
    }

    fun stopServer() {
        serviceScope.launch {
            ktorServerEngine.stop()
            powerManagerHelper.releaseLocks()
            _isServerRunning.value = false
            _serverUrl.value = ""
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    fun getLocalIp(): String = ktorServerEngine.getLocalIpAddress()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(contentText: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Android Server Ishlamoqda")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        ktorServerEngine.stop()
        powerManagerHelper.releaseLocks()
        serviceScope.cancel()
        _isServerRunning.value = false
    }

    companion object {
        const val CHANNEL_ID = "android_server_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "com.androidserver.action.START"
        const val ACTION_STOP = "com.androidserver.action.STOP"
        const val EXTRA_PORT = "com.androidserver.extra.PORT"

        private val _isServerRunning = MutableStateFlow(false)
        val isServerRunning: StateFlow<Boolean> = _isServerRunning.asStateFlow()

        private val _serverUrl = MutableStateFlow("")
        val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()
    }
}
