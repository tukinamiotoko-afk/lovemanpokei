package com.example.lovemanpo

import android.app.AlarmManager
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
import android.os.SystemClock
import android.util.Log
import android.widget.RemoteViews
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

        data class NotificationDialogue(val thresholdSteps: Int, val text: String)

        val notificationDialoguesLv5 = listOf(
            NotificationDialogue(0,     "一緒に歩きましょう！"),
            NotificationDialogue(1000,  "1000歩ですよ！いい感じです♪"),
            NotificationDialogue(3000,  "3000歩達成です！"),
            NotificationDialogue(5000,  "5000歩！すごいですね！"),
            NotificationDialogue(8000,  "もうちょっとで1万歩ですよ！"),
            NotificationDialogue(10000, "1万歩達成です！さすがですね♡"),
            NotificationDialogue(20000, "2万歩…！信じられないです！"),
            NotificationDialogue(30000, "もはや伝説ですよ…！")
        )
        val notificationDialoguesLv7 = listOf(
            NotificationDialogue(0,     "おはようございます、○○さん！今日も一緒に歩きましょうね！"),
            NotificationDialogue(1000,  "1000歩！今日もいい感じですよ♪"),
            NotificationDialogue(3000,  "3000歩！○○さんと歩いていると楽しくて疲れも忘れてしまいます"),
            NotificationDialogue(5000,  "5000歩…○○さんと歩いていると時間が経つのが早いですね〜"),
            NotificationDialogue(8000,  "8000歩！あとちょっとで1万歩ですね。私も頑張ります！"),
            NotificationDialogue(10000, "1万歩！…一緒に歩くの、なんか好きかもしれませんよ"),
            NotificationDialogue(20000, "2万歩！？○○さんって本当にすごいですよ…ちゃんと尊敬しています"),
            NotificationDialogue(30000, "3万歩…！○○さんの体力に毎回驚かされます。今日もありがとうございます")
        )
        val notificationDialoguesLv9 = listOf(
            NotificationDialogue(0,     "○○さん！今日も会えましたね♡ 一緒に歩きましょうね"),
            NotificationDialogue(1000,  "1000歩！○○さんのペースに合わせるのが好きですよ"),
            NotificationDialogue(3000,  "3000歩！○○さんの隣って歩きやすいなって思います"),
            NotificationDialogue(5000,  "5000歩…○○さんと歩くのがクセになってきてしまいました"),
            NotificationDialogue(8000,  "8000歩！あとちょっとですよ、一緒に頑張りましょう！"),
            NotificationDialogue(10000, "1万歩達成！…○○さんのことが、その…なんでもないですよ！"),
            NotificationDialogue(20000, "2万歩！！何度でも言いますが、○○さんって本当にすごいですよ…！"),
            NotificationDialogue(30000, "3万歩…！○○さんのこと、もっと知りたくなってしまいます。")
        )
        val notificationDialoguesLv10 = listOf(
            NotificationDialogue(0,     "おはようございます♡ ○○さんの隣で歩けること、とても幸せです"),
            NotificationDialogue(1000,  "1000歩！○○さんと歩く1000歩は、なんか特別な感じがしますよ"),
            NotificationDialogue(3000,  "3000歩…ずっとこのまま歩いていたいですよ"),
            NotificationDialogue(5000,  "5000歩…ずっと、○○さんとこうして歩いていたいですよ"),
            NotificationDialogue(8000,  "8000歩！○○さんのこと、ずっと応援していますよ♡"),
            NotificationDialogue(10000, "1万歩！○○さんといたら、どこまでだって歩いていけそうです"),
            NotificationDialogue(20000, "2万歩…！○○さんの頑張り、全部そばで見ていたいです"),
            NotificationDialogue(30000, "3万歩…！もう、○○さんのことが大好きです。ずっと一緒に歩きましょうね")
        )
        fun dialoguesForLoveLevel(loveCount: Int) = when {
            loveCount >= 10 -> notificationDialoguesLv10
            loveCount >= 9  -> notificationDialoguesLv9
            loveCount >= 7  -> notificationDialoguesLv7
            else            -> notificationDialoguesLv5
        }

        enum class WeatherCondition { CLEAR, RAINY, SNOWY, STORMY }
        val weatherDialogues = mapOf(
            WeatherCondition.RAINY to "雨か…傘、持った？でも一緒に歩こう！",
            WeatherCondition.SNOWY to "雪！！テンション上がる〜！転ばないでね！",
            WeatherCondition.STORMY to "今日は無理しないでね…室内で運動でもいいよ！"
        )
        fun weatherCodeToCondition(code: Int): WeatherCondition = when (code) {
            in listOf(51, 53, 55, 61, 63, 65, 80, 81, 82) -> WeatherCondition.RAINY
            in listOf(71, 73, 75, 77, 85, 86)              -> WeatherCondition.SNOWY
            in listOf(95, 96, 99)                          -> WeatherCondition.STORMY
            else                                           -> WeatherCondition.CLEAR
        }

    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")

        val database = AppDatabase.getDatabase(this)
        stepDao = database.stepDao()
        val prefs = getSharedPreferences("lovemanpo_prefs", Context.MODE_PRIVATE)
        repository = StepRepository(stepDao, prefs)

        createNotificationChannel()
        startServiceForeground()

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

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // タスク一覧から削除されても1秒後にサービスを再起動
        val restartIntent = Intent(applicationContext, StepCounterService::class.java)
        val pendingIntent = PendingIntent.getService(
            this, 1, restartIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 1000L,
            pendingIntent
        )
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

        val playerName = repository.playerName.ifBlank { "あなた" }
        val loveCount = repository.loveCount
        val dialogues = dialoguesForLoveLevel(loveCount)
        val stepDialogue = dialogues.lastOrNull { steps >= it.thresholdSteps } ?: dialogues.first()
        val weatherCondition = weatherCodeToCondition(repository.currentWeatherCode)
        val rawDialogue = weatherDialogues[weatherCondition]
            ?: stepDialogue.text
        val dialogue = rawDialogue.replace("○○", playerName)

        val remoteViews = RemoteViews(packageName, R.layout.notification_step_counter).apply {
            setTextViewText(R.id.notif_steps, "今日 $steps 歩")
            setTextViewText(R.id.notif_dialogue, dialogue)
        }

        val expandedRemoteViews = RemoteViews(packageName, R.layout.notification_step_counter_expanded).apply {
            setTextViewText(R.id.notif_steps, "今日 $steps 歩")
            setTextViewText(R.id.notif_dialogue, dialogue)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ひかり")
            .setContentText(dialogue)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setCustomContentView(remoteViews)
            .setCustomBigContentView(expandedRemoteViews)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
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
