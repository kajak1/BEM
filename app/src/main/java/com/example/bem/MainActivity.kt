package com.example.bem

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.bem.ui.theme.BEMTheme

class MainActivity : ComponentActivity() {
    private val permissionManager: PermissionManager by lazy {
        PermissionManager(applicationContext, this)
    }

    private val bth: Bluetooth by lazy {
        Bluetooth(applicationContext, this)
    }

    private val bthViewModel: BluetoothViewModel by lazy {
        BluetoothViewModel(bth, permissionManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= 31) {
            permissionManager.requestPermission(
                Manifest.permission.BLUETOOTH_CONNECT,
                BluetoothAdapter.ACTION_REQUEST_ENABLE
            )
        } else {
            permissionManager.requestPermission(
                Manifest.permission.BLUETOOTH,
                BluetoothAdapter.ACTION_REQUEST_ENABLE
            )
        }

        // Register for broadcasts when a device is discovered.
        val foundFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        val enableFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        val discoveryStartFilter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        val discoveryFinishFilter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        val discoveryScanModeFilter = IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)

        registerReceiver(foundReceiver, foundFilter)
        registerReceiver(bthStateUpdateReceiver, enableFilter)
        registerReceiver(bthDiscoveryStartReceiver, discoveryStartFilter)
        registerReceiver(bthDiscoveryFinishReceiver, discoveryFinishFilter)
        registerReceiver(bthDiscoveryScanModeReceiver, discoveryScanModeFilter)

        setContent {
            InitialScreen(bthViewModel)
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private val foundReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = when (Build.VERSION.SDK_INT) {
                        33 -> {
                            permissionManager.requestPermission(Manifest.permission.BLUETOOTH_CONNECT)
                            intent.getParcelableExtra(
                                BluetoothDevice.EXTRA_DEVICE,
                                BluetoothDevice::class.java
                            )
                        }

                        else -> {
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        }
                    }

                    try {
                        if (device == null) {
                            bthViewModel.addAvailableDevice(Device("null device", "null MAC"))
                            Log.i("FOUND: ", "null device | null MAC")
                        } else {
                            val deviceName = device.name ?: "name not provided"
                            val deviceHardwareAddress = device.address
                            Log.i("FOUND: ", "$deviceName | $deviceHardwareAddress")
                            bthViewModel.addAvailableDevice(
                                Device(
                                    deviceName,
                                    deviceHardwareAddress
                                )
                            )
                        }
                    } catch (e: SecurityException) {
                        Log.i("EXCEPTION: ", "device.name permission to not granted")
                    }
                }
            }
        }
    }

    private val bthStateUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action!!) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state =
                        intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)

                    when (state) {
                        BluetoothAdapter.STATE_ON -> {
                            bthViewModel.updateBluetoothEnabled(true)
                            bthViewModel.lookupPairedDevices()
                            Log.i("STATE", "state ON")
                        }

                        BluetoothAdapter.STATE_OFF -> {
                            bthViewModel.updateBluetoothEnabled(false)
                            Log.i("STATE", "state OFF")
                        }
                    }
                }

            }
        }
    }

    private val bthDiscoveryStartReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action!!) {
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Log.i("SCAN", "start")
                    bthViewModel.updateIsSearching(true)
                    bthViewModel.clearAvailableDevices()
                }
            }
        }
    }

    private val bthDiscoveryFinishReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action!!) {
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.i("SCAN", "end")
                    bthViewModel.updateIsSearching(false)
                }
            }
        }
    }

    private val bthDiscoveryScanModeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action!!) {
                BluetoothAdapter.ACTION_SCAN_MODE_CHANGED -> {

                    val state =
                        intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR)

                    when (state) {
                        BluetoothAdapter.SCAN_MODE_CONNECTABLE -> {
                            Log.i(
                                "BTH",
                                "SCAN_MODE_CONNECTABLE - cant discover, can receive connection"
                            )
                            bthViewModel.updateIsDiscoverable(false)
                        }

                        BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE -> {
                            Log.i("BTH", "SCAN_MODE_CONNECTABLE_DISCOVERABLE")
                            bthViewModel.updateIsDiscoverable(true)

                        }

                        BluetoothAdapter.SCAN_MODE_NONE -> {
                            Log.i("BTH", "SCAN_MODE_NONE")
                            bthViewModel.updateIsDiscoverable(false)

                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(foundReceiver)
        unregisterReceiver(bthStateUpdateReceiver)
        unregisterReceiver(bthDiscoveryStartReceiver)
        unregisterReceiver(bthDiscoveryFinishReceiver)
        unregisterReceiver(bthDiscoveryScanModeReceiver)
    }
}

@Composable
fun BluetoothSupport(isSupported: Boolean) {
    Surface {
        Text(text = if (isSupported) "Device does support bluetooth" else "Device does not support bluetooth")
    }
}

@Composable
fun BluetoothStatus(isOn: Boolean) {
    Surface {
        Text(text = if (isOn) "Bluetooth is on!" else "Bluetooth is off :(")
    }
}

@Composable
fun DeviceCard(
    device: Device,
    content: (@Composable () -> Unit)? = null,
//    viewModel: BluetoothViewModel = viewModel()
) {
    var isExpanded by remember { mutableStateOf(false) }
    var isConnecting by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 5.dp)
            .defaultMinSize(minHeight = 48.dp)
            .border(1.dp, Color.Green)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .border(1.dp, Color.Red)
        ) {
            Surface(modifier = Modifier
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { /* Called when the gesture starts */ },
                        onDoubleTap = { /* Called on Double Tap */ },
                        onLongPress = { /* asdas */ },
                        onTap = { isConnecting = !isConnecting }
                    )
                }
                .weight(1f)
                .border(1.dp, Color.Blue)
            ) {
                Text(
                    text = device.name,
                    color = if (isConnecting) MaterialTheme.colors.secondary else MaterialTheme.colors.primary
                )
            }

            content?.let {
                Button(onClick = { isExpanded = !isExpanded }) {
                    content()
                }
            }
        }

        if (isExpanded) {
            Row {
                Text(
                    text = device.MAC,
                    color = MaterialTheme.colors.primary,
                )

                Button(onClick = { /* TODO add connect functionality - bth.connect() */ }) {
                    Text("connect")
                }
            }

        }

    }
}

