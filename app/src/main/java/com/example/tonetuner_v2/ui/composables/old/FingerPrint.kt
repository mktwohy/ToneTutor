package com.example.tonetuner_v2.ui.composables.old

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.tonetuner_v2.app.AppSettings
import com.example.tonetuner_v2.audio.audioProcessing.Harmonic

@Composable
fun FingerPrint(
    modifier: Modifier = Modifier,
    fingerPrint: List<Harmonic>,
    color: Color = Color.Green,
) {
    if (fingerPrint.isEmpty()) return

    val f = fingerPrint.map { it.freq.toInt() to it.mag.toFloat() }.toMap()
    val bars = List(AppSettings.FINGERPRINT_SIZE) { i -> f[i] ?: 0f }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier
                .fillMaxHeight(0.9f)
                .fillMaxWidth()
        ) {
            val barWidth = this.size.width / bars.size

            for (i in bars.indices) {
                val barHeight = bars[i] * this.size.height
                drawRect(
                    topLeft = Offset(
                        (i / bars.size.toFloat()) * this.size.width,
                        this.size.height - barHeight
                    ),
                    color = color,
                    size = Size(barWidth, barHeight),
                )
                drawRect(
                    topLeft = Offset(
                        (i / bars.size.toFloat()) * this.size.width,
                        this.size.height - barHeight
                    ),
                    color = Color.Black,
                    size = Size(barWidth, barHeight),
                    style = Stroke(1f)
                )
            }
        }
    }
}
