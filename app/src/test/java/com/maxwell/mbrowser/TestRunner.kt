package com.maxwell.mbrowser

import com.maxwell.mbrowser.devtools.DevToolsManagerTest
import com.maxwell.mbrowser.engine.AdTrackerBlockerTest
import com.maxwell.mbrowser.office.GeminiOfficeAssistantTest
import com.maxwell.mbrowser.office.OfficeDocumentTest
import com.maxwell.mbrowser.server.LocalServerEngineTest
import org.junit.runner.JUnitCore

/**
 * TestRunner - Unified standalone runner for executing the full MBrowser unit test suite.
 */
object TestRunner {
    @JvmStatic
    fun main(args: Array<String>) {
        println("==================================================")
        println("🧪 INICIANDO SUITE DE PRUEBAS UNITARIAS: MBROWSER")
        println("==================================================")

        val result = JUnitCore.runClasses(
            AdTrackerBlockerTest::class.java,
            DevToolsManagerTest::class.java,
            OfficeDocumentTest::class.java,
            GeminiOfficeAssistantTest::class.java,
            LocalServerEngineTest::class.java
        )

        println("📊 RESUMEN DE PRUEBAS:")
        println("• Pruebas Ejecutadas: ${result.runCount}")
        println("• Pruebas Fallidas:   ${result.failureCount}")
        println("• Pruebas Ignoradas:  ${result.ignoreCount}")
        println("• Tiempo Total:       ${result.runTime} ms")

        if (result.wasSuccessful()) {
            println("✅ TODAS LAS PRUEBAS UNITARIAS PASARON EXITOSAMENTE (100% OK)")
        } else {
            println("❌ FALLOS DETECTADOS:")
            for (failure in result.failures) {
                println("  - ${failure.testHeader}: ${failure.message}")
            }
        }
        println("==================================================")
    }
}
