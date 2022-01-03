package com.example.tonetuner_v2.ui.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun BarChart(
    modifier: Modifier,
    barValues: List<Float>,
    xTicks: List<Any>,
    yTicks: List<Any>,
    tickColor: Color,
    barColor: Color
){

    BoxWithConstraints(modifier) {
        Row{
            YAxis(
                modifier = Modifier
                    .fillMaxHeight(0.95f)
                    .fillMaxWidth(0.1f),
                ticks = yTicks
            )
            Column {
                BarChartNoAxis(
                    modifier = Modifier
                        .fillMaxHeight(0.95f)
                        .fillMaxWidth(),
                    barValues = barValues,
                    barColor = barColor
                )
                XAxis(
                    modifier = Modifier.fillMaxSize(),
                    ticks = xTicks,
                    color = tickColor
                )
            }
        }
    }
}

@Composable
fun BarChartNoAxis(
    modifier: Modifier,
    barValues: List<Float>,
    barColor: Color
){
    Box(modifier = modifier, contentAlignment = Alignment.Center){
        Canvas(modifier = Modifier
            .fillMaxHeight(0.9f)
            .fillMaxWidth()
        ) {
            val barWidth = this.size.width / barValues.size

            for(i in barValues.indices){
                val barHeight = barValues[i] * this.size.height
                drawRect(
                    topLeft = Offset(
                        (i / barValues.size.toFloat()) * this.size.width,
                        this.size.height - barHeight
                    ),
                    color = barColor,
                    size = Size(barWidth, barHeight),
                )
                drawRect(
                    topLeft = Offset(
                        (i / barValues.size.toFloat()) * this.size.width,
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

@Composable
fun XAxis(
    modifier: Modifier = Modifier,
    ticks: List<Any> = listOf('f',1,2,3,4,5,6,7,8,9,10),
    color: Color = Color.White
){
    BoxWithConstraints(modifier = modifier) {
        val tickWidth = this.maxWidth / ticks.size
        val tickHeight = this.maxHeight
        Row {
            for (t in ticks){
                Box(
                    modifier = Modifier.size(tickWidth, tickHeight),
                    contentAlignment = Alignment.Center
                ){
                    Text(
                        text = t.toString(),
                        color = color
                    )
                }
            }
        }
    }
}

@Composable
fun YAxis(
    modifier: Modifier,
    ticks: List<Any>,
    color: Color = Color.White
){
    BoxWithConstraints(modifier = modifier) {
        val tickWidth = this.maxWidth
        val tickHeight = this.maxHeight / ticks.size
        Column {
            for (t in ticks.reversed()){
                Box(
                    modifier = Modifier.size(tickWidth, tickHeight),
                    contentAlignment = Alignment.Center
                ){
                    Text(
                        text = t.toString(),
                        color = color
                    )
                }
            }
        }
    }
}




