package com.example.tonetuner_v2.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.signallib.enums.Note
import com.example.tonetuner_v2.Harmonic
import com.example.tonetuner_v2.audio.audioProcessing.AudioProc
import com.example.tonetuner_v2.normalize
import com.example.tonetuner_v2.normalizeBySum
import com.example.tonetuner_v2.toNoteAndCents
import com.example.tonetuner_v2.ui.navigation.MainLayout
import com.example.tonetuner_v2.ui.navigation.MainLayout.SpectrumType.*

object AppModel{
    // State
    var fft by mutableStateOf(listOf<Float>())
    var fingerPrint by mutableStateOf(listOf<Harmonic>())
    var pitch by mutableStateOf(0.0)
    var quality by mutableStateOf(0.0)
    var note by mutableStateOf<Note?>(null)
    var cents by mutableStateOf(0)

    var spectrumType by mutableStateOf(FFT)

    // Settings (requires app restart)
    const val PROC_BUFFER_SIZE      = 2048    // values < 512 cause crash
    const val CAPTURE_BUFFER_SIZE   = 2048
    const val SAMPLE_RATE           = 44100
    const val UI_LAG                = 15L
    const val FFT_QUEUE_SIZE        = 7
    const val FINGERPRINT_QUEUE_SIZE= 7
    const val QUALITY_QUEUE_SIZE    = 30
    const val PITCH_QUEUE_SIZE      = 3
    const val NOISE_THRESHOLD       = 0.03f
    const val NUM_HARMONICS         = 25
    const val TEST_MODE             = false
    val NOTE_RANGE = Note.toList(Note.C_1, Note.E_6) // drop C (bass) high E string (guitar)

    fun changeSpectrumType(){
        spectrumType = if (spectrumType == FFT) FINGERPRINT else FFT
    }

    fun updateAppModel(audioProc: AudioProc){
        pitch       = audioProc.pitch
        quality     = audioProc.quality
        fingerPrint = audioProc.fingerPrint
        fft = audioProc.fft.run {
            map { it.toFloat() }.normalizeBySum()
        }
        val (newNote, newCents) = pitch.toNoteAndCents()
        note = newNote
        cents = newCents
    }
}