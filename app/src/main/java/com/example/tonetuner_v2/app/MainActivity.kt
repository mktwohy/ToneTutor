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
    private val audioSource =
        if (AppModel.TEST_MODE)
            SignalSource(AppModel.FINGERPRINT_SIZE)
        else
            MicSource()


    private val audioProc = AudioProc(
        audioSource = audioSource,
        pitchAlgo = PitchAlgorithms.twm
    )

    private val audioUpdateThread = Thread{
        while(true){
            if (AppModel.playState){
                AppModel.updateAppModel(audioProc)
            }
            Thread.sleep(AppModel.UI_LAG)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startAudioInput()

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContent {
            if (audioSource is SignalSource){
                Text(
                    text = "Note: ${audioSource.notes.toString().substring(1..3)} " +
                            "\nBend: ${(audioSource.pitchBend * 100).toInt()} cents",
                    color = Color.White
                )
            }
//            MainScreen(
//                modifier = Modifier.fillMaxSize(),
//                color = if(AppModel.note != null) Color.Green else Color.Gray
//            )
            Navigation()
        }
    }

    private fun startAudioInput(){
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                if (audioSource is MicSource) audioSource.startCapture()
                if (audioSource is SignalSource) audioSource.generateRandom()
                logd("Capture Started")
            } else {
                logd("Microphone Permission Denied")
            }
        }.launch(Manifest.permission.RECORD_AUDIO)

        audioUpdateThread.start()
    }

}
