package com.example.tonetuner_v2.ui.navigation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tonetuner_v2.app.AppModel
import com.example.tonetuner_v2.ui.composables.*

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
    ){
// simple test for android navigation.
//        Button(
//            onClick = {
//                navController.navigate(Screen.DetailScreen.withArgs("Michael"))
//            }
//        ) {
//            Text(text = "To Detail Screen")
//        }
        // On/Off/Freeze button
        FreezeButton(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.05f)
        )
        CircularTuner(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.3f),
            note = AppModel.note,
            cents = AppModel.cents
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
        FftOrSpectrumViewer(modifier = Modifier.fillMaxSize())
    }
}
