package com.example.bem

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionManager(private val ctx: Context, private val activity: ComponentActivity){
    private val getBluetoothPermission =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.i("Bluetooth permission: ", "Granted")
            } else {
                Log.i("Bluetooth permission: ", "Denied")
            }
        }

    fun requestPermission(permission: String) {
        when {
            ContextCompat.checkSelfPermission(
                ctx,
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.i("PERMISSION:", "PackageManger - granted")
                activity.startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                // You can use the API that requires the permission.
            }
            ActivityCompat.shouldShowRequestPermissionRationale(activity, permission) -> {
                Log.i("PERMISSION:", "shouldShowRequestPermissionRationale - asking")
                getBluetoothPermission.launch(permission)

                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected, and what
                // features are disabled if it's declined. In this UI, include a
                // "cancel" or "no thanks" button that lets the user continue
                // using your app without granting the permission.
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                getBluetoothPermission.launch(permission)
            }
        }
    }
}

//private val getBluetoothPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
//    if (isGranted) {
//        Log.i("Bluetooth permission: ", "Granted")
//    } else {
//        Log.i("Bluetooth permission: ", "Denied")
//    }
//}
//
//private fun requestPermission(persmission: String) {
//    when {
//        ContextCompat.checkSelfPermission(
//            this,
//            persmission
//        ) == PackageManager.PERMISSION_GRANTED -> {
//            Log.i("PERMISSION:", "PackageManger - granted")
//            startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
//            // You can use the API that requires the permission.
//        }
//        ActivityCompat.shouldShowRequestPermissionRationale(this, persmission) -> {
//            Log.i("PERMISSION:", "shouldShowRequestPermissionRationale - asking")
//            getBluetoothPermission.launch(persmission)
//
//            // In an educational UI, explain to the user why your app requires this
//            // permission for a specific feature to behave as expected, and what
//            // features are disabled if it's declined. In this UI, include a
//            // "cancel" or "no thanks" button that lets the user continue
//            // using your app without granting the permission.
//        }
//        else -> {
//            // You can directly ask for the permission.
//            // The registered ActivityResultCallback gets the result of this request.
//            getBluetoothPermission.launch(persmission)
//        }
//    }
//}