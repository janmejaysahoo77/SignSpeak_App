package com.example.signspeak

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvSignup = findViewById<TextView>(R.id.tvSignup)

        // LOGIN BUTTON
        btnLogin.setOnClickListener {

            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString()

            if (email.isEmpty() || pass.isEmpty()) {
                showToast("Please enter both email and password")
                return@setOnClickListener
            }

            btnLogin.isEnabled = false
            showToast("Logging in...")

            auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->
                    btnLogin.isEnabled = true
                    if (task.isSuccessful) {

                        val user = auth.currentUser
                        if (user != null) {
                            showToast("Login successful: Welcome ${user.displayName ?: ""}")

                            // Go to MainActivity
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                    } else {
                        showToast("Login failed: ${task.exception?.localizedMessage}")
                    }
                }
        }

        // GO TO SIGNUP
        tvSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
