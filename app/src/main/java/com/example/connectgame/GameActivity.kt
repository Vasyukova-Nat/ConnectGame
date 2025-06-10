package com.example.connectgame

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        friendsQuestions = AssetReader.readLinesFromAsset(this, "friends_questions.txt")
        friendsActions = AssetReader.readLinesFromAsset(this, "friends_actions.txt")
        romanticQuestions = AssetReader.readLinesFromAsset(this, "romantic_questions.txt")
        romanticActions = AssetReader.readLinesFromAsset(this, "romantic_actions.txt")
        bothActions = AssetReader.readLinesFromAsset(this, "both_actions.txt")

        contentTextView = findViewById(R.id.contentTextView)
        truthButton = findViewById(R.id.truthButton)
        actionButton = findViewById(R.id.actionButton)
        nextButton = findViewById(R.id.nextButton)

        mode = intent.getStringExtra("mode") ?: "friends"

        if (friendsQuestions.isEmpty() || friendsActions.isEmpty() || romanticQuestions.isEmpty() || romanticActions.isEmpty()) {
            contentTextView.text = "Ошибка загрузки вопросов"
            truthButton.isEnabled = false
            actionButton.isEnabled = false
            nextButton.isEnabled = false
            return
        }

        setupUI()
    }

    private fun setupUI() {
        truthButton.visibility = View.VISIBLE
        actionButton.visibility = View.VISIBLE
        nextButton.visibility = View.GONE
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
        val questions = if (mode == "friends") {
            friendsQuestions
        } else {
            friendsQuestions + romanticQuestions
        }

        val randomQuestion = questions.random()
        contentTextView.text = randomQuestion
        showNextButton()
    }

    private fun showRandomAction() {
        val actions = if (mode == "friends") friendsActions + bothActions else romanticActions + bothActions
        val randomAction = actions.random()
        contentTextView.text = randomAction
        showNextButton()
    }

    private fun showNextButton() {
        nextButton.visibility = View.VISIBLE
        nextButton.setOnClickListener { resetChoiceButtons() }
    }

    private fun resetChoiceButtons() {
        contentTextView.text = "Выберите"
        truthButton.visibility = View.VISIBLE
        actionButton.visibility = View.VISIBLE
        nextButton.visibility = View.GONE
    }
}