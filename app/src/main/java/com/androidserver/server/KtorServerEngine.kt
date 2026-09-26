package com.androidserver.server

import android.content.Context
import android.os.Build
import com.androidserver.server.logger.LogType
import com.androidserver.server.logger.ServerLogManager
import com.androidserver.server.model.ServerStatusResponse
import com.androidserver.server.model.SimpleMessage
import com.androidserver.service.PowerManagerHelper
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import java.net.Inet4Address
import java.net.NetworkInterface

class KtorServerEngine(
    private val appContext: Context,
    private val powerManagerHelper: PowerManagerHelper
) {
    private var engine: ApplicationEngine? = null
    private var startTime: Long = 0
    var isRunning: Boolean = false
        private set

    fun start(port: Int = 8080): Boolean {
        if (isRunning) return true

        return try {
            startTime = System.currentTimeMillis()
            ServerLogManager.log("Ktor server boshlanmoqda (Port: $port)...", LogType.INFO)

            engine = embeddedServer(CIO, port = port, host = "0.0.0.0") {
                configurePlugins()
                configureRouting(port)
            }.start(wait = false)

            isRunning = true
            ServerLogManager.log("Server muvaffaqiyatli ishga tushdi: http://${getLocalIpAddress()}:$port", LogType.SUCCESS)
            true
        } catch (e: Exception) {
            isRunning = false
            ServerLogManager.log("Serverni yoqishda xatolik: ${e.message}", LogType.ERROR)
            false
        }
    }

    fun stop() {
        try {
            engine?.stop(1000, 2000)
            engine = null
            isRunning = false
            ServerLogManager.log("Server to'xtatildi", LogType.WARNING)
        } catch (e: Exception) {
            ServerLogManager.log("Serverni to'xtatishda xato: ${e.message}", LogType.ERROR)
        }
    }

    private fun Application.configurePlugins() {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(CORS) {
            anyHost()
            allowHeader(io.ktor.http.HttpHeaders.ContentType)
            allowMethod(io.ktor.http.HttpMethod.Get)
            allowMethod(io.ktor.http.HttpMethod.Post)
            allowMethod(io.ktor.http.HttpMethod.Put)
            allowMethod(io.ktor.http.HttpMethod.Delete)
        }
    }

    private fun Application.configureRouting(currentPort: Int) {
        routing {
            // Asosiy Web Dashboard (assets/web/index.html)
            get("/") {
                val clientIp = call.request.local.remoteHost
                ServerLogManager.log("[$clientIp] GET / - 200 OK (Web Dashboard)", LogType.INFO)
                try {
                    val htmlContent = appContext.assets.open("web/index.html").bufferedReader(Charsets.UTF_8).use { it.readText() }
                    call.respondText(htmlContent, ContentType.Text.Html)
                } catch (e: Exception) {
                    call.respondText("Web Dashboard yuklanmadi: ${e.message}", ContentType.Text.Plain, HttpStatusCode.InternalServerError)
                }
            }

            // REST API - Status
            get("/api/status") {
                val clientIp = call.request.local.remoteHost
                ServerLogManager.log("[$clientIp] GET /api/status - 200 OK", LogType.SUCCESS)

                val uptimeSec = (System.currentTimeMillis() - startTime) / 1000
                val deviceName = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"
                val response = ServerStatusResponse(
                    status = "ONLINE",
                    device = deviceName,
                    androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
                    uptimeSeconds = uptimeSec,
                    battery = powerManagerHelper.getBatteryLevel(),
                    ip = getLocalIpAddress(),
                    port = currentPort
                )
                call.respond(response)
            }

            // REST API - Ping
            get("/api/ping") {
                val clientIp = call.request.local.remoteHost
                ServerLogManager.log("[$clientIp] GET /api/ping - 200 OK", LogType.INFO)
                call.respond(SimpleMessage(message = "Pong! Android server faol ishlamoqda."))
            }
        }
    }

    fun getLocalIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                if (networkInterface.isLoopback || !networkInterface.isUp) continue

                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (address is Inet4Address && !address.isLoopbackAddress) {
                        val host = address.hostAddress
                        if (host != null && !host.startsWith("127.")) {
                            return host
                        }
                    }
                }
            }
        } catch (e: Exception) {
            ServerLogManager.log("IP manzilni aniqlashda xato: ${e.message}", LogType.WARNING)
        }
        return "127.0.0.1"
    }
}
