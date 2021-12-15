package com.example.tonetuner_v2

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Slider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.signallib.Note

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // todo store these in app state
        // create audio processing objects
        val audioSource: AudioSource    = AudioCapture()
//        val audioSource: AudioSource    = SignalManagerWrapper()
        val audioProc                   = AudioProc(audioSource)

        // request audio permissions. the app will begin recording audio
        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    if (audioSource is AudioCapture) {
                        audioSource.startCapture()
                    }
                    if (audioSource is SignalManagerWrapper){
                        audioSource.notes = setOf(Note.A_4)
                        audioSource.signalSettings.harmonicSeries.generateRandom()
                    }
                    logd("Capture Started")
                } else {
                    logd("Microphone Permission Denied")
                }
            }
        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)

        // todo extract this out to a method
        // update loop
        Thread{
            while(true){
                with(AppModel){
                    val tunerData = pitch.toNoteAndCents()
                    pitch = audioProc.pitch
                    quality = audioProc.quality
                    fft = audioProc.fft.map { it.toFloat() }.normalize(0f, 1f)
                    note = tunerData.first
                    cents = tunerData.second
                    Thread.sleep(UI_LAG)
                }
            }
        }.start()

        setContent {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Tuner(
                    note    = AppModel.note,
                    cents   = AppModel.cents,
                    hz      = AppModel.pitch,
                    nullNoteMessage = "Too Quiet"
                )
                QualityMeter(
                    quality = AppModel.quality
                )
                TapeMeter(
                    modifier = Modifier.fillMaxWidth().fillMaxSize(.1f),
                    value    = AppModel.quality,
                    range    = 5,
                    allowNegatives = false
                )
                XYPlot(
                    modifier = Modifier
                        .fillMaxHeight(0.8f)
                        .fillMaxWidth()
                        .border(2.dp, Color.White),
                    y = AppModel.fft,
                )
            }
//            TestTapeMeter()

        }
    }

}
