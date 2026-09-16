package com.maxwell.mbrowser.office

import android.app.Dialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LifecycleCoroutineScope
import com.maxwell.mbrowser.R
import com.maxwell.mbrowser.cloud.GoogleDriveConnector
import com.maxwell.mbrowser.office.ai.GeminiOfficeAssistant
import com.maxwell.mbrowser.office.models.DocumentType
import com.maxwell.mbrowser.office.models.OfficeDocument
import kotlinx.coroutines.launch

/**
 * OfficeViewerDialog - Dedicated Workspace for OfficeFreeToAndroid suite in MBrowser.
 * Provides instant document creation (Word/Writer, Excel/Calc, PowerPoint/Impress),
 * rich editing, Gemini AI assistant, and direct Google Drive cloud synchronization.
 */
object OfficeViewerDialog {

    fun showSuiteHub(context: Context, scope: LifecycleCoroutineScope) {
        val dialog = Dialog(context, R.style.Theme_MBrowser_Dialog)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_office_suite, null)
        dialog.setContentView(view)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnClose = view.findViewById<ImageButton>(R.id.btnCloseOffice)
        val btnWriter = view.findViewById<Button>(R.id.btnNewWriter)
        val btnCalc = view.findViewById<Button>(R.id.btnNewCalc)
        val btnImpress = view.findViewById<Button>(R.id.btnNewImpress)
        val btnAi = view.findViewById<Button>(R.id.btnAiAssistant)
        val layoutDocs = view.findViewById<LinearLayout>(R.id.layoutOfficeDocs)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmptyOffice)

        fun refreshDocList() {
            layoutDocs.removeAllViews()
            val docs = OfficeSuiteManager.loadAllDocuments(context)
            if (docs.isEmpty()) {
                layoutDocs.addView(tvEmpty)
            } else {
                for (doc in docs) {
                    val item = LayoutInflater.from(context).inflate(R.layout.item_office_doc, layoutDocs, false)
                    val ivIcon = item.findViewById<ImageView>(R.id.ivDocIcon)
                    val tvTitle = item.findViewById<TextView>(R.id.tvDocTitle)
                    val tvMeta = item.findViewById<TextView>(R.id.tvDocMeta)
                    val ivSync = item.findViewById<ImageView>(R.id.ivCloudSyncStatus)

                    tvTitle.text = doc.title
                    tvMeta.text = "${doc.type.name} • ${doc.getFormattedSize()}"

                    when (doc.type) {
                        DocumentType.WRITER -> ivIcon.setImageResource(R.drawable.ic_description)
                        DocumentType.CALC -> ivIcon.setImageResource(R.drawable.ic_table_chart)
                        DocumentType.IMPRESS -> ivIcon.setImageResource(R.drawable.ic_slideshow)
                        DocumentType.PDF -> ivIcon.setImageResource(R.drawable.ic_picture_as_pdf)
                    }

                    if (doc.isSyncedWithDrive) {
                        ivSync.setColorFilter(context.getColor(R.color.aqua_cyan))
                    }

                    item.setOnClickListener {
                        dialog.dismiss()
                        showEditor(context, doc, scope)
                    }

                    layoutDocs.addView(item)
                }
            }
        }

        refreshDocList()

        btnClose.setOnClickListener { dialog.dismiss() }

        btnWriter.setOnClickListener {
            val doc = OfficeSuiteManager.createNewDocument(context, "Documento_${System.currentTimeMillis() % 10000}", DocumentType.WRITER)
            dialog.dismiss()
            showEditor(context, doc, scope)
        }

        btnCalc.setOnClickListener {
            val doc = OfficeSuiteManager.createNewDocument(context, "Hoja_Calculo_${System.currentTimeMillis() % 10000}", DocumentType.CALC)
            dialog.dismiss()
            showEditor(context, doc, scope)
        }

        btnImpress.setOnClickListener {
            val doc = OfficeSuiteManager.createNewDocument(context, "Presentacion_${System.currentTimeMillis() % 10000}", DocumentType.IMPRESS)
            dialog.dismiss()
            showEditor(context, doc, scope)
        }

        btnAi.setOnClickListener {
            val doc = OfficeSuiteManager.createNewDocument(context, "Borrador_Gemini_AI", DocumentType.WRITER)
            dialog.dismiss()
            showEditor(context, doc, scope, autoTriggerAi = true)
        }

        dialog.show()
    }

    fun showEditor(context: Context, doc: OfficeDocument, scope: LifecycleCoroutineScope, autoTriggerAi: Boolean = false) {
        val dialog = Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_office_editor, null)
        dialog.setContentView(view)

        val etTitle = view.findViewById<EditText>(R.id.etEditorDocTitle)
        val tvSubtitle = view.findViewById<TextView>(R.id.tvDocTypeSubtitle)
        val ivType = view.findViewById<ImageView>(R.id.ivEditorDocType)
        val etContent = view.findViewById<EditText>(R.id.etDocumentContent)
        val btnClose = view.findViewById<ImageButton>(R.id.btnCloseEditor)
        val btnDrive = view.findViewById<ImageButton>(R.id.btnDriveSyncEditor)
        val btnSave = view.findViewById<Button>(R.id.btnSaveDocument)
        val btnPreview = view.findViewById<Button>(R.id.btnPrintPreview)
        val tvWordCount = view.findViewById<TextView>(R.id.tvWordCount)
        val tvCloudStatus = view.findViewById<TextView>(R.id.tvCloudSyncStatus)

        // AI Buttons
        val btnAiSummarize = view.findViewById<Button>(R.id.btnAiSummarize)
        val btnAiImprove = view.findViewById<Button>(R.id.btnAiImprove)
        val btnAiFormulas = view.findViewById<Button>(R.id.btnAiFormulas)
        val btnAiTranslate = view.findViewById<Button>(R.id.btnAiTranslate)

        // Formatting Ribbon Buttons
        val btnBold = view.findViewById<Button>(R.id.btnFormatBold)
        val btnItalic = view.findViewById<Button>(R.id.btnFormatItalic)
        val btnH1 = view.findViewById<Button>(R.id.btnFormatH1)
        val btnH2 = view.findViewById<Button>(R.id.btnFormatH2)
        val btnInsertTable = view.findViewById<Button>(R.id.btnInsertTable)
        val btnInsertBullets = view.findViewById<Button>(R.id.btnInsertBullets)

        etTitle.setText(doc.title)
        val initialContent = OfficeSuiteManager.readDocumentContent(doc)
        etContent.setText(initialContent)

        when (doc.type) {
            DocumentType.WRITER -> {
                ivType.setImageResource(R.drawable.ic_description)
                tvSubtitle.text = "OfficeFree Writer • Editor de Documentos (.docx / .odt)"
                btnInsertTable.text = "➕ Tabla"
            }
            DocumentType.CALC -> {
                ivType.setImageResource(R.drawable.ic_table_chart)
                tvSubtitle.text = "OfficeFree Calc • Hoja de Cálculo (.xlsx / .ods)"
                btnInsertTable.text = "➕ Fila/Col"
                btnH1.text = "fx SUM"
                btnH2.text = "fx PROM"
            }
            DocumentType.IMPRESS -> {
                ivType.setImageResource(R.drawable.ic_slideshow)
                tvSubtitle.text = "OfficeFree Impress • Diapositivas (.pptx / .odp)"
                btnInsertTable.text = "➕ Diapositiva"
                btnH1.text = "Título Diapositiva"
                btnH2.text = "Puntos Clave"
            }
            DocumentType.PDF -> {
                ivType.setImageResource(R.drawable.ic_picture_as_pdf)
                tvSubtitle.text = "Visor de Documentos PDF"
            }
        }

        fun updateWordCount() {
            val text = etContent.text.toString().trim()
            val words = if (text.isEmpty()) 0 else text.split("\\s+".toRegex()).size
            val chars = text.length
            tvWordCount.text = "Palabras: $words • Caracteres: $chars"
        }

        updateWordCount()

        etContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateWordCount()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        fun insertTextAtCursor(textToInsert: String) {
            val start = etContent.selectionStart.coerceAtLeast(0)
            val end = etContent.selectionEnd.coerceAtLeast(0)
            etContent.text.replace(start.coerceAtMost(end), start.coerceAtLeast(end), textToInsert, 0, textToInsert.length)
        }

        // Formatting actions
        btnBold.setOnClickListener {
            insertTextAtCursor("**Texto en Negrita**")
        }

        btnItalic.setOnClickListener {
            insertTextAtCursor("*Texto en Cursiva*")
        }

        btnH1.setOnClickListener {
            when (doc.type) {
                DocumentType.CALC -> insertTextAtCursor("=SUMA(A1:A10)")
                DocumentType.IMPRESS -> insertTextAtCursor("\n=== [DIAPOSITIVA: TÍTULO PRINCIPAL] ===\n")
                else -> insertTextAtCursor("\n# TÍTULO PRINCIPAL\n")
            }
        }

        btnH2.setOnClickListener {
            when (doc.type) {
                DocumentType.CALC -> insertTextAtCursor("=PROMEDIO(B1:B10)")
                DocumentType.IMPRESS -> insertTextAtCursor("\n• Punto Clave 1:\n• Punto Clave 2:\n• Punto Clave 3:\n")
                else -> insertTextAtCursor("\n## Subtítulo de Sección\n")
            }
        }

        btnInsertBullets.setOnClickListener {
            insertTextAtCursor("\n• Elemento 1\n• Elemento 2\n• Elemento 3\n")
        }

        btnInsertTable.setOnClickListener {
            when (doc.type) {
                DocumentType.CALC -> insertTextAtCursor("\n| Columna A | Columna B | Total |\n|---|---|---|\n| Item 1 | 100 | =A2*B2 |\n")
                DocumentType.IMPRESS -> insertTextAtCursor("\n\n--- [NUEVA DIAPOSITIVA] ---\n# Título\n- Contenido visual\n")
                else -> insertTextAtCursor("\n| Encabezado 1 | Encabezado 2 | Encabezado 3 |\n|---|---|---|\n| Dato A | Dato B | Dato C |\n| Dato X | Dato Y | Dato Z |\n")
            }
        }

        btnClose.setOnClickListener {
            // Auto save on exit
            doc.title = etTitle.text.toString().trim()
            OfficeSuiteManager.saveDocumentContent(doc, etContent.text.toString())
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            doc.title = etTitle.text.toString().trim()
            OfficeSuiteManager.saveDocumentContent(doc, etContent.text.toString())
            Toast.makeText(context, "💾 Documento guardado correctamente.", Toast.LENGTH_SHORT).show()
        }

        btnPreview.setOnClickListener {
            Toast.makeText(context, "📄 Exportando vista previa a PDF / Imprimir...", Toast.LENGTH_LONG).show()
        }

        btnDrive.setOnClickListener {
            doc.title = etTitle.text.toString().trim()
            OfficeSuiteManager.saveDocumentContent(doc, etContent.text.toString())
            Toast.makeText(context, "☁️ Sincronizando con Google Drive...", Toast.LENGTH_SHORT).show()
            GoogleDriveConnector.syncDocument(context, doc) {
                tvCloudStatus.text = "Sincronizado en Drive 🟢"
                tvCloudStatus.setTextColor(context.getColor(R.color.aqua_cyan))
            }
        }

        // Gemini AI Assistants
        btnAiSummarize.setOnClickListener {
            val text = etContent.text.toString()
            if (text.isBlank()) {
                Toast.makeText(context, "Escribe o pega texto para resumir.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(context, "✨ Gemini AI analizando y resumiendo...", Toast.LENGTH_SHORT).show()
            scope.launch {
                val summary = GeminiOfficeAssistant.processText(GeminiOfficeAssistant.AIAction.SUMMARIZE, text)
                etContent.setText(summary + "\n\n--- Contenido Original ---\n" + text)
            }
        }

        btnAiImprove.setOnClickListener {
            val text = etContent.text.toString()
            if (text.isBlank()) {
                Toast.makeText(context, "Escribe texto para que Gemini AI mejore la redacción.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(context, "✨ Gemini AI mejorando redacción y ortografía...", Toast.LENGTH_SHORT).show()
            scope.launch {
                val improved = GeminiOfficeAssistant.processText(GeminiOfficeAssistant.AIAction.IMPROVE_WRITING, text)
                etContent.setText(improved)
            }
        }

        btnAiFormulas.setOnClickListener {
            Toast.makeText(context, "✨ Gemini AI generando fórmulas de cálculo...", Toast.LENGTH_SHORT).show()
            scope.launch {
                val formulaHelp = GeminiOfficeAssistant.processText(GeminiOfficeAssistant.AIAction.GENERATE_FORMULA, "Fórmulas financieras y estadísticas")
                insertTextAtCursor("\n" + formulaHelp + "\n")
            }
        }

        btnAiTranslate.setOnClickListener {
            val text = etContent.text.toString()
            if (text.isBlank()) {
                Toast.makeText(context, "Escribe texto para traducir.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(context, "✨ Gemini AI traduciendo...", Toast.LENGTH_SHORT).show()
            scope.launch {
                val translated = GeminiOfficeAssistant.processText(GeminiOfficeAssistant.AIAction.TRANSLATE_ES, text)
                etContent.setText(translated)
            }
        }

        if (autoTriggerAi) {
            scope.launch {
                val draft = GeminiOfficeAssistant.processText(GeminiOfficeAssistant.AIAction.DRAFT_IDEAS, "Documento de Trabajo Inteligente")
                etContent.setText(draft)
            }
        }

        dialog.show()
    }
}
