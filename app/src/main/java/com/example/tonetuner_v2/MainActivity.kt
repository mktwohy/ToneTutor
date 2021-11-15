package com.example.tonetuner_v2

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object AppModel{
    // State
    var fft by mutableStateOf(listOf<Float>())
    var pitch by mutableStateOf(123.45)
    var quality by mutableStateOf(123.45)
    var note by mutableStateOf(Note.A_4)
    var cents by mutableStateOf(0)

    // Settings (requires app restart)
    const val PROC_BUFFER_SIZE      = 512    // values < 512 cause crash
    const val CAPTURE_BUFFER_SIZE   = 512
    const val SAMPLE_RATE           = 44100
    const val UI_LAG                = 10L
    const val FFT_QUEUE_SIZE        = 5
    const val QUALITY_QUEUE_SIZE    = 10
    const val PITCH_QUEUE_SIZE      = 40
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // create audio processing objects
        val audioCapture    = AudioCapture()
        val audioProc       = AudioProc(audioCapture)

        // request audio permissions. the app will begin recording audio
        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    audioCapture.startCapture()
                    logd("Yes!")
                } else {
                    logd("No!")
                }
            }
        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)

        Thread{

            while(true){
                with(AppModel){
                    val tunerData = pitch.toNoteAndCents()
                    pitch = audioProc.pitch
                    quality = audioProc.quality
                    fft = audioProc.fft.map { it.toFloat() - 1 }
                    note = tunerData.first
                    cents = tunerData.second
                    Thread.sleep(UI_LAG)
                }
            }
        }.start()

        setContent {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                XYPlot(
                    modifier = Modifier
                        .fillMaxHeight(0.8f)
                        .fillMaxWidth()
                        .border(2.dp, Color.White),
                    y = AppModel.fft
                )
                Text(
                    text = "Pitch: ${AppModel.pitch.toString(5)}",
                    color = Color.White
                )
//                Text(
//                    text = "Note: ${AppModel.note}",
//                    color = Color.White
//                )
                Text(
                    text = "Quality: ${AppModel.quality.toString(4)}",
                    color = Color.White
                )
//                NoteList(
//                    modifier = Modifier
//                        .fillMaxWidth(0.5f)
//                        .border(2.dp, Color.White),
//                    closestNote = AppModel.currentNote,
//                )
                Tuner(note = AppModel.note, cents = AppModel.cents)
            }
        }
    }

}
