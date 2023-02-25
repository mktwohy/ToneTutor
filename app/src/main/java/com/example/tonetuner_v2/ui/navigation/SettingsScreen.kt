package com.example.tonetuner_v2.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun SettingsScreen(
    name: String?,
    navController: NavController
) {
    Column {
        Button(
            onClick = {
                navController.navigate(Screen.MainScreen.route)
            }
        ) {
            Text(text = "Back")
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "SETTINGS SCREEN",
                color = Color.White,
                fontSize = 50.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
