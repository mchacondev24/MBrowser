package com.maxwell.mbrowser.office

import com.maxwell.mbrowser.office.ai.GeminiOfficeAssistant
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GeminiOfficeAssistantTest {

    @Test
    fun testSummarizeText() = runBlocking {
        val input = "MBrowser es una super app para Android con motor GeckoView y suite ofimática."
        val result = GeminiOfficeAssistant.processText(GeminiOfficeAssistant.AIAction.SUMMARIZE, input)
        assertNotNull(result)
        assertTrue("Result should contain summary keywords", result.contains("Resumen"))
    }

    @Test
    fun testGenerateFormula() = runBlocking {
        val input = "Buscar el precio del producto en la tabla"
        val result = GeminiOfficeAssistant.processText(GeminiOfficeAssistant.AIAction.GENERATE_FORMULA, input)
        assertNotNull(result)
        assertTrue("Result should contain spreadsheet formula", result.contains("BUSCARV") || result.contains("Fórmula"))
    }

    @Test
    fun testDraftIdeas() = runBlocking {
        val input = "Estrategia de lanzamiento"
        val result = GeminiOfficeAssistant.processText(GeminiOfficeAssistant.AIAction.DRAFT_IDEAS, input)
        assertNotNull(result)
        assertTrue("Result should contain draft points", result.contains("Borrador"))
    }

    @Test
    fun testEmptyInputHandled() = runBlocking {
        val result = GeminiOfficeAssistant.processText(GeminiOfficeAssistant.AIAction.SUMMARIZE, "")
        assertTrue("Empty input should return instructional message", result.contains("Por favor ingresa"))
    }
}
