package com.example.tonetuner_v2

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.lang.StringBuilder

@Composable
fun Tuner(
    note: Note,
    cents: Int
){
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = note.toString(), color = Color.White)
        LinearProgressIndicator(
            progress = cents/50f
        )

    }
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