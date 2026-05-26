package com.example.lovemanpo

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.lovemanpo.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class StepCounterService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    private var stepDetectorSensor: Sensor? = null
    private lateinit var repository: StepRepository
    private lateinit var stepDao: StepDao

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val handler = Handler(Looper.getMainLooper())

    private var todayStepsCached: Int = 0
    private var lastCounterValue: Int? = null

    private var walkingStartTime: Long = 0L
    private var lastStepTime: Long = 0L
    private var todayWalkingTimeMs: Long = 0L
    private var sessionTimeAlreadyRecordedMs: Long = 0L
    private val walkingTimeoutMs = 10000L

    companion object {
        private const val CHANNEL_ID = "step_counter_channel"
        private const val NOTIFICATION_ID = 1001
        private const val TAG = "StepCounterService"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")

        createNotificationChannel()
        startServiceForeground()

        val database = AppDatabase.getDatabase(this)
        stepDao = database.stepDao()
        val prefs = getSharedPreferences("lovemanpo_prefs", Context.MODE_PRIVATE)
        repository = StepRepository(stepDao, prefs)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        initializeData()
        registerStepSensors()

        handler.post(walkingCheckRunnable)
    }

    private fun startServiceForeground() {
        val initialNotification = createNotification(todayStepsCached)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                initialNotification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
            )
        } else {
            startForeground(NOTIFICATION_ID, initialNotification)
        }
    }

    private fun initializeData() {
        val today = LocalDate.now().toString()
        serviceScope.launch {
            val records = repository.getAllStepRecords()
            val todayRecord = records.find { it.date == today }
            
            todayStepsCached = todayRecord?.stepCount ?: 0
            todayWalkingTimeMs = todayRecord?.activeTimeMillis ?: 0L
            
            updateNotification(todayStepsCached)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand")
        startServiceForeground()
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "Service onDestroy")
        finalizeWalkingIfNeeded()
        sensorManager.unregisterListener(this)
        handler.removeCallbacks(walkingCheckRunnable)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun registerStepSensors() {
        stepDetectorSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        stepCounterSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_STEP_DETECTOR -> handleDetectedStep()
            Sensor.TYPE_STEP_COUNTER -> {
                val currentValue = event.values[0].toInt()
                handleStepCounter(currentValue)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun handleDetectedStep() {
        val now = System.currentTimeMillis()
        if (walkingStartTime == 0L) {
            walkingStartTime = now
            sessionTimeAlreadyRecordedMs = 0L
        }
        lastStepTime = now
    }

    private fun handleStepCounter(currentValue: Int) {
        val today = LocalDate.now().toString()
        val currentHour = LocalTime.now().hour
        val now = System.currentTimeMillis()

        serviceScope.launch {
            val lastSavedDay = repository.lastUpdateDay

            if (lastSavedDay != today) {
                repository.lastUpdateDay = today
                lastCounterValue = currentValue
                repository.lastSensorValue = currentValue
                todayStepsCached = 0
                todayWalkingTimeMs = 0L
                walkingStartTime = 0L
                sessionTimeAlreadyRecordedMs = 0L
                updateNotification(0)
                repository.recordSteps(today, 0, 0L)
                // 新しい日の最初の時間帯も初期化
                stepDao.upsertHourly(HourlyStepRecord(today, currentHour, 0, 0L))
                return@launch
            }

            val previousValue = lastCounterValue ?: repository.lastSensorValue.let { if (it < 0) null else it }

            if (previousValue == null) {
                lastCounterValue = currentValue
                repository.lastSensorValue = currentValue
                return@launch
            }

            val diff = currentValue - previousValue

            if (diff > 0) {
                if (walkingStartTime == 0L) {
                    walkingStartTime = now
                    sessionTimeAlreadyRecordedMs = 0L
                }
                lastStepTime = now

                repository.cumulativeSteps += diff
                todayStepsCached += diff
                
                // 累積時間の計算（増分だけを足していく方式に変更）
                val currentSessionMs = now - walkingStartTime
                val timeIncrement = currentSessionMs - sessionTimeAlreadyRecordedMs
                if (timeIncrement > 0) {
                    todayWalkingTimeMs += timeIncrement
                    sessionTimeAlreadyRecordedMs = currentSessionMs
                }
                
                // 1日単位の更新
                repository.recordSteps(today, todayStepsCached, todayWalkingTimeMs)
                
                // 時間単位の更新
                val hourlyRecords = stepDao.getHourlyRecordsForDay(today)
                val currentHourly = hourlyRecords.find { it.hour == currentHour }
                val newHourlySteps = (currentHourly?.stepCount ?: 0) + diff
                val newHourlyTime = (currentHourly?.activeTimeMillis ?: 0L) + (if (timeIncrement > 0) timeIncrement else 0L)
                stepDao.upsertHourly(HourlyStepRecord(today, currentHour, newHourlySteps, newHourlyTime))
                
                lastCounterValue = currentValue
                repository.lastSensorValue = currentValue
                
                updateNotification(todayStepsCached)
            } else if (diff < 0) {
                lastCounterValue = currentValue
                repository.lastSensorValue = currentValue
            }
        }
    }

    private val walkingCheckRunnable = object : Runnable {
        override fun run() {
            checkWalkingStop()
            handler.postDelayed(this, 2000L)
        }
    }

    private fun checkWalkingStop() {
        val now = System.currentTimeMillis()
        if (walkingStartTime != 0L && lastStepTime != 0L) {
            if (now - lastStepTime >= walkingTimeoutMs) {
                // セッション終了。todayWalkingTimeMs は既に handleStepCounter で更新済み。
                walkingStartTime = 0L
                lastStepTime = 0L
                sessionTimeAlreadyRecordedMs = 0L
            }
        }
    }

    private fun finalizeWalkingIfNeeded() {
        // 終了時。todayWalkingTimeMs は既に handleStepCounter で更新済み。
        walkingStartTime = 0L
        lastStepTime = 0L
        sessionTimeAlreadyRecordedMs = 0L
    }

    private fun createNotification(steps: Int): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ラブ万歩計: 歩数計測中")
            .setContentText("現在 $steps 歩です。ひかりが見守っています！")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun updateNotification(steps: Int) {
        val notification = createNotification(steps)
        val manager = getSystemService(NotificationManager::class.java)
        manager?.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "歩数計測サービス",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "バックグラウンドで継続的に歩数をカウントします"
            channel.setShowBadge(false)
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
}
