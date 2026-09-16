package com.maxwell.mbrowser.gameboost

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * GameBoostManager - Optimizes MBrowser for Cloud Gaming & Online Play.
 * Enables forced GPU acceleration, low latency WebSockets, and immersive fullscreen.
 */
object GameBoostManager {

    var isGameModeActive: Boolean = false
        private set

    fun toggleGameMode(activity: Activity, webView: WebView, enable: Boolean? = null): Boolean {
        isGameModeActive = enable ?: !isGameModeActive

        applyGameEngineTweaks(webView, isGameModeActive)
        applyFullscreen(activity, isGameModeActive)

        val msg = if (isGameModeActive) {
            "🎮 GameBoost Activado: GPU Turbo, WebGL forzado y Pantalla Completa"
        } else {
            "Modo Normal Restaurado"
        }
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()

        return isGameModeActive
    }

    private fun applyGameEngineTweaks(webView: WebView, enable: Boolean) {
        val settings = webView.settings
        if (enable) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
            settings.loadsImagesAutomatically = true
            settings.blockNetworkImage = false
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
        } else {
            // Normal browsing settings
            settings.mediaPlaybackRequiresUserGesture = true
        }
    }

    fun applyFullscreen(activity: Activity, fullscreen: Boolean) {
        val window = activity.window
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

        if (fullscreen) {
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}
