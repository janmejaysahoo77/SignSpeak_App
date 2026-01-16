package com.example.signspeak

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity

class SettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        val rGroup = findViewById<RadioGroup>(R.id.rGroupLanguage)
        val btnConfirm = findViewById<Button>(R.id.btnConfirmLanguage)

        // Load saved language to keep radio state
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val savedLang = prefs.getString("language", "en")

        if (savedLang == "en") {
            findViewById<RadioButton>(R.id.rbEnglish).isChecked = true
        } else if (savedLang == "or") {
            findViewById<RadioButton>(R.id.rbOdia).isChecked = true
        }

        btnConfirm.setOnClickListener {
            val selectedId = rGroup.checkedRadioButtonId
            if (selectedId != -1) {
                val language = when (selectedId) {
                    R.id.rbEnglish -> "en"
                    R.id.rbOdia -> "or"
                    else -> "en"
                }
                saveLanguage(language)
            }
        }
    }

    private fun saveLanguage(lang: String) {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        prefs.edit().putString("language", lang).apply()
        restartApp()
    }

    private fun restartApp() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
}
