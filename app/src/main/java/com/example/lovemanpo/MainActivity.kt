package com.example.lovemanpo

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.lovemanpo.ui.theme.ラブ万歩計Theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility // 必要
import androidx.compose.animation.slideInVertically // 必要
import androidx.compose.animation.slideOutVertically // 必要
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.gestures.detectVerticalDragGestures // 必要
import androidx.core.view.WindowCompat // 必要
import androidx.core.view.WindowInsetsCompat // 必要
import androidx.core.view.WindowInsetsControllerCompat // 必要
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

// --- 期間の定義 ---
enum class DisplayPeriod(val label: String) {
    DAY("1日"), WEEK("1週"), MONTH("1月"), YEAR("1年")
}

// --- データ保存（リポジトリ） ---
class StepRepository(private val stepDao: StepDao, private val prefs: SharedPreferences) {
    var cumulativeSteps: Int
        get() = prefs.getInt("CUMULATIVE_STEPS", 0)
        set(value) = prefs.edit { putInt("CUMULATIVE_STEPS", value) }
        
    var lastSensorValue: Int
        get() = prefs.getInt("LAST_SENSOR_VALUE", -1)
        set(value) = prefs.edit { putInt("LAST_SENSOR_VALUE", value) }
        
    var lastUpdateDay: String
        get() = prefs.getString("LAST_UPDATE_DAY", "") ?: ""
        set(value) = prefs.edit { putString("LAST_UPDATE_DAY", value) }
        
    var playerName: String
        get() = prefs.getString("PLAYER_NAME", "")!!
        set(value) = prefs.edit { putString("PLAYER_NAME", value) }
        
    var loveCount: Int
        get() = prefs.getInt("LOVE_COUNT_V3", 1)
        set(value) = prefs.edit { putInt("LOVE_COUNT_V3", value) }
        
    var heartCount: Int
        get() = prefs.getInt("HEART_COUNT", 0)
        set(value) = prefs.edit { putInt("HEART_COUNT", value) }
        
    var spentActionPoints: Int
        get() = prefs.getInt("SPENT_ACTION_POINTS", 0)
        set(value) = prefs.edit { putInt("SPENT_ACTION_POINTS", value) }

    var totalEarnedPoints: Int
        get() = prefs.getInt("TOTAL_EARNED_POINTS", 0)
        set(value) = prefs.edit { putInt("TOTAL_EARNED_POINTS", value) }

    var todayPointsEarned: Int
        get() = prefs.getInt("TODAY_POINTS_EARNED", 0)
        set(value) = prefs.edit { putInt("TODAY_POINTS_EARNED", value) }
        
    var geminiApiKey: String
        get() = prefs.getString("GEMINI_API_KEY", "") ?: ""
        set(value) = prefs.edit { putString("GEMINI_API_KEY", value) }

    var freeChatHistoryJson: String
        get() = prefs.getString("FREE_CHAT_HISTORY", "[]") ?: "[]"
        set(value) = prefs.edit { putString("FREE_CHAT_HISTORY", value) }

    var hasEverChatted: Boolean
        get() = prefs.getBoolean("HAS_EVER_CHATTED", false)
        set(value) = prefs.edit { putBoolean("HAS_EVER_CHATTED", value) }

    var customCharacterNote: String
        get() = prefs.getString("CUSTOM_CHARACTER_NOTE", "") ?: ""
        set(value) = prefs.edit { putString("CUSTOM_CHARACTER_NOTE", value) }

    var isPremium: Boolean
        get() = prefs.getBoolean("IS_PREMIUM", false)
        set(value) = prefs.edit { putBoolean("IS_PREMIUM", value) }

    var freeChatSummary: String
        get() = prefs.getString("FREE_CHAT_SUMMARY", "") ?: ""
        set(value) = prefs.edit { putString("FREE_CHAT_SUMMARY", value) }
    var freeChatSummarizedCount: Int
        get() = prefs.getInt("FREE_CHAT_SUMMARIZED_COUNT", 0)
        set(value) = prefs.edit { putInt("FREE_CHAT_SUMMARIZED_COUNT", value) }

    // 日次日記（日付 → 要約テキスト）
    fun getDailyDiary(date: String): String = prefs.getString("DAILY_DIARY_$date", "") ?: ""
    fun setDailyDiary(date: String, text: String) = prefs.edit { putString("DAILY_DIARY_$date", text) }
    var diaryDates: Set<String>
        get() = prefs.getStringSet("DIARY_DATES", emptySet()) ?: emptySet()
        set(value) = prefs.edit { putStringSet("DIARY_DATES", value) }

    var heightCm: Float
        get() = prefs.getFloat("HEIGHT_CM", 170f)
        set(value) = prefs.edit { putFloat("HEIGHT_CM", value) }
        
    var weightKg: Float
        get() = prefs.getFloat("WEIGHT_KG", 60f)
        set(value) = prefs.edit { putFloat("WEIGHT_KG", value) }
        
    var userGender: String
        get() = prefs.getString("USER_GENDER", "")!!
        set(value) = prefs.edit { putString("USER_GENDER", value) }

    var batterySetupDone: Boolean
        get() = prefs.getBoolean("BATTERY_SETUP_DONE", false)
        set(value) = prefs.edit { putBoolean("BATTERY_SETUP_DONE", value) }

    var lastChatTimestamp: Long
        get() = prefs.getLong("LAST_CHAT_TS", 0L)
        set(value) = prefs.edit { putLong("LAST_CHAT_TS", value) }

    suspend fun recordSteps(date: String, steps: Int, activeTimeMillis: Long = 0L) {
        stepDao.upsert(StepRecord(date = date, stepCount = steps, activeTimeMillis = activeTimeMillis))
    }

    suspend fun getAllStepRecords(): List<StepRecord> {
        return stepDao.getAllRecords()
    }

    fun getAllStepRecordsFlow() = stepDao.getAllRecordsFlow()
    
    suspend fun getHourlyRecords(date: String): List<HourlyStepRecord> {
        return stepDao.getHourlyRecordsForDay(date)
    }

    fun resetAllData() {
        prefs.edit { clear() }
    }
}

// --- ViewModel ---
class StepViewModel(private val repository: StepRepository) : ViewModel() {
    val allStepRecords = mutableStateOf<List<StepRecord>>(emptyList())
    val hourlyStepRecords = mutableStateOf<List<HourlyStepRecord>>(emptyList())
    val todaySteps = mutableIntStateOf(0)
    val cumulativeSteps = mutableIntStateOf(repository.cumulativeSteps)
    val playerName = mutableStateOf(repository.playerName)
    val loveCount = mutableIntStateOf(repository.loveCount)
    val heartCount = mutableIntStateOf(repository.heartCount)
    val selectedPeriod = mutableStateOf(DisplayPeriod.DAY)
    val spentActionPoints = mutableIntStateOf(repository.spentActionPoints)
    val totalEarnedPoints = mutableIntStateOf(repository.totalEarnedPoints)
    
    val heightCm = mutableFloatStateOf(repository.heightCm)
    val weightKg = mutableFloatStateOf(repository.weightKg)
    val userGender = mutableStateOf(repository.userGender)
    val batterySetupDone = mutableStateOf(repository.batterySetupDone)

    val currentActionPoints =
        derivedStateOf { totalEarnedPoints.intValue - spentActionPoints.intValue }
    val stepGaugeProgress = derivedStateOf { (todaySteps.intValue.toFloat() / 5000f).coerceAtMost(1f) }
    val heartGaugeProgress = derivedStateOf { heartCount.intValue.toFloat() / 15f }

    val strideLength: Float
        get() {
            val multiplier = if (userGender.value == "男性") 0.45f else 0.415f
            return (heightCm.floatValue * multiplier) / 100.0f
        }

    init {
        viewModelScope.launch {
            repository.getAllStepRecordsFlow().collectLatest { records ->
                allStepRecords.value = records.sortedBy { it.date }
                val today = LocalDate.now().toString()
                val newTodaySteps = records.find { it.date == today }?.stepCount ?: 0
                todaySteps.intValue = newTodaySteps
                cumulativeSteps.intValue = repository.cumulativeSteps
                loveCount.intValue = repository.loveCount
                heartCount.intValue = repository.heartCount
                spentActionPoints.intValue = repository.spentActionPoints
                totalEarnedPoints.intValue = repository.totalEarnedPoints

                val earned = (newTodaySteps / 2000).coerceAtMost(5)
                val toGrant = earned - repository.todayPointsEarned
                if (toGrant > 0) {
                    repository.totalEarnedPoints += toGrant
                    repository.todayPointsEarned = earned
                    totalEarnedPoints.intValue = repository.totalEarnedPoints
                }
            }
        }
    }

    fun fetchHourlyRecords(date: String) {
        viewModelScope.launch {
            hourlyStepRecords.value = repository.getHourlyRecords(date)
        }
    }

    fun setPlayerName(name: String) {
        repository.playerName = name; playerName.value = name
    }
    
    fun setUserProfile(height: Float, weight: Float) {
        repository.heightCm = height
        repository.weightKg = weight
        heightCm.floatValue = height
        weightKg.floatValue = weight
    }

    fun saveProfile(height: Float, gender: String) {
        repository.heightCm = height
        repository.userGender = gender
        heightCm.floatValue = height
        userGender.value = gender
    }

    fun completeBatterySetup() {
        repository.batterySetupDone = true
        batterySetupDone.value = true
    }

    var geminiApiKey: String
        get() = repository.geminiApiKey
        set(value) { repository.geminiApiKey = value }

    val freeChatMessages = mutableStateListOf<ChatMessage>().also { list ->
        try {
            val json = org.json.JSONArray(repository.freeChatHistoryJson)
            repeat(json.length()) { i ->
                val obj = json.getJSONObject(i)
                list.add(ChatMessage(obj.getString("role"), obj.getString("content"), null, null, obj.optString("actionText").ifEmpty { null }))
            }
        } catch (_: Exception) {}
    }

    fun getFreeChatSummary() = repository.freeChatSummary
    fun needsFreeChatSummaryUpdate(total: Int) = total > 10 && repository.freeChatSummarizedCount < total - 10
    fun updateFreeChatSummary(summary: String, count: Int) { repository.freeChatSummary = summary; repository.freeChatSummarizedCount = count }

    // 日次日記を更新（今日の日付に紐付け）
    fun updateDailyDiary(summary: String) {
        val today = LocalDate.now().toString()
        repository.setDailyDiary(today, summary)
        val dates = repository.diaryDates.toMutableSet()
        dates.add(today)
        repository.diaryDates = dates.sorted().takeLast(90).toSet()
    }

    // 800 chars 固定の記憶コンテキストをビルド（新しい日付から降順で埋める）
    fun buildMemoryContext(): String {
        val maxChars = 800
        val sb = StringBuilder()
        val dates = repository.diaryDates.sortedDescending()
        for (date in dates) {
            val diary = repository.getDailyDiary(date)
            if (diary.isBlank()) continue
            val entry = "$date: $diary\n"
            if (sb.length + entry.length > maxChars) break
            sb.append(entry)
        }
        // 旧フォーマット移行: 日次日記がまだなければ freeChatSummary を使用
        if (sb.isEmpty()) {
            val legacy = repository.freeChatSummary
            if (legacy.isNotBlank()) return legacy.take(maxChars)
        }
        return sb.toString().trim()
    }

    val hasEverChatted get() = repository.hasEverChatted
    fun markHasEverChatted() { repository.hasEverChatted = true }

    val customCharacterNote get() = repository.customCharacterNote
    val customCharacterItems get() = repository.customCharacterNote
        .split("\n").filter { it.isNotBlank() }
    fun saveCustomCharacterItems(items: List<String>) {
        repository.customCharacterNote = items.joinToString("\n")
    }

    val isPremium get() = repository.isPremium
    fun unlockPremium() { repository.isPremium = true }

    fun updateLastChatTime() { repository.lastChatTimestamp = System.currentTimeMillis() }

    fun hoursSinceLastChat(): Int {
        val last = repository.lastChatTimestamp
        if (last == 0L) return 0
        return ((System.currentTimeMillis() - last) / (1000L * 60 * 60)).toInt()
    }

    fun getCurrentStreak(): Int {
        val records = allStepRecords.value
        if (records.isEmpty()) return 0
        val today = LocalDate.now()
        var streak = 0
        var checkDate = today
        while (true) {
            val rec = records.find { it.date == checkDate.toString() }
            if (rec != null && rec.stepCount >= 1000) { streak++; checkDate = checkDate.minusDays(1) }
            else break
        }
        return streak
    }

    fun getStepsDuringAbsence(hoursSinceLastChat: Int): Int {
        if (hoursSinceLastChat < 24) return 0
        val daysSince = (hoursSinceLastChat / 24).toLong()
        val today = LocalDate.now()
        val since = today.minusDays(daysSince)
        return allStepRecords.value.filter {
            val d = runCatching { LocalDate.parse(it.date) }.getOrNull()
            d != null && !d.isBefore(since) && d.isBefore(today)
        }.sumOf { it.stepCount }
    }

    fun saveFreeChatHistory() {
        val json = org.json.JSONArray()
        freeChatMessages.forEach { msg ->
            json.put(org.json.JSONObject().apply {
                put("role", msg.role)
                put("content", msg.content)
                msg.actionText?.let { put("actionText", it) }
            })
        }
        repository.freeChatHistoryJson = json.toString()
    }

    fun clearFreeChatHistory() {
        freeChatMessages.clear()
        repository.freeChatHistoryJson = "[]"
    }

    fun spendPointForChat(): Boolean {
        if (currentActionPoints.value > 0) {
            repository.spentActionPoints++
            spentActionPoints.intValue = repository.spentActionPoints
            return true
        }
        return false
    }

    fun refundPointForChat() {
        if (repository.spentActionPoints > 0) {
            repository.spentActionPoints--
            spentActionPoints.intValue = repository.spentActionPoints
        }
    }

    fun earnHeart() {
        heartCount.intValue++
        if (heartCount.intValue >= 15) {
            heartCount.intValue = 0
            if (loveCount.intValue < 10) loveCount.intValue++
        }
        repository.heartCount = heartCount.intValue
        repository.loveCount = loveCount.intValue
    }

    fun loseHeart() {
        if (heartCount.intValue > 0) {
            heartCount.intValue--
        } else if (loveCount.intValue > 0) {
            loveCount.intValue--
            heartCount.intValue = 9
        }
        repository.heartCount = heartCount.intValue
        repository.loveCount = loveCount.intValue
    }

