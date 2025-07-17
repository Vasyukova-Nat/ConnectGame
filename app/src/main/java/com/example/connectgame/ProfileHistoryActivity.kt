package com.example.connectgame

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class ProfileHistoryActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_PAIR_NAME = "extra_pair_name"
    }

    private lateinit var editButton: Button
    private lateinit var saveButton: Button
    private lateinit var historyTextView: TextView
    private lateinit var historyContainer: LinearLayout
    private lateinit var pairName: String
    private lateinit var pairDir: File

    private val questionFiles = listOf(
        "both_questions.txt" to "Вопросы: оба (друзья+романтика)",
        "romantic_questions.txt" to "Вопросы: романтика",
        "hot_questions.txt" to "Вопросы: hot"
    )

    private val actionFiles = listOf(
        "both_actions.txt" to "Действия: оба (друзья+романтика)",
        "friends_actions.txt" to "Действия: друзья",
        "romantic_actions.txt" to "Действия: романтика",
        "hot_actions.txt" to "Действия: hot"
    )

    private lateinit var playerNames: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_history)

        pairName = intent.getStringExtra(EXTRA_PAIR_NAME) ?: return
        historyTextView = findViewById(R.id.historyTextView)
        editButton = findViewById(R.id.editButton)
        saveButton = findViewById(R.id.saveButton)
        historyContainer = findViewById(R.id.historyContainer)

        pairDir = File(File(filesDir, "pairs"), pairName)
        playerNames = pairName.split("-").map { it.replace("_", " ") }

        editButton.setOnClickListener { switchToEditMode() }
        saveButton.setOnClickListener { saveChanges() }

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

        val allQuestions = questionFiles.associate { (fileName, _) ->
            fileName to AssetReader.readLinesFromAsset(this, fileName)
        }

        val allActions = actionFiles.associate { (fileName, _) ->
            fileName to AssetReader.readLinesFromAsset(this, fileName)
        }

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

        questionFiles.forEach { (fileName, title) ->
            val questions = allQuestions[fileName] ?: emptyList()
            if (questions.isNotEmpty()) {
                val sectionHeader = TextView(this).apply {
                    text = title
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

                    val source = fileName.removeSuffix("_questions.txt")
                    questions.forEach { question ->
                        val isUsed = usedItems[player]?.get("$source|questions")?.contains(question) == true

                        val checkBoxLayout = LinearLayout(this).apply {
                            orientation = LinearLayout.HORIZONTAL
                            setPadding(32, 4, 16, 4)
                        }

                        val checkBox = CheckBox(this).apply {
                            isChecked = isUsed
                            tag = "$player|$source|questions|$question"
                        }

                        val questionView = TextView(this).apply {
                            text = question
                            textSize = 16f
                            setPadding(16, 0, 0, 0)
                        }

                        checkBoxLayout.addView(checkBox)
                        checkBoxLayout.addView(questionView)
                        historyContainer.addView(checkBoxLayout)
                    }
                }
            }
        }

        actionFiles.forEach { (fileName, title) ->
            val actions = allActions[fileName] ?: emptyList()
            if (actions.isNotEmpty()) {
                val sectionHeader = TextView(this).apply {
                    text = title
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

                    val source = fileName.removeSuffix("_actions.txt")
                    actions.forEach { action ->
                        val isUsed = usedItems[player]?.get("$source|actions")?.contains(action) == true

                        val checkBoxLayout = LinearLayout(this).apply {
                            orientation = LinearLayout.HORIZONTAL
                            setPadding(32, 4, 16, 4)
                        }

                        val checkBox = CheckBox(this).apply {
                            isChecked = isUsed
                            tag = "$player|$source|actions|$action"
                        }

                        val actionView = TextView(this).apply {
                            text = action
                            textSize = 16f
                            setPadding(16, 0, 0, 0)
                        }

                        checkBoxLayout.addView(checkBox)
                        checkBoxLayout.addView(actionView)
                        historyContainer.addView(checkBoxLayout)
                    }
                }
            }
        }
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
}