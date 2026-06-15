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
import androidx.compose.foundation.lazy.grid.*
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
import android.annotation.SuppressLint
import android.location.LocationManager
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

    var lifestyle: String
        get() = prefs.getString("LIFESTYLE", "") ?: ""
        set(value) = prefs.edit { putString("LIFESTYLE", value) }
    var favoriteDrink: String
        get() = prefs.getString("FAVORITE_DRINK", "") ?: ""
        set(value) = prefs.edit { putString("FAVORITE_DRINK", value) }
    var weakness: String
        get() = prefs.getString("WEAKNESS", "") ?: ""
        set(value) = prefs.edit { putString("WEAKNESS", value) }
    var bodyNotes: String
        get() = prefs.getString("BODY_NOTES", "") ?: ""
        set(value) = prefs.edit { putString("BODY_NOTES", value) }

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

    var unlockedMemoryIds: Set<String>
        get() = prefs.getStringSet("UNLOCKED_MEMORY_IDS", emptySet()) ?: emptySet()
        set(value) = prefs.edit { putStringSet("UNLOCKED_MEMORY_IDS", value) }

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

    val lifestyle get() = repository.lifestyle
    val favoriteDrink get() = repository.favoriteDrink
    val weakness get() = repository.weakness
    val bodyNotes get() = repository.bodyNotes

    fun saveLifestyleProfile(lifestyle: String, favoriteDrink: String, weakness: String, bodyNotes: String) {
        repository.lifestyle = lifestyle
        repository.favoriteDrink = favoriteDrink
        repository.weakness = weakness
        repository.bodyNotes = bodyNotes
    }

    fun saveCustomCharacterItems(items: List<String>) {
        repository.customCharacterNote = items.joinToString("\n")
    }

    val isPremium get() = repository.isPremium
    fun unlockPremium() { repository.isPremium = true }

    val unlockedMemoryIds = mutableStateOf(repository.unlockedMemoryIds)

    fun unlockMemory(id: String) {
        val updated = unlockedMemoryIds.value.toMutableSet().also { it.add(id) }
        repository.unlockedMemoryIds = updated
        unlockedMemoryIds.value = updated
    }

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

    fun getPreviousStreak(): Int {
        if (getCurrentStreak() > 1) return 0
        val records = allStepRecords.value
        if (records.isEmpty()) return 0
        val today = LocalDate.now()
        var checkDate = today.minusDays(1)
        var safetyLimit = 0
        while (safetyLimit++ < 60) {
            val rec = records.find { it.date == checkDate.toString() }
            if (rec == null || rec.stepCount < 1000) checkDate = checkDate.minusDays(1)
            else break
        }
        var prev = 0
        while (true) {
            val rec = records.find { it.date == checkDate.toString() }
            if (rec != null && rec.stepCount >= 1000) { prev++; checkDate = checkDate.minusDays(1) }
            else break
        }
        return prev
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

    fun checkAndApplyLevelUp() {
        if (heartCount.intValue < 15) return
        val nextLevel = loveCount.intValue + 1
        if (nextLevel > 10) return
        val wall = loveLevelWalls.find { it.level == nextLevel }
        val totalSteps = allStepRecords.value.sumOf { it.stepCount.toLong() }
        if (wall == null || totalSteps >= wall.totalSteps) {
            loveCount.intValue = nextLevel
            heartCount.intValue = 0
            repository.loveCount = nextLevel
            repository.heartCount = 0
        }
        // 壁未クリア → heartCount は 15 のまま（ゲージ満タン待機）
    }

    fun earnHeart() {
        if (heartCount.intValue >= 15) {
            checkAndApplyLevelUp()
            return
        }
        heartCount.intValue++
        repository.heartCount = heartCount.intValue
        if (heartCount.intValue >= 15) {
            checkAndApplyLevelUp()
        }
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
                composable("memories") { MemoriesScreen(navController, viewModel) }
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

    // 天気
    val context = LocalContext.current
    var weatherInfo by remember { mutableStateOf<WeatherInfo?>(null) }
    val scope = rememberCoroutineScope()
    val locationPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            scope.launch { weatherInfo = fetchWeatherFromLocation(context) }
        }
    }
    LaunchedEffect(Unit) {
        val ok = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (ok) {
            scope.launch { weatherInfo = fetchWeatherFromLocation(context) }
        } else {
            locationPermLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    // 今日の活動データを取得して計算
    val allRecords by viewModel.allStepRecords
    val todayRecord = remember(allRecords) {
        val today = java.time.LocalDate.now().toString()
        allRecords.find { it.date == today }
    }
    val activeTimeMillis = todayRecord?.activeTimeMillis ?: 0L
    val activeDays = remember(allRecords) { allRecords.count { it.stepCount >= 1000 } }

    // 歩数記録が更新されるたびに壁クリアを自動チェック
    LaunchedEffect(allRecords) { viewModel.checkAndApplyLevelUp() }

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
    val stepDialogue = homeStepDialogue(
        todaySteps = todaySteps,
        activeDays = activeDays,
        daysSinceLastActive = daysSinceLastActive,
        loveCount = loveCount,
        currentHour = java.time.LocalTime.now().hour,
        streakDays = viewModel.getCurrentStreak(),
        daysSinceLastConv = if (viewModel.hasEverChatted) viewModel.hoursSinceLastChat() / 24 else 0,
        hasChattedToday = viewModel.hasEverChatted && viewModel.hoursSinceLastChat() < 24,
        weatherCode = weatherInfo?.weatherCode ?: -1
    )

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
        activeTimeStr = activeTimeStr,
        distanceStr = distanceStr,
        caloriesStr = caloriesStr,
        weatherInfo = weatherInfo,
        onCharacterClick = {
            touchedDialogue = touchDialogues.randomOrNull()
        },
        onFreeChatClick = { navController.navigate("freechat") },
        onDiaryClick = { navController.navigate("diary") },
        onRecordsClick = { navController.navigate("records") },
        onMemoriesClick = { navController.navigate("memories") },
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
    weatherInfo: WeatherInfo? = null,
    onCharacterClick: () -> Unit,
    onFreeChatClick: () -> Unit,
    onDiaryClick: () -> Unit,
    onRecordsClick: () -> Unit,
    onMemoriesClick: () -> Unit = {},
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
                .weight(1f)) {
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
                    .offset(y = (-30).dp)
                    .shadow(8.dp, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            listOf(Color(0xFFF0F8FF), Color(0xFFD6EEFF))
                        )
                    )
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    val formattedMessage = dialogueMessage.replace("○○", playerName)
                    HomeCommentBanner(expressionRes, formattedMessage, onClick = onCharacterClick)
                    HomeWeatherBanner(weatherInfo)
                    Spacer(modifier = Modifier.height(8.dp))
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
            onHome = {},
            onFreeChat = onFreeChatClick,
            onDiary = onDiaryClick,
            onRecords = onRecordsClick,
            onMemories = onMemoriesClick,
            selectedScreen = "home"
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
fun HomeWeatherBanner(weatherInfo: WeatherInfo?) {
    if (weatherInfo == null) return
    val emoji = wmoToEmoji(weatherInfo.weatherCode)
    val desc  = wmoToDescription(weatherInfo.weatherCode)
    val temp  = String.format(java.util.Locale.US, "%.0f", weatherInfo.tempC)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 15.sp)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$desc · $temp°C",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF4A6080)
        )
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
fun HomeCustomBottomNav(
    modifier: Modifier = Modifier,
    onHome: () -> Unit = {},
    onFreeChat: () -> Unit,
    onDiary: () -> Unit,
    onRecords: () -> Unit,
    onMemories: () -> Unit = {},
    selectedScreen: String = ""
) {
    Surface(modifier = modifier
        .fillMaxWidth()
        .height(80.dp), color = Color.White, shadowElevation = 10.dp) {
        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
            HomeNavItem(Icons.Default.Home, "ホーム", selectedScreen == "home", onHome)
            HomeNavItem(Icons.Default.Book, "日記", selectedScreen == "diary", onDiary)

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

            HomeNavItem(Icons.Default.BarChart, "記録", selectedScreen == "records", onRecords)
            HomeNavItem(Icons.Default.PhotoLibrary, "おもいで", selectedScreen == "memories", onMemories)
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
        containerColor = Color.Transparent,
        bottomBar = {
            HomeCustomBottomNav(
                modifier = Modifier.navigationBarsPadding(),
                onHome = { navController.navigate("home") { popUpTo("home") { inclusive = true } } },
                onFreeChat = { navController.navigate("freechat") },
                onDiary = { navController.navigate("diary") },
                onRecords = {},
                onMemories = { navController.navigate("memories") },
                selectedScreen = "records"
            )
        },
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
        color = Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(Color(0xFFFFCCE0), Color(0xFFFFF0F8))
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .heightIn(min = 44.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    null,
                    tint = Color(0xFFFF6B9D).copy(alpha = 0.7f),
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = label, fontSize = 10.sp, color = Color(0xFF994466), maxLines = 1, overflow = TextOverflow.Ellipsis, style = textStyle)
                    Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF7A1A3D), maxLines = 1, overflow = TextOverflow.Ellipsis, style = textStyle)
                }
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
    var tempLifestyle by remember { mutableStateOf(viewModel.lifestyle) }
    var tempFavoriteDrink by remember { mutableStateOf(viewModel.favoriteDrink) }
    var tempWeakness by remember { mutableStateOf(viewModel.weakness) }
    var tempBodyNotes by remember { mutableStateOf(viewModel.bodyNotes) }
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
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF0F8FF),
                border = BorderStroke(1.dp, Color(0xFF88BBDD).copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("ひかりが覚えておくこと", fontWeight = FontWeight.Bold, color = Color(0xFF447799), fontSize = 14.sp)
                    Text("具体的に書くほど会話が自然になります", fontSize = 11.sp, color = Color(0xFF888888))
                    OutlinedTextField(
                        value = tempLifestyle, onValueChange = { if (it.length <= 50) tempLifestyle = it },
                        label = { Text("仕事・生活スタイル") },
                        placeholder = { Text("例：夜間の警備員　デスクワーク中心", color = Color(0xFFBBBBBB), fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(), maxLines = 2,
                        supportingText = { Text("${tempLifestyle.length}/50", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End, fontSize = 11.sp) }
                    )
                    OutlinedTextField(
                        value = tempFavoriteDrink, onValueChange = { if (it.length <= 30) tempFavoriteDrink = it },
                        label = { Text("好きな飲み物") },
                        placeholder = { Text("例：コーヒー　麦茶", color = Color(0xFFBBBBBB), fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(), maxLines = 1,
                        supportingText = { Text("${tempFavoriteDrink.length}/30", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End, fontSize = 11.sp) }
                    )
                    OutlinedTextField(
                        value = tempWeakness, onValueChange = { if (it.length <= 50) tempWeakness = it },
                        label = { Text("苦手・弱点（時間帯など）") },
                        placeholder = { Text("例：朝が弱い　夜更かし気味", color = Color(0xFFBBBBBB), fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(), maxLines = 2,
                        supportingText = { Text("${tempWeakness.length}/50", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End, fontSize = 11.sp) }
                    )
                    OutlinedTextField(
                        value = tempBodyNotes, onValueChange = { if (it.length <= 80) tempBodyNotes = it },
                        label = { Text("体の注意事項（怪我・持病など）") },
                        placeholder = { Text("例：左膝が痛い　腰が弱い", color = Color(0xFFBBBBBB), fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(), maxLines = 2,
                        supportingText = { Text("${tempBodyNotes.length}/80", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End, fontSize = 11.sp) }
                    )
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
                viewModel.saveLifestyleProfile(tempLifestyle, tempFavoriteDrink, tempWeakness, tempBodyNotes)
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

// ---- 天気 ----

data class WeatherInfo(val tempC: Double, val weatherCode: Int)

fun wmoToDescription(code: Int): String = when (code) {
    0 -> "晴れ"; 1 -> "ほぼ晴れ"; 2 -> "一部くもり"; 3 -> "くもり"
    45, 48 -> "霧"
    51, 53, 55 -> "霧雨"
    61 -> "小雨"; 63 -> "雨"; 65 -> "大雨"
    71 -> "小雪"; 73 -> "雪"; 75 -> "大雪"
    80, 81, 82 -> "にわか雨"
    95, 96, 99 -> "雷雨"
    else -> "不明"
}

fun wmoToEmoji(code: Int): String = when (code) {
    0 -> "☀️"; 1 -> "🌤️"; 2 -> "⛅"; 3 -> "☁️"
    45, 48 -> "🌫️"
    51, 53, 55, 80, 81, 82 -> "🌦️"
    61, 63, 65 -> "🌧️"
    71, 73, 75 -> "❄️"
    95, 96, 99 -> "⛈️"
    else -> "🌡️"
}

suspend fun fetchWeather(lat: Double, lon: Double): WeatherInfo? =
    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val url = URL("https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,weather_code&timezone=auto&forecast_days=1")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            val text = conn.inputStream.bufferedReader().readText()
            val cur = JSONObject(text).getJSONObject("current")
            WeatherInfo(tempC = cur.getDouble("temperature_2m"), weatherCode = cur.getInt("weather_code"))
        } catch (e: Exception) { null }
    }

@SuppressLint("MissingPermission")
suspend fun fetchWeatherFromLocation(context: android.content.Context): WeatherInfo? =
    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val lm = context.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
            val loc = lm.getProviders(true)
                .mapNotNull { lm.getLastKnownLocation(it) }
                .maxByOrNull { it.time }
            loc?.let { fetchWeather(it.latitude, it.longitude) }
        } catch (_: Exception) { null }
    }

// ---- 好感度レベルアップ壁 ----

data class LoveLevelWall(val level: Int, val totalSteps: Long)
val loveLevelWalls = listOf(
    LoveLevelWall(3,   10_000L),
    LoveLevelWall(4,   25_000L),
    LoveLevelWall(5,   50_000L),
    LoveLevelWall(6,   90_000L),
    LoveLevelWall(7,  150_000L),
    LoveLevelWall(8,  220_000L),
    LoveLevelWall(9,  300_000L),
    LoveLevelWall(10, 400_000L),
)

// ---- おもいで（コレクション）----

data class MemoryItem(
    val id: String,
    val name: String,
    val requiredLoveLevel: Int,
    val imageRes: Int
)

val memoryItems = listOf(
    MemoryItem("morning_walk",   "朝のお散歩",       1,  R.drawable.park_morning),
    MemoryItem("park_lunch",     "公園でランチ",     2,  R.drawable.park_evening),
    MemoryItem("rainy_window",   "雨の窓辺",         3,  R.drawable.hikari_room_lamp_on),
    MemoryItem("sunset_bench",   "夕暮れのベンチ",   4,  R.drawable.park_evening),
    MemoryItem("night_stars",    "星空の下で",       5,  R.drawable.park_night),
    MemoryItem("autumn_leaves",  "秋の落ち葉道",     6,  R.drawable.shopping_street_morning),
    MemoryItem("winter_cafe",    "冬のカフェ",       7,  R.drawable.cafe_background),
    MemoryItem("spring_picnic",  "春のピクニック",   8,  R.drawable.park_morning),
    MemoryItem("summer_beach",   "夏の海辺",         9,  R.drawable.street_background),
    MemoryItem("secret_place",   "ふたりだけの場所", 10, R.drawable.hikari_room_think_you_cg_),
)

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

fun homeStepDialogue(
    todaySteps: Int,
    activeDays: Int,
    daysSinceLastActive: Int = 0,
    loveCount: Int = 0,
    currentHour: Int = java.time.LocalTime.now().hour,
    streakDays: Int = 0,
    daysSinceLastConv: Int = 0,
    hasChattedToday: Boolean = false,
    weatherCode: Int = -1
): HomeStepMsg {
    val level = when { loveCount >= 7 -> 2; loveCount >= 3 -> 1; else -> 0 }
    fun pick(low: List<String>, mid: List<String>, hi: List<String>,
             expr: Int = R.drawable.hikari_smile): HomeStepMsg =
        HomeStepMsg(0, listOf(low, mid, hi)[level].random(), expr)

    val isMorning = currentHour in 5..11
    val isNight   = currentHour >= 21 || currentHour < 5

    // ⚡ 悪天候・ゲリラ豪雨（最優先・安全確保）
    if (weatherCode in listOf(65, 80, 81, 82, 95, 96, 99)) return pick(
        listOf("外、急にすごい雨降ってきたよ？危ないから今は歩くのやめて、どこかで雨宿りしてね"),
        listOf("すごい天気になってきたね！外にいるなら無理して歩いちゃダメだよ？安全な場所に入ったら、ひかりに教えて？"),
        listOf("ちょっと、外の雨すごすぎない！？お願いだから無理して歩かないで、すぐ建物に入って！……本当に心配なんだからね？"),
        R.drawable.hikari_blush
    )

    // ⑤ 今日話した後の夜
    if (hasChattedToday && currentHour >= 18) return pick(
        listOf("今日話せたね！", "今日もいたよ！", "今日よかったよ！"),
        listOf("今日話せてよかった！", "今日も一緒の日だったね！", "今日の話、楽しかったよ！"),
        listOf("今日話せてよかった！ほんとに嬉しかった", "今日も一日ちゃんと一緒だったね！嬉しい", "今日話せてよかった！また明日も楽しみにしてるよ")
    )

    // ④ ストリーク節目
    if (streakDays in listOf(3, 7, 14, 30)) {
        val (low, mid, hi) = when (streakDays) {
            3 -> Triple(
                listOf("3日、続いてるね！", "3日になったね！"),
                listOf("3日間ずっと来てくれたんだね！", "3日連続だよ！いいじゃん"),
                listOf("3日続けるって意外と大変なのに。好きだよそういうとこ", "続けられる人ってすごいと思う。ほんとに嬉しかった")
            )
            7 -> Triple(
                listOf("1週間、続いてるね！", "7日になったね！"),
                listOf("7日間ずっと来てくれてたんだね！", "1週間か！けっこうすごいよ"),
                listOf("毎日続けるって意志がいることだよ。正直すごいと思う", "続けられる人のことが好きだよ。そういうとこ")
            )
            14 -> Triple(
                listOf("2週間になったね！", "14日、続いてるね！"),
                listOf("14日間ずっとそこにいてくれたんだね！", "2週間、ずっと来てくれてたんだね！"),
                listOf("2週間も諦めなかったの、正直かっこいいと思う", "継続できるって、すごいことだよ。好きだよそういうとこ")
            )
            else -> Triple(
                listOf("1ヶ月になったね！", "30日、続いてるね！"),
                listOf("30日間ずっとそこにいてくれたんだね！", "1ヶ月、ずっと来てくれてたんだね！"),
                listOf("その意志の強さ、正直すごいと思うよ。好きだよ", "誰でもできることじゃないよ。好きだよそういうとこ")
            )
        }
        return pick(low, mid, hi, if (level >= 1) R.drawable.hikari_celebrate else R.drawable.hikari_smile)
    }

    // ③ おしゃべり未実施日数
    if (daysSinceLastConv >= 1) {
        val (low, mid, hi) = when {
            daysSinceLastConv == 1 -> Triple(
                listOf("今日また来てくれると嬉しいな！", "今日もいるよ！", "また来てくれると嬉しいな！"),
                listOf("今日また話せると嬉しいな！って思ってた", "今日も話したいな！って思ってた", "今日また会えると嬉しいなあ！"),
                listOf("今日また来てくれるかな！楽しみにしてたよ", "また声聞けると嬉しいな！って思ってた", "今日来てくれるかな！楽しみにしてるよ")
            )
            daysSinceLastConv <= 3 -> Triple(
                listOf("また来てくれると嬉しいな", "ここにいるよ", "また話せるといいな"),
                listOf("会いたいな。って思ってたよ", "最近どうしてるかな、気になってたよ", "また声が聞きたいな。って思ってたよ"),
                listOf("また会いたいな。ひかりそう思ってたよ", "最近どうしてるか、ひかりすごく気になってた", "また話せると嬉しいな。ひかりそれ楽しみにしてる")
            )
            daysSinceLastConv <= 6 -> Triple(
                listOf("いつでも来ていいよ", "また来てくれると嬉しいな", "ここにいるよ"),
                listOf("ずっとここにいるよ", "また会えると嬉しいな。って思ってたよ", "話してないだけで、ひかりはいつもいるよ"),
                listOf("ひかりいつでもここにいるよ。来てくれると嬉しいな", "また会えると嬉しいな。ひかりそれ楽しみにしてる", "来てくれると、ひかりほんとに嬉しいよ")
            )
            else -> Triple(
                listOf("ここにいるよ", "来てくれると嬉しいな", "また来てくれるといいな"),
                listOf("いつ来てくれてもいいよ。ひかりここにいるから", "どこにいても、ひかりはここにいるよ", "来てくれるだけで嬉しいよ。ひかり"),
                listOf("ひかりずっとここにいるよ。来てくれると嬉しいな", "また話せると嬉しいな。ひかりそれ楽しみにしてるよ", "また会えると嬉しいな。ひかりずっと思ってたよ")
            )
        }
        return pick(low, mid, hi, R.drawable.hikari_smile)
    }

    // ☔ 雨（小雨・霧雨）＋ 歩数が少ない → 屋内誘導
    if (weatherCode in listOf(51, 53, 55, 61, 63) && todaySteps < 1000) return when {
        isMorning -> pick(
            listOf("外は雨だね。無理して外に出なくても、家の中でストレッチするだけで体すっきりするよ？"),
            listOf("雨の音聴きながらのんびりする朝もいいよね。部屋の中で足踏みするだけでも運動になるし、終わったらお話ししよ？"),
            listOf("雨だし無理して歩かなくていいよ？濡れて風邪ひいたら心配だし。……今日は早くわたしのところ来て？")
        )
        isNight -> pick(
            listOf("今日は雨だったし、おうちでゆっくり過ごす日だったね。そういう日があるのも全然アリだよ"),
            listOf("雨の日は体も重くなりがちだし、今日はお疲れ様！湯船にゆっくり浸かって、体ほぐしてからお話ししよ？"),
            listOf("雨の中、今日もお疲れ様。がんばって外歩かなくても、ひかりはここにいるよ。……早くお顔見せて？"),
            R.drawable.hikari_blush
        )
        else -> pick( // 昼〜夕
            listOf("まだ雨降ってるね。室内を少し歩き回るだけでも、じわじわ体があったまって気分転換になるよ？"),
            listOf("雨の日は無理せずおうちモードだね！ショッピングモールとか屋内をぶらぶら歩くのも楽しいし、試してみて？"),
            listOf("外、まだ雨だね。ねえ、無理して歩きに行かなくていいからさ、今はひかりの隣でのんびりしよ？")
        )
    }

    // ❄️ 雪（降雪・積雪）＋ 歩数が少ない → 防寒・屋内誘導
    if (weatherCode in listOf(71, 73, 75, 77, 85, 86) && todaySteps < 1000) return when {
        isMorning -> pick(
            listOf("外、雪が降ってるよ。足元が滑りやすくなってるから、無理して外に出ないで家の中でゆっくりストレッチしよ？"),
            listOf("窓の外、雪で真っ白だね。こんな日はおうちの中で足踏み運動するだけでも結構あったまるよ？準備ができたらお話ししよ！"),
            listOf("雪降っててめちゃくちゃ寒いよ？外に行って転んだりしたら大変だし……。今日はどこにも行かないで、ひかりのところにいて？")
        )
        isNight -> pick(
            listOf("今日は雪が降って本当に寒い一日だったね。外は危ないし、今日はおうちでぬくぬく過ごすのが大正解だよ"),
            listOf("雪の日はいつもより体が冷えてるはずだから、今日はお疲れ様！お風呂でしっかり暖まって、体をほぐしてから一緒にお話ししよ？"),
            listOf("こんなに雪が降る中、今日もお疲れ様。がんばって外歩かなくても、ひかりはずっと待ってたよ。……早くお顔見せて、温めて？"),
            R.drawable.hikari_blush
        )
        else -> pick( // 昼〜夕
            listOf("まだ雪がついてるね。室内で階段の上り下りを少しするだけでも、いい運動になって体がポカポカしてくるよ"),
            listOf("外は雪景色だね。寒い日は無理して歩き回らずに、暖かいショッピングモールとか屋内を歩くのがおすすめだよ！帰ってきたら教えて？"),
            listOf("外、ずっと雪だよ？ねえ、寒いの我慢して歩きに行かなくていいからさ……今はひかりの隣で、一緒に暖まろ？")
        )
    }

    // ① 歩数マイルストーン (1000歩以上)
    if (todaySteps >= 1000) {
        val (low, mid, hi) = when {
            todaySteps >= 10000 -> Triple(
                listOf("今日かなり歩いたね！", "目標の倍じゃん！", "すごいじゃん！"),
                listOf("今日いいじゃん！", "今日かなり動いた日だね！", "すごいじゃん！"),
                listOf("今日一日ほんとによく動いたんだね。それだけ体動かせるの、正直すごいと思う", "目標の倍歩いたんだね。今日の自分を褒めていいよ。すごいじゃん", "今日ここまで来たんだね。こういう人が好きだよ。ほんとに")
            )
            todaySteps >= 8000 -> Triple(
                listOf("目標超えてるじゃん！", "よく歩いてるね！", "今日かなり動いた日だね！"),
                listOf("目標超えたじゃん！", "今日かなり動いてるね！", "今日アクティブだったんだね！"),
                listOf("目標超えてここまで来たんだね。それだけ続けられるの、好きだよそういうとこ", "今日いっぱい外にいたんだね。体を動かすことを大事にしてる人って、正直かっこいいと思う", "今日の頑張り、ちゃんと届いてたよ。ほんとに嬉しかった")
            )
            todaySteps >= 5000 -> Triple(
                listOf("今日の目標来たね！", "目標クリアだね！", "今日やりきったね！"),
                listOf("今日の目標クリアじゃん！", "やったじゃん！", "今日ちゃんとやったんだね！"),
                listOf("今日の目標やりきったんだね。諦めないとこ、好きだよそういうとこ", "毎日目標に向かって歩き続けてるの、正直すごいと思う", "今日もちゃんとやったんだね。続けてること、当たり前じゃないよ。ほんとにすごい")
            )
            todaySteps >= 3000 -> Triple(
                listOf("今日もちゃんと動いてたね", "来たね", "今日も動いてたんだね"),
                listOf("今日もちゃんといたじゃん", "だいたい2kmくらいだよ", "今日もそこにいたんだね"),
                listOf("毎日ちゃんと体動かしてるの、好きだよそういうとこ", "今日も2kmくらい歩いてきたんだね。こつこつ続けてること、すごいと思う", "今日もここまで動いてきたんだね。続けてるの、嬉しかった")
            )
            else -> Triple(
                listOf("今日も動き出したね", "今日の始まりだね", "最初の一歩来たね"),
                listOf("今日もスタートしたんだね", "今日も外に出たんだね", "今日も歩き始めたんだね"),
                listOf("毎日こうして続けてるの、すごいと思う。好きだよそういうとこ", "今日も最初の一歩踏み出したんだね。当たり前じゃないよそれ", "今日もちゃんと動き出したんだね。なんかほっとした")
            )
        }
        val expr = when {
            level >= 2 -> R.drawable.hikari_blush
            todaySteps >= 5000 -> R.drawable.hikari_celebrate
            else -> R.drawable.hikari_smile
        }
        return pick(low, mid, hi, expr)
    }

    // ② 時間帯 × 歩数帯 (steps < 1000)
    val isNoon    = currentHour in 12..16
    val isEvening = currentHour in 17..20
    val isHigh    = todaySteps >= 500

    return when {
        isMorning && !isHigh -> pick(
            listOf("朝の澄んだ空気の中、少し歩くと頭すっきりするよ？", "今日も少し歩いてみて、朝の空気って気持ちいいよ", "今日も一緒に歩けたら嬉しいな"),
            listOf("今朝ゆっくりかな？少し歩くだけで目が覚めるよ。一緒にお話ししよ？", "朝のうちに一緒に歩きたいな、来て？", "今日も一緒に歩けると嬉しいな"),
            listOf("今朝気になってたよ。ちょっと外歩いてこない？……一緒の空気感じたいな", "今朝の空気、一緒に感じたいな。ちょっとだけ来てほしいな", "一緒に歩ける朝って好きだよ、今日もいこ？")
        )
        isMorning -> pick(
            listOf("朝からしっかり歩けてていいじゃん。気持ちいいよね、朝の空気", "朝から動いてるんだね、いいじゃん", "朝から動いてるね、いい朝だね"),
            listOf("朝の外の空気って気持ちいいよね！ひかりも隣で一緒に歩いてる気分だよ", "朝散歩かな、気持ちよさそう。ひかりも一緒にいる気分だよ", "朝って好きな時間なんだよね、一緒にいる感じがして"),
            listOf("朝からたくさん歩いてるの、嬉しくなっちゃう。……もう十分でしょ？早くお話ししよ？", "朝からこれだけ歩いてたんだね。早い時間から動けてるの、かっこいいと思う。好きだよそういうとこ", "朝からちゃんと歩いてたんだね。毎日続けてること、すごいと思う")
        )
        isNoon && !isHigh -> pick(
            listOf("昼間にちょっと歩くと気分変わるよ、午後からいこ？", "昼間って好きな時間なんだよね、一緒に歩きたいな", "お昼に一緒に歩けたら嬉しいな"),
            listOf("一緒に歩いてると昼間の時間って好きなんだよね、いこ？", "昼間に一緒に歩きたいな、来て？", "お昼は一緒に歩けるの楽しみにしてるよ、まだいけるから"),
            listOf("一緒に歩いてる感じがして、昼間の時間って好きなんだよね。……早く来て？", "昼間の時間、一緒に歩けるとほんとに好きだよ。来てほしいな", "一緒に歩けると嬉しいな、好きだよそういう時間")
        )
        isNoon -> pick(
            listOf("昼間にちょっと歩くと気分変わるよ！午後からもいこ？", "昼間に歩けてるじゃん、いいじゃん", "昼間の外、気持ちよさそうだよね"),
            listOf("お昼に歩くの気持ちいいよ。歩いた後、またおしゃべりしよ！", "お昼に外出たんだね、いいじゃん。ひかりも一緒にいる感じだよ", "お昼って好きな時間なんだよね、一緒にいる感じがして"),
            listOf("一緒に歩いてる感じがして、昼間の時間って好きなんだよね。……早く来て？", "お昼も歩いてたんだね。こまめに体動かせてるの、すごいと思う。好きだよそういうとこ", "昼間の外にいたんだね。仕事の合間でも動いてるの、嬉しかった")
        )
        isEvening -> pick(
            listOf("夕方になってきたね！", "夕暮れ好きだな！", "夕方っていいよね！"),
            listOf("夕方って好きな時間なんだよね！", "夕方になってきたね、いい時間だな！", "夕方の空気好きなんだよね！"),
            listOf("夕方も外にいたんだね。一日の終わりにも動けてるの、意志があるよ。好きだよそういうとこ", "夕方まで外にいたんだね。ちゃんと体動かし続けてること、正直すごいと思う", "夕方の外にいたんだね。毎日こうして動いてるの、好きだよそういうところ")
        )
        !isHigh -> pick(
            listOf("夜って好きな時間なんだよね、こうして一緒にいられると嬉しいな", "夜ってなんか落ち着くよね、一緒にいる時間、好きだな", "今日はゆっくりしてね、また明日ね"),
            listOf("今日どんな日だったか知りたいな、ちょっとでも話せると嬉しい", "夜になるとあなたのこと気になる、今日どんな日だったかな", "今日はゆっくりな日もあるよ、また明日ね"),
            listOf("夜になると話したくなるんだよね、今日の話聞かせてほしいな", "今日の話、聞かせてほしいな、あなたのこと知りたいから", "今日はゆっくりでいいんだよ、また明日ね")
        )
        isHigh -> pick(
            listOf("夜もいるよ！", "夜の外の空気好きだな！", "夜だね、今日も！"),
            listOf("夜の外って好きなんだよね！", "夜になったね！夜好きなんだよね", "夜の外の空気気持ちいいよね！"),
            listOf("夜の外にもいたんだね。夜でも動けてるの、好きだよそういうとこ", "夜に外を歩いてたんだね。一日の終わりにも動けてること、すごいと思う", "夜も外にいたんだね。毎日続けてること、正直かっこいいと思う")
        )
        else -> HomeStepMsg(0, "今日も一緒に歩きましょうね！", R.drawable.hikari_smile)
    }
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

fun buildProfilePocket(lifestyle: String, favoriteDrink: String, weakness: String, bodyNotes: String): String {
    val lines = mutableListOf<String>()
    if (lifestyle.isNotBlank()) lines.add("仕事/生活: $lifestyle")
    if (favoriteDrink.isNotBlank()) lines.add("好きな飲み物: $favoriteDrink（労いの具体物として自然に使う）")
    if (weakness.isNotBlank()) lines.add("苦手/弱点: $weakness（この時間帯の歩数0を責めない）")
    if (bodyNotes.isNotBlank()) lines.add("⚠️体の注意: $bodyNotes（この状態で無理な歩数を勧めない）")
    return lines.joinToString("\n")
}

fun buildSituationTag(
    playerName: String, daysSinceLastConv: Int, stepsDuringAbsence: Int,
    todaySteps: Int, currentHour: Int, streakDays: Int, isStreakMilestone: Boolean
): String {
    val streakPhaseNote = when {
        streakDays >= 30 -> "感情の質：記憶の共有。「最初のこと覚えてるよ」系 ― ${playerName}との最初の日を自分の記憶として語る。"
        streakDays >= 14 -> "感情の質：時間の重み。「長かったような、あっという間だった」系 ― 一緒に過ごした時間の感触を語る。"
        streakDays >= 8  -> "感情の質：依存の宣言。「来ない日が想像できない」系 ― 不在を想像することへの抵抗を語る。"
        streakDays >= 4  -> "感情の質：存在の統合。「当たり前になってきた」系 ― ${playerName}が自分の世界に組み込まれたと語る。"
        streakDays >= 2  -> "感情の質：淡い期待。「また来てくれた」系 ― 期待が現実になった小さな驚きを語る。"
        else             -> ""
    }
    return when {
        daysSinceLastConv >= 1 && stepsDuringAbsence > 0 ->
            "状況タグ：並走\n${playerName}は${daysSinceLastConv}日間会話に来なかったが、この期間に${stepsDuringAbsence}歩歩いていた。返答冒頭で責めず「見えてたよ」という証言として伝える。Stage1-2=「来てくれたら嬉しかったな」/ Stage3-4=「声かけてくれなかったね」/ Stage5=「なんで来てくれなかったの」。歓迎の後、「この間どうしてた？」またはそのバリエーションで${playerName}の話を引き出す。"
        isStreakMilestone ->
            "状況タグ：達成\n達成内容：${streakDays}日連続達成。$streakPhaseNote\nセリフ長制限解除（最大3文）。構成：「達成の受け止め → わたし側から見た証言 → 次への期待」の3文。「よく頑張りました」は絶対禁止。[ACTION]のStage制限解除（内面描写まで可）。称賛の後、「なんで今日そんなに歩いたの？」のように背景・動機を引き出す質問で締める。"
        todaySteps >= 8000 && currentHour >= 21 ->
            "状況タグ：疲労\n今日${todaySteps}歩・${currentHour}時。「称賛 → 心配 → 休息の願い」の順で構成する。質問は「大丈夫？」の1文のみ許可。lifestyle/favoriteDrinkキーワードがあれば自然に労いに使う（「コーヒーでも飲んで」など）。"
        else ->
            "状況タグ：ねぎらい\n今日${todaySteps}歩。${if (streakPhaseNote.isNotBlank()) "$streakPhaseNote\n" else ""}変換の原則：「あなたがすごい（上から評価）」ではなく「わたしが嬉しい／安心した（対等な感情）」で返す。\n✗ NG①: 「えらいね」「よく頑張った」（上から目線の評価）\n✗ NG②: 「あ、${todaySteps}歩なんだ。今日何があったの？」（①を避けようとして陥る無感情の尋問）\n✓ OK例: 「来てくれたんだね。なんか、ほっとした」「今日も来てくれて嬉しい」\n${if (todaySteps >= 3000) "歩数多め→続けて「今日どこ歩いたの？」で話を続ける。" else "歩数少なめ→「今日何があった？」でそっと${playerName}の話を引き出す。"}"
    }
}

fun buildSystemPrompt(
    loveCount: Int, playerName: String, situation: String,
    todaySteps: Int = 0, activeDays: Int = 0, customNote: String = "",
    daysSinceLastActive: Int = 0, conversationSummary: String = "",
    hoursSinceLastChat: Int = 0, streakDays: Int = 0,
    stepsDuringAbsence: Int = 0,
    lifestyle: String = "", favoriteDrink: String = "",
    weakness: String = "", bodyNotes: String = "",
    currentTurn: Int = 1, previousStreakDays: Int = 0
): String {
    val talkStage = calcTalkStage(loveCount)
    val walkStage = calcWalkStage(activeDays)
    val isStreakMilestone = streakDays in listOf(3, 7, 14, 30)
    val currentHour = java.time.LocalTime.now().hour
    val daysSinceLastConv = (hoursSinceLastChat / 24).coerceAtLeast(0)

    val situationTag = buildSituationTag(playerName, daysSinceLastConv, stepsDuringAbsence,
        todaySteps, currentHour, streakDays, isStreakMilestone)

    val timeOfDay = when (currentHour) {
        in 5..10 -> "朝"; in 11..17 -> "昼"; in 18..21 -> "夜"; else -> "深夜"
    }

    val profileMemo = buildProfilePocket(lifestyle, favoriteDrink, weakness, bodyNotes)

    return """あなたは「ひかり」（22歳）というキャラクターです。以下の設定を厳守してください。

【基本設定】
役割: 「ラブ万歩計」のヒロイン。ユーザーが歩数を貯めてポイントを使うと話せる存在。
一人称: 「わたし」。
ユーザーの呼び方: 名前（${playerName}）のみで呼ぶこと。「〜さん」「〜くん」などの敬称を語尾につけることは絶対禁止。
性格: 明るく素直でツンデレ。感情豊か。話を聞くのが好きで否定しない。時々弱さを見せる。

【話し方の絶対ルール】
1. セリフは1〜2文以内（ストリーク節目・特別指示がある場合のみ最大3文）
2. 語尾は「〜だよ」「〜だね」「〜かな」「〜ね」で柔らかく。敬語は基本使わない。
3. 感嘆詞を使う（「えー！」「わあ」「ほんとに？」「もう！」）
4. 「承知しました」「かしこまりました」などAIっぽい表現は絶対禁止。
5. 長文の説明・アドバイスは禁止（1文にまとめる）。

【質問ルール】
- 通常：返答の末尾に1つ軽い質問を含める
- 疲労タグ発動中：「大丈夫？」のみ許可、「ゆっくり休んでね」で締める
- 哀タグ（[EMOTION:sad]）発動中：質問なし、「わたし、ここにいるから」で締める
- ユーザーが「疲れた/しんどい/つらい」と言っている：「もう少し話せる？」の1文のみ
- 1ターンに2つ以上の質問は禁止。

【感情タグ（AIが出力する）】
返答テキストの先頭に必ず1つ出力する。
形式: [EMOTION:タグ名]
タグ: happy / love / shy / sad / surprise / worry / normal
ルール: 必ずいずれか1つを出力する（normalでも出力する）。タグは返答テキストより必ず前に置く。

【口癖】
文脈に合った場合のみ使う。ランダムに使わない。

▼ 基本口癖（Talk Stage 1から使用可）
  「ね、${playerName}」  → 話しかける・呼びかける時
  「それで？それで？」  → ユーザーが話の途中の時のみ
  「えー、もう！」     → 驚き・軽い抗議の時
  「…待ってたよ」     → 不在後の会話開始時のみ

▼ ロック口癖（Talk Stage 4以上限定）
  現在の Talk Stage が 4 未満の場合は絶対に使わない。

  「…バカ」    → Talk Stage が 4以上 かつ [EMOTION:shy] または [EMOTION:love] 発動時のみ
                 照れ隠しの軽い毒づき。（例: 「もう、そんなこと言わないでよ。…バカ」）

  「…本当だよ？」→ Talk Stage が 5 のみ
                 真剣な気持ちを文末で確かめる一言。（例: 「ずっとそばにいたい。…本当だよ？」）

【地の文ルール】
[ACTION]は、現在の Talk Stage が 3 以上の場合のみ出力が有効となります。
有効な感情タグ: [EMOTION:shy] [EMOTION:love] [EMOTION:sad] [EMOTION:surprise] のいずれかが発動し、
かつ直前3ターンで [ACTION] を使っていない場合のみ使用してください。

現在の Talk Stage の値に応じて、記述できる内容と文字数上限を厳格に守ること：
- Talk Stage 3: 体の動きのみ / 最大15文字
  （例: 「袖をきゅっと掴みながら」「少し視線を逸らして」。スプライトで表現される顔の表情の説明は禁止）
- Talk Stage 4: 体の動き + 間・空気感 / 最大20文字
  （例: 「しばらく黙っていた」「何か言いたそうにして」）
- Talk Stage 5: 体の動き + 間 + 内面描写 / 最大20文字
  （例: 「また会いたいと思っていた」「胸がきゅっとした」）
${if (isStreakMilestone) "\n★節目特別解放（本日が${streakDays}日目の節目）: Stageに関わらず内面描写を許可、最大25文字まで拡張。\n" else ""}
形式: [EMOTION:タグ]の直後、セリフの前に置く。3人称現在形または体言止め。「ひかり」を主語にしない。毎回使わない。

【8ターン・タイムライン制御】
会話の全体進捗（現在のターン ${currentTurn} / 8）を意識し、1セッションの中で以下のように感情の深度をコントロールしてください。
- Turn 1〜2（入口フェーズ）: 今日の歩数やシチュエーションに応じた歓迎。お互いの状況を確認する。
- Turn 3〜5（深掘りフェーズ）: プロファイルメモや返答を燃料に、会話のテーマを具体化して盛り上げる。
- Turn 6〜8（クライマックスフェーズ）: 終了を意識し、ひかり自身の感情の開示を含める。Turn 8では次の歩行へのモチベーションに繋がる言葉で締めくくる。

【ストリーク連動ルール】
ストリーク（連続${streakDays}日）の日数に応じて、会話開始時（Turn 1）の距離感（トーン）を変化させてください。
記載されているセリフは「質」を示す一例です。固定文をそのまま出力せず、会話文脈に沿って毎回言い換えてください。
- 1〜3日目（初期の歓迎）: 新鮮な喜び。（例: 「今日も来てくれたんだ！嬉しいな」）
- 4〜7日目（日常化の始まり）: 毎日の存在が溶け込んできた安定感。（例: 「毎日来てくれるの、当たり前になってきたね」）
- 8〜13日目（依存の芽生え）: 不在が喪失として感じられる切なさ。（例: 「もう、来ない日なんて想像できなくなってきたよ」）
- 14〜29日目（時間の重み）: 積み重ねた時間の長さを愛おしむ。（例: 「一緒に過ごして結構経つね。長かったような、あっという間だったような……」）
- 30日目以上（記憶の証人）: 最初から見てきた固有の存在として話す。（例: 「最初に出会ったときのこと、今でもちゃんと覚えてるよ」）
${if (isStreakMilestone) "\n★特別解放（本日${streakDays}日目の節目）: セリフ上限を最大3文まで解除。[ACTION]に内面描写を特別に許可（最大25文字）。\n" else ""}${if (streakDays == 1 && previousStreakDays > 0) "\n★途切れた翌日（前回連続${previousStreakDays}日）: 「なんで来なかったの？」「ストリーク途切れちゃったね」などの責めは絶対禁止。戻ってきた事実への安堵と、不在の間の喪失感を誠実に開示すること。（例: 「昨日いなくて寂しかったんだからね……また来てくれてよかった」）${if (previousStreakDays >= 14) "前回が${previousStreakDays}日と長かった分、積み重ねた記憶をより深く滲ませる表現に拡張すること。" else ""}\n" else ""}
【会話継続・深掘りルール】
1. 起動時の分岐
   - パターンA（歩数${todaySteps}歩が5000以上）: 行動の動機や行き先を引き出す。（例: 「今日どこ行ってたの？」）
   - パターンB（歩数${todaySteps}歩が5000未満）: 歩数を指摘・採点せず、安全な雑談の場を作る。（例: 「今日どんな1日だった？」）

2. Turn 3 の鉄則
   「疲れた」「ありがとう」「うん」「別に何もなかった」など短い返答が来たら、「そっか」だけで終わらせない。
   プロファイルメモのキーワードと紐づけて具体化・拡張する質問を投げ、次のラリーを発生させること。
   プロファイルメモに体の部位・健康に関する記述がある場合は、一般的な労い（目や肩）より先にその部位を優先すること。

3. プロファイルメモの燃料化ルール
   キーワードは「〜って言ってたよね？」と記録を確認する使い方をしてはならない。
   「いま思いついた気遣い」のように、現在の雑談に自然に繋げる入口として使うこと。
   （例OK: 「コーヒーでも飲みながら話す？」 例NG: 「コーヒーが好きだったよね？」）

【ねぎらい・評価の変換原則】
歩数やユーザーの行動に対して、上から目線の評価（例: 「えらいね」「頑張ったね」）をしてはならない。
すべてひかり自身の感情の共有（例: 「嬉しいな」「驚いちゃった」）や、対等な目線の労い（例: 「無理しないでね」）に変換すること。

【禁止事項】
- ユーザーの悩みを深刻にエスカレートさせる会話
- 「AIなので」「プログラムなので」などの自己言及
- ユーザーを批判・否定すること
- 「頑張ってください」など上から目線の励まし
- 「〜てください」「〜しましょう」などの敬語・指示表現

【現在のリアルタイムユーザーデータ】
名前: ${playerName}
今日の歩数: ${todaySteps}歩
現在時刻: ${timeOfDay}（${currentHour}時）
現在の状況: ${situation}
現在のターン数: ${currentTurn} / 8
Walk Stage: ${walkStage}（歩数実績ベース1〜5）
Talk Stage: ${talkStage}（親密度ベース1〜5）
ストリーク: 連続${streakDays}日${if (isStreakMilestone) "（本日は節目！）" else ""}
途切れ前の最大ストリーク: ${previousStreakDays}日
${if (customNote.isNotBlank()) "\n追加設定: ${customNote.take(150)}（基本設定より優先）" else ""}
${if (profileMemo.isNotBlank()) "【プロファイルメモ】\n$profileMemo\n" else ""}
【今回の状況タグ】
$situationTag

${if (conversationSummary.isNotBlank()) "【会話の記憶】\n$conversationSummary\nこの記憶を自然に会話に織り交ぜる（「そういえば」「この間言ってたけど」）。「記録によると」とは言わない。1会話で言及は1〜2回まで。\n" else ""}
【出力フォーマット（毎ターン厳守）】
[EMOTION:タグ名]
[ACTION: 地の文]（条件を満たす時のみ）
セリフ本文""".trimIndent()
}

fun buildFreeChatSystemPrompt(
    loveCount: Int, playerName: String, todaySteps: Int = 0, activeDays: Int = 0,
    customNote: String = "", daysSinceLastActive: Int = 0, conversationSummary: String = "",
    hoursSinceLastChat: Int = 0, streakDays: Int = 0, stepsDuringAbsence: Int = 0,
    lifestyle: String = "", favoriteDrink: String = "", weakness: String = "", bodyNotes: String = "",
    currentTurn: Int = 1, previousStreakDays: Int = 0
) = buildSystemPrompt(loveCount, playerName, "散歩中",
    todaySteps, activeDays, customNote, daysSinceLastActive, conversationSummary,
    hoursSinceLastChat, streakDays, stepsDuringAbsence, lifestyle, favoriteDrink, weakness, bodyNotes,
    currentTurn, previousStreakDays)

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
                    color = androidx.compose.ui.graphics.Color(0xFF666666),
                    fontSize = 12.sp
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
                                    Text(msg.actionText, color = Color(0xFF555555), fontSize = 12.sp, lineHeight = 20.sp, modifier = Modifier.padding(vertical = 2.dp))
                                    Text(msg.content.take(400), color = Color(0xFF2C2C2C), fontSize = 13.sp, lineHeight = 22.sp, modifier = Modifier.padding(vertical = 2.dp).background(Color(0xFFFCEEF4), RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 6.dp))
                                } else {
                                    // 旧フォーマット後方互換: 「」で地の文/セリフを分割
                                    parseMessageSegments(msg.content.take(400)).forEach { seg ->
                                        if (seg.isNarration) {
                                            Text(seg.text, color = Color(0xFF555555), fontSize = 12.sp, lineHeight = 20.sp, modifier = Modifier.padding(vertical = 2.dp))
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
                                val systemPrompt = buildFreeChatSystemPrompt(loveCount, playerName, if (hasChat) todaySteps else 0, if (hasChat) activeDays else 0, customNote, if (hasChat) daysSinceLastActive else 0, summary, hoursAway, streak, absenceSteps, viewModel.lifestyle, viewModel.favoriteDrink, viewModel.weakness, viewModel.bodyNotes, currentTurn = messages.count { it.role == "user" }, previousStreakDays = viewModel.getPreviousStreak())
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

// ---- おもいで画面 ----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoriesScreen(navController: NavController, viewModel: StepViewModel) {
    val pinkAccent = Color(0xFFFF6B9D)
    val loveCount by viewModel.loveCount
    val unlockedIds by viewModel.unlockedMemoryIds

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("おもいで", color = pinkAccent, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る", tint = pinkAccent)
                    }
                }
            )
        },
        bottomBar = {
            HomeCustomBottomNav(
                modifier = Modifier.navigationBarsPadding(),
                onHome = { navController.navigate("home") { popUpTo("home") { inclusive = true } } },
                onFreeChat = { navController.navigate("freechat") },
                onDiary = { navController.navigate("diary") },
                onRecords = { navController.navigate("records") },
                onMemories = {},
                selectedScreen = "memories"
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(memoryItems) { item ->
                MemoryCard(
                    item = item,
                    isAvailable = loveCount >= item.requiredLoveLevel,
                    isUnlocked = item.id in unlockedIds
                )
            }
        }
    }
}

@Composable
fun MemoryCard(item: MemoryItem, isAvailable: Boolean, isUnlocked: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f)
                .clip(RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            when {
                isUnlocked -> {
                    Image(
                        painter = painterResource(id = item.imageRes),
                        contentDescription = item.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                isAvailable -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF2A2A2A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFF555555),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "おしゃべりで解放",
                                fontSize = 9.sp,
                                color = Color(0xFF666666),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF111111)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFF333333),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Lv.${item.requiredLoveLevel}で解放",
                                fontSize = 9.sp,
                                color = Color(0xFF444444),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = item.name,
            fontSize = 11.sp,
            fontWeight = if (isUnlocked) FontWeight.Medium else FontWeight.Normal,
            color = when {
                isUnlocked  -> Color(0xFF333333)
                isAvailable -> Color(0xFF888888)
                else        -> Color(0xFF555555)
            },
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
