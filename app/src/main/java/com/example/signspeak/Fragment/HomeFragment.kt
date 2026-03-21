package com.example.signspeak.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.signspeak.R
import com.example.signspeak.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupName()
        setupAnimations()
        setupClickListeners()

        return binding.root
    }

    private fun setupName() {
        val user = FirebaseAuth.getInstance().currentUser
        val name = user?.displayName?.split(" ")?.firstOrNull() ?: "User"
        binding.tvUserGreeting.text = "$name!"
    }

    private fun setupAnimations() {
        // ✨ Floating Hero Animation
        val floatAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.float_up_down)
        binding.tvGoodMorning.startAnimation(floatAnim)
        binding.tvUserGreeting.startAnimation(floatAnim)
        binding.tvDescription.startAnimation(floatAnim)

        // 🧠 AI Status Pulse
        val pulseAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.pulse_scale)
        binding.aiPulse.startAnimation(pulseAnim)

        // 🟢 Live Activity Indicator
        val pingAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.ping_pulse)
        binding.liveIndicator.startAnimation(pingAnim)
    }

    private fun setupClickListeners() {
        // Feature Cards
        binding.btnGestureToSpeech.setOnClickListener { navigateTo(R.id.gestureToSpeechFragment) }
        binding.btnStartScanning.setOnClickListener { navigateTo(R.id.gestureToSpeechFragment) }

        binding.btnSpeechToGesture.setOnClickListener { navigateTo(R.id.speechToGestureFragment) }
        binding.btnOpenVisualizer.setOnClickListener { navigateTo(R.id.speechToGestureFragment) }

        // Learning Section
        binding.btnLearnSign.setOnClickListener { navigateTo(R.id.learnSignLanguageFragment) }
        
        // Alarm Section
        binding.btnAlarmCard.setOnClickListener {
            startActivity(android.content.Intent(requireContext(), com.example.signspeak.AlarmActivity::class.java))
        }
    }

    private fun navigateTo(destinationId: Int) {
        findNavController().navigate(destinationId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
