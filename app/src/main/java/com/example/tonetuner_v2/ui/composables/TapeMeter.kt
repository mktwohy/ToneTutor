package com.example.tonetuner_v2.ui.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.unit.dp

@Composable
fun TapeMeter(
    modifier: Modifier = Modifier,
    value: Float = 1.9f,
    range: Int = 5,
    allowNegatives: Boolean = true
) {
    if (value.isNaN()) return

    val minValue = value.toInt() - range
    val maxValue = value.toInt() + range
    val numElements = 2 * range + 1

    BoxWithConstraints(
        modifier = modifier
            .border(2.dp, Color.Black)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        val cellHeight = this.maxHeight
        val cellWidthDP = this.maxWidth / numElements
        val offset = (cellWidthDP * (value - value.toInt()).toFloat()).times(-1f)

        // draw gradient
        Box(
            Modifier
                .size(this.maxWidth + 1.dp - (cellWidthDP.times(2f)), this.maxHeight)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Black, Color.Gray, Color.Transparent, Color.Gray, Color.Black
                        ),
                        tileMode = TileMode.Repeated
                    )
                ),
        )

        // draw "tape"
        Row(
            Modifier
                .fillMaxSize()
                .offset(offset)
        ) {
            for (i in minValue..maxValue) {
                Box(
                    modifier = Modifier
                        .size(cellWidthDP, cellHeight)
                        .border(1.dp, Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    val text = if (!allowNegatives && i < 0) "" else "$i"
                    Text(text = text, color = Color.Black)
                }
            }
        }

        // draw walls of window
        Canvas(Modifier.fillMaxSize()) {
            val cellWidthFloat = this.size.width / numElements

            drawLine(
                color = Color.Red,
                start = Offset(this.size.width / 2, 0f),
                end = Offset(this.size.width / 2, this.size.height),
                strokeWidth = 4f
            )
            drawRect(
                color = Color.Black,
                topLeft = Offset(0f, 0f),
                size = Size(cellWidthFloat, this.size.height)
            )
            drawRect(
                color = Color.Black,
                topLeft = Offset(this.size.width - cellWidthFloat, 0f),
                size = Size(this.size.width / numElements, this.size.height)
            )
        }
    }
}

@Composable
fun TestTapeMeter() {
    var sliderState by remember { mutableStateOf(0.5f) }

    Column {
        Text(
            text = "Value: ${sliderState * 50}",
            color = Color.White
        )
        TapeMeter(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize(.2f),
            value = (sliderState * 50)
        )
        Slider(
            value = sliderState,
            onValueChange = { sliderState = it }
        )
    }
}
