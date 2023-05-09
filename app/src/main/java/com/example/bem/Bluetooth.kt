package com.example.bem

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import java.util.UUID

val BLUETOOTH_BEM_SERVICE_UUID = UUID.fromString("BEM")

class Bluetooth(ctx: Context) {
    private val bluetoothManager = getSystemService(ctx, BluetoothManager::class.java)
    private val bluetoothAdapter = bluetoothManager?.adapter

    private var serverSocket: BluetoothServerSocket? = null
    val isBluetoothSupported: Boolean
        get() = bluetoothAdapter != null

    val isBluetoothEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled == true

    fun listen() {
        try {
            serverSocket = bluetoothAdapter?.listenUsingInsecureRfcommWithServiceRecord(
                "BEM",
                BLUETOOTH_BEM_SERVICE_UUID
            )
        } catch (e: SecurityException) {
            Log.i("EXCEPTION: ", "missing BLUETOOTH_CONNECT permission")
        }
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
}