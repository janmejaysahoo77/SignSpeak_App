package com.example.signspeak

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.signspeak.Fragment.GestureRecognizerHelper
import com.example.signspeak.Fragment.GestureRecognizerHelper.ResultBundle
import com.google.android.material.button.MaterialButton
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class BasicGestureTestActivity : AppCompatActivity(),
    GestureRecognizerHelper.GestureRecognizerListener {

    private lateinit var tvGestureName: TextView
    private lateinit var imgGesturePreview: ImageView
    private lateinit var cameraPreview: PreviewView
    private lateinit var btnStartTest: MaterialButton

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var gestureRecognizerHelper: GestureRecognizerHelper

    private var targetGesture = ""
    private var isTesting = false

    // ⭐ FIXED: Explicit type declaration (removes type inference errors)
    private val gestureList: List<Pair<String, Int>> = listOf(
        "Thumb_Up" to R.raw.wash,
        "Victory" to R.raw.way,
        "Open_Palm" to R.raw.where,
        "Closed_Fist" to R.raw.we,
        "Pointing_Up" to R.raw.we
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_basic_gesture_test)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvGestureName = findViewById(R.id.tvGestureName)
        imgGesturePreview = findViewById(R.id.imgGesturePreview)
        cameraPreview = findViewById(R.id.cameraPreview)
        btnStartTest = findViewById(R.id.btnStartTest)

        cameraExecutor = Executors.newSingleThreadExecutor()

        // Initialize MediaPipe Gesture Recognizer
        gestureRecognizerHelper = GestureRecognizerHelper(
            context = this,
            gestureRecognizerListener = this
        )

        // Pick first gesture
        loadRandomGesture()

        // Camera permissions
        if (allPermissionsGranted()) startCamera()
        else requestPermission()

        btnStartTest.setOnClickListener {
            isTesting = true
            Toast.makeText(this, "Perform the gesture now!", Toast.LENGTH_SHORT).show()
        }
    }

    // Load Random Gesture + GIF
    private fun loadRandomGesture() {
        val (gestureName, gifRes) = gestureList.random()
        targetGesture = gestureName

        tvGestureName.text = gestureName.replace("_", " ").uppercase()

        Glide.with(this)
            .asGif()
            .load(gifRes)
            .into(imgGesturePreview)
    }

    // Start CameraX + MediaPipe Analyzer
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(cameraPreview.surfaceProvider)
            }

            val analyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { img ->
                        gestureRecognizerHelper.recognizeLiveStream(img)
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, analyzer
                )
            } catch (e: Exception) {
                Toast.makeText(this, "Camera error: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    // Get results from MediaPipe
    override fun onResults(resultBundle: ResultBundle) {
        if (!isTesting) return

        val detectedGesture = resultBundle.results.firstOrNull()
            ?.gestures()?.firstOrNull()?.firstOrNull()
            ?.categoryName() ?: return

        runOnUiThread {
            Log.d("GestureTest", "Detected = $detectedGesture, Target = $targetGesture")

            if (detectedGesture == targetGesture) {
                isTesting = false
                Toast.makeText(this, "Correct Gesture! 🎉", Toast.LENGTH_LONG).show()
                loadRandomGesture()
            } else {
                Toast.makeText(this, "Try again!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onError(error: String, errorCode: Int) {
        runOnUiThread {
            Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        gestureRecognizerHelper.clearGestureRecognizer()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 1001
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
