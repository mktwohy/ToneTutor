package com.example.tonetuner_v2.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tonetuner_v2.*
import com.example.tonetuner_v2.app.AppModel
import com.example.tonetuner_v2.app.AppModel.FFT_MAX_FREQ
import com.example.tonetuner_v2.ui.composables.BarChart
import com.example.tonetuner_v2.ui.composables.CircularTuner
import com.example.tonetuner_v2.ui.composables.TapeMeter
import com.example.tonetuner_v2.ui.composables.old.XYPlot
import com.example.tonetuner_v2.ui.navigation.MainLayout.SpectrumType.*
import kotlin.math.ln

object MainLayout{
    enum class SpectrumType { FFT, FINGERPRINT }
}

@Composable
fun MainScreen(
    modifier: Modifier,
    navController: NavController,
    color: Color,
    spectrumType: MainLayout.SpectrumType
){
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
//        Button(
//            onClick = {
//                navController.navigate(Screen.DetailScreen.withArgs("Michael"))
//            }
//        ) {
//            Text(text = "To Detail Screen")
//        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.05f),
            contentAlignment = Alignment.BottomEnd
        ){
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.2f)
                    .fillMaxHeight()
                    .background(Color.DarkGray)
                    .clickable { AppModel.playState = !AppModel.playState  },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if(AppModel.playState) "ON" else "OFF",
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
        CircularTuner(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.3f),
            note = AppModel.note,
            centsErr = AppModel.cents
        )
        TapeMeter(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.3f)
                .border(2.dp, Color.White),
            value    = AppModel.quality,
            range    = 3,
            allowNegatives = false
        )
        Box(
            Modifier
                .fillMaxSize()
                .clickable { AppModel.changeSpectrumType() }) {
            when (spectrumType){
                FINGERPRINT -> BarChart(
                    modifier = Modifier.fillMaxSize(),
                    barValues = AppModel.fingerPrint.toFingerPrint(),
                    xTicks = List(AppModel.FINGERPRINT_SIZE){ i -> if (i == 0) 'f' else i+1 },
                    yTicks = (0.0f..1.0f).toList(0.1f).map { it.toString().substring(0..2) },
                    barColor = Color.Green,
                    tickColor = Color.White
                )
                FFT -> XYPlot(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.9f),
                    y = AppModel.fft.toGraphRepr()
                )
            }
        }
    }
}