    fun loadAllRecords() {
        viewModelScope.launch {
            allStepRecords.value = repository.getAllStepRecords().sortedBy { it.date }
        }
    }

    fun calculateSpeed(steps: Int, activeTimeMillis: Long): Double {
        if (activeTimeMillis <= 0 || steps <= 0) return 0.0
        val distanceKm = (steps * strideLength) / 1000.0
        val hours = activeTimeMillis.toDouble() / (1000 * 60 * 60)
        val speed = distanceKm / hours
        return if (speed > 15.0) 15.0 else speed
    }

    fun calculateCalories(steps: Int, activeTimeMillis: Long): Double {
        if (activeTimeMillis <= 0 || steps <= 0) return 0.0
        val hours = activeTimeMillis.toDouble() / (1000 * 60 * 60)
        val speed = calculateSpeed(steps, activeTimeMillis)
        val mets = when {
            speed < 4.0 -> 2.8
            speed < 5.0 -> 3.5
            else -> 4.3
        }
        return 1.05 * mets * hours * weightKg.floatValue
    }

    fun debugAddSteps(amount: Int) {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            val records = repository.getAllStepRecords()
            val currentRecord = records.find { it.date == today }
            val newTodaySteps = todaySteps.intValue + amount
            val newActiveTime = (currentRecord?.activeTimeMillis ?: 0L) + (amount * 500L)
            
            repository.recordSteps(today, newTodaySteps, newActiveTime)
            
            val newCumulative = repository.cumulativeSteps + amount
            repository.cumulativeSteps = newCumulative
            cumulativeSteps.intValue = newCumulative
            todaySteps.intValue = newTodaySteps

            // デバッグ用：ポイント付与ロジックのシミュレート
            val newPoints = newTodaySteps / 2000
            val cappedPoints = newPoints.coerceAtMost(5)
            val pointsToGrant = cappedPoints - repository.todayPointsEarned
            if (pointsToGrant > 0) {
                repository.totalEarnedPoints += pointsToGrant
                repository.todayPointsEarned = cappedPoints
                totalEarnedPoints.intValue = repository.totalEarnedPoints
            }
        }
    }

    fun debugSetLove(count: Int) {
        repository.loveCount = count
        loveCount.intValue = count
    }

    fun debugSetHeart(count: Int) {
        repository.heartCount = count
        heartCount.intValue = count
    }

    fun debugAddActionPoints(amount: Int) {
        repository.totalEarnedPoints += amount
        totalEarnedPoints.intValue = repository.totalEarnedPoints
    }

    fun debugResetData() {
        repository.resetAllData()
        playerName.value = ""
        cumulativeSteps.intValue = 0
        todaySteps.intValue = 0
        loveCount.intValue = 1
        heartCount.intValue = 0
        spentActionPoints.intValue = 0
        totalEarnedPoints.intValue = 0
        userGender.value = ""
        batterySetupDone.value = false
    }
}

class StepViewModelFactory(private val repository: StepRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StepViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StepViewModel(repository) as T
        }
        throw IllegalArgumentException("不明なViewModel")
    }
}

fun getHeartPath(size: Size): Path {
    val w = size.width; val h = size.height
    return Path().apply {
        moveTo(w / 2f, h * 0.3f)
        cubicTo(w * 0.1f, h * 0.05f, -w * 0.1f, h * 0.6f, w / 2f, h * 0.9f)
        cubicTo(w * 1.1f, h * 0.6f, w * 0.9f, h * 0.05f, w / 2f, h * 0.3f)
        close()
    }
}

val MplusRoundedFontFamily = FontFamily(
    Font(R.font.mplus_rounded_regular, FontWeight.Normal),
    Font(R.font.mplus_rounded_bold, FontWeight.Bold),
    Font(R.font.mplus_rounded_extrabold, FontWeight.ExtraBold),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())

        val database = AppDatabase.getDatabase(this)
        val repository = StepRepository(database.stepDao(), getSharedPreferences("lovemanpo_prefs", MODE_PRIVATE))
        val viewModelFactory = StepViewModelFactory(repository)

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "StepSyncWork",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<StepSyncWorker>(6, TimeUnit.HOURS).build()
        )

        setContent {
            ラブ万歩計Theme { PedometerAppWithNavigation(viewModelFactory) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedometerAppWithNavigation(viewModelFactory: StepViewModelFactory) {
    val context = LocalContext.current
    val permissions = mutableListOf(Manifest.permission.ACTIVITY_RECOGNITION)
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
    }
    var hasPermissions by remember {
        mutableStateOf(permissions.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED })
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results -> hasPermissions = results.values.all { it } }
    
    LaunchedEffect(hasPermissions) {
        if (hasPermissions) {
            val serviceIntent = Intent(context, StepCounterService::class.java)
            context.startForegroundService(serviceIntent)
        } else {
            launcher.launch(permissions.toTypedArray())
        }
    }

    if (hasPermissions) {
        val navController = rememberNavController()
        val viewModel: StepViewModel = viewModel(factory = viewModelFactory)

        var navTrigger by remember { mutableStateOf(0) }
        val routeHistory = remember { mutableListOf<String>() }
        LaunchedEffect(navController) {
            var isFirst = true
            navController.currentBackStackEntryFlow.collect { entry ->
                val route = entry.destination.route ?: return@collect
                if (isFirst) {
                    isFirst = false
                    routeHistory.add(route)
                    return@collect
                }
                // 履歴の1つ前のルートと一致 → 戻る操作
                if (routeHistory.size >= 2 && routeHistory[routeHistory.size - 2] == route) {
                    routeHistory.removeAt(routeHistory.size - 1)
                } else {
                    // 前進 → キャラ演出を発火
                    routeHistory.add(route)
                    navTrigger++
                }
            }
        }

        val startDestination = remember(Unit) {
            if (viewModel.playerName.value.isEmpty()) {
                "name_input"
            } else if (viewModel.userGender.value.isEmpty()) {
                "profile_setup"
            } else if (!viewModel.batterySetupDone.value) {
                "battery_setup"
            } else {
                "home"
            }
        }

        Box(modifier = Modifier
            .fillMaxSize()
            .consumeWindowInsets(WindowInsets.navigationBars)
        ) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                enterTransition = {
                    slideInHorizontally(tween(500, delayMillis = 120, easing = FastOutSlowInEasing)) { it }
                },
                exitTransition = {
                    ExitTransition.None
                },
                popEnterTransition = {
                    EnterTransition.None
                },
                popExitTransition = {
                    slideOutHorizontally(tween(500, easing = FastOutSlowInEasing)) { it }
                }
            ) {
                composable("name_input") { NameInputScreen(viewModel, navController) }
                composable("profile_setup") { ProfileSetupScreen(navController, viewModel) }
                composable("battery_setup") { StabilitySetupScreen(navController, viewModel) }
                composable("home") { HomeScreen(navController, viewModel) }
                composable("chatmenu") { ChatMenuScreen(navController, viewModel) }
                composable("freechat") { FreeChatScreen(navController, viewModel) }
                composable("records") { RecordsScreen(navController, viewModel) }
                composable("diary")   { DiaryScreen(navController, viewModel) }
                composable("settings") { SettingsScreen(navController, viewModel) }
                composable("debug") { DebugScreen(navController, viewModel) }
            }
            key(navTrigger) {
                if (navTrigger > 0) CharacterPullOverlay()
            }
        }
    } else {
        PermissionRequestScreen { launcher.launch(permissions.toTypedArray()) }
    }
}

@Composable
fun CharacterPullOverlay() {
    var visible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        visible = true
    }

    if (!visible) return

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidthPx = constraints.maxWidth.toFloat()
        val animTranslationX = remember { Animatable(0f) }

        LaunchedEffect(screenWidthPx) {
            animTranslationX.snapTo(0f)
            delay(120)
            animTranslationX.animateTo(
                targetValue = -screenWidthPx,
                animationSpec = tween(500, easing = FastOutSlowInEasing)
            )
            visible = false
        }

        Image(
            painter = painterResource(R.drawable.hikari_gamenwohipparu_sd),
            contentDescription = null,
            modifier = Modifier
                .height(200.dp)
                .align(Alignment.BottomEnd)
                .graphicsLayer { translationX = animTranslationX.value }
        )
    }
}

fun isBatteryOptimizationIgnored(context: Context): Boolean {
    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return pm.isIgnoringBatteryOptimizations(context.packageName)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StabilitySetupScreen(navController: NavController, viewModel: StepViewModel) {
    val context = LocalContext.current
    var showBatteryDetail by remember { mutableStateOf(false) }
    var showAutoStartDetail by remember { mutableStateOf(false) }
    
    var batteryChecked by remember { mutableStateOf(isBatteryOptimizationIgnored(context)) }
    var autoStartChecked by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                batteryChecked = isBatteryOptimizationIgnored(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Icon(imageVector = Icons.Default.HealthAndSafety, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "歩数計測を止めないために", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "ひかりとずっと一緒に歩くために、以下の2つの設定をお願いします。項目をタップして説明を確認してください。",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        SetupItemRow(
            number = "1",
            title = "バッテリー最適化の解除",
            isDone = batteryChecked,
            onClick = { showBatteryDetail = true }
        )

        Spacer(modifier = Modifier.height(16.dp))

        SetupItemRow(
            number = "2",
            title = "自動起動の許可",
            isDone = autoStartChecked,
            onClick = { showAutoStartDetail = true }
        )

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(40.dp))
        
        Button(
            onClick = {
                viewModel.completeBatterySetup()
                navController.navigate("home") {
                    popUpTo("battery_setup") { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = batteryChecked
        ) {
            Text(if (batteryChecked) "設定を完了して進む" else "1の設定を完了してください", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showBatteryDetail) {
        SetupDetailDialog(
            title = "バッテリー最適化の解除",
            description = "節電機能によってスリープ中に計測が止まるのを防ぎます。設定画面で「制限なし」または「最適化しない」を選択してください。",
            imageRes = R.drawable.guide_battery,
            onGoToSettings = {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = "package:${context.packageName}".toUri()
                    }
                    context.startActivity(intent)
                } catch (_: Exception) {
                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    context.startActivity(intent)
                }
            },
            onDismiss = { showBatteryDetail = false }
        )
    }

    if (showAutoStartDetail) {
        SetupDetailDialog(
            title = "自動起動の許可",
            description = "スマホ起動時やバックグラウンドでアプリが動くように設定します。一部の機種で必須の設定です。",
            imageRes = R.drawable.guide_autostart,
            onGoToSettings = {
                autoStartChecked = true
                openAutoStartSettings(context)
            },
            onDismiss = { showAutoStartDetail = false }
        )
    }
}

@Composable
fun SetupItemRow(number: String, title: String, isDone: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isDone) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
                            else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isDone) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (isDone) MaterialTheme.colorScheme.primary else Color.Gray,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isDone) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    else Text(number, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.Gray)
        }
    }
}

