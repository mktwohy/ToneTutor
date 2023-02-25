package com.example.tonetuner_v2.extensions

import android.content.Context
import android.content.pm.PackageManager
import android.view.LayoutInflater
import androidx.annotation.RawRes
import androidx.core.content.ContextCompat
import java.io.FileNotFoundException
import java.io.IOException

fun Context.hasPermission(permissionType: String): Boolean =
    ContextCompat.checkSelfPermission(this, permissionType) ==
        PackageManager.PERMISSION_GRANTED

val Context.layoutInflater: LayoutInflater get() =
    getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

fun Context.readTextFile(@RawRes fileResource: Int): String {
    return resources
        .openRawResource(fileResource)
        .bufferedReader()
        .use { it.readText() }
}

@Throws(FileNotFoundException::class, IOException::class)
fun Context.writeToInternalStorage(filename: String, content: ByteArray, append: Boolean = false) {
    val writeMode = if (append) Context.MODE_PRIVATE or Context.MODE_APPEND else Context.MODE_PRIVATE
    openFileOutput(filename, writeMode).use { fileOutput ->
        fileOutput.write(content)
    }
}

@Throws(FileNotFoundException::class, IOException::class)
fun Context.writeToInternalStorage(filename: String, content: String, append: Boolean = false) {
    writeToInternalStorage(filename, content.toByteArray(), append)
}

@Throws(FileNotFoundException::class, IOException::class)
fun Context.readFromInternalStorage(filename: String): ByteArray {
    openFileInput(filename).use {
        return it.readBytes()
    }
}

fun Context.readFromInternalStorageOrNull(filename: String): ByteArray? =
    try {
        readFromInternalStorage(filename)
    } catch (e: Exception) {
        null
    }
