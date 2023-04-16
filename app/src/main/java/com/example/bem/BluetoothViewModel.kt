package com.example.bem

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
    var availableDevices: List<Device> = emptyList(),
    var isSearching: Boolean = false,
)

class BluetoothViewModel(private val bth: Bluetooth): ViewModel() {
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
        bth.searchForDevices()
    }

    fun addAvailableDevice(device: Device) {
        _uiState.update { currentState ->
            currentState.copy(
                availableDevices = listOf(*(currentState.availableDevices.toTypedArray()), device)
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