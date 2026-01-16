package com.example.signspeak.Fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.signspeak.AlarmActivity
import com.example.signspeak.R
import com.example.signspeak.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.btnSpeechToGesture.setOnClickListener {
            findNavController().navigate(R.id.speechToGestureFragment)
        }

        binding.btnGestureToSpeech.setOnClickListener {
            findNavController().navigate(R.id.gestureToSpeechFragment)
        }

        binding.btnLearnSign.setOnClickListener {
            findNavController().navigate(R.id.learnSignLanguageFragment)
        }

        // ✅ Navigate to AlarmActivity
        binding.alarmBar.setOnClickListener {
            val intent = Intent(requireContext(), AlarmActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
