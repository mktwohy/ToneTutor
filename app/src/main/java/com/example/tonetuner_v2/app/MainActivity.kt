package com.example.tonetuner_v2.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.Manifest
import com.example.tonetuner_v2.audio.audioProcessing.AudioProc
import com.example.tonetuner_v2.audio.audioProcessing.PitchAlgorithms
import com.example.tonetuner_v2.audio.audioSources.MicSource
import com.example.tonetuner_v2.extensions.requestPermission
import com.example.tonetuner_v2.ui.navigation.Navigation
import com.example.tonetuner_v2.util.Logger

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.init()

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val audioProc = AudioProc(
            audioSource = MicSource(this),
            pitchAlgo = PitchAlgorithms.twm
        )

        startAppUpdateThread(audioProc)

        setContent {
//            MainScreen(
//                modifier = Modifier.fillMaxSize(),
//                color = if(AppModel.note != null) Color.Green else Color.Gray
//            )
            Navigation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startAppUpdateThread(audioProc: AudioProc) {
        requestPermission(Manifest.permission.RECORD_AUDIO) { isGranted ->
            if (isGranted) {
                with(audioProc.audioSource) {
                    if (this is MicSource) this.startCapture()
                }
            }
        }

        Thread {
            while (true) {
                if (AppModel.playState)
                    AppModel.updateAppModel(audioProc)
                Thread.sleep(AppModel.UI_LAG)
            }
        }.start()
    }
}
