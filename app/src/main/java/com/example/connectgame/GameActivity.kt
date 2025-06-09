package com.example.connectgame

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class GameActivity : AppCompatActivity() {
    private lateinit var contentTextView: TextView
    private lateinit var nextButton: Button
    private lateinit var mode: String

    private lateinit var friendsQuestions: List<String>
    private lateinit var romanticQuestions: List<String>
    private lateinit var romanticActions: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // Загружаем вопросы из файлов
        friendsQuestions = AssetReader.readLinesFromAsset(this, "friends_questions.txt")
        romanticQuestions = AssetReader.readLinesFromAsset(this, "romantic_questions.txt")
        romanticActions = AssetReader.readLinesFromAsset(this, "romantic_actions.txt")

        contentTextView = findViewById(R.id.contentTextView)
        nextButton = findViewById(R.id.nextButton)

        mode = intent.getStringExtra("mode") ?: "friends"

        // Проверяем, что вопросы загружены
        if (friendsQuestions.isEmpty() || romanticQuestions.isEmpty() || romanticActions.isEmpty()) {
            contentTextView.text = "Ошибка загрузки вопросов"
            nextButton.isEnabled = false
            return
        }

        showRandomContent()

        nextButton.setOnClickListener {
            showRandomContent()
        }
    }

    private fun showRandomContent() {
        val content = when {
            mode == "friends" -> friendsQuestions.random()
            else -> when ((1..3).random()) {
                1 -> friendsQuestions.random()
                2 -> romanticQuestions.random()
                else -> romanticActions.random()
            }
        }
        contentTextView.text = content
    }
}