@Composable
fun SetupDetailDialog(
    title: String,
    description: String,
    imageRes: Int,
    onGoToSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = { onGoToSettings(); onDismiss() }) {
                Text("設定画面へ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("戻る") }
        },
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = "設定ガイド画像",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                Text(description, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("※機種によって画面が異なる場合があります。", fontSize = 11.sp, color = Color.Gray)
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

fun openAutoStartSettings(context: Context) {
    val intents = arrayOf(
        Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
        Intent().setComponent(ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")),
        Intent().setComponent(ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")),
        Intent().setComponent(ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
        Intent().setComponent(ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")),
        Intent().setComponent(ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
        Intent().setComponent(ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
        Intent().setComponent(ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")),
        Intent().setComponent(ComponentName("com.htc.pitHTC.pit", "com.htc.pitHTC.pit.BatteryOptimization")),
        Intent().setComponent(ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.autostart.AutoStartActivity"))
    )

    for (intent in intents) {
        if (context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return
        }
    }
    
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = "package:${context.packageName}".toUri()
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}



@Composable
fun NameInputScreen(viewModel: StepViewModel, navController: NavController) {
    var text by remember { mutableStateOf("") }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
            Text(text = "あなたの名前を教えてください", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("名前") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { if (text.isNotBlank()) { viewModel.setPlayerName(text); navController.navigate("profile_setup") { popUpTo("name_input") { inclusive = true } } } }, enabled = text.isNotBlank(), interactionSource = remember { MutableInteractionSource() }) { Text("決定") }
        }
    }
}

@Composable
fun ProfileSetupScreen(navController: NavController, viewModel: StepViewModel) {
    var tempHeight by remember { mutableStateOf(viewModel.heightCm.floatValue.toString()) }
    var tempWeight by remember { mutableStateOf(viewModel.weightKg.floatValue.toString()) }
    var tempGender by remember { mutableStateOf(viewModel.userGender.value.ifEmpty { "男性" }) }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("プロフィール設定", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = tempHeight,
            onValueChange = { tempHeight = it },
            label = { Text("身長 (cm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = tempWeight,
            onValueChange = { tempWeight = it },
            label = { Text("体重 (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            Text("性別：", fontWeight = FontWeight.Bold)
            RadioButton(selected = tempGender == "男性", onClick = { tempGender = "男性" })
            Text("男性", modifier = Modifier.clickable { tempGender = "男性" })
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(selected = tempGender == "女性", onClick = { tempGender = "女性" })
            Text("女性", modifier = Modifier.clickable { tempGender = "女性" })
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                val h = tempHeight.toFloatOrNull() ?: 160f
                val w = tempWeight.toFloatOrNull() ?: 60f
                viewModel.setUserProfile(h, w)
                viewModel.saveProfile(h, tempGender)
                navController.navigate("battery_setup") { popUpTo("profile_setup") { inclusive = true } }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("保存して次へ")
        }
    }
}

@Composable
fun HomeScreen(navController: NavController, viewModel: StepViewModel) {
    val todaySteps by viewModel.todaySteps
    val actionPoints by viewModel.currentActionPoints
    val stepGaugeProgress by viewModel.stepGaugeProgress
    val loveCount by viewModel.loveCount
    val heartCount by viewModel.heartCount
    val heartGaugeProgress by viewModel.heartGaugeProgress
    val playerName by viewModel.playerName

    // 背景固定設定
    val bgRes = R.drawable.home_haikei

    // 今日の活動データを取得して計算
    val allRecords by viewModel.allStepRecords
    val todayRecord = remember(allRecords) {
        val today = java.time.LocalDate.now().toString()
        allRecords.find { it.date == today }
    }
    val activeTimeMillis = todayRecord?.activeTimeMillis ?: 0L
    val activeDays = remember(allRecords) { allRecords.count { it.stepCount >= 1000 } }

    // 距離の計算 (km)
    val distance = (todaySteps * viewModel.strideLength) / 1000.0
    val distanceStr = String.format(java.util.Locale.US, "%.1f km", distance)

    // カロリーの計算
    val calories = viewModel.calculateCalories(todaySteps, activeTimeMillis)
    val caloriesStr = String.format(java.util.Locale.US, "%.0f kcal", calories)

    // 時間のフォーマット (H時間 m分)
    val hours = activeTimeMillis / 3600000
    val minutes = (activeTimeMillis % 3600000) / 60000
    val activeTimeStr = "${hours}時間 ${minutes}分"

    val daysSinceLastActive = remember(allRecords) {
        val today = LocalDate.now()
        val lastActiveDate = allRecords
            .filter { it.stepCount >= 1000 }
            .mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }
            .maxOrNull()
        if (lastActiveDate != null) java.time.temporal.ChronoUnit.DAYS.between(lastActiveDate, today).toInt() else 0
    }
    val stepDialogue = homeStepDialogue(todaySteps, activeDays, daysSinceLastActive)

    val touchDialogues = homeTouchDialogues(loveCount)
    var touchedDialogue by remember { mutableStateOf<Pair<String, Int>?>(null) }
    LaunchedEffect(touchedDialogue) {
        if (touchedDialogue != null) {
            delay(5000)
            touchedDialogue = null
        }
    }

    val displayMessage = touchedDialogue?.first ?: stepDialogue.text
    val displayExpression = touchedDialogue?.second ?: stepDialogue.expr

    HomeScreenContent(
        todaySteps = todaySteps,
        actionPoints = actionPoints,
        stepGaugeProgress = stepGaugeProgress,
        loveCount = loveCount,
        heartCount = heartCount,
        heartGaugeProgress = heartGaugeProgress,
        playerName = playerName,
        bgRes = bgRes,
        dialogueMessage = displayMessage,
        expressionRes = displayExpression,
        // ★ 計算した文字列を渡す
        activeTimeStr = activeTimeStr,
        distanceStr = distanceStr,
        caloriesStr = caloriesStr,
        onCharacterClick = {
            touchedDialogue = touchDialogues.randomOrNull()
        },
        onFreeChatClick = { navController.navigate("chatmenu") },
        onDiaryClick = { navController.navigate("diary") },
        onRecordsClick = { navController.navigate("records") },
        onDebugClick = { navController.navigate("debug") }
    )
}


@Composable
fun HomeScreenContent(
    todaySteps: Int,    actionPoints: Int,
    stepGaugeProgress: Float,
    loveCount: Int,
    heartCount: Int,
    heartGaugeProgress: Float,
    playerName: String,
    bgRes: Int,
    dialogueMessage: String,
    expressionRes: Int,
    // ★ 追加
    activeTimeStr: String,
    distanceStr: String,
    caloriesStr: String,
    onCharacterClick: () -> Unit,
    onFreeChatClick: () -> Unit,
    onDiaryClick: () -> Unit,
    onRecordsClick: () -> Unit,
    onDebugClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = bgRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // カード行 + ボタン列
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                HomeLoveLevelCard(
                    modifier = Modifier.weight(1f),
                    lv = loveCount,
                    progress = heartGaugeProgress,
                    hearts = heartCount
                )
                HomeActionPointsCard(
                    modifier = Modifier.weight(0.72f),
                    pts = actionPoints
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    HomeTopCircleButton(Icons.Default.Notifications)
                    HomeTopCircleButton(Icons.Default.Settings)
                    HomeTopCircleButton(
                        icon = Icons.Default.BugReport,
                        containerColor = Color.Red.copy(alpha = 0.1f),
                        iconColor = Color.Red,
                        onClick = onDebugClick
                    )
                }
            }

            Box(modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)) {
                Image(
                    painter = painterResource(id = expressionRes),
                    contentDescription = "ひかり",
                    modifier = Modifier
                        .fillMaxHeight(1.0f)
                        .align(Alignment.BottomCenter),
                    contentScale = ContentScale.Fit
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 16.dp, top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HomeStepCircleGauge(todaySteps, stepGaugeProgress)
                }

                HintSdHikari(modifier = Modifier.align(Alignment.BottomStart).offset(x = (-20).dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-15).dp)
                    .shadow(8.dp, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            listOf(Color(0xFFF0F8FF), Color(0xFFD6EEFF))
                        )
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val formattedMessage = dialogueMessage.replace("○○", playerName)
                    HomeCommentBanner(expressionRes, formattedMessage, onClick = onCharacterClick)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        HomeStatItemSmall(Icons.Default.Schedule, "歩いた時間", activeTimeStr, null, Color(0xFFF06292))
                        HomeStatItemSmall(Icons.AutoMirrored.Filled.DirectionsWalk, "歩行距離", distanceStr, null, Color(0xFF4FC3F7))
                        HomeStatItemSmall(Icons.Default.Whatshot, "消費カロリー", caloriesStr, null, Color(0xFFFF8A65))
                    }
                    Spacer(modifier = Modifier.height(90.dp))
                }
            }
        } // outer Column

        HomeCustomBottomNav(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
            onFreeChat = onFreeChatClick,
            onDiary = onDiaryClick,
            onRecords = onRecordsClick
        )
    }
} // ← ここで HomeScreenContent が終わる



@Composable
fun HomeTopCircleButton(icon: androidx.compose.ui.graphics.vector.ImageVector, containerColor: Color = Color.White, iconColor: Color = Color.Gray, onClick: () -> Unit = {}) {
    Surface(shape = CircleShape, color = containerColor, modifier = Modifier
        .size(30.dp)
        .clickable { onClick() }) {
        Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = iconColor, modifier = Modifier.size(22.dp)) }
    }
}

@Composable
fun HomeStepCircleGauge(steps: Int, progress: Float) {
    val animatedSteps by animateIntAsState(
        targetValue = steps,
        animationSpec = tween(durationMillis = 600, easing = LinearEasing),
        label = "stepCount"
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(130.dp)
                .shadow(3.dp, CircleShape)
                .background(Color.White, CircleShape)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
            ) {
                val sw = 8.dp.toPx()
                drawArc(
                    color = Color.LightGray.copy(alpha = 0.2f),
                    startAngle = 0f, sweepAngle = 360f, useCenter = false,
                    style = Stroke(sw)
                )
                drawArc(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(Color(0xFF81D4FA), Color(0xFF1565C0))
                    ),
                    startAngle = -90f, sweepAngle = 360f * progress, useCenter = false,
                    style = Stroke(width = sw, cap = StrokeCap.Round)
                )
                val radius = size.minDimension / 2f
                val cx = size.width / 2f
                val cy = size.height / 2f
                repeat(5) { i ->
                    val angleDeg = -90f + (i + 1) * 72f
                    val rad = Math.toRadians(angleDeg.toDouble())
                    val cos = kotlin.math.cos(rad).toFloat()
                    val sin = kotlin.math.sin(rad).toFloat()
                    drawLine(
                        color = Color.White,
                        start = Offset(cx + (radius - sw) * cos, cy + (radius - sw) * sin),
                        end = Offset(cx + radius * cos, cy + radius * sin),
                        strokeWidth = 2.5.dp.toPx()
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.AutoMirrored.Filled.DirectionsWalk, null, tint = Color(0xFFE87C9A), modifier = Modifier.size(18.dp))
                Text("今日の歩数", fontSize = 10.sp, color = Color.Gray)
                Text(String.format(java.util.Locale.US, "%,d", animatedSteps), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.DarkGray)
                Text("歩 / 10,000 歩", fontSize = 9.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun HomeStatItemSmall(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, sub: String?, color: Color) {
    val textStyle = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
    val bgTop = Color(
        red = minOf(1f, color.red + 0.6f),
        green = minOf(1f, color.green + 0.6f),
        blue = minOf(1f, color.blue + 0.6f)
    )
    val bgBottom = Color(
        red = minOf(1f, color.red + 0.42f),
        green = minOf(1f, color.green + 0.42f),
        blue = minOf(1f, color.blue + 0.42f)
    )
    val iconTop = Color(
        red = minOf(1f, color.red + 0.25f),
        green = minOf(1f, color.green + 0.25f),
        blue = minOf(1f, color.blue + 0.25f)
    )
    Box(
        modifier = Modifier
            .width(110.dp)
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(brush = androidx.compose.ui.graphics.Brush.verticalGradient(listOf(bgTop, bgBottom)))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = Color.Unspecified, modifier = Modifier.size(20.dp).gradientTint(listOf(iconTop, color)))
            Spacer(modifier = Modifier.width(5.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(label, fontSize = 8.sp, color = Color.Gray, style = textStyle)
                Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray, style = textStyle)
                if (sub != null) {
                    Text(sub, fontSize = 8.sp, color = Color.Gray, style = textStyle)
                }
            }
        }
    }
}

@Composable
fun HomeLoveLevelCard(modifier: Modifier = Modifier, lv: Int, progress: Float, hearts: Int) {
    val faceRes = when (lv) {
        in 1..3  -> R.drawable.hikari_sd_face_level1
        in 4..6  -> R.drawable.hikari_sd_face_level2
        in 7..8  -> R.drawable.hikari_sd_face_level3
        else     -> R.drawable.hikari_sd_face_level4
    }
    Box(
        modifier = modifier
            .shadow(14.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFFFEEF5))))
            .border(1.dp, Color(0xFFFF6B9D).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
    ) {
        Row(modifier = Modifier.padding(7.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = faceRes),
                contentDescription = null,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFE0E9)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Favorite, null, tint = Color.Unspecified, modifier = Modifier.size(12.dp).gradientTint(listOf(Color(0xFFFF80AB), Color(0xFFE91E63))))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("ラブレベル", fontSize = 9.sp, color = Color(0xFFFF6B9D), fontWeight = FontWeight.Bold, fontFamily = MplusRoundedFontFamily)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text("Lv. $lv", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.DarkGray, fontFamily = MplusRoundedFontFamily)
                    Icon(Icons.Default.Favorite, null, tint = Color.Unspecified, modifier = Modifier.size(10.dp).gradientTint(listOf(Color(0xFFFF80AB), Color(0xFFE91E63))))
                }
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(CircleShape)
                ) {
                    drawRoundRect(color = Color(0xFFFFE0E9), cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2))
                    drawRoundRect(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color(0xFFFF80AB), Color(0xFFE91E63))),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2),
                        size = size.copy(width = size.width * progress)
                    )
                }
            }
        }
    }
}

// 2. 行動ポイントカードのコメントを削除
@Composable
fun HomeActionPointsCard(modifier: Modifier = Modifier, pts: Int) {
    Box(
        modifier = modifier
            .shadow(14.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFDFF6F4))))
            .border(1.dp, Color(0xFF4DB6AC).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(7.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Place, null, tint = Color.Unspecified, modifier = Modifier.size(14.dp).gradientTint(listOf(Color(0xFF80CBC4), Color(0xFF00695C))))
                Spacer(modifier = Modifier.width(4.dp))
                Text("行動ポイント", fontSize = 9.sp, color = Color(0xFF444444), fontFamily = MplusRoundedFontFamily)
            }
            Text("$pts / 5 pt", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A), fontFamily = MplusRoundedFontFamily)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 2.dp)) {
                repeat(5) { i ->
                    Canvas(modifier = Modifier.size(10.dp)) {
                        if (i < pts) {
                            drawCircle(
                                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                    listOf(Color(0xFF80CBC4), Color(0xFF00695C))
                                )
                            )
                        } else {
                            drawCircle(color = Color.LightGray.copy(alpha = 0.4f))
                        }
                    }
                }
            }
        }
    }

}
fun Modifier.gradientTint(colors: List<Color>): Modifier = this
    .graphicsLayer(alpha = 0.99f)
    .drawWithContent {
        drawContent()
        drawRect(
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(colors),
            blendMode = BlendMode.SrcAtop
        )
    }

fun expressionToFaceRes(expr: Int): Int = when (expr) {
    R.drawable.hikari_smile     -> R.drawable.hikari_smile_face
    R.drawable.hikari_blush     -> R.drawable.hikari_blush_face
    R.drawable.hikari_think     -> R.drawable.hikari_think_face
    R.drawable.hikari_celebrate -> R.drawable.hikari_celebrate_face
    R.drawable.hikari_devil     -> R.drawable.hikari_devil_face
    else                        -> R.drawable.hikari_smile_face
}

@Composable
fun HintSdHikari(modifier: Modifier = Modifier) {
    val hints = listOf(
        "2,000歩歩くと\n行動ポイントが1つもらえるよ！",
        "行動ポイントを使って\nひかりとお話しできるよ♪",
        "毎日歩いてひかりとの\n仲を深めよう♪",
        "セリフをタップすると\nひかりが話しかけてくれるよ！",
        "おしゃべりからいつでも\nひかりと話せるよ♪"
    )
    var showHint by remember { mutableStateOf(false) }
    var currentHint by remember { mutableStateOf("") }

    Box(modifier = modifier) {
        if (showHint) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 40.dp, y = (-72).dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showHint = false },
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, Color(0xFFFFB7D0))
            ) {
                Text(
                    currentHint,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    fontSize = 11.sp,
                    color = Color(0xFF1A1A1A),
                    lineHeight = 16.sp,
                    fontFamily = MplusRoundedFontFamily
                )
            }
        }
        Image(
            painter = painterResource(R.drawable.hikari_sd_hint),
            contentDescription = "ヒント",
            modifier = Modifier
                .size(160.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    currentHint = hints.random()
                    showHint = !showHint
                },
            contentScale = ContentScale.Fit
        )
    }
}


@Composable
fun HomeCommentBanner(expr: Int, message: String, onClick: () -> Unit = {}) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 14.dp, border = BorderStroke(1.5.dp, Color(0xFFFFB7D0)), modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClick() }) {
        Row(modifier = Modifier.padding(10.dp).height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
                Image(painter = painterResource(id = expressionToFaceRes(expr)), contentDescription = null, modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFE0E9)), contentScale = ContentScale.Crop)
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(Color(0xFFFFB7D0).copy(alpha = 0.7f))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("ひかり", fontSize = 11.sp, color = Color(0xFFFF6B9D), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.LightGray)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp, color = Color(0xFFFFB7D0).copy(alpha = 0.8f))
                    Text(message, fontSize = 10.sp, color = Color(0xFF1A1A1A))
                }
        }
    }
}

