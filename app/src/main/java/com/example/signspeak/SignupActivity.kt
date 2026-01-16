package com.example.signspeak

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()

        val etName = findViewById<TextInputEditText>(R.id.etName)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val etConfirm = findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val btnSignup = findViewById<Button>(R.id.btnSignup)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)

        btnSignup.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString()
            val confirmPass = etConfirm.text.toString()

            // Validation
            if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
                showToast("All fields are required")
                return@setOnClickListener
            }

            if (pass.length < 6) {
                showToast("Password must be at least 6 characters")
                return@setOnClickListener
            }

            if (pass != confirmPass) {
                showToast("Passwords do not match")
                return@setOnClickListener
            }

            // Disable button to prevent double clicks (optional)
            btnSignup.isEnabled = false
            showToast("Creating account...")

            // Create user with Firebase Auth
            auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->
                    btnSignup.isEnabled = true
                    if (task.isSuccessful) {

                        // set display name on Firebase user (so you can read it later)
                        val user = auth.currentUser
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()

                        user?.updateProfile(profileUpdates)
                            ?.addOnCompleteListener { profileTask ->
                                if (profileTask.isSuccessful) {
                                    showToast("Signup successful")
                                } else {
                                    showToast("Signup succeeded, but name not set")
                                }

                                // (Optional) send email verification
                                user?.sendEmailVerification()

                                // go to Login (or MainActivity depending on your flow)
                                startActivity(Intent(this, LoginActivity::class.java))
                                finish()
                            }

                    } else {
                        showToast("Signup failed: ${task.exception?.localizedMessage ?: "Unknown error"}")
                    }
                }
        }

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
