package com.androidserver.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidserver.ui.theme.*

@Composable
fun ServerDashboardCard(
    isRunning: Boolean,
    serverUrl: String,
    deviceModel: String,
    batteryInfo: String,
    onCopyUrl: (String) -> Unit
) {
    val statusColor by animateColorAsState(
        targetValue = if (isRunning) SuccessEmerald else Color.Gray,
        label = "statusColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Yuqori qator: Model va Status nishoni
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = deviceModel,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLight
                    )
                    Text(
                        text = "Ktor Native CIO Server",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }

                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(50),
                    border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Text(
                            text = if (isRunning) "FAOL (ONLINE)" else "TO'XTATILGAN",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isRunning) SuccessEmerald else Color.LightGray
                        )
                    }
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

            // URL maydoni
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "SERVER MANZILI (URL):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )

                if (isRunning && serverUrl.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = PrimaryIndigo,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = serverUrl,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        IconButton(
                            onClick = { onCopyUrl(serverUrl) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Nusxa olish",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Server o'chiq. Uni ishga tushirish uchun quyidagi tugmani bosing.",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            }

            // Statistika qatori
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Batareya", fontSize = 11.sp, color = TextMuted)
                    Text(batteryInfo, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextLight)
                }
                Column {
                    Text("Orqa fon", fontSize = 11.sp, color = TextMuted)
                    Text(if (isRunning) "WakeLock Faol" else "Nofaol", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (isRunning) SuccessEmerald else TextMuted)
                }
                Column {
                    Text("Dvigatel", fontSize = 11.sp, color = TextMuted)
                    Text("CIO Engine", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = PrimaryIndigo)
                }
            }
        }
    }
}
