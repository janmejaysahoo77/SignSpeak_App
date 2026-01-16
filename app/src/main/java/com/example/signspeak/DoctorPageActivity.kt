package com.example.signspeak

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DoctorPageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_doctor_page)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 🔥 Call Doctor Button
        val callButton = findViewById<Button>(R.id.btnCallDoctor)

        callButton.setOnClickListener {
            // Send CHANNEL NAME to VideoCallActivity
            val intent = Intent(this, VideoCallActivity::class.java)
            intent.putExtra("CHANNEL_NAME", "doctorchannel")
            startActivity(intent)
        }
    }
}