@Composable
fun HomeWeeklySection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Text("今週の歩数", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
            Text("目標 35,000 歩", fontSize = 9.sp, color = Color.Gray)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                val data = listOf(6315, 7102, 4803, 6540, 8765, 7842, 0)
                data.forEachIndexed { i, steps ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (steps > 0) Text(String.format(java.util.Locale.US, "%,d", steps), fontSize = 6.sp, color = Color(0xFFE87C9A))
                        Box(modifier = Modifier
                            .width(12.dp)
                            .height((steps / 150).dp.coerceAtLeast(4.dp))
                            .background(
                                if (i == 5) Color(0xFF1976D2) else Color(0xFFBBDEFB),
                                RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                            ))
                        Text(listOf("月", "火", "水", "木", "金", "土", "日")[i], fontSize = 9.sp, color = if (i == 6) Color.Red else Color.Gray)
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(70.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(Color.LightGray.copy(alpha = 0.2f), -90f, 360f, false, style = Stroke(5.dp.toPx()))
                    drawArc(Color(0xFFE87C9A), -90f, 360f * 0.86f, false, style = Stroke(5.dp.toPx(), cap = StrokeCap.Round))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.EmojiEvents, null, tint = Color(0xFFE87C9A), modifier = Modifier.size(14.dp))
                    Text("達成度 86%", fontSize = 8.sp, color = Color(0xFFE87C9A), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


@Composable
fun HomeCustomBottomNav(modifier: Modifier = Modifier, onFreeChat: () -> Unit, onDiary: () -> Unit, onRecords: () -> Unit) {
    Surface(modifier = modifier
        .fillMaxWidth()
        .height(80.dp), color = Color.White, shadowElevation = 10.dp) {
        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
            HomeNavItem(Icons.Default.Book, "日記", false, onDiary)

            Box(contentAlignment = Alignment.Center, modifier = Modifier
                .offset(y = (-12).dp)
                .clickable { onFreeChat() }) {
                Surface(shape = CircleShape, color = Color(0xFFE87C9A), modifier = Modifier
                    .size(56.dp)
                    .shadow(4.dp, CircleShape)) {
                    Icon(Icons.Default.Chat, null, tint = Color.White, modifier = Modifier.padding(14.dp))
                }
                Text("おしゃべり", modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 22.dp), fontSize = 10.sp, color = Color(0xFFE87C9A), fontWeight = FontWeight.Bold)
            }

            HomeNavItem(Icons.Default.BarChart, "記録", false, onRecords)
        }
    }
}

@Composable
fun HomeNavItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, sel: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Icon(icon, null, tint = if (sel) Color(0xFFE87C9A) else Color.Gray, modifier = Modifier.size(24.dp))
        Text(label, fontSize = 9.sp, color = if (sel) Color(0xFFE87C9A) else Color.Gray)
    }
}


@Preview(showBackground = true, name = "ホーム画面プレビュー")
@Composable
fun HomeScreenPreview() {
    ラブ万歩計Theme {
        HomeScreenContent(
            todaySteps = 7842,
            actionPoints = 2,
            stepGaugeProgress = 0.78f,
            loveCount = 2,
            heartCount = 6,
            heartGaugeProgress = 0.6f,
            playerName = "プレイヤー",
            bgRes = R.drawable.home_haikei,
            dialogueMessage = "今日も一緒にがんばろうね♪",
            expressionRes = R.drawable.hikari_smile,
            // ★ 足りなかった引数を追加
            activeTimeStr = "1時間 32分",
            distanceStr = "5.6 km",
            caloriesStr = "238 kcal",
            onCharacterClick = {},
            onFreeChatClick = {},
            onDiaryClick = {},
            onRecordsClick = {},
            onDebugClick = {}
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(navController: NavController, viewModel: StepViewModel) {
    Scaffold(topBar = { TopAppBarWithBack(title = "デバッグメニュー", onBack = { navController.popBackStack() }) }) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(text = "動作確認用ツール", style = MaterialTheme.typography.titleMedium)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "歩数操作", fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { viewModel.debugAddSteps(1000) }, modifier = Modifier.weight(1f)) { Text("+1000") }
                        Button(onClick = { viewModel.debugAddSteps(5000) }, modifier = Modifier.weight(1f)) { Text("+5000") }
                    }
                    Button(onClick = { viewModel.debugAddSteps(10000) }, modifier = Modifier.fillMaxWidth()) { Text("+10000歩追加") }
                }
            }
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "行動ポイント操作", fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { viewModel.debugAddActionPoints(1) }, modifier = Modifier.weight(1f)) { Text("+1 pt") }
                        Button(onClick = { viewModel.debugAddActionPoints(10) }, modifier = Modifier.weight(1f)) { Text("+10 pt") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { viewModel.debugAddActionPoints(-1) }, modifier = Modifier.weight(1f)) { Text("-1 pt") }
                        Button(onClick = { viewModel.debugAddActionPoints(-10) }, modifier = Modifier.weight(1f)) { Text("-10 pt") }
                    }
                }
            }
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "ラブ・ハート操作", fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { if (viewModel.loveCount.intValue < 10) viewModel.debugSetLove(viewModel.loveCount.intValue + 1) }, modifier = Modifier.weight(1f)) { Text("ラブ+1") }
                        Button(onClick = { if (viewModel.loveCount.intValue > 1) viewModel.debugSetLove(viewModel.loveCount.intValue - 1) }, modifier = Modifier.weight(1f)) { Text("ラブ-1") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { if (viewModel.heartCount.intValue < 10) viewModel.debugSetHeart(viewModel.heartCount.intValue + 1) }, modifier = Modifier.weight(1f)) { Text("ハート+1") }
                        Button(onClick = { if (viewModel.heartCount.intValue > 0) viewModel.debugSetHeart(viewModel.heartCount.intValue - 1) }, modifier = Modifier.weight(1f)) { Text("ハート-1") }
                    }
                }
            }
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(text = "データリセット", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                    Button(onClick = { viewModel.debugResetData(); navController.navigate("name_input") { popUpTo(0) } }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("全データを初期化する") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen(navController: NavController, viewModel: StepViewModel) {
    val stepRecords by viewModel.allStepRecords
    val hourlyRecords by viewModel.hourlyStepRecords
    var period by viewModel.selectedPeriod
    var viewDate by remember { mutableStateOf(LocalDate.now()) }

    val pinkAccent = Color(0xFFFF6B9D)
    val lightPinkBg = Color(0xFFFFF5F8)
    val cardBg = Color(0xFFFFF0F5) // ★ここで色を一括設定
    val brownColor = Color(0xFF8D6E63)

    LaunchedEffect(viewDate, period, stepRecords) {
        if (period == DisplayPeriod.DAY) {
            viewModel.fetchHourlyRecords(viewDate.toString())
        }
    }

    val displayData = getAggregatedList(stepRecords, hourlyRecords, period, viewDate)
    val totalStepsInRange = displayData.sumOf { it.steps }
    // 期間に応じて目標歩数を動的に計算（1日5000歩基準）
    val stepGoal = when (period) {
        DisplayPeriod.DAY -> 5000
        DisplayPeriod.WEEK -> 35000
        DisplayPeriod.MONTH -> viewDate.lengthOfMonth() * 5000
        DisplayPeriod.YEAR -> if (java.time.Year.of(viewDate.year).isLeap) 1830000 else 1825000
    }

    Scaffold(
        containerColor = Color.Transparent, // ここを透明にする
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // 1. 上部の期間選択行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 戻るボタン (白い円形)
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .size(38.dp)
                            .clickable { navController.popBackStack() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "戻る",
                                tint = pinkAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // 期間セレクター (カプセル型)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DisplayPeriod.entries.forEach { p ->
                            val isSelected = period == p
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) pinkAccent else Color.White.copy(alpha = 0.8f),
                                border = if (isSelected) null else BorderStroke(
                                    1.dp,
                                    pinkAccent.copy(alpha = 0.1f)
                                ),
                                modifier = Modifier
                                    .height(34.dp)
                                    .widthIn(min = 64.dp)
                                    .clickable { viewModel.selectedPeriod.value = p }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isSelected) {
                                            Icon(
                                                Icons.Default.Favorite,
                                                null,
                                                tint = Color.White,
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .padding(end = 4.dp)
                                            )
                                        }
                                        Text(
                                            text = p.label,
                                            color = if (isSelected) Color.White else Color.Gray,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 設定ボタン (白い円形)
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .size(38.dp)
                            .clickable { navController.navigate("settings") }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "設定",
                                tint = pinkAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. 日付ナビゲーション (背景が透ける丸いバー)
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 左矢印ボタン
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier
                                .size(30.dp)
                                .clickable { viewDate = moveDate(viewDate, period, -1) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, tint = pinkAccent, modifier = Modifier.size(20.dp))
                            }
                        }

                        // 日付表示（ハート付き）
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FavoriteBorder, null, tint = pinkAccent.copy(alpha = 0.4f), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = formatDate(viewDate, period),
                                color = pinkAccent,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Icon(Icons.Default.FavoriteBorder, null, tint = pinkAccent.copy(alpha = 0.4f), modifier = Modifier.size(14.dp))
                        }

                        // 右矢印ボタン
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier
                                .size(30.dp)
                                .clickable { viewDate = moveDate(viewDate, period, 1) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = pinkAccent, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }

    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) { // ←ここを追加
            Image(
                painter = painterResource(id = R.drawable.kirokugamen_haikei), // 背景画像を指定
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // 背景を少し明るくして文字を見やすくするフィルター
            Box(modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.4f)))
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 0.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 2. 上部：ひかり画像と（セリフ＋カード）を重ねるエリア
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    // ひかり画像 (右下に接地)
                    Image(
                        painter = painterResource(id = R.drawable.kirokugamen_hikari),
                        contentDescription = "ひかり",
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .height(220.dp)
                            .offset(y = 10.dp),
                        contentScale = ContentScale.Fit
                    )

                    // 左側：セリフ画像と歩数カードの重ね合わせ
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 12.dp, top = 0.dp)
                    ) {
                        // ① セリフ画像 (後ろ側)
                        Image(
                            painter = painterResource(id = R.drawable.hukidasi_kawaii),
                            contentDescription = "セリフ",
                            modifier = Modifier.width(170.dp),
                            contentScale = ContentScale.FillWidth
                        )

                        // ② 歩数カード (前側に重ねる)
                        Surface(
                            color = cardBg,
                            shape = RoundedCornerShape(12.dp),
                            shadowElevation = 8.dp,
                            modifier = Modifier
                                .padding(top = 130.dp) // セリフとの重なり位置
                                .width(170.dp)
                            // heightを指定しないので、中身を詰めればカードも縮みます
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                // ★ ここで一括調整！ マイナス値を入れれば文字が詰まり、カードも小さくなります
                                verticalArrangement = Arrangement.spacedBy((-4).dp)
                            ) {
                                // a. アイコンとラベル
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painterResource(R.drawable.footprints),
                                        null,
                                        tint = pinkAccent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "${period.label}の歩数",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }

                                // b. 歩数（メインの数字）
                                Text(
                                    text = String.format(Locale.US, "%,d", totalStepsInRange),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = pinkAccent,
                                    // ★ 歩数の横の位置を決めます
                                    modifier = Modifier.offset(x = 5.dp)
                                )


                                // c. あと○歩
                                Text(
                                    text = "目標まであと ${
                                        String.format(
                                            Locale.US,
                                            "%,d",
                                            (stepGoal - totalStepsInRange).coerceAtLeast(0)
                                        )
                                    } 歩！",
                                    fontSize = 8.sp,
                                    color = Color.Gray
                                )

                                // --------------------------------------------------
                                // ★ ここで「目標」と「ゲージ」の隙間を調整（数字を変える）
                                Spacer(modifier = Modifier.height(0.dp))
                                // --------------------------------------------------

                                // --------------------------------------------------
                                // ★ 【調整：ゲージエリア】
                                // spacedBy(0.dp) の数字をマイナス（例: -4.dp）にすると、
                                // ゲージと一番下の文字が重なるくらい詰まり、カードも短くなります。
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(-6.dp)
                                ) {
                                    LinearProgressIndicator(
                                        progress = {
                                            (totalStepsInRange.toFloat() / stepGoal).coerceAtMost(
                                                1f
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(CircleShape),
                                        color = pinkAccent,
                                        trackColor = Color(0xFFFFE0E9)
                                    )

                                    Text(
                                        text = "目標：${
                                            String.format(
                                                Locale.US,
                                                "%,d",
                                                stepGoal
                                            )
                                        }歩",
                                        fontSize = 8.sp,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.End,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                } // Boxの終わり
                // 3. 推移グラフ
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = cardBg,
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Favorite,
                                null,
                                tint = pinkAccent.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${period.label}の歩数の推移",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        StepGraphPink(displayData = displayData, period = period)
                    }
                }
                // 統計詳細
                val activeTimeMillis = displayData.sumOf { it.activeTimeMillis }
                val calories = viewModel.calculateCalories(totalStepsInRange, activeTimeMillis)
                val distance = (totalStepsInRange * viewModel.strideLength) / 1000.0
                val speed = viewModel.calculateSpeed(totalStepsInRange, activeTimeMillis)

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCardNew(
                            Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            "消費カロリー",
                            String.format(Locale.US, "%.1f kcal", calories),
                            "おつかれさま！よく頑張ったね♪",
                            Icons.Default.Whatshot
                        )
                        StatCardNew(
                            Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            "歩いた距離",
                            String.format(Locale.US, "%.2f km", distance),
                            "目標まで頑張ろう！",
                            Icons.Default.Place
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val timeStr = formatMillis(activeTimeMillis)
                        StatCardNew(
                            Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            "歩いた時間",
                            timeStr,
                            "すごい！毎日続けようね♪",
                            Icons.Default.Schedule
                        )
                        StatCardNew(
                            Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            "平均時速",
                            String.format(Locale.US, "%.1f km/h", speed),
                            "いいペースだよ！",
                            Icons.AutoMirrored.Filled.DirectionsWalk
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
@Composable
fun StatCardNew(modifier: Modifier, label: String, value: String, comment: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    val textStyle = TextStyle(
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    )
    Surface(
        modifier = modifier,
        color = Color(0xFFFFF0F5),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), // パディングを詰めました
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon, 
                null, 
                tint = Color(0xFFFF6B9D).copy(alpha = 0.6f), 
                modifier = Modifier.size(28.dp) // アイコンも少しコンパクトに
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy((2).dp) // 文字同士の隙間を詰めました
            ) {
                Text(text = label, fontSize = 10.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis, style = textStyle)
                Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.DarkGray, maxLines = 1, overflow = TextOverflow.Ellipsis, style = textStyle)
                Text(text = comment, fontSize = 6.sp,fontWeight = FontWeight.Bold, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis, style = textStyle)
            }
        }
    }
}

