package com.maxwell.mbrowser.office

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.lifecycle.LifecycleCoroutineScope
import com.maxwell.mbrowser.R
import com.maxwell.mbrowser.cloud.GoogleDriveConnector
import com.maxwell.mbrowser.office.ai.GeminiOfficeAssistant
import com.maxwell.mbrowser.office.models.DocumentType
import com.maxwell.mbrowser.office.models.OfficeDocument
import kotlinx.coroutines.launch

/**
 * OfficeViewerDialog - Fullscreen OpenOffice / LibreOffice Suite (OfficeFreeToAndroid) in MBrowser.
 * Provides the authentic, pixel-perfect LibreOffice interface with multi-tabs, toolbars,
 * Writer (Word), Calc (Excel), Impress (PowerPoint), Gemini AI Assistant, and Google Drive sync.
 */
object OfficeViewerDialog {

    @SuppressLint("SetJavaScriptEnabled")
    fun showSuiteHub(context: Context, scope: LifecycleCoroutineScope) {
        val dialog = Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_office_libreoffice_fullscreen, null)
        dialog.setContentView(view)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        val webView = view.findViewById<WebView>(R.id.officeWebView)
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Load documents into JS state if needed
                val docs = OfficeSuiteManager.loadAllDocuments(context)
                if (docs.isNotEmpty()) {
                    val first = docs.first()
                    val content = OfficeSuiteManager.readDocumentContent(first).replace("\\", "\\\\").replace("`", "\\`").replace("$", "\\$")
                    val escapedTitle = first.title.replace("'", "\\'")
                    val script = """
                        if (window.documents && window.documents.length > 0) {
                            window.documents[0] = {
                                id: '${first.id}',
                                title: '$escapedTitle',
                                type: '${first.type.name}',
                                content: `$content`
                            };
                            if (typeof renderTabs === 'function') renderTabs();
                            if (typeof loadActiveDocument === 'function') loadActiveDocument();
                        }
                    """.trimIndent()
                    webView.evaluateJavascript(script, null)
                }
            }
        }

        webView.addJavascriptInterface(object {
            @JavascriptInterface
            fun returnToBrowsing() {
                dialog.dismiss()
            }

            @JavascriptInterface
            fun saveDocument(id: String, title: String, type: String, content: String) {
                val docType = when (type.uppercase()) {
                    "CALC" -> DocumentType.CALC
                    "IMPRESS" -> DocumentType.IMPRESS
                    "PDF" -> DocumentType.PDF
                    else -> DocumentType.WRITER
                }
                val existing = OfficeSuiteManager.loadAllDocuments(context).find { it.id == id }
                val doc = existing ?: OfficeSuiteManager.createNewDocument(context, title, docType)
                doc.title = title
                OfficeSuiteManager.saveDocumentContent(doc, content)
                (context as? android.app.Activity)?.runOnUiThread {
                    Toast.makeText(context, "💾 Documento guardado: $title", Toast.LENGTH_SHORT).show()
                }
            }

            @JavascriptInterface
            fun syncDrive(id: String, title: String, content: String) {
                val existing = OfficeSuiteManager.loadAllDocuments(context).find { it.id == id }
                val doc = existing ?: OfficeSuiteManager.createNewDocument(context, title, DocumentType.WRITER)
                OfficeSuiteManager.saveDocumentContent(doc, content)
                (context as? android.app.Activity)?.runOnUiThread {
                    Toast.makeText(context, "☁️ Sincronizando con Google Drive...", Toast.LENGTH_SHORT).show()
                    GoogleDriveConnector.syncDocument(context, doc) {
                        Toast.makeText(context, "✅ Sincronizado en Drive: $title", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            @JavascriptInterface
            fun askGemini(action: String, text: String) {
                scope.launch {
                    val aiAction = when (action.uppercase()) {
                        "IMPROVE" -> GeminiOfficeAssistant.AIAction.IMPROVE_WRITING
                        "FORMULA" -> GeminiOfficeAssistant.AIAction.GENERATE_FORMULA
                        "TRANSLATE" -> GeminiOfficeAssistant.AIAction.TRANSLATE_ES
                        "DRAFT" -> GeminiOfficeAssistant.AIAction.DRAFT_IDEAS
                        else -> GeminiOfficeAssistant.AIAction.SUMMARIZE
                    }
                    val result = GeminiOfficeAssistant.processText(aiAction, text)
                    val escapedResult = result.replace("\\", "\\\\").replace("`", "\\`").replace("$", "\\$")
                    (context as? android.app.Activity)?.runOnUiThread {
                        val script = """
                            const page = document.getElementById('writer-page');
                            if (page) {
                                page.innerHTML += '<div style="background: #e8f4fd; border-left: 4px solid #00e5ff; padding: 10px; margin: 12px 0;"><strong>✨ Respuesta de Gemini AI:</strong><br>`$escapedResult`</div>';
                                if (typeof updateWordCount === 'function') updateWordCount();
                            }
                        """.trimIndent()
                        webView.evaluateJavascript(script, null)
                    }
                }
            }
        }, "OfficeBridge")

        webView.loadUrl("file:///android_asset/office/libreoffice.html")
        dialog.show()
    }

    fun showEditor(
        context: Context,
        doc: OfficeDocument,
        scope: LifecycleCoroutineScope,
        autoTriggerAi: Boolean = false,
        onDismissCallback: (() -> Unit)? = null
    ) {
        showSuiteHub(context, scope)
    }
}
