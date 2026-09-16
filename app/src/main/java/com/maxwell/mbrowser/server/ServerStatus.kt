package com.maxwell.mbrowser.server

data class ServerService(
    val name: String,
    val port: Int,
    var isRunning: Boolean = false,
    val version: String,
    val description: String
)

data class ServerStatus(
    val apache: ServerService = ServerService("Apache HTTP Server", 8080, false, "2.4.58", "Servidor Web HTTP/HTTPS"),
    val php: ServerService = ServerService("PHP Engine", 8080, false, "8.3.10", "Intérprete de scripts PHP 8.0-8.3"),
    val mysql: ServerService = ServerService("MySQL Database", 3306, false, "8.0.36", "Base de datos relacional MariaDB/MySQL"),
    val postgresql: ServerService = ServerService("PostgreSQL Database", 5432, false, "16.2", "Base de datos SQL avanzada"),
    val sqlite: ServerService = ServerService("SQLite Engine", 0, true, "3.45.1", "Base de datos embebida"),
    var lanIp: String = "127.0.0.1",
    var isLanEnabled: Boolean = true
)
