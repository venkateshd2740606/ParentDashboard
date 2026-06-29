package com.parentdashboard.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object FeedbackHelper {

    private var toneGenerator: ToneGenerator? = null

    fun onPour(context: Context, hapticEnabled: Boolean, soundEnabled: Boolean) {
        if (hapticEnabled) vibrate(context)
        if (soundEnabled) playClickTone()
    }

    fun onPuzzleSolved(context: Context, hapticEnabled: Boolean, soundEnabled: Boolean) {
        if (hapticEnabled) vibrate(context, durationMs = 80)
        if (soundEnabled) playSuccessTone()
    }

    private fun vibrate(context: Context, durationMs: Long = 25) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(VibratorManager::class.java)
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        } ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(durationMs)
        }
    }

    private fun playClickTone() {
        runCatching {
            if (toneGenerator == null) {
                toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 60)
            }
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 40)
        }
    }

    private fun playSuccessTone() {
        runCatching {
            if (toneGenerator == null) {
                toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
            }
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 120)
        }
    }
}
