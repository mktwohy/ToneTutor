package com.example.tonetuner_v2.ui.composables.old

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

@Composable
fun XYPlot(
    modifier: Modifier = Modifier,
    y: List<Float>,
    color: Color = Color.Green,
    strokeWidth: Float = 3f,
) {
    if(y.isEmpty()) return
    Box(modifier = modifier, contentAlignment = Alignment.Center){
        Canvas(modifier = Modifier
            .fillMaxHeight(0.9f)
            .fillMaxWidth()
        ) {
            for(i in 0..y.size-2){
                drawLine(
                    start = Offset(
                        x = i * size.width / (y.size-1),
                        y = size.height - (y[i] * size.height)
                    ),
                    end = Offset(
                        x = (i+1) * size.width / (y.size-1),
                        y = size.height - (y[i+1] * size.height)
                    ),
                    color = color,
                    strokeWidth = strokeWidth
                )
            }
        }
    }
}