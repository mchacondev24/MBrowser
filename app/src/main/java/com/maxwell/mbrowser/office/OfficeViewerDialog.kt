package com.maxwell.mbrowser.office

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
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
 * OfficeViewerDialog - Integrated UI controller for OfficeFreeToAndroid suite in MBrowser.
 * Provides document creation, viewing/editing, Gemini AI assist, and print preview.
 */
object OfficeViewerDialog {

    fun showSuiteHub(context: Context, scope: LifecycleCoroutineScope) {
        val dialog = Dialog(context, R.style.Theme_MBrowser_Dialog)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_office_suite, null)
        dialog.setContentView(view)
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
            val doc = OfficeSuiteManager.createNewDocument(context, "Nuevo_Documento_${System.currentTimeMillis() % 1000}", DocumentType.WRITER)
            dialog.dismiss()
            showEditor(context, doc, scope)
        }

        btnCalc.setOnClickListener {
            val doc = OfficeSuiteManager.createNewDocument(context, "Nueva_Hoja_${System.currentTimeMillis() % 1000}", DocumentType.CALC)
            dialog.dismiss()
            showEditor(context, doc, scope)
        }

        btnImpress.setOnClickListener {
            val doc = OfficeSuiteManager.createNewDocument(context, "Nueva_Presentacion_${System.currentTimeMillis() % 1000}", DocumentType.IMPRESS)
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
        val dialog = Dialog(context, R.style.Theme_MBrowser_Dialog)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_office_editor, null)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tvTitle = view.findViewById<TextView>(R.id.tvEditorTitle)
        val ivType = view.findViewById<ImageView>(R.id.ivEditorDocType)
        val etContent = view.findViewById<EditText>(R.id.etDocumentContent)
        val btnClose = view.findViewById<ImageButton>(R.id.btnCloseEditor)
        val btnDrive = view.findViewById<ImageButton>(R.id.btnDriveSyncEditor)
        val btnSave = view.findViewById<Button>(R.id.btnSaveDocument)
        val btnPreview = view.findViewById<Button>(R.id.btnPrintPreview)
        val btnAiSummarize = view.findViewById<Button>(R.id.btnAiSummarize)
        val btnAiImprove = view.findViewById<Button>(R.id.btnAiImprove)

        tvTitle.text = doc.title
        etContent.setText(OfficeSuiteManager.readDocumentContent(doc))

        when (doc.type) {
            DocumentType.WRITER -> ivType.setImageResource(R.drawable.ic_description)
            DocumentType.CALC -> ivType.setImageResource(R.drawable.ic_table_chart)
            DocumentType.IMPRESS -> ivType.setImageResource(R.drawable.ic_slideshow)
            DocumentType.PDF -> ivType.setImageResource(R.drawable.ic_picture_as_pdf)
        }

        btnClose.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            OfficeSuiteManager.saveDocumentContent(doc, etContent.text.toString())
            Toast.makeText(context, "Documento guardado con éxito.", Toast.LENGTH_SHORT).show()
        }

        btnPreview.setOnClickListener {
            Toast.makeText(context, "Generando vista previa de impresión y exportando a PDF...", Toast.LENGTH_SHORT).show()
        }

        btnDrive.setOnClickListener {
            GoogleDriveConnector.syncDocument(context, doc) {
                // Done
            }
        }

        btnAiSummarize.setOnClickListener {
            val text = etContent.text.toString()
            scope.launch {
                val summary = GeminiOfficeAssistant.processText(GeminiOfficeAssistant.AIAction.SUMMARIZE, text)
                etContent.setText(summary + "\n\n--- Texto Original ---\n" + text)
            }
        }

        btnAiImprove.setOnClickListener {
            val text = etContent.text.toString()
            scope.launch {
                val improved = GeminiOfficeAssistant.processText(GeminiOfficeAssistant.AIAction.IMPROVE_WRITING, text)
                etContent.setText(improved)
            }
        }

        if (autoTriggerAi) {
            scope.launch {
                val draft = GeminiOfficeAssistant.processText(GeminiOfficeAssistant.AIAction.DRAFT_IDEAS, "Borrador")
                etContent.setText(draft)
            }
        }

        dialog.show()
    }
}
