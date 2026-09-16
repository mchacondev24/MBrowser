package com.maxwell.mbrowser.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AdTrackerBlockerTest {

    @Before
    fun setUp() {
        AdTrackerBlocker.isEnabled = true
        AdTrackerBlocker.resetBlockedCount()
    }

    @Test
    fun testBlockKnownTrackers() {
        val tracker1 = "https://www.google-analytics.com/analytics.js"
        val tracker2 = "https://connect.facebook.net/en_US/fbevents.js"
        val tracker3 = "https://adservice.google.com/adsid/google/ui"
        val tracker4 = "https://static.doubleclick.net/pixel.gif"

        assertTrue("Google Analytics should be blocked", AdTrackerBlocker.isTrackerOrAd(tracker1))
        assertTrue("Facebook Events should be blocked", AdTrackerBlocker.isTrackerOrAd(tracker2))
        assertTrue("AdService should be blocked", AdTrackerBlocker.isTrackerOrAd(tracker3))
        assertTrue("DoubleClick should be blocked", AdTrackerBlocker.isTrackerOrAd(tracker4))

        assertEquals(4, AdTrackerBlocker.getBlockedCount())
    }

    @Test
    fun testAllowLegitimateUrls() {
        val clean1 = "https://ingemaxwellchacon.com"
        val clean2 = "https://github.com/mchacondev24"
        val clean3 = "https://en.wikipedia.org/wiki/Android"
        val clean4 = "https://mozilla.org"

        assertFalse("Official portfolio should NOT be blocked", AdTrackerBlocker.isTrackerOrAd(clean1))
        assertFalse("GitHub should NOT be blocked", AdTrackerBlocker.isTrackerOrAd(clean2))
        assertFalse("Wikipedia should NOT be blocked", AdTrackerBlocker.isTrackerOrAd(clean3))
        assertFalse("Mozilla should NOT be blocked", AdTrackerBlocker.isTrackerOrAd(clean4))

        assertEquals(0, AdTrackerBlocker.getBlockedCount())
    }

    @Test
    fun testDisabledBlockerPassesEverything() {
        AdTrackerBlocker.isEnabled = false
        val tracker = "https://www.google-analytics.com/analytics.js"
        assertFalse("When disabled, tracker should not be blocked", AdTrackerBlocker.isTrackerOrAd(tracker))
        assertEquals(0, AdTrackerBlocker.getBlockedCount())
    }
}
