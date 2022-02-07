package com.example.tonetuner_v2.audio.audioSources

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.example.tonetuner_v2.app.AppModel.CAPTURE_BUFFER_SIZE
import com.example.tonetuner_v2.app.AppModel.SAMPLE_RATE
import com.example.tonetuner_v2.checkMicPermission
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
) : Runnable, AudioSource {
    private var recorder: AudioRecord? = null
    private var recordThread: Thread? = null
    private var running = false
    private val queue: BlockingQueue<Float> = ArrayBlockingQueue(bufferSize)

    init {
        /** Open the microphone and create the recording thread.*/

        // todo: Figure out how to properly get permissions from user
            // This works now, but I'm not sure why this is still red underlined

        startCapture()
    }

    /** Begin capturing and buffering audio */
    fun startCapture() {
        if (running) return

        if (!checkMicPermission(context)){
            running = false
            return
        }

        if (recorder == null){
            recorder = AudioRecord(
                MediaRecorder.AudioSource.DEFAULT,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_FLOAT,
                bufferSize
            )
        }


        if (recordThread == null ) {
            recordThread = Thread(this)
            recordThread?.start()
        }

        running = true
    }

    /**
     * Retrieve audio data from the buffer.
     * @param n The number of elements to take
     * @return An ArrayList of data elements
     */
    override fun getAudio(bufferSize: Int) =
        List<Float>(bufferSize) { queue.take() }


    /**
     * Audio capture thread entry point.
     *
     * Captures and buffers audio data.
     */
    override fun run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO)

        if (recorder?.state != AudioRecord.STATE_INITIALIZED) {
            Log.e("AudioProc", "Audio Record can't initialize!")
            return
        }
        recorder?.startRecording()

        Log.v("AudioProc", "Start recording")

        val audioBuffer = FloatArray(bufferSize)
        while (running) {

            // Fetch data from the microphone
            val numread = recorder?.read(audioBuffer, 0,
                audioBuffer.size, AudioRecord.READ_BLOCKING)

            // Put data into the blocking queue
            audioBuffer.forEach { queue.offer(it) }

            // Todo if something broke, uncomment this:
            // recordThread?.isAlive
        }
        recorder?.stop()
        recorder?.release()
    }
}
