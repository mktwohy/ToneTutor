package com.example.tonetuner_v2.ui.composables

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.sp
import com.example.signallib.enums.Note
import com.example.signallib.enums.Note.Companion.minus
import com.example.signallib.enums.Note.Companion.plus
import com.example.signallib.enums.PitchClass
import com.example.tonetuner_v2.ui.theme.noteTextPaint


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
                elements = PitchClass.classes,
                radius = (outerRadius + innerRadius)/2,
                textPaint = noteTextPaint,
                customToString = { it.name.replace('s','#') }
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


@Composable
fun TestCircularTuner(){
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

fun calcTunerAngle(note: Note, cents: Int) =
    when(note.pitchClass){
        PitchClass.Ds -> 0f
        PitchClass.E -> 30f
        PitchClass.F -> 60f
        PitchClass.Fs -> 90f
        PitchClass.G -> 120f
        PitchClass.Gs -> 150f
        PitchClass.A -> 180f
        PitchClass.As -> 210f
        PitchClass.B -> 240f
        PitchClass.C -> 270f
        PitchClass.Cs -> 300f
        PitchClass.D -> 330f
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

fun <T> DrawScope.drawElementsInCircle(
    elements: List<T>,
    radius: Float,
    textPaint: Paint,
    customToString: ((T) -> String)? = null
) {
    val pieSliceInnerAngle = 360f / 12f

    for (i in elements.indices) {
        val centerSliceAngle = i * pieSliceInnerAngle
        val text: String =
            if (customToString == null)
                elements[i].toString()
            else
                customToString(elements[i])

        rotate(centerSliceAngle) {
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
