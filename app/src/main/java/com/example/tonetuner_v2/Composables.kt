package com.example.tonetuner_v2

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun Meter(
    modifier: Modifier = Modifier,
    needleValue: Float = 0f,
    arcAngle: Float = 170f,
    arcWidth: Float = 5f,
    arcColor: Color = Color.Green,
){
    Canvas(
        modifier = modifier
    ){
        rotate(
            degrees = needleValue * arcAngle/2,
            pivot = Offset(this.size.width/2, this.size.height)
        ){
            drawLine(
                color = Color.White,
                start = Offset(this.size.width/2, this.size.height),
                end = Offset(this.size.width/2, 0f),
                strokeWidth = 2f
            )
        }
        drawArc(
            color = arcColor,
            startAngle = 270 - arcAngle/2,
            sweepAngle = arcAngle ,
            //topLeft = Offset(0f, this.size.height/50),
            size = Size(this.size.width, this.size.height*2f),
            useCenter = false,
            style = Stroke(width = arcWidth, cap = StrokeCap.Round),
        )

    }
}

@Composable
fun Tuner(
    note: Note,
    cents: Int,
    hz: Double,
){

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Pitch: ${hz.toString(5)}",
                color = Color.White
            )
            LinearProgressIndicator(hz.toFloat()/Note.B_8.freq)
            Text(text = "$note",color = Color.White)
            Text(text = "$cents",color = Color.White)
            Meter(
                modifier = Modifier.fillMaxWidth(0.25f).fillMaxHeight(0.25f),
                needleValue = cents/50f
            )
        }
}

@Composable
fun QualityMeter(
    quality: Double
){
    Text(
        text = "Quality: ${quality.toString(4)}",
        color = Color.White
    )
    LinearProgressIndicator(quality.toFloat()/7f)
}

@Composable
fun FftPlot(
    modifier: Modifier,
    harmonics: List<Harmonic>,
    color: Color = Color.Green,
    strokeWidth: Float = 3f
){
    XYPlot(
        modifier = modifier,
        x = harmonics.map { it.freq.toFloat() },
        y = harmonics.map { it.mag.toFloat() },
        color = color,
        strokeWidth = strokeWidth
    )
}

@Composable
fun XYPlot(
    modifier: Modifier,
    x: List<Float>,
    y: List<Float>,
    color: Color = Color.Green,
    strokeWidth: Float = 3f
){

}

@Composable
fun XYPlot(
    modifier: Modifier = Modifier,
    y: List<Float>,
    color: Color = Color.Green,
    strokeWidth: Float = 3f,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center){
        Canvas(modifier = Modifier
            .fillMaxHeight(0.9f)
            .fillMaxWidth()) {
            for(i in 0..y.size-2){
                drawLine(
                    start = Offset(
                        x = i * size.width / (y.size-1),
                        y = (y[i] * -1 * size.height/2) + (size.height/2)
                    ),
                    end = Offset(
                        x = (i+1) * size.width / (y.size-1),
                        y = (y[i+1] * -1 * size.height/2) + (size.height/2)
                    ),
                    color = color,
                    strokeWidth = strokeWidth
                )
            }
        }
    }
}

@Composable
fun NoteList(modifier: Modifier, closestNote: Note){
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LazyRow(
        modifier = modifier,
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(10.dp),

        ){
        coroutineScope.launch {
            listState.animateScrollToItem(Note.toList().indexOf(closestNote))
        }
        items(96){
            Text(
                text = "${Note.toList()[it]}",
                color = Color.White,
                fontSize = 40.sp
            )
        }
    }
}