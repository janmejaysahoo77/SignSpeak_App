package com.example.signspeak

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.Context
import android.media.AudioManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.views.overlay.MapEventsOverlay
import androidx.activity.OnBackPressedCallback
import android.content.Intent
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

// ========================================================================
// [VOSK - DISABLED] Audio imports — kept for future raw audio mode
// import android.media.AudioAttributes
// import android.media.AudioFormat
// import android.media.AudioTrack
// import java.util.concurrent.Executors
// import org.vosk.Model
// import org.vosk.Recognizer
// ========================================================================

class DeviceActivity : AppCompatActivity(), BLEManager.BLEListener {

    private lateinit var statusText: TextView
    private lateinit var latText: TextView
    private lateinit var lonText: TextView
    private lateinit var lastMessageText: TextView
    private lateinit var signalStrengthText: TextView
    private lateinit var deviceNameText: TextView
    private lateinit var batteryText: TextView
    private lateinit var latencyText: TextView
    private lateinit var uptimeText: TextView
    private lateinit var bleManager: BLEManager
    private var lastVibrateTime = 0L
    private var speechRecognizer: SpeechRecognizer? = null
    
    // For muting the SpeechRecognizer beep
    private var originalMusicVolume = 0
    private var originalSystemVolume = 0
    private var isMutedForSpeech = false

    // OSMDroid Map
    private lateinit var mapView: MapView
    private var deviceMarker: Marker? = null
    
    // Full Screen Map
    private lateinit var mapViewFullScreen: MapView
    private var deviceMarkerFullScreen: Marker? = null
    private lateinit var fullScreenMapContainer: View
    private lateinit var mainScrollView: View

