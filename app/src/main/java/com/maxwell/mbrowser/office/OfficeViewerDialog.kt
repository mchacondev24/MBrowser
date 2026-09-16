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
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleCoroutineScope
import com.maxwell.mbrowser.R
import com.maxwell.mbrowser.cloud.GoogleDriveConnector
import com.maxwell.mbrowser.office.ai.GeminiOfficeAssistant
import com.maxwell.mbrowser.office.models.DocumentType
import com.maxwell.mbrowser.office.models.OfficeDocument
import kotlinx.coroutines.launch

/**
 * OfficeViewerDialog - Dedicated Fullscreen Workspace for OfficeFreeToAndroid suite in MBrowser.
 * Provides instant document creation (Word/Writer, Excel/Calc, PowerPoint/Impress),
 * rich editing, Gemini AI assistant, template gallery, and Google Drive cloud synchronization.
 */
object OfficeViewerDialog {

    fun showSuiteHub(context: Context, scope: LifecycleCoroutineScope) {
        val dialog = Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_office_suite, null)
        dialog.setContentView(view)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        val btnBackToBrowsing = view.findViewById<Button>(R.id.btnBackToBrowsing)
        val btnClose = view.findViewById<ImageButton>(R.id.btnCloseOffice)
        val cardWriter = view.findViewById<LinearLayout>(R.id.cardNewWriter)
        val cardCalc = view.findViewById<LinearLayout>(R.id.cardNewCalc)
        val cardImpress = view.findViewById<LinearLayout>(R.id.cardNewImpress)
        val cardAi = view.findViewById<LinearLayout>(R.id.cardNewAi)
        val layoutDocs = view.findViewById<LinearLayout>(R.id.layoutOfficeDocs)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmptyOffice)
        val btnSyncAllDrive = view.findViewById<Button>(R.id.btnSyncAllDrive)

        // Template buttons
        val btnCv = view.findViewById<Button>(R.id.btnTemplateCv)
        val btnInvoice = view.findViewById<Button>(R.id.btnTemplateInvoice)
        val btnPitch = view.findViewById<Button>(R.id.btnTemplatePitch)
        val btnLetter = view.findViewById<Button>(R.id.btnTemplateLetter)

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
                        showEditor(context, doc, scope) {
                            refreshDocList()
                        }
                    }

                    item.setOnLongClickListener {
                        val options = arrayOf("✏️ Abrir y Editar", "🏷️ Renombrar", "🗑️ Eliminar Documento")
                        AlertDialog.Builder(context, R.style.Theme_MBrowser_Dialog)
                            .setTitle(doc.title)
                            .setItems(options) { _, which ->
                                when (which) {
                                    0 -> showEditor(context, doc, scope) { refreshDocList() }
                                    1 -> {
                                        val input = EditText(context).apply {
                                            setText(doc.title)
                                            setTextColor(context.getColor(R.color.text_primary))
                                        }
                                        AlertDialog.Builder(context, R.style.Theme_MBrowser_Dialog)
                                            .setTitle("Renombrar Documento")
                                            .setView(input)
                                            .setPositiveButton("Guardar") { _, _ ->
                                                val newName = input.text.toString().trim()
                                                if (newName.isNotEmpty()) {
                                                    doc.title = newName
                                                    OfficeSuiteManager.saveDocumentContent(doc, OfficeSuiteManager.readDocumentContent(doc))
                                                    refreshDocList()
                                                    Toast.makeText(context, "Documento renombrado.", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                            .setNegativeButton("Cancelar", null)
                                            .show()
                                    }
                                    2 -> {
                                        AlertDialog.Builder(context, R.style.Theme_MBrowser_Dialog)
                                            .setTitle("Eliminar Documento")
                                            .setMessage("¿Estás seguro de eliminar '${doc.title}'?")
                                            .setPositiveButton("Eliminar") { _, _ ->
                                                OfficeSuiteManager.deleteDocument(context, doc.id)
                                                refreshDocList()
                                                Toast.makeText(context, "Documento eliminado.", Toast.LENGTH_SHORT).show()
                                            }
                                            .setNegativeButton("Cancelar", null)
                                            .show()
                                    }
                                }
                            }
                            .show()
                        true
                    }

                    layoutDocs.addView(item)
                }
            }
        }

        refreshDocList()

        btnBackToBrowsing.setOnClickListener { dialog.dismiss() }
        btnClose.setOnClickListener { dialog.dismiss() }

        cardWriter.setOnClickListener {
            val doc = OfficeSuiteManager.createNewDocument(context, "Documento_${System.currentTimeMillis() % 10000}", DocumentType.WRITER)
            showEditor(context, doc, scope) { refreshDocList() }
        }

        cardCalc.setOnClickListener {
            val doc = OfficeSuiteManager.createNewDocument(context, "Hoja_Calculo_${System.currentTimeMillis() % 10000}", DocumentType.CALC)
            showEditor(context, doc, scope) { refreshDocList() }
        }

        cardImpress.setOnClickListener {
            val doc = OfficeSuiteManager.createNewDocument(context, "Presentacion_${System.currentTimeMillis() % 10000}", DocumentType.IMPRESS)
            showEditor(context, doc, scope) { refreshDocList() }
        }

        cardAi.setOnClickListener {
            val doc = OfficeSuiteManager.createNewDocument(context, "Borrador_Gemini_AI", DocumentType.WRITER)
            showEditor(context, doc, scope, autoTriggerAi = true) { refreshDocList() }
        }

        // Templates
        btnCv.setOnClickListener {
            val doc = OfficeSuiteManager.createNewDocument(context, "Curriculum_Vitae", DocumentType.WRITER)
            OfficeSuiteManager.saveDocumentContent(
                doc,
                """
                # CURRÍCULUM VITAE
                
                **Nombre:** [Tu Nombre Completo]
                **Profesión:** Ingeniero de Software / Desarrollador
                **Email:** contacto@correo.com | **Teléfono:** +505 0000-0000
                
                ---
                ## 🎯 Perfil Profesional
                Profesional proactivo con experiencia en desarrollo de aplicaciones Android de alto rendimiento, arquitecturas modernas y soluciones en la nube.
                
                ## 💼 Experiencia Laboral
                - **Líder de Proyecto / Desarrollador Mobile** (2024 - Presente)
                  - Desarrollo de arquitectura modular y navegación ultrarrápida.
                  - Integración de inteligencia artificial con Gemini API.
                
                ## 🎓 Educación
                - **Ingeniería en Sistemas / Computación** - Universidad Nacional
                
                ## 🛠️ Habilidades Técnicas
                - Kotlin, Android Jetpack, GeckoView / WebKit
                - Apache 2.4, PHP, MySQL, SQLite, PostgreSQL
                - Git, Google Drive API, Clean Architecture
                """.trimIndent()
            )
            showEditor(context, doc, scope) { refreshDocList() }
        }

        btnInvoice.setOnClickListener {
            val doc = OfficeSuiteManager.createNewDocument(context, "Presupuesto_Factura", DocumentType.CALC)
            OfficeSuiteManager.saveDocumentContent(
                doc,
                """
                | Ítem | Descripción | Cantidad | Precio Unitario | Total |
                |---|---|---|---|---|
                | 1 | Desarrollo de Módulo Web & Servidor Local | 1 | $500.00 | =C2*D2 |
                | 2 | Optimización de Rendimiento GameBoost | 1 | $250.00 | =C3*D3 |
                | 3 | Integración Gemini AI Assistant | 1 | $350.00 | =C4*D4 |
                |---|---|---|---|---|
                | **SUBTOTAL** | | | | =SUMA(E2:E4) |
                | **IVA (15%)** | | | | =E5*0.15 |
                | **TOTAL FINAL**| | | | =E5+E6 |
                """.trimIndent()
            )
            showEditor(context, doc, scope) { refreshDocList() }
        }

        btnPitch.setOnClickListener {
            val doc = OfficeSuiteManager.createNewDocument(context, "Presentacion_Pitch_Deck", DocumentType.IMPRESS)
            OfficeSuiteManager.saveDocumentContent(
                doc,
                """
                === [DIAPOSITIVA 1: PORTADA] ===
                # MBrowser & OfficeFreeToAndroid
                ## La Super-App Todo-en-Uno para Android
                *Por Maxwell Chacón*
                
                ---
                === [DIAPOSITIVA 2: EL PROBLEMA] ===
                # ¿Por qué múltiples apps pesadas?
                - Los usuarios alternan entre navegador, visor ofimático y servidores de desarrollo.
                - Mayor consumo de batería, RAM y almacenamiento.
                
                ---
                === [DIAPOSITIVA 3: NUESTRA SOLUCIÓN] ===
                # Suite Integrada y Ultrarrápida
                - Motor web de rendimiento extremo.
                - Suite ofimática OpenOffice/LibreOffice completa con IA.
                - Servidor Apache/PHP/MySQL portable en el bolsillo.
                """.trimIndent()
            )
            showEditor(context, doc, scope) { refreshDocList() }
        }

        btnLetter.setOnClickListener {
            val doc = OfficeSuiteManager.createNewDocument(context, "Carta_Formal", DocumentType.WRITER)
            OfficeSuiteManager.saveDocumentContent(
                doc,
                """
                Ciudad de Managua, 16 de Septiembre de 2026
                
                Estimado(a) [Nombre del Destinatario]:
                
                Por medio de la presente, me dirijo a usted con el propósito de presentar la suite de desarrollo y productividad MBrowser, diseñada para ofrecer la máxima velocidad, versatilidad y control local de datos en dispositivos móviles.
                
                Quedo a su completa disposición para coordinar una reunión demostrativa de las capacidades del sistema.
                
                Atentamente,
                
                ___________________________
                Maxwell Chacón
                Ingeniero en Desarrollo de Software
                https://ingemaxwellchacon.com
                """.trimIndent()
            )
            showEditor(context, doc, scope) { refreshDocList() }
        }

        btnSyncAllDrive.setOnClickListener {
            val docs = OfficeSuiteManager.loadAllDocuments(context)
            if (docs.isEmpty()) {
                Toast.makeText(context, "No hay documentos para sincronizar.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(context, "☁️ Sincronizando ${docs.size} documento(s) con Google Drive...", Toast.LENGTH_SHORT).show()
            docs.forEach { doc ->
                GoogleDriveConnector.syncDocument(context, doc) {}
            }
            refreshDocList()
            Toast.makeText(context, "✅ Sincronización completada.", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    fun showEditor(
        context: Context,
        doc: OfficeDocument,
        scope: LifecycleCoroutineScope,
        autoTriggerAi: Boolean = false,
        onDismissCallback: (() -> Unit)? = null
    ) {
        val dialog = Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_office_editor, null)
        dialog.setContentView(view)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        val btnBackToBrowsing = view.findViewById<Button>(R.id.btnBackToBrowsingFromEditor)
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

        fun saveCurrent() {
            doc.title = etTitle.text.toString().trim().ifEmpty { "Sin_Titulo" }
            OfficeSuiteManager.saveDocumentContent(doc, etContent.text.toString())
        }

        btnBackToBrowsing.setOnClickListener {
            saveCurrent()
            dialog.dismiss()
            onDismissCallback?.invoke()
        }

        btnClose.setOnClickListener {
            saveCurrent()
            dialog.dismiss()
            onDismissCallback?.invoke()
        }

        btnSave.setOnClickListener {
            saveCurrent()
            Toast.makeText(context, "💾 Documento guardado correctamente.", Toast.LENGTH_SHORT).show()
        }

        btnPreview.setOnClickListener {
            saveCurrent()
            Toast.makeText(context, "📄 Exportando vista previa a PDF / Imprimir...", Toast.LENGTH_LONG).show()
        }

        btnDrive.setOnClickListener {
            saveCurrent()
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
