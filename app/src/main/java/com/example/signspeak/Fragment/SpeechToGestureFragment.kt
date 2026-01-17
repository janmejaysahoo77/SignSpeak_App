package com.example.signspeak.Fragment

import android.Manifest
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
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.signspeak.R
import kotlinx.coroutines.*

class SpeechToGestureFragment : Fragment() {

    private lateinit var gestureImageView: ImageView
    private lateinit var speakButton: Button
    private lateinit var speechRecognizer: SpeechRecognizer

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

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())

        speakButton.setOnClickListener {
            if (hasAudioPermission()) {
                startListening()
            } else {
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 1)
            }
        }

        return view
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spokenText = matches?.firstOrNull()?.lowercase()
                if (!spokenText.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "You said: $spokenText", Toast.LENGTH_SHORT).show()
                    showMatchingGifs(spokenText)
                }
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
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
            Toast.makeText(requireContext(), "No matching gestures!", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            for (gif in gifs) {
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
}
