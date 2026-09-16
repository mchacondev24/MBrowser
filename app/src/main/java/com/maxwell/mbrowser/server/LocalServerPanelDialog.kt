package com.maxwell.mbrowser.server

import android.app.Activity
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.maxwell.mbrowser.R
import java.io.File

/**
 * LocalServerPanelDialog - Fullscreen Controller for ApacheMysqlAndroid_Server inside MBrowser.
 * Features:
 * - Full Apache 2.4, PHP 8.3, MySQL 8.0 & PostgreSQL stack controls.
 * - Complete Web Root File Manager for htdocs/ (Upload, Create, Edit, Rename, Delete).
 * - Fullscreen Code Editor with PHP / HTML / JS / SQL snippets.
 * - Interactive SQL Query Console & Database Manager.
 * - LAN Sharing and Live Server Logs.
 */
object LocalServerPanelDialog {

    private var onFileImportRequested: ((callback: (Uri) -> Unit) -> Unit)? = null

    fun setFileImportHandler(handler: (callback: (Uri) -> Unit) -> Unit) {
        onFileImportRequested = handler
    }

    fun show(context: Context, onOpenUrlInBrowser: (url: String) -> Unit) {
        val dialog = Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_local_server, null)
        dialog.setContentView(view)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        val btnBackToBrowsing = view.findViewById<Button>(R.id.btnBackToBrowsingFromServer)
        val btnClose = view.findViewById<ImageButton>(R.id.btnCloseServer)
        val tvMaster = view.findViewById<TextView>(R.id.tvServerMasterStatus)
        val btnMaster = view.findViewById<Button>(R.id.btnToggleAllServices)
        val tvLan = view.findViewById<TextView>(R.id.tvLanAddress)
        val btnCopyLan = view.findViewById<Button>(R.id.btnCopyLanUrl)
        val btnShareLan = view.findViewById<Button>(R.id.btnShareLanUrl)
        val btnOpenLocalhost = view.findViewById<Button>(R.id.btnOpenLocalhostInBrowser)

        // File manager views
        val btnUploadFile = view.findViewById<Button>(R.id.btnUploadFileToHtdocs)
        val btnNewFile = view.findViewById<Button>(R.id.btnNewFileInHtdocs)
        val btnNewFolder = view.findViewById<Button>(R.id.btnNewFolderInHtdocs)
        val btnDeploy = view.findViewById<Button>(R.id.btnDeployWebsite)
        val btnRefreshFiles = view.findViewById<Button>(R.id.btnRefreshFiles)
        val layoutFileList = view.findViewById<LinearLayout>(R.id.layoutHtdocsFileList)
        val tvEmptyHtdocs = view.findViewById<TextView>(R.id.tvEmptyHtdocs)

        // Database & SQL views
        val tvDatabases = view.findViewById<TextView>(R.id.tvActiveDatabase)
        val btnCreateDb = view.findViewById<Button>(R.id.btnCreateDb)
        val btnExecuteSql = view.findViewById<Button>(R.id.btnExecuteSql)

