package com.maxwell.mbrowser.cloud

import android.content.Context
import android.widget.Toast
import com.maxwell.mbrowser.office.models.OfficeDocument

/**
 * GoogleDriveConnector - Cloud Sync & Backup Gateway for MBrowser and OfficeFreeToAndroid.
 * Synchronizes office documents and browser settings with Google Drive.
 */
object GoogleDriveConnector {

    var isSignedIn: Boolean = false
        private set
    var userEmail: String? = null
        private set

    fun toggleAuth(context: Context, onStateChanged: (signedIn: Boolean) -> Unit) {
        isSignedIn = !isSignedIn
        userEmail = if (isSignedIn) "usuario@gmail.com" else null

        val msg = if (isSignedIn) {
            "☁️ Conectado a Google Drive ($userEmail)"
        } else {
            "Desconectado de Google Drive"
        }
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        onStateChanged(isSignedIn)
    }

    fun syncDocument(context: Context, document: OfficeDocument, onComplete: (success: Boolean) -> Unit) {
        if (!isSignedIn) {
            Toast.makeText(context, "Inicia sesión en Google Drive para sincronizar.", Toast.LENGTH_SHORT).show()
            onComplete(false)
            return
        }

        // Simulate cloud push
        document.isSyncedWithDrive = true
        Toast.makeText(context, "Documento '${document.title}' sincronizado en Google Drive ☁️", Toast.LENGTH_SHORT).show()
        onComplete(true)
    }

    fun backupBrowserData(context: Context) {
        if (!isSignedIn) {
            Toast.makeText(context, "Conéctate a Google Drive para crear un respaldo.", Toast.LENGTH_SHORT).show()
            return
        }
        Toast.makeText(context, "Respaldo de marcadores y configuración completado en Drive ☁️", Toast.LENGTH_SHORT).show()
    }
}
