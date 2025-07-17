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

    private lateinit var allQuestions: Map<String, List<String>>
    private lateinit var allActions: Map<String, List<String>>
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

        allQuestions = mapOf(
            "friends" to AssetReader.readLinesFromAsset(this, "both_questions.txt"),
            "romantic" to AssetReader.readLinesFromAsset(this, "both_questions.txt") +
                    AssetReader.readLinesFromAsset(this, "romantic_questions.txt"),
            "hot" to AssetReader.readLinesFromAsset(this, "hot_questions.txt")
        )

        allActions = mapOf(
            "friends" to AssetReader.readLinesFromAsset(this, "friends_actions.txt") +
                    AssetReader.readLinesFromAsset(this, "both_actions.txt"),
            "romantic" to AssetReader.readLinesFromAsset(this, "romantic_actions.txt") +
                    AssetReader.readLinesFromAsset(this, "both_actions.txt"),
            "hot" to AssetReader.readLinesFromAsset(this, "hot_actions.txt")
        )

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

        val usedItems = mutableMapOf<String, MutableMap<String, Set<String>>>()

        for (player in playerNames) {
            usedItems[player] = mutableMapOf()
            for (mode in allQuestions.keys) {
                val questionsFile = File(pairDir, "$mode/${player}_used_questions_${mode}.txt")
                if (questionsFile.exists()) {
                    usedItems[player]?.put("$mode-questions", questionsFile.readLines().toSet())
                }

                val actionsFile = File(pairDir, "$mode/${player}_used_actions_${mode}.txt")
                if (actionsFile.exists()) {
                    usedItems[player]?.put("$mode-actions", actionsFile.readLines().toSet())
                }
            }
        }

        for (mode in allQuestions.keys) {
            val modeTitle = when (mode) {
                "friends" -> "Режим: Друзья"
                "romantic" -> "Режим: Романтика"
                "hot" -> "Режим: Hot"
                else -> "Режим: $mode"
            }

            val modeHeader = TextView(this).apply {
                text = modeTitle
                textSize = 20f
                setPadding(0, 24, 0, 16)
            }
            historyContainer.addView(modeHeader)

            for (player in playerNames) {
                val playerHeader = TextView(this).apply {
                    text = "Игрок: $player"
                    textSize = 18f
                    setPadding(0, 16, 0, 8)
                }
                historyContainer.addView(playerHeader)

                val questionsHeader = TextView(this).apply {
                    text = "Вопросы:"
                    textSize = 16f
                    setPadding(16, 8, 0, 8)
                }
                historyContainer.addView(questionsHeader)

                for (question in allQuestions[mode] ?: emptyList()) {
                    val isUsed = usedItems[player]?.get("$mode-questions")?.contains(question) == true

                    val checkBoxLayout = LinearLayout(this).apply {
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(32, 4, 16, 4)
                    }

                    val checkBox = CheckBox(this).apply {
                        isChecked = isUsed
                        tag = "$mode|$player|question|$question"
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

                val actions = allActions[mode]
                if (!actions.isNullOrEmpty()) {
                    val actionsHeader = TextView(this).apply {
                        text = "Действия:"
                        textSize = 16f
                        setPadding(16, 8, 0, 8)
                    }
                    historyContainer.addView(actionsHeader)

                    for (action in actions) {
                        val isUsed = usedItems[player]?.get("$mode-actions")?.contains(action) == true

                        val checkBoxLayout = LinearLayout(this).apply {
                            orientation = LinearLayout.HORIZONTAL
                            setPadding(32, 4, 16, 4)
                        }

                        val checkBox = CheckBox(this).apply {
                            isChecked = isUsed
                            tag = "$mode|$player|action|$action"
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
        val changes = mutableMapOf<String, MutableSet<String>>()

        for (i in 0 until historyContainer.childCount) {
            val view = historyContainer.getChildAt(i)
            if (view is LinearLayout && view.childCount > 0) {
                val checkBox = view.getChildAt(0)
                if (checkBox is CheckBox && checkBox.tag is String) {
                    val (mode, player, type, content) = (checkBox.tag as String).split("|", limit = 4)
                    val key = "$mode/$player"

                    if (!changes.containsKey(key)) {
                        changes[key] = mutableSetOf()
                    }

                    if (checkBox.isChecked) {
                        changes[key]?.add("${type}s|$content")
                    }
                }
            }
        }

        for ((key, items) in changes) {
            val (mode, player) = key.split("/")
            val modeDir = File(pairDir, mode)
            if (!modeDir.exists()) modeDir.mkdirs()

            val questions = items.filter { it.startsWith("questions|") }
                .map { it.removePrefix("questions|") }
            val actions = items.filter { it.startsWith("actions|") }
                .map { it.removePrefix("actions|") }

            if (questions.isNotEmpty()) {
                File(modeDir, "${player}_used_questions_${mode}.txt").writeText(
                    questions.joinToString("\n")
                )
            }

            if (actions.isNotEmpty()) {
                File(modeDir, "${player}_used_actions_${mode}.txt").writeText(
                    actions.joinToString("\n")
                )
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