    // ========================================================================
    // [VOSK - DISABLED] Audio/Speech fields — kept for future raw audio mode
    // private var audioTrack: AudioTrack? = null
    // private var audioPacketCount = 0
    // private var voskModel: Model? = null
    // private var voskRecognizer: Recognizer? = null
    // private val audioExecutor = Executors.newSingleThreadExecutor()
    // ========================================================================

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize OSMDroid configuration before setting content view
        Configuration.getInstance().load(applicationContext, getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        
        setContentView(R.layout.activity_device)

        // Prevent back button from exiting activity if full screen map is open
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (fullScreenMapContainer.visibility == View.VISIBLE) {
                    closeFullScreenMap()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        // Hide default action bar — using custom KINETIC HUD header
        supportActionBar?.hide()

        // Set status bar color to match the root background (#050A12)
        window.statusBarColor = android.graphics.Color.parseColor("#050A12")

        statusText = findViewById(R.id.statusText)
        latText = findViewById(R.id.latText)
        lonText = findViewById(R.id.lonText)
        lastMessageText = findViewById(R.id.lastMessageText)
        signalStrengthText = findViewById(R.id.signalStrengthText)
        deviceNameText = findViewById(R.id.deviceNameText)
        batteryText = findViewById(R.id.batteryText)
        latencyText = findViewById(R.id.latencyText)
        uptimeText = findViewById(R.id.uptimeText)

        mainScrollView = findViewById(R.id.mainScrollView)
        fullScreenMapContainer = findViewById(R.id.fullScreenMapContainer)
        mapViewFullScreen = findViewById(R.id.mapViewFullScreen)

        findViewById<View>(R.id.btnExitFullScreen).setOnClickListener {
            closeFullScreenMap()
        }

        mapView = findViewById(R.id.mapView)
        setupMap()

        bleManager = BLEManager(this, this)

        // ========================================================================
        // [VOSK - DISABLED] AudioTrack setup for live 16kHz PCM audio streaming
        // val minBufferSize = AudioTrack.getMinBufferSize(
        //     16000,
        //     AudioFormat.CHANNEL_OUT_MONO,
        //     AudioFormat.ENCODING_PCM_16BIT
        // )
        // audioTrack = AudioTrack.Builder()
        //     .setAudioAttributes(
        //         AudioAttributes.Builder()
        //             .setUsage(AudioAttributes.USAGE_MEDIA)
        //             .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        //             .build()
        //     )
        //     .setAudioFormat(
        //         AudioFormat.Builder()
        //             .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
        //             .setSampleRate(16000)
        //             .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
        //             .build()
        //     )
        //     .setBufferSizeInBytes(minBufferSize * 4)
        //     .setTransferMode(AudioTrack.MODE_STREAM)
        //     .build()
        // audioTrack?.play()
        // ========================================================================

        // ========================================================================
        // [VOSK - DISABLED] Vosk Speech AI initialization
        // Log.d("VOSK", "========================================")
        // Log.d("VOSK", "🧠 Starting Vosk Speech AI initialization...")
        // Log.d("VOSK", "========================================")
        // Thread {
        //     try {
        //         val modelDir = java.io.File(filesDir, "vosk-model")
        //         if (!java.io.File(modelDir, "final.mdl").exists()) {
        //             Log.d("VOSK", "📦 Copying model from assets to internal storage...")
        //             copyAssetFolder("model", modelDir.absolutePath)
        //             Log.d("VOSK", "📦 Model copy complete!")
        //         } else {
        //             Log.d("VOSK", "📦 Model already exists in internal storage, skipping copy.")
        //         }
        //         Log.d("VOSK", "🔧 Creating Vosk Model object...")
        //         voskModel = Model(modelDir.absolutePath)
        //         Log.d("VOSK", "🔧 Creating Vosk Recognizer (16kHz)...")
        //         voskRecognizer = Recognizer(voskModel, 16000.0f)
        //         Log.d("VOSK", "✅ Speech AI Engine Ready! Listening for 'hello'...")
        //         runOnUiThread {
        //             Toast.makeText(this@DeviceActivity, "Offline Speech AI Ready!", Toast.LENGTH_SHORT).show()
        //         }
        //     } catch (e: Exception) {
        //         Log.e("VOSK", "❌ Vosk init FAILED: ${e.message}", e)
        //     }
        // }.start()
        // ========================================================================

        Log.d("BLE", "========================================")
        Log.d("BLE", "📡 TEXT MODE ACTIVE — Listening for ESP32 text messages")
        Log.d("BLE", "========================================")

        findViewById<TextView>(R.id.tvHiddenTrigger).setOnClickListener {
            Log.d("VOICE", "Hidden trigger clicked: Requesting microphone and starting voice recognition")
            if (checkAndRequestPermissions()) {
                startVoiceRecognition()
            }
        }

        findViewById<Button>(R.id.btnConnect).setOnClickListener {
            Log.d("BLE", "Connect button clicked")
            if (checkAndRequestPermissions()) {
                checkBluetoothAndScan()
            }
        }

        findViewById<View>(R.id.btnVibrate).setOnClickListener {
            Log.d("BLE", "Vibration button clicked")
            bleManager.sendCommand("VIBRATE")
        }

        findViewById<View>(R.id.btnSOS).setOnClickListener {
            Log.d("BLE", "SOS button clicked")
            bleManager.sendCommand("SOS")
        }
    }

    // --- OSMDroid Map Setup ---
    private fun setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        val mapController = mapView.controller
        mapController.setZoom(18.0)
        
        // Default center point or wait for GPS
        val startPoint = GeoPoint(20.3, 85.8) // Default e.g. Bhubaneswar
        mapController.setCenter(startPoint)

        deviceMarker = Marker(mapView).apply {
            position = startPoint
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "Smart Band Location"
        }
        mapView.overlays.add(deviceMarker)
        mapView.invalidate()

        // --- Full Screen Map Setup ---
        mapViewFullScreen.setTileSource(TileSourceFactory.MAPNIK)
        mapViewFullScreen.setMultiTouchControls(true)
        val fullScreenController = mapViewFullScreen.controller
        fullScreenController.setZoom(18.0)
        fullScreenController.setCenter(startPoint)

        deviceMarkerFullScreen = Marker(mapViewFullScreen).apply {
            position = startPoint
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "Smart Band Location"
        }
        mapViewFullScreen.overlays.add(deviceMarkerFullScreen)
        mapViewFullScreen.invalidate()

        // Tap on mini map -> Expand to full screen
        val mReceive = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                openFullScreenMap()
                return true
            }
            override fun longPressHelper(p: GeoPoint?): Boolean { return false }
        }
        mapView.overlays.add(MapEventsOverlay(mReceive))
    }

    private fun openFullScreenMap() {
        runOnUiThread {
            mainScrollView.visibility = View.GONE
            fullScreenMapContainer.visibility = View.VISIBLE
            // Sync center and zoom
            mapViewFullScreen.controller.setCenter(mapView.mapCenter)
            mapViewFullScreen.controller.setZoom(mapView.zoomLevelDouble)
            mapViewFullScreen.invalidate()
        }
    }

    private fun closeFullScreenMap() {
        runOnUiThread {
            fullScreenMapContainer.visibility = View.GONE
            mainScrollView.visibility = View.VISIBLE
            // Reverse sync
            mapView.controller.setCenter(mapViewFullScreen.mapCenter)
            mapView.controller.setZoom(mapViewFullScreen.zoomLevelDouble)
            mapView.invalidate()
        }
    }

    private fun updateDeviceLocation(lat: Double, lon: Double) {
        val newPoint = GeoPoint(lat, lon)
        // Update Marker positions
        deviceMarker?.position = newPoint
        deviceMarkerFullScreen?.position = newPoint
        
        // Animate Maps smoothly to new location
        mapView.controller.animateTo(newPoint)
        mapViewFullScreen.controller.animateTo(newPoint)
        
        // Update Text Fields
        latText.text = String.format("%.4f", lat)
        lonText.text = String.format("%.4f", lon)
        
        mapView.invalidate() // Force map refresh
        mapViewFullScreen.invalidate()
    }

    private val bluetoothEnableLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            if (isLocationEnabled()) {
                statusText.text = "SCANNING..."
                statusText.setTextColor(android.graphics.Color.parseColor("#FFAB40"))
                bleManager.startScan()
            } else {
                showLocationAlert()
            }
        } else {
            Toast.makeText(this, "Bluetooth must be enabled to connect", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkBluetoothAndScan() {
        val bluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show()
            return
        }
        if (!bluetoothAdapter.isEnabled) {
            Log.d("BLE", "Bluetooth is OFF. Requesting user to enable...")
            val enableBtIntent = android.content.Intent(android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE)
            bluetoothEnableLauncher.launch(enableBtIntent)
            return
        }

        if (!isLocationEnabled()) {
            Log.e("BLE", "Location Services are OFF. BLE scan will return 0 results.")
            showLocationAlert()
            return
        }

        Log.d("BLE", "Bluetooth ON ✅, Location ON ✅ — Starting scan")
        statusText.text = "SCANNING..."
        statusText.setTextColor(android.graphics.Color.parseColor("#FFAB40"))
        bleManager.startScan()
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
               locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun showLocationAlert() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Location Required")
            .setMessage("BLE scanning requires Location Services to be turned ON. Please enable it in Settings.")
            .setPositiveButton("Open Settings") { _, _ ->
                startActivity(android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun checkAndRequestPermissions(): Boolean {
        val permissionsNeeded = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.RECORD_AUDIO)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        if (permissionsNeeded.isNotEmpty()) {
            Log.d("BLE", "Requesting permissions: $permissionsNeeded")
            ActivityCompat.requestPermissions(this, permissionsNeeded.toTypedArray(), PERMISSION_REQUEST_CODE)
            return false
        }
        Log.d("BLE", "All permissions already granted ✅")
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Log.d("BLE", "Permissions granted ✅")
                checkBluetoothAndScan()
            } else {
                Toast.makeText(this, "BLE permissions required", Toast.LENGTH_SHORT).show()
                Log.e("BLE", "Permissions denied by user")
            }
        }
    }

    // --- BLEListener Callbacks ---

    override fun onDeviceFound(device: BluetoothDevice) {
        runOnUiThread {
            statusText.text = "CONNECTING..."
            statusText.setTextColor(android.graphics.Color.parseColor("#FFAB40"))
            signalStrengthText.text = "SIGNAL STRENGTH: GOOD"
        }
        bleManager.connect(device)
    }

    override fun onConnected() {
        runOnUiThread {
            statusText.text = "CONNECTED"
            statusText.setTextColor(android.graphics.Color.parseColor("#4ADE80"))
            signalStrengthText.text = "SIGNAL STRENGTH: EXCELLENT"
            deviceNameText.text = "SIGNSPEAK_BAND"
        }
    }

    override fun onDisconnected() {
        runOnUiThread {
            statusText.text = "DISCONNECTED"
            statusText.setTextColor(android.graphics.Color.parseColor("#FF5252"))
            signalStrengthText.text = "SIGNAL STRENGTH: --"
        }
    }

    override fun onDataReceived(data: ByteArray) {
        // ====================================================================
        // TEXT MODE: ESP32 now sends text messages like "HELLO" instead of raw audio
        // ====================================================================

        // Safely convert the raw bytes to a UTF-8 string
        val message = try {
            String(data, Charsets.UTF_8).trim()
        } catch (e: Exception) {
            Log.e("BLE", "❌ Failed to decode BLE data as string: ${e.message}")
            return
        }

        // Ignore empty or null-like messages
        if (message.isEmpty()) {
            return
        }

        Log.d("BLE", "📩 Received text from ESP32: \"$message\" (${data.size} bytes)")

        // ----- GPS LIVE TRACKING LOGIC -----
        // Check if message is a GPS coordinate (e.g. "GPS:20.2961,85.8245")
        if (message.startsWith("GPS:", ignoreCase = true)) {
            Log.d("BLE", "📍 Received GPS String: $message")
            val coords = message.substring(4).split(",")
            if (coords.size >= 2) {
                val lat = coords[0].trim().toDoubleOrNull()
                val lon = coords[1].trim().toDoubleOrNull()
                if (lat != null && lon != null) {
                    runOnUiThread {
                        updateDeviceLocation(lat, lon)
                        lastMessageText.text = "GPS: $lat, $lon"
                    }
                } else {
                    Log.e("BLE", "❌ Failed to parse GPS coordinates: $message")
                }
            }
            return // Skip voice keyword matching for GPS data
        }

        // Update UI with the received general text
        runOnUiThread {
            lastMessageText.text = "Received: $message"
        }

        // Check for "HELLO" command (case-insensitive)
        if (message.equals("HELLO", ignoreCase = true)) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastVibrateTime > 3000) { // 3-second debounce
                lastVibrateTime = currentTime
                Log.d("BLE", "========================================")
                Log.d("BLE", "🎯 'HELLO' DETECTED from ESP32!")
                Log.d("BLE", "⬅️ SENDING BLE COMMAND: 'VIBRATE'")
                Log.d("BLE", "========================================")

                bleManager.sendCommand("VIBRATE")

                runOnUiThread {
                    lastMessageText.text = "Detected 'HELLO' → Sent VIBRATE"
                    Toast.makeText(this, "HELLO detected! Vibrating...", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.d("BLE", "⏳ HELLO detected but debounce active (wait 3s)")
            }
        }

        // ====================================================================
        // [VOSK - DISABLED] Old audio processing — kept for future raw audio mode
        //
        // audioPacketCount++
        //
        // if (audioPacketCount == 1) {
        //     Log.d("BLE", "========================================")
        //     Log.d("BLE", "🔥 FIRST PCM AUDIO PACKET RECEIVED! (${data.size} bytes)")
        //     Log.d("BLE", "🔥 ESP32 MIC IS ACTIVELY TRANSMITTING AUDIO VIA BLE!")
        //     Log.d("BLE", "========================================")
        // } else if (audioPacketCount % 20 == 0) {
        //     Log.d("BLE", "🔊 [LIVE AUDIO] Still receiving audio stream... (Packet #$audioPacketCount, ${data.size} bytes)")
        // }
        //
        // audioExecutor.execute {
        //     audioTrack?.write(data, 0, data.size)
        //
        //     val recognizer = voskRecognizer
        //     if (recognizer == null) {
        //         if (audioPacketCount % 100 == 1) {
        //             Log.w("VOSK", "⚠️ Recognizer is NULL at packet #$audioPacketCount. Model not loaded yet!")
        //         }
        //         return@execute
        //     }
        //
        //     val isFinal = recognizer.acceptWaveForm(data, data.size)
        //     val resultJson = if (isFinal) recognizer.result else recognizer.partialResult
        //
        //     if (audioPacketCount % 40 == 0) {
        //         Log.d("VOSK", "👂 Vosk hears: $resultJson")
        //     }
        //
        //     if (resultJson.contains("hello", ignoreCase = true)) {
        //         val currentTime = System.currentTimeMillis()
        //         if (currentTime - lastVibrateTime > 3000) {
        //             lastVibrateTime = currentTime
        //             Log.d("VOSK", "🎯 VOSK AI HEARD: 'hello'!")
        //             bleManager.sendCommand("VIBRATE")
        //             runOnUiThread {
        //                 lastMessageText.text = "Detected 'hello' → Sent VIBRATE"
        //             }
        //         }
        //     }
        //
        //     if (isFinal && resultJson.length > 20) {
        //         Log.d("VOSK", "Transcript: $resultJson")
        //     }
        // }
        //
        // if (audioPacketCount % 5 == 0) {
        //     runOnUiThread {
        //         lastMessageText.text = "Streaming Live PCM Audio... Packet #$audioPacketCount (${data.size} bytes)"
        //     }
        // }
        // ====================================================================
    }

    override fun onScanTimeout() {
        runOnUiThread {
            statusText.text = "NOT FOUND"
            statusText.setTextColor(android.graphics.Color.parseColor("#FFA500"))
            Toast.makeText(this, "Could not find SignSpeak_Band. Make sure ESP32 is powered on and advertising.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onError(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            statusText.text = "ERROR"
            statusText.setTextColor(android.graphics.Color.parseColor("#FF5252"))
        }
    }

    // --- Voice Recognition Logic ---
    private fun restoreAudioVolume() {
        if (isMutedForSpeech) {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalMusicVolume, 0)
            audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, originalSystemVolume, 0)
            isMutedForSpeech = false
        }
    }

    private fun startVoiceRecognition() {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Log.d("VOICE", "🎤 Ready for speech...")
                    // Restore volume a short time after readiness so the beep remains muted
                    restoreAudioVolume()
                    runOnUiThread {
                        lastMessageText.text = "Listening..."
                    }
                }
                override fun onBeginningOfSpeech() {
                    Log.d("VOICE", "🗣️ Speech started...")
                }
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    Log.d("VOICE", "🛑 Speech ended.")
                }
                override fun onError(error: Int) {
                    val errorMessage = getErrorText(error)
                    Log.e("VOICE", "❌ Speech recognition error: $errorMessage")
                    restoreAudioVolume()
                    runOnUiThread {
                        lastMessageText.text = "Voice Error: $errorMessage"
                    }
                }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val text = matches[0]
                        Log.d("VOICE", "✅ Recognized speech: \"$text\"")
                        processSpeech(text)
                    } else {
                        Log.w("VOICE", "⚠️ No speech matches found.")
                    }
                    restoreAudioVolume()
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }

        try {
            Log.d("VOICE", "🚀 Starting SpeechRecognizer listening...")
            
            // Mute the starting "beep" sound
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (!isMutedForSpeech) {
                originalMusicVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                originalSystemVolume = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM)
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
                audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, 0)
                isMutedForSpeech = true
            }

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e("VOICE", "❌ Failed to start voice recognition: ${e.message}", e)
            restoreAudioVolume()
        }
    }

    private fun processSpeech(text: String) {
        val lowerText = text.lowercase()
        Log.d("VOICE", "🧠 Processing speech text: \"$lowerText\"")
        
        runOnUiThread {
            lastMessageText.text = "Heard: $text"
        }

        if (lowerText.contains("hello")) {
            Log.d("VOICE", "========================================")
            Log.d("VOICE", "🎯 KEYWORD 'HELLO' DETECTED -> 1 TIME VIBRATION")
            Log.d("VOICE", "========================================")
            sendBLECommand("VIBRATE_1")
            runOnUiThread {
                lastMessageText.text = "Detected 'hello' -> Sent VIBRATE_1"
            }
        } else if (lowerText.contains("help")) {
            Log.d("VOICE", "========================================")
            Log.d("VOICE", "🎯 KEYWORD 'HELP' DETECTED -> 2 TIME VIBRATION")
            Log.d("VOICE", "========================================")
            sendBLECommand("VIBRATE_2")
            runOnUiThread {
                lastMessageText.text = "Detected 'help' -> Sent VIBRATE_2"
            }
        } else {
            Log.d("VOICE", "No keyword match found for \"$lowerText\"")
        }
    }

    private fun sendBLECommand(command: String) {
        Log.d("VOICE", "⬅️ SENDING BLE COMMAND: '$command'")
        bleManager.sendCommand(command)
    }

    private fun getErrorText(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
            SpeechRecognizer.ERROR_SERVER -> "Error from server"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input timeout"
            else -> "Didn't understand, please try again."
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        mapViewFullScreen.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        mapViewFullScreen.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDetach()
        mapViewFullScreen.onDetach()
        bleManager.disconnect()
        speechRecognizer?.destroy()
        speechRecognizer = null
        // ========================================================================
        // [VOSK - DISABLED] Audio cleanup — kept for future raw audio mode
        // audioTrack?.stop()
        // audioTrack?.release()
        // audioExecutor.shutdown()
        // ========================================================================
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    // ========================================================================
    // [VOSK - DISABLED] Helper to copy assets — kept for future raw audio mode
    // private fun copyAssetFolder(assetPath: String, outputPath: String) {
    //     val assetManager = assets
    //     val files = assetManager.list(assetPath) ?: return
    //     val outDir = java.io.File(outputPath)
    //     if (!outDir.exists()) outDir.mkdirs()
    //     for (file in files) {
    //         val subAssetPath = "$assetPath/$file"
    //         val subFiles = assetManager.list(subAssetPath)
    //         if (subFiles != null && subFiles.isNotEmpty()) {
    //             copyAssetFolder(subAssetPath, "$outputPath/$file")
    //         } else {
    //             assetManager.open(subAssetPath).use { input ->
    //                 java.io.File(outputPath, file).outputStream().use { output ->
    //                     input.copyTo(output)
    //                 }
    //             }
    //         }
    //     }
    // }
    // ========================================================================
}
