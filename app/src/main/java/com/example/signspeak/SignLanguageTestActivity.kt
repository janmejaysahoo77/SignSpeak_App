package com.example.signspeak

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class SignLanguageTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_language_test)

        // Card Views
        val cardBasicGestures = findViewById<CardView>(R.id.cardBasicGestures)
        val cardAlphabetTest = findViewById<CardView>(R.id.cardAlphabetTest)
        val cardWordsTest = findViewById<CardView>(R.id.cardWordsTest)
        val cardRandomTest = findViewById<CardView>(R.id.cardRandomTest)

        // ------------------------------
        // CLICK 1: BASIC GESTURES TEST
        // ------------------------------
        cardBasicGestures.setOnClickListener {
            startActivity(Intent(this, BasicGestureTestActivity::class.java))
        }

        // ------------------------------
        // CLICK 2: ALPHABET TEST (A–Z)
        // ------------------------------
        cardAlphabetTest.setOnClickListener {
            startActivity(Intent(this, AlphabetTestActivity::class.java))
        }

        // ------------------------------
        // CLICK 3: WORDS TEST
        // ------------------------------
        cardWordsTest.setOnClickListener {
            startActivity(Intent(this, WordsTestActivity::class.java))
        }

        // ------------------------------
        // CLICK 4: RANDOM TEST MODE
        // ------------------------------
        cardRandomTest.setOnClickListener {
            startActivity(Intent(this, RandomTestActivity::class.java))
        }
    }
}
