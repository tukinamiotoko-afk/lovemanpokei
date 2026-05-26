package com.example.lovemanpo

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

enum class StepMode {
    STEP_COUNTER,
    ACCELEROMETER,
    UNSUPPORTED
}

interface StepListener {
    // 累計ではなく、増分を通知する方式に調整（ServiceやActivityで合算しやすいため）
    fun onStepAdded(count: Int)
}

class HybridStepTracker(
    private val context: Context,
    private val listener: StepListener
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var mode: StepMode = StepMode.UNSUPPORTED

    // STEP_COUNTER用
    private var lastCounterValue = -1f

    // ACCELEROMETER用
    private var lastStepTime = 0L
    private var prevMagnitude = 0f

    fun start() {
        mode = when {
            stepCounterSensor != null -> StepMode.STEP_COUNTER
            accelerometerSensor != null -> StepMode.ACCELEROMETER
            else -> StepMode.UNSUPPORTED
        }

        when (mode) {
            StepMode.STEP_COUNTER -> {
                sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI)
            }
            StepMode.ACCELEROMETER -> {
                sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME)
            }
            else -> {}
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> handleStepCounter(event)
            Sensor.TYPE_ACCELEROMETER -> handleAccelerometer(event)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun handleStepCounter(event: SensorEvent) {
        val currentValue = event.values[0]
        if (lastCounterValue < 0f) {
            lastCounterValue = currentValue
            return
        }
        val diff = (currentValue - lastCounterValue).toInt()
        if (diff > 0) {
            listener.onStepAdded(diff)
            lastCounterValue = currentValue
        }
    }

    private fun handleAccelerometer(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val magnitude = sqrt(x * x + y * y + z * z)
        val diff = magnitude - prevMagnitude
        prevMagnitude = magnitude

        val now = System.currentTimeMillis()
        if (diff > 1.2f && now - lastStepTime > 350) {
            lastStepTime = now
            listener.onStepAdded(1)
        }
    }
}
