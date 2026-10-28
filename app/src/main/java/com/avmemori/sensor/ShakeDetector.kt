package com.avmemori.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * Ловит встряхивание телефона по акселерометру. Отдельных разрешений не нужно.
 * В покое сила ≈ 1 g, при встряхивании заметно больше.
 */
class ShakeDetector(context: Context, private val onShake: () -> Unit) : SensorEventListener {
    private val manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var lastShake = 0L

    fun start() {
        accelerometer?.let { manager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    fun stop() {
        manager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val gx = event.values[0] / SensorManager.GRAVITY_EARTH
        val gy = event.values[1] / SensorManager.GRAVITY_EARTH
        val gz = event.values[2] / SensorManager.GRAVITY_EARTH
        val gForce = sqrt(gx * gx + gy * gy + gz * gz)
        val now = System.currentTimeMillis()
        if (gForce > SHAKE_THRESHOLD_G && now - lastShake > COOLDOWN_MS) {
            lastShake = now
            onShake()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private companion object {
        const val SHAKE_THRESHOLD_G = 2.7f
        const val COOLDOWN_MS = 1500L
    }
}
