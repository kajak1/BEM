package com.example.bem

import android.Manifest
import android.os.Build
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class Device(val name: String, val MAC: String)

data class BluetoothUiState(
    var isBluetoothEnabled: Boolean = false,
    var isBluetoothSupported: Boolean = false,
//    var pairedDevices: List<Device> = emptyList(),
    var pairedDevices: List<Device> = listOf(
        Device("default device 1", "default MAC 1"),
        Device("default device 2", "default MAC 2")
    ),
//    var availableDevices: List<Device> = emptyList(),
    var availableDevices: Set<Device> = setOf(Device("available device 1", "available MAC 1")),
//    var availableDevices: Set<Device> = emptySet(),
    var isSearching: Boolean = false,
    var isDiscoverable: Boolean = false
)

class BluetoothViewModel(
    private val bth: Bluetooth,
    private val permissionManager: PermissionManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        BluetoothUiState(
            isBluetoothEnabled = bth.isBluetoothEnabled,
            isBluetoothSupported = bth.isBluetoothSupported,
//            pairedDevices = bth.getPairedDevices(),
        )
    )

    val uiState: StateFlow<BluetoothUiState> = _uiState.asStateFlow()

    fun updateIsDiscoverable(isDiscoverable: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                isDiscoverable = isDiscoverable
            )
        }
    }

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

    fun listen() {
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch { bth.listen() }
    }

    fun stopListening() {
        bth.stopListening()
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

    fun connect(MAC: String) {
        val scope = CoroutineScope(Dispatchers.Default)
//        scope.launch { bth.connect() }
    }
}