package com.androidserver.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidserver.server.logger.LogEntry
import com.androidserver.ui.components.LogViewerComponent
import com.androidserver.ui.components.ServerDashboardCard
import com.androidserver.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    isRunning: Boolean,
    serverUrl: String,
    deviceModel: String,
    batteryInfo: String,
    logs: List<LogEntry>,
    onStartServer: (Int) -> Unit,
    onStopServer: () -> Unit,
    onClearLogs: () -> Unit
) {
    val context = LocalContext.current
    var portText by remember { mutableStateOf("8080") }

    val buttonColor by animateColorAsState(
        targetValue = if (isRunning) ErrorRose else SuccessEmerald,
        label = "btnColor"
    )

    Scaffold(
        containerColor = DarkBase,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("⚡", fontSize = 22.sp)
                        Text(
                            text = "Android Server Host",
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = TextLight
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBase
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Dashboard kartasi
            ServerDashboardCard(
                isRunning = isRunning,
                serverUrl = serverUrl,
                deviceModel = deviceModel,
                batteryInfo = batteryInfo,
                onCopyUrl = { url ->
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Server URL", url))
                    Toast.makeText(context, "URL nusxalandi!", Toast.LENGTH_SHORT).show()
                }
            )

            // Port sozlash maydoni (faqat server o'chiq paytida o'zgartiriladi)
            if (!isRunning) {
                OutlinedTextField(
                    value = portText,
                    onValueChange = { if (it.length <= 5) portText = it.filter { char -> char.isDigit() } },
                    label = { Text("Server Porti (Masalan: 8080)", color = TextMuted) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryIndigo,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Katta Boshqaruv Tugmasi
            Button(
                onClick = {
                    if (isRunning) {
                        onStopServer()
                    } else {
                        val port = portText.toIntOrNull() ?: 8080
                        onStartServer(port)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Text(
                        text = if (isRunning) "SERVERNI TO'XTATISH" else "SERVERNI ISHGA TUSHIRISH",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }

            // Brauzerda ochish tugmasi (faqat server faol bo'lsa)
            if (isRunning && serverUrl.isNotEmpty()) {
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(serverUrl))
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryIndigo)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.OpenInBrowser, contentDescription = null)
                        Text("Web Dashboardni Brauzerda Ochish", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Samsung OneUI Batareya himoyasi tugmasi
            Surface(
                onClick = {
                    try {
                        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Sozlamalarga o'tib bo'lmadi", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = CardDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BatteryAlert,
                        contentDescription = null,
                        tint = WarningAmber
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Samsung Batareya Himoyasi",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = TextLight
                        )
                        Text(
                            text = "Ekran o'chganda to'xtab qolmasligi uchun 'Cheklovsiz' (Unrestricted) qiling",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            // Jonli Loglar
            LogViewerComponent(
                logs = logs,
                onClearLogs = onClearLogs
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
