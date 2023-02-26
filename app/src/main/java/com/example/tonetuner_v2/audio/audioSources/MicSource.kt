package com.example.tonetuner_v2.audio.audioSources

import android.Manifest
import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import com.example.tonetuner_v2.app.AppSettings.CAPTURE_BUFFER_SIZE
import com.example.tonetuner_v2.app.AppSettings.SAMPLE_RATE
import com.example.tonetuner_v2.extensions.hasPermission
import com.example.tonetuner_v2.util.ContextHolder
import com.example.tonetuner_v2.util.Logger
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

/**
 * Opens the microphone and buffers audio data
 * @author gtruch
 */
@SuppressLint("MissingPermission")
class MicSource(
    val sampleRate: Int = SAMPLE_RATE,
    val bufferSize: Int = CAPTURE_BUFFER_SIZE,
) : AudioSource {
    private var running = false
    private val queue: BlockingQueue<Float> = ArrayBlockingQueue(bufferSize)

    init { startCapture() }

    override fun getAudio(bufferSize: Int) =
        List<Float>(bufferSize) { queue.take() }

    /**
     * Begin capturing and buffering audio. Must grant RECORD_AUDIO permission before calling
     * this function. If permission is denied, audio will not be captured
     * */
    @RequiresPermission("android.permission.RECORD_AUDIO")
    fun startCapture() {
        Logger.i("Attempt start MicSource")
        if (running) {
            Logger.i("MicSource already running")
            return
        }

        if (!ContextHolder.get().hasPermission(Manifest.permission.RECORD_AUDIO)) {
            Logger.e("Microphone permission denied")
            running = false
            return
        }

        running = true
        Thread {
            Logger.i("Start microphone capture")
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO)

            val buffer = FloatArray(bufferSize)
            val ar = AudioRecord(
                MediaRecorder.AudioSource.DEFAULT,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_FLOAT,
                bufferSize
            )

            ar.startRecording()
            while (running) {
                // Fetch data from the microphone
                ar.read(
                    buffer, 0,
                    buffer.size, AudioRecord.READ_BLOCKING
                )

                // Put data into the blocking queue
                buffer.forEach { queue.offer(it) }
            }
            ar.stop()
            ar.release()
            Logger.i("End microphone capture")
        }.start()
    }

    fun stopCapture() { running = false }
}
