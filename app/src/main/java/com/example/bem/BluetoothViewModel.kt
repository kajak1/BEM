package com.example.bem

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.os.Build
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.*

data class Device(val name: String, val MAC: String)

data class BluetoothUiState(
    var isBluetoothEnabled: Boolean = false,
    var isBluetoothSupported: Boolean = false,
    var pairedDevices: List<Device> = emptyList(),
//    var availableDevices: List<Device> = emptyList(),
    var availableDevices: Set<Device> = emptySet(),
    var isSearching: Boolean = false,
)

class BluetoothViewModel(private val bth: Bluetooth, private val permissionManager: PermissionManager): ViewModel() {
    private val _uiState = MutableStateFlow(
        BluetoothUiState(
            isBluetoothEnabled = bth.isBluetoothEnabled,
            isBluetoothSupported = bth.isBluetoothSupported,
            pairedDevices = bth.getPairedDevices(),
        )
    )

    val uiState: StateFlow<BluetoothUiState> = _uiState.asStateFlow()

    fun updateBluetoothEnabled(isEnabled: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                isBluetoothEnabled = isEnabled
            )
        }
    }

    fun updateIsSearching(newIsSearching: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                isSearching = newIsSearching
            )
        }
    }

    fun searchForDevices() {
        if (!_uiState.value.isSearching) {
            when {
                Build.VERSION.SDK_INT >= 31 -> {
                    permissionManager.requestPermission(Manifest.permission.BLUETOOTH_SCAN)
                    bth.searchForDevices()
                }

                else -> {
                    permissionManager.requestPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    bth.searchForDevices()
                }
            }
        }
    }

    fun cancelSearch() {
        if (_uiState.value.isSearching) {
            bth.cancelSearch()
        }
    }

    fun addAvailableDevice(device: Device) {
        _uiState.update { currentState ->
            currentState.copy(
                availableDevices = setOf(*(currentState.availableDevices.toTypedArray()), device)
            )
        }
    }

    fun clearAvailableDevices() {
        _uiState.update { currentState ->
            currentState.copy(
                availableDevices = emptySet()
//                availableDevices = emptyList()
            )
        }
    }

    fun lookupPairedDevices() {
        _uiState.update { currentState ->
            currentState.copy(
                pairedDevices = bth.getPairedDevices()
            )
        }
    }
}