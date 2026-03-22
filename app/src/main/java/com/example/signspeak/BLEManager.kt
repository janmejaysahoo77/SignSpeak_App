package com.example.signspeak

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import java.util.UUID

@SuppressLint("MissingPermission")
class BLEManager(private val context: Context, private val bleListener: BLEListener) {

    private val bluetoothAdapter: BluetoothAdapter?
        get() = BluetoothAdapter.getDefaultAdapter()

    private val bluetoothLeScanner
        get() = bluetoothAdapter?.bluetoothLeScanner

    private var bluetoothGatt: BluetoothGatt? = null
    private var isScanning = false
    private val handler = Handler(Looper.getMainLooper())
    private val SCAN_TIMEOUT = 15000L // 15 seconds timeout

    // Requesting Maximum MTU allows streaming large audio arrays
    private val DEVICE_NAME = "SignSpeak_Band"
    private val SERVICE_UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")
    private val CHAR_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")

    interface BLEListener {
        fun onDeviceFound(device: BluetoothDevice)
        fun onConnected()
        fun onDisconnected()
        fun onDataReceived(data: ByteArray)
        fun onError(message: String)
        fun onScanTimeout()
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val deviceName = device.name
            val deviceAddress = device.address
            val scanRecordName = result.scanRecord?.deviceName
            val serviceUuids = result.scanRecord?.serviceUuids

            // Log EVERY device found (critical for debugging)
            Log.d("BLE", "-----------------------------")
            Log.d("BLE", "FOUND → Name: ${deviceName ?: "null"}")
            Log.d("BLE", "         ScanRecord Name: ${scanRecordName ?: "null"}")
            Log.d("BLE", "         Address: $deviceAddress")
            Log.d("BLE", "         RSSI: ${result.rssi}")
            Log.d("BLE", "         Service UUIDs: ${serviceUuids?.joinToString() ?: "none"}")

            // Match by ANY of these conditions:
            // 1. device.name matches (exact or case-insensitive)
            // 2. scanRecord name matches
            // 3. device.name contains our target name
            // 4. advertised service UUID matches our service UUID
            val nameMatch = deviceName?.equals(DEVICE_NAME, ignoreCase = true) == true
            val scanNameMatch = scanRecordName?.equals(DEVICE_NAME, ignoreCase = true) == true
            val containsMatch = deviceName?.contains("SignSpeak", ignoreCase = true) == true
            val uuidMatch = serviceUuids?.any { it.uuid == SERVICE_UUID } == true

            if (nameMatch || scanNameMatch || containsMatch || uuidMatch) {
                Log.d("BLE", "✅ TARGET DEVICE MATCHED! (name=$nameMatch, scanName=$scanNameMatch, contains=$containsMatch, uuid=$uuidMatch)")
                stopScan()
                bleListener.onDeviceFound(device)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            isScanning = false
            val errorMsg = when (errorCode) {
                SCAN_FAILED_ALREADY_STARTED -> "Scan already started"
                SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> "App registration failed"
                SCAN_FAILED_FEATURE_UNSUPPORTED -> "BLE not supported"
                SCAN_FAILED_INTERNAL_ERROR -> "Internal error"
                else -> "Unknown error: $errorCode"
            }
            Log.e("BLE", "Scan failed: $errorMsg")
            bleListener.onError("Scan failed: $errorMsg")
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            Log.d("BLE", "Connection state changed → status: $status, newState: $newState")
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("BLE", "Connected to GATT server")
                bleListener.onConnected()
                
                // For small text messages like GPS, wait briefly then discover services directly.
                // Requesting large MTUs (512) can crash some ESP32 cores upon connection!
                handler.postDelayed({
                    Log.d("BLE", "Discovering services immediately (No MTU stretch needed for text)...")
                    gatt.discoverServices()
                }, 500)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("BLE", "Disconnected from GATT server")
                bleListener.onDisconnected()
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            Log.d("BLE", "MTU changed to: $mtu (Status: $status)")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BLE", "Discovering services after successful MTU stretch...")
                gatt.discoverServices()
            } else {
                Log.e("BLE", "MTU stretch failed! Attempting service discovery anyway...")
                gatt.discoverServices()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            Log.d("BLE", "Services discovered callback triggered, status: $status")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BLE", "Services discovered successfully")

                // Log ALL services for debugging
                for (service in gatt.services) {
                    Log.d("BLE", "  Service: ${service.uuid}")
                    for (char in service.characteristics) {
                        Log.d("BLE", "    Characteristic: ${char.uuid}, Properties: ${char.properties}")
                    }
                }

                val service = gatt.getService(SERVICE_UUID)
                if (service != null) {
                    Log.d("BLE", "Target service found: $SERVICE_UUID")
                    val characteristic = service.getCharacteristic(CHAR_UUID)
                    if (characteristic != null) {
                        Log.d("BLE", "Characteristic found: $CHAR_UUID")
                        gatt.setCharacteristicNotification(characteristic, true)
                        Log.d("BLE", "Notifications enabled")

                        // Standard CCCD UUID for BLE Notifications
                        val cccdUuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
                        val cccd = characteristic.getDescriptor(cccdUuid)
                        
                        if (cccd != null) {
                            val properties = characteristic.properties
                            // Check if characteristic supports Indicate vs Notify
                            if ((properties and BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
                                cccd.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                                Log.d("BLE", "Enabling INDICATIONS on CCCD")
                            } else {
                                cccd.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                                Log.d("BLE", "Enabling NOTIFICATIONS on CCCD")
                            }
                            gatt.writeDescriptor(cccd)
                        } else {
                            Log.e("BLE", "Standard CCCD (0x2902) not found!")
                            // Fallback just in case they used a non-standard descriptor setup
                            val descriptorList = characteristic.descriptors
                            if (descriptorList.isNotEmpty()) {
                                val fallback = descriptorList[0]
                                fallback.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                                gatt.writeDescriptor(fallback)
                                Log.w("BLE", "Wrote to fallback descriptor: ${fallback.uuid}")
                            } else {
                                Log.e("BLE", "No descriptors at all on this characteristic!")
                            }
                        }
                    } else {
                        Log.e("BLE", "Characteristic not found: $CHAR_UUID")
                        bleListener.onError("Characteristic not found")
                    }
                } else {
                    Log.e("BLE", "Service not found: $SERVICE_UUID")
                    bleListener.onError("Service not found")
                }
            } else {
                Log.e("BLE", "Service discovery failed with status: $status")
            }
        }

