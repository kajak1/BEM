package com.example.bem

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService

class Bluetooth(ctx: Context, val permissionManager: PermissionManager) {
    private val bluetoothManager = getSystemService(ctx, BluetoothManager::class.java)

    private val bluetoothAdapter = bluetoothManager?.adapter

    val isBluetoothSupported: Boolean
        get() = bluetoothAdapter != null

    val isBluetoothEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled == true

    fun getAvailableDevices(): List<Device> {
        try {
            val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices

            val mappedDevices = pairedDevices?.map<BluetoothDevice, Device> { device ->
                Device(device.name)
//                val deviceHardwareAddress = device.address // MAC address
            } ?: emptyList()

            if (mappedDevices.isEmpty()) {
                return listOf(Device("default device"))
            }

            return mappedDevices

        } catch (e: SecurityException) {
            Log.i("EXCEPTION: ","no CONNECT permission")

            return listOf(Device("expection device"))
        }
    }
}