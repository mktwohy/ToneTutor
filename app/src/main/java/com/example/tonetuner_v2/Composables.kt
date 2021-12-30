package com.example.tonetuner_v2


import android.graphics.Paint
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
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.signallib.enums.PitchClass.*
import com.example.signallib.enums.Note
import com.example.signallib.enums.Note.Companion.plus
import com.example.signallib.enums.Note.Companion.minus
import com.example.tonetuner_v2.AppModel.NUM_HARMONICS
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
    Box(modifier){
        Canvas(modifier = Modifier.fillMaxSize()){
            val outerRadius = this.size.minDimension/2
            val innerRadius = outerRadius * 0.65f
            val centerRadius = outerRadius * 0.2f

            drawPie(
                numSlices = 12,
                radius = outerRadius,
                circleColor = Color.DarkGray,
                lineColor = Color.LightGray
            )
            drawCircle(
                color = Color.Gray,
                radius = innerRadius,
            )
            drawElementsInCircle(
                elements = Note.toList(octave = 1),
                radius = (outerRadius + innerRadius)/2,
                textPaint = noteTextPaint,
                customToString = { it.toPrettyString() }
            )
            if (note != null){
                drawPieNeedle(
                    angle = calcTunerAngle(note, centsErr),
                    sweepAngle = 360f/12,
                    radius = outerRadius,
                    color = Color.Green
                )
            }
            drawCircle(
                color = Color.DarkGray,
                radius = centerRadius,
            )
        }
        if (note != null){
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ){
                Text(
                    text = note.octave.toString(),
                    color = Color.White
                )
            }
        }
    }
}

fun calcTunerAngle(note: Note, cents: Int) =
    when(note.pitchClass){
        Ds   -> 0f
        E    -> 30f
        F    -> 60f
        Fs   -> 90f
        G    -> 120f
        Gs   -> 150f
        A    -> 180f
        As   -> 210f
        B    -> 240f
        C    -> 270f
        Cs   -> 300f
        D    -> 330f
} + (cents / 100f * 360f/12)

fun DrawScope.drawPieNeedle(
    angle: Float,
    sweepAngle: Float,
    radius: Float,
    color: Color,
    center: Offset = this.center
){
    rotate(angle){
        drawArc(
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius*2, radius*2),
            color = color,
            startAngle = -1f * (sweepAngle / 2f),
            sweepAngle = sweepAngle,
            useCenter = true,
            alpha = 0.4f
        )
    }

}

fun DrawScope.drawPie(
    numSlices: Int,
    radius: Float,
    circleColor: Color,
    lineColor: Color,
    strokeWidth: Float = 4f
){
    drawCircle(
        color = circleColor,
        radius = radius,
    )

    val pieSliceInnerAngle = 360f/numSlices
    for (i in 0 until numSlices){
        val rotateAngle = (i * pieSliceInnerAngle) + (pieSliceInnerAngle / 2)
        rotate(rotateAngle){
            drawLine(
                color = lineColor,
                start = this.center,
                end   = Offset(this.center.x, this.center.y - radius),
                strokeWidth = strokeWidth
            )
        }
    }
}

fun <T>DrawScope.drawElementsInCircle(
    elements: List<T>,
    radius: Float,
    textPaint: Paint,
    customToString: ((T) -> String)? = null
){
    val pieSliceInnerAngle = 360f/12f

    for (i in elements.indices){
        val centerSliceAngle = i * pieSliceInnerAngle
        val text: String =
            if (customToString == null)
                elements[i].toString()
            else
                customToString(elements[i])

        rotate(centerSliceAngle){
            drawIntoCanvas {
                it.nativeCanvas.drawText(
                    text,
                    this.center.x,
                    this.center.y - radius,
                    textPaint
                )
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
fun FingerPrint(
    modifier: Modifier = Modifier,
    fingerPrint: List<Harmonic>,
    color: Color = Color.Green,
) {
    if(fingerPrint.isEmpty()) return

    val f = fingerPrint.map { it.freq.toInt() to it.mag.toFloat() }.toMap()
    val bars = List(NUM_HARMONICS){ i -> f[i] ?: 0f }

    Box(modifier = modifier, contentAlignment = Alignment.Center){
        Canvas(modifier = Modifier
            .fillMaxHeight(0.9f)
            .fillMaxWidth()
        ) {
            val barWidth = this.size.width / bars.size

            for(i in bars.indices){
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

@Composable
fun NoteList(
    modifier: Modifier,
    note: Note?
){
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