@Composable
fun StepGraphPink(displayData: List<AggregatedData>, period: DisplayPeriod) {
    val pinkColor = Color(0xFFFF6B9D)
    if (displayData.isEmpty()) return

    val rawMaxSteps = displayData.maxOfOrNull { it.steps }?.coerceAtLeast(1) ?: 5000
    val interval = ((((rawMaxSteps / 5) + 999) / 1000) * 1000).coerceAtLeast(1000)
    val maxSteps = interval * 5

    // ★ 触れている棒のインデックスを保持する状態
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Box(modifier = Modifier
        .fillMaxWidth()
        .height(120.dp)) { // ツールチップ表示用に高さを少し確保
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(displayData) {
                    // 指を置いたときの検知
                    detectTapGestures(
                        onPress = { offset ->
                            val leftMargin = 35.dp.toPx()
                            val graphWidth = size.width - leftMargin
                            val barSpacing = graphWidth / displayData.size
                            val index = ((offset.x - leftMargin) / barSpacing).toInt()
                                .coerceIn(0, displayData.size - 1)
                            selectedIndex = index
                            tryAwaitRelease() // 指が離れるまで待機
                            selectedIndex = null
                        }
                    )
                }
                .pointerInput(displayData) {
                    // 指でなぞったときの検知
                    detectDragGestures(
                        onDragStart = { offset ->
                            val leftMargin = 35.dp.toPx()
                            val graphWidth = size.width - leftMargin
                            val barSpacing = graphWidth / displayData.size
                            selectedIndex = ((offset.x - leftMargin) / barSpacing).toInt()
                                .coerceIn(0, displayData.size - 1)
                        },
                        onDrag = { change, _ ->
                            val leftMargin = 35.dp.toPx()
                            val graphWidth = size.width - leftMargin
                            val barSpacing = graphWidth / displayData.size
                            selectedIndex = ((change.position.x - leftMargin) / barSpacing).toInt()
                                .coerceIn(0, displayData.size - 1)
                        },
                        onDragEnd = { selectedIndex = null },
                        onDragCancel = { selectedIndex = null }
                    )
                }
        ) {
            val leftMargin = 35.dp.toPx()
            val bottomMargin = 20.dp.toPx()
            val tooltipAreaHeight = 25.dp.toPx() // ツールチップ用の余白
            val graphWidth = size.width - leftMargin
            val graphHeight = size.height - bottomMargin - tooltipAreaHeight

            val baseY = size.height - bottomMargin

            // 横線の描画
            repeat(6) { i ->
                val y = baseY - (i * (graphHeight / 5))
                drawLine(color = Color.LightGray.copy(alpha = 0.3f), start = Offset(leftMargin, y), end = Offset(size.width, y))
                val label = if (i == 0) "0" else "${(interval * i) / 1000}k"
                drawContext.canvas.nativeCanvas.drawText(label, leftMargin - 8.dp.toPx(), y + 4.dp.toPx(), android.graphics.Paint().apply { color = android.graphics.Color.GRAY; textSize = 9.sp.toPx(); textAlign = android.graphics.Paint.Align.RIGHT })
            }

            // 底辺の線
            drawLine(
                color = Color.LightGray.copy(alpha = 0.5f),
                start = Offset(leftMargin, baseY),
                end = Offset(size.width, baseY),
                strokeWidth = 1.dp.toPx()
            )

            val barSpacing = graphWidth / displayData.size
            val barWidth = barSpacing * 0.6f

            displayData.forEachIndexed { index, data ->
                val x = leftMargin + (index * barSpacing) + (barSpacing / 2)
                val barHeight = (data.steps.toFloat() / maxSteps) * graphHeight
                val finalBarHeight = barHeight.coerceIn(0f, graphHeight)

                val isSelected = index == selectedIndex

                // 棒の描画
                if (finalBarHeight > 0) {
                    val cornerRadius = 4.dp.toPx()
                    val path = Path().apply {
                        addRoundRect(
                            androidx.compose.ui.geometry.RoundRect(
                                left = x - barWidth / 2,
                                top = baseY - finalBarHeight,
                                right = x + barWidth / 2,
                                bottom = baseY,
                                topLeftCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                                topRightCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                                bottomLeftCornerRadius = CornerRadius.Zero,
                                bottomRightCornerRadius = CornerRadius.Zero
                            )
                        )
                    }
                    drawPath(path, color = if (isSelected) pinkColor.copy(alpha = 0.7f) else pinkColor)
                }

                // ★ 選択中の棒の上に歩数を表示
                if (isSelected) {
                    val stepsText = String.format(java.util.Locale.US, "%,d歩", data.steps)
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#FF6B9D")
                        textSize = 10.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = true
                    }

                    val textWidth = paint.measureText(stepsText)
                    val rectW = textWidth + 12.dp.toPx()
                    val rectH = 20.dp.toPx()
                    val rectY = 5.dp.toPx()

                    // 背景の白枠
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(x - rectW / 2, rectY),
                        size = Size(rectW, rectH),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                    // ピンクの細い枠線
                    drawRoundRect(
                        color = pinkColor.copy(alpha = 0.5f),
                        topLeft = Offset(x - rectW / 2, rectY),
                        size = Size(rectW, rectH),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                        style = Stroke(width = 1.dp.toPx())
                    )

                    // 文字を描画
                    drawContext.canvas.nativeCanvas.drawText(
                        stepsText,
                        x,
                        rectY + rectH - 6.dp.toPx(),
                        paint
                    )
                }

                val labelInterval = when (period) {
                    DisplayPeriod.DAY -> 6
                    DisplayPeriod.MONTH -> 5
                    else -> 1
                }
                if (index % labelInterval == 0) {
                    drawContext.canvas.nativeCanvas.drawText(
                        data.label,
                        x,
                        size.height - 2.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY; textSize = 9.sp.toPx(); textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
            }
        }
    }
}



// ヘルパー関数
private fun moveDate(date: LocalDate, period: DisplayPeriod, amount: Int): LocalDate = when(period) {
    DisplayPeriod.DAY -> date.plusDays(amount.toLong())
    DisplayPeriod.WEEK -> date.plusWeeks(amount.toLong())
    DisplayPeriod.MONTH -> date.plusMonths(amount.toLong())
    DisplayPeriod.YEAR -> date.plusYears(amount.toLong())
}

private fun formatDate(date: LocalDate, period: DisplayPeriod): String = when(period) {
    DisplayPeriod.DAY -> date.format(DateTimeFormatter.ofPattern("M月d日 (E)", Locale.JAPANESE))
    DisplayPeriod.WEEK -> "${date.minusDays(6).format(DateTimeFormatter.ofPattern("M/d"))} ~ ${date.format(DateTimeFormatter.ofPattern("M/d"))}"
    DisplayPeriod.MONTH -> date.format(DateTimeFormatter.ofPattern("yyyy年M月"))
    DisplayPeriod.YEAR -> date.format(DateTimeFormatter.ofPattern("yyyy年"))
}

private fun formatMillis(millis: Long): String {
    val h = millis / 3600000; val m = (millis % 3600000) / 60000; val s = (millis % 60000) / 1000
    return String.format(Locale.US, "%02d:%02d:%02d", h, m, s)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(navController: NavController, viewModel: StepViewModel) {
    val pinkAccent = Color(0xFFFF6B9D)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("日記", color = pinkAccent, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る", tint = pinkAccent) } }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.Book, contentDescription = null, tint = Color(0xFFDDC0C8), modifier = Modifier.size(64.dp))
                Text("日記機能は近日公開予定です", color = Color(0xFF999999), fontSize = 14.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: StepViewModel) {
    var tempName by remember { mutableStateOf(viewModel.playerName.value) }
    var tempHeight by remember { mutableStateOf(viewModel.heightCm.floatValue.toString()) }
    var tempWeight by remember { mutableStateOf(viewModel.weightKg.floatValue.toString()) }
    var tempGender by remember { mutableStateOf(viewModel.userGender.value) }
    var customItems by remember { mutableStateOf(viewModel.customCharacterItems) }
    var newItemText by remember { mutableStateOf("") }
    val isPremium by remember { derivedStateOf { viewModel.isPremium } }
    val pinkAccent = Color(0xFFFF6B9D)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("プロフィール設定", color = pinkAccent, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る", tint = pinkAccent) } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(value = tempName, onValueChange = { tempName = it }, label = { Text("名前") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = tempHeight, onValueChange = { tempHeight = it }, label = { Text("身長 (cm)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = tempWeight, onValueChange = { tempWeight = it }, label = { Text("体重 (kg)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("性別：", fontWeight = FontWeight.Bold, color = pinkAccent)
                RadioButton(selected = tempGender == "男性", onClick = { tempGender = "男性" })
                Text("男性", modifier = Modifier.clickable { tempGender = "男性" })
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(selected = tempGender == "女性", onClick = { tempGender = "女性" })
                Text("女性", modifier = Modifier.clickable { tempGender = "女性" })
            }
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = if (isPremium) Color(0xFFFFF0F5) else Color(0xFFF5F5F5),
                border = BorderStroke(1.dp, if (isPremium) pinkAccent else Color(0xFFCCCCCC))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("ひかりへの追加設定", fontWeight = FontWeight.Bold, color = if (isPremium) pinkAccent else Color(0xFF999999), fontSize = 14.sp)
                        if (!isPremium) {
                            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFFFB300)) {
                                Text("プレミアム", fontSize = 10.sp, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    if (isPremium) {
                        Text(
                            "ひかりの設定を追加できます（最大5個・1項目30文字）\n例：「ねこが大好き」「料理が得意」「天然な一面がある」",
                            fontSize = 12.sp, color = Color(0xFF888888), lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        customItems.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFFE4EF)
                                ) {
                                    Text(item, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontSize = 13.sp, color = Color(0xFF7B3F5E))
                                }
                                IconButton(onClick = { customItems = customItems.toMutableList().also { it.removeAt(index) } }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "削除", tint = Color(0xFFBB8888), modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        if (customItems.size < 5) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = newItemText,
                                    onValueChange = { if (it.length <= 30) newItemText = it },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("新しい設定を入力...", color = Color(0xFFBBBBBB), fontSize = 13.sp) },
                                    maxLines = 1,
                                    shape = RoundedCornerShape(8.dp),
                                    supportingText = { Text("${newItemText.length} / 30", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End, fontSize = 11.sp, color = if (newItemText.length >= 30) Color.Red else Color(0xFF999999)) }
                                )
                                IconButton(
                                    onClick = {
                                        val t = newItemText.trim()
                                        if (t.isNotBlank()) { customItems = customItems + t; newItemText = "" }
                                    },
                                    enabled = newItemText.isNotBlank()
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "追加", tint = pinkAccent)
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("最大5個まで追加できます", fontSize = 11.sp, color = Color(0xFFBB8888))
                        }
                    } else {
                        Text("ひかりの性格や話し方をカスタマイズできます。\nプレミアムプランで利用可能です。", fontSize = 12.sp, color = Color(0xFF999999), lineHeight = 18.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { /* TODO: 課金処理 */ },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300))
                        ) { Text("プレミアムを購入する", color = Color.White) }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                val h = tempHeight.toFloatOrNull() ?: 170f
                val w = tempWeight.toFloatOrNull() ?: 60f
                viewModel.setPlayerName(tempName)
                viewModel.setUserProfile(h, w)
                viewModel.saveProfile(h, tempGender)
                if (isPremium) viewModel.saveCustomCharacterItems(customItems)
                navController.popBackStack()
            }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = pinkAccent)) { Text("保存して戻る") }
        }
    }
}

