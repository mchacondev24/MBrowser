package com.maxwell.mbrowser.server

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.maxwell.mbrowser.R

/**
 * LocalServerPanelDialog - UI controller for ApacheMysqlAndroid_Server inside MBrowser.
 * Features: Instant service start/stop, LAN sharing for multi-phone testing,
 * ZIP website deployment, and SQL database management.
 */
object LocalServerPanelDialog {

    fun show(context: Context, onOpenUrlInBrowser: (url: String) -> Unit) {
        val dialog = Dialog(context, R.style.Theme_MBrowser_Dialog)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_local_server, null)
        dialog.setContentView(view)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnClose = view.findViewById<ImageButton>(R.id.btnCloseServer)
        val tvMaster = view.findViewById<TextView>(R.id.tvServerMasterStatus)
        val btnMaster = view.findViewById<Button>(R.id.btnToggleAllServices)
        val tvLan = view.findViewById<TextView>(R.id.tvLanAddress)
        val btnCopyLan = view.findViewById<Button>(R.id.btnCopyLanUrl)
        val btnShareLan = view.findViewById<Button>(R.id.btnShareLanUrl)
        val btnDeploy = view.findViewById<Button>(R.id.btnDeployWebsite)
        val tvDatabases = view.findViewById<TextView>(R.id.tvActiveDatabase)
        val btnCreateDb = view.findViewById<Button>(R.id.btnCreateDb)
        val btnExecuteSql = view.findViewById<Button>(R.id.btnExecuteSql)
        val tvApache = view.findViewById<TextView>(R.id.tvStatusApache)
        val tvPhp = view.findViewById<TextView>(R.id.tvStatusPhp)
        val tvMysql = view.findViewById<TextView>(R.id.tvStatusMysql)
        val btnOpenLocalhost = view.findViewById<Button>(R.id.btnOpenLocalhostInBrowser)

        fun updateUI() {
            val isAny = LocalServerEngine.isAnyServiceRunning()
            if (isAny) {
                tvMaster.text = "Estado: Servidor Activo 🟢"
                btnMaster.text = "Detener"
                btnMaster.setBackgroundResource(R.drawable.bg_dev_badge)
            } else {
                tvMaster.text = "Estado: Servidor Detenido 🔴"
                btnMaster.text = "Iniciar"
                btnMaster.setBackgroundResource(R.drawable.bg_aqua_button)
            }

            fun setBadge(tv: TextView, name: String, running: Boolean) {
                if (running) {
                    tv.text = "$name: ON"
                    tv.setTextColor(ContextCompat.getColor(context, R.color.game_emerald))
                } else {
                    tv.text = "$name: OFF"
                    tv.setTextColor(ContextCompat.getColor(context, R.color.dev_error_red))
                }
            }

            setBadge(tvApache, "Apache", LocalServerEngine.status.apache.isRunning)
            setBadge(tvPhp, "PHP 8.3", LocalServerEngine.status.php.isRunning)
            setBadge(tvMysql, "MySQL", LocalServerEngine.status.mysql.isRunning)

            tvLan.text = LocalServerEngine.getLanServerUrl()
            tvDatabases.text = "Bases de datos: " + LocalServerEngine.getDatabaseList().joinToString(", ")
        }

        // Initialize services if first time opening
        if (!LocalServerEngine.isAnyServiceRunning()) {
            LocalServerEngine.startAllServices(context)
        }
        updateUI()

        btnClose.setOnClickListener { dialog.dismiss() }

        btnMaster.setOnClickListener {
            if (LocalServerEngine.isAnyServiceRunning()) {
                LocalServerEngine.stopAllServices()
                Toast.makeText(context, "Servidores locales detenidos.", Toast.LENGTH_SHORT).show()
            } else {
                LocalServerEngine.startAllServices(context)
                Toast.makeText(context, "🚀 Apache, PHP y MySQL iniciados correctamente.", Toast.LENGTH_SHORT).show()
            }
            updateUI()
        }

        btnCopyLan.setOnClickListener {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = ClipData.newPlainText("LAN Server URL", LocalServerEngine.getLanServerUrl())
            clipboard?.setPrimaryClip(clip)
            Toast.makeText(context, "📋 URL LAN copiada al portapapeles: ${LocalServerEngine.getLanServerUrl()}", Toast.LENGTH_SHORT).show()
        }

        btnShareLan.setOnClickListener {
            LocalServerEngine.shareLanUrl(context)
        }

        btnDeploy.setOnClickListener {
            LocalServerEngine.deploySampleWebProject(context, "mi_sitio_web")
            Toast.makeText(context, "📦 Sitio Web desplegado con éxito en htdocs/ (index.php listo).", Toast.LENGTH_LONG).show()
        }

        btnCreateDb.setOnClickListener {
            val input = EditText(context).apply {
                hint = "Nombre de nueva base de datos"
                setTextColor(context.getColor(R.color.text_primary))
                setHintTextColor(context.getColor(R.color.text_muted))
            }

            AlertDialog.Builder(context, R.style.Theme_MBrowser_Dialog)
                .setTitle("Crear Base de Datos MySQL")
                .setView(input)
                .setPositiveButton("Crear") { _, _ ->
                    val dbName = input.text.toString().trim()
                    if (dbName.isNotEmpty()) {
                        LocalServerEngine.createDatabase(dbName)
                        Toast.makeText(context, "Base de datos '$dbName' creada exitosamente.", Toast.LENGTH_SHORT).show()
                        updateUI()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        btnExecuteSql.setOnClickListener {
            val sqlInput = EditText(context).apply {
                setText("SELECT * FROM usuarios LIMIT 5;")
                setTextColor(context.getColor(R.color.text_primary))
                setHintTextColor(context.getColor(R.color.text_muted))
            }

            AlertDialog.Builder(context, R.style.Theme_MBrowser_Dialog)
                .setTitle("Consola SQL Interactiva")
                .setMessage("Ingresa la consulta a ejecutar en MySQL / PostgreSQL:")
                .setView(sqlInput)
                .setPositiveButton("Ejecutar") { _, _ ->
                    val result = LocalServerEngine.executeSqlQuery(sqlInput.text.toString())
                    AlertDialog.Builder(context, R.style.Theme_MBrowser_Dialog)
                        .setTitle("Resultado de Consulta SQL")
                        .setMessage(result)
                        .setPositiveButton("Aceptar", null)
                        .show()
                }
                .setNegativeButton("Cerrar", null)
                .show()
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
