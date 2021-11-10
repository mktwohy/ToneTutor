package com.example.tonetuner_v2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tonetuner_v2.Constants.BUFFER_SIZE
import kotlinx.coroutines.launch

object AppModel{
    var fft by mutableStateOf(listOf<Float>())
    var pitch by mutableStateOf(123.45)
    var currentNote by mutableStateOf(Note.A_4)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    logd("Yes!")
                } else {
                    logd("No!")
                }
            }


        //requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)


        audioCapture.startCapture()


        Thread{
            while(true){
                recordAudio()
                Thread.sleep(25)
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
                    text = "Freq: ${AppModel.pitch.toString().substring(0, 6)} " +
                            "\nNote: ${AppModel.currentNote}",
                    color = Color.White
                )

                NoteList(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .border(2.dp, Color.White),
                    closestNote = AppModel.currentNote,
                    freq = AppModel.pitch
                )
            }
        }
    }

}


@Composable
fun NoteList(modifier: Modifier, freq: Double, closestNote: Note){
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LazyRow(
        modifier = modifier,
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(10.dp),

    ){
        coroutineScope.launch {
            listState.animateScrollToItem(Note.notes.indexOf(closestNote))
        }
        items(96){
            Text(
                text = "${Note.notes[it]}",
                color = Color.White,
                fontSize = 40.sp
            )
        }
    }
}

val audioCapture = AudioCapture()

val pitches = mutableListOf<Double>()
fun recordAudio(){
    var audioBuffer = AudioSample()

    // Fetch samples from the AudioCapture
    val samples = audioCapture.getAudioData(BUFFER_SIZE)

    //Feed samples to the audioBuffer
    audioBuffer = audioBuffer.dropAndAdd(samples)


    //Calculate FFT
    AppModel.fft = audioBuffer.fft
//        .asSequence()
//        .filterIndexed { index, d -> index % 2 == 0 }
        .map { it.toFloat() }
        .toMutableList()
        .also { it.normalize() }

    //Calculate pitch
    pitches.add(audioBuffer.pitch)
    if(pitches.size >= 10){
        AppModel.pitch = pitches.average()
        AppModel.currentNote = AppModel.pitch.toNote()
        pitches.clear()
    }




//    val fft = audioBuffer.fft.map { it.toFloat() }

//    logd(fft)
//    with(AppModel.fft){
//        this.clear()
//        for(i in this.indices){
//            this.add(fft[i])
//        }
//    }
}
