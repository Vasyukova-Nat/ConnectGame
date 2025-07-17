package com.example.connectgame

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class ProfileHistoryActivity : AppCompatActivity() {
    companion object {
        const val EXPORT_REQUEST_CODE = 101
        const val IMPORT_REQUEST_CODE = 102
        const val EXTRA_PAIR_NAME = "extra_pair_name"
    }

    private lateinit var editButton: Button
    private lateinit var saveButton: Button
    private lateinit var exportButton: Button
    private lateinit var importButton: Button
    private lateinit var historyTextView: TextView
    private lateinit var historyContainer: LinearLayout
    private lateinit var pairName: String
    private lateinit var pairDir: File
    private lateinit var playerNames: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_history)

        pairName = intent.getStringExtra(EXTRA_PAIR_NAME) ?: return
        historyTextView = findViewById(R.id.historyTextView)
        editButton = findViewById(R.id.editButton)
        saveButton = findViewById(R.id.saveButton)
        exportButton = findViewById(R.id.exportButton)
        importButton = findViewById(R.id.importButton)
        historyContainer = findViewById(R.id.historyContainer)

        pairDir = File(File(filesDir, "pairs"), pairName)
        playerNames = pairName.split("-").map { it.replace("_", " ") }

        editButton.setOnClickListener { switchToEditMode() }
        saveButton.setOnClickListener { saveChanges() }
        exportButton.setOnClickListener { exportHistory() }
        importButton.setOnClickListener { importHistory() }

        loadPairHistory()
    }

    private fun loadPairHistory() {
        val history = StringBuilder("История пары $pairName:\n\n")

        pairDir.walk().filter { it.isFile }.forEach { file ->
            history.append("${file.name}:\n")
            history.append(file.readText())
            history.append("\n\n")
        }

        historyTextView.text = if (history.isNotEmpty()) history.toString() else "Нет данных"
    }

    private fun switchToEditMode() {
        historyTextView.visibility = View.GONE
        editButton.visibility = View.GONE
        saveButton.visibility = View.VISIBLE
        historyContainer.removeAllViews()

        val allQuestionsMap = mapOf(
            "both_questions.txt" to AssetReader.readLinesFromAsset(this, "both_questions.txt"),
            "romantic_questions.txt" to AssetReader.readLinesFromAsset(this, "romantic_questions.txt"),
            "hot_questions.txt" to AssetReader.readLinesFromAsset(this, "hot_questions.txt")
        )

        val allActionsMap = mapOf(
            "friends_actions.txt" to AssetReader.readLinesFromAsset(this, "friends_actions.txt"),
            "both_actions.txt" to AssetReader.readLinesFromAsset(this, "both_actions.txt"),
            "romantic_actions.txt" to AssetReader.readLinesFromAsset(this, "romantic_actions.txt"),
            "hot_actions.txt" to AssetReader.readLinesFromAsset(this, "hot_actions.txt")
        )

        val usedItems = mutableMapOf<String, MutableMap<String, Set<String>>>()

        for (player in playerNames) {
            usedItems[player] = mutableMapOf()

            pairDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("${player}_used_") && file.isFile) {
                    val parts = file.name.removePrefix("${player}_used_").removeSuffix(".txt").split("_")
                    if (parts.size == 2) {
                        val (source, type) = parts
                        usedItems[player]?.put("$source|$type", file.readLines().toSet())
                    }
                }
            }
        }

        fun showItems(title: String, fileName: String, type: String) {
            val items = if (type == "questions") allQuestionsMap[fileName] else allActionsMap[fileName]
            if (!items.isNullOrEmpty()) {
                val sectionHeader = TextView(this).apply {
                    text = when {
                        fileName == "friends_actions.txt" -> "Действия: друзья"
                        fileName == "both_questions.txt" -> "Вопросы: оба (друзья+романтика)"
                        fileName == "both_actions.txt" -> "Действия: оба (друзья+романтика)"
                        fileName == "romantic_questions.txt" -> "Вопросы: романтика"
                        fileName == "romantic_actions.txt" -> "Действия: романтика"
                        fileName == "hot_questions.txt" -> "Вопросы: hot"
                        fileName == "hot_actions.txt" -> "Действия: hot"
                        else -> title
                    }
                    textSize = 18f
                    setPadding(0, 24, 0, 8)
                }
                historyContainer.addView(sectionHeader)

                playerNames.forEach { player ->
                    val playerHeader = TextView(this).apply {
                        text = "Игрок: $player"
                        textSize = 16f
                        setPadding(16, 8, 0, 8)
                    }
                    historyContainer.addView(playerHeader)

                    val source = fileName.removeSuffix("_${type}.txt")
                    items.forEach { item ->
                        val isUsed = usedItems[player]?.get("$source|$type")?.contains(item) == true

                        val checkBoxLayout = LinearLayout(this).apply {
                            orientation = LinearLayout.HORIZONTAL
                            setPadding(32, 4, 16, 4)
                        }

                        val checkBox = CheckBox(this).apply {
                            isChecked = isUsed
                            tag = "$player|$source|$type|$item"
                        }

                        val itemView = TextView(this).apply {
                            text = item
                            textSize = 16f
                            setPadding(16, 0, 0, 0)
                        }

                        checkBoxLayout.addView(checkBox)
                        checkBoxLayout.addView(itemView)
                        historyContainer.addView(checkBoxLayout)
                    }
                }
            }
        }

        showItems("", "friends_actions.txt", "actions")
        showItems("", "both_questions.txt", "questions")
        showItems("", "both_actions.txt", "actions")
        showItems("", "romantic_questions.txt", "questions")
        showItems("", "romantic_actions.txt", "actions")
        showItems("", "hot_questions.txt", "questions")
        showItems("", "hot_actions.txt", "actions")
    }

    private fun saveChanges() {
        pairDir.listFiles()?.forEach { file ->
            if (file.name.startsWith("${playerNames[0]}_used_") ||
                file.name.startsWith("${playerNames[1]}_used_")) {
                file.delete()
            }
        }

        val changes = mutableMapOf<String, MutableMap<String, MutableSet<String>>>()

        for (i in 0 until historyContainer.childCount) {
            val view = historyContainer.getChildAt(i)
            if (view is LinearLayout && view.childCount > 0) {
                val checkBox = view.getChildAt(0)
                if (checkBox is CheckBox && checkBox.tag is String) {
                    val (player, source, type, content) = (checkBox.tag as String).split("|", limit = 4)

                    if (!changes.containsKey(player)) {
                        changes[player] = mutableMapOf()
                    }

                    val fileKey = "${source}_$type"
                    if (!changes[player]!!.containsKey(fileKey)) {
                        changes[player]!![fileKey] = mutableSetOf()
                    }

                    if (checkBox.isChecked) {
                        changes[player]!![fileKey]!!.add(content)
                    }
                }
            }
        }

        for ((player, types) in changes) {
            for ((fileKey, items) in types) {
                if (items.isNotEmpty()) {
                    File(pairDir, "${player}_used_$fileKey.txt").writeText(items.joinToString("\n"))
                }
            }
        }

        saveButton.visibility = View.GONE
        editButton.visibility = View.VISIBLE
        historyTextView.visibility = View.VISIBLE
        historyContainer.removeAllViews()
        historyContainer.addView(historyTextView)
        loadPairHistory()

        Toast.makeText(this, "Изменения сохранены", Toast.LENGTH_SHORT).show()
    }

    private fun exportHistory() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/zip"
            putExtra(Intent.EXTRA_TITLE, "connect_game_${pairName}_backup.zip")
        }
        startActivityForResult(intent, EXPORT_REQUEST_CODE)
    }

    private fun performExport(uri: Uri) {
        try {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                ZipOutputStream(BufferedOutputStream(outputStream)).use { zip ->
                    pairDir.listFiles()?.forEach { file ->
                        if (file.isFile) {
                            zip.putNextEntry(ZipEntry(file.name))
                            file.inputStream().use { it.copyTo(zip) }
                            zip.closeEntry()
                        }
                    }
                }
            }
            Toast.makeText(this, "Экспорт завершен", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка экспорта: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun importHistory() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/zip"
        }
        startActivityForResult(intent, IMPORT_REQUEST_CODE)
    }

    private fun performImport(uri: Uri) {
        try {
            val tempDir = File(cacheDir, "temp_import")
            tempDir.deleteRecursively()
            tempDir.mkdirs()

            contentResolver.openInputStream(uri)?.use { inputStream ->
                ZipInputStream(BufferedInputStream(inputStream)).use { zip ->
                    var entry: ZipEntry?
                    while (zip.nextEntry.also { entry = it } != null) {
                        val file = File(tempDir, entry!!.name)
                        file.outputStream().use { zip.copyTo(it) }
                    }
                }
            }

            tempDir.listFiles()?.forEach { file ->
                file.copyTo(File(pairDir, file.name), overwrite = true)
            }

            Toast.makeText(this, "Импорт завершен", Toast.LENGTH_SHORT).show()
            loadPairHistory()
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка импорта: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            EXPORT_REQUEST_CODE -> {
                if (resultCode == RESULT_OK) {
                    data?.data?.let { uri ->
                        performExport(uri)
                    }
                }
            }
            IMPORT_REQUEST_CODE -> {
                if (resultCode == RESULT_OK) {
                    data?.data?.let { uri ->
                        AlertDialog.Builder(this)
                            .setTitle("Подтверждение")
                            .setMessage("Заменить существующую историю? Текущие данные не сохранятся.")
                            .setPositiveButton("Да") { _, _ -> performImport(uri) }
                            .setNegativeButton("Отмена", null)
                            .show()
                    }
                }
            }
        }
    }
}