        @Deprecated("Deprecated in API 33, but used for wider compatibility")
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            val data = characteristic.value
            if (data != null) {
                bleListener.onDataReceived(data)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            bleListener.onDataReceived(value)
        }
    }

    fun startScan() {
        val scanner = bluetoothLeScanner
        if (scanner == null) {
            Log.e("BLE", "Bluetooth LE Scanner is null. Is Bluetooth turned ON?")
            bleListener.onError("Bluetooth LE Scanner is null. Turn on Bluetooth.")
            return
        }

        if (isScanning) {
            Log.d("BLE", "Already scanning, ignoring duplicate request")
            return
        }

        Log.d("BLE", "========== SCAN STARTED ==========")
        Log.d("BLE", "Looking for device: $DEVICE_NAME or Service UUID: $SERVICE_UUID")
        Log.d("BLE", "Scan timeout: ${SCAN_TIMEOUT / 1000} seconds")
        isScanning = true
        
        // Android hides text names on some phones, so we MUST scan actively for the UUID
        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(SERVICE_UUID))
            .build()
            
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
            
        scanner.startScan(listOf(filter), settings, scanCallback)

        // Auto-stop after timeout
        handler.postDelayed({
            if (isScanning) {
                Log.d("BLE", "========== SCAN TIMED OUT ==========")
                Log.d("BLE", "Device '$DEVICE_NAME' was NOT found in ${SCAN_TIMEOUT / 1000}s")
                stopScan()
                bleListener.onScanTimeout()
            }
        }, SCAN_TIMEOUT)
    }

    fun stopScan() {
        if (isScanning) {
            val scanner = bluetoothLeScanner
            scanner?.stopScan(scanCallback)
            isScanning = false
            handler.removeCallbacksAndMessages(null)
            Log.d("BLE", "Scan stopped")
        }
    }

    fun connect(device: BluetoothDevice) {
        Log.d("BLE", "Connecting to device: ${device.name} (${device.address})...")
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }

    fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        Log.d("BLE", "Disconnected and GATT closed")
    }

    fun sendCommand(command: String) {
        if (bluetoothGatt == null) {
            Log.e("BLE", "Cannot send command '$command'. Not connected.")
            return
        }
        val service = bluetoothGatt?.getService(SERVICE_UUID)
        val characteristic = service?.getCharacteristic(CHAR_UUID)
        if (characteristic != null) {
            characteristic.value = command.toByteArray()
            characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            bluetoothGatt?.writeCharacteristic(characteristic)
            Log.d("BLE", "Command sent: $command")
        } else {
            Log.e("BLE", "Cannot send command. Characteristic not found.")
        }
    }
}