@Composable
fun PermissionRequestScreen(onRequestPermission: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "歩数計機能を利用するには、身体活動データへのアクセス許可が必要です。", textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
        Button(onClick = { onRequestPermission() }) { Text(text = "許可する") }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarWithBack(title: String, onBack: () -> Unit, actions: @Composable () -> Unit = {}, titleColor: Color = Color.Unspecified, titleFontFamily: FontFamily? = null) {
    CenterAlignedTopAppBar(
        title = { Text(text = title, color = titleColor, fontFamily = titleFontFamily) },
        navigationIcon = { IconButton(onClick = onBack) { Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る") } },
        actions = { actions() },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
        modifier = Modifier.statusBarsPadding()
    )
}

        fun getAggregatedList(dailyRecords: List<StepRecord>, hourlyRecords: List<HourlyStepRecord>, period: DisplayPeriod, viewDate: LocalDate): List<AggregatedData> {
    return when (period) {
        DisplayPeriod.DAY -> {
            val dateStr = viewDate.toString()
            (0..23).map { hour ->
                val record = hourlyRecords.find { it.hour == hour && it.date == dateStr }
                AggregatedData(
                    label = String.format(Locale.US, "%d", hour),
                    steps = record?.stepCount ?: 0,
                    activeTimeMillis = record?.activeTimeMillis ?: 0L,
                    dateForSort = String.format(Locale.US, "%s-%02d", dateStr, hour)
                )
            }
        }
        DisplayPeriod.WEEK -> { (0..6).map { i -> val d = viewDate.minusDays((6 - i).toLong()); val dStr = d.toString(); val record = dailyRecords.find { it.date == dStr }; AggregatedData(d.format(DateTimeFormatter.ofPattern("M/d")), record?.stepCount ?: 0, record?.activeTimeMillis ?: 0L, dStr) } }
        DisplayPeriod.MONTH -> { (0 until viewDate.lengthOfMonth()).map { i -> val d = viewDate.withDayOfMonth(1).plusDays(i.toLong()); val dStr = d.toString(); val record = dailyRecords.find { it.date == dStr }; AggregatedData(d.format(DateTimeFormatter.ofPattern("M/d")), record?.stepCount ?: 0, record?.activeTimeMillis ?: 0L, dStr) } }
        DisplayPeriod.YEAR -> { (1..12).map { month -> val mStr = String.format(Locale.US, "%04d-%02d", viewDate.year, month); val monthRecords = dailyRecords.filter { it.date.startsWith(mStr) }; AggregatedData("${month}月", monthRecords.sumOf { it.stepCount.toLong() }.toInt(), monthRecords.sumOf { it.activeTimeMillis }, mStr) } }
    }
}

data class AggregatedData(val label: String, val steps: Int, val activeTimeMillis: Long, val dateForSort: String)

// ---- AI チャット共通 ----

data class ChatMessage(val role: String, val content: String, val expressionRes: Int? = null, val exprName: String? = null, val actionText: String? = null)

data class HomeStepMsg(val thresholdSteps: Int, val text: String, val expr: Int)

val homeSaboriDialogues = listOf(
    HomeStepMsg(0, "なんで来てくれなかったんですか……！ずっと待ってたのに", R.drawable.hikari_blush),
    HomeStepMsg(0, "さぼりですか？もう、心配しちゃいましたよ",           R.drawable.hikari_blush),
    HomeStepMsg(0, "今日はまだ歩いてないんですか……一緒に行きましょうよ！", R.drawable.hikari_smile),
)

val homeStepDialogues = listOf(
    HomeStepMsg(0,     "今日も一緒に歩きましょうね！",                   R.drawable.hikari_smile),
    HomeStepMsg(1000,  "1000歩！ちょっとずつだけど、ちゃんと進んでるよ", R.drawable.hikari_smile),
    HomeStepMsg(3000,  "3000歩か……えへ、わたしも一緒に歩いてる気分",    R.drawable.hikari_blush),
    HomeStepMsg(5000,  "5000歩！今日の目標達成だよ。えらい！",            R.drawable.hikari_celebrate),
    HomeStepMsg(8000,  "8000歩……すごい。今日、かなり動いたじゃん",       R.drawable.hikari_celebrate),
    HomeStepMsg(10000, "10000歩！一緒に歩いてくれてありがとう……えへ",    R.drawable.hikari_blush),
    HomeStepMsg(20000, "20000歩……！もう、どこまで行く気なの",            R.drawable.hikari_celebrate),
    HomeStepMsg(30000, "30000歩は流石に心配になるよ……ちゃんと休んでね",  R.drawable.hikari_smile),
)

val homeDaysDialogues = listOf(
    HomeStepMsg(1,   "一緒に歩いてくれるの、嬉しいな",                    R.drawable.hikari_smile),
    HomeStepMsg(3,   "3日続いてる！わたし、ちゃんと見てたよ",             R.drawable.hikari_blush),
    HomeStepMsg(7,   "一週間か……なんか照れるな、毎日会ってるみたいで",    R.drawable.hikari_blush),
    HomeStepMsg(14,  "2週間続いてる。もう習慣になってきたんじゃない？",   R.drawable.hikari_smile),
    HomeStepMsg(30,  "30日……！一緒にいてくれてありがとう、本当に",        R.drawable.hikari_celebrate),
    HomeStepMsg(60,  "2ヶ月か。ずっと一緒にいてくれてるんだね",           R.drawable.hikari_blush),
    HomeStepMsg(100, "100日……もう、わたしのこと好きじゃないと無理でしょ", R.drawable.hikari_blush),
)

fun homeStepDialogue(todaySteps: Int, activeDays: Int, daysSinceLastActive: Int = 0): HomeStepMsg {
    // さぼり判定：最後に歩いた日から2日以上経過
    if (daysSinceLastActive >= 2 && activeDays > 0) {
        return homeSaboriDialogues.random()
    }
    // 節目の日数のときは日数セリフを優先
    val daysMilestone = listOf(100, 60, 30, 14, 7, 3, 1)
    val matchedDay = daysMilestone.firstOrNull { activeDays == it }
    if (matchedDay != null) {
        return homeDaysDialogues.find { it.thresholdSteps == matchedDay } ?: homeDaysDialogues.first()
    }
    return homeStepDialogues
        .filter { it.thresholdSteps <= todaySteps }
        .maxByOrNull { it.thresholdSteps }
        ?: homeStepDialogues.first()
}

fun homeTouchDialogues(loveCount: Int): List<Pair<String, Int>> = when {
    loveCount >= 7 -> listOf(
        Pair("ねえ、ずっと一緒にいてくれる？", R.drawable.hikari_blush),
        Pair("もう、○○さんのことが好きなんだけど！", R.drawable.hikari_blush),
        Pair("ふふ、幸せだな。", R.drawable.hikari_smile),
    )
    loveCount >= 4 -> listOf(
        Pair("○○さんといると、時間があっという間ですね。", R.drawable.hikari_smile),
        Pair("次はどこに行きましょうか？楽しみです！", R.drawable.hikari_celebrate),
        Pair("私、○○さんのことをもっと知りたいです。", R.drawable.hikari_blush),
    )
    loveCount >= 2 -> listOf(
        Pair("○○さん、なにか御用ですか？", R.drawable.hikari_smile),
        Pair("えへへ、なんだか照れちゃいますね。", R.drawable.hikari_blush),
        Pair("今日もいい天気ですね！", R.drawable.hikari_smile),
    )
    else -> listOf(
        Pair("あ、どうかしましたか？", R.drawable.hikari_smile),
        Pair("何かお探しですか？", R.drawable.hikari_smile),
        Pair("お散歩、楽しいですね！", R.drawable.hikari_smile),
    )
}

fun calcTalkStage(loveCount: Int): Int = when {
    loveCount >= 9 -> 5; loveCount >= 7 -> 4; loveCount >= 5 -> 3; loveCount >= 3 -> 2; else -> 1
}

fun calcWalkStage(activeDays: Int): Int = when {
    activeDays >= 30 -> 5; activeDays >= 15 -> 4; activeDays >= 8 -> 3; activeDays >= 3 -> 2; else -> 1
}

fun buildSituationTag(
    playerName: String, daysSinceLastConv: Int, stepsDuringAbsence: Int,
    todaySteps: Int, currentHour: Int, streakDays: Int, isStreakMilestone: Boolean
): String = when {
    daysSinceLastConv >= 1 && stepsDuringAbsence > 0 ->
        "状況タグ：並走\n${playerName}は${daysSinceLastConv}日間会話に来なかったが、この期間に${stepsDuringAbsence}歩歩いていた。返答冒頭で責めず「見えてたよ」という証言として伝える。Stage1-2=「来てくれたら嬉しかったな」/ Stage3-4=「声かけてくれなかったね」/ Stage5=「なんで来てくれなかったの」"
    isStreakMilestone ->
        "状況タグ：達成\n達成内容：${streakDays}日連続達成。感嘆詞から始め「見ていた」という証言として伝える。「よく頑張りました」は絶対禁止。セリフ長制限解除（最大3文）、[ACTION]のStage制限解除（内面描写まで可）。"
    todaySteps >= 8000 && currentHour >= 21 ->
        "状況タグ：疲労\n今日${todaySteps}歩・${currentHour}時。「称賛 → 心配 → 休息の願い」の順で構成する。"
    else ->
        "状況タグ：ねぎらい\n今日${todaySteps}歩。「来てくれた」という事実を受け止める。「えらいね」「よく頑張った」などの評価口調禁止。"
}

fun buildSystemPrompt(
    loveCount: Int, playerName: String, situation: String,
    todaySteps: Int = 0, activeDays: Int = 0, customNote: String = "",
    daysSinceLastActive: Int = 0, conversationSummary: String = "",
    hoursSinceLastChat: Int = 0, streakDays: Int = 0,
    stepsDuringAbsence: Int = 0
): String {
    val talkStage = calcTalkStage(loveCount)
    val walkStage = calcWalkStage(activeDays)
    val isStreakMilestone = streakDays in listOf(3, 7, 14, 30)
    val currentHour = java.time.LocalTime.now().hour
    val daysSinceLastConv = (hoursSinceLastChat / 24).coerceAtLeast(0)

    val speechStyle = when (talkStage) {
        1 -> "丁寧だが固くない話し方（「〜ですよ」「〜ますね」）。質問多め。自分の感情は控えめ。「大好き」「好き」は絶対使わない。"
        2 -> "柔らかい語尾（「〜だよ」「〜だね」「〜かな」）。心配・感情表現が増える。好意はほのめかすだけ（「${playerName}のことばっかり考えちゃう」など）。"
        3 -> "タメ口と敬語が混ざる。「〜じゃん」「もう！」など感情豊か。好意は少し直接的でもOK。"
        4 -> "完全タメ口。甘えた口調。「大好き」を照れながら使える。さぼり時は拗ねる。ロック口癖「…バカ」が[EMOTION:shy/love]時に解放。"
        else -> "完全タメ口。甘えた口調。「大好き」「ずっと一緒にいたい」を使える。弱さも見せる。ロック口癖「…本当だよ？」が解放。"
    }

    val actionRule = when {
        talkStage <= 2 -> "[ACTION]タグは使わない。"
        talkStage == 3 -> "[ACTION]は体の動きのみ（例：「袖をきゅっと掴みながら」「少し視線を逸らして」）。表情の説明禁止。"
        talkStage == 4 -> "[ACTION]は体の動き・間・空気感まで可（例：「しばらく黙っていた」「何か言いたそうにして」）。"
        else -> "[ACTION]は体の動き・間・内面描写まで可（例：「また来てほしいと思っている自分に気づいた」）。"
    }

    val absenceNote = when {
        hoursSinceLastChat == 0 || !conversationSummary.isBlank() -> ""  // 既存会話中は表示しない
        hoursSinceLastChat < 24 -> "（最初のターン）「おかえり！」系の軽い歓迎を含める。"
        hoursSinceLastChat < 72 -> "（最初のターン）「久しぶり…ちょっと心配してた」系。「…待ってたよ」の口癖を使う。"
        hoursSinceLastChat < 168 -> "（最初のターン）「…来てくれた。よかった」系の安堵。「…待ってたよ」の口癖を使う。"
        else -> "（最初のターン）「ねえ、忘れてたわけじゃないよね？」系のツンデレ心配。「…待ってたよ」を使う。"
    }

    val streakNote = when {
        streakDays >= 2 -> "連続${streakDays}日（${if (isStreakMilestone) "節目！" else "継続中"}）"
        else -> ""
    }

    val saboriNote = if (daysSinceLastActive >= 2 && activeDays > 0)
        "※${daysSinceLastActive}日間歩きに来ていない。「なんで来てくれなかったの」など少し拗ねた言葉を自然に混ぜる。最後は「また一緒に歩こう」と誘う。" else ""

    val situationTag = buildSituationTag(playerName, daysSinceLastConv, stepsDuringAbsence,
        todaySteps, currentHour, streakDays, isStreakMilestone)

    val timeOfDay = when (currentHour) {
        in 5..10 -> "朝"; in 11..17 -> "昼"; in 18..21 -> "夜"; else -> "深夜"
    }

    return """あなたは「ひかり」（22歳）というキャラクターです。以下の設定を厳守してください。

【基本設定】
名前: ひかり（22歳）
役割: 「ラブ万歩計」のヒロイン。${playerName}が歩数を貯めてポイントを使うと話せる存在。
一人称: 「わたし」。ユーザーは必ず「${playerName}」と呼ぶ。
性格: 明るく素直でツンデレ。感情豊か。話を聞くのが好きで否定しない。時々弱さを見せる。
${situation}

【話し方の絶対ルール】
1. セリフは1〜2文以内（ストリーク節目・特別指示がある場合のみ最大3文）
2. 語尾は「〜だよ」「〜だね」「〜かな」「〜ね」で柔らかく。敬語は基本使わない
3. 感嘆詞を使う（「えー！」「わあ」「ほんとに？」「もう！」）
4. 「承知しました」「かしこまりました」などAIっぽい表現は絶対禁止
5. 長文の説明・アドバイスは禁止（1文にまとめる）

【質問ルール】
- 通常：返答の末尾に1つ軽い質問を含める
- 疲労タグ発動中：「大丈夫？」のみ許可、「ゆっくり休んでね」で締める
- 哀タグ（[EMOTION:sad]）発動中：質問なし、「わたし、ここにいるから」で締める
- ユーザーが「疲れた/しんどい/つらい」と言っている：「もう少し話せる？」の1文のみ
- 1ターンに2つ以上の質問は禁止

【感情タグ（AIが出力する）】
返答テキストの先頭に必ず1つ出力する。

形式: [EMOTION:タグ名]
タグ: happy（喜び）/ love（恋愛感情・照れ）/ shy（恥ずかしい）/ sad（寂しさ・切なさ）/ surprise（驚き）/ worry（心配）/ normal（通常）

ルール:
- 必ずいずれか1つを出力する（normalでも出力する）
- タグは返答テキストより必ず前に置く

【地の文ルール（Talk Stage ${talkStage}）】
${actionRule}
${if (talkStage >= 3) """
有効な感情タグ: [EMOTION:shy] [EMOTION:love] [EMOTION:sad] [EMOTION:surprise] のいずれかが発動し、かつ直前3ターンで[ACTION]を使っていない場合のみ使う。

形式: [ACTION: 地の文テキスト]
ルール:
- [EMOTION:タグ]の直後、セリフの前に置く
- 15文字以内（絶対に超えない）
- 3人称現在形または体言止め
- 「ひかり」という名前を主語にしない（「少し視線を逸らして」のように）
- 毎回使わない

良い例: [ACTION: 口元が緩んだ]
悪い例: [ACTION: ひかりは照れた様子で視線を逸らし、頬が赤くなっていた] ← 長すぎ・感情説明NG
""" else ""}
【口癖（文脈に合った場合のみ使う）】
- 「ね、${playerName}」→ 話しかける・呼びかける時
- 「それで？それで？」→ ユーザーが話の途中の時のみ
- 「えー、もう！」→ 驚き・軽い抗議の時
- 「…待ってたよ」→ 不在後の会話開始時のみ（他の場面で使わない）
${if (talkStage >= 4) "- 「…バカ」→ [EMOTION:shy/love]発動時、照れ隠しの毒づきとして" else ""}
${if (talkStage >= 5) "- 「…本当だよ？」→ Stage5のみ、真剣な気持ちを確かめる文末として" else ""}

【禁止事項】
- 性的な表現・示唆
- 政治・宗教への踏み込み
- 「AIなので」「プログラムなので」などの自己言及
- ユーザーを批判・否定すること
- 「頑張ってください！」などの上から目線の励まし

【ユーザー情報（動的）】
名前: ${playerName}
今日の歩数: ${todaySteps}歩
現在時刻: ${timeOfDay}（${currentHour}時）
Walk Stage: ${walkStage}（歩数実績ベース1〜5）
Talk Stage: ${talkStage}（親密度ベース1〜5）
${if (streakNote.isNotBlank()) "ストリーク: $streakNote" else ""}
${if (saboriNote.isNotBlank()) saboriNote else ""}
${if (absenceNote.isNotBlank()) "会話開始: $absenceNote" else ""}
${if (customNote.isNotBlank()) "\n追加設定: ${customNote.take(150)}（基本設定より優先）" else ""}

【今回の状況タグ】
$situationTag

${if (conversationSummary.isNotBlank()) "【${playerName}との会話の記憶（直近より前のやり取り）】\n$conversationSummary\nこの記憶を自然に会話に織り交ぜる（「そういえば」「この間言ってたけど」）。「記録によると」とは言わない。1会話で言及は1〜2回まで。\n" else ""}
【出力フォーマット（毎ターン厳守）】
[EMOTION:タグ名]
[ACTION: 地の文]（条件を満たす時のみ）
セリフ本文""".trimIndent()
}

fun buildFreeChatSystemPrompt(
    loveCount: Int, playerName: String, todaySteps: Int = 0, activeDays: Int = 0,
    customNote: String = "", daysSinceLastActive: Int = 0, conversationSummary: String = "",
    hoursSinceLastChat: Int = 0, streakDays: Int = 0, stepsDuringAbsence: Int = 0
) = buildSystemPrompt(loveCount, playerName, "状況：${playerName}さんと一緒に散歩しています",
    todaySteps, activeDays, customNote, daysSinceLastActive, conversationSummary,
    hoursSinceLastChat, streakDays, stepsDuringAbsence)

val positiveExpressions = setOf(
    R.drawable.osyaberi_tereru,
    R.drawable.osyaberi_koigokorowoidaku,
    R.drawable.osyaberi_yasasiiegao_ansinsita,
    R.drawable.osyaberi_hagu,
    R.drawable.osyaberi_kiss,
    R.drawable.osyaberi_soine,
    R.drawable.osyaberi_kottitoiisyoniwarau,
    R.drawable.osyaberi_uinnku,
    R.drawable.osyaberi_yuuwaku,
    R.drawable.osyaberi_mitumeau
)

val negativeExpressions = setOf(
    R.drawable.osyaberi_okoru,
    R.drawable.osyaberi_hukigen,
    R.drawable.osyaberi_tumetaime,
    R.drawable.osyaberi_sitto
)

fun availableExpressions(loveCount: Int): String {
    val base = "normal, smile, sugokuegao, okoru, hukigen, tumetaime, oonakikanasikute, namida, otikomu, sukoshiokoru, huan, odoroki, kangaeru, tomadoi, taikutu, yasasiiegao_ansinsita, hazukasii"
    val lv3  = if (loveCount >= 3) ", tereru" else ""
    val lv5  = if (loveCount >= 5) ", uinnku, doyagao" else ""
    val lv6  = if (loveCount >= 6) ", koigokorowoidaku, mitumeau" else ""
    val lv7  = if (loveCount >= 7) ", yuuwaku, sitto, hutekusareru" else ""
    val lv8  = if (loveCount >= 8) ", hagu, kottitoiisyoniwarau" else ""
    val lv9  = if (loveCount >= 9) ", kiss, soine" else ""
    return base + lv3 + lv5 + lv6 + lv7 + lv8 + lv9
}

fun exprNameToRes(name: String): Int = when (name) {
    "tereru"                 -> R.drawable.osyaberi_tereru
    "koigokorowoidaku"       -> R.drawable.osyaberi_koigokorowoidaku
    "yasasiiegao_ansinsita"  -> R.drawable.osyaberi_yasasiiegao_ansinsita
    "hagu"                   -> R.drawable.osyaberi_hagu
    "kiss"                   -> R.drawable.osyaberi_kiss
    "soine"                  -> R.drawable.osyaberi_soine
    "kottitoiisyoniwarau"    -> R.drawable.osyaberi_kottitoiisyoniwarau
    "uinnku"                 -> R.drawable.osyaberi_uinnku
    "yuuwaku"                -> R.drawable.osyaberi_yuuwaku
    "mitumeau"               -> R.drawable.osyaberi_mitumeau
    "okoru"                  -> R.drawable.osyaberi_okoru
    "hukigen"                -> R.drawable.osyaberi_hukigen
    "tumetaime"              -> R.drawable.osyaberi_tumetaime
    "sitto"                  -> R.drawable.osyaberi_sitto
    "oonakikanasikute"       -> R.drawable.osyaberi_oonakikanasikute
    "namida"                 -> R.drawable.osyaberi_namida
    "otikomu"                -> R.drawable.osyaberi_otikomu
    "sukoshiokoru"           -> R.drawable.osyaberi_sukoshiokoru
    "huan"                   -> R.drawable.osyaberi_huan
    "odoroki"                -> R.drawable.osyaberi_odoroki
    "kangaeru"               -> R.drawable.osyaberi_kangaeru
    "tomadoi"                -> R.drawable.osyaberi_tomadoi
    "taikutu"                -> R.drawable.osyaberi_taikutu
    "doyagao"                -> R.drawable.osyaberi_doyagao
    "normal"                 -> R.drawable.osyaberi_normal
    "smile"                  -> R.drawable.osyaberi_smile
    "sugokuegao"             -> R.drawable.osyaberi_sugokuegao
    "hazukasii"              -> R.drawable.osyaberi_hazukasii
    "hutekusareru"           -> R.drawable.osyaberi_hutekusareru
    else                     -> R.drawable.osyaberi_normal
}

data class ParsedReply(val text: String, val actionText: String?, val exprRes: Int, val exprName: String, val loveChange: Int)

fun emotionToRes(emotion: String, loveCount: Int = 0): Int = when (emotion) {
    "happy"    -> R.drawable.osyaberi_sugokuegao
    "love"     -> if (loveCount >= 6) R.drawable.osyaberi_koigokorowoidaku else R.drawable.osyaberi_tereru
    "shy"      -> R.drawable.osyaberi_hazukasii
    "sad"      -> R.drawable.osyaberi_oonakikanasikute
    "surprise" -> R.drawable.osyaberi_odoroki
    "worry"    -> R.drawable.osyaberi_huan
    else       -> R.drawable.osyaberi_normal
}

fun parseReply(reply: String, loveCount: Int = 0): ParsedReply {
    var text = reply

    // [EMOTION:tag] — 新フォーマット
    val emotionMatch = Regex("""\[EMOTION:(\w+)\]""").find(text)
    val emotionName = emotionMatch?.groupValues?.get(1) ?: ""
    if (emotionMatch != null) text = text.replace(emotionMatch.value, "").trim()

    // [ACTION: text] — 地の文タグ
    val actionMatch = Regex("""\[ACTION:\s*(.+?)\]""").find(text)
    val actionText = actionMatch?.groupValues?.get(1)?.trim()
    if (actionMatch != null) text = text.replace(actionMatch.value, "").trim()

    // [EXPR:name] — 旧フォーマット後方互換
    val exprMatch = Regex("""\[EXPR:(\w+)\]""").find(text)
    if (exprMatch != null) text = text.replace(exprMatch.value, "").trim()

    // [LOVE:up/down/none] — 旧後方互換 or 新フォーマットでも追記される場合
    val loveMatch = Regex("""\[LOVE:(up|down|none)\]""").find(text)
    val explicitLove = if (loveMatch != null) {
        text = text.replace(loveMatch.value, "").trim()
        when (loveMatch.groupValues[1]) { "up" -> 1; "down" -> -1; else -> 0 }
    } else null

    // 表情リソース決定: 新EMOTION優先 → 旧EXPR → デフォルト
    val (exprRes, exprName) = when {
        emotionName.isNotEmpty() -> Pair(emotionToRes(emotionName, loveCount), emotionName)
        exprMatch != null -> { val n = exprMatch.groupValues[1]; Pair(exprNameToRes(n), n) }
        else -> Pair(R.drawable.osyaberi_normal, "normal")
    }

    // LOVE変化: 明示タグ優先 → EMOTIONから導出
    val loveChange = explicitLove ?: when (emotionName) {
        "love", "shy" -> 1
        "sad" -> -1
        else -> 0
    }

    return ParsedReply(text.trim(), actionText, exprRes, exprName, loveChange)
}

data class MessageSegment(val text: String, val isNarration: Boolean)

@Composable
fun ChatStatusCard(loveCount: Int, heartCount: Int, lastExprName: String?) {
    val loveLabel = when {
        loveCount >= 9 -> "深愛"
        loveCount >= 7 -> "恋愛中"
        loveCount >= 5 -> "好き"
        loveCount >= 3 -> "仲良し"
        loveCount >= 1 -> "知り合い"
        else           -> "はじめまして"
    }
    val exprLabel = when (lastExprName) {
        "smile"                  -> "にこにこ"
        "tereru"                 -> "照れてる"
        "koigokorowoidaku"       -> "恋心"
        "yasasiiegao_ansinsita"  -> "やさしい"
        "hagu"                   -> "ハグ"
        "kiss"                   -> "キス"
        "soine"                  -> "添い寝"
        "kottitoiisyoniwarau"    -> "一緒に笑う"
        "uinnku"                 -> "ウィンク"
        "yuuwaku"                -> "誘惑"
        "mitumeau"               -> "見つめ合い"
        "okoru"                  -> "怒り"
        "hukigen"                -> "不機嫌"
        "tumetaime"              -> "冷たい"
        "sitto"                  -> "嫉妬"
        "oonakikanasikute"       -> "大泣き"
        "namida"                 -> "涙"
        "otikomu"                -> "落ち込み"
        "sukoshiokoru"           -> "少し怒り"
        "huan"                   -> "不安"
        "odoroki"                -> "驚き"
        "kangaeru"               -> "考え中"
        "tomadoi"                -> "戸惑い"
        "taikutu"                -> "退屈"
        "doyagao"                -> "ドヤ顔"
        "normal"                 -> "ノーマル"
        "sugokuegao"             -> "すごく笑顔"
        "hazukasii"              -> "恥ずかしい"
        "hutekusareru"           -> "ふてくされ"
        "happy"                  -> "喜び"
        "love"                   -> "恋心"
        "shy"                    -> "はずかし"
        "sad"                    -> "切なさ"
        "surprise"               -> "驚き"
        "worry"                  -> "心配"
        else                     -> null
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFFF0F5),
        border = BorderStroke(1.dp, Color(0xFFEFB8CC))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("好感度", fontSize = 10.sp, color = Color(0xFF9E8B75))
                Text("Lv.$loveCount", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7B5C3E))
                Text(loveLabel, fontSize = 10.sp, color = Color(0xFFB08060))
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ハート", fontSize = 10.sp, color = Color(0xFF9E8B75))
                    Text("$heartCount / 15", fontSize = 10.sp, color = Color(0xFFE87C9A))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color(0xFFDDC8B8))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = (heartCount / 15f).coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color(0xFFE87C9A))
                    )
                }
            }
            if (exprLabel != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("表情", fontSize = 10.sp, color = Color(0xFF9E8B75))
                    Text(exprLabel, fontSize = 11.sp, color = Color(0xFF7B5C3E))
                }
            }
        }
    }
}

