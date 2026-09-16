package com.maxwell.mbrowser.office

import android.content.Context
import com.maxwell.mbrowser.office.models.DocumentType
import com.maxwell.mbrowser.office.models.OfficeDocument
import java.io.File
import java.util.UUID

/**
 * OfficeSuiteManager - Core manager for OfficeFreeToAndroid integrated in MBrowser.
 * Provides template creation, document management, and file storage for Writer, Calc, Impress, and PDF.
 */
object OfficeSuiteManager {

    private val documentsList = mutableListOf<OfficeDocument>()

    fun getDocumentsDir(context: Context): File {
        val dir = File(context.filesDir, "mbrowser_office_docs")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun loadAllDocuments(context: Context): List<OfficeDocument> {
        val dir = getDocumentsDir(context)
        val files = dir.listFiles() ?: return emptyList()
        documentsList.clear()

        for (file in files.sortedByDescending { it.lastModified() }) {
            val ext = file.extension.lowercase()
            val type = when (ext) {
                "docx", "doc", "odt", "txt" -> DocumentType.WRITER
                "xlsx", "xls", "ods", "csv" -> DocumentType.CALC
                "pptx", "ppt", "odp" -> DocumentType.IMPRESS
                "pdf" -> DocumentType.PDF
                else -> DocumentType.WRITER
            }
            documentsList.add(
                OfficeDocument(
                    id = file.nameWithoutExtension,
                    title = file.name,
                    type = type,
                    fileExtension = ext,
                    localPath = file.absolutePath,
                    lastModified = file.lastModified(),
                    sizeBytes = file.length()
                )
            )
        }
        return documentsList
    }

    fun createNewDocument(context: Context, title: String, type: DocumentType): OfficeDocument {
        val dir = getDocumentsDir(context)
        val ext = when (type) {
            DocumentType.WRITER -> "docx"
            DocumentType.CALC -> "xlsx"
            DocumentType.IMPRESS -> "pptx"
            DocumentType.PDF -> "pdf"
        }
        val safeTitle = if (title.endsWith(".$ext")) title else "$title.$ext"
        val file = File(dir, safeTitle)

        // Write initial template content
        val initialContent = when (type) {
            DocumentType.WRITER -> "=== ${file.nameWithoutExtension} ===\n\nDocumento creado con MBrowser OfficeFreeToAndroid Suite.\nListo para edición y asistencia de Gemini AI.\n"
            DocumentType.CALC -> "ID,Nombre,Categoría,Monto,Estado\n1,Servidor Cloud,Infraestructura,150.00,Pagado\n2,Licencia Software,Desarrollo,75.50,Pendiente\n3,Dominio Web,Hosting,12.00,Activo\n"
            DocumentType.IMPRESS -> "=== Diapositiva 1: ${file.nameWithoutExtension} ===\n\n• Presentación creada con MBrowser Impress Engine\n• Soporte para gráficos y exportación a PDF\n"
            DocumentType.PDF -> "%PDF-1.4 [Documento PDF generado por MBrowser Office Suite]"
        }
        file.writeText(initialContent)

        val doc = OfficeDocument(
            id = UUID.randomUUID().toString(),
            title = safeTitle,
            type = type,
            fileExtension = ext,
            localPath = file.absolutePath,
            lastModified = file.lastModified(),
            sizeBytes = file.length()
        )
        documentsList.add(0, doc)
        return doc
    }

    fun readDocumentContent(doc: OfficeDocument): String {
        return try {
            val file = File(doc.localPath)
            if (file.exists()) file.readText() else "Archivo no disponible."
        } catch (e: Exception) {
            "Error al leer documento: ${e.message}"
        }
    }

    fun saveDocumentContent(doc: OfficeDocument, newContent: String) {
        try {
            val file = File(doc.localPath)
            file.writeText(newContent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteDocument(context: Context, docId: String): Boolean {
        return try {
            val doc = documentsList.find { it.id == docId }
            if (doc != null) {
                val file = File(doc.localPath)
                if (file.exists()) file.delete()
                documentsList.remove(doc)
                true
            } else {
                val dir = getDocumentsDir(context)
                val matching = dir.listFiles()?.firstOrNull { it.nameWithoutExtension == docId || it.name == docId }
                matching?.delete() ?: false
            }
        } catch (e: Exception) {
            false
        }
    }
}
