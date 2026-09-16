package com.maxwell.mbrowser.devtools

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DevToolsManagerTest {

    @Before
    fun setUp() {
        DevToolsManager.clearLogs()
    }

    @Test
    fun testLogAdditionAndErrorCount() {
        DevToolsManager.addLog(
            ConsoleLogItem(
                level = LogLevel.ERROR,
                message = "Uncaught SyntaxError: Unexpected token '<'",
                sourceId = "bundle.js",
                lineNumber = 104
            )
        )
        DevToolsManager.addLog(
            ConsoleLogItem(
                level = LogLevel.WARN,
                message = "Deprecated feature used",
                sourceId = "main.js",
                lineNumber = 22
            )
        )
        DevToolsManager.addLog(
            ConsoleLogItem(
                level = LogLevel.INFO,
                message = "Page initialization complete",
                sourceId = "app.js",
                lineNumber = 5
            )
        )

        assertEquals(3, DevToolsManager.getLogs().size)
        assertEquals(1, DevToolsManager.getErrorCount())
    }

    @Test
    fun testClipboardTextFormatting() {
        DevToolsManager.addLog(
            ConsoleLogItem(
                level = LogLevel.ERROR,
                message = "Failed to load resource: 404 Not Found",
                sourceId = "https://api.example.com/data",
                lineNumber = 1
            )
        )

        val formattedText = DevToolsManager.getAllErrorsAsText()
        assertTrue("Formatted text should contain header", formattedText.contains("MBrowser DevTools Console Logs"))
        assertTrue("Formatted text should contain ERROR tag", formattedText.contains("[ERROR]"))
        assertTrue("Formatted text should contain the error message", formattedText.contains("404 Not Found"))
    }

    @Test
    fun testClearLogs() {
        DevToolsManager.addLog(
            ConsoleLogItem(
                level = LogLevel.ERROR,
                message = "Test Error",
                sourceId = "test.js",
                lineNumber = 10
            )
        )
        assertEquals(1, DevToolsManager.getLogs().size)

        DevToolsManager.clearLogs()
        assertEquals(0, DevToolsManager.getLogs().size)
        assertEquals(0, DevToolsManager.getErrorCount())
    }
}
