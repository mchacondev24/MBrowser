package com.maxwell.mbrowser.server

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LocalServerEngineTest {

    @Before
    fun setUp() {
        LocalServerEngine.stopAllServices()
    }

    @Test
    fun testInitialServerState() {
        assertFalse("Initially no service should be running", LocalServerEngine.isAnyServiceRunning())
        assertEquals("http://localhost:8080", LocalServerEngine.getLocalServerUrl())
    }

    @Test
    fun testToggleService() {
        LocalServerEngine.toggleService("apache")
        assertTrue("Apache should be running", LocalServerEngine.status.apache.isRunning)
        assertTrue("isAnyServiceRunning should be true", LocalServerEngine.isAnyServiceRunning())

        LocalServerEngine.toggleService("mysql")
        assertTrue("MySQL should be running", LocalServerEngine.status.mysql.isRunning)

        LocalServerEngine.toggleService("apache")
        assertFalse("Apache should be stopped", LocalServerEngine.status.apache.isRunning)
        assertTrue("MySQL is still running", LocalServerEngine.isAnyServiceRunning())
    }

    @Test
    fun testDatabaseCreation() {
        val created = LocalServerEngine.createDatabase("tienda_virtual")
        assertTrue("Database creation should succeed", created)
        assertTrue("Database list should contain newly created DB", LocalServerEngine.getDatabaseList().contains("tienda_virtual"))
    }

    @Test
    fun testExecuteSqlQuery() {
        val selectResult = LocalServerEngine.executeSqlQuery("SELECT * FROM usuarios")
        assertTrue("SELECT query should return table data", selectResult.contains("Maxwell Chacón"))

        val insertResult = LocalServerEngine.executeSqlQuery("INSERT INTO usuarios VALUES (4, 'Test')")
        assertTrue("INSERT query should return OK", insertResult.contains("Query OK"))

        val showTablesResult = LocalServerEngine.executeSqlQuery("SHOW TABLES")
        assertTrue("SHOW TABLES should list tables", showTablesResult.contains("usuarios"))
    }

    @Test
    fun testFormatFileSize() {
        assertEquals("500 B", LocalServerEngine.formatFileSize(500))
        assertEquals("2.0 KB", LocalServerEngine.formatFileSize(2048))
        assertEquals("1.50 MB", LocalServerEngine.formatFileSize((1.5 * 1024 * 1024).toLong()))
    }

    @Test
    fun testServerLogs() {
        LocalServerEngine.log("Prueba de registro de log")
        val logs = LocalServerEngine.getLogs()
        assertTrue("Logs should contain the recorded message", logs.any { it.contains("Prueba de registro de log") })
    }
}
