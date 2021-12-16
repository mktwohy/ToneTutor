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
import androidx.compose.material.Button
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.signallib.Note
import com.example.signallib.Note.Companion.minus
import com.example.signallib.Note.Companion.plus
import com.example.tonetuner_v2.ui.theme.noteTextPaint
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
        Canvas(Modifier.fillMaxSize()){
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
fun Gauge(
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
fun CircularTunerTest(){
    var note by remember { mutableStateOf(Note.A_4) }
    var sliderState by remember { mutableStateOf(0.5f) }
    
    fun sliderToCents() = ((sliderState - 0.5f) * 200).toInt()
    
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        CircularTuner(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f),
            note = note,
            centsErr = sliderToCents()
        )
        Text(
            text = "Note: $note \nCents: ${sliderToCents()}",
            color = Color.White,
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.fillMaxHeight(0.25f))

        Row{
            Button(onClick = { note -= 1 }) {
                Text(text = "Prev Note", color = Color.White)
            }
            Button(onClick = { note = Note.random() }) {
                Text(text = "Random Note", color = Color.White)
            }
            Button(onClick = { note += 1 }) {
                Text(text = "Prev Note", color = Color.White)
            }
        }


        Spacer(modifier = Modifier.fillMaxHeight(0.25f))

        Slider(value = sliderState, onValueChange = { sliderState = it })
    }
}

// todo what if you could turn off certain notes? it would limit what notes the pitch algo tests
@Composable
fun CircularTuner(
    modifier: Modifier = Modifier,
    note: Note?,
    centsErr: Int,
) {
    val pieSliceInnerAngle = 360f/12f

    Box(modifier){
        Canvas(modifier = Modifier.fillMaxSize()){
            val radius = this.size.minDimension/2
            val innerRadius = radius * 0.65f
            val centerRadius = radius * 0.2f

            drawCircle(
                color = Color.DarkGray,
                radius = radius,
            )
            drawCircle(
                color = Color.Gray,
                radius = innerRadius,
            )
            for (i in 0 until 12){
                val centerSliceAngle = i * pieSliceInnerAngle
                val rightSliceAngle = centerSliceAngle + (pieSliceInnerAngle / 2)
                rotate(rightSliceAngle){
                    drawLine(
                        color = Color.Gray,
                        start = Offset(this.center.x, this.center.y - innerRadius),
                        end   = Offset(this.center.x, this.center.y - radius),
                        strokeWidth = 4f
                    )
                }
                rotate(centerSliceAngle){
                    drawIntoCanvas {
                        it.nativeCanvas.drawText(
                            Note.notes[i].toPrettyString(),
                            this.center.x,
                            this.center.y - (radius + innerRadius)/2,
                            noteTextPaint
                        )
                    }
                }
            }
            if (note != null){
                val tunerAngle =
                    when(note.toPrettyString()){
                        "C"     -> 0f
                        "C#"    -> 30f
                        "D"     -> 60f
                        "D#"    -> 90f
                        "E"     -> 120f
                        "F"     -> 150f
                        "F#"    -> 180f
                        "G"     -> 210f
                        "G#"    -> 240f
                        "A"     -> 270f
                        "A#"    -> 300f
                        "B"     -> 330f
                        else    -> Float.NaN
                    } + (centsErr / 100f * pieSliceInnerAngle)

                rotate(tunerAngle){
                    drawLine(
                        color = Color.Green,
                        start = this.center,
                        end   = Offset(this.center.x, this.center.y - radius),
                        strokeWidth = 4f
                    )
                }
            }
            drawCircle(
                color = Color.DarkGray,
                radius = centerRadius,
            )
        }
        if (note != null){
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                Text(text = note.name[2].toString(), color = Color.White)
            }
        }
    }

}


@Composable
fun GaugeTuner(
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
        Gauge(
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