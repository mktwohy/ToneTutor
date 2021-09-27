package com.example.tonetuner_v2

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.tonetuner_v2.Constants.BUFFER_SIZE
import com.example.tonetuner_v2.Util.logd
import com.example.tonetuner_v2.Util.normalize
import java.text.Normalizer.normalize
import kotlin.concurrent.thread
import kotlin.math.log

object AppModel{
    var fft by mutableStateOf(listOf<Float>())
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
            }
        }.start()


        setContent {
            XYPlot(
                modifier = Modifier
                    .fillMaxSize()
                    .border(2.dp, Color.White),
                data = AppModel.fft
            )
        }
    }

}

val audioCapture = AudioCapture()

fun recordAudio(){
    var audioBuffer = AudioSamp()

    // Fetch samples from the AudioCapture
    val samples = audioCapture.getAudioData(BUFFER_SIZE)

    //Feed samples to the audioBuffer
    audioBuffer = audioBuffer.dropAndAdd(samples)


    //Calculate FFT
    AppModel.fft = audioBuffer.fft
        .map { it.toFloat() }
        .toMutableList()
        .also { it.normalize() }
//    val fft = audioBuffer.fft.map { it.toFloat() }

//    logd(fft)
//    with(AppModel.fft){
//        this.clear()
//        for(i in this.indices){
//            this.add(fft[i])
//        }
//    }
}
