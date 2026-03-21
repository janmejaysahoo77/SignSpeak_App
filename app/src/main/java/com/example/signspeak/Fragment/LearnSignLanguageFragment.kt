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

        val tabLayout = view.findViewById<com.google.android.material.tabs.TabLayout>(R.id.tabLayout)
        val learnContainer = view.findViewById<View>(R.id.learnContainer)
        val testContainer = view.findViewById<View>(R.id.testContainer)

        tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        learnContainer.visibility = View.VISIBLE
                        testContainer.visibility = View.GONE
                    }
                    1 -> {
                        learnContainer.visibility = View.GONE
                        testContainer.visibility = View.VISIBLE
                    }
                }
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })

        // Button 1 — Open YouTube (Mapped to Alphabets Card for now)
        val btnLearn = view.findViewById<View>(R.id.btnAlphabets)
        btnLearn.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://youtu.be/JPV-vboWfhY?si=18SjrUL-qdnPhrv_")
            )
            startActivity(intent)
        }

        // Button 2 — Go to Test Your Sign Language (Mapped to Test Skills Card)
        val btnTestSign = view.findViewById<View>(R.id.btnTestSkills)
        btnTestSign.setOnClickListener {
            val intent = Intent(requireContext(), SignLanguageTestActivity::class.java)
            startActivity(intent)
        }

        return view
    }
}
