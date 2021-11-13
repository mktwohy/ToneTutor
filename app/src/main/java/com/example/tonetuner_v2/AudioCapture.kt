package com.example.tonetuner_v2

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.example.tonetuner_v2.AppModel.CAPTURE_BUFFER_SIZE
import com.example.tonetuner_v2.AppModel.SAMPLE_RATE
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

/**
 * Opens the microphone and buffers audio data
 * @author gtruch
 */
class AudioCapture(
    val sampleRate: Int = SAMPLE_RATE,
    val bufferSize: Int = CAPTURE_BUFFER_SIZE,
) : Runnable {
    private var record: AudioRecord? = null
    private var recordThread: Thread? = null
    private var running = false
    private val queue: BlockingQueue<Double> = ArrayBlockingQueue(bufferSize)

    init {
        /** Open the microphone and create the recording thread.*/

        // todo: Figure out how to properly get permissions from user
            // This works now, but I'm not sure why this is still red underlined
        record = AudioRecord(
            MediaRecorder.AudioSource.DEFAULT,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_FLOAT,
            bufferSize)

        recordThread = Thread(this)
    }

    /** Begin capturing and buffering audio */
    fun startCapture() {
        running = true
        recordThread?.start()
    }

    /**
     * Retrieve audio data from the buffer.
     * @param n The number of elements to take
     * @return An ArrayList of data elements
     */
    fun getAudioData(bufferSize: Int) =
        List<Double>(bufferSize) { queue.take() }


    /**
     * Audio capture thread entry point.
     *
     * Captures and buffers audio data.
     */
    override fun run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO)

        if (record?.state != AudioRecord.STATE_INITIALIZED) {
            Log.e("AudioProc", "Audio Record can't initialize!")
            return
        }
        record?.startRecording()

        Log.v("AudioProc", "Start recording")

        val audioBuffer = FloatArray(bufferSize)
        while (running) {

            // Fetch data from the microphone
            val numread = record?.read(audioBuffer, 0,
                audioBuffer.size, AudioRecord.READ_BLOCKING)

            // Put data into the blocking queue
            audioBuffer.forEach { queue.offer(it.toDouble()) }

            // Todo if something broke, uncomment this:
            // recordThread?.isAlive
        }
        record?.stop()
        record?.release()
    }
}
