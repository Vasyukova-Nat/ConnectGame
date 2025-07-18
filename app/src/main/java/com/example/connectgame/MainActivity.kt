package com.example.connectgame

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputEditText
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var profilesButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        profilesButton = findViewById(R.id.profilesButton)
        profilesButton.setOnClickListener { showProfilesDialog() }

        val friendsButton = findViewById<Button>(R.id.friendsButton)
        val romanticButton = findViewById<Button>(R.id.romanticButton)
        val hotButton = findViewById<Button>(R.id.hotButton)

        friendsButton.setOnClickListener {
            showModeSelectionDialog("friends")
        }

        romanticButton.setOnClickListener {
            showModeSelectionDialog("romantic")
        }

        hotButton.setOnClickListener {
            showModeSelectionDialog("hot")
        }
    }

    private fun showModeSelectionDialog(mode: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.pair_selection_dialog, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val pairs = getSavedPairs()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, pairs)
        dialogView.findViewById<Spinner>(R.id.pairsSpinner).adapter = adapter

        dialogView.findViewById<Button>(R.id.startGameButton).setOnClickListener {
            if (pairs.isNotEmpty()) {
                val selectedPair = pairs[dialogView.findViewById<Spinner>(R.id.pairsSpinner).selectedItemPosition]
                startGame(mode, selectedPair)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Нет сохраненных пар", Toast.LENGTH_SHORT).show()
            }
        }

        dialogView.findViewById<Button>(R.id.createNewPairButton).setOnClickListener {
            dialog.dismiss()
            showCreateNewPairDialog(mode)
        }

        dialog.show()
    }

    private fun showCreateNewPairDialog(mode: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.player_names, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogView.findViewById<Button>(R.id.confirmButton).setOnClickListener {
            val player1 = dialogView.findViewById<TextInputEditText>(R.id.player1Input).text?.toString()?.trim()?.takeIf { it.isNotBlank() } ?: "1"
            val player2 = dialogView.findViewById<TextInputEditText>(R.id.player2Input).text?.toString()?.trim()?.takeIf { it.isNotBlank() } ?: "2"

            val pairName = "${player1}-${player2}".replace(" ", "_")
            startGame(mode, pairName)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun startGame(mode: String, pairName: String) {
        val intent = Intent(this, GameActivity::class.java).apply {
            putExtra("mode", mode)
            putExtra("pairName", pairName)
        }
        startActivity(intent)
    }

    private fun showProfilesDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.profiles, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val pairs = getSavedPairs()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, pairs)
        dialogView.findViewById<Spinner>(R.id.profilesSpinner).adapter = adapter

        dialogView.findViewById<Button>(R.id.viewProfileButton).setOnClickListener {
            val selectedPair = pairs[dialogView.findViewById<Spinner>(R.id.profilesSpinner).selectedItemPosition]
            openProfileHistory(selectedPair)
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.editNamesButton).setOnClickListener {
            val selectedPair = pairs[dialogView.findViewById<Spinner>(R.id.profilesSpinner).selectedItemPosition]
            showEditNamesDialog(selectedPair, dialog)
        }

        dialogView.findViewById<Button>(R.id.deleteProfileButton).setOnClickListener {
            val selectedPair = pairs[dialogView.findViewById<Spinner>(R.id.profilesSpinner).selectedItemPosition]
            confirmDeleteProfile(selectedPair, dialog)
        }

        dialog.show()
    }

    private fun showEditNamesDialog(oldPairName: String, parentDialog: AlertDialog) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.edit_names_dialog, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val (oldPlayer1, oldPlayer2) = oldPairName.split("-").map { it.replace("_", " ") }
        dialogView.findViewById<TextInputEditText>(R.id.player1Input).setText(oldPlayer1)
        dialogView.findViewById<TextInputEditText>(R.id.player2Input).setText(oldPlayer2)

        dialogView.findViewById<Button>(R.id.saveButton).setOnClickListener {
            val newPlayer1 = dialogView.findViewById<TextInputEditText>(R.id.player1Input).text?.toString()?.trim() ?: ""
            val newPlayer2 = dialogView.findViewById<TextInputEditText>(R.id.player2Input).text?.toString()?.trim() ?: ""

            if (newPlayer1.isEmpty() || newPlayer2.isEmpty()) {
                Toast.makeText(this, "Введите имена обоих игроков", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPlayer1 == newPlayer2) {
                Toast.makeText(this, "Имена игроков не должны совпадать", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newPairName = "${newPlayer1}-${newPlayer2}".replace(" ", "_")
            if (renamePair(oldPairName, newPairName)) {
                Toast.makeText(this, "Имена успешно изменены", Toast.LENGTH_SHORT).show()
                parentDialog.dismiss()
                dialog.dismiss()
                showProfilesDialog()
            } else {
                Toast.makeText(this, "Ошибка при изменении имен", Toast.LENGTH_SHORT).show()
            }
        }

        dialogView.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun renamePair(oldPairName: String, newPairName: String): Boolean {
        if (oldPairName == newPairName) return true

        val pairsDir = File(filesDir, "pairs")
        val oldDir = File(pairsDir, oldPairName)
        val newDir = File(pairsDir, newPairName)

        if (!oldDir.exists()) return false
        if (newDir.exists()) {
            Toast.makeText(this, "Пара с такими именами уже существует", Toast.LENGTH_SHORT).show()
            return false
        }

        val oldPlayers = oldPairName.split("-")
        val newPlayers = newPairName.split("-")

        val tempDir = File(pairsDir, "temp_${System.currentTimeMillis()}")
        if (!tempDir.mkdirs()) return false

        try {
            oldDir.listFiles()?.forEach { oldFile ->
                val newFileName = when {
                    oldFile.name.startsWith("${oldPlayers[0]}_used_") ->
                        oldFile.name.replace("${oldPlayers[0]}_used_", "${newPlayers[0]}_used_")
                    oldFile.name.startsWith("${oldPlayers[1]}_used_") ->
                        oldFile.name.replace("${oldPlayers[1]}_used_", "${newPlayers[1]}_used_")
                    else -> oldFile.name
                }

                oldFile.copyTo(File(tempDir, newFileName))
            }

            oldDir.deleteRecursively()

            return tempDir.renameTo(newDir)
        } catch (e: Exception) {
            tempDir.deleteRecursively()
            return false
        }
    }

    private fun openProfileHistory(pairName: String) {
        val intent = Intent(this, ProfileHistoryActivity::class.java).apply {
            putExtra(ProfileHistoryActivity.EXTRA_PAIR_NAME, pairName)
        }
        startActivity(intent)
    }

    private fun getSavedPairs(): List<String> {
        val pairsDir = File(filesDir, "pairs")
        return if (pairsDir.exists() && pairsDir.isDirectory) {
            pairsDir.list()?.toList() ?: emptyList()
        } else {
            emptyList()
        }
    }

    private fun confirmDeleteProfile(pairName: String, parentDialog: AlertDialog) {
        AlertDialog.Builder(this)
            .setTitle("Подтверждение")
            .setMessage("Удалить все данные пары '$pairName'? Это действие нельзя отменить!")
            .setPositiveButton("Удалить") { _, _ ->
                deleteProfile(pairName)
                parentDialog.dismiss()
                Toast.makeText(this, "Профиль '$pairName' удален", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun deleteProfile(pairName: String) {
        val pairDir = File(File(filesDir, "pairs"), pairName)
        if (pairDir.exists()) {
            pairDir.deleteRecursively()
        }
    }
}