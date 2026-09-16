package com.maxwell.mbrowser.dialogs

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import com.maxwell.mbrowser.R

/**
 * AboutDialog - Displays legal attribution, MPL 2.0 Open Source notices,
 * and developer information for MBrowser.
 */
object AboutDialog {

    fun show(context: Context) {
        val dialog = Dialog(context, R.style.Theme_MBrowser_Dialog)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_about, null)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnOk = view.findViewById<Button>(R.id.btnOkAbout)
        btnOk.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
