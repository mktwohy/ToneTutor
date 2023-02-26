package com.example.tonetuner_v2.ui.navigation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.tonetuner_v2.app.MainViewModel
import com.example.tonetuner_v2.ui.composables.CircularTuner
import com.example.tonetuner_v2.ui.composables.FftOrSpectrumViewer
import com.example.tonetuner_v2.ui.composables.FreezeButton
import com.example.tonetuner_v2.ui.composables.TapeMeter



@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier,
//    navController: NavController,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
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
            isFrozen = viewModel.isFrozen,
            onIsFrozenChange = { viewModel.isFrozen = it },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.05f)
        )
        CircularTuner(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.3f),
            note = viewModel.note,
            centsErr = viewModel.cents
        )
        TapeMeter(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.3f)
                .border(2.dp, Color.White),
            value = viewModel.quality,
            range = 3,
            allowNegatives = false
        )
        FftOrSpectrumViewer(
            fingerPrint = viewModel.fingerPrint,
            fft = viewModel.fft,
            spectrumType = viewModel.spectrumType,
            onSpectrumTypeChange = {
                viewModel.spectrumType = it
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
