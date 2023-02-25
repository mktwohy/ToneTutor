package com.example.tonetuner_v2.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.tonetuner_v2.app.AppModel
import com.example.tonetuner_v2.app.AppModel.spectrumType
import com.example.tonetuner_v2.audio.audioProcessing.toFingerPrint
import com.example.tonetuner_v2.audio.audioProcessing.toGraphRepr
import com.example.tonetuner_v2.extensions.step
import com.example.tonetuner_v2.ui.navigation.MainLayout

// todo Composable is too stateful.
// todo fft does not plot correctly
@Composable
fun FftOrSpectrumViewer(modifier: Modifier) {
    Box(
        modifier.clickable { AppModel.changeSpectrumType() }
    ) {
        when (spectrumType) {
            MainLayout.SpectrumType.FINGERPRINT ->
                BarChart(
                    modifier = Modifier.fillMaxSize(),
                    barValues = AppModel.fingerPrint.toFingerPrint(),
                    xTicks = List(AppModel.FINGERPRINT_SIZE) { i -> if (i == 0) 'f' else i + 1 },
                    yTicks = (0.0f..1.0f step 0.1f).map { it.toString().substring(0..2) },
                    barColor = Color.Green,
                    tickColor = Color.White
                )
            MainLayout.SpectrumType.FFT ->
                XYPlot(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.9f),
                    y = AppModel.fft.toGraphRepr()
                )
        }
    }
}
