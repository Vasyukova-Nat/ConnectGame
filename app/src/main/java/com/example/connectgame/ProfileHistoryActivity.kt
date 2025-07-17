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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_history)

        pairName = intent.getStringExtra(EXTRA_PAIR_NAME) ?: return
        historyTextView = findViewById(R.id.historyTextView)
        editButton = findViewById(R.id.editButton)
        saveButton = findViewById(R.id.saveButton)
        historyContainer = findViewById(R.id.historyContainer)

        pairDir = File(File(filesDir, "pairs"), pairName)

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

        val files = pairDir.walk().filter { it.isFile }.toList()

        for (file in files) {
            val fileNameView = TextView(this).apply {
                text = file.name
                textSize = 18f
                setPadding(0, 16, 0, 8)
            }
            historyContainer.addView(fileNameView)

            val lines = file.readLines()

            for (line in lines) {
                if (line.isNotBlank()) {
                    val checkBoxLayout = LinearLayout(this).apply {
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(16, 8, 16, 8)
                    }

                    val checkBox = CheckBox(this).apply {
                        isChecked = true
                    }

                    val questionView = TextView(this).apply {
                        text = line
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

    private fun saveChanges() {
        // TODO: Реализовать сохранение изменений
        saveButton.visibility = View.GONE
        editButton.visibility = View.VISIBLE
        historyTextView.visibility = View.VISIBLE

        historyContainer.removeAllViews()
        historyContainer.addView(historyTextView)
        loadPairHistory()

        Toast.makeText(this, "Изменения сохранены", Toast.LENGTH_SHORT).show()
    }
}