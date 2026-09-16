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
    fun testStopAllServices() {
        LocalServerEngine.status.apache.isRunning = true
        LocalServerEngine.status.mysql.isRunning = true
        LocalServerEngine.status.postgresql.isRunning = true

        LocalServerEngine.stopAllServices()

        assertFalse(LocalServerEngine.status.apache.isRunning)
        assertFalse(LocalServerEngine.status.mysql.isRunning)
        assertFalse(LocalServerEngine.status.postgresql.isRunning)
        assertFalse(LocalServerEngine.isAnyServiceRunning())
    }
}
