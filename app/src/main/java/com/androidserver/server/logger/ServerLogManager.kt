package com.androidserver.server.logger

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LogEntry(
    val id: Long = System.currentTimeMillis() + (0..999).random(),
    val timestamp: String,
    val message: String,
    val type: LogType = LogType.INFO
)

enum class LogType {
    INFO, SUCCESS, WARNING, ERROR
}

object ServerLogManager {
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    fun log(message: String, type: LogType = LogType.INFO) {
        val entry = LogEntry(
            timestamp = timeFormat.format(Date()),
            message = message,
            type = type
        )
        val current = _logs.value.toMutableList()
        if (current.size > 200) {
            current.removeAt(0)
        }
        current.add(entry)
        _logs.value = current
    }

    fun clear() {
        _logs.value = emptyList()
    }
}
