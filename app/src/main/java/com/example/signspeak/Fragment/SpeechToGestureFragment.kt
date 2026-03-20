package com.example.signspeak.Fragment

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.signspeak.R
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*

class SpeechToGestureFragment : Fragment() {

    private lateinit var gestureImageView: ImageView
    private lateinit var speakButton: Button
    private lateinit var speechRecognizer: SpeechRecognizer

    // New UI Elements for different states
    private lateinit var cardInstruction: View
    private lateinit var layoutPlaceholder: View
    private lateinit var layoutListening: View
    private lateinit var cardResult: View
    private lateinit var tvRecognizedText: TextView
    private lateinit var pulseCircleOuter: View
    private lateinit var pulseCircleInner: View

    private var pulseAnim1: ObjectAnimator? = null
    private var pulseAnim2: ObjectAnimator? = null

    // FIX: Make all values a List<Int>
    private val wordToGifMap = mapOf(
        "hello" to listOf(R.raw.hello),
        "good" to listOf(R.raw.good),
        "morning" to listOf(R.raw.morning),
        "suprabhat" to listOf(R.raw.good, R.raw.morning),
        "walk" to listOf(R.raw.walk),
        "wash" to listOf(R.raw.wash),
        "way" to listOf(R.raw.way),
        "why" to listOf(R.raw.why),
        "will" to listOf(R.raw.will),
        "with" to listOf(R.raw.with),
        "without" to listOf(R.raw.without),
        "eat" to listOf(R.raw.eat),
        "Thank You" to listOf(R.raw.thankyou),
        "you" to listOf(R.raw.you)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_speech_to_gesture, container, false)

        gestureImageView = view.findViewById(R.id.gestureImageView)
        speakButton = view.findViewById(R.id.speakButton)

        cardInstruction = view.findViewById(R.id.cardInstruction)
        layoutPlaceholder = view.findViewById(R.id.layoutPlaceholder)
        layoutListening = view.findViewById(R.id.layoutListening)
        cardResult = view.findViewById(R.id.cardResult)
        tvRecognizedText = view.findViewById(R.id.tvRecognizedText)
        pulseCircleOuter = view.findViewById(R.id.pulseCircleOuter)
        pulseCircleInner = view.findViewById(R.id.pulseCircleInner)

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())

        speakButton.setOnClickListener {
            if (hasAudioPermission()) {
                startListening()
            } else {
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 1)
            }
        }

        showPlaceholder() // Initial state

        return view
    }

    private fun showPlaceholder() {
        cardInstruction.visibility = View.VISIBLE
        layoutPlaceholder.visibility = View.VISIBLE
        layoutListening.visibility = View.GONE
        cardResult.visibility = View.GONE
        stopPulseAnimation()
    }

    private fun showListening() {
        cardInstruction.visibility = View.GONE
        layoutPlaceholder.visibility = View.GONE
        layoutListening.visibility = View.VISIBLE
        cardResult.visibility = View.GONE
        startPulseAnimation()
    }

    private fun showResult(text: String) {
        cardInstruction.visibility = View.GONE
        layoutPlaceholder.visibility = View.GONE
        layoutListening.visibility = View.GONE
        cardResult.visibility = View.VISIBLE
        tvRecognizedText.text = text.uppercase()
        stopPulseAnimation()
    }

    private fun startPulseAnimation() {
        pulseAnim1 = ObjectAnimator.ofPropertyValuesHolder(
            pulseCircleOuter,
            PropertyValuesHolder.ofFloat("scaleX", 1f, 1.5f, 1f),
            PropertyValuesHolder.ofFloat("scaleY", 1f, 1.5f, 1f),
            PropertyValuesHolder.ofFloat("alpha", 0.2f, 0f, 0.2f)
        ).apply {
            duration = 1500
            repeatCount = ObjectAnimator.INFINITE
            start()
        }

        pulseAnim2 = ObjectAnimator.ofPropertyValuesHolder(
            pulseCircleInner,
            PropertyValuesHolder.ofFloat("scaleX", 1f, 1.4f, 1f),
            PropertyValuesHolder.ofFloat("scaleY", 1f, 1.4f, 1f),
            PropertyValuesHolder.ofFloat("alpha", 0.4f, 0f, 0.4f)
        ).apply {
            duration = 1500
            startDelay = 200
            repeatCount = ObjectAnimator.INFINITE
            start()
        }
    }

    private fun stopPulseAnimation() {
        pulseAnim1?.cancel()
        pulseAnim2?.cancel()
    }

    private fun startListening() {
        showListening()
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spokenText = matches?.firstOrNull()?.lowercase()
                if (!spokenText.isNullOrEmpty()) {
                    showResult(spokenText)
                    showMatchingGifs(spokenText)
                } else {
                    showPlaceholder()
                    Toast.makeText(requireContext(), "Could not recognize speech.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                // Not calling showPlaceholder here because we wait for onResults or onError
            }
            override fun onError(error: Int) {
                showPlaceholder()
                Toast.makeText(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer.startListening(intent)
    }

    private fun showMatchingGifs(text: String) {
        val words = text.split(" ")

        // Flatten all matched GIFs
        val gifs = words.flatMap { wordToGifMap[it] ?: emptyList() }

        if (gifs.isEmpty()) {
            Toast.makeText(requireContext(), "No matching gestures for '$text'!", Toast.LENGTH_SHORT).show()
            gestureImageView.setImageDrawable(null) // clear previous
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            for (gif in gifs) {
                if (!isAdded) return@launch
                Glide.with(requireContext())
                    .asGif()
                    .load(gif)
                    .into(gestureImageView)

                delay(2000)
            }
        }
    }

    private fun hasAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroyView() {
        super.onDestroyView()
        speechRecognizer.destroy()
        stopPulseAnimation()
    }
}
