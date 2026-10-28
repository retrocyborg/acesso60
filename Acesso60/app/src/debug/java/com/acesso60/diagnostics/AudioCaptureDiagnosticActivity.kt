package com.acesso60.diagnostics

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import org.json.JSONObject
import java.io.File
import java.io.RandomAccessFile
import kotlin.concurrent.thread
import kotlin.math.abs
import kotlin.math.sqrt

/** Diagnóstico exclusivo do build debug; não faz parte do APK de produção. */
class AudioCaptureDiagnosticActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val durationSeconds = intent.getIntExtra(EXTRA_DURATION_SECONDS, 5).coerceIn(1, 30)
        thread(name = "acesso60-audio-diagnostic") {
            runCatching { capture(durationSeconds) }
                .onFailure { writeFailure(it) }
            runOnUiThread { finish() }
        }
    }

    private fun capture(durationSeconds: Int) {
        check(checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            "RECORD_AUDIO not granted"
        }
        val outputDir = requireNotNull(getExternalFilesDir("audio-diagnostics"))
        val wavFile = File(outputDir, WAV_NAME)
        val summaryFile = File(outputDir, SUMMARY_NAME)
        wavFile.delete()
        summaryFile.delete()

        val minimum = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_MASK, ENCODING)
        check(minimum > 0) { "Invalid AudioRecord buffer size: $minimum" }
        val bufferSize = maxOf(minimum, 4096)
        val recorder = AudioRecord.Builder()
            .setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(ENCODING)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(CHANNEL_MASK)
                    .build(),
            )
            .setBufferSizeInBytes(bufferSize)
            .build()
        check(recorder.state == AudioRecord.STATE_INITIALIZED) { "AudioRecord initialization failed" }

        val targetSamples = SAMPLE_RATE * durationSeconds
        val samples = ShortArray(bufferSize / 2)
        var capturedSamples = 0
        var peak = 0
        var nonZero = 0L
        var sumSquares = 0.0

        RandomAccessFile(wavFile, "rw").use { output ->
            writeWaveHeader(output, 0)
            recorder.startRecording()
            try {
                while (capturedSamples < targetSamples) {
                    val requested = minOf(samples.size, targetSamples - capturedSamples)
                    val read = recorder.read(samples, 0, requested, AudioRecord.READ_BLOCKING)
                    check(read >= 0) { "AudioRecord read failed: $read" }
                    if (read == 0) continue
                    repeat(read) { index ->
                        val sample = samples[index].toInt()
                        val absolute = abs(sample)
                        if (absolute > peak) peak = absolute
                        if (sample != 0) nonZero++
                        sumSquares += sample.toDouble() * sample.toDouble()
                        output.write(sample and 0xff)
                        output.write((sample shr 8) and 0xff)
                    }
                    capturedSamples += read
                }
            } finally {
                recorder.stop()
                recorder.release()
            }
            writeWaveHeader(output, capturedSamples * 2)
        }

        val rms = if (capturedSamples > 0) sqrt(sumSquares / capturedSamples) else 0.0
        summaryFile.writeText(
            JSONObject()
                .put("status", "captured")
                .put("duration_seconds", capturedSamples.toDouble() / SAMPLE_RATE)
                .put("sample_rate_hz", SAMPLE_RATE)
                .put("channels", 1)
                .put("bits_per_sample", 16)
                .put("samples", capturedSamples)
                .put("peak", peak)
                .put("peak_normalized", peak / 32768.0)
                .put("rms", rms)
                .put("rms_normalized", rms / 32768.0)
                .put("non_zero_samples", nonZero)
                .put("has_non_zero_samples", nonZero > 0)
                .toString(2),
        )
    }

    private fun writeFailure(error: Throwable) {
        val outputDir = getExternalFilesDir("audio-diagnostics") ?: return
        File(outputDir, SUMMARY_NAME).writeText(
            JSONObject()
                .put("status", "failed")
                .put("error", error.message ?: error::class.java.simpleName)
                .toString(2),
        )
    }

    private fun writeWaveHeader(output: RandomAccessFile, dataSize: Int) {
        output.seek(0)
        output.writeBytes("RIFF")
        writeLittleEndianInt(output, 36 + dataSize)
        output.writeBytes("WAVEfmt ")
        writeLittleEndianInt(output, 16)
        writeLittleEndianShort(output, 1)
        writeLittleEndianShort(output, 1)
        writeLittleEndianInt(output, SAMPLE_RATE)
        writeLittleEndianInt(output, SAMPLE_RATE * 2)
        writeLittleEndianShort(output, 2)
        writeLittleEndianShort(output, 16)
        output.writeBytes("data")
        writeLittleEndianInt(output, dataSize)
    }

    private fun writeLittleEndianInt(output: RandomAccessFile, value: Int) {
        repeat(4) { byte -> output.write((value shr (8 * byte)) and 0xff) }
    }

    private fun writeLittleEndianShort(output: RandomAccessFile, value: Int) {
        repeat(2) { byte -> output.write((value shr (8 * byte)) and 0xff) }
    }

    private companion object {
        const val EXTRA_DURATION_SECONDS = "duration_seconds"
        const val SAMPLE_RATE = 48_000
        const val CHANNEL_MASK = AudioFormat.CHANNEL_IN_MONO
        const val ENCODING = AudioFormat.ENCODING_PCM_16BIT
        const val WAV_NAME = "usb4-test.wav"
        const val SUMMARY_NAME = "capture-summary.json"
    }
}
