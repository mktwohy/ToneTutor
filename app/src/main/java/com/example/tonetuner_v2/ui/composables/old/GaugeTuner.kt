package com.example.tonetuner_v2.ui.composables.old

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.signallib.enums.Note
import com.example.tonetuner_v2.util.toString
import kotlin.math.ln

@Composable
fun GaugeTuner(
    note: Note?,
    cents: Int,
    hz: Float,
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