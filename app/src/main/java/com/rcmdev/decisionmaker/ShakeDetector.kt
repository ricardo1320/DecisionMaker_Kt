package com.rcmdev.decisionmaker

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

private const val SHAKE_THRESHOLD_GRAVITY = 2.7f
private const val SHAKE_SLOP_TIME_MS = 500

/**
 * Class which implements SensorEventListener,
 * to detect when the phone shakes.
 */
class ShakeDetector: SensorEventListener {
    private var listener: OnShakeListener? = null
    private var shakeTimestamp: Long = 0

    fun setOnShakeListener(listener: OnShakeListener?){ this.listener = listener }

    interface OnShakeListener{
        fun onShake(speed: Float)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val gX = x / SensorManager.GRAVITY_EARTH
        val gY = y / SensorManager.GRAVITY_EARTH
        val gZ = z / SensorManager.GRAVITY_EARTH

        val gForce = sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()

        if (gForce > SHAKE_THRESHOLD_GRAVITY) {
            if (shakeTimestamp + SHAKE_SLOP_TIME_MS > now()) { return }
            shakeTimestamp = now()
            listener!!.onShake(gForce)
        }
    }

    private fun now(): Long{ return System.currentTimeMillis() }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}