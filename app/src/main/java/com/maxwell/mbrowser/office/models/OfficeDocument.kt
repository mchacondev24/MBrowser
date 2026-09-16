package com.maxwell.mbrowser.office.models

import java.io.File

enum class DocumentType {
    WRITER,   // Documento de Texto (.docx, .odt, .txt)
    CALC,     // Hoja de Cálculo (.xlsx, .ods, .csv)
    IMPRESS,  // Presentación (.pptx, .odp)
    PDF       // Documento Portable (.pdf)
}

data class OfficeDocument(
    val id: String,
    var title: String,
    val type: DocumentType,
    val fileExtension: String,
    val localPath: String,
    var lastModified: Long = System.currentTimeMillis(),
    var sizeBytes: Long = 0,
    var isSyncedWithDrive: Boolean = false
) {
    val file: File get() = File(localPath)
    
    fun getFormattedSize(): String {
        return when {
            sizeBytes < 1024 -> "$sizeBytes B"
            sizeBytes < 1024 * 1024 -> "${sizeBytes / 1024} KB"
            else -> String.format("%.1f MB", sizeBytes / (1024.0 * 1024.0))
        }
    }
}
