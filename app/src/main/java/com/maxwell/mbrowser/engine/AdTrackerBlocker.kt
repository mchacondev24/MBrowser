package com.maxwell.mbrowser.engine

import android.net.Uri
import android.webkit.WebResourceResponse
import java.io.ByteArrayInputStream
import java.util.concurrent.atomic.AtomicInteger

/**
 * AdTrackerBlocker - Core network interceptor for MBrowser Super Hiper Veloz Mode.
 * Aggressively cancels tracking pixels, intrusive ad scripts, telemetry, and analytics.
 */
object AdTrackerBlocker {

    private val blockedTrackerCount = AtomicInteger(0)
    var isEnabled: Boolean = true

    // Common tracking, analytics, and advertising host fragments
    private val BLOCKED_DOMAINS = hashSetOf(
        "google-analytics.com",
        "googletagmanager.com",
        "googleadservices.com",
        "googlesyndication.com",
        "doubleclick.net",
        "facebook.net",
        "connect.facebook.net",
        "graph.facebook.com",
        "ads.twitter.com",
        "static.ads-twitter.com",
        "analytics.twitter.com",
        "adservice.google.com",
        "adsystem.com",
        "criteo.com",
        "criteo.net",
        "scorecardresearch.com",
        "outbrain.com",
        "taboola.com",
        "quantserve.com",
        "adnxs.com",
        "amazon-adsystem.com",
        "advertising.com",
        "rubiconproject.com",
        "pubmatic.com",
        "openx.net",
        "adroll.com",
        "hotjar.com",
        "segment.io",
        "mixpanel.com",
        "amplitude.com",
        "newrelic.com",
        "clarity.ms",
        "sentry.io",
        "branch.io",
        "appsflyer.com",
        "adjust.com",
        "popads.net",
        "propellerads.com",
        "zedo.com",
        "moatads.com",
        "chartbeat.com"
    )

    private val BLOCKED_EXTENSIONS = listOf(
        "/ads.js",
        "/analytics.js",
        "/gtm.js",
        "/fbevents.js",
        "/pixel.gif",
        "/tracker.js",
        "/telemetry.js"
    )

    fun isTrackerOrAd(url: String?): Boolean {
        if (!isEnabled || url.isNullOrEmpty()) return false
        
        try {
            val uri = Uri.parse(url)
            val host = uri.host?.lowercase() ?: return false

            // Check host matching
            for (blocked in BLOCKED_DOMAINS) {
                if (host == blocked || host.endsWith(".$blocked")) {
                    blockedTrackerCount.incrementAndGet()
                    return true
                }
            }

            // Check path matching
            val path = uri.path?.lowercase() ?: ""
            for (ext in BLOCKED_EXTENSIONS) {
                if (path.endsWith(ext) || path.contains(ext)) {
                    blockedTrackerCount.incrementAndGet()
                    return true
                }
            }
        } catch (_: Exception) {
            // Ignore malformed URIs
        }

        return false
    }

    /**
     * Returns an empty response (200 OK, 0 bytes) to cancel blocked tracker requests immediately.
     */
    fun createEmptyBlockedResponse(): WebResourceResponse {
        return WebResourceResponse(
            "text/plain",
            "UTF-8",
            ByteArrayInputStream(ByteArray(0))
        )
    }

    fun getBlockedCount(): Int = blockedTrackerCount.get()

    fun resetBlockedCount() {
        blockedTrackerCount.set(0)
    }
}
