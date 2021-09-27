package com.example.tonetuner_v2

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.tonetuner_v2.Util.logd


@Composable
fun XYPlot(
    modifier: Modifier = Modifier,
    data: List<Float>,
    color: Color = Color.Green,
    strokeWidth: Float = 3f,
) {
    logd("DRAW!")

    Box(modifier = modifier, contentAlignment = Alignment.Center){
        Canvas(modifier = Modifier
            .fillMaxHeight(0.9f)
            .fillMaxWidth()) {
            for(i in 0..data.size-2){
                drawLine(
                    start = Offset(
                        x = i * size.width / (data.size-1),
                        y = (data[i] * -1 * size.height/2) + (size.height/2)
                    ),
                    end = Offset(
                        x = (i+1) * size.width / (data.size-1),
                        y = (data[i+1] * -1 * size.height/2) + (size.height/2)
                    ),
                    color = color,
                    strokeWidth = strokeWidth
                )
            }
        }
    }
}