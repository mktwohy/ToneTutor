package com.example.tonetuner_v2

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.signallib.Note
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    private fun audioSourceGenerateRandom(audioSource: SignalManagerWrapper){
        audioSource.notes = setOf(AppModel.NOTE_RANGE.random())
        audioSource.pitchBend = Random.nextDouble(-0.5, 0.5).toFloat()
        audioSource.signalSettings.harmonicSeries.generateRandom()
    }

//    val audioSource: AudioSource    = AudioCapture()
    val audioSource: AudioSource    = SignalManagerWrapper()
    val audioProc                   = AudioProc(audioSource)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                        audioSourceGenerateRandom(audioSource)
                    }
                    logd("Capture Started")
                } else {
                    logd("Microphone Permission Denied")
                }
            }
        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)

        // todo extract this out to a method

        var counter = 0
        // update loop
        Thread{
            while(true){
                if (counter == 300 && audioSource is SignalManagerWrapper){
                    audioSourceGenerateRandom(audioSource)
                    counter = 0
                }

                with(AppModel){
                    val tunerData = pitch.toNoteAndCents()
                    pitch = audioProc.pitch
                    quality = audioProc.quality
//                    fft = audioProc.fft.map { it.toFloat() }.normalize(0f, 1f)
                    fft = audioProc.fft.map { it.toFloat() }.normalize(0f, 1f)
                    note = tunerData.first
                    cents = tunerData.second

                    if(audioSource is SignalManagerWrapper) counter += 1

                    Thread.sleep(UI_LAG)
                }
            }
        }.start()

        setContent {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                if (audioSource is SignalManagerWrapper){
                    Text(
                        text = "Note: ${audioSource.notes.toString().substring(1..3)} " +
                                "\nPitch Bend: ${(audioSource.pitchBend * 100).toInt()} cents",
                        color = Color.White
                    )
                }


                CircularTuner(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.3f),
                    note = AppModel.note,
                    centsErr = AppModel.cents
                )
                TapeMeter(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.2f)
                        .border(2.dp, Color.White),
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
//            CircularTunerTest()


        }
    }

}
