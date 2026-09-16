package com.maxwell.mbrowser.hub

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.maxwell.mbrowser.R
import com.maxwell.mbrowser.engine.AdTrackerBlocker

/**
 * IngeHubManager - Central ecosystem hub connecting to Maxwell Chacon's web portfolio,
 * developer utilities, and optimized cloud gaming launchers.
 */
object IngeHubManager {

    fun showIngeHub(context: Context, onNavigate: (url: String) -> Unit) {
        val bottomSheetDialog = BottomSheetDialog(context, R.style.Theme_MBrowser_BottomSheet)
        val view = LayoutInflater.from(context).inflate(R.layout.sheet_ingehub, null)
        bottomSheetDialog.setContentView(view)

        val cardPortfolio = view.findViewById<LinearLayout>(R.id.cardPortfolio)
        val btnXbox = view.findViewById<Button>(R.id.btnLaunchXboxCloud)
        val btnGeForce = view.findViewById<Button>(R.id.btnLaunchGeForce)
        val btnPoki = view.findViewById<Button>(R.id.btnLaunchPoki)
        val tvStats = view.findViewById<TextView>(R.id.tvHubStats)

        val blocked = AdTrackerBlocker.getBlockedCount()
        tvStats.text = "Modo Super Veloz: $blocked rastreadores y scripts bloqueados en esta sesión."

        cardPortfolio.setOnClickListener {
            onNavigate("https://ingemaxwellchacon.com")
            bottomSheetDialog.dismiss()
        }

        btnXbox.setOnClickListener {
            onNavigate("https://www.xbox.com/play")
            bottomSheetDialog.dismiss()
        }

        btnGeForce.setOnClickListener {
            onNavigate("https://play.geforcenow.com")
            bottomSheetDialog.dismiss()
        }

        btnPoki.setOnClickListener {
            onNavigate("https://poki.com")
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }
}
