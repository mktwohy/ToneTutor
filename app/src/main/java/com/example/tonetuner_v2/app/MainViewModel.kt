package com.example.tonetuner_v2.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.signallib.enums.Note
import com.example.tonetuner_v2.audio.audioProcessing.AudioProc
import com.example.tonetuner_v2.audio.audioProcessing.Harmonic
import com.example.tonetuner_v2.util.freqToNoteAndCents

class MainViewModel : ViewModel() {
    var fft by mutableStateOf(listOf<Harmonic>())
    var fingerPrint by mutableStateOf(listOf<Harmonic>())
    var pitch by mutableStateOf(0f)
    var quality by mutableStateOf(0f)
    var note by mutableStateOf<Note?>(null)
    var cents by mutableStateOf(0)
    var isFrozen by mutableStateOf(false)
    var color by mutableStateOf(Color.Green)
    var spectrumType by mutableStateOf(SpectrumType.FINGERPRINT)

    fun update(audioProc: AudioProc) {
        pitch = audioProc.pitch
        quality = audioProc.quality
        fingerPrint = audioProc.fingerPrint
        fft = audioProc.fft
        val (newNote, newCents) = freqToNoteAndCents(pitch)
        note = newNote
        cents = newCents
    }

    enum class SpectrumType { FFT, FINGERPRINT }
}