        // Individual service status
        val tvApache = view.findViewById<TextView>(R.id.tvStatusApache)
        val tvPhp = view.findViewById<TextView>(R.id.tvStatusPhp)
        val tvMysql = view.findViewById<TextView>(R.id.tvStatusMysql)
        val tvLogs = view.findViewById<TextView>(R.id.tvServerLogs)

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
            tvDatabases.text = "Bases de datos activas: " + LocalServerEngine.getDatabaseList().joinToString(", ")
            tvLogs.text = LocalServerEngine.getLogs().takeLast(10).joinToString("\n")
        }

        fun refreshHtdocsFiles() {
            layoutFileList.removeAllViews()
            val files = LocalServerEngine.listHtdocsFiles(context)

            if (files.isEmpty()) {
                layoutFileList.addView(tvEmptyHtdocs)
            } else {
                for (file in files) {
                    val item = LayoutInflater.from(context).inflate(R.layout.item_server_file, layoutFileList, false)
                    val tvBadge = item.findViewById<TextView>(R.id.tvFileBadge)
                    val tvName = item.findViewById<TextView>(R.id.tvFileName)
                    val tvMeta = item.findViewById<TextView>(R.id.tvFileMeta)
                    val btnOpenInBrowser = item.findViewById<ImageButton>(R.id.btnOpenFileInBrowser)
                    val btnEditCode = item.findViewById<ImageButton>(R.id.btnEditFileCode)
                    val btnMore = item.findViewById<ImageButton>(R.id.btnFileMoreOptions)

                    tvName.text = file.name

                    if (file.isDirectory) {
                        tvBadge.text = "DIR"
                        tvBadge.setBackgroundResource(R.drawable.bg_aqua_pill)
                        val childCount = file.listFiles()?.size ?: 0
                        tvMeta.text = "Carpeta • $childCount elemento(s)"
                        btnOpenInBrowser.visibility = android.view.View.GONE
                        btnEditCode.visibility = android.view.View.GONE
                    } else {
                        val ext = file.extension.uppercase()
                        tvBadge.text = if (ext.length > 4) ext.take(3) else ext.ifEmpty { "TXT" }
                        when (ext) {
                            "PHP" -> tvBadge.setBackgroundResource(R.drawable.bg_dev_badge)
                            "HTML", "HTM" -> tvBadge.setBackgroundResource(R.drawable.bg_turbo_badge)
                            "JS", "TS" -> tvBadge.setBackgroundResource(R.drawable.bg_game_badge)
                            "CSS" -> tvBadge.setBackgroundResource(R.drawable.bg_aqua_pill)
                            "SQL" -> tvBadge.setBackgroundResource(R.drawable.bg_aqua_button)
                            else -> tvBadge.setBackgroundResource(R.drawable.bg_glass_card)
                        }
                        tvMeta.text = "${LocalServerEngine.formatFileSize(file.length())} • ${ext}"
                    }

                    // Open in Code Editor on tap
                    item.setOnClickListener {
                        if (!file.isDirectory) {
                            showCodeEditor(context, file) {
                                refreshHtdocsFiles()
                                updateUI()
                            }
                        }
                    }

                    btnEditCode.setOnClickListener {
                        showCodeEditor(context, file) {
                            refreshHtdocsFiles()
                            updateUI()
                        }
                    }

                    btnOpenInBrowser.setOnClickListener {
                        if (!LocalServerEngine.status.apache.isRunning) {
                            LocalServerEngine.startAllServices(context)
                        }
                        dialog.dismiss()
                        val url = "${LocalServerEngine.getLocalServerUrl()}/${file.name}"
                        onOpenUrlInBrowser(url)
                    }

                    btnMore.setOnClickListener {
                        val options = if (file.isDirectory) {
                            arrayOf("🏷️ Renombrar Carpeta", "🗑️ Eliminar Carpeta")
                        } else {
                            arrayOf("✏️ Editar Código", "🌐 Abrir en Navegador", "🏷️ Renombrar Archivo", "🗑️ Eliminar Archivo")
                        }

                        AlertDialog.Builder(context, R.style.Theme_MBrowser_Dialog)
                            .setTitle(file.name)
                            .setItems(options) { _, which ->
                                if (file.isDirectory) {
                                    when (which) {
                                        0 -> promptRenameFile(context, file) { refreshHtdocsFiles() }
                                        1 -> promptDeleteFile(context, file) { refreshHtdocsFiles() }
                                    }
                                } else {
                                    when (which) {
                                        0 -> showCodeEditor(context, file) { refreshHtdocsFiles(); updateUI() }
                                        1 -> {
                                            if (!LocalServerEngine.status.apache.isRunning) {
                                                LocalServerEngine.startAllServices(context)
                                            }
                                            dialog.dismiss()
                                            onOpenUrlInBrowser("${LocalServerEngine.getLocalServerUrl()}/${file.name}")
                                        }
                                        2 -> promptRenameFile(context, file) { refreshHtdocsFiles() }
                                        3 -> promptDeleteFile(context, file) { refreshHtdocsFiles() }
                                    }
                                }
                            }
                            .show()
                    }

                    layoutFileList.addView(item)
                }
            }
        }

        // Initialize services if first time opening
        if (!LocalServerEngine.isAnyServiceRunning()) {
            LocalServerEngine.startAllServices(context)
        }
        updateUI()
        refreshHtdocsFiles()

        btnBackToBrowsing.setOnClickListener { dialog.dismiss() }
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
            Toast.makeText(context, "📋 URL LAN copiada: ${LocalServerEngine.getLanServerUrl()}", Toast.LENGTH_SHORT).show()
        }

        btnShareLan.setOnClickListener {
            LocalServerEngine.shareLanUrl(context)
        }

        btnRefreshFiles.setOnClickListener {
            refreshHtdocsFiles()
            updateUI()
            Toast.makeText(context, "Lista de archivos actualizada.", Toast.LENGTH_SHORT).show()
        }

        // Upload / Import File
        btnUploadFile.setOnClickListener {
            val handler = onFileImportRequested
            if (handler != null) {
                handler { uri ->
                    val success = LocalServerEngine.importFileFromUri(context, uri)
                    if (success) {
                        Toast.makeText(context, "✅ Archivo subido exitosamente a htdocs/", Toast.LENGTH_SHORT).show()
                        refreshHtdocsFiles()
                        updateUI()
                    } else {
                        Toast.makeText(context, "❌ Error al importar archivo", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // Fallback direct creation
                promptNewFile(context) { refreshHtdocsFiles(); updateUI() }
            }
        }

        // New File in htdocs
        btnNewFile.setOnClickListener {
            promptNewFile(context) { file ->
                refreshHtdocsFiles()
                updateUI()
                showCodeEditor(context, file) {
                    refreshHtdocsFiles()
                    updateUI()
                }
            }
        }

        // New Folder in htdocs
        btnNewFolder.setOnClickListener {
            val input = EditText(context).apply {
                hint = "Nombre de carpeta (ej. api, assets)"
                setTextColor(context.getColor(R.color.text_primary))
                setHintTextColor(context.getColor(R.color.text_muted))
            }
            AlertDialog.Builder(context, R.style.Theme_MBrowser_Dialog)
                .setTitle("Nueva Carpeta en htdocs/")
                .setView(input)
                .setPositiveButton("Crear") { _, _ ->
                    val folderName = input.text.toString().trim()
                    if (folderName.isNotEmpty()) {
                        val created = LocalServerEngine.createHtdocsFolder(context, folderName)
                        if (created) {
                            Toast.makeText(context, "Carpeta '$folderName' creada.", Toast.LENGTH_SHORT).show()
                            refreshHtdocsFiles()
                        } else {
                            Toast.makeText(context, "La carpeta ya existe o nombre inválido.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        btnDeploy.setOnClickListener {
            LocalServerEngine.deploySampleWebProject(context, "mi_sitio_web")
            refreshHtdocsFiles()
            updateUI()
            Toast.makeText(context, "📦 Archivos demo restaurados en htdocs/ (index.php, api.php, style.css).", Toast.LENGTH_LONG).show()
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
            showSqlConsole(context) { updateUI() }
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

    private fun promptNewFile(context: Context, onCreated: (File) -> Unit) {
        val input = EditText(context).apply {
            hint = "nombre_archivo.php"
            setText("script_${System.currentTimeMillis() % 1000}.php")
            setTextColor(context.getColor(R.color.text_primary))
            setHintTextColor(context.getColor(R.color.text_muted))
        }

        AlertDialog.Builder(context, R.style.Theme_MBrowser_Dialog)
            .setTitle("Nuevo Archivo en htdocs/")
            .setView(input)
            .setPositiveButton("Crear") { _, _ ->
                val fileName = input.text.toString().trim()
                if (fileName.isNotEmpty()) {
                    val defaultContent = when {
                        fileName.endsWith(".php") -> "<?php\n// MBrowser Apache & PHP 8.3\necho '<h1>¡Hola desde $fileName!</h1>';\n"
                        fileName.endsWith(".html") -> "<!DOCTYPE html>\n<html>\n<head><title>$fileName</title></head>\n<body><h1>Página $fileName</h1></body>\n</html>"
                        fileName.endsWith(".js") -> "// JavaScript Script\nconsole.log('Script $fileName cargado.');\n"
                        else -> "// Archivo creado en MBrowser htdocs\n"
                    }
                    val created = LocalServerEngine.createHtdocsFile(context, fileName, defaultContent)
                    if (created) {
                        Toast.makeText(context, "Archivo '$fileName' creado.", Toast.LENGTH_SHORT).show()
                        val file = File(LocalServerEngine.getWebRootDir(context), fileName)
                        onCreated(file)
                    } else {
                        Toast.makeText(context, "El archivo ya existe o nombre inválido.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun promptRenameFile(context: Context, file: File, onRenamed: () -> Unit) {
        val input = EditText(context).apply {
            setText(file.name)
            setTextColor(context.getColor(R.color.text_primary))
        }

        AlertDialog.Builder(context, R.style.Theme_MBrowser_Dialog)
            .setTitle("Renombrar ${file.name}")
            .setView(input)
            .setPositiveButton("Guardar") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty() && newName != file.name) {
                    val renamed = LocalServerEngine.renameHtdocsFile(file, newName)
                    if (renamed) {
                        Toast.makeText(context, "Renombrado a '$newName'.", Toast.LENGTH_SHORT).show()
                        onRenamed()
                    } else {
                        Toast.makeText(context, "Error al renombrar o nombre duplicado.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun promptDeleteFile(context: Context, file: File, onDeleted: () -> Unit) {
        AlertDialog.Builder(context, R.style.Theme_MBrowser_Dialog)
            .setTitle("Eliminar Archivo")
            .setMessage("¿Estás seguro de eliminar permanentemente '${file.name}' del servidor Apache?")
            .setPositiveButton("Eliminar") { _, _ ->
                val deleted = LocalServerEngine.deleteHtdocsFile(file)
                if (deleted) {
                    Toast.makeText(context, "'${file.name}' eliminado.", Toast.LENGTH_SHORT).show()
                    onDeleted()
                } else {
                    Toast.makeText(context, "Error al eliminar archivo.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    fun showCodeEditor(context: Context, file: File, onSaved: () -> Unit) {
        val dialog = Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_server_code_editor, null)
        dialog.setContentView(view)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        val btnBack = view.findViewById<Button>(R.id.btnBackToFilesFromEditor)
        val tvFileName = view.findViewById<TextView>(R.id.tvEditorFileName)
        val btnSave = view.findViewById<Button>(R.id.btnSaveCodeFile)
        val btnClose = view.findViewById<ImageButton>(R.id.btnCloseCodeEditor)
        val etContent = view.findViewById<EditText>(R.id.etCodeContent)
        val tvCharCount = view.findViewById<TextView>(R.id.tvCodeCharCount)

        // Snippet buttons
        val btnPhp = view.findViewById<Button>(R.id.btnSnippetPhp)
        val btnEcho = view.findViewById<Button>(R.id.btnSnippetEcho)
        val btnHtml = view.findViewById<Button>(R.id.btnSnippetHtml5)
        val btnMysql = view.findViewById<Button>(R.id.btnSnippetMysql)
        val btnJson = view.findViewById<Button>(R.id.btnSnippetJson)

        tvFileName.text = file.name
        val initialContent = LocalServerEngine.readHtdocsFile(file)
        etContent.setText(initialContent)

        fun updateCharCount() {
            val text = etContent.text.toString()
            val lines = if (text.isEmpty()) 0 else text.lines().size
            val chars = text.length
            tvCharCount.text = "Líneas: $lines • Caracteres: $chars • ${LocalServerEngine.formatFileSize(chars.toLong())}"
        }

        updateCharCount()

        etContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateCharCount()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        fun insertSnippet(snippet: String) {
            val start = etContent.selectionStart.coerceAtLeast(0)
            val end = etContent.selectionEnd.coerceAtLeast(0)
            etContent.text.replace(start.coerceAtMost(end), start.coerceAtLeast(end), snippet, 0, snippet.length)
        }

        btnPhp.setOnClickListener { insertSnippet("<?php\n\n?>") }
        btnEcho.setOnClickListener { insertSnippet("echo \"<p>Resultado: \" . date('Y-m-d H:i:s') . \"</p>\";\n") }
        btnHtml.setOnClickListener {
            insertSnippet(
                """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="utf-8">
                    <title>Mi Sitio Web</title>
                </head>
                <body>
                    <h1>¡Bienvenido a MBrowser Local Server!</h1>
                </body>
                </html>
                """.trimIndent()
            )
        }
        btnMysql.setOnClickListener {
            insertSnippet(
                """
                ${'$'}mysqli = new mysqli("localhost", "root", "", "app_default_db");
                if (${'$'}mysqli->connect_error) {
                    die("Error de conexión: " . ${'$'}mysqli->connect_error);
                }
                echo "Conexión a MySQL exitosa.";
                """.trimIndent()
            )
        }
        btnJson.setOnClickListener {
            insertSnippet("header('Content-Type: application/json');\necho json_encode(['status' => 'ok', 'time' => time()]);\n")
        }

        fun saveFile() {
            val content = etContent.text.toString()
            LocalServerEngine.saveHtdocsFile(file, content)
            onSaved()
        }

        btnSave.setOnClickListener {
            saveFile()
            Toast.makeText(context, "💾 Archivo guardado correctamente.", Toast.LENGTH_SHORT).show()
        }

        btnBack.setOnClickListener {
            saveFile()
            dialog.dismiss()
        }

        btnClose.setOnClickListener {
            saveFile()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showSqlConsole(context: Context, onExecuted: () -> Unit) {
        val input = EditText(context).apply {
            setText("SELECT * FROM usuarios LIMIT 5;")
            setTextColor(context.getColor(R.color.text_primary))
            setHintTextColor(context.getColor(R.color.text_muted))
            textSize = 13f
            typeface = android.graphics.Typeface.MONOSPACE
        }

        AlertDialog.Builder(context, R.style.Theme_MBrowser_Dialog)
            .setTitle("Consola SQL Interactiva")
            .setMessage("Ejecuta consultas en Apache / MySQL / PostgreSQL:")
            .setView(input)
            .setPositiveButton("Ejecutar") { _, _ ->
                val query = input.text.toString()
                val result = LocalServerEngine.executeSqlQuery(query)
                onExecuted()

                AlertDialog.Builder(context, R.style.Theme_MBrowser_Dialog)
                    .setTitle("Resultado de Consulta SQL")
                    .setMessage(result)
                    .setPositiveButton("Aceptar", null)
                    .show()
            }
            .setNegativeButton("Cerrar", null)
            .show()
    }
}
