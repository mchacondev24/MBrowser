package com.maxwell.mbrowser.cloud

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import com.maxwell.mbrowser.R

/**
 * CloudDriveDialog - UI controller for Google Drive Sync & Backup in MBrowser.
 */
object CloudDriveDialog {

    fun show(context: Context) {
        val dialog = Dialog(context, R.style.Theme_MBrowser_Dialog)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_cloud_drive, null)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnClose = view.findViewById<ImageButton>(R.id.btnCloseDrive)
        val tvStatus = view.findViewById<TextView>(R.id.tvDriveAccountStatus)
        val btnConnect = view.findViewById<Button>(R.id.btnConnectDrive)
        val btnBackup = view.findViewById<Button>(R.id.btnBackupBrowserData)

        fun updateUI() {
            if (GoogleDriveConnector.isSignedIn) {
                tvStatus.text = "Estado: Conectado (${GoogleDriveConnector.userEmail}) ☁️"
                btnConnect.text = "Desconectar Cuenta"
            } else {
                tvStatus.text = "Estado: Desconectado ⚪"
                btnConnect.text = "Conectar con Google Drive"
            }
        }

        updateUI()

        btnClose.setOnClickListener { dialog.dismiss() }

        btnConnect.setOnClickListener {
            GoogleDriveConnector.toggleAuth(context) {
                updateUI()
            }
        }

        btnBackup.setOnClickListener {
            GoogleDriveConnector.backupBrowserData(context)
        }

        dialog.show()
    }
}
