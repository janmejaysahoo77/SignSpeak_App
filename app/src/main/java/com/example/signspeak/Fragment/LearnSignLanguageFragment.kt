package com.example.signspeak.Fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.signspeak.R
import com.example.signspeak.SignLanguageTestActivity

class LearnSignLanguageFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_learn_sign_language, container, false)

        // Button 1 — Open YouTube
        val btnLearn = view.findViewById<Button>(R.id.btnOpenYoutube)
        btnLearn.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://youtu.be/JPV-vboWfhY?si=18SjrUL-qdnPhrv_")
            )
            startActivity(intent)
        }

        // Button 2 — Go to Test Your Sign Language
        val btnTestSign = view.findViewById<Button>(R.id.btnTestSign)
        btnTestSign.setOnClickListener {
            val intent = Intent(requireContext(), SignLanguageTestActivity::class.java)
            startActivity(intent)
        }

        return view
    }
}
