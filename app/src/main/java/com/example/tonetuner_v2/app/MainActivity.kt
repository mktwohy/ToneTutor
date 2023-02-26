package com.example.tonetuner_v2.app

import android.Manifest
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
import com.example.tonetuner_v2.extensions.requestPermission
import com.example.tonetuner_v2.ui.navigation.MainScreen
import com.example.tonetuner_v2.util.ContextHolder
import com.example.tonetuner_v2.util.Logger

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ContextHolder.hold(this)
        Logger.init()
        requestFullscreen()
        startAppUpdateThread(
            AudioProc(
                audioSource = MicSource(),
                pitchAlgo = PitchAlgorithms.twm
            )
        )
        val viewModel by viewModels<MainViewModel>()
        setContent {
            MainScreen(
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun startAppUpdateThread(audioProc: AudioProc) {
        val viewModel by viewModels<MainViewModel>()

        requestPermission(Manifest.permission.RECORD_AUDIO) { isGranted ->
            if (isGranted) {
                when (audioProc.audioSource) {
                    is MicSource -> audioProc.audioSource.startCapture()
                }
            }
        }
        Thread {
            while (true) {
                if (!viewModel.isFrozen) {
                    viewModel.update(audioProc)
                }
                Thread.sleep(AppModel.UI_LAG)
            }
        }.start()
    }
}
