package com.example.tonetuner_v2.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat

/**
 * calls [checkMicPermission] to ensure permission isn't already granted. If permission denied,
 * it launches a permission request, and callback will be invoked with the result
 * (true if permission granted) as its input.
 */
fun requestMicPermission(context: ComponentActivity, callback: (Boolean) -> Unit = { } ){
    if (checkMicPermission(context)) return

    context.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        callback(it)
    }.launch(Manifest.permission.RECORD_AUDIO)
}

fun checkMicPermission(context: Context): Boolean{
    val status = ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
    return status == PackageManager.PERMISSION_GRANTED
}