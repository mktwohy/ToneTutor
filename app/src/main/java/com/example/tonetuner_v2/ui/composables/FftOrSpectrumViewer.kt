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
import com.example.tonetuner_v2.app.MainViewModel
import com.example.tonetuner_v2.audio.audioProcessing.Harmonic
import com.example.tonetuner_v2.audio.audioProcessing.toFingerPrint
import com.example.tonetuner_v2.audio.audioProcessing.toGraphRepr
import com.example.tonetuner_v2.extensions.step

// todo Composable is too stateful.
// todo fft does not plot correctly
@Composable
fun FftOrSpectrumViewer(
    fingerPrint: List<Harmonic>?,
    fft: List<Harmonic>?,
    spectrumType: MainViewModel.SpectrumType,
    onSpectrumTypeChange: (MainViewModel.SpectrumType) -> Unit,
    modifier: Modifier
) {
    Box(
        modifier.clickable {
            onSpectrumTypeChange(
                when (spectrumType) {
                    MainViewModel.SpectrumType.FFT -> MainViewModel.SpectrumType.FINGERPRINT
                    MainViewModel.SpectrumType.FINGERPRINT -> MainViewModel.SpectrumType.FFT
                }
            )
        }
    ) {
        when (spectrumType) {
            MainViewModel.SpectrumType.FINGERPRINT ->
                BarChart(
                    modifier = Modifier.fillMaxSize(),
                    barValues = fingerPrint!!.toFingerPrint(),
                    xTicks = List(AppModel.FINGERPRINT_SIZE) { i -> if (i == 0) 'f' else i + 1 },
                    yTicks = (0.0f..1.0f step 0.1f).map { it.toString().substring(0..2) },
                    barColor = Color.Green,
                    tickColor = Color.White
                )
            MainViewModel.SpectrumType.FFT ->
                XYPlot(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.9f),
                    y = fft!!.toGraphRepr()
                )
        }
    }
}
