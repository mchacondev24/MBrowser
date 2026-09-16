package com.maxwell.mbrowser

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.maxwell.mbrowser.cloud.CloudDriveDialog
import com.maxwell.mbrowser.databinding.ActivityMainBinding
import com.maxwell.mbrowser.devtools.ConsoleLogItem
import com.maxwell.mbrowser.devtools.DevToolsManager
import com.maxwell.mbrowser.devtools.LogLevel
import com.maxwell.mbrowser.dialogs.AboutDialog
import com.maxwell.mbrowser.engine.AdTrackerBlocker
import com.maxwell.mbrowser.gameboost.GameBoostManager
import com.maxwell.mbrowser.hub.IngeHubManager
import com.maxwell.mbrowser.office.OfficeViewerDialog
import com.maxwell.mbrowser.server.LocalServerPanelDialog

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var webView: WebView
    private val defaultHomeUrl = "https://ingemaxwellchacon.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        webView = binding.mainWebView

        setupWebView()
        setupListeners()
        setupDevToolsObserver()
        setupBackNavigation()

        // Load Default Home Page (Inge Maxwell Chacon Portfolio)
        loadUrl(defaultHomeUrl)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        settings.cacheMode = WebSettings.LOAD_DEFAULT

        // High Performance Hardware Acceleration
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        // Javascript Bridge for DevTools
        webView.addJavascriptInterface(MBrowserJsBridge(), "MBrowserBridge")

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (newProgress < 100) {
                    binding.progressBarLoading.visibility = View.VISIBLE
                    binding.progressBarLoading.progress = newProgress
                } else {
                    binding.progressBarLoading.visibility = View.GONE
                }
            }

            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                if (consoleMessage != null && DevToolsManager.isDevModeEnabled) {
                    val level = when (consoleMessage.messageLevel()) {
                        ConsoleMessage.MessageLevel.ERROR -> LogLevel.ERROR
                        ConsoleMessage.MessageLevel.WARNING -> LogLevel.WARN
                        ConsoleMessage.MessageLevel.LOG -> LogLevel.LOG
                        else -> LogLevel.INFO
                    }
                    DevToolsManager.addLog(
                        ConsoleLogItem(
                            level = level,
                            message = consoleMessage.message() ?: "",
                            sourceId = consoleMessage.sourceId(),
                            lineNumber = consoleMessage.lineNumber()
                        )
                    )
                }
                return true
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                val url = request?.url?.toString()
                if (AdTrackerBlocker.isTrackerOrAd(url)) {
                    return AdTrackerBlocker.createEmptyBlockedResponse()
                }
                return super.shouldInterceptRequest(view, request)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                url?.let {
                    binding.etUrlInput.setText(it)
                    updateSecurityIcon(it)
                }
                binding.btnGoOrReload.setImageResource(R.drawable.ic_close)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.btnGoOrReload.setImageResource(R.drawable.ic_refresh)
                updateNavigationButtons()

                // Inject client-side error reporter to catch runtime exceptions and unhandled promise rejections
                val errorScript = """
                    (function() {
                        if (window.__mbrowser_injected) return;
                        window.__mbrowser_injected = true;
                        window.addEventListener('error', function(e) {
                            if (window.MBrowserBridge) {
                                window.MBrowserBridge.reportError(e.message || '', e.filename || '', e.lineno || 0);
                            }
                        });
                        window.addEventListener('unhandledrejection', function(e) {
                            if (window.MBrowserBridge) {
                                window.MBrowserBridge.reportError('Unhandled Promise Rejection: ' + (e.reason ? e.reason.message || e.reason : ''), '', 0);
                            }
                        });
                    })();
                """.trimIndent()
                webView.evaluateJavascript(errorScript, null)
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                    DevToolsManager.addLog(
                        ConsoleLogItem(
                            level = LogLevel.ERROR,
                            message = "Network Error (${error?.errorCode}): ${error?.description}",
                            sourceId = request.url.toString(),
                            lineNumber = 0
                        )
                    )
                }
            }
        }
    }

    private fun setupListeners() {
        // Address input listener
        binding.etUrlInput.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_GO ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                val input = binding.etUrlInput.text.toString().trim()
                if (input.isNotEmpty()) {
                    loadUrl(input)
                    hideKeyboard()
                }
                true
            } else {
                false
            }
        }

        // Quick Cache Purge Button inside search bar
        binding.btnQuickClearCache.setOnClickListener {
            DevToolsManager.clearCache(this, webView)
            DevToolsManager.hardReload(this, webView)
        }

        // Go / Reload Button
        binding.btnGoOrReload.setOnClickListener {
            if (binding.progressBarLoading.visibility == View.VISIBLE) {
                webView.stopLoading()
            } else {
                webView.reload()
            }
        }

        // IngeHub Button
        binding.btnIngeHub.setOnClickListener {
            IngeHubManager.showIngeHub(this) { url ->
                loadUrl(url)
            }
        }

        // OfficeFreeToAndroid Module Button
        binding.btnOfficeModule.setOnClickListener {
            OfficeViewerDialog.showSuiteHub(this, lifecycleScope)
        }

        // Local Server Module Button
        binding.btnServerModule.setOnClickListener {
            LocalServerPanelDialog.show(this) { url ->
                loadUrl(url)
            }
        }

        // Cloud Drive Module Button
        binding.btnCloudDriveModule.setOnClickListener {
            CloudDriveDialog.show(this)
        }

        // Super Veloz Mode Toggle
        binding.btnSuperVelozToggle.setOnClickListener {
            AdTrackerBlocker.isEnabled = !AdTrackerBlocker.isEnabled
            if (AdTrackerBlocker.isEnabled) {
                binding.tvSuperVelozLabel.text = "Turbo: ON"
                binding.btnSuperVelozToggle.setBackgroundResource(R.drawable.bg_turbo_badge)
                Toast.makeText(this, "🚀 Modo Super Hiper Veloz Activado", Toast.LENGTH_SHORT).show()
            } else {
                binding.tvSuperVelozLabel.text = "Turbo: OFF"
                binding.btnSuperVelozToggle.setBackgroundResource(R.drawable.bg_aqua_pill)
                Toast.makeText(this, "Modo Super Veloz Desactivado", Toast.LENGTH_SHORT).show()
            }
        }

        // GameBoost Mode Toggle
        binding.btnGameBoostToggle.setOnClickListener {
            toggleGameBoostMode()
        }

        binding.btnExitFullscreen.setOnClickListener {
            toggleGameBoostMode(forceDisable = true)
        }

        // DevTools Dialog Toggle
        binding.btnDevToolsToggle.setOnClickListener {
            DevToolsManager.showDevToolsDialog(this, webView)
        }

        // Floating DevTools Error Bar
        binding.floatingDevBar.setOnClickListener {
            DevToolsManager.showDevToolsDialog(this, webView)
        }

        binding.btnFloatingCopyErrors.setOnClickListener {
            val errorsText = DevToolsManager.getAllErrorsAsText()
            DevToolsManager.copyToClipboard(
                this,
                errorsText,
                getString(R.string.devtools_errors_copied)
            )
        }

        // Bottom Navigation Bar Buttons
        binding.btnNavBack.setOnClickListener {
            if (webView.canGoBack()) webView.goBack()
        }

        binding.btnNavForward.setOnClickListener {
            if (webView.canGoForward()) webView.goForward()
        }

        binding.btnNavHome.setOnClickListener {
            loadUrl(defaultHomeUrl)
        }

        binding.btnHardReload.setOnClickListener {
            DevToolsManager.hardReload(this, webView)
        }

        binding.btnNavMenu.setOnClickListener {
            showMainMenu()
        }
    }

    private fun setupDevToolsObserver() {
        DevToolsManager.onLogsUpdatedListener = { errorCount ->
            runOnUiThread {
                if (errorCount > 0) {
                    binding.tvDevBadgeErrors.text = "Dev ($errorCount)"
                    binding.tvDevBadgeErrors.setTextColor(ContextCompat.getColor(this, R.color.dev_error_red))
                    binding.floatingDevBar.visibility = View.VISIBLE
                    binding.tvFloatingErrorText.text = "$errorCount error(es) JavaScript detectados"
                } else {
                    binding.tvDevBadgeErrors.text = "Dev"
                    binding.tvDevBadgeErrors.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
                    binding.floatingDevBar.visibility = View.GONE
                }
            }
        }
    }

    private fun toggleGameBoostMode(forceDisable: Boolean = false) {
        val target = if (forceDisable) false else !GameBoostManager.isGameModeActive
        val active = GameBoostManager.toggleGameMode(this, webView, target)

        if (active) {
            binding.tvGameBoostLabel.text = "Gaming: ON"
            binding.btnGameBoostToggle.setBackgroundResource(R.drawable.bg_game_badge)
            binding.topToolbarContainer.visibility = View.GONE
            binding.bottomToolbarContainer.visibility = View.GONE
            binding.btnExitFullscreen.visibility = View.VISIBLE
        } else {
            binding.tvGameBoostLabel.text = "Gaming"
            binding.btnGameBoostToggle.setBackgroundResource(R.drawable.bg_aqua_pill)
            binding.topToolbarContainer.visibility = View.VISIBLE
            binding.bottomToolbarContainer.visibility = View.VISIBLE
            binding.btnExitFullscreen.visibility = View.GONE
        }
    }

    private fun loadUrl(rawUrl: String) {
        var formatted = rawUrl.trim()
        if (!formatted.startsWith("http://") && !formatted.startsWith("https://")) {
            formatted = if (formatted.contains(".") && !formatted.contains(" ")) {
                "https://$formatted"
            } else {
                "https://www.google.com/search?q=" + java.net.URLEncoder.encode(formatted, "UTF-8")
            }
        }
        DevToolsManager.clearLogs()
        webView.loadUrl(formatted)
    }

    private fun updateSecurityIcon(url: String) {
        if (url.startsWith("https://")) {
            binding.ivSecurityStatus.setImageResource(R.drawable.ic_shield)
            binding.ivSecurityStatus.setColorFilter(ContextCompat.getColor(this, R.color.aqua_cyan))
        } else {
            binding.ivSecurityStatus.setImageResource(R.drawable.ic_shield)
            binding.ivSecurityStatus.setColorFilter(ContextCompat.getColor(this, R.color.dev_warn_yellow))
        }
    }

    private fun updateNavigationButtons() {
        binding.btnNavBack.alpha = if (webView.canGoBack()) 1.0f else 0.4f
        binding.btnNavForward.alpha = if (webView.canGoForward()) 1.0f else 0.4f
    }

    private fun showMainMenu() {
        val options = arrayOf(
            "🏠 Ir a Inicio (ingemaxwellchacon.com)",
            "📄 Suite Ofimática (Writer, Calc, Impress, AI)",
            "🖥️ Servidor Local (Apache, PHP, MySQL, Postgres)",
            "☁️ Google Drive & Respaldo Cloud",
            "🧹 Limpiar Todo el Caché y Datos",
            "🚀 Modo Super Hiper Veloz (${if (AdTrackerBlocker.isEnabled) "Activo" else "Inactivo"})",
            "🎮 Modo Gaming & Cloud Play",
            "🛠️ Abrir Consola / Inspector de Errores",
            "ℹ️ Acerca de MBrowser & Atribuciones"
        )

        AlertDialog.Builder(this, R.style.Theme_MBrowser_Dialog)
            .setTitle("Menú de MBrowser All-in-One")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> loadUrl(defaultHomeUrl)
                    1 -> OfficeViewerDialog.showSuiteHub(this, lifecycleScope)
                    2 -> LocalServerPanelDialog.show(this) { url -> loadUrl(url) }
                    3 -> CloudDriveDialog.show(this)
                    4 -> DevToolsManager.clearCache(this, webView, showToast = true)
                    5 -> binding.btnSuperVelozToggle.performClick()
                    6 -> toggleGameBoostMode()
                    7 -> DevToolsManager.showDevToolsDialog(this, webView)
                    8 -> AboutDialog.show(this)
                }
            }
            .show()
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (GameBoostManager.isGameModeActive) {
                    toggleGameBoostMode(forceDisable = true)
                    return
                }
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    finish()
                }
            }
        })
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(binding.etUrlInput.windowToken, 0)
    }

    inner class MBrowserJsBridge {
        @JavascriptInterface
        fun reportError(message: String, source: String, lineno: Int) {
            DevToolsManager.addLog(
                ConsoleLogItem(
                    level = LogLevel.ERROR,
                    message = message,
                    sourceId = source,
                    lineNumber = lineno
                )
            )
        }
    }
}
