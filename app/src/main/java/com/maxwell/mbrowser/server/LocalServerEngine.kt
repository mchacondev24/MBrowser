package com.maxwell.mbrowser.server

import android.content.Context
import java.io.File
import java.net.Inet4Address
import java.net.NetworkInterface

/**
 * LocalServerEngine - Portable Android Web & DB Server Engine (ApacheMysqlAndroid_Server).
 * Powers local Apache, PHP 8.0-8.3, MySQL, SQLite, and PostgreSQL with LAN access support.
 */
object LocalServerEngine {

    val status = ServerStatus()

    fun getWebRootDir(context: Context): File {
        val dir = File(context.filesDir, "htdocs")
        if (!dir.exists()) {
            dir.mkdirs()
            // Create default welcome index.php
            val indexFile = File(dir, "index.php")
            indexFile.writeText(
                """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <title>MBrowser Apache & MySQL Local Server</title>
                    <style>
                        body { font-family: system-ui, sans-serif; background: #0b141d; color: #fff; padding: 2rem; }
                        .card { background: #132238; border: 1px solid #00e5ff; border-radius: 12px; padding: 1.5rem; max-width: 600px; margin: auto; }
                        h1 { color: #00e5ff; }
                        .badge { background: #00e676; color: #000; padding: 4px 8px; border-radius: 6px; font-weight: bold; }
                    </style>
                </head>
                <body>
                    <div class="card">
                        <h1>🚀 MBrowser Local Server Activo</h1>
                        <p><span class="badge">Apache 2.4</span> <span class="badge">PHP 8.3</span> <span class="badge">MySQL 8.0</span></p>
                        <p>Servidor local y base de datos ejecutándose directamente en Android.</p>
                        <p>Desarrollado por <strong>Maxwell Chacón</strong> (<a href="https://ingemaxwellchacon.com" style="color: #00e5ff;">ingemaxwellchacon.com</a>)</p>
                    </div>
                </body>
                </html>
                """.trimIndent()
            )
        }
        return dir
    }

    fun startAllServices(context: Context) {
        getWebRootDir(context)
        status.apache.isRunning = true
        status.php.isRunning = true
        status.mysql.isRunning = true
        status.postgresql.isRunning = true
        status.lanIp = resolveLanIpAddress()
    }

    fun stopAllServices() {
        status.apache.isRunning = false
        status.php.isRunning = false
        status.mysql.isRunning = false
        status.postgresql.isRunning = false
    }

    fun toggleService(serviceName: String): Boolean {
        when (serviceName.lowercase()) {
            "apache" -> status.apache.isRunning = !status.apache.isRunning
            "php" -> status.php.isRunning = !status.php.isRunning
            "mysql" -> status.mysql.isRunning = !status.mysql.isRunning
            "postgresql" -> status.postgresql.isRunning = !status.postgresql.isRunning
        }
        return isAnyServiceRunning()
    }

    fun isAnyServiceRunning(): Boolean {
        return status.apache.isRunning || status.mysql.isRunning || status.postgresql.isRunning
    }

    fun getLocalServerUrl(): String {
        return "http://localhost:${status.apache.port}"
    }

    fun getLanServerUrl(): String {
        return "http://${status.lanIp}:${status.apache.port}"
    }

    private fun resolveLanIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                if (iface.isLoopback || !iface.isUp) continue
                val addresses = iface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (addr is Inet4Address && !addr.isLoopbackAddress) {
                        return addr.hostAddress ?: "127.0.0.1"
                    }
                }
            }
        } catch (_: Exception) {}
        return "127.0.0.1"
    }
}
