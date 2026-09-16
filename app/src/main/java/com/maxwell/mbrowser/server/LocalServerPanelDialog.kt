package com.maxwell.mbrowser.server

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.maxwell.mbrowser.R

/**
 * LocalServerPanelDialog - UI controller for ApacheMysqlAndroid_Server inside MBrowser.
 */
object LocalServerPanelDialog {

    fun show(context: Context, onOpenUrlInBrowser: (url: String) -> Unit) {
        val dialog = Dialog(context, R.style.Theme_MBrowser_Dialog)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_local_server, null)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnClose = view.findViewById<ImageButton>(R.id.btnCloseServer)
        val tvMaster = view.findViewById<TextView>(R.id.tvServerMasterStatus)
        val btnMaster = view.findViewById<Button>(R.id.btnToggleAllServices)
        val tvApache = view.findViewById<TextView>(R.id.tvStatusApache)
        val tvPhp = view.findViewById<TextView>(R.id.tvStatusPhp)
        val tvMysql = view.findViewById<TextView>(R.id.tvStatusMysql)
        val tvPostgres = view.findViewById<TextView>(R.id.tvStatusPostgres)
        val tvLan = view.findViewById<TextView>(R.id.tvLanAddress)
        val btnOpenLocalhost = view.findViewById<Button>(R.id.btnOpenLocalhostInBrowser)

        fun updateUI() {
            val isAny = LocalServerEngine.isAnyServiceRunning()
            if (isAny) {
                tvMaster.text = "Estado: Servidores Activos 🟢"
                btnMaster.text = "Detener Todos"
            } else {
                tvMaster.text = "Estado: Servicios Inactivos 🔴"
                btnMaster.text = "Iniciar Todos"
            }

            fun setBadge(tv: TextView, running: Boolean) {
                if (running) {
                    tv.text = "ONLINE"
                    tv.setTextColor(ContextCompat.getColor(context, R.color.game_emerald))
                } else {
                    tv.text = "OFF"
                    tv.setTextColor(ContextCompat.getColor(context, R.color.dev_error_red))
                }
            }

            setBadge(tvApache, LocalServerEngine.status.apache.isRunning)
            setBadge(tvPhp, LocalServerEngine.status.php.isRunning)
            setBadge(tvMysql, LocalServerEngine.status.mysql.isRunning)
            setBadge(tvPostgres, LocalServerEngine.status.postgresql.isRunning)

            tvLan.text = "LAN IP: ${LocalServerEngine.getLanServerUrl()} (Enrutamiento LAN activo)"
        }

        updateUI()

        btnClose.setOnClickListener { dialog.dismiss() }

        btnMaster.setOnClickListener {
            if (LocalServerEngine.isAnyServiceRunning()) {
                LocalServerEngine.stopAllServices()
                Toast.makeText(context, "Servidores detenidos.", Toast.LENGTH_SHORT).show()
            } else {
                LocalServerEngine.startAllServices(context)
                Toast.makeText(context, "Apache, PHP, MySQL y PostgreSQL iniciados 🚀", Toast.LENGTH_SHORT).show()
            }
            updateUI()
        }

        btnOpenLocalhost.setOnClickListener {
            if (!LocalServerEngine.status.apache.isRunning) {
                LocalServerEngine.startAllServices(context)
            }
            dialog.dismiss()
            onOpenUrlInBrowser(LocalServerEngine.getLocalServerUrl())
        }

        dialog.show()
    }
}
