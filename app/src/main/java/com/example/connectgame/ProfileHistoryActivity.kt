package com.example.connectgame
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class ProfileHistoryActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_PAIR_NAME = "extra_pair_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_history)

        val pairName = intent.getStringExtra(EXTRA_PAIR_NAME) ?: return
        val historyTextView = findViewById<TextView>(R.id.historyTextView)

        val history = loadPairHistory(pairName)
        historyTextView.text = if (history.isNotEmpty()) history else "Нет данных"
    }

    private fun loadPairHistory(pairName: String): String {
        val pairDir = File(File(filesDir, "pairs"), pairName)
        val history = StringBuilder("История пары $pairName:\n\n")

        pairDir.walk().filter { it.isFile }.forEach { file ->
            history.append("${file.name}:\n")
            history.append(file.readText())
            history.append("\n\n")
        }

        return history.toString()
    }
}