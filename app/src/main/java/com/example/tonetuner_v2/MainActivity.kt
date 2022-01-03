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
import com.example.signallib.enums.HarmonicFilter
import com.example.signallib.enums.WaveShape
import java.util.*

class MainActivity : ComponentActivity() {
    private val audioSource =
        if (AppModel.TEST_MODE)
            SignalSource(AppModel.NUM_HARMONICS)
        else
            MicSource()

    private val audioProc = AudioProc(audioSource = audioSource, pitchAlgo = PitchAlgorithms.twm)

    private val pitchTests: Queue<PitchTest> = createPitchTests(
        notes = AppModel.NOTE_RANGE,
        pitchBends = listOf(-0.25f, 0f, 0.25f),
        amps = listOf(1f),
        waveShapes = WaveShape.values().toList(),
        decayRates = listOf(0f),
        floors = listOf(0f),
        ceilings = listOf(1f),
        filters = HarmonicFilter.values().toList()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // request audio permissions. the app will begin recording audio
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                if (audioSource is MicSource) {
                    audioSource.startCapture()
                }
                if (audioSource is SignalSource){
                    audioSource.generateRandom()
                }
                logd("Capture Started")
            } else {
                logd("Microphone Permission Denied")
            }
        }.launch(Manifest.permission.RECORD_AUDIO)

        var counter = 0
        Thread{
            while(true){
                if(audioSource is SignalSource) {
                    if (counter == 300){
                        val test = pitchTests.poll() as PitchTest.SignalPitchTest

                        audioSource.notes = setOf(test.note)
                        audioSource.pitchBend = test.pitchBend
                        audioSource.amp = test.amp
                        audioSource.signalSettings.waveShape = test.waveShape
                        test.updateHarmonicSeries(audioSource.signalSettings.harmonicSeries)
                        counter = 0
                    }
                    else{
                        counter += 1
                    }
                }
                AppModel.updateAppModel(audioProc)
                Thread.sleep(AppModel.UI_LAG)
            }
        }.start()

        setContent {
            val color = if(AppModel.note != null) Color.Green else Color.Gray
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                if (audioSource is SignalSource){
                    Text(
                        text = "Note: ${audioSource.notes.toString().substring(1..3)} " +
                                "\nBend: ${(audioSource.pitchBend * 100).toInt()} cents",
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
                        .fillMaxHeight(0.3f)
                        .border(2.dp, Color.White),
                    value    = AppModel.quality,
                    range    = 3,
                    allowNegatives = false
                )
                BarChart(
                    modifier = Modifier.fillMaxSize(),
                    barValues = AppModel.fingerPrint.toFingerPrint(),
                    xTicks = List(AppModel.NUM_HARMONICS){ i -> if (i == 0) 'f' else i+1 },
                    yTicks = (0.0f..1.0f).toList(0.1f).map { it.toString().substring(0..2) },
                    barColor = Color.Green,
                    tickColor = Color.White
                )
            }
        }
    }

}
