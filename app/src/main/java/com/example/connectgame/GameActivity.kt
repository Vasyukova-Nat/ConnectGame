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
    private lateinit var romanticQuestions: List<String>
    private lateinit var romanticActions: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        friendsQuestions = AssetReader.readLinesFromAsset(this, "friends_questions.txt")
        romanticQuestions = AssetReader.readLinesFromAsset(this, "romantic_questions.txt")
        romanticActions = AssetReader.readLinesFromAsset(this, "romantic_actions.txt")

        contentTextView = findViewById(R.id.contentTextView)
        truthButton = findViewById(R.id.truthButton)
        actionButton = findViewById(R.id.actionButton)
        nextButton = findViewById(R.id.nextButton)

        mode = intent.getStringExtra("mode") ?: "friends"

        if (friendsQuestions.isEmpty() || romanticQuestions.isEmpty() || romanticActions.isEmpty()) {
            contentTextView.text = "Ошибка загрузки вопросов"
            truthButton.isEnabled = false
            actionButton.isEnabled = false
            nextButton.isEnabled = false
            return
        }

        setupUI()
        showInitialContent()
    }

    private fun setupUI() {
        if (mode == "friends") {
            truthButton.visibility = View.GONE // В дружеском режиме скрываем кнопки Правды/Действия
            actionButton.visibility = View.GONE
            nextButton.setOnClickListener { showRandomQuestion() }
        } else {
            truthButton.visibility = View.VISIBLE
            actionButton.visibility = View.VISIBLE
            nextButton.visibility = View.GONE

            truthButton.setOnClickListener { showRandomQuestion() }
            actionButton.setOnClickListener { showRandomAction() }
        }
    }

    private fun showInitialContent() {
        contentTextView.text = if (mode == "friends") {
            "Начать? (кнопка 'Далее' со стрелочкой)"
        } else {
            "Выберите 'Правда' или 'Действие'!"
        }
    }

    private fun showRandomQuestion() {
        val questions = if (mode == "friends") {
            friendsQuestions
        } else {
            friendsQuestions + romanticQuestions
        }

        val randomQuestion = questions.random()
        contentTextView.text = "$randomQuestion"

        if (mode == "romantic") {
            truthButton.visibility = View.GONE
            actionButton.visibility = View.GONE
            nextButton.visibility = View.VISIBLE
            nextButton.setOnClickListener { resetChoiceButtons() }
        }
    }

    private fun showRandomAction() {
        val randomAction = romanticActions.random()
        contentTextView.text = "Действие: $randomAction"
        truthButton.visibility = View.GONE
        actionButton.visibility = View.GONE
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