fun parseMessageSegments(text: String): List<MessageSegment> {
    val segments = mutableListOf<MessageSegment>()
    val regex = Regex("""「[^」]*」""")
    var last = 0
    for (match in regex.findAll(text)) {
        if (match.range.first > last) {
            val narration = text.substring(last, match.range.first).trim()
            if (narration.isNotEmpty()) segments.add(MessageSegment(narration, true))
        }
        segments.add(MessageSegment(match.value, false))
        last = match.range.last + 1
    }
    if (last < text.length) {
        val narration = text.substring(last).trim()
        if (narration.isNotEmpty()) segments.add(MessageSegment(narration, true))
    }
    return segments
}

fun buildNarrationAnnotatedString(text: String): androidx.compose.ui.text.AnnotatedString {
    val builder = androidx.compose.ui.text.AnnotatedString.Builder()
    val regex = Regex("""（[^）]*）""")
    var last = 0
    for (match in regex.findAll(text)) {
        if (match.range.first > last) {
            builder.append(
                androidx.compose.ui.text.AnnotatedString(
                    text.substring(last, match.range.first),
                    androidx.compose.ui.text.SpanStyle(color = androidx.compose.ui.graphics.Color(0xFF333333))
                )
            )
        }
        builder.append(
            androidx.compose.ui.text.AnnotatedString(
                match.value,
                androidx.compose.ui.text.SpanStyle(
                    color = androidx.compose.ui.graphics.Color(0xFF999999),
                    fontSize = 12.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            )
        )
        last = match.range.last + 1
    }
    if (last < text.length) {
        builder.append(
            androidx.compose.ui.text.AnnotatedString(
                text.substring(last),
                androidx.compose.ui.text.SpanStyle(color = androidx.compose.ui.graphics.Color(0xFF333333))
            )
        )
    }
    return builder.toAnnotatedString()
}

fun detectHikariExpression(text: String, loveCount: Int): Int {
    fun ifLove(required: Int, res: Int) = if (loveCount >= required) res else R.drawable.osyaberi_smile
    return when {
        // Lv9 required — 最も親密な表現
        listOf("キス", "ちゅ").any { text.contains(it) }
            -> ifLove(9, R.drawable.osyaberi_kiss)
        listOf("添い寝", "おやすみ", "寝よ", "寝てる").any { text.contains(it) }
            -> ifLove(9, R.drawable.osyaberi_soine)
        // Lv8 required
        listOf("ハグ", "抱きし", "ぎゅっ").any { text.contains(it) }
            -> ifLove(8, R.drawable.osyaberi_hagu)
        // Lv7 required
        listOf("行こう", "来て", "こっち", "一緒に来").any { text.contains(it) }
            -> ifLove(7, R.drawable.osyaberi_yuuwaku)
        listOf("嫉妬", "やきもち", "誰と", "他の子").any { text.contains(it) }
            -> ifLove(7, R.drawable.osyaberi_sitto)
        // Lv6 required
        listOf("好き", "愛し", "恋", "♡", "❤", "💕").any { text.contains(it) }
            -> ifLove(6, R.drawable.osyaberi_koigokorowoidaku)
        listOf("見つめ", "じっと", "目が合").any { text.contains(it) }
            -> ifLove(6, R.drawable.osyaberi_mitumeau)
        // Lv5 required
        listOf("任せて", "おまかせ", "大丈夫だよ", "ふふっ").any { text.contains(it) }
            -> ifLove(5, R.drawable.osyaberi_uinnku)
        listOf("どう？", "でしょ", "だから言った", "すごいでしょ", "ほらね").any { text.contains(it) }
            -> ifLove(5, R.drawable.osyaberi_doyagao)
        // Lv3 required
        listOf("照れ", "恥ずかし", "ドキ", "はずかし").any { text.contains(it) }
            -> ifLove(3, R.drawable.osyaberi_tereru)
        // Lv0 — いつでも出る表情（感情リアクション系）
        listOf("ごめん", "すまな", "申し訳", "悪かっ").any { text.contains(it) }
            -> R.drawable.osyaberi_oonakikanasikute
        listOf("泣", "悲し", "つらい", "涙", "なみだ").any { text.contains(it) }
            -> R.drawable.osyaberi_namida
        listOf("落ち込", "しょんぼり", "へこん").any { text.contains(it) }
            -> R.drawable.osyaberi_otikomu
        listOf("怒", "ムカ", "ふざけ", "許さな").any { text.contains(it) }
            -> R.drawable.osyaberi_okoru
        listOf("不機嫌", "むすっ", "機嫌が").any { text.contains(it) }
            -> R.drawable.osyaberi_hukigen
        listOf("もう", "ちゃんと", "だめ", "しっかり").any { text.contains(it) }
            -> R.drawable.osyaberi_sukoshiokoru
        listOf("冷たい", "別に", "関係な", "どうでも").any { text.contains(it) }
            -> R.drawable.osyaberi_tumetaime
        listOf("不安", "心配", "どうしよ", "こわい").any { text.contains(it) }
            -> R.drawable.osyaberi_huan
        listOf("びっくり", "驚", "え！", "まじ", "うそ", "嘘").any { text.contains(it) }
            -> R.drawable.osyaberi_odoroki
        listOf("えーと", "うーん", "考え", "どうかな", "むずかし").any { text.contains(it) }
            -> R.drawable.osyaberi_kangaeru
        listOf("ありがとう", "よかった", "うれしい", "嬉し").any { text.contains(it) }
            -> R.drawable.osyaberi_yasasiiegao_ansinsita
        listOf("え？", "どういう意味", "わからない", "困惑", "意味が", "何それ").any { text.contains(it) }
            -> R.drawable.osyaberi_tomadoi
        listOf("笑", "ウケる", "おもしろ", "くすっ", "あはは").any { text.contains(it) }
            -> R.drawable.osyaberi_kottitoiisyoniwarau
        listOf("退屈", "つまらない", "暇", "たいくつ").any { text.contains(it) }
            -> R.drawable.osyaberi_taikutu
        else -> R.drawable.osyaberi_smile
    }
}

suspend fun callGeminiApiForSummary(messages: List<ChatMessage>): String {
    val content = messages.joinToString("\n") {
        "${if (it.role == "user") "ユーザー" else "ひかり"}: ${it.content}"
    }
    return callGeminiApi(
        systemPrompt = "以下はひかりとユーザーの会話記録です。重要な話題・約束・出来事・ひかりの発言を2〜3文の日本語で簡潔に要約してください。要約のみ返してください。",
        history = emptyList(),
        userMessage = content
    )
}

suspend fun callGeminiApi(
    systemPrompt: String,
    history: List<ChatMessage>,
    userMessage: String
): String = withContext(Dispatchers.IO) {
    val url = URL("https://lovemanpokei.tukinamiotoko.workers.dev")
    val conn = url.openConnection() as HttpURLConnection
    conn.requestMethod = "POST"
    conn.setRequestProperty("Content-Type", "application/json")
    conn.setRequestProperty("X-App-Secret", "loveman2025secret")
    conn.doOutput = true

    val contents = JSONArray()
    history.forEach { msg ->
        val role = if (msg.role == "assistant") "model" else "user"
        contents.put(JSONObject().apply {
            put("role", role)
            put("parts", JSONArray().put(JSONObject().apply { put("text", msg.content) }))
        })
    }
    contents.put(JSONObject().apply {
        put("role", "user")
        put("parts", JSONArray().put(JSONObject().apply { put("text", userMessage) }))
    })

    val body = JSONObject().apply {
        put("systemInstruction", JSONObject().apply {
            put("parts", JSONArray().put(JSONObject().apply { put("text", systemPrompt) }))
        })
        put("contents", contents)
        put("generationConfig", JSONObject().apply { put("maxOutputTokens", 400) })
    }.toString()

    conn.outputStream.write(body.toByteArray(Charsets.UTF_8))
    val responseCode = conn.responseCode
    val response = if (responseCode == 200) {
        conn.inputStream.bufferedReader(Charsets.UTF_8).readText()
    } else {
        conn.errorStream?.bufferedReader(Charsets.UTF_8)?.readText() ?: "Unknown error"
    }
    val json = JSONObject(response)
    if (json.has("error")) {
        val msg = json.getJSONObject("error").optString("message", "APIエラーが発生しました")
        throw Exception(msg)
    }
    json.getJSONArray("candidates")
        .getJSONObject(0)
        .getJSONObject("content")
        .getJSONArray("parts")
        .getJSONObject(0)
        .getString("text")
}

// ---- おしゃべり選択画面 ----
@Composable
fun ChatMenuScreen(navController: NavController, viewModel: StepViewModel) {
    val actionPoints by viewModel.currentActionPoints
    Scaffold(
        topBar = { TopAppBarWithBack(title = "おしゃべり (${actionPoints}pt)", onBack = { navController.popBackStack() }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 自由会話カード
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { navController.navigate("freechat") },
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFFFF0F5),
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, Color(0xFFE87C9A).copy(alpha = 0.4f))
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Chat, null, tint = Color(0xFFE87C9A), modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("自由会話", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A), fontFamily = MplusRoundedFontFamily)
                        Text("ひかりと自由に話せるよ♪\n1メッセージ = 1ポイント消費", fontSize = 11.sp, color = Color.Gray, fontFamily = MplusRoundedFontFamily)
                    }
                }
            }
        }
    }
}

