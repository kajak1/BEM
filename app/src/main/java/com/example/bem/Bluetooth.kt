package com.example.bem

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.getSystemService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID

val BLUETOOTH_BEM_SERVICE_UUID: UUID = UUID.fromString("e3bee544-4ddb-4768-a5f9-0eb7111a0a23")

@SuppressLint("MissingPermission")
class Bluetooth(ctx: Context, private val activity: Activity) {
    private val bluetoothManager = getSystemService(ctx, BluetoothManager::class.java)
    private val bluetoothAdapter = bluetoothManager?.adapter

    private val mmServerSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
        bluetoothAdapter?.listenUsingInsecureRfcommWithServiceRecord(
            "BEM",
            BLUETOOTH_BEM_SERVICE_UUID
        )
    }

    private var mmClientSocket: BluetoothSocket? = null

    val isBluetoothSupported: Boolean
        get() = bluetoothAdapter != null

    val isBluetoothEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled == true

    private fun makeDiscoverable() {
        val requestCode = 1;
        val discoverableIntent: Intent =
            Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 60)
            }
        startActivityForResult(activity, discoverableIntent, requestCode, null)
    }

    fun searchForDevices() {
        try {
            bluetoothAdapter?.startDiscovery()
        } catch (e: SecurityException) {
            Log.i("EXCEPTION: ", "start discovery, no DISCOVERY permission")
        }
    }

    fun cancelSearch() {
        try {
            bluetoothAdapter?.cancelDiscovery()
        } catch (e: SecurityException) {
            Log.i("EXCEPTION: ", "cancel discovery, no DISCOVERY permission")
        }
    }

    fun getPairedDevices(): List<Device> {
        try {
            val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices

            val mappedDevices = pairedDevices?.map<BluetoothDevice, Device> { device ->
                Device(device.name, device.address)
//                val deviceHardwareAddress = device.address // MAC address
            } ?: emptyList()

            if (mappedDevices.isEmpty()) {
                return listOf(Device("default device", "default MAC"))
            }

            return mappedDevices

        } catch (e: SecurityException) {
            Log.i("EXCEPTION: ", "no CONNECT permission")

            return listOf(Device("exception device", "exception MAC"))
        }
    }

    suspend fun listen() {
        makeDiscoverable()
        var shouldLoop = true
        withContext(Dispatchers.Default) {
            while (shouldLoop) {
                val socket: BluetoothSocket? = try {
                    Log.i("TRYING", "searching")
                    mmServerSocket?.accept()
                } catch (e: IOException) {
                    Log.e("ERROR", "Socket's accept() method failed", e)
                    shouldLoop = false
                    null
                }
                socket?.also {
//                    manageMyConnectedSocket(it)
                    Log.i("CONNECTED SOCKET SERVER", "Socket's accept() method successful")
                    mmServerSocket?.close()
                    shouldLoop = false
                }
            }
        }
    }

    fun stopListening() {
        Log.i("SCAN MODE", "${bluetoothAdapter?.scanMode}")
        try {
            mmServerSocket?.close()
            Log.i("TRYING", "closing")
        } catch (e: IOException) {
            Log.e("ERROR", "Could not close the connect socket", e)
        }
    }

    suspend fun connect(device: BluetoothDevice) {
        withContext(Dispatchers.Default) {
            bluetoothAdapter?.cancelDiscovery()

            mmClientSocket = device.createRfcommSocketToServiceRecord(BLUETOOTH_BEM_SERVICE_UUID)

            mmClientSocket?.let {
                try {
                    it.connect()
                } catch (e: IOException) {
                    it.close()
                    mmClientSocket = null
                }

                Log.i("CONNECTED", "connected to a server socket")
//            manageMyConnectedSocket(socket)
            }
        }

    }

    fun disconnect() {
        try {
            mmClientSocket?.close()
        } catch (e: IOException) {
            Log.e("CLIENT_SOCKET", "Could not close the client socket", e)
        }

    }
}