package com.example.tonetuner_v2.app

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.tonetuner_v2.audio.audioProcessing.AudioProc
import com.example.tonetuner_v2.audio.audioProcessing.PitchAlgorithms
import com.example.tonetuner_v2.audio.audioSources.MicSource
import com.example.tonetuner_v2.extensions.requestFullscreen
import com.example.tonetuner_v2.extensions.requestPermission
import com.example.tonetuner_v2.ui.navigation.Navigation
import com.example.tonetuner_v2.util.Logger

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.init()
        requestFullscreen()
        startAppUpdateThread(
            AudioProc(
                audioSource = MicSource(this),
                pitchAlgo = PitchAlgorithms.twm
            )
        )
        setContent {
            Navigation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startAppUpdateThread(audioProc: AudioProc) {
        requestPermission(Manifest.permission.RECORD_AUDIO) { isGranted ->
            if (isGranted) {
                when (audioProc.audioSource) {
                    is MicSource -> audioProc.audioSource.startCapture()
                }
            }
        }
        Thread {
            while (true) {
                if (AppModel.playState) {
                    AppModel.updateAppModel(audioProc)
                }
                Thread.sleep(AppModel.UI_LAG)
            }
        }.start()
    }
}
