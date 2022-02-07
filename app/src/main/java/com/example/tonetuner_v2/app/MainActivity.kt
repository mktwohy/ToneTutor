package com.example.tonetuner_v2.app

import android.Manifest
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.example.tonetuner_v2.audio.audioProcessing.AudioProc
import com.example.tonetuner_v2.audio.audioProcessing.PitchAlgorithms
import com.example.tonetuner_v2.audio.audioSources.MicSource
import com.example.tonetuner_v2.checkMicPermission
import com.example.tonetuner_v2.requestMicPermission
import com.example.tonetuner_v2.ui.navigation.Navigation



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

    private fun startAppUpdateThread(audioProc: AudioProc){
        requestMicPermission(this){ isGranted ->
            if (isGranted){
                with (audioProc.audioSource){
                    if (this is MicSource) this.startCapture()
                }
            }
        }

        Thread{
            while(true){
                if (AppModel.playState)
                    AppModel.updateAppModel(audioProc)
                Thread.sleep(AppModel.UI_LAG)
            }
        }.start()
    }
}
