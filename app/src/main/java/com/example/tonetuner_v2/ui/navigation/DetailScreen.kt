package com.example.tonetuner_v2.ui.navigation

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.tonetuner_v2.app.AppModel
import com.example.tonetuner_v2.ui.composables.old.XYPlot

@Composable
fun DetailScreen(
    name: String?,
    navController: NavController
){
    Button(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.1f),
        onClick = {
            navController.navigate(Screen.MainScreen.route)
        }
    ) {
        Text(text = "$name, click here to go back to main")
    }
    XYPlot(
        modifier = Modifier.fillMaxSize(),
        y = AppModel.fft
    )
}