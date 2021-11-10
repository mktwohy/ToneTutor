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
    var fft by mutableStateOf(listOf<Float>())
    var pitch by mutableStateOf(123.45)
    var quality by mutableStateOf(123.45)
    var currentNote by mutableStateOf(Note.A_4)

    const val BUFFER_SIZE = 4096
    const val SAMPLE_RATE = 44100
}

// Todo: Redo this entire activity so that it makes use of AudioProc
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
                    pitch = audioProc.pitch
                    currentNote = pitch.toNote()
                    quality = audioProc.quality
                    fft = audioProc.fft.map { it.toFloat() }
                }
                Thread.sleep(10)
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
                    text = "Freq: ${AppModel.pitch.toString().substring(0, 3)} " +
                            "\nNote: ${AppModel.currentNote}",
                    color = Color.White
                )

                NoteList(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .border(2.dp, Color.White),
                    closestNote = AppModel.currentNote,
                )
            }
        }
    }

}
