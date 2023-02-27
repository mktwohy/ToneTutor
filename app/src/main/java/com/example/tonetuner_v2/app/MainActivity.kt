package com.example.tonetuner_v2.app

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.tonetuner_v2.audio.audioProcessing.AudioProc
import com.example.tonetuner_v2.audio.audioProcessing.PitchAlgorithms
import com.example.tonetuner_v2.audio.audioSources.MicSource
import com.example.tonetuner_v2.extensions.requestFullscreen
import com.example.tonetuner_v2.ui.navigation.MainScreen

class MainActivity : ComponentActivity() {
    private val app by lazy { application as App }
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestFullscreen()
        startAppUpdateThread()
        setContent {
            MainScreen(
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun startAppUpdateThread() {
        Thread {
            while (true) {
                if (!viewModel.isFrozen) {
                    viewModel.update(app.audioProcessor)
                }
                Thread.sleep(AppSettings.UI_LAG)
            }
        }.start()
    }
}
