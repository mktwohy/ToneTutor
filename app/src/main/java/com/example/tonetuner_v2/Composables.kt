package com.example.tonetuner_v2


import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.signallib.Note
import kotlinx.coroutines.launch
import kotlin.math.ln

@Composable
fun TestTapeMeter(){
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
            value    = (sliderState * 50).toDouble()
        )
        Slider(
            value = sliderState,
            onValueChange = { sliderState = it }
        )
    }
}

@Composable
fun TapeMeter(
    modifier: Modifier = Modifier,
    value: Double = 1.9,
    range: Int = 5,
    allowNegatives: Boolean = true
){
    if(value.isNaN()) return

    val minValue = value.toInt() - range
    val maxValue = value.toInt() + range
    val numElements = 2 * range + 1


    BoxWithConstraints(
        modifier = modifier
            .border(2.dp, Color.Black)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        val cellHeight  = this.maxHeight
        val cellWidthDP   = this.maxWidth / numElements
        val offset      = (cellWidthDP * (value - value.toInt()).toFloat()).times(-1f)

        // draw gradient
        Box(
            modifier = Modifier
                .size(this.maxWidth  + 1.dp - (cellWidthDP.times(2f)), this.maxHeight)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Black, Color.Gray, Color.Transparent, Color.Gray, Color.Black),
                        tileMode = TileMode.Repeated
                    )
                ),
        )
        // draw "tape"
        Row(
            Modifier
                .fillMaxSize()
                .offset(offset)) {
            for (i in minValue..maxValue){
                Box(
                    modifier = Modifier
                        .size(cellWidthDP, cellHeight)
                        .border(1.dp, Color.Black),
                    contentAlignment = Alignment.Center
                ){
                    val text = if (!allowNegatives && i < 0) "" else "$i"
                    Text(text = text, color = Color.Black)
                }

            }
        }
        // draw walls of window
        Canvas(
            modifier = Modifier.fillMaxSize()
        ){
            val cellWidthFloat = this.size.width / numElements

            drawLine(
                color   = Color.Red,
                start   = Offset(this.size.width/2, 0f),
                end     = Offset(this.size.width/2, this.size.height),
                strokeWidth = 4f
            )
            drawRect(
                color = Color.Black,
                topLeft = Offset(0f,0f),
                size    = Size(cellWidthFloat, this.size.height)
            )
            drawRect(
                color = Color.Black,
                topLeft = Offset(this.size.width - cellWidthFloat,0f),
                size    = Size(this.size.width / numElements, this.size.height)
            )
        }
    }

}

@Composable
fun Meter(
    modifier: Modifier = Modifier,
    needleValue: Float = 0f,
    arcAngle: Float = 150f,
    arcWidth: Float = 5f,
    arcColor: Color = Color.Green,
    aspectRatio: Float = 2/1f
){
    val animNeedle by animateFloatAsState(
        targetValue = needleValue,
        animationSpec = tween(durationMillis = 100)
    )
    val animColor by animateColorAsState(
        targetValue = arcColor,
        animationSpec = tween(durationMillis = 200)
    )

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.size(this.maxWidth, this.maxWidth*(1/aspectRatio))
        ){
            val bottomCenter = Offset(this.size.width/2, this.size.height)
            rotate(
                degrees = animNeedle * arcAngle/2,
                pivot = bottomCenter
            ){
                drawLine(
                    color = Color.White,
                    start = bottomCenter,
                    end = Offset(this.size.width/2, 0f),
                    strokeWidth = 2f
                )
            }
            drawCircle(color = Color.White, radius = 5f, center = bottomCenter)
            drawArc(
                color = animColor,
                startAngle = 270 - arcAngle/2,
                sweepAngle = arcAngle ,
                //topLeft = Offset(0f, this.size.height/50),
                size = Size(this.size.width, this.size.height*2f),
                useCenter = false,
                style = Stroke(width = arcWidth, cap = StrokeCap.Round),
            )
        }
    }

}

@Composable
fun Tuner(
    note: Note?,
    cents: Int,
    hz: Double,
    nullNoteMessage: String = "N/A"
){
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = if(note == null) nullNoteMessage else "$note",
            color = Color.White
        )
        Meter(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .fillMaxHeight(0.15f),
            needleValue = cents/50f,
            arcColor = if(note != null) Color.Green else Color.Red
        )
        Text(text = "$cents",color = Color.White)
        Row {
            Text(
                text = "Pitch: ${hz.toString(5)}",
                color = Color.White
            )
            LinearProgressIndicator( ln(hz.toFloat() + Note.C_0.freq) / Note.B_8.freq )
        }
    }
}

@Composable
fun QualityMeter(
    quality: Double
){
    Row {
        Text(
            text = "Quality: ${quality.toString(4)}",
            color = Color.White
        )
        LinearProgressIndicator(quality.toFloat()/7f)
    }
}


@Composable
fun XYPlot(
    modifier: Modifier = Modifier,
    y: List<Float>,
    color: Color = Color.Green,
    strokeWidth: Float = 3f,
) {
    if(y.isNotEmpty()){
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
                        color = if(AppModel.note != null) color else Color.Red,
                        strokeWidth = strokeWidth
                    )
                }
            }
        }
    }
}

@Composable
fun NoteList(modifier: Modifier, note: Note?){
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LazyRow(
        modifier = modifier,
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(10.dp),

        ){
        coroutineScope.launch {
            listState.animateScrollToItem(Note.toList().indexOf(note))
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