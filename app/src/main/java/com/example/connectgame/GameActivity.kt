package com.example.connectgame

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import java.io.File

class GameActivity : AppCompatActivity() {
    private lateinit var contentTextView: TextView
    private lateinit var truthButton: Button
    private lateinit var actionButton: Button
    private lateinit var randomChoiceButton: Button
    private lateinit var nextButton: Button
    private lateinit var mode: String
    private lateinit var playerNames: List<String>
    private lateinit var currentPairFolder: File

    private lateinit var bothQuestions: List<String>
    private lateinit var friendsActions: List<String>
    private lateinit var romanticQuestions: List<String>
    private lateinit var romanticActions: List<String>
    private lateinit var bothActions: List<String>
    private lateinit var hotQuestions: List<String>
    private lateinit var hotActions: List<String>

    private lateinit var allQuestions: List<String>
    private lateinit var allActions: List<String>

    private var currentPlayer = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        mode = intent.getStringExtra("mode") ?: "friends"
        val pairName = intent.getStringExtra("pairName") ?: return

        if (mode == "hot") {
            setTheme(R.style.Theme_ConnectGame_Hot)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        contentTextView = findViewById(R.id.contentTextView)
        truthButton = findViewById(R.id.truthButton)
        actionButton = findViewById(R.id.actionButton)
        randomChoiceButton = findViewById(R.id.randomChoiceButton)
        nextButton = findViewById(R.id.nextButton)

        playerNames = pairName.split("-").map { it.replace("_", " ") }

        bothQuestions = AssetReader.readLinesFromAsset(this, "both_questions.txt")
        friendsActions = AssetReader.readLinesFromAsset(this, "friends_actions.txt")
        romanticQuestions = AssetReader.readLinesFromAsset(this, "romantic_questions.txt")
        romanticActions = AssetReader.readLinesFromAsset(this, "romantic_actions.txt")
        bothActions = AssetReader.readLinesFromAsset(this, "both_actions.txt")
        hotQuestions = AssetReader.readLinesFromAsset(this, "hot_questions.txt")
        hotActions = AssetReader.readLinesFromAsset(this, "hot_actions.txt")

        allQuestions = when (mode) {
            "friends" -> bothQuestions
            "romantic" -> bothQuestions + romanticQuestions
            else -> hotQuestions
        }

        allActions = when (mode) {
            "friends" -> friendsActions + bothActions
            "romantic" -> romanticActions + bothActions
            else -> hotActions
        }

        if (mode == "hot") {
            applyHotThemeColors()
        }

        setupPairStorage()
        initUI()
    }

    private fun setupPairStorage() {
        val pairName = intent.getStringExtra("pairName") ?: return
        val pairsFolder = File(filesDir, "pairs")
        if (!pairsFolder.exists()) pairsFolder.mkdirs()

        currentPairFolder = File(pairsFolder, pairName)
        if (!currentPairFolder.exists()) currentPairFolder.mkdirs()
    }

    private fun applyHotThemeColors() {
        if (!::contentTextView.isInitialized || !::truthButton.isInitialized ||
            !::actionButton.isInitialized || !::randomChoiceButton.isInitialized) {
            return
        }

        val rootView = findViewById<LinearLayout>(R.id.rootLayout)
        val hotBackground = ContextCompat.getColor(this, R.color.background_dark)
        val hotButton = ContextCompat.getColor(this, R.color.button_dark)
        val hotText = ContextCompat.getColor(this, R.color.text_dark)

        rootView.setBackgroundColor(hotBackground)
        contentTextView.setTextColor(hotText)
        truthButton.setBackgroundColor(hotButton)
        truthButton.setTextColor(hotText)
        actionButton.setBackgroundColor(hotButton)
        actionButton.setTextColor(hotText)
        randomChoiceButton.setBackgroundColor(hotButton)
        randomChoiceButton.setTextColor(hotText)
    }

    private fun getUsedQuestionsFile(playerName: String, source: String): File {
        return File(currentPairFolder, "${playerName}_used_${source}_questions.txt")
    }

    private fun getUsedActionsFile(playerName: String, source: String): File {
        return File(currentPairFolder, "${playerName}_used_${source}_actions.txt")
    }

    private fun loadUsedQuestions(playerName: String): Set<String> {
        val usedQuestions = mutableSetOf<String>()

        if (mode == "romantic") {
            File(currentPairFolder, "${playerName}_used_both_questions.txt")
                .takeIf { it.exists() }?.readLines()?.let { usedQuestions.addAll(it) }
            File(currentPairFolder, "${playerName}_used_romantic_questions.txt")
                .takeIf { it.exists() }?.readLines()?.let { usedQuestions.addAll(it) }
        }
        else if (mode == "friends") {
            File(currentPairFolder, "${playerName}_used_both_questions.txt")
                .takeIf { it.exists() }?.readLines()?.let { usedQuestions.addAll(it) }
            File(currentPairFolder, "${playerName}_used_friends_questions.txt")
                .takeIf { it.exists() }?.readLines()?.let { usedQuestions.addAll(it) }
        }
        else if (mode == "hot") {
            File(currentPairFolder, "${playerName}_used_hot_questions.txt")
                .takeIf { it.exists() }?.readLines()?.let { usedQuestions.addAll(it) }
        }

        return usedQuestions
    }

