package com.example.signspeak.Fragment

import android.Manifest
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.signspeak.R
import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizer
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// -------------------- GestureRecognizerHelper CLASS --------------------
class GestureRecognizerHelper(
    var minHandDetectionConfidence: Float = DEFAULT_HAND_DETECTION_CONFIDENCE,
    var minHandTrackingConfidence: Float = DEFAULT_HAND_TRACKING_CONFIDENCE,
    var minHandPresenceConfidence: Float = DEFAULT_HAND_PRESENCE_CONFIDENCE,
    var currentDelegate: Int = DELEGATE_CPU,
    var runningMode: RunningMode = RunningMode.LIVE_STREAM,
    val context: Context,
    val gestureRecognizerListener: GestureRecognizerListener? = null
) {
    private var gestureRecognizer: GestureRecognizer? = null

    init {
        setupGestureRecognizer()
    }

    fun clearGestureRecognizer() {
        gestureRecognizer?.close()
        gestureRecognizer = null
    }

    fun setupGestureRecognizer() {
        val baseOptionBuilder = BaseOptions.builder()
        baseOptionBuilder.setModelAssetPath(MP_RECOGNIZER_TASK)

        when (currentDelegate) {
            DELEGATE_CPU -> baseOptionBuilder.setDelegate(Delegate.CPU)
            DELEGATE_GPU -> baseOptionBuilder.setDelegate(Delegate.GPU)
        }

        try {
            val baseOptions = baseOptionBuilder.build()
            val optionsBuilder = GestureRecognizer.GestureRecognizerOptions.builder()
                .setBaseOptions(baseOptions)
                .setMinHandDetectionConfidence(minHandDetectionConfidence)
                .setMinTrackingConfidence(minHandTrackingConfidence)
                .setMinHandPresenceConfidence(minHandPresenceConfidence)
                .setRunningMode(runningMode)

            if (runningMode == RunningMode.LIVE_STREAM) {
                optionsBuilder.setResultListener(this::returnLivestreamResult)
                    .setErrorListener(this::returnLivestreamError)
            }

            val options = optionsBuilder.build()
            gestureRecognizer = GestureRecognizer.createFromOptions(context, options)
        } catch (e: Exception) {
            gestureRecognizerListener?.onError("Gesture recognizer failed: ${e.message}")
        }
    }

    fun recognizeLiveStream(imageProxy: ImageProxy) {
        val frameTime = SystemClock.uptimeMillis()
        val bitmap = imageProxy.toBitmap()
        imageProxy.close()

        val matrix = Matrix().apply {
            postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
            postScale(-1f, 1f, bitmap.width.toFloat(), bitmap.height.toFloat())
        }

        val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        val mpImage = BitmapImageBuilder(rotatedBitmap).build()
        recognizeAsync(mpImage, frameTime)
    }

    @VisibleForTesting
    fun recognizeAsync(mpImage: MPImage, frameTime: Long) {
        gestureRecognizer?.recognizeAsync(mpImage, frameTime)
    }

    private fun returnLivestreamResult(result: GestureRecognizerResult, input: MPImage) {
        val finishTimeMs = SystemClock.uptimeMillis()
        val inferenceTime = finishTimeMs - result.timestampMs()
        gestureRecognizerListener?.onResults(
            ResultBundle(listOf(result), inferenceTime, input.height, input.width)
        )
    }

    private fun returnLivestreamError(error: RuntimeException) {
        gestureRecognizerListener?.onError(error.message ?: "Unknown error")
    }

    data class ResultBundle(
        val results: List<GestureRecognizerResult>,
        val inferenceTime: Long,
        val inputImageHeight: Int,
        val inputImageWidth: Int,
    )

    interface GestureRecognizerListener {
        fun onError(error: String, errorCode: Int = OTHER_ERROR)
        fun onResults(resultBundle: ResultBundle)
    }

    companion object {
        private const val MP_RECOGNIZER_TASK = "gesture_recognizer.task"
        const val DELEGATE_CPU = 0
        const val DELEGATE_GPU = 1
        const val DEFAULT_HAND_DETECTION_CONFIDENCE = 0.5F
        const val DEFAULT_HAND_TRACKING_CONFIDENCE = 0.5F
        const val DEFAULT_HAND_PRESENCE_CONFIDENCE = 0.5F
        const val OTHER_ERROR = 0
        const val GPU_ERROR = 1
    }
}

// ---------------------- GestureToSpeechFragment ----------------------
class GestureToSpeechFragment : Fragment(), GestureRecognizerHelper.GestureRecognizerListener {

    private lateinit var previewView: PreviewView
    private lateinit var gestureText: TextView
    private lateinit var btnShowEmotion: Button // Defined the button here
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var gestureRecognizerHelper: GestureRecognizerHelper
    private var isTtsReady = false
    private var lastSpokenGesture: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_gesture_to_speech, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        previewView = view.findViewById(R.id.previewView)
        gestureText = view.findViewById(R.id.gestureText)
        btnShowEmotion = view.findViewById(R.id.btnShowEmotion) // Find button ID

        cameraExecutor = Executors.newSingleThreadExecutor()

        // ------------------ NEW LOGIC FOR EMOTION BUTTON ------------------
        btnShowEmotion.setOnClickListener {
            // 1. Show "Normal" immediately
            Toast.makeText(requireContext(), "Emotion: Normal", Toast.LENGTH_SHORT).show()

            // 2. Wait 9000 milliseconds (9 seconds), then show "Happy"
            Handler(Looper.getMainLooper()).postDelayed({
                // Check if the app is still open to prevent crash
                if (isAdded && context != null) {
                    Toast.makeText(requireContext(), "Emotion: Happy", Toast.LENGTH_SHORT).show()
                }
            }, 9000)
        }
        // ------------------------------------------------------------------

        gestureRecognizerHelper = GestureRecognizerHelper(
            context = requireContext(),
            gestureRecognizerListener = this
        )

        textToSpeech = TextToSpeech(requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale.US
                isTtsReady = true
            }
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 101)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        gestureRecognizerHelper.recognizeLiveStream(imageProxy)
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview, imageAnalyzer)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Camera init failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    override fun onResults(resultBundle: GestureRecognizerHelper.ResultBundle) {
        val gesture = resultBundle.results.firstOrNull()
            ?.gestures()?.firstOrNull()?.firstOrNull()?.categoryName() ?: "Unknown"

        Log.d("GestureTTS", "Gesture detected: $gesture")

        val mappedSpeech = when (gesture) {
            "Thumb_Up" -> "Great Job"
            "Victory" -> "I want peace"
            "Closed_Fist" -> "I am hungry"
            "Open_Palm" -> "You are welcome"
            else -> gesture
        }

        activity?.runOnUiThread {
            gestureText.text = "Gesture: $mappedSpeech"

            if (isTtsReady && mappedSpeech != lastSpokenGesture) {
                textToSpeech.speak(mappedSpeech, TextToSpeech.QUEUE_FLUSH, null, null)
                lastSpokenGesture = mappedSpeech
            }
        }
    }

    override fun onError(error: String, errorCode: Int) {
        activity?.runOnUiThread {
            gestureText.text = "Error: $error"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        gestureRecognizerHelper.clearGestureRecognizer()
        cameraExecutor.shutdown()
        textToSpeech.shutdown()
    }
}