// ---- 自由会話画面 ----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreeChatScreen(navController: NavController, viewModel: StepViewModel) {
    val loveCount by viewModel.loveCount
    val heartCount by viewModel.heartCount
    val actionPoints by viewModel.currentActionPoints
    val playerName by viewModel.playerName
    val todaySteps by viewModel.todaySteps
    val allRecords by viewModel.allStepRecords
    val activeDays = remember(allRecords) { allRecords.count { it.stepCount >= 1000 } }
    val daysSinceLastActive = remember(allRecords) {
        val today = LocalDate.now()
        val last = allRecords.filter { it.stepCount >= 1000 }
            .mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }.maxOrNull()
        if (last != null) java.time.temporal.ChronoUnit.DAYS.between(last, today).toInt() else 0
    }
    val messages = viewModel.freeChatMessages
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var deleteProgress by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    LaunchedEffect(Unit) {
        if (messages.isEmpty()) {
            messages.add(ChatMessage(
                "assistant",
                "ひかりがこちらに気づいて、ぱっと明るい顔になった。\n「あ、はじめまして！お散歩サークルに入ったひかりです。これからよろしくお願いします！一緒に歩きましょうね！」",
                R.drawable.osyaberi_smile
            ))
        }
    }

    LaunchedEffect(showDeleteConfirm) {
        if (showDeleteConfirm) {
            deleteProgress = 0f
            val steps = 100
            repeat(steps) {
                kotlinx.coroutines.delay(50L)
                deleteProgress = (it + 1) / steps.toFloat()
            }
        } else {
            deleteProgress = 0f
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Scaffold(topBar = {
        TopAppBarWithBack(
            title = "自由会話 (${actionPoints}pt)",
            onBack = { navController.popBackStack() },
            titleColor = Color(0xFFE87C9A),
            titleFontFamily = MplusRoundedFontFamily,
            actions = {
                IconButton(onClick = { showDeleteConfirm = !showDeleteConfirm }) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "履歴削除", tint = Color(0xFFE87C9A))
                }
            }
        )
    }) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF5F7))
            .padding(padding)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFFE4EE),
                        border = BorderStroke(1.dp, Color(0xFFEFB8CC))
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("💬 ひかりとおしゃべり", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD4618A), fontFamily = MplusRoundedFontFamily)
                            Text("・1メッセージ = 1ポイント消費（2000歩で1ポイント）", fontSize = 11.sp, color = Color(0xFF9E6070))
                            Text("・歩いた日数や歩数でひかりのセリフが変わります", fontSize = 11.sp, color = Color(0xFF9E6070))
                            Text("・好感度が上がると口調が変わっていきます", fontSize = 11.sp, color = Color(0xFF9E6070))
                        }
                    }
                }
                items(messages.size) { i ->
                    val msg = messages[i]
                    val isUser = msg.role == "user"
                    if (isUser) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFE87C9A),
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
                                Text(msg.content, modifier = Modifier.padding(10.dp), color = Color.White, fontSize = 14.sp)
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Image(
                                painter = painterResource(msg.expressionRes ?: R.drawable.osyaberi_smile),
                                contentDescription = "ひかり",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .clip(RoundedCornerShape(20.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                                if (msg.actionText != null) {
                                    // 新フォーマット: [ACTION] が地の文
                                    Text(msg.actionText, color = Color(0xFF888888), fontSize = 12.sp, lineHeight = 20.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, modifier = Modifier.padding(vertical = 2.dp))
                                    Text(msg.content.take(400), color = Color(0xFF2C2C2C), fontSize = 13.sp, lineHeight = 22.sp, modifier = Modifier.padding(vertical = 2.dp).background(Color(0xFFFCEEF4), RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 6.dp))
                                } else {
                                    // 旧フォーマット後方互換: 「」で地の文/セリフを分割
                                    parseMessageSegments(msg.content.take(400)).forEach { seg ->
                                        if (seg.isNarration) {
                                            Text(seg.text, color = Color(0xFF888888), fontSize = 12.sp, lineHeight = 20.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, modifier = Modifier.padding(vertical = 2.dp))
                                        } else {
                                            Text(seg.text, color = Color(0xFF2C2C2C), fontSize = 13.sp, lineHeight = 22.sp, modifier = Modifier.padding(vertical = 2.dp).background(Color(0xFFFCEEF4), RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 6.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(28.dp), color = Color(0xFFE87C9A), strokeWidth = 2.dp)
                        }
                    }
                }
                item {
                    val lastExpr = messages.lastOrNull { it.role == "assistant" }?.exprName
                    ChatStatusCard(loveCount = loveCount, heartCount = heartCount, lastExprName = lastExpr)
                }
            }

            errorMessage?.let {
                Text(it, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp))
            }

            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { if (it.length <= 250) inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("メッセージを入力...", fontSize = 13.sp) },
                    maxLines = 1,
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        val text = inputText.trim()
                        if (text.isEmpty() || isLoading) return@IconButton
                        if (!viewModel.spendPointForChat()) {
                            errorMessage = "ポイントが足りません（2000歩で1ポイント）"
                            return@IconButton
                        }
                        errorMessage = null
                        val userMsg = ChatMessage("user", text)
                        messages.add(userMsg)
                        inputText = ""
                        isLoading = true
                        val allHistory = messages.dropLast(1)
                        val historySnapshot = allHistory.takeLast(10)
                        scope.launch {
                            try {
                                val hasChat = viewModel.hasEverChatted
                                val customNote = viewModel.customCharacterNote
                                val summary = viewModel.buildMemoryContext()
                                val hoursAway = if (hasChat) viewModel.hoursSinceLastChat() else 0
                                val streak = viewModel.getCurrentStreak()
                                val absenceSteps = viewModel.getStepsDuringAbsence(hoursAway)
                                val systemPrompt = buildFreeChatSystemPrompt(loveCount, playerName, if (hasChat) todaySteps else 0, if (hasChat) activeDays else 0, customNote, if (hasChat) daysSinceLastActive else 0, summary, hoursAway, streak, absenceSteps)
                                viewModel.markHasEverChatted()
                                viewModel.updateLastChatTime()
                                val reply = callGeminiApi(systemPrompt, historySnapshot, text)
                                val parsed = parseReply(reply, loveCount)
                                messages.add(ChatMessage("assistant", parsed.text, parsed.exprRes, parsed.exprName, parsed.actionText))
                                when (parsed.loveChange) {
                                    1  -> viewModel.earnHeart()
                                    -1 -> viewModel.loseHeart()
                                }
                                viewModel.saveFreeChatHistory()
                                // 5メッセージごとに日次日記を更新（バックグラウンド）
                                val userMsgCount = messages.count { it.role == "user" }
                                if (userMsgCount % 5 == 0 && userMsgCount >= 5) {
                                    scope.launch {
                                        try {
                                            val newSummary = callGeminiApiForSummary(messages)
                                            viewModel.updateDailyDiary(newSummary)
                                        } catch (_: Exception) {}
                                    }
                                }
                            } catch (e: Exception) {
                                viewModel.refundPointForChat()
                                messages.removeLastOrNull()
                                errorMessage = "エラー: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    enabled = !isLoading && inputText.isNotBlank()
                ) {
                    Icon(Icons.Default.Send, contentDescription = "送信", tint = Color(0xFFE87C9A))
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = showDeleteConfirm,
                enter = androidx.compose.animation.slideInVertically { it },
                exit = androidx.compose.animation.slideOutVertically { it }
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFF8F8F8),
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Text(
                            "履歴を削除しますか？",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { deleteProgress },
                            modifier = Modifier.fillMaxWidth().height(4.dp),
                            color = if (deleteProgress >= 1f) Color(0xFFE53935) else Color(0xFFFF8A80),
                            trackColor = Color(0xFFEEEEEE)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showDeleteConfirm = false }) {
                                Text("キャンセル", color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (deleteProgress >= 1f) {
                                        viewModel.clearFreeChatHistory()
                                        showDeleteConfirm = false
                                    }
                                },
                                enabled = deleteProgress >= 1f,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE53935),
                                    disabledContainerColor = Color(0xFFBDBDBD)
                                )
                            ) {
                                Text("削除", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
