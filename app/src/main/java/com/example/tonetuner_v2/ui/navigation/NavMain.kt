package com.example.tonetuner_v2.ui.navigation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.tonetuner_v2.app.AppModel
import com.example.tonetuner_v2.toFingerPrint
import com.example.tonetuner_v2.toList
import com.example.tonetuner_v2.ui.composables.BarChart
import com.example.tonetuner_v2.ui.composables.CircularTuner
import com.example.tonetuner_v2.ui.composables.TapeMeter

@Composable
fun NavMain(modifier: Modifier, color: Color){
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
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
        BarChart(
            modifier = Modifier.fillMaxSize(),
            barValues = AppModel.fingerPrint.toFingerPrint(),
            xTicks = List(AppModel.NUM_HARMONICS){ i -> if (i == 0) 'f' else i+1 },
            yTicks = (0.0f..1.0f).toList(0.1f).map { it.toString().substring(0..2) },
            barColor = Color.Green,
            tickColor = Color.White
        )
    }
}