package com.example.bem

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bem.ui.theme.BEMTheme

class BluetoothStatusViewModel(bth: Bluetooth): ViewModel() {
    private val isBluetoothOn = MutableLiveData<Boolean>(bth.isBluetoothEnabled)
}

@RequiresApi(Build.VERSION_CODES.M)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissionManager = PermissionManager(applicationContext, this)
        val bth = Bluetooth(applicationContext, permissionManager)

        permissionManager.requestPermission(Manifest.permission.BLUETOOTH)
//        permissionManager.requestPermission(Manifest.permission.BLUETOOTH_SCAN)
        val scanned = bth.getAvailableDevices()

        // Register for broadcasts when a device is discovered.
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)



        setContent {
            InitialScreen(bth.isBluetoothSupported, bth.isBluetoothEnabled, scanned)
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private val receiver = object : BroadcastReceiver() {

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun onReceive(context: Context, intent: Intent) {
            when(intent.action!!) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)!!
                    try {
                        val deviceName = device.name
                        val deviceHardwareAddress = device.address // MAC address
                        Log.i("FOUND: ", "$deviceName | $deviceHardwareAddress")
                    } catch(e: SecurityException) {
                        Log.i("EXECPTION: ", "device.name persmission to not granted")
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver)
    }

}

@Composable
fun Greeting(name: String) {
    Surface {
        Text(text = "Hi, my name is $name!", modifier = Modifier.padding(24.dp))
    }
}

@Composable
fun BluetoothSupport(isSupported: Boolean) {
    Surface() {
        Text(text = if (isSupported) "Device does support bluetooth" else "Device does not support bluetooth")
    }
}

@Composable
fun BluetoothStatus(isOn: Boolean) {
    Surface() {
        Text(text = if (isOn) "Bluetooth is on!" else "Bluetooth is off :(")
    }
}

data class Device(val name: String)

val sampleDevices: List<Device> = listOf(Device("jeden"), Device("dwa"))

@Composable
fun DeviceCard(device: Device) {
    Column(modifier = Modifier.padding(30.dp, 10.dp)){
        Text(text = device.name, color = Color.DarkGray)
    }
}

@Composable
fun AvailableDevices(devices: List<Device>) {
    LazyColumn {
        items(devices) { device ->
            DeviceCard(device)
        }
    }
}

@Composable
fun InitialScreen(isSupported: Boolean, isOn: Boolean, devices: List<Device>) {
    var isBthSupported by remember { mutableStateOf(devices) }

    BEMTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.Magenta) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column {
                    BluetoothSupport(isSupported)
                    BluetoothStatus(isOn)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    AvailableDevices(devices)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    InitialScreen(true, false, sampleDevices)
}