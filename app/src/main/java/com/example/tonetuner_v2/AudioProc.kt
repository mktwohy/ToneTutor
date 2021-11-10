package com.example.tonetuner_v2

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

/**
 * AudioProc.java - Opens the microphone and buffers audio data
 * @author gtruch
 */
class AudioProc : Runnable {
    val audioData
        get() = { n: Int -> (0 until n).map { queue.take() } }
    val SAMPLE_RATE = 44100

    private var record: AudioRecord? = null
    private var recordThread: Thread? = null
    private var running = false
    private val queue: BlockingQueue<Double> = ArrayBlockingQueue(4096)

    init {
        /**
         * Open the microphone and create the recording thread.
         */
        var bufSize = 2

        val min_buf_size = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                                                    AudioFormat.CHANNEL_IN_MONO,
                                                    AudioFormat.ENCODING_PCM_FLOAT)

        bufSize = 2048


        record = AudioRecord(MediaRecorder.AudioSource.DEFAULT,SAMPLE_RATE,
                                 AudioFormat.CHANNEL_IN_MONO,
                                 AudioFormat.ENCODING_PCM_FLOAT,
                                 bufSize)

        recordThread = Thread(this)
    }

    /**
     * Begin capturing and buffering audio
     */
    fun startCapture() {
        running = true
        recordThread?.start()
    }


    /**
     * Retrieve audio data from the buffer.
     * @param n The number of elements to take
     * @return An ArrayList of data elements
     */
    val getAudioData = { n: Int -> (0 until n).map { queue.take() } }

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

        val audioBuffer = FloatArray(2048)
        while (running) {

            // Fetch data from the microphone
            val numread = record?.read(audioBuffer, 0,
                    audioBuffer.size, AudioRecord.READ_BLOCKING)

            // Put data into the blocking queue
            audioBuffer.forEach { queue.offer(it.toDouble()) }


            recordThread?.isAlive
        }
        record?.stop()
        record?.release()
    }

}
