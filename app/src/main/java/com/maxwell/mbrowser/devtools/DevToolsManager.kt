package com.maxwell.mbrowser.devtools

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebStorage
import android.webkit.WebView
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.maxwell.mbrowser.R
import java.util.concurrent.CopyOnWriteArrayList

/**
 * DevToolsManager - Real-time JavaScript Console, Error Inspector & Cache Purger for MBrowser.
 * Solves caching headaches during web development and allows 1-tap copying of errors.
 */
object DevToolsManager {

    var isDevModeEnabled: Boolean = true
    private val logList = CopyOnWriteArrayList<ConsoleLogItem>()
    var onLogsUpdatedListener: ((errorCount: Int) -> Unit)? = null

    fun addLog(item: ConsoleLogItem) {
        logList.add(0, item) // newest first
        if (logList.size > 200) {
            logList.removeAt(logList.size - 1)
        }
        val errorCount = getErrorCount()
        onLogsUpdatedListener?.invoke(errorCount)
    }

    fun getLogs(): List<ConsoleLogItem> = logList

    fun getErrorCount(): Int {
        return logList.count { it.level == LogLevel.ERROR }
    }

    fun clearLogs() {
        logList.clear()
        onLogsUpdatedListener?.invoke(0)
    }

    /**
     * Purges Memory Cache, Disk Cache, Web Storage, IndexedDB, and Cookies.
     */
    fun clearCache(context: Context, webView: WebView?, showToast: Boolean = true) {
        try {
            webView?.clearCache(true)
            webView?.clearFormData()
            WebStorage.getInstance().deleteAllData()

            val cookieManager = CookieManager.getInstance()
            cookieManager.removeAllCookies(null)
            cookieManager.flush()

            // Purge local cache folder
            context.cacheDir.deleteRecursively()

            if (showToast) {
                Toast.makeText(
                    context,
                    context.getString(R.string.devtools_cache_cleared),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Executes a Hard Reload on the active WebView, bypassing cache completely.
     */
    fun hardReload(context: Context, webView: WebView) {
        clearCache(context, webView, showToast = false)
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        webView.reload()
        Toast.makeText(context, "Hard Reload ejecutado (Sin Caché)", Toast.LENGTH_SHORT).show()
    }

    /**
     * Compiles all console logs/errors into a clean string for clipboard.
     */
    fun getAllErrorsAsText(): String {
        if (logList.isEmpty()) {
            return "MBrowser DevTools: No errors or console logs detected."
        }
        val sb = StringBuilder()
        sb.append("=== MBrowser DevTools Console Logs ===\n")
        sb.append("Total Items: ").append(logList.size).append("\n")
        sb.append("Errors: ").append(getErrorCount()).append("\n\n")

        for (log in logList) {
            sb.append(log.toFormattedString()).append("\n")
        }
        return sb.toString()
    }

    /**
     * Copies any string text to the Android Clipboard.
     */
    fun copyToClipboard(context: Context, text: String, message: String = "Copiado al portapapeles") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("MBrowser DevTools Logs", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Displays the Glassmorphism DevTools Console Dialog.
     */
    fun showDevToolsDialog(context: Context, webView: WebView) {
        val dialog = Dialog(context, R.style.Theme_MBrowser_Dialog)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_devtools, null)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tvSummary = view.findViewById<TextView>(R.id.tvDevSummary)
        val btnClose = view.findViewById<ImageButton>(R.id.btnCloseDevTools)
        val btnCopyAll = view.findViewById<Button>(R.id.btnCopyAllErrors)
        val btnClearCache = view.findViewById<Button>(R.id.btnClearCacheDev)
        val layoutLogs = view.findViewById<LinearLayout>(R.id.layoutLogItems)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmptyLogs)

        fun refreshLogListUI() {
            layoutLogs.removeAllViews()
            val logs = getLogs()
            val errorCount = getErrorCount()
            tvSummary.text = "Consola activa: $errorCount error(es), ${logs.size} registros en total."

            if (logs.isEmpty()) {
                layoutLogs.addView(tvEmpty)
            } else {
                for (item in logs) {
                    val itemView = LayoutInflater.from(context).inflate(R.layout.item_console_log, layoutLogs, false)
                    val tvLevel = itemView.findViewById<TextView>(R.id.tvLogLevel)
                    val tvSource = itemView.findViewById<TextView>(R.id.tvSource)
                    val tvMsg = itemView.findViewById<TextView>(R.id.tvLogMessage)
                    val btnCopySingle = itemView.findViewById<ImageButton>(R.id.btnCopySingleLog)

                    tvLevel.text = item.level.name
                    when (item.level) {
                        LogLevel.ERROR -> {
                            tvLevel.setTextColor(ContextCompat.getColor(context, R.color.dev_error_red))
                            tvLevel.setBackgroundResource(R.drawable.bg_clear_cache_button)
                        }
                        LogLevel.WARN -> {
                            tvLevel.setTextColor(ContextCompat.getColor(context, R.color.dev_warn_yellow))
                            tvLevel.setBackgroundResource(R.drawable.bg_turbo_badge)
                        }
                        LogLevel.INFO -> {
                            tvLevel.setTextColor(ContextCompat.getColor(context, R.color.dev_info_blue))
                            tvLevel.setBackgroundResource(R.drawable.bg_aqua_pill)
                        }
                        LogLevel.LOG -> {
                            tvLevel.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                            tvLevel.setBackgroundResource(R.drawable.bg_dev_badge)
                        }
                    }

                    tvSource.text = if (!item.sourceId.isNullOrEmpty()) "${item.sourceId}:${item.lineNumber}" else "inline"
                    tvMsg.text = item.message

                    btnCopySingle.setOnClickListener {
                        copyToClipboard(context, item.toFormattedString(), "Error copiado")
                    }

                    layoutLogs.addView(itemView)
                }
            }
        }

        refreshLogListUI()

        btnClose.setOnClickListener { dialog.dismiss() }

        btnCopyAll.setOnClickListener {
            copyToClipboard(context, getAllErrorsAsText(), context.getString(R.string.devtools_errors_copied))
        }

        btnClearCache.setOnClickListener {
            clearCache(context, webView, showToast = true)
            hardReload(context, webView)
            refreshLogListUI()
            dialog.dismiss()
        }

        dialog.show()
    }
}
