package com.example.tonetuner_v2.extensions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

inline fun <reified T : Activity> Activity.launchActivity(intent: Intent.() -> Unit = {}) {
    startActivity(
        Intent(this, T::class.java).apply(intent)
    )
}

fun Activity.hideKeyboard() {
    this.currentFocus?.let { view ->
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

/**
 * Wrapper for [Toast.makeText]. Returns [text] so that it can be chained into other functions
 *
 * Example:
 * ```kotlin
 * showToast("Hello, World!")
 *     .also(Timber::e)
 * ```
 */
fun Activity.showToast(text: String, length: Int = Toast.LENGTH_SHORT): String {
    runOnUiThread {
        Toast.makeText(this, text, length).show()
    }
    return text
}

@Throws(FileNotFoundException::class, IOException::class)
fun Activity.writeToExternalStorage(uri: Uri, content: ByteArray) {
    applicationContext.contentResolver.openFileDescriptor(uri, "w")?.use { parcelDescriptor ->
        FileOutputStream(parcelDescriptor.fileDescriptor).use {
            it.write(content)
        }
    }
}

@Throws(FileNotFoundException::class, IOException::class)
fun Activity.writeToExternalStorage(uri: Uri, content: String) {
    writeToExternalStorage(uri, content.toByteArray())
}

fun ComponentActivity.requestPermission(permission: String, callback: (hasPermission: Boolean) -> Unit) {
    if (hasPermission(permission)) {
        callback(true)
    }
    registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        callback(it)
    }.launch(permission)
}
