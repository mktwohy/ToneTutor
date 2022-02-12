package com.example.tonetuner_v2.audio.audioSources

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.example.tonetuner_v2.app.AppModel.CAPTURE_BUFFER_SIZE
import com.example.tonetuner_v2.app.AppModel.SAMPLE_RATE
import com.example.tonetuner_v2.util.checkMicPermission
import com.example.tonetuner_v2.util.logd
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

/**
 * Opens the microphone and buffers audio data
 * @author gtruch
 */
class MicSource(
    val context: Context,
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
    fun startCapture() {
        logd("attempt start")
        if (running) {
            logd("already running")
            return
        }

        if (!checkMicPermission(context)){
            logd("permission denied")
            running = false
            return
        }

        running = true
        Thread {
            logd("thread start")
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
                ar.read(buffer, 0,
                    buffer.size, AudioRecord.READ_BLOCKING)

                // Put data into the blocking queue
                buffer.forEach { queue.offer(it) }
            }
            ar.stop()
            ar.release()
            logd("thread stop")
        }.start()

    }

    fun stopCapture(){ running = false }
}