@Composable
fun DevicesList(devices: List<Device>, deviceFn: (@Composable (device: Device) -> Unit)? = null) {
    LazyColumn {
        items(devices) { device ->
            deviceFn?.let { deviceFn(device) }
        }
    }
}

@Composable
fun PairedDevices(devices: List<Device>) {
    Column {
        Text("Previously connected devices:")
        DevicesList(devices.toList()) {
            DeviceCard(it, content = { Icon(Icons.Rounded.Settings, "Device options icon") })
        }
    }
}

@Composable
fun AvailableDevices(devices: Set<Device>) {
    Column {
        Text("AvailableDevices")
        DevicesList(devices.toList()) { DeviceCard(it) }
    }
}

@Composable
fun InitialScreen(bluetoothViewModel: BluetoothViewModel) {
    val bluetoothUiState by bluetoothViewModel.uiState.collectAsState()

    BEMTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Column {
                    BluetoothSupport(bluetoothUiState.isBluetoothSupported)
                    BluetoothStatus(bluetoothUiState.isBluetoothEnabled)
                }

                Button(onClick = { if (bluetoothUiState.isDiscoverable) bluetoothViewModel.stopListening() else bluetoothViewModel.listen() }) {
                    Text(if (bluetoothUiState.isDiscoverable) "waiting" else "Make device discoverable")
                }

                PairedDevices(bluetoothUiState.pairedDevices)

                Column {
                    Button(onClick = {
                        if (bluetoothUiState.isSearching) {
                            bluetoothViewModel.cancelSearch()
                        } else {
                            bluetoothViewModel.searchForDevices()
                        }
                    }) {
                        Text(if (bluetoothUiState.isSearching) "Searching..." else "Search for devices")
                    }

                    AvailableDevices(bluetoothUiState.availableDevices)
                }
            }
        }
    }
}

//@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
//    InitialScreen(true, false, sampleDevices)
}