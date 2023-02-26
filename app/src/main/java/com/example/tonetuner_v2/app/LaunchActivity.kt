package com.example.tonetuner_v2.app

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.tonetuner_v2.extensions.launchActivity
import com.example.tonetuner_v2.ui.composables.AlertDialog
import com.example.tonetuner_v2.util.ContextHolder
import com.example.tonetuner_v2.util.Logger
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@SuppressLint("CustomSplashScreen")
class LaunchActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ContextHolder.hold(this)
        Logger.init()

        setContent {
            // If more permissions are needed, use rememberMultiplePermissionsState()
            // see https://google.github.io/accompanist/permissions/
            val recordAudioPermissionState = rememberPermissionState(
                Manifest.permission.RECORD_AUDIO
            )
            var showRecordAudioRationale by remember { mutableStateOf(false) }

            when {
                recordAudioPermissionState.status.isGranted -> {
                    launchActivity<MainActivity>()
                }
                recordAudioPermissionState.status.shouldShowRationale -> {
                    showRecordAudioRationale = true
                }
                else -> {
                    SideEffect {
                        recordAudioPermissionState.launchPermissionRequest()
                    }
                }
            }

            AlertDialog(
                show = showRecordAudioRationale,
                title = "Permission Required: Record Audio",
                message = "Tone Tutor needs access to the microphone to display timbre metrics.",
                onDismiss = {
                    showRecordAudioRationale = false
                    recordAudioPermissionState.launchPermissionRequest()
                },
            )
        }
    }
}
