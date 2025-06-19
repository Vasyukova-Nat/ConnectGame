package com.example.connectgame

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class GameActivity : AppCompatActivity() {
    private lateinit var contentTextView: TextView
    private lateinit var truthButton: Button
    private lateinit var actionButton: Button
    private lateinit var nextButton: Button
    private lateinit var mode: String

    private lateinit var friendsQuestions: List<String>
    private lateinit var friendsActions: List<String>
    private lateinit var romanticQuestions: List<String>
    private lateinit var romanticActions: List<String>
    private lateinit var bothActions: List<String>
    private lateinit var hotQuestions: List<String>
    private lateinit var hotActions: List<String>

    private var currentPlayer = 1
    private lateinit var playerNames: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        friendsQuestions = AssetReader.readLinesFromAsset(this, "friends_questions.txt")
        friendsActions = AssetReader.readLinesFromAsset(this, "friends_actions.txt")
        romanticQuestions = AssetReader.readLinesFromAsset(this, "romantic_questions.txt")
        romanticActions = AssetReader.readLinesFromAsset(this, "romantic_actions.txt")
        bothActions = AssetReader.readLinesFromAsset(this, "both_actions.txt")
        hotQuestions = AssetReader.readLinesFromAsset(this, "hot_questions.txt")
        hotActions = AssetReader.readLinesFromAsset(this, "hot_actions.txt")

        contentTextView = findViewById(R.id.contentTextView)
        truthButton = findViewById(R.id.truthButton)
        actionButton = findViewById(R.id.actionButton)
        nextButton = findViewById(R.id.nextButton)

        mode = intent.getStringExtra("mode") ?: "friends"

        showPlayerNamesDialog()
    }

    private fun showPlayerNamesDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.player_names, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogView.findViewById<Button>(R.id.confirmButton).setOnClickListener {
            val player1 = dialogView.findViewById<TextInputEditText>(R.id.player1Input).text.toString()
            val player2 = dialogView.findViewById<TextInputEditText>(R.id.player2Input).text.toString()

            if (player1.isNotBlank() && player2.isNotBlank()) {
                playerNames = listOf(player1, player2)
                dialog.dismiss()
                initUI()
            } else {
                Toast.makeText(this, "Введите имена игроков", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun initUI() {
        contentTextView = findViewById(R.id.contentTextView)
        truthButton = findViewById(R.id.truthButton)
        actionButton = findViewById(R.id.actionButton)
        nextButton = findViewById(R.id.nextButton)

        setupUI()
    }

    private fun setupUI() {
        truthButton.visibility = View.VISIBLE
        actionButton.visibility = View.VISIBLE
        nextButton.visibility = View.GONE

        contentTextView.text = "Ход ${playerNames[currentPlayer-1]}. Выберите:"

        truthButton.setOnClickListener {
            showRandomQuestion()
            truthButton.visibility = View.GONE
            actionButton.visibility = View.GONE
        }

        actionButton.setOnClickListener {
            showRandomAction()
            truthButton.visibility = View.GONE
            actionButton.visibility = View.GONE
        }
    }

    private fun showRandomQuestion() {
        val questions = when (mode) {
            "friends" -> friendsQuestions
            "romantic" -> friendsQuestions + romanticQuestions
            else -> hotQuestions
        }

        val randomQuestion = questions.random()
        contentTextView.text = randomQuestion
        showNextButton()
    }

    private fun showRandomAction() {
        val actions = when (mode) {
            "friends" -> friendsActions + bothActions
            "romantic" -> romanticActions + bothActions
            else -> hotActions
        }

        val randomAction = actions.random()
        contentTextView.text = randomAction
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
        contentTextView.text = "Ход ${playerNames[currentPlayer-1]}. Выберите:"
        truthButton.visibility = View.VISIBLE
        actionButton.visibility = View.VISIBLE
        nextButton.visibility = View.GONE
    }

    private fun showError(message: String) {
        contentTextView.text = message
        findViewById<Button>(R.id.truthButton).isEnabled = false
        findViewById<Button>(R.id.actionButton).isEnabled = false
        findViewById<Button>(R.id.nextButton).isEnabled = false
    }
}