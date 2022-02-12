package com.example.tonetuner_v2.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.example.tonetuner_v2.app.AppModel

@Composable
fun FreezeButton(
    modifier: Modifier
){
    Box(
        modifier = modifier,
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
}