package com.example.tonetuner_v2.app

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.signallib.enums.HarmonicFilter
import com.example.signallib.enums.WaveShape
import com.example.tonetuner_v2.audio.audioProcessing.AudioProc
import com.example.tonetuner_v2.audio.audioProcessing.PitchAlgorithms
import com.example.tonetuner_v2.audio.audioSources.MicSource
import com.example.tonetuner_v2.audio.audioSources.SignalSource
import com.example.tonetuner_v2.logd
import com.example.tonetuner_v2.pitchTesting.PitchTest
import com.example.tonetuner_v2.pitchTesting.createPitchTests
import com.example.tonetuner_v2.ui.navigation.MainScreen
import com.example.tonetuner_v2.ui.navigation.Navigation

class MainActivity : ComponentActivity() {
    private val audioSource =
        if (AppModel.TEST_MODE)
            SignalSource(AppModel.NUM_HARMONICS)
        else
            MicSource()

    private val audioProc = AudioProc(
        audioSource = audioSource,
        pitchAlgo = PitchAlgorithms.twm
    )

    private val pitchTests = createPitchTests(
        notes = AppModel.NOTE_RANGE,
        pitchBends = listOf(-0.25f, 0f, 0.25f),
        amps = listOf(1f),
        waveShapes = WaveShape.values().toList(),
        decayRates = listOf(0f),
        floors = listOf(0f),
        ceilings = listOf(1f),
        filters = HarmonicFilter.values().toList()
    )

    private val audioUpdateThread = Thread{
        var counter = 0
        while(true){
            if(audioSource is SignalSource) {
                if (counter == 300){
                    audioSource.startNextTest(pitchTests.poll() as PitchTest.SignalPitchTest)
                    counter = 0
                }
                else{
                    counter += 1
                }
            }
            AppModel.updateAppModel(audioProc)
            Thread.sleep(AppModel.UI_LAG)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startAudioInput()

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

    private fun SignalSource.startNextTest(test: PitchTest.SignalPitchTest) {
        this.notes = setOf(test.note)
        this.pitchBend = test.pitchBend
        this.amp = test.amp
        this.signalSettings.waveShape = test.waveShape
        test.updateHarmonicSeries(this.signalSettings.harmonicSeries)
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
