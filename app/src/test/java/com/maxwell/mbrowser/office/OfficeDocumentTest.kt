package com.maxwell.mbrowser.office

import com.maxwell.mbrowser.office.models.DocumentType
import com.maxwell.mbrowser.office.models.OfficeDocument
import org.junit.Assert.assertEquals
import org.junit.Test

class OfficeDocumentTest {

    @Test
    fun testFormattedSizeFormatting() {
        val smallDoc = OfficeDocument(
            id = "1",
            title = "Test.txt",
            type = DocumentType.WRITER,
            fileExtension = "txt",
            localPath = "/tmp/test.txt",
            sizeBytes = 512
        )
        assertEquals("512 B", smallDoc.getFormattedSize())

        val mediumDoc = OfficeDocument(
            id = "2",
            title = "Balance.xlsx",
            type = DocumentType.CALC,
            fileExtension = "xlsx",
            localPath = "/tmp/balance.xlsx",
            sizeBytes = 1024 * 45 // 45 KB
        )
        assertEquals("45 KB", mediumDoc.getFormattedSize())

        val largeDoc = OfficeDocument(
            id = "3",
            title = "Presentation.pptx",
            type = DocumentType.IMPRESS,
            fileExtension = "pptx",
            localPath = "/tmp/presentation.pptx",
            sizeBytes = (1024 * 1024 * 3.5).toLong() // 3.5 MB
        )
        assertEquals("3.5 MB", largeDoc.getFormattedSize())
    }
}
