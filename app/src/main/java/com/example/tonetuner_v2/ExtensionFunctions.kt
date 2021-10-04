package com.example.tonetuner_v2

import androidx.compose.ui.graphics.Color
import com.example.tonetuner_v2.Util.logd
import kotlin.math.abs


fun Color.mix(that: Color) =
    Color(
        this.red/2 + that.red/2,
        this.green/2 + that.green/2,
        this.blue/2 + that.blue/2,
        this.alpha/2 + that.alpha/2
    )

operator fun Color.plus(that: Color) = this.mix(that)

fun Double.toNote(): Note{
    var i = 0
    while(i < Note.notes.size && this >= Note.notes[i].freq) {
        i++
    }

    val lowerNote = Note.notes[i]
    val upperNote = Note.notes[i+1]

    val lowerErr = abs(this - lowerNote.freq)
    val upperErr = abs(upperNote.freq - this)

    return if(lowerErr < upperErr) lowerNote else upperNote
}