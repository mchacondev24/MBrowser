package com.maxwell.mbrowser.devtools

enum class LogLevel {
    ERROR,
    WARN,
    INFO,
    LOG
}

data class ConsoleLogItem(
    val level: LogLevel,
    val message: String,
    val sourceId: String?,
    val lineNumber: Int,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toFormattedString(): String {
        val src = if (!sourceId.isNullOrEmpty()) " [$sourceId:$lineNumber]" else ""
        return "[${level.name}]$src $message"
    }
}