    private fun loadUsedActions(playerName: String): Set<String> {
        val usedActions = mutableSetOf<String>()

        if (mode == "romantic") {
            File(currentPairFolder, "${playerName}_used_both_actions.txt")
                .takeIf { it.exists() }?.readLines()?.let { usedActions.addAll(it) }
            File(currentPairFolder, "${playerName}_used_romantic_actions.txt")
                .takeIf { it.exists() }?.readLines()?.let { usedActions.addAll(it) }
        }
        else if (mode == "friends") {
            File(currentPairFolder, "${playerName}_used_both_actions.txt")
                .takeIf { it.exists() }?.readLines()?.let { usedActions.addAll(it) }
            File(currentPairFolder, "${playerName}_used_friends_actions.txt")
                .takeIf { it.exists() }?.readLines()?.let { usedActions.addAll(it) }
        }
        else if (mode == "hot") {
            File(currentPairFolder, "${playerName}_used_hot_actions.txt")
                .takeIf { it.exists() }?.readLines()?.let { usedActions.addAll(it) }
        }

        return usedActions
    }

    private fun saveUsedQuestion(playerName: String, question: String) {
        val source = when {
            bothQuestions.contains(question) -> "both"
            romanticQuestions.contains(question) -> "romantic"
            hotQuestions.contains(question) -> "hot"
            else -> return
        }
        getUsedQuestionsFile(playerName, source).appendText("$question\n")
    }

    private fun saveUsedAction(playerName: String, action: String) {
        val source = when {
            bothActions.contains(action) -> "both"
            friendsActions.contains(action) -> "friends"
            romanticActions.contains(action) -> "romantic"
            hotActions.contains(action) -> "hot"
            else -> return
        }
        getUsedActionsFile(playerName, source).appendText("$action\n")
    }

    private fun initUI() {
        setupUI()
    }

    private fun setupUI() {
        truthButton.visibility = View.VISIBLE
        actionButton.visibility = View.VISIBLE
        randomChoiceButton.visibility = View.VISIBLE
        nextButton.visibility = View.GONE

        contentTextView.text = "Ход игрока ${playerNames[currentPlayer-1]}. Выберите:"

        truthButton.setOnClickListener {
            showRandomQuestion()
            truthButton.visibility = View.GONE
            actionButton.visibility = View.GONE
            randomChoiceButton.visibility = View.GONE
        }

        actionButton.setOnClickListener {
            showRandomAction()
            truthButton.visibility = View.GONE
            actionButton.visibility = View.GONE
            randomChoiceButton.visibility = View.GONE
        }

        randomChoiceButton.setOnClickListener {
            if ((0..1).random() == 0) showRandomQuestion() else showRandomAction()
            truthButton.visibility = View.GONE
            actionButton.visibility = View.GONE
            randomChoiceButton.visibility = View.GONE
        }
    }

    private fun showRandomQuestion() {
        val currentPlayerName = playerNames[currentPlayer-1]
        val usedQuestions = loadUsedQuestions(currentPlayerName)
        val availableQuestions = allQuestions.filter { it !in usedQuestions }

        if (availableQuestions.isEmpty()) {
            contentTextView.text = "$currentPlayerName, вопросы закончились!"
            nextButton.visibility = View.VISIBLE
            return
        }

        val randomQuestion = availableQuestions.random()
        contentTextView.text = randomQuestion
        saveUsedQuestion(currentPlayerName, randomQuestion)
        showNextButton()
    }

    private fun showRandomAction() {
        val currentPlayerName = playerNames[currentPlayer-1]
        val usedActions = loadUsedActions(currentPlayerName)
        val availableActions = allActions.filter { it !in usedActions }

        if (availableActions.isEmpty()) {
            contentTextView.text = "$currentPlayerName, действия закончились!"
            nextButton.visibility = View.VISIBLE
            return
        }

        val randomAction = availableActions.random()
        contentTextView.text = randomAction
        saveUsedAction(currentPlayerName, randomAction)
        showNextButton()
    }

    private fun showNextButton() {
        nextButton.visibility = View.VISIBLE
        nextButton.setOnClickListener {
            switchToNextPlayer()
        }
    }

    private fun switchToNextPlayer() {
        currentPlayer = if (currentPlayer == 1) 2 else 1
        contentTextView.text = "Ход игрока ${playerNames[currentPlayer-1]}. Выберите:"
        truthButton.visibility = View.VISIBLE
        actionButton.visibility = View.VISIBLE
        randomChoiceButton.visibility = View.VISIBLE
        nextButton.visibility = View.GONE
    }
}