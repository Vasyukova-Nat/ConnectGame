package com.example.connectgame

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val friendsButton = findViewById<Button>(R.id.friendsButton)
        val romanticButton = findViewById<Button>(R.id.romanticButton)

        friendsButton.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("mode", "friends")
            startActivity(intent)
        }

        romanticButton.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("mode", "romantic")
            startActivity(intent)
        }
    }
}