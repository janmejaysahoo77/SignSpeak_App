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

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device)

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

        bleManager = BLEManager(this, this)

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

    override fun onDataReceived(data: String) {
        runOnUiThread {
            lastMessageText.text = "Last: $data"

            if (data.trim() == "VOICE") {
                Log.d("BLE", "Received: VOICE")
                lastMessageText.text = "VOICE → VIBRATE"

                bleManager.sendCommand("VIBRATE")
                Log.d("BLE", "Sent: VIBRATE")
            }
        }
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

    override fun onDestroy() {
        super.onDestroy()
        bleManager.disconnect()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
