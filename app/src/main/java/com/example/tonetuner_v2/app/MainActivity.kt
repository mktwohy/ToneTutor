package com.example.tonetuner_v2.app

import android.Manifest
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.Text
import androidx.compose.ui.graphics.Color
import com.example.tonetuner_v2.audio.audioProcessing.AudioProc
import com.example.tonetuner_v2.audio.audioProcessing.PitchAlgorithms
import com.example.tonetuner_v2.audio.audioSources.MicSource
import com.example.tonetuner_v2.audio.audioSources.SignalSource
import com.example.tonetuner_v2.logd
import com.example.tonetuner_v2.ui.navigation.Navigation



class MainActivity : ComponentActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val audioProc = AudioProc(
            audioSource = MicSource(this.application),
            pitchAlgo = PitchAlgorithms.twm
        )

        startAudioInput(audioProc)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContent {
//            MainScreen(
//                modifier = Modifier.fillMaxSize(),
//                color = if(AppModel.note != null) Color.Green else Color.Gray
//            )
            Navigation()
        }
    }

    private fun startAudioInput(audioProc: AudioProc){
        val audioUpdateThread = Thread{
            while(true){
                if (AppModel.playState){
                    AppModel.updateAppModel(audioProc)
                }
                Thread.sleep(AppModel.UI_LAG)
            }
        }

        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                with(audioProc.audioSource) {
                    if (this is MicSource) this.startCapture()
                }
            }
        }.launch(Manifest.permission.RECORD_AUDIO)

        audioUpdateThread.start()
    }

}
