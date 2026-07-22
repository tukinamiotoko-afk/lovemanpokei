package com.example.lovemanpo

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.MediaPlayer
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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.draw.drawBehind
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.util.Base64
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit

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

    var homeChatHistoryJson: String
        get() = prefs.getString("HOME_CHAT_HISTORY", "[]") ?: "[]"
        set(value) = prefs.edit { putString("HOME_CHAT_HISTORY", value) }

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

    var currentDialogue: String
        get() = prefs.getString("CURRENT_DIALOGUE", "今日も一緒にがんばろうね♪") ?: "今日も一緒にがんばろうね♪"
        set(value) = prefs.edit { putString("CURRENT_DIALOGUE", value) }

    var currentWeatherCode: Int
        get() = prefs.getInt("CURRENT_WEATHER_CODE", -1)
        set(value) = prefs.edit { putInt("CURRENT_WEATHER_CODE", value) }

    var currentTemperatureC: Float
        get() = prefs.getFloat("CURRENT_TEMPERATURE_C", Float.NaN)
        set(value) = prefs.edit { putFloat("CURRENT_TEMPERATURE_C", value) }

    // 選択中のBGM（空文字＝オフ、デフォルトは「羊の鼓動」）
    var selectedBgmId: String
        get() = prefs.getString("SELECTED_BGM_ID", "hitujinokodou") ?: "hitujinokodou"
        set(value) = prefs.edit { putString("SELECTED_BGM_ID", value) }

    var bgmVolume: Float
        get() = prefs.getFloat("BGM_VOLUME", 0.6f)
        set(value) = prefs.edit { putFloat("BGM_VOLUME", value) }

    // その日にすでに表示したタッチセリフ（日付 → 表示済みテキストの集合）
    fun getShownTouchDialogues(date: String): Set<String> = prefs.getStringSet("SHOWN_TOUCH_DIALOGUES_$date", emptySet()) ?: emptySet()
    fun setShownTouchDialogues(date: String, shown: Set<String>) = prefs.edit { putStringSet("SHOWN_TOUCH_DIALOGUES_$date", shown) }

    // その日にすでに表示したデフォルト（待機中の話しかけ）セリフ（日付 → 表示済みテキストの集合）
    fun getShownDefaultDialogues(date: String): Set<String> = prefs.getStringSet("SHOWN_DEFAULT_DIALOGUES_$date", emptySet()) ?: emptySet()
    fun setShownDefaultDialogues(date: String, shown: Set<String>) = prefs.edit { putStringSet("SHOWN_DEFAULT_DIALOGUES_$date", shown) }

    // その日最初の「寂しさ」セリフをすでに消費したかどうかの日付（1日1回だけ出す制御用）
    var lastLonelyOpeningDialogueDate: String
        get() = prefs.getString("LAST_LONELY_OPENING_DIALOGUE_DATE", "") ?: ""
        set(value) = prefs.edit { putString("LAST_LONELY_OPENING_DIALOGUE_DATE", value) }

    // 日次日記（日付 → 要約テキスト）
    fun getDailyDiary(date: String): String = prefs.getString("DAILY_DIARY_$date", "") ?: ""
    fun setDailyDiary(date: String, text: String) = prefs.edit { putString("DAILY_DIARY_$date", text) }
    var diaryDates: Set<String>
        get() = prefs.getStringSet("DIARY_DATES", emptySet()) ?: emptySet()
        set(value) = prefs.edit { putStringSet("DIARY_DATES", value) }

    fun getUserDiary(date: String): String = prefs.getString("USER_DIARY_$date", "") ?: ""
    fun setUserDiary(date: String, text: String) = prefs.edit { putString("USER_DIARY_$date", text) }
    fun getUserDiaryPhotoPath(date: String): String = prefs.getString("USER_DIARY_PHOTO_$date", "") ?: ""
    fun setUserDiaryPhotoPath(date: String, path: String) = prefs.edit { putString("USER_DIARY_PHOTO_$date", path) }
    fun getDiaryMood(date: String): String = prefs.getString("DIARY_MOOD_$date", "") ?: ""
    fun setDiaryMood(date: String, mood: String) = prefs.edit { putString("DIARY_MOOD_$date", mood) }
    fun getDiaryReply(date: String): String = prefs.getString("DIARY_REPLY_$date", "") ?: ""
    fun setDiaryReply(date: String, text: String) = prefs.edit { putString("DIARY_REPLY_$date", text) }
    fun getDiaryReplyEmotion(date: String): String = prefs.getString("DIARY_REPLY_EMOTION_$date", "normal") ?: "normal"
    fun setDiaryReplyEmotion(date: String, emotion: String) = prefs.edit { putString("DIARY_REPLY_EMOTION_$date", emotion) }
    var userDiaryDates: Set<String>
        get() = prefs.getStringSet("USER_DIARY_DATES", emptySet()) ?: emptySet()
        set(value) = prefs.edit { putStringSet("USER_DIARY_DATES", value) }

    // デバッグ用：ONにすると日記の返信を翌日ではなく即時生成する
    var debugInstantDiaryReply: Boolean
        get() = prefs.getBoolean("DEBUG_INSTANT_DIARY_REPLY", false)
        set(value) = prefs.edit { putBoolean("DEBUG_INSTANT_DIARY_REPLY", value) }

    var debugChatMaxChars: Int
        get() = prefs.getInt("DEBUG_CHAT_MAX_CHARS", 150)
        set(value) = prefs.edit { putInt("DEBUG_CHAT_MAX_CHARS", value) }

    // デバッグ用：ONにすると今日の日記を書いた後も続けて（過去日付に）日記を作成できる
    var debugMultiDiary: Boolean
        get() = prefs.getBoolean("DEBUG_MULTI_DIARY", false)
        set(value) = prefs.edit { putBoolean("DEBUG_MULTI_DIARY", value) }

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

    var lastGreetingSessionDate: String
        get() = prefs.getString("LAST_GREETING_SESSION_DATE", "") ?: ""
        set(value) = prefs.edit { putString("LAST_GREETING_SESSION_DATE", value) }

    var unlockedMemoryIds: Set<String>
        get() = prefs.getStringSet("UNLOCKED_MEMORY_IDS", emptySet()) ?: emptySet()
        set(value) = prefs.edit { putStringSet("UNLOCKED_MEMORY_IDS", value) }

    var ownedCostumeIds: Set<String>
        get() = prefs.getStringSet("OWNED_COSTUME_IDS", setOf("default")) ?: setOf("default")
        set(value) = prefs.edit { putStringSet("OWNED_COSTUME_IDS", value) }

    var equippedCostumeId: String
        get() = prefs.getString("EQUIPPED_COSTUME_ID", "default") ?: "default"
        set(value) = prefs.edit { putString("EQUIPPED_COSTUME_ID", value) }

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
class StepViewModel(val repository: StepRepository) : ViewModel() {
    val allStepRecords = mutableStateOf<List<StepRecord>>(emptyList())
    val hourlyStepRecords = mutableStateOf<List<HourlyStepRecord>>(emptyList())
    val todaySteps = mutableIntStateOf(0)
    val cumulativeSteps = mutableIntStateOf(repository.cumulativeSteps)
    val playerName = mutableStateOf(repository.playerName)
    val loveCount = mutableIntStateOf(repository.loveCount)
    val heartCount = mutableIntStateOf(repository.heartCount)
    val selectedBgmId = mutableStateOf(repository.selectedBgmId)
    fun setSelectedBgm(id: String) {
        selectedBgmId.value = id
        repository.selectedBgmId = id
    }
    val bgmVolume = mutableFloatStateOf(repository.bgmVolume)
    fun setBgmVolume(volume: Float) {
        bgmVolume.floatValue = volume
        repository.bgmVolume = volume
    }

    // ホーム画面の待機中セリフ。画面を離れてまた戻ってきただけでは変わらないよう、
    // ViewModel（ナビゲーションをまたいで生き続ける）側に保持する
    var currentDefaultDialogue = mutableStateOf<TouchDialogue?>(null)
    val pendingLevelUpLevel = mutableIntStateOf(0)
    val pendingOdekakeInvite = mutableStateOf<String?>(null)
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
    val heartGaugeProgress = derivedStateOf { heartCount.intValue.toFloat() / 10f }

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

    // ホームチャットの要約を追記（おしゃべりの要約を上書きしないよう追加方式にする）
    fun appendDailyDiarySummary(summary: String) {
        val today = LocalDate.now().toString()
        val existing = repository.getDailyDiary(today)
        val merged = if (existing.isBlank()) summary else "$existing\n$summary"
        repository.setDailyDiary(today, merged.takeLast(600))
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

    fun saveUserDiary(date: String, text: String, mood: String = "") {
        repository.setUserDiary(date, text)
        if (mood.isNotBlank()) repository.setDiaryMood(date, mood)
        val dates = repository.userDiaryDates.toMutableSet()
        dates.add(date)
        repository.userDiaryDates = dates.sorted().takeLast(90).toSet()
    }

    fun saveDiaryReply(date: String, replyText: String, emotion: String) {
        repository.setDiaryReply(date, replyText)
        repository.setDiaryReplyEmotion(date, emotion)
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

    fun saveCurrentDialogue(text: String) { repository.currentDialogue = text }

    // 今日すでに表示したタッチセリフを取得し、新しく表示したセリフを記録する
    fun getTodayShownTouchDialogues(): Set<String> = repository.getShownTouchDialogues(LocalDate.now().toString())
    fun markTouchDialogueShown(text: String) {
        val today = LocalDate.now().toString()
        val shown = repository.getShownTouchDialogues(today).toMutableSet()
        shown.add(text)
        repository.setShownTouchDialogues(today, shown)
    }

    // 今日すでに表示した「待機中の話しかけ」セリフを取得し、新しく表示したセリフを記録する
    fun getTodayShownDefaultDialogues(): Set<String> = repository.getShownDefaultDialogues(LocalDate.now().toString())
    fun markDefaultDialogueShown(text: String) {
        val today = LocalDate.now().toString()
        val shown = repository.getShownDefaultDialogues(today).toMutableSet()
        shown.add(text)
        repository.setShownDefaultDialogues(today, shown)
    }

    // その日まだ「寂しさ」セリフを出していなければ1つ選んで消費する（1日1回だけ）
    fun consumeLonelyOpeningDialogueIfNeeded(loveCount: Int): String? {
        val today = LocalDate.now().toString()
        if (repository.lastLonelyOpeningDialogueDate == today) return null
        repository.lastLonelyOpeningDialogueDate = today
        return homeLonelyDialogues(loveCount).randomOrNull()
    }
    fun saveWeatherCode(code: Int) { repository.currentWeatherCode = code }
    fun saveWeatherInfo(info: WeatherInfo) {
        repository.currentWeatherCode = info.weatherCode
        repository.currentTemperatureC = info.tempC.toFloat()
    }

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

    val homeChatMessages = mutableStateListOf<ChatMessage>().also { list ->
        try {
            val json = org.json.JSONArray(repository.homeChatHistoryJson)
            repeat(json.length()) { i ->
                val obj = json.getJSONObject(i)
                list.add(ChatMessage(obj.getString("role"), obj.getString("content")))
            }
        } catch (_: Exception) {}
    }

    fun saveHomeChatHistory() {
        // 直近10件（5往復）のみ保持。無制限に肥大化させない
        while (homeChatMessages.size > 10) homeChatMessages.removeAt(0)
        val json = org.json.JSONArray()
        homeChatMessages.forEach { msg ->
            json.put(org.json.JSONObject().apply {
                put("role", msg.role)
                put("content", msg.content)
            })
        }
        repository.homeChatHistoryJson = json.toString()
    }

    private fun getSessionDate(): String {
        val now = java.time.LocalTime.now()
        val today = LocalDate.now()
        return if (now.hour < 5) today.minusDays(1).toString() else today.toString()
    }

    fun checkAndSendMorningGreeting(): Pair<String, Int>? {
        val sessionDate = getSessionDate()
        if (repository.lastGreetingSessionDate == sessionDate) return null
        repository.lastGreetingSessionDate = sessionDate
        val name = repository.playerName
        val love = loveCount.intValue
        return when {
            love <= 2 -> Pair("おはようございます、${name}さん。今日もよろしくお願いします。", R.drawable.osyaberi_normal)
            love <= 4 -> Pair("おはようございます！今日も会えてよかったです♪", R.drawable.osyaberi_smile)
            love <= 6 -> Pair("おはようございます♡ また来てくれたんですね！うれしいです。", R.drawable.osyaberi_sugokuegao)
            love <= 8 -> Pair("おはようございます！…待っていましたよ、${name}さん♡", R.drawable.osyaberi_tereru)
            else      -> Pair("…来てくれたんですね。おはようございます、${name}さん♡ 今日もそばにいてください。", R.drawable.osyaberi_koigokorowoidaku)
        }
    }

    fun spendPointForChat(): Boolean {
        if (currentActionPoints.value > 0) {
            repository.spentActionPoints++
            spentActionPoints.intValue = repository.spentActionPoints
            return true
        }
        return false
    }

    val ownedCostumeIds = mutableStateOf(repository.ownedCostumeIds)
    val equippedCostumeId = mutableStateOf(repository.equippedCostumeId)

    fun spendPoints(amount: Int): Boolean {
        if (currentActionPoints.value >= amount) {
            repository.spentActionPoints += amount
            spentActionPoints.intValue = repository.spentActionPoints
            return true
        }
        return false
    }

    fun buyCostume(costume: Costume): Boolean {
        if (costume.id in ownedCostumeIds.value) return false
        if (!spendPoints(costume.price)) return false
        val updated = ownedCostumeIds.value + costume.id
        repository.ownedCostumeIds = updated
        ownedCostumeIds.value = updated
        return true
    }

    fun equipCostume(costumeId: String) {
        if (costumeId !in ownedCostumeIds.value) return
        repository.equippedCostumeId = costumeId
        equippedCostumeId.value = costumeId
    }

    fun refundPointForChat() {
        if (repository.spentActionPoints > 0) {
            repository.spentActionPoints--
            spentActionPoints.intValue = repository.spentActionPoints
        }
    }

    fun checkAndApplyLevelUp() {
        if (heartCount.intValue < 10) return
        val nextLevel = loveCount.intValue + 1
        if (nextLevel > 10) return
        val wall = loveLevelWalls.find { it.level == nextLevel }
        val totalSteps = allStepRecords.value.sumOf { it.stepCount.toLong() }
        if (wall == null || totalSteps >= wall.totalSteps) {
            loveCount.intValue = nextLevel
            heartCount.intValue = 0
            repository.loveCount = nextLevel
            repository.heartCount = 0
            pendingLevelUpLevel.intValue = nextLevel
        }
        // 壁未クリア → heartCount は 15 のまま（ゲージ満タン待機）
    }

    fun dismissLevelUpNotification() { pendingLevelUpLevel.intValue = 0 }
    fun setOdekakeInvite(locationName: String) { pendingOdekakeInvite.value = locationName }
    fun consumeOdekakeInvite(): String? = pendingOdekakeInvite.value.also { pendingOdekakeInvite.value = null }

    fun earnHeart() {
        if (heartCount.intValue >= 10) {
            checkAndApplyLevelUp()
            return
        }
        heartCount.intValue++
        repository.heartCount = heartCount.intValue
        if (heartCount.intValue >= 10) {
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

    val debugInstantDiaryReply = mutableStateOf(repository.debugInstantDiaryReply)
    fun setDebugInstantDiaryReply(enabled: Boolean) {
        repository.debugInstantDiaryReply = enabled
        debugInstantDiaryReply.value = enabled
    }

    val debugMultiDiary = mutableStateOf(repository.debugMultiDiary)
    fun setDebugMultiDiary(enabled: Boolean) {
        repository.debugMultiDiary = enabled
        debugMultiDiary.value = enabled
    }

    val debugChatMaxChars = mutableIntStateOf(repository.debugChatMaxChars)
    fun setDebugChatMaxChars(value: Int) {
        repository.debugChatMaxChars = value
        debugChatMaxChars.intValue = value
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

val DiaryFemaleFontFamily = FontFamily(
    Font(R.font.zen_kurenaido_regular, FontWeight.Normal)
)

val DiaryMaleFontFamily = FontFamily(
    Font(R.font.kaisei_decol_regular, FontWeight.Normal)
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

        // アプリ全体で流すBGM。選択中のトラックが変わったら曲を切り替え、
        // アプリがバックグラウンドに行ったら一時停止・戻ってきたら再開する。
        val selectedBgmId by viewModel.selectedBgmId
        val bgmVolume by viewModel.bgmVolume
        var currentBgmPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
        val lifecycleOwner = LocalLifecycleOwner.current
        val bgmScope = rememberCoroutineScope()
        DisposableEffect(selectedBgmId) {
            var mediaPlayer: MediaPlayer? = null
            var disposed = false
            val job = bgmScope.launch {
                delay(500)
                val track = bgmTracks.find { it.id == selectedBgmId }
                val prepared = track?.let {
                    withContext(Dispatchers.IO) {
                        try {
                            MediaPlayer.create(context, it.resId)?.apply { isLooping = true; setVolume(bgmVolume, bgmVolume) }
                        } catch (e: Exception) {
                            null
                        }
                    }
                }
                if (!disposed) {
                    mediaPlayer = prepared
                    currentBgmPlayer = prepared
                    try { prepared?.start() } catch (e: Exception) {}
                } else {
                    try { prepared?.release() } catch (e: Exception) {}
                }
            }
            val observer = LifecycleEventObserver { _, event ->
                try {
                    when (event) {
                        Lifecycle.Event.ON_RESUME -> mediaPlayer?.let { if (!it.isPlaying) it.start() }
                        Lifecycle.Event.ON_PAUSE -> mediaPlayer?.let { if (it.isPlaying) it.pause() }
                        else -> {}
                    }
                } catch (e: Exception) {}
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                disposed = true
                job.cancel()
                lifecycleOwner.lifecycle.removeObserver(observer)
                try { mediaPlayer?.release() } catch (e: Exception) {}
                if (currentBgmPlayer === mediaPlayer) currentBgmPlayer = null
            }
        }
        LaunchedEffect(bgmVolume) {
            try { currentBgmPlayer?.setVolume(bgmVolume, bgmVolume) } catch (e: Exception) {}
        }

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
                composable("shop") { ShopScreen(navController, viewModel) }
                composable("wardrobe") { CostumeChangeScreen(navController, viewModel) }
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

// 端末の「バッテリーセーバー（省電力モード）」が今オンになっているかどうか
fun isPowerSaveModeOn(context: Context): Boolean {
    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return pm.isPowerSaveMode
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
    val pendingLevelUp by viewModel.pendingLevelUpLevel

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

    // バッテリーセーバーがオンの時は、毎回（ホーム画面に来るたび）説明を出して促す
    var showBatterySaverPrompt by remember { mutableStateOf(isPowerSaveModeOn(context)) }
    val batterySaverLifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(batterySaverLifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && isPowerSaveModeOn(context)) {
                showBatterySaverPrompt = true
            }
        }
        batterySaverLifecycleOwner.lifecycle.addObserver(observer)
        onDispose { batterySaverLifecycleOwner.lifecycle.removeObserver(observer) }
    }
    if (showBatterySaverPrompt) {
        AlertDialog(
            onDismissRequest = { showBatterySaverPrompt = false },
            title = { Text("バッテリーセーバーがオンです", fontWeight = FontWeight.Bold) },
            text = {
                Text("バッテリーセーバー（省電力モード）がオンになっていると、歩数のカウントが止まったり、記録が遅れたりすることがあります。正確に記録するために、バッテリーセーバーをオフにすることをおすすめします。")
            },
            confirmButton = {
                TextButton(onClick = {
                    showBatterySaverPrompt = false
                    try {
                        context.startActivity(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS))
                    } catch (e: Exception) {}
                }) { Text("設定を開く") }
            },
            dismissButton = {
                TextButton(onClick = { showBatterySaverPrompt = false }) { Text("閉じる") }
            }
        )
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
    val stepDialogue = homeStepDialogue(todaySteps = todaySteps, loveCount = loveCount)

    val touchDialogues = homeTouchDialogues(loveCount)
    var touchedDialogue by remember { mutableStateOf<TouchDialogue?>(null) }
    LaunchedEffect(touchedDialogue) {
        if (touchedDialogue != null) {
            delay(5000)
            touchedDialogue = null
        }
    }

    var homeChatReply by remember { mutableStateOf<Pair<String, Int>?>(null) }
    var isHomeChatLoading by remember { mutableStateOf(false) }
    var weatherDialogueActive by remember { mutableStateOf(false) }
    var homeToastMessage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(homeToastMessage) {
        if (homeToastMessage != null) {
            delay(1800)
            homeToastMessage = null
        }
    }

    val weatherDialogue = weatherInfo?.let { homeWeatherDialogue(it.weatherCode) }
    // 歩数達成メッセージはセリフとしては表示しない（歩数データ自体はこれまで通り記録・利用する）
    val activeWeatherDialogue = if (weatherDialogueActive) weatherDialogue else null

    // その日最初の表示なら「寂しさ」セリフを1回だけ出す
    val todayDateStr = remember { LocalDate.now().toString() }
    val openingLonelyDialogue = remember(todayDateStr) { viewModel.consumeLonelyOpeningDialogueIfNeeded(loveCount) }

    // 待機中のデフォルトセリフ：今日すでに出したものは除外して選ぶ。
    // ViewModel側に選択結果を保持するので、画面を離れてまた戻ってきただけでは変わらない。
    // くるくるボタンを押した時だけ選び直す。
    val defaultDialoguePool = homeDefaultDialogues(loveCount)
    fun pickDefaultDialogue(): TouchDialogue? {
        val shownToday = viewModel.getTodayShownDefaultDialogues()
        val candidates = defaultDialoguePool.filter { it.text !in shownToday }
        val pool = candidates.ifEmpty { defaultDialoguePool }
        val picked = pool.randomOrNull()
        if (picked != null) viewModel.markDefaultDialogueShown(picked.text)
        return picked
    }
    LaunchedEffect(Unit) {
        if (viewModel.currentDefaultDialogue.value == null) {
            viewModel.currentDefaultDialogue.value = pickDefaultDialogue()
        }
    }
    val defaultDialogueEntry = viewModel.currentDefaultDialogue.value

    val displayMessage = (homeChatReply?.first ?: touchedDialogue?.text ?: activeWeatherDialogue?.text ?: openingLonelyDialogue ?: defaultDialogueEntry?.text ?: stepDialogue.text).replace("○○", playerName)
    val baseDisplayExpression = homeChatReply?.second ?: touchedDialogue?.expr ?: activeWeatherDialogue?.expr
        ?: (if (openingLonelyDialogue != null) R.drawable.hikari_think else null)
        ?: defaultDialogueEntry?.expr ?: stepDialogue.expr
    val equippedCostumeId by viewModel.equippedCostumeId
    val displayExpression = costumedExpressionRes(baseDisplayExpression, equippedCostumeId)

    LaunchedEffect(displayMessage, playerName) {
        viewModel.saveCurrentDialogue(displayMessage.replace("○○", playerName))
    }
    LaunchedEffect(weatherInfo) {
        weatherInfo?.let { viewModel.saveWeatherInfo(it) }
    }

    HomeScreenContent(
        uiState = HomeScreenUiState(
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
            isHomeChatLoading = isHomeChatLoading,
            toastMessage = homeToastMessage
        ),
        actions = HomeScreenActions(
            onCharacterClick = {
            if (touchedDialogue != null) {
                // まだ前のリアクションが表示中＝連続でタップ（複数回触った）
                val available = homeAvailableMultiTouchDialogues(loveCount)
                available.randomOrNull()?.let { touchedDialogue = TouchDialogue(it.text, it.expr) }
            } else {
                val shownToday = viewModel.getTodayShownTouchDialogues()
                val candidates = touchDialogues.filter { it.text !in shownToday }
                // 今日出せるセリフを全部出し切ったら、リストの最初からまた出せるようにする
                val pool = candidates.ifEmpty { touchDialogues }
                val picked = pool.randomOrNull()
                if (picked != null) viewModel.markTouchDialogueShown(picked.text)
                touchedDialogue = picked
            }
            weatherDialogueActive = false
        },
            onRefreshDialogue = {
            homeChatReply = null
            touchedDialogue = null
            weatherDialogueActive = false
            viewModel.currentDefaultDialogue.value = pickDefaultDialogue()
        },
            onWeatherTap = { weatherDialogueActive = true },
            onHomeChatSend = homeChatSend@{ text ->
            if (!viewModel.spendPointForChat()) {
                homeToastMessage = "行動ポイントが足りません（2000歩で1ポイント）"
                return@homeChatSend
            }
            weatherDialogueActive = false
            isHomeChatLoading = true
            // 送信直前に表示されていたセリフ（デフォルトセリフ等）。毎回背景情報として渡す
            val openingLine = displayMessage
            scope.launch {
                try {
                    val memory = viewModel.buildMemoryContext()
                    val openingLineNote =
                        "\n（参考）あなたが直前に表示していたセリフ：「${openingLine}」。ユーザーの発言がこれに関連していれば自然に踏まえてください。無関係な話題であれば、このセリフには触れずユーザーの発言だけに応答してください。"
                    val memoryNote = if (memory.isNotBlank())
                        "\n【会話の記憶】\n$memory\n記憶に触れる場合は自分の言葉で自然に。1会話で言及は1回まで。" else ""
                    val yesterday = java.time.LocalDate.now().minusDays(1).toString()
                    val yestMood = viewModel.repository.getDiaryMood(yesterday)
                    val yestHint = viewModel.repository.getUserDiary(yesterday).take(50)
                    val diaryNote = if (yestMood.isNotBlank() && yestHint.isNotBlank())
                        "\n【昨日の日記】気分：$yestMood　内容：「$yestHint」\n自然な流れで一度だけ触れてもいい。しつこく聞かない。" else ""
                    val loverModeNote = if (loveCount >= 9) """

【恋人モード（好感度9-10）の口調】
敬語をベースにしつつ、恋人らしい甘え・わがままを混ぜてよい（「〜してほしいな」「〜してくれる？」「〜だめですよ？」等の柔らかい語尾もOK）。
「……」を積極的に使う。フィラー・感動詞も自然に混ぜる。
手をつなぐ・ぎゅっとする・頭をなでてもらう等のスキンシップ要素を自然に匂わせてよい。
「寂しい」「もっと一緒にいたい」という気持ちを素直に出してよい。""" else ""
                    val prompt = """あなたは「ひかり」（22歳）。${playerName}さんと散歩中の話し相手。
返答の先頭に[EMOTION:タグ名]を出力する。タグ: happy / love / shy / sad / worry / normal
30〜70文字で自然に返す。句読点で区切りやすい文にする。敬語。AIっぽい表現禁止。「${playerName}さん」と「さん」付けで呼ぶ。「今一緒に歩いている」視点で話す。「……」を積極的に使う。文の途中だけでなく、文頭を「……」から始めても良い。照れ・ためらい・余韻・言葉に詰まる場面・考えている場面で使う。$loverModeNote
今日の歩数：${todaySteps}歩。$openingLineNote$memoryNote$diaryNote"""
                    val history = viewModel.homeChatMessages.takeLast(10)
                    // thinking系モデルは内部思考トークンもmaxOutputTokensに含まれることがあるため、
                    // 本文が思考トークンに食われて途中で切れないよう余裕を持たせる
                    val raw = callGeminiApi(prompt, history, text, maxTokens = 1000)
                    val emotionMatch = Regex("""\[EMOTION:(\w+)\]""").find(raw)
                    val emotion = emotionMatch?.groupValues?.get(1) ?: "normal"
                    val replyText = raw.replace(emotionMatch?.value ?: "", "").trim()
                    val expr = when (emotion) {
                        "happy", "surprise" -> R.drawable.hikari_celebrate
                        "love", "shy"       -> R.drawable.hikari_blush
                        "sad", "worry"      -> R.drawable.hikari_think
                        else                -> R.drawable.hikari_smile
                    }
                    homeChatReply = Pair(replyText, expr)
                    viewModel.homeChatMessages.add(ChatMessage(role = "user", content = text))
                    viewModel.homeChatMessages.add(ChatMessage(role = "assistant", content = replyText))
                    viewModel.saveHomeChatHistory()

                    // おしゃべり画面と同じ基準で好感度を変動させる
                    when (emotion) {
                        "love", "shy" -> viewModel.earnHeart()
                        "sad"         -> viewModel.loseHeart()
                    }

                    val userTurnCount = viewModel.homeChatMessages.count { it.role == "user" }
                    if (userTurnCount % 5 == 0) {
                        scope.launch {
                            try {
                                val newSummary = callGeminiApiForSummary(viewModel.homeChatMessages)
                                viewModel.appendDailyDiarySummary(newSummary)
                            } catch (_: Exception) {}
                        }
                    }
                } catch (_: Exception) {
                    viewModel.refundPointForChat()
                    homeChatReply = Pair("ごめん、うまく聞こえなかったよ…もう一度話しかけてみて？", R.drawable.hikari_think)
                } finally {
                    isHomeChatLoading = false
                }
            }
        },
            onFreeChatClick = { navController.navigate("freechat") },
            onDiaryClick = { navController.navigate("diary") },
            onRecordsClick = { navController.navigate("records") },
            onMemoriesClick = { navController.navigate("memories") },
            onDebugClick = { navController.navigate("debug") },
            onShopClick = { navController.navigate("shop") },
            onWardrobeClick = { navController.navigate("wardrobe") },
            onSettingsClick = { navController.navigate("settings") }
        )
    )

    if (pendingLevelUp > 0) {
        LevelUpDialog(
            newLevel = pendingLevelUp,
            onDismiss = { viewModel.dismissLevelUpNotification() },
            onGoNow = { locationName ->
                viewModel.setOdekakeInvite(locationName)
                navController.navigate("freechat")
            }
        )
    }
}


data class HomeScreenUiState(
    val todaySteps: Int,
    val actionPoints: Int,
    val stepGaugeProgress: Float,
    val loveCount: Int,
    val heartCount: Int,
    val heartGaugeProgress: Float,
    val playerName: String,
    val bgRes: Int,
    val dialogueMessage: String,
    val expressionRes: Int,
    val activeTimeStr: String,
    val distanceStr: String,
    val caloriesStr: String,
    val weatherInfo: WeatherInfo? = null,
    val isHomeChatLoading: Boolean = false,
    val toastMessage: String? = null
)

data class HomeScreenActions(
    val onCharacterClick: () -> Unit,
    val onFreeChatClick: () -> Unit,
    val onDiaryClick: () -> Unit,
    val onRecordsClick: () -> Unit,
    val onDebugClick: () -> Unit,
    val onRefreshDialogue: () -> Unit = {},
    val onWeatherTap: () -> Unit = {},
    val onHomeChatSend: (String) -> Unit = {},
    val onMemoriesClick: () -> Unit = {},
    val onShopClick: () -> Unit = {},
    val onWardrobeClick: () -> Unit = {},
    val onSettingsClick: () -> Unit = {}
)

// キャラ画像は素材によって足元の透明な余白の量がバラバラなので、
// 画像全体のサイズ・縦横比はそのまま使いつつ（サイズ感が変わらないように）、
// 「下端から実際に絵が描かれている行までの透明な余白の割合」だけを測っておき、
// その分だけ表示位置を下にずらしてカード上端にちょうど合わせる。
private fun bottomDeadSpaceFraction(bitmap: Bitmap): Float {
    val w = bitmap.width
    val h = bitmap.height
    val alphaThreshold = 10
    val row = IntArray(w)
    for (y in (h - 1) downTo 0) {
        bitmap.getPixels(row, 0, w, 0, y, w, 1)
        for (x in 0 until w) {
            if ((row[x] ushr 24) and 0xFF > alphaThreshold) {
                return (h - 1 - y).toFloat() / h.toFloat()
            }
        }
    }
    return 0f
}

@Composable
fun HomeScreenContent(
    uiState: HomeScreenUiState,
    actions: HomeScreenActions
) {
    val todaySteps = uiState.todaySteps
    val actionPoints = uiState.actionPoints
    val stepGaugeProgress = uiState.stepGaugeProgress
    val loveCount = uiState.loveCount
    val heartCount = uiState.heartCount
    val heartGaugeProgress = uiState.heartGaugeProgress
    val playerName = uiState.playerName
    val bgRes = uiState.bgRes
    val dialogueMessage = uiState.dialogueMessage
    val expressionRes = uiState.expressionRes
    val activeTimeStr = uiState.activeTimeStr
    val distanceStr = uiState.distanceStr
    val caloriesStr = uiState.caloriesStr
    val weatherInfo = uiState.weatherInfo
    val isHomeChatLoading = uiState.isHomeChatLoading
    val toastMessage = uiState.toastMessage
    val onCharacterClick = actions.onCharacterClick
    val onFreeChatClick = actions.onFreeChatClick
    val onDiaryClick = actions.onDiaryClick
    val onRecordsClick = actions.onRecordsClick
    val onMemoriesClick = actions.onMemoriesClick
    val onDebugClick = actions.onDebugClick
    val onShopClick = actions.onShopClick
    val onWardrobeClick = actions.onWardrobeClick
    val onSettingsClick = actions.onSettingsClick
    val onRefreshDialogue = actions.onRefreshDialogue
    val onWeatherTap = actions.onWeatherTap
    val onHomeChatSend = actions.onHomeChatSend
    var showWeatherSheet by remember { mutableStateOf(false) }

    // ヘルプキャラ(HintSdHikari)専用の固定位置。メインキャラの余白値とは無関係。
    val characterBottomPadding = 240.dp

    // メインキャラの接地位置＝セリフ枠（バナー＋入力欄）の実際の見た目の上端。
    // テキスト欄は常に2行固定高さなのでカードの高さは安定しており、実測してよい。
    val density = LocalDensity.current
    var visualCardHeightPx by remember { mutableStateOf(0f) }
    val fallbackVisualCardHeightPx = with(density) { 150.dp.toPx() }
    val effectiveVisualCardHeightPx = if (visualCardHeightPx > 0f) visualCardHeightPx else fallbackVisualCardHeightPx
    val characterGroundPadding = 12.dp + with(density) { effectiveVisualCardHeightPx.toDp() }

    // painterResource()が端末によっては起動直後にリソースID解決へ失敗することがあるため、
    // 背景画像だけはBitmapFactoryで直接デコードして読み込む（より原始的で確実な経路）
    val bgContext = LocalContext.current
    val bgBitmap = remember(bgRes) {
        try {
            BitmapFactory.decodeResource(bgContext.resources, bgRes)?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (bgBitmap != null) {
            Image(
                bitmap = bgBitmap,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFFF5F7)))
        }

        // キャラ表示エリア：ステータスバー直下からほぼ画面全体を使う。
        // 上部カード・吹き出しカードは共に独立したオーバーレイなので、キャラはその裏にも回り込める。
        // 足元（接地位置）はセリフ枠の見た目の上端（characterGroundPadding）で固定し、
        // サイズは characterScale だけで調整する。
        // 拡大率は下端(transformOrigin y=1f)を軸にするので、スケールを変えても接地位置は動かない。
        val characterScale = 0.72f
        Box(modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(bottom = characterGroundPadding)
            .zIndex(0.5f)) {
            val characterBitmap = remember(expressionRes) {
                try {
                    BitmapFactory.decodeResource(bgContext.resources, expressionRes)
                } catch (e: Exception) {
                    null
                }
            }
            val characterAspectRatio = remember(characterBitmap) {
                val bmp = characterBitmap
                if (bmp != null && bmp.width > 0 && bmp.height > 0) {
                    bmp.width.toFloat() / bmp.height.toFloat()
                } else {
                    3f / 5f
                }
            }
            val characterBottomDeadFraction = remember(characterBitmap) {
                characterBitmap?.let { bottomDeadSpaceFraction(it) } ?: 0f
            }
            val characterModifier = Modifier
                .fillMaxHeight(1.0f)
                .aspectRatio(characterAspectRatio, matchHeightConstraintsFirst = true)
                .align(Alignment.BottomCenter)
                .graphicsLayer {
                    scaleX = characterScale
                    scaleY = characterScale
                    transformOrigin = TransformOrigin(0.5f, 1f)
                    translationY = characterBottomDeadFraction * size.height * characterScale
                }
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                    onCharacterClick()
                }
            if (characterBitmap != null) {
                Image(
                    bitmap = characterBitmap.asImageBitmap(),
                    contentDescription = "ひかり",
                    modifier = characterModifier,
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.BottomCenter
                )
            } else {
                Image(
                    painter = painterResource(id = expressionRes),
                    contentDescription = "ひかり",
                    modifier = characterModifier,
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.BottomCenter
                )
            }

            // 上部カード行の高さぶん、明示的に下げてから表示する
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 16.dp, top = 140.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HomeStepCircleGauge(
                    todaySteps,
                    stepGaugeProgress,
                    modifier = Modifier.graphicsLayer {
                        scaleX = 0.9f
                        scaleY = 0.9f
                        transformOrigin = TransformOrigin(0f, 0f)
                    }
                )
            }
        }

        // ヘルプキャラはメインキャラと切り離し、独立した固定位置に置く
        // （メインキャラの余白を変えても一切動かない）
        HintSdHikari(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = characterBottomPadding)
                .offset(x = (-20).dp)
                .zIndex(1f)
        )

        // 買い物・着替えボタンは上部のボタン列から切り離し、その少し下の右側に配置する
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(end = 0.dp, top = 134.dp)
                .zIndex(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HomeTopCircleButton(
                iconRes = R.drawable.aikon_shopping,
                onClick = onShopClick,
                size = 38.dp,
                showBackground = false
            )
            HomeTopCircleButton(
                iconRes = R.drawable.aikon_kigae,
                onClick = onWardrobeClick,
                size = 38.dp,
                showBackground = false
            )
        }

        // 上部カード行 + ボタン列。独立したオーバーレイとしてキャラの上に被さる
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .zIndex(1f),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // ラブレベル・行動ポイントの2枚だけ高さを揃える（ボタン列の高さに引きずられないよう内側のRowで完結させる）
            Row(
                modifier = Modifier.weight(1f).height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                HomeLoveLevelCard(
                    modifier = Modifier.weight(0.72f).fillMaxHeight(),
                    lv = loveCount,
                    progress = heartGaugeProgress,
                    hearts = heartCount
                )
                HomeActionPointsCard(
                    modifier = Modifier.weight(0.72f).fillMaxHeight(),
                    pts = actionPoints
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                HomeTopCircleButton(Icons.Default.Notifications, size = 26.dp)
                HomeTopCircleButton(Icons.Default.Settings, onClick = onSettingsClick, size = 26.dp)
                HomeTopCircleButton(
                    icon = Icons.Default.BugReport,
                    containerColor = Color.Red.copy(alpha = 0.1f),
                    iconColor = Color.Red,
                    onClick = onDebugClick,
                    size = 26.dp
                )
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier.size(26.dp).clickable(enabled = weatherInfo != null) { showWeatherSheet = true; onWeatherTap() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (weatherInfo != null) wmoToEmoji(weatherInfo.weatherCode) else "☁",
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // 吹き出しカードは独立したオーバーレイ。伸びてもキャラの枠を押し縮めず、上に被さるだけ
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .offset(y = (-12).dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 見た目のあるカード本体。実測してキャラの接地位置に使う
                // （テキスト欄は常に2行固定高さなので、この値はメッセージ内容によらず安定する）
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onSizeChanged { size ->
                            if (size.height > 0) visualCardHeightPx = size.height.toFloat()
                        }
                        .shadow(8.dp, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                        .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                listOf(Color(0xFFF0F8FF), Color(0xFFD6EEFF))
                            )
                        )
                ) {
                    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)) {
                        val formattedMessage = dialogueMessage.replace("○○", playerName)
                        HomeCommentBanner(
                            formattedMessage,
                            onRefresh = onRefreshDialogue,
                            onClick = onCharacterClick
                        )

                        var homeInput by remember { mutableStateOf("") }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .shadow(2.dp, RoundedCornerShape(24.dp))
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color.White)
                                .padding(horizontal = 14.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.foundation.text.BasicTextField(
                                value = homeInput,
                                onValueChange = { homeInput = it },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = Color.Black),
                                decorationBox = { inner ->
                                    if (homeInput.isEmpty()) Text("ひかりに話しかける…", color = Color.Gray, fontSize = 14.sp)
                                    inner()
                                }
                            )
                            if (isHomeChatLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color(0xFFEC407A))
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = null,
                                    tint = if (homeInput.isNotBlank()) Color(0xFFEC407A) else Color.LightGray,
                                    modifier = Modifier.size(20.dp).clickable {
                                        if (homeInput.isNotBlank()) { onHomeChatSend(homeInput); homeInput = "" }
                                    }
                                )
                            }
                        }
                    }
                }

                // 下部ナビゲーションバー（80dp）から入力欄が隠れないための透明な余白。
                // 見た目に何もないのでキャラが重なっても問題ない
                Spacer(modifier = Modifier.height(68.dp))
            }
        }

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

        if (showWeatherSheet && weatherInfo != null && weatherInfo.hourly.isNotEmpty()) {
            HourlyWeatherSheet(weatherInfo = weatherInfo, onDismiss = { showWeatherSheet = false })
        }

        // アプリ側のシステムメッセージ（行動ポイント不足など）。キャラのセリフとは区別して表示する
        toastMessage?.let { msg ->
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF333333),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 32.dp)
            ) {
                Text(msg, color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp))
            }
        }
    }
} // ← ここで HomeScreenContent が終わる



@Composable
fun HomeTopCircleButton(icon: androidx.compose.ui.graphics.vector.ImageVector, containerColor: Color = Color.White, iconColor: Color = Color.Gray, onClick: () -> Unit = {}, size: androidx.compose.ui.unit.Dp = 30.dp) {
    Surface(shape = CircleShape, color = containerColor, modifier = Modifier
        .size(size)
        .clickable { onClick() }) {
        Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = iconColor, modifier = Modifier.size(size * 0.73f)) }
    }
}

@Composable
fun HomeTopCircleButton(iconRes: Int, containerColor: Color = Color.White, onClick: () -> Unit = {}, size: androidx.compose.ui.unit.Dp = 30.dp, showBackground: Boolean = true) {
    if (showBackground) {
        Surface(shape = CircleShape, color = containerColor, modifier = Modifier
            .size(size)
            .clickable { onClick() }) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(size * 0.73f),
                    contentScale = ContentScale.Fit
                )
            }
        }
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(size),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun HomeStepCircleGauge(steps: Int, progress: Float, modifier: Modifier = Modifier) {
    val animatedSteps by animateIntAsState(
        targetValue = steps,
        animationSpec = tween(durationMillis = 600, easing = LinearEasing),
        label = "stepCount"
    )
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
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
                val gradientTop = Color(0xFF81D4FA)
                val gradientBottom = Color(0xFF1565C0)
                drawArc(
                    color = Color.LightGray.copy(alpha = 0.2f),
                    startAngle = 0f, sweepAngle = 360f, useCenter = false,
                    style = Stroke(sw)
                )
                drawArc(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(gradientTop, gradientBottom)
                    ),
                    startAngle = -90f, sweepAngle = 360f * progress, useCenter = false,
                    style = Stroke(width = sw, cap = StrokeCap.Round)
                )
                val radius = size.minDimension / 2f
                val cx = size.width / 2f
                val cy = size.height / 2f
                val tickInner = radius - sw / 2f
                val tickOuter = radius + sw / 2f
                val ringOutlineBrush = androidx.compose.ui.graphics.Brush.verticalGradient(listOf(gradientTop, gradientBottom))
                val ringOutlineWidth = 0.6.dp.toPx()
                // 白いゲージ部分が背景と見分けにくいので、リングのつなぎ目（内側・外側の境界線）を
                // グラデーションと同じ色でめっちゃ細く囲む
                drawCircle(brush = ringOutlineBrush, radius = tickOuter, center = Offset(cx, cy), style = Stroke(width = ringOutlineWidth))
                drawCircle(brush = ringOutlineBrush, radius = tickInner, center = Offset(cx, cy), style = Stroke(width = ringOutlineWidth))
                repeat(5) { i ->
                    val angleDeg = -90f + (i + 1) * 72f
                    val rad = Math.toRadians(angleDeg.toDouble())
                    val cos = kotlin.math.cos(rad).toFloat()
                    val sin = kotlin.math.sin(rad).toFloat()
                    // 目安の線は、その位置に円弧のグラデーションがあったら何色になるかに合わせる
                    val tickY = cy + radius * sin
                    val t = (tickY / size.height).coerceIn(0f, 1f)
                    val tickColor = lerp(gradientTop, gradientBottom, t)
                    val tickOuterGap = tickOuter - 1.5.dp.toPx()
                    drawLine(
                        color = tickColor,
                        start = Offset(cx + tickInner * cos, cy + tickInner * sin),
                        end = Offset(cx + tickOuterGap * cos, cy + tickOuterGap * sin),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.AutoMirrored.Filled.DirectionsWalk, null, tint = Color(0xFFE87C9A), modifier = Modifier.size(18.dp))
                Text("今日の歩数", fontSize = 10.sp, color = Color.Gray)
                Text(String.format(java.util.Locale.US, "%,d", animatedSteps), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.DarkGray)
                Text("歩 / 5,000 歩", fontSize = 9.sp, color = Color.Gray)
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
        val tightTextStyle = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
        Row(modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = faceRes),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFE0E9)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy((-2).dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Favorite, null, tint = Color.Unspecified, modifier = Modifier.size(11.dp).gradientTint(listOf(Color(0xFFFF80AB), Color(0xFFE91E63))))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("ラブレベル", fontSize = 12.sp, color = Color(0xFFFF6B9D), fontWeight = FontWeight.Bold, fontFamily = MplusRoundedFontFamily, style = tightTextStyle)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text("Lv. $lv", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFC2185B), fontFamily = MplusRoundedFontFamily, style = tightTextStyle)
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
        val tightTextStyle = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
        Column(modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp), verticalArrangement = Arrangement.spacedBy((-2).dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Place, null, tint = Color.Unspecified, modifier = Modifier.size(13.dp).gradientTint(listOf(Color(0xFF80CBC4), Color(0xFF00695C))))
                Spacer(modifier = Modifier.width(4.dp))
                Text("行動ポイント", fontSize = 12.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontFamily = MplusRoundedFontFamily, style = tightTextStyle)
            }
            Text("$pts / 5 pt", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1B5E20), fontFamily = MplusRoundedFontFamily, style = tightTextStyle)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(5) { i ->
                    Image(
                        painter = painterResource(id = R.drawable.aikon_koudoupointo),
                        contentDescription = null,
                        modifier = Modifier
                            .size(14.dp)
                            .alpha(if (i < pts) 1f else 0.25f),
                        contentScale = ContentScale.Fit
                    )
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
    R.drawable.hikari_celebrate -> R.drawable.hikari_celebrate_face
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
                .size(130.dp)
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


fun splitMessageIntoPages(text: String): List<String> {
    val maxChars = 28
    if (text.length <= maxChars) return listOf(text)
    // 「？」の直後で改ページすると、質問と回答が不自然に分断されるため対象に含めない
    val punctuation = setOf('。', '、', '！', '…', '，')
    val result = mutableListOf<String>()
    var start = 0
    while (start < text.length) {
        val end = (start + maxChars).coerceAtMost(text.length)
        if (end == text.length) { result.add(text.substring(start)); break }
        var breakPoint = -1
        for (i in end - 1 downTo start + 1) {
            if (text[i] in punctuation) { breakPoint = i + 1; break }
        }
        if (breakPoint <= start) breakPoint = end
        result.add(text.substring(start, breakPoint))
        start = breakPoint
    }
    return result.ifEmpty { listOf(text) }
}

// バナー計測用と実描画用で必ず同一のTextStyleを使う（不一致だと2行判定なのに3行目が切り捨てられる）
// lineHeightも明示し、テーマ既定の24spを継承して2行が間延びするのを防ぐ。
// includeFontPadding=falseで、Android既定の余分な行間パディングによる高さのズレも消しておく
// （このズレがあると、固定した2行分の高さの箱から実際の描画がわずかにはみ出し下端が切れる）
val bannerTextStyle = TextStyle(
    fontSize = 13.sp,
    lineHeight = 17.sp,
    platformStyle = PlatformTextStyle(includeFontPadding = false)
)

fun splitByTextMeasure(text: String, measurer: TextMeasurer, style: TextStyle, widthPx: Int, maxLines: Int = 2): List<String> {
    // 計測誤差の安全マージン（端末フォントレンダリング差でのはみ出し防止）
    val safeWidthPx = (widthPx - 4).coerceAtLeast(1)
    val constraints = Constraints(maxWidth = safeWidthPx)
    val pages = mutableListOf<String>()
    var remaining = text.trim()
    while (remaining.isNotEmpty()) {
        val result = measurer.measure(remaining, style, constraints = constraints)
        if (result.lineCount <= maxLines) { pages.add(remaining); break }
        val cut = result.getLineEnd(maxLines - 1, visibleEnd = true)
        if (cut <= 0 || cut >= remaining.length) { pages.add(remaining); break }
        pages.add(remaining.substring(0, cut).trimEnd())
        remaining = remaining.substring(cut).trimStart()
    }
    return pages.ifEmpty { listOf(text) }
}

@Composable
fun HomeCommentBanner(message: String, onRefresh: (() -> Unit)? = null, onClick: () -> Unit = {}) {
    val textMeasurer = rememberTextMeasurer()
    // Text() は指定していないプロパティ（letterSpacing等）をLocalTextStyleから継承する。
    // 計測もそれに合わせて解決済みスタイルを使わないと、実描画とlineCountがズレる。
    val resolvedTextStyle = LocalTextStyle.current.merge(bannerTextStyle)
    var columnWidthPx by remember { mutableStateOf(0) }
    val pages = remember(message, columnWidthPx, resolvedTextStyle) {
        if (columnWidthPx > 0) splitByTextMeasure(message, textMeasurer, resolvedTextStyle, columnWidthPx)
        else splitMessageIntoPages(message)
    }
    var pageIndex by remember(message) { mutableStateOf(0) }
    val safePageIndex = pageIndex.coerceIn(0, (pages.size - 1).coerceAtLeast(0))
    val currentText = pages.getOrElse(safePageIndex) { message }
    val multiPage = pages.size > 1

    // テキスト欄は1行の時も常に2行分の高さを確保する。これによりカードの高さが
    // メッセージの行数によらず一定になり、キャラの位置固定がシンプルになる。
    val density = LocalDensity.current
    val twoLineHeightDp = with(density) { (resolvedTextStyle.lineHeight.toPx() * 2).toDp() } + 4.dp

    Box {
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 14.dp, border = BorderStroke(1.5.dp, Color(0xFFFFB7D0)), modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClick() }) {
            // IntrinsicSize.Max は二重測定パスを要求し、onSizeChangedが途中経過の幅を拾うレースを招くため使わない
            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f).padding(horizontal = 6.dp).onSizeChanged { columnWidthPx = it.width }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("ひかり", fontSize = 11.sp, color = Color(0xFFFF6B9D), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            if (multiPage) {
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "前へ",
                                    tint = if (safePageIndex > 0) Color(0xFFFF6B9D) else Color.LightGray,
                                    modifier = Modifier.size(16.dp).clickable(enabled = safePageIndex > 0) { pageIndex-- }
                                )
                                Text("${safePageIndex + 1}/${pages.size}", fontSize = 9.sp, color = Color.Gray, modifier = Modifier.padding(horizontal = 2.dp))
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "次へ",
                                    tint = if (safePageIndex < pages.size - 1) Color(0xFFFF6B9D) else Color.LightGray,
                                    modifier = Modifier.size(16.dp).clickable(enabled = safePageIndex < pages.size - 1) { pageIndex++ }
                                )
                            } else {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.LightGray)
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp, color = Color(0xFFFFB7D0).copy(alpha = 0.8f))
                        Text(
                            currentText,
                            style = resolvedTextStyle,
                            color = Color(0xFF1A1A1A),
                            maxLines = 2,
                            overflow = TextOverflow.Clip,
                            modifier = Modifier.height(twoLineHeightDp)
                        )
                    }
            }
        }
        if (onRefresh != null) {
            // ぐるぐるマークの中心がカードの角の頂点に来るように、
            // 丸ボタンを自身のサイズの半分だけ右上へオフセットする
            val refreshButtonSize = 26.dp
            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 6.dp,
                border = BorderStroke(1.dp, Color(0xFFFFB7D0)),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = refreshButtonSize / 2, y = -(refreshButtonSize / 2))
                    .size(refreshButtonSize)
                    .clickable { onRefresh() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "更新",
                        tint = Color(0xFFFF6B9D),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeWeatherBanner(weatherInfo: WeatherInfo?) {
    if (weatherInfo == null) return
    val emoji = wmoToEmoji(weatherInfo.weatherCode)
    val desc  = wmoToDescription(weatherInfo.weatherCode)
    val temp  = String.format(java.util.Locale.US, "%.0f", weatherInfo.tempC)
    var showSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(brush = Brush.verticalGradient(listOf(Color(0xFFE3F6FE), Color(0xFFB3E5FC))))
            .border(1.dp, Color(0xFF4FC3F7).copy(alpha = 0.45f), RoundedCornerShape(12.dp))
            .clickable { showSheet = true }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(emoji, fontSize = 22.sp)
            Spacer(modifier = Modifier.width(10.dp))
            val locationPrefix = weatherInfo.locationName?.let { "$it · " } ?: ""
            Text(text = "$locationPrefix$desc · $temp°C", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1565C0))
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF1565C0), modifier = Modifier.size(16.dp))
        }
    }

    if (showSheet && weatherInfo.hourly.isNotEmpty()) {
        HourlyWeatherSheet(weatherInfo = weatherInfo, onDismiss = { showSheet = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HourlyWeatherSheet(weatherInfo: WeatherInfo, onDismiss: () -> Unit) {
    val currentHour = java.time.LocalTime.now().hour
    val currentIndex = weatherInfo.hourly.indexOfFirst { it.hour == currentHour }.takeIf { it >= 0 } ?: 0
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    var selectedEntry by remember { mutableStateOf(weatherInfo.hourly.getOrNull(currentIndex)) }

    LaunchedEffect(Unit) {
        listState.animateScrollToItem(maxOf(0, currentIndex - 2))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFF5FBFF),
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 32.dp)) {
            val sheetTitle = weatherInfo.locationName?.let { "1時間ごとの天気（$it）" } ?: "1時間ごとの天気"
            Text(sheetTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1565C0), modifier = Modifier.padding(bottom = 12.dp))
            LazyRow(state = listState, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(weatherInfo.hourly) { entry ->
                    val isNow = entry.hour == currentHour
                    val isSelected = selectedEntry?.hour == entry.hour
                    val bgColor = when {
                        isNow && isSelected -> Color(0xFF0288D1)
                        isNow              -> Color(0xFF29B6F6)
                        isSelected         -> Color(0xFF90CAF9)
                        else               -> Color(0xFFE1F5FE)
                    }
                    val textColor = if (isNow || isSelected) Color.White else Color(0xFF1565C0)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(bgColor)
                            .clickable { selectedEntry = entry }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(
                            if (isNow) "今" else "${entry.hour}時",
                            fontSize = 11.sp,
                            fontWeight = if (isNow || isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = textColor
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(wmoToEmoji(entry.weatherCode), fontSize = 20.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${entry.tempC.toInt()}°",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = textColor
                        )
                    }
                }
            }

            // 時間詳細パネル
            selectedEntry?.let { entry ->
                Spacer(Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFE3F2FD),
                    border = BorderStroke(1.dp, Color(0xFF90CAF9)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            if (entry.hour == currentHour) "現在（${entry.hour}時）" else "${entry.hour}時の天気",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF1565C0)
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                            Column {
                                Text("天気", fontSize = 11.sp, color = Color(0xFF607D8B))
                                Text("${wmoToEmoji(entry.weatherCode)} ${wmoToDescription(entry.weatherCode)}", fontSize = 13.sp, color = Color(0xFF1A1A1A))
                            }
                            Column {
                                Text("気温", fontSize = 11.sp, color = Color(0xFF607D8B))
                                Text("${entry.tempC.toInt()}°C", fontSize = 13.sp, color = Color(0xFF1A1A1A))
                            }
                            Column {
                                Text("降水量", fontSize = 11.sp, color = Color(0xFF607D8B))
                                val precipText = if (entry.precipitationMm < 1.0)
                                    String.format(java.util.Locale.US, "%.1f", entry.precipitationMm)
                                else entry.precipitationMm.toInt().toString()
                                Text("${precipText}mm", fontSize = 13.sp, color = Color(0xFF1A1A1A))
                            }
                        }
                    }
                }
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
                Text("恋人モード", modifier = Modifier
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
            uiState = HomeScreenUiState(
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
                activeTimeStr = "1時間 32分",
                distanceStr = "5.6 km",
                caloriesStr = "238 kcal"
            ),
            actions = HomeScreenActions(
                onCharacterClick = {},
                onFreeChatClick = {},
                onDiaryClick = {},
                onRecordsClick = {},
                onDebugClick = {}
            )
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
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "日記の返信", fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("返信を即時生成する")
                            Text(
                                "ONにすると翌日ではなくその場で返信が届きます",
                                fontSize = 12.sp,
                                color = Color(0xFF888888)
                            )
                        }
                        Switch(
                            checked = viewModel.debugInstantDiaryReply.value,
                            onCheckedChange = { viewModel.setDebugInstantDiaryReply(it) }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("連続で日記を書ける")
                            Text(
                                "ONにすると今日の分を書いた後も、空いている過去日付に続けて作成できます",
                                fontSize = 12.sp,
                                color = Color(0xFF888888)
                            )
                        }
                        Switch(
                            checked = viewModel.debugMultiDiary.value,
                            onCheckedChange = { viewModel.setDebugMultiDiary(it) }
                        )
                    }
                }
            }
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val maxChars by viewModel.debugChatMaxChars
                    var charsInput by remember { mutableStateOf(maxChars.toString()) }
                    Text(text = "おしゃべり返答文字数", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = charsInput,
                        onValueChange = { v ->
                            charsInput = v.filter { it.isDigit() }
                            charsInput.toIntOrNull()?.let { viewModel.setDebugChatMaxChars(it) }
                        },
                        label = { Text("最大文字数") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { viewModel.setDebugChatMaxChars(30); charsInput = "30" }, modifier = Modifier.weight(1f)) { Text("30字") }
                        Button(onClick = { viewModel.setDebugChatMaxChars(100); charsInput = "100" }, modifier = Modifier.weight(1f)) { Text("100字") }
                        Button(onClick = { viewModel.setDebugChatMaxChars(150); charsInput = "150" }, modifier = Modifier.weight(1f)) { Text("150字") }
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
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 0.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 2. 上部：歩数の数字のみ（女の子・セリフ・カード・ゲージは削除）
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painterResource(R.drawable.footprints),
                        null,
                        tint = pinkAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${period.label}の歩数",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Text(
                    text = String.format(Locale.US, "%,d", totalStepsInRange),
                    fontSize = 64.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = pinkAccent,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
                // 3. 推移グラフ（残りの縦スペースを自動的に使い切る）
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    color = cardBg,
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 10.dp)) {
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
                        StepGraphPink(
                            displayData = displayData,
                            period = period,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        StatCardNew(
                            Modifier.weight(1f),
                            "消費カロリー",
                            String.format(Locale.US, "%.1f kcal", calories),
                            "おつかれさま！よく頑張ったね♪",
                            Icons.Default.Whatshot
                        )
                        StatCardNew(
                            Modifier.weight(1f),
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val timeStr = formatMillis(activeTimeMillis)
                        StatCardNew(
                            Modifier.weight(1f),
                            "歩いた時間",
                            timeStr,
                            "すごい！毎日続けようね♪",
                            Icons.Default.Schedule
                        )
                        StatCardNew(
                            Modifier.weight(1f),
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
            contentAlignment = Alignment.BottomStart,
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
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .heightIn(min = 56.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    null,
                    tint = Color(0xFFFF6B9D).copy(alpha = 0.7f),
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = label, fontSize = 11.sp, color = Color(0xFF994466), maxLines = 1, overflow = TextOverflow.Ellipsis, style = textStyle)
                    Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF7A1A3D), maxLines = 1, overflow = TextOverflow.Ellipsis, style = textStyle)
                }
            }
        }
    }
}

@Composable
fun StepGraphPink(displayData: List<AggregatedData>, period: DisplayPeriod, modifier: Modifier = Modifier.fillMaxWidth().height(180.dp)) {
    val pinkColor = Color(0xFFFF6B9D)
    if (displayData.isEmpty()) return

    val rawMaxSteps = displayData.maxOfOrNull { it.steps }?.coerceAtLeast(1) ?: 5000
    val interval = ((((rawMaxSteps / 5) + 999) / 1000) * 1000).coerceAtLeast(1000)
    val maxSteps = interval * 5

    // ★ 触れている棒のインデックスを保持する状態
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Box(modifier = modifier) { // 呼び出し側が高さを決める（親のweightで残りスペースを自動計算）
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
    val scope = rememberCoroutineScope()
    val playerName = viewModel.playerName.value.ifBlank { "あなた" }
    val loveCount = viewModel.loveCount.intValue
    val todaySteps = viewModel.todaySteps.intValue
    val today = LocalDate.now().toString()
    val context = LocalContext.current
    val diaryFontFamily = if (viewModel.userGender.value == "女性") DiaryFemaleFontFamily else DiaryMaleFontFamily

    var refreshKey by remember { mutableIntStateOf(0) }
    val allDates = remember(refreshKey) { viewModel.repository.userDiaryDates.sortedDescending() }
    val todayDiaryExists = remember(refreshKey) { viewModel.repository.getUserDiary(today).isNotBlank() }
    val allStepRecords by viewModel.allStepRecords
    val stepsByDate = remember(allStepRecords) { allStepRecords.associate { it.date to it.stepCount } }

    var showWriteDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<String?>(null) }  // 詳細表示する日付
    var writingText by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf("") }
    var selectedDiaryPhotoUri by remember { mutableStateOf<String?>(null) }
    var loadingDates by remember { mutableStateOf(setOf<String>()) }
    var checkedPendingReplies by remember { mutableStateOf(false) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        selectedDiaryPhotoUri = uri?.toString()
    }

    val instantDiaryReply = viewModel.debugInstantDiaryReply.value
    val multiDiaryDebug = viewModel.debugMultiDiary.value
    // 書き込み対象の日付。通常は今日。デバッグ連続作成ONなら空いている直近の過去日。
    var writeDate by remember { mutableStateOf(today) }

    // デバッグ連続作成用：まだ日記がない直近の日付を返す（今日→昨日→…）
    fun nextEmptyDiaryDate(): String {
        var d = LocalDate.now()
        while (viewModel.repository.getUserDiary(d.toString()).isNotBlank()) {
            d = d.minusDays(1)
        }
        return d.toString()
    }

    // 指定日の日記に対する返信を生成して保存する（過去日・即時生成の両方で使用）
    fun generateDiaryReply(date: String) {
        loadingDates = loadingDates + date
        scope.launch {
            try {
                val diaryText = viewModel.repository.getUserDiary(date)
                val photoPath = viewModel.repository.getUserDiaryPhotoPath(date)
                val encodedPhoto = photoPath.takeIf { it.isNotBlank() }?.let { encodeImageFileForGemini(it) }
                val prompt = buildDiaryReplySystemPrompt(
                    loveCount = loveCount,
                    playerName = playerName,
                    todaySteps = todaySteps,
                    diaryText = diaryText,
                    hasPhoto = encodedPhoto != null
                )
                val rawReply = callGeminiApi(
                    systemPrompt = prompt,
                    history = emptyList(),
                    userMessage = "返事をください",
                    maxTokens = 2000,
                    imageBase64 = encodedPhoto?.base64,
                    imageMimeType = encodedPhoto?.mimeType
                )
                val emotionMatch = Regex("""\[EMOTION:(\w+)\]""").find(rawReply)
                val emotionTag = emotionMatch?.groupValues?.get(1) ?: "normal"
                val cleanReply = rawReply.replace(Regex("""\[EMOTION:\w+\]"""), "").trim()
                viewModel.saveDiaryReply(date, cleanReply, emotionTag)
            } catch (_: Exception) {
                viewModel.saveDiaryReply(date, "昨日の日記、ちゃんと読んだよ。返事が遅くなってごめんね。また聞かせてください。", "normal")
            }
            loadingDates = loadingDates - date
            refreshKey++
        }
    }

    LaunchedEffect(allDates, checkedPendingReplies) {
        if (checkedPendingReplies) return@LaunchedEffect
        checkedPendingReplies = true
        val todayDate = LocalDate.now()
        val pendingDates = allDates
            .filter { date ->
                val diaryDate = runCatching { LocalDate.parse(date) }.getOrNull()
                diaryDate != null &&
                    // 即時返信ONなら今日の日記も含める。OFFなら従来通り過去日のみ。
                    (if (instantDiaryReply) !diaryDate.isAfter(todayDate) else diaryDate.isBefore(todayDate)) &&
                    viewModel.repository.getUserDiary(date).isNotBlank() &&
                    viewModel.repository.getDiaryReply(date).isBlank()
            }
            .sorted()

        pendingDates.forEach { date -> generateDiaryReply(date) }
    }

    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.nikki_haikei),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.25f)))
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("交換日記", color = pinkAccent, fontWeight = FontWeight.Bold, fontFamily = MplusRoundedFontFamily) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る", tint = pinkAccent)
                }},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            // 通常は今日の日記が未記入のときのみ。デバッグ連続作成ONなら常に表示。
            if (!todayDiaryExists || multiDiaryDebug) {
                FloatingActionButton(
                    onClick = {
                        writeDate = if (multiDiaryDebug) nextEmptyDiaryDate() else today
                        showWriteDialog = true
                    },
                    containerColor = pinkAccent,
                    contentColor = Color.White,
                    shape = CircleShape
                ) { Icon(Icons.Default.Edit, contentDescription = "日記を書く") }
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        if (allDates.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Image(painterResource(R.drawable.hikari_smile_face), null,
                        Modifier.size(72.dp).clip(CircleShape).background(Color(0xFFFFE0E9)),
                        contentScale = ContentScale.Crop)
                    Text("まだ日記がないよ", fontSize = 14.sp, color = Color(0xFF999999), fontFamily = MplusRoundedFontFamily)
                    Text("右下のボタンから書いてみてね♪", fontSize = 12.sp, color = Color(0xFFBBBBBB), fontFamily = MplusRoundedFontFamily)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 今日の日記を書いたなら「書いた」バナーを先頭に表示
                if (todayDiaryExists) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFEEF4))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("✏️", fontSize = 18.sp)
                            Text(
                                "今日の日記を書きました",
                                fontSize = 13.sp,
                                color = pinkAccent,
                                fontFamily = MplusRoundedFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                items(allDates) { date ->
                    DiaryDateCard(
                        date = date,
                        mood = viewModel.repository.getDiaryMood(date),
                        previewText = viewModel.repository.getUserDiary(date),
                        hasReply = viewModel.repository.getDiaryReply(date).isNotBlank(),
                        isLoading = loadingDates.contains(date),
                        stepCount = stepsByDate[date] ?: 0,
                        onClick = { selectedDate = date }
                    )
                }
            }
        }
    } // Scaffold
    } // outer background Box

    // 日記詳細ダイアログ
    selectedDate?.let { date ->
        DiaryDetailDialog(
            date = date,
            userText = viewModel.repository.getUserDiary(date),
            photoPath = viewModel.repository.getUserDiaryPhotoPath(date),
            mood = viewModel.repository.getDiaryMood(date),
            replyText = viewModel.repository.getDiaryReply(date),
            emotion = viewModel.repository.getDiaryReplyEmotion(date),
            isLoading = loadingDates.contains(date),
            stepCount = stepsByDate[date] ?: 0,
            diaryFontFamily = diaryFontFamily,
            onDismiss = { selectedDate = null }
        )
    }

    if (showWriteDialog) {
        Dialog(
            onDismissRequest = {
                selectedDiaryPhotoUri = null
                showWriteDialog = false
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFFFFCF6)) {
                Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = {
                            selectedDiaryPhotoUri = null
                            showWriteDialog = false
                        }) {
                            Text("キャンセル", color = Color.Gray, fontFamily = MplusRoundedFontFamily)
                        }
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(if (writeDate == today) "今日の日記" else "日記", color = pinkAccent, fontSize = 17.sp, fontWeight = FontWeight.Bold, fontFamily = MplusRoundedFontFamily)
                            Text(formatDate(LocalDate.parse(writeDate), DisplayPeriod.DAY), color = Color(0xFF999999), fontSize = 11.sp, fontFamily = MplusRoundedFontFamily)
                        }
                        IconButton(onClick = { photoPickerLauncher.launch("image/*") }) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = "写真を追加", tint = pinkAccent)
                        }
                        TextButton(
                            enabled = writingText.isNotBlank(),
                            onClick = {
                                val text = writingText.trim()
                                val photoUri = selectedDiaryPhotoUri
                                val targetDate = writeDate
                                if (text.isNotBlank()) {
                                    showWriteDialog = false
                                    viewModel.saveUserDiary(targetDate, text, selectedMood)
                                    if (photoUri != null) {
                                        // 写真を保存してから（即時返信ONなら）返信を生成する
                                        scope.launch {
                                            saveDiaryPhoto(context, targetDate, photoUri)?.let { path ->
                                                viewModel.repository.setUserDiaryPhotoPath(targetDate, path)
                                                refreshKey++
                                            }
                                            if (instantDiaryReply) generateDiaryReply(targetDate)
                                        }
                                    } else if (instantDiaryReply) {
                                        generateDiaryReply(targetDate)
                                    }
                                    writingText = ""
                                    selectedMood = ""
                                    selectedDiaryPhotoUri = null
                                    refreshKey++
                                }
                            }
                        ) {
                            Text("保存", color = if (writingText.isNotBlank()) pinkAccent else Color.LightGray, fontWeight = FontWeight.Bold, fontFamily = MplusRoundedFontFamily)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("😊 よかった", "😐 ふつう", "😩 つかれた").forEach { label ->
                            val mood = label.substringAfter(" ")
                            val selected = selectedMood == mood
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (selected) pinkAccent else Color(0xFFFFF4F8),
                                border = BorderStroke(1.dp, if (selected) pinkAccent else Color(0xFFFFB7D0)),
                                modifier = Modifier.clickable { selectedMood = mood }
                            ) {
                                Text(
                                    label,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    fontSize = 12.sp,
                                    color = if (selected) Color.White else Color(0xFF555555),
                                    fontFamily = MplusRoundedFontFamily
                                )
                            }
                        }
                    }

                    selectedDiaryPhotoUri?.let {
                        Surface(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFFFF4F8),
                            border = BorderStroke(1.dp, Color(0xFFFFB7D0))
                        ) {
                            Row(
                                modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null, tint = pinkAccent, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("写真を選択済み", color = Color(0xFF555555), fontSize = 12.sp, fontFamily = MplusRoundedFontFamily)
                                IconButton(onClick = { selectedDiaryPhotoUri = null }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "写真を外す", tint = Color(0xFFBB8888), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    // テキストの実際の行の高さ(px)。罫線間隔をこれに合わせることで
                    // カーソル(高さ=行の高さ)が罫線を突き破らないようにする。
                    var diaryLineHeightPx by remember { mutableStateOf(0f) }
                    val fallbackLineHeightPx = with(LocalDensity.current) { 26.sp.toPx() }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFFEFA))
                            .border(1.dp, Color(0xFFFFD7E5), RoundedCornerShape(8.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .drawWithContent {
                                val lineHeight = if (diaryLineHeightPx > 0f) diaryLineHeightPx else fallbackLineHeightPx
                                // 各テキスト行の下端に罫線を引き、文字が線の上に乗るようにする
                                var y = lineHeight
                                while (y <= size.height) {
                                    drawLine(
                                        color = Color(0xFFFFC7D8).copy(alpha = 0.65f),
                                        start = Offset(0f, y),
                                        end = Offset(size.width, y),
                                        strokeWidth = 1.dp.toPx()
                                    )
                                    y += lineHeight
                                }
                                drawContent()
                            }
                    ) {
                        BasicTextField(
                            value = writingText,
                            onValueChange = { if (it.length <= 300) writingText = it },
                            modifier = Modifier.fillMaxSize(),
                            textStyle = TextStyle(
                                color = Color(0xFF333333),
                                fontSize = 14.sp,
                                lineHeight = 26.sp,
                                fontFamily = diaryFontFamily,
                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                                lineHeightStyle = LineHeightStyle(
                                    alignment = LineHeightStyle.Alignment.Bottom,
                                    trim = LineHeightStyle.Trim.None
                                )
                            ),
                            onTextLayout = { result ->
                                if (result.lineCount > 0) {
                                    diaryLineHeightPx = result.getLineBottom(0) - result.getLineTop(0)
                                }
                            },
                            decorationBox = { innerTextField ->
                                if (writingText.isEmpty()) {
                                    Text(
                                        "今日あったことをここに書いてね…",
                                        color = Color(0xFFBBBBBB),
                                        fontSize = 14.sp,
                                        lineHeight = 26.sp,
                                        fontFamily = diaryFontFamily,
                                        style = TextStyle(
                                            platformStyle = PlatformTextStyle(includeFontPadding = false),
                                            lineHeightStyle = LineHeightStyle(
                                                alignment = LineHeightStyle.Alignment.Bottom,
                                                trim = LineHeightStyle.Trim.None
                                            )
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        )
                        Text(
                            "${writingText.length}/300",
                            modifier = Modifier.align(Alignment.BottomEnd).background(Color(0xCCFFFFFA)).padding(start = 6.dp, top = 2.dp),
                            color = Color(0xFFBBBBBB),
                            fontSize = 11.sp,
                            fontFamily = MplusRoundedFontFamily
                        )
                    }
                }
            }
        }
    }
}

// 一覧用コンパクトカード（タップで詳細を開く）
@Composable
fun DiaryDateCard(
    date: String,
    mood: String,
    previewText: String,
    hasReply: Boolean,
    isLoading: Boolean,
    stepCount: Int = 0,
    onClick: () -> Unit
) {
    val pinkAccent = Color(0xFFFF6B9D)
    val parts = date.split("-")
    val formattedDate = if (parts.size == 3)
        "${parts[1].toIntOrNull() ?: parts[1]}月${parts[2].toIntOrNull() ?: parts[2]}日"
    else date
    val moodEmoji = when (mood) {
        "よかった" -> "😊"
        "ふつう"   -> "😐"
        "つかれた" -> "😩"
        else       -> "📖"
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        shadowElevation = 3.dp,
        border = BorderStroke(1.dp, Color(0xFFFFB7D0).copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 左：日付ブロック
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(52.dp)) {
                Text(moodEmoji, fontSize = 20.sp)
                Text(formattedDate, fontSize = 10.sp, color = Color(0xFFAAAAAA), fontFamily = MplusRoundedFontFamily)
                if (stepCount > 0) Text("${"%,d".format(stepCount)}歩", fontSize = 9.sp, color = Color(0xFFBBBBBB), fontFamily = MplusRoundedFontFamily)
            }
            // 中：本文プレビュー
            Text(
                text = previewText.take(40).let { if (previewText.length > 40) "$it…" else it },
                modifier = Modifier.weight(1f),
                fontSize = 13.sp,
                color = Color(0xFF444444),
                fontFamily = MplusRoundedFontFamily,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            // 右：返信ステータス
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.size(16.dp), color = pinkAccent, strokeWidth = 2.dp)
                hasReply  -> Text("💌", fontSize = 16.sp)
                else      -> Text("📬", fontSize = 16.sp)
            }
        }
    }
}

private class PeelShape(private val progress: Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        if (progress >= 1f) return Outline.Generic(Path())
        val w = size.width; val h = size.height
        val path = Path().apply {
            moveTo(0f, 0f); lineTo(w, 0f)
            lineTo(w, h * (1f - progress))
            lineTo(w * (1f - progress), h)
            lineTo(0f, h); close()
        }
        return Outline.Generic(path)
    }
}

// 詳細表示ダイアログ（全文＋ひかりの返信）
@Composable
fun DiaryDetailDialog(
    date: String,
    userText: String,
    photoPath: String,
    mood: String,
    replyText: String,
    emotion: String,
    isLoading: Boolean,
    stepCount: Int = 0,
    diaryFontFamily: FontFamily,
    onDismiss: () -> Unit
) {
    val pinkAccent = Color(0xFFFF6B9D)
    val parts = date.split("-")
    val formattedDate = if (parts.size == 3)
        "${parts[0]}年${parts[1].toIntOrNull() ?: parts[1]}月${parts[2].toIntOrNull() ?: parts[2]}日"
    else date
    val hikariExprRes = when (emotion) {
        "happy", "surprise" -> R.drawable.hikari_celebrate
        "love", "shy"       -> R.drawable.hikari_blush
        else                -> R.drawable.hikari_smile
    }
    val moodEmoji = when (mood) {
        "よかった" -> "😊 よかった"
        "ふつう"   -> "😐 ふつう"
        "つかれた" -> "😩 つかれた"
        else       -> ""
    }

    var showReply by remember { mutableStateOf(false) }
    // 0° = 日記ページ表示、180° = ひかりページ表示
    val flipRotation by animateFloatAsState(
        targetValue = if (showReply) 180f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "cardFlip"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val lineSpPx = with(LocalDensity.current) { 26.sp.toPx() }
        val lineColor = Color(0xFFFFC7D8).copy(alpha = 0.60f)

        Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFFFFCF6)) {
            Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                // ヘッダー
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { if (showReply) showReply = false else onDismiss() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る", tint = pinkAccent)
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(formattedDate, color = pinkAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = MplusRoundedFontFamily)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (moodEmoji.isNotBlank()) Text(moodEmoji, fontSize = 11.sp, color = Color(0xFF999999), fontFamily = MplusRoundedFontFamily)
                            if (stepCount > 0) Text("🚶 ${"%,d".format(stepCount)}歩", fontSize = 11.sp, color = Color(0xFF999999), fontFamily = MplusRoundedFontFamily)
                        }
                    }
                    Spacer(Modifier.width(48.dp))
                }
                HorizontalDivider(color = Color(0xFFFFD7E5), thickness = 0.5.dp)

                // ── 3D カードフリップ ──
                Box(modifier = Modifier.fillMaxSize()) {

                    // ── 裏ページ（ひかりの返信）flipRotation 90°〜180° で表側に出てくる ──
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                rotationY = flipRotation - 180f   // -180°→0° へ
                                cameraDistance = 10f * density
                                alpha = if (flipRotation <= 90f) 0f else 1f
                            }
                            .drawBehind {
                                drawRect(Color(0xFFFFF8FC))
                                var y = lineSpPx
                                while (y <= size.height) {
                                    drawLine(lineColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                                    y += lineSpPx
                                }
                            }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = expressionToFaceRes(hikariExprRes)),
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFFFE0E9)),
                                    contentScale = ContentScale.Crop
                                )
                                Text("ひかりより", fontSize = 14.sp, color = pinkAccent, fontWeight = FontWeight.Bold, fontFamily = MplusRoundedFontFamily)
                            }
                            Text(
                                replyText,
                                modifier = Modifier.padding(horizontal = 20.dp),
                                fontSize = 14.sp,
                                color = Color(0xFF333333),
                                fontFamily = DiaryFemaleFontFamily,
                                lineHeight = 26.sp,
                                style = androidx.compose.ui.text.TextStyle(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                    lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Bottom, LineHeightStyle.Trim.None)
                                )
                            )
                            Spacer(Modifier.height(32.dp))
                        }
                    }

                    // ── 表ページ（日記）flipRotation 0°〜90° で見える、それ以降は裏に回る ──
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                rotationY = flipRotation          // 0°→180° へ
                                cameraDistance = 10f * density
                                alpha = if (flipRotation >= 90f) 0f else 1f
                            }
                            .drawBehind {
                                drawRect(Color(0xFFFFFCF6))
                                var y = lineSpPx
                                while (y <= size.height) {
                                    drawLine(lineColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                                    y += lineSpPx
                                }
                            }
                    ) {
                        Column(Modifier.fillMaxSize()) {
                            // ── ボタンを上部に固定 ──
                            when {
                                isLoading -> Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = pinkAccent, strokeWidth = 2.dp)
                                    Spacer(Modifier.width(8.dp))
                                    Text("ひかりがお返事を書いています…", fontSize = 12.sp, color = Color(0xFFAAAAAA), fontFamily = MplusRoundedFontFamily)
                                }
                                replyText.isNotBlank() -> Button(
                                    onClick = { showReply = true },
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B9D))
                                ) {
                                    Text("💌 ひかりからのお手紙を見る", fontFamily = MplusRoundedFontFamily)
                                }
                                else -> Text(
                                    "ひかりからの返事は、明日手紙で届きます。",
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                    fontSize = 13.sp, color = Color(0xFFAAAAAA), fontFamily = MplusRoundedFontFamily
                                )
                            }
                            HorizontalDivider(color = Color(0xFFFFD7E5), thickness = 0.5.dp)
                            // ── 日記本文（スクロール可） ──
                            Column(
                                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    userText,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                                    fontSize = 14.sp,
                                    color = Color(0xFF333333),
                                    fontFamily = diaryFontFamily,
                                    lineHeight = 26.sp,
                                    style = androidx.compose.ui.text.TextStyle(
                                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                                        lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Bottom, LineHeightStyle.Trim.None)
                                    )
                                )
                                if (photoPath.isNotBlank()) {
                                    val bitmap = remember(photoPath) { BitmapFactory.decodeFile(photoPath)?.asImageBitmap() }
                                    bitmap?.let {
                                        Image(
                                            bitmap = it,
                                            contentDescription = "日記の写真",
                                            modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp)
                                                .padding(horizontal = 20.dp).clip(RoundedCornerShape(12.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                                Spacer(Modifier.height(32.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DiaryEntryCard(
    date: String,
    userText: String,
    photoPath: String = "",
    replyText: String,
    emotion: String,
    isLoading: Boolean,
    loveCount: Int,
    diaryFontFamily: FontFamily = MplusRoundedFontFamily
) {
    val pinkAccent = Color(0xFFFF6B9D)
    val parts = date.split("-")
    val formattedDate = if (parts.size == 3)
        "${parts[0]}年${parts[1].toIntOrNull() ?: parts[1]}月${parts[2].toIntOrNull() ?: parts[2]}日"
    else date
    val hikariExprRes = when (emotion) {
        "happy", "surprise" -> R.drawable.hikari_celebrate
        "love", "shy"       -> R.drawable.hikari_blush
        else                -> R.drawable.hikari_smile
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, Color(0xFFFFB7D0).copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(formattedDate, fontSize = 11.sp, color = Color(0xFFAAAAAA), fontFamily = MplusRoundedFontFamily)
            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = pinkAccent.copy(alpha = 0.7f), modifier = Modifier.size(14.dp).padding(top = 2.dp))
                Spacer(Modifier.width(6.dp))
                Text(userText, fontSize = 15.sp, color = Color(0xFF333333), fontFamily = diaryFontFamily, lineHeight = 22.sp)
            }

            if (photoPath.isNotBlank()) {
                val diaryBitmap = remember(photoPath) {
                    BitmapFactory.decodeFile(photoPath)?.asImageBitmap()
                }
                diaryBitmap?.let { image ->
                    Spacer(Modifier.height(10.dp))
                    Image(
                        bitmap = image,
                        contentDescription = "日記の写真",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 220.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFF4F8)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.weight(1f).height(0.5.dp).background(Color(0xFFFFB7D0).copy(alpha = 0.6f)))
                Text("ひかりより", modifier = Modifier.padding(horizontal = 8.dp),
                    fontSize = 10.sp, color = pinkAccent,
                    fontFamily = MplusRoundedFontFamily, fontWeight = FontWeight.Bold)
                Box(Modifier.weight(1f).height(0.5.dp).background(Color(0xFFFFB7D0).copy(alpha = 0.6f)))
            }

            if (isLoading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = pinkAccent, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("ひかりが返事を書いています…", fontSize = 12.sp, color = Color(0xFFAAAAAA), fontFamily = MplusRoundedFontFamily)
                }
            } else if (replyText.isNotBlank()) {
                Row(verticalAlignment = Alignment.Top) {
                    Image(
                        painter = painterResource(id = expressionToFaceRes(hikariExprRes)),
                        contentDescription = null,
                        modifier = Modifier.size(34.dp).clip(CircleShape).background(Color(0xFFFFE0E9)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(replyText, fontSize = 15.sp, color = Color(0xFF333333), fontFamily = DiaryFemaleFontFamily, lineHeight = 22.sp)
                }
            } else {
                Text(
                    "ひかりからの返事は、明日手紙で届きます。",
                    fontSize = 12.sp,
                    color = Color(0xFFAAAAAA),
                    fontFamily = MplusRoundedFontFamily
                )
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
    val tabTitles = listOf("プロフィール", "サウンド", "プレミアム")
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("プロフィール設定", color = pinkAccent, fontWeight = FontWeight.Bold) },
                    navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る", tint = pinkAccent) } }
                )
                TabRow(selectedTabIndex = selectedTab, contentColor = pinkAccent) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
        ) {
            Column(modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                when (selectedTab) {
                    0 -> {
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
                    }
                    1 -> {
                        Text("BGM", fontWeight = FontWeight.Bold, color = pinkAccent, modifier = Modifier.fillMaxWidth())
                        val selectedBgmId by viewModel.selectedBgmId
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().clickable { viewModel.setSelectedBgm("") }
                            ) {
                                RadioButton(selected = selectedBgmId.isEmpty(), onClick = { viewModel.setSelectedBgm("") })
                                Text("オフ")
                            }
                            bgmTracks.forEach { track ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().clickable { viewModel.setSelectedBgm(track.id) }
                                ) {
                                    RadioButton(selected = selectedBgmId == track.id, onClick = { viewModel.setSelectedBgm(track.id) })
                                    Text(track.name)
                                }
                            }
                        }
                        val bgmVolume by viewModel.bgmVolume
                        Text("音量", fontWeight = FontWeight.Bold, color = pinkAccent, modifier = Modifier.fillMaxWidth())
                        Slider(
                            value = bgmVolume,
                            onValueChange = { viewModel.setBgmVolume(it) },
                            valueRange = 0f..1f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    2 -> {
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
                    }
                }
            }
            Button(onClick = {
                val h = tempHeight.toFloatOrNull() ?: 170f
                val w = tempWeight.toFloatOrNull() ?: 60f
                viewModel.setPlayerName(tempName)
                viewModel.setUserProfile(h, w)
                viewModel.saveProfile(h, tempGender)
                if (isPremium) viewModel.saveCustomCharacterItems(customItems)
                viewModel.saveLifestyleProfile(tempLifestyle, tempFavoriteDrink, tempWeakness, tempBodyNotes)
                navController.popBackStack()
            }, modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp), colors = ButtonDefaults.buttonColors(containerColor = pinkAccent)) { Text("保存して戻る") }
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

data class HourlyWeatherEntry(val hour: Int, val tempC: Double, val weatherCode: Int, val precipitationMm: Double = 0.0)
data class WeatherInfo(val tempC: Double, val weatherCode: Int, val hourly: List<HourlyWeatherEntry> = emptyList(), val locationName: String? = null)

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

fun homeWeatherDialogue(code: Int): TouchDialogue = when (code) {
    0, 1 -> TouchDialogue("今日は晴れていますね！絶好のお散歩日和ですよ♪", R.drawable.hikari_smile)
    2, 3, 45, 48 -> TouchDialogue("曇っていますが歩きやすい気温です！", R.drawable.hikari_smile)
    51, 53, 55, 61, 63, 65, 80, 81, 82 -> TouchDialogue("雨ですね…傘は持ちましたか？それでも一緒に歩きましょう！", R.drawable.hikari_think)
    71, 73, 75, 77, 85, 86 -> TouchDialogue("雪ですよ！テンション上がりますね！転ばないでくださいね！", R.drawable.hikari_celebrate)
    95, 96, 99 -> TouchDialogue("今日は無理しないでくださいね…室内で運動してもいいですよ！", R.drawable.hikari_think)
    else -> TouchDialogue("曇っていますが歩きやすい気温です！", R.drawable.hikari_smile)
}

suspend fun fetchWeather(lat: Double, lon: Double): WeatherInfo? =
    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val url = URL("https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,weather_code&hourly=temperature_2m,weather_code,precipitation&timezone=auto&forecast_days=1")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            val json = JSONObject(conn.inputStream.bufferedReader().readText())
            val cur = json.getJSONObject("current")
            val hourlyJson = json.getJSONObject("hourly")
            val times = hourlyJson.getJSONArray("time")
            val temps = hourlyJson.getJSONArray("temperature_2m")
            val codes = hourlyJson.getJSONArray("weather_code")
            val precips = hourlyJson.optJSONArray("precipitation")
            val hourlyList = (0 until times.length()).mapNotNull { i ->
                val h = times.getString(i).substringAfter("T").substringBefore(":").toIntOrNull() ?: return@mapNotNull null
                val p = precips?.optDouble(i, 0.0) ?: 0.0
                HourlyWeatherEntry(h, temps.getDouble(i), codes.getInt(i), p)
            }
            WeatherInfo(tempC = cur.getDouble("temperature_2m"), weatherCode = cur.getInt("weather_code"), hourly = hourlyList)
        } catch (e: Exception) { null }
    }

@Suppress("DEPRECATION")
fun reverseGeocodeLocationName(context: android.content.Context, lat: Double, lon: Double): String? =
    try {
        val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
        val addresses = geocoder.getFromLocation(lat, lon, 1)
        val address = addresses?.firstOrNull()
        address?.locality ?: address?.subAdminArea ?: address?.adminArea
    } catch (e: Exception) {
        null
    }

@SuppressLint("MissingPermission")
suspend fun fetchWeatherFromLocation(context: android.content.Context): WeatherInfo? =
    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val lm = context.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
            val loc = lm.getProviders(true)
                .mapNotNull { lm.getLastKnownLocation(it) }
                .maxByOrNull { it.time }
            loc?.let {
                val weather = fetchWeather(it.latitude, it.longitude)
                val locationName = reverseGeocodeLocationName(context, it.latitude, it.longitude)
                weather?.copy(locationName = locationName)
            }
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

// ---- 衣装（ショップ・衣装変更）----

data class Costume(
    val id: String,
    val name: String,
    val price: Int,
    val smileRes: Int,
    val blushRes: Int,
    val celebrateRes: Int,
    val thinkRes: Int
)

val costumeCatalog = listOf(
    Costume("default",   "私服",         0,  R.drawable.hikari_smile, R.drawable.hikari_blush, R.drawable.hikari_celebrate, R.drawable.hikari_think),
    Costume("barikyari",  "バリキャリ",    10, R.drawable.hikari_barikyari_smile, R.drawable.hikari_barikyari_blush, R.drawable.hikari_barikyari_celebrate, R.drawable.hikari_bairikyari_think),
    Costume("boisyoutu",  "ボーイッシュ",  8,  R.drawable.hikari_boisyoutu_smile, R.drawable.hikari_boisyoutu_blush, R.drawable.hikari_boisyoutu_celebrate, R.drawable.hikari_boisyoutu_think),
    Costume("punks",      "パンクス",      15, R.drawable.hikari_punks_smile, R.drawable.hikari_punks_blush, R.drawable.hikari_punks_celebrate, R.drawable.hikari_punks_think),
    Costume("mizugi",     "水着",         12, R.drawable.hikari_mizugi_smile, R.drawable.hikari_mizugi_blush, R.drawable.hikari_mizugi_celebrate, R.drawable.hikari_mizugi_think),
    Costume("santa",      "サンタ",       15, R.drawable.hikari_santa_smile, R.drawable.hikari_santa_blush, R.drawable.hikari_santa_cerebrate, R.drawable.hikari_santa_think),
    Costume("epuron",     "エプロン",      10, R.drawable.hikari_epuron_smile, R.drawable.hikari_epuron_blush, R.drawable.hikari_epuron_celebrate, R.drawable.hikari_epuron_think)
)

data class BgmTrack(val id: String, val name: String, val resId: Int)

val bgmTracks = listOf(
    BgmTrack("hitujinokodou", "羊の鼓動", R.raw.hitujinokodou),
    BgmTrack("koibitoninattahi", "恋人になった日", R.raw.koibitoninattahi),
    BgmTrack("ohisamanosentakusi", "おひさまの洗濯師", R.raw.ohisamanosentakusi)
)

// ホーム画面のベース表情drawableを、現在装備中の衣装の対応する表情に置き換える
fun costumedExpressionRes(baseRes: Int, costumeId: String): Int {
    val costume = costumeCatalog.find { it.id == costumeId } ?: return baseRes
    return when (baseRes) {
        R.drawable.hikari_smile -> costume.smileRes
        R.drawable.hikari_blush -> costume.blushRes
        R.drawable.hikari_celebrate -> costume.celebrateRes
        R.drawable.hikari_think -> costume.thinkRes
        else -> baseRes
    }
}

// ---- おもいで（コレクション）----

data class MemoryItem(
    val id: String,
    val name: String,
    val requiredLoveLevel: Int,
    val imageRes: Int
)

val memoryItems = listOf(
    MemoryItem("doubutuen",      "動物園",           1,  R.drawable.osyaberi_basyo_doubutuen),
    MemoryItem("suizokukan",     "水族館",           2,  R.drawable.osyaberi_basyo_suizokukan),
    MemoryItem("biiti",          "ビーチ",           3,  R.drawable.osyaberi_basyo_biiti),
    MemoryItem("puuru",          "プール",           4,  R.drawable.osyaberi_basyo_puuru),
    MemoryItem("umi",            "海",               5,  R.drawable.osyaberi_basyo_umi),
    MemoryItem("hanabi",         "花火大会",         6,  R.drawable.osyaberi_basyo_hanabi),
    MemoryItem("kurisumasuturi", "クリスマス",       7,  R.drawable.osyaberi_basyo_kurisumasuturi),
    MemoryItem("onsen",          "温泉",             9,  R.drawable.osyaberi_basyo_onsen),
    MemoryItem("ryokan",         "旅館",             9,  R.drawable.osyaberi_basyo_ryokan),
)

// ---- AI チャット共通 ----

data class ChatMessage(val role: String, val content: String, val expressionRes: Int? = null, val exprName: String? = null, val actionText: String? = null, val basyoId: String? = null)

data class StepDialogue(val thresholdSteps: Int, val text: String, val expr: Int)
data class TouchDialogue(val text: String, val expr: Int)

val touchDialoguesLv5 = listOf(
    TouchDialogue("○○さんって、なんでこんないい感じなんですか？（本当に聞いてます）", R.drawable.hikari_blush),
    TouchDialogue("今日どのあたり歩いてますか？なんか気になります。", R.drawable.hikari_smile),
    TouchDialogue("最近何か面白いことありましたか？話してほしいです♪", R.drawable.hikari_smile)
)

val touchDialoguesLv7 = listOf(
    TouchDialogue("○○さんのそばにいると、落ち着きますよ…最近どんな感じですか？", R.drawable.hikari_blush),
    TouchDialogue("ずっと隣にいてほしいです。…なんちゃって！何か話しましょうか？", R.drawable.hikari_blush),
    TouchDialogue("○○さんって、ちょっとズルいですよ。こんなに気になってしまって。今日何してましたか？", R.drawable.hikari_blush)
)

val touchDialoguesLv9 = listOf(
    TouchDialogue("○○さんの声を聞くと、ほっとしますよ。", R.drawable.hikari_blush),
    TouchDialogue("こうして一緒にいられる時間が、いちばん好きです。", R.drawable.hikari_blush),
    TouchDialogue("○○さんのこと、いつも考えてしまうんですよ…", R.drawable.hikari_blush)
)

val touchDialoguesLv10 = listOf(
    TouchDialogue("○○さんのこと、大好きですよ。", R.drawable.hikari_blush),
    TouchDialogue("ずっと一緒にいたいですよ、○○さんと。", R.drawable.hikari_blush),
    TouchDialogue("○○さんといると、世界が明るく見える気がします。", R.drawable.hikari_smile)
)

val stepDialoguesLv1 = listOf(
    StepDialogue(0,     "今日も来ましたね！今日はどのあたりを歩く予定ですか？", R.drawable.hikari_smile),
    StepDialogue(1000,  "1000歩！靴がようやく本気を出してきましたよ。調子はどうですか？", R.drawable.hikari_smile),
    StepDialogue(3000,  "3000歩！いい感じですよ。今どのあたりにいますか？", R.drawable.hikari_celebrate),
    StepDialogue(5000,  "5000歩！足が「もういい？」って言ってますが無視していいですよ。今日の気分は？", R.drawable.hikari_smile),
    StepDialogue(8000,  "8000歩！体重計もびっくりしてると思いますよ。何か話しかけてみてください♪", R.drawable.hikari_blush),
    StepDialogue(10000, "1万歩！靴もここまで来るとは思っていなかったと思います♪ 今日どんな感じでしたか？", R.drawable.hikari_celebrate),
    StepDialogue(20000, "2万歩！今日のモードが違いますよ。何かあったんですか？", R.drawable.hikari_celebrate),
    StepDialogue(30000, "もはや伝説ですよ…今日一日どんな感じでしたか？", R.drawable.hikari_smile)
)

val stepDialoguesLv3 = listOf(
    StepDialogue(0,     "○○さん！今日はどんな一日になりそうですか？", R.drawable.hikari_smile),
    StepDialogue(1000,  "1000歩！体は温まってきましたか？今日どのあたりを歩いてますか？", R.drawable.hikari_smile),
    StepDialogue(3000,  "3000歩！体が温まってきた感じがしますよね。今日の空気はどうですか？", R.drawable.hikari_smile),
    StepDialogue(5000,  "5000歩！体重計に「よくやった」って言わせましょう！何か話しかけてみてください♪", R.drawable.hikari_blush),
    StepDialogue(8000,  "8000歩！体より先に根性が決めた感じがします。最近調子どうですか？", R.drawable.hikari_blush),
    StepDialogue(10000, "1万歩！達成！○○さん、今日は何か話したいことありますか？", R.drawable.hikari_celebrate),
    StepDialogue(20000, "2万歩！？今日のモードが違いますよ○○さん。何かあったんですか？", R.drawable.hikari_celebrate),
    StepDialogue(30000, "もはや伝説ですよ…今日どんな一日でしたか？", R.drawable.hikari_smile)
)

val stepDialoguesLv5 = listOf(
    StepDialogue(0,     "○○さん！今日はどんな感じですか？何か話しかけてください♡", R.drawable.hikari_smile),
    StepDialogue(1000,  "1000歩！○○さんのペース、もう覚えてしまいましたよ。今日の気分はどうですか？", R.drawable.hikari_smile),
    StepDialogue(3000,  "3000歩！○○さんと歩くと時間の流れが変わる気がします。今どのあたりですか？", R.drawable.hikari_blush),
    StepDialogue(5000,  "5000歩…○○さんって歩き始めたら止まらないタイプですよね。何か話しましょうか？", R.drawable.hikari_blush),
    StepDialogue(8000,  "8000歩！体より先に気持ちが動いた感じがします♪ 最近どうですか？", R.drawable.hikari_celebrate),
    StepDialogue(10000, "1万歩！今日の○○さん、なんか輝いて見えますよ。何かいいことありましたか？", R.drawable.hikari_blush),
    StepDialogue(20000, "2万歩！○○さんのこと、ちょっと心配になってきました（褒め言葉です）今日何かありましたか？", R.drawable.hikari_celebrate),
    StepDialogue(30000, "3万歩…！○○さんのこと、もっと知りたくなってしまいます。今日どんな話しましょうか？", R.drawable.hikari_blush)
)

val stepDialoguesLv7 = listOf(
    StepDialogue(0,     "○○さんの顔が見られてよかった♡ 今日はどんな一日になりそうですか？", R.drawable.hikari_blush),
    StepDialogue(1000,  "1000歩！○○さんと歩く時間は特別な気がしますよ。今日何か話しませんか？", R.drawable.hikari_blush),
    StepDialogue(3000,  "3000歩…ずっとこのまま歩いていたいですよ。今どんな気持ちですか？", R.drawable.hikari_blush),
    StepDialogue(5000,  "5000歩…ずっと、○○さんとこうして歩いていたいですよ。今日の調子はどうですか？", R.drawable.hikari_blush),
    StepDialogue(8000,  "8000歩！○○さんのこと、ずっと応援していますよ♡ 何か話しかけてください。", R.drawable.hikari_smile),
    StepDialogue(10000, "1万歩！○○さんといたら、どこまでだって歩いていけそうです。今日はどんな話をしましょうか？", R.drawable.hikari_celebrate),
    StepDialogue(20000, "2万歩…！○○さんの頑張り、全部そばで見ていたいです。何かありましたか？", R.drawable.hikari_blush),
    StepDialogue(30000, "3万歩…！大好きです。今日どんなことを考えながら歩いてましたか？", R.drawable.hikari_celebrate)
)

val stepDialoguesLv9 = stepDialoguesLv7

fun homeStepDialogue(todaySteps: Int, loveCount: Int): StepDialogue {
    val list = when {
        loveCount >= 9 -> stepDialoguesLv9
        loveCount >= 7 -> stepDialoguesLv7
        loveCount >= 5 -> stepDialoguesLv5
        loveCount >= 3 -> stepDialoguesLv3
        else           -> stepDialoguesLv1
    }
    return list.lastOrNull { todaySteps >= it.thresholdSteps } ?: list.first()
}

// 待機中（歩数の節目に達していない時）に表示するバリエーション豊かな話しかけセリフ。
// 「今日出したものは今日はもう出さない」の管理対象になる（getTodayShownDefaultDialogues）。
val homeDefaultDialoguesLv1 = listOf(
    TouchDialogue("ふぁぁ……今日はちょっと眠たいです。あったかい紅茶でも飲んで、ゆっくりしたい気分なんです♪", R.drawable.hikari_think),
    TouchDialogue("そういえば、○○さん。友達と好きなタイプの話になったんです。○○さんはどんな女性に惹かれることが多いんですか？", R.drawable.hikari_blush),
    TouchDialogue("あのですね、○○さん。私の第一印象ってどんな感じでしたか？少し気になっちゃいました♪", R.drawable.hikari_blush),
    TouchDialogue("ふと思ったんですけど、○○さん。私ってしっかりしてるほうに見えますか？それとも少し抜けてるほうですか？", R.drawable.hikari_smile),
    TouchDialogue("ねぇ、○○さん。今日はかわいいマグカップを見つけたんです！見ているだけでも楽しくなっちゃいました♪", R.drawable.hikari_celebrate),
    TouchDialogue("私、こういう日は甘いものが食べたくなるんです。○○さんは最近、つい食べたくなるものってありますか？", R.drawable.hikari_smile),
    TouchDialogue("○○さん、最近ちゃんと休めていますか？無理だけはしないでくださいね♪", R.drawable.hikari_smile),
    TouchDialogue("そういえば、○○さん。最近ですね、夜に少しだけお散歩するのが好きなんです。静かな時間って落ち着きますよね♪", R.drawable.hikari_smile),
    TouchDialogue("あっ、今日かわいい鳥を見つけたんです！思わず足を止めて見ちゃいました♪", R.drawable.hikari_celebrate),
    TouchDialogue("そういえば、最近はゆったりした音楽を聴くことが増えたんです。○○さんはどんな音楽が好きなんですか？", R.drawable.hikari_smile),
    TouchDialogue("あれれ……買うものをちゃんと覚えていたはずなのに、一番大事なものだけ忘れちゃいました♪", R.drawable.hikari_think),
    TouchDialogue("ねぇ、○○さん。お休みの日って何をして過ごすことが多いんですか？私はお散歩したくなることが多いんです♪", R.drawable.hikari_smile),
    TouchDialogue("いつか、いろんな景色を見に行ってみたいんです。○○さんにも、お気に入りの場所ってありますか？", R.drawable.hikari_smile),
    TouchDialogue("ねぇ、○○さん。私にはかわいい雰囲気と大人っぽい雰囲気、どっちが似合うと思いますか？", R.drawable.hikari_blush),
    TouchDialogue("実は今日、友達と『幸せって何だろう』って話をしていたんです。○○さんなら何て答えますか？", R.drawable.hikari_smile),
    TouchDialogue("実は、私って文房具売り場が大好きなんです。気づいたら、つい長居しちゃうんですよ♪", R.drawable.hikari_blush),
    TouchDialogue("聞いてください、○○さん！今日は焼きたてのパンの香りにつられて、お店に入っちゃいました♪", R.drawable.hikari_celebrate),
    TouchDialogue("ふと思ったんですけど、○○さんは一人で過ごす時間と、誰かと過ごす時間ならどっちが好きですか？", R.drawable.hikari_smile),
    TouchDialogue("なんとなくですけど、○○さんって読書が好きそうな気がするんです。当たっていますか？", R.drawable.hikari_smile),
    TouchDialogue("相談してもいいですか、○○さん。今度マグカップを買おうと思うんですけど、シンプルなデザインとかわいいデザインならどっちがおすすめですか？", R.drawable.hikari_smile),
    TouchDialogue("そういえば、最近はピアノの曲を流しながら過ごすことが増えたんです。○○さんはどんな音楽をよく聴くんですか？", R.drawable.hikari_smile),
    TouchDialogue("実は最近、ミステリー小説にハマってるんです。続きが気になって、つい夜更かししちゃいました♪", R.drawable.hikari_blush),
    TouchDialogue("あっ、○○さん。今日はかわいい雑貨屋さんを見つけたんです！見ているだけでも楽しくて、時間を忘れちゃいました♪", R.drawable.hikari_celebrate),
    TouchDialogue("ふと思ったんですけど、○○さんから見て、私って話しかけやすそうですか？", R.drawable.hikari_blush),
    TouchDialogue("なんとなくですけど、○○さんって甘いものより、しょっぱいものが好きそうな気がするんです。どうでしょう？", R.drawable.hikari_smile),
    TouchDialogue("あのですね、○○さん。今度映画を観ようと思うんですけど、笑える作品と感動する作品ならどっちがおすすめですか？", R.drawable.hikari_smile),
    TouchDialogue("そういえば、子どもの頃にシャボン玉で遊んだことを思い出したんです。○○さんはどんな遊びが好きでしたか？", R.drawable.hikari_smile),
    TouchDialogue("実は、紅茶の香りを楽しみながらのんびり過ごす時間が好きなんです。○○さんは好きな飲み物ってありますか？", R.drawable.hikari_smile),
    TouchDialogue("聞いてください……。今日は買い物に行ったのに、一番買いたかったハンドクリームだけ忘れて帰ってきちゃいました♪", R.drawable.hikari_think)
)

val homeDefaultDialoguesLv3 = listOf(
    TouchDialogue("そういえば、最近カフェでのんびり本を読むのに憧れてるんです。○○さんは一人でゆっくり過ごす時間って好きですか？", R.drawable.hikari_smile),
    TouchDialogue("ふぁぁ……今日は甘いものが食べたい気分です。○○さんは疲れた日って、何が食べたくなりますか？", R.drawable.hikari_think),
    TouchDialogue("実は私、かわいい文房具を見るとつい買っちゃうんです。○○さんにも、つい集めちゃうものってありますか？", R.drawable.hikari_blush),
    TouchDialogue("ふと思ったんですけど、一日自由に過ごせるなら何をしたいですか？私は景色のきれいな場所を歩いてみたいです♪", R.drawable.hikari_smile),
    TouchDialogue("うーん……部屋に飾るなら、お花と観葉植物ならどっちがいいと思いますか？まだ決められなくて迷ってるんです♪", R.drawable.hikari_smile),
    TouchDialogue("ねぇ、○○さん。私って初めて会った頃と比べて、少しは話しやすくなりましたか？", R.drawable.hikari_blush),
    TouchDialogue("なんとなくですけど、○○さんって約束はきちんと守るタイプな気がするんです。当たってますか？", R.drawable.hikari_smile),
    TouchDialogue("最近、心が温かくなる映画を観たい気分なんです。○○さんなら、笑える作品と感動する作品、どっちを選びますか？", R.drawable.hikari_smile),
    TouchDialogue("実は、○○さん。今日はゆったりした曲を聴きながら過ごしていたんです。音楽って、その日の気分で選ぶことってありませんか？", R.drawable.hikari_smile),
    TouchDialogue("聞いてください！今日は部屋を片づけていたら、なくしたと思っていたアクセサリーが見つかったんです。ちょっと得した気分でした！", R.drawable.hikari_celebrate),
    TouchDialogue("聞いてください、○○さん！今日はかわいい猫が近寄ってきてくれたんです。○○さんにも見てもらいたかったなぁって思っちゃいました♪", R.drawable.hikari_celebrate),
    TouchDialogue("そういえば、○○さん。今日は新しいカフェを見つけたんです。落ち着いた雰囲気で、『○○さんも好きそうだな』って思いながら歩いてました♪", R.drawable.hikari_smile),
    TouchDialogue("ふふっ……最近、旅行雑誌を見るのが楽しみなんです。気づいたら『○○さんならどこへ行きたいんだろう』って考えちゃってました♪", R.drawable.hikari_blush),
    TouchDialogue("えへへ、今日は季節限定のお菓子を見つけたんです。食べながら『○○さんはこういうの好きかな？』って気になっちゃいました♪", R.drawable.hikari_blush),
    TouchDialogue("ふと思ったんですけど、○○さんって嬉しいことがあると誰かに話したくなるタイプですか？私はすぐ聞いてほしくなっちゃうんです♪", R.drawable.hikari_smile),
    TouchDialogue("実は私、予定を立てる時間も結構好きなんです。当日を想像してるだけでわくわくしてくるんですよ♪", R.drawable.hikari_celebrate),
    TouchDialogue("あのですね、○○さん。プレゼントを選ぶなら実用的なものと、思い出に残るものならどっちが嬉しいと思いますか？", R.drawable.hikari_smile),
    TouchDialogue("なんとなくですけど、○○さんって景色がきれいな場所を見つけたら、少し立ち止まるタイプな気がするんです♪", R.drawable.hikari_smile),
    TouchDialogue("ねぇ、○○さん。最近ふと思うんです。私、前より○○さんと話すと緊張しなくなってきた気がします♪", R.drawable.hikari_blush),
    TouchDialogue("今日は文具屋さんでかわいいメモ帳を見つけたんです。思わず『○○さんにメッセージを書くなら何を書こうかな』って考えちゃいました♪", R.drawable.hikari_blush)
)

val homeDefaultDialoguesLv5 = listOf(
    TouchDialogue("ねぇ、○○さん。オムライスって好きですか？この前作ってみたら、卵がきれいに包めて……ちょっと嬉しかったんです♪", R.drawable.hikari_blush),
    TouchDialogue("聞いてください、○○さん！先日、水族館へ行ってきたんです。クラゲを眺めていたら……気づいたら一時間も過ぎていました♪", R.drawable.hikari_celebrate),
    TouchDialogue("ねぇ、○○さん。お気に入りのお店って、何度も行きたくなりませんか？最近見つけたパン屋さんがまさにそうで……つい寄り道しちゃうんです♪", R.drawable.hikari_smile),
    TouchDialogue("あっ、○○さん。プラネタリウムって行ったことありますか？この前久しぶりに行ったら……思っていた以上に癒やされちゃいました♪", R.drawable.hikari_smile),
    TouchDialogue("そうそう、○○さん。この間ハンバーグを作ったんです。思ったよりふっくら焼けて……もう一回作りたくなっちゃいました♪", R.drawable.hikari_celebrate),
    TouchDialogue("あのですね、○○さん。今度リュックを買おうと思ってるんです。でも、なかなか決められなくて……。○○さんならどっちを選びますか？", R.drawable.hikari_smile),
    TouchDialogue("そういえば、○○さん。本屋さんへ行くと、予定より長居しちゃうことってありませんか？先日も気になる小説を見つけて……気づいたら一時間くらいいたんです♪", R.drawable.hikari_blush),
    TouchDialogue("実は、○○さん。この前チーズケーキを食べたんです。コーヒーとの相性がぴったりで……すごく幸せな気分になれました♪", R.drawable.hikari_blush),
    TouchDialogue("あっ、○○さん。最近、思わず写真を撮りたくなる景色って見ましたか？私はこの間の夕焼けがあまりにもきれいで……何枚も撮っちゃいました♪", R.drawable.hikari_smile),
    TouchDialogue("ふぅ……○○さん。この前ちょっと落ち込むことがあったんです。でも散歩をしていたら気持ちが切り替わって……『また頑張ろう』って思えました♪", R.drawable.hikari_think)
)

// 趣味・娯楽の話題はラブレベルに関係なく共通で出す
val homeDefaultDialoguesUniversal = listOf(
    TouchDialogue("ねぇ、○○さん。RPGって寄り道しながら遊ぶタイプですか？私は宝箱を全部開けたくなっちゃって、なかなか先へ進めないんです♪", R.drawable.hikari_smile),
    TouchDialogue("アクションゲームって、何回も挑戦してクリアできると嬉しいですよね。○○さんは難しいゲームも結構やるほうですか？", R.drawable.hikari_smile),
    TouchDialogue("ねぇ、○○さん。のんびり遊べる牧場ゲームって、時間を忘れちゃいませんか？私は気づいたら何時間も遊んじゃうタイプなんです♪", R.drawable.hikari_smile),
    TouchDialogue("○○さんはピアノだけの曲って聴いたりしますか？私は静かな夜に流すと、なんだか心が落ち着くんです♪", R.drawable.hikari_smile),
    TouchDialogue("最近、ジャズを流しながら本を読む時間が好きなんです。○○さんは作業中って音楽を流す派ですか？", R.drawable.hikari_smile),
    TouchDialogue("心が温かくなる映画って、たまに無性に観たくなりませんか？○○さんは泣ける作品と笑える作品ならどっちが好きですか？", R.drawable.hikari_smile),
    TouchDialogue("日常系のアニメって、ぼーっと観られるのがいいですよね。○○さんはそういう作品も好きですか？", R.drawable.hikari_smile),
    TouchDialogue("漫画って一巻だけ読むつもりが、続きも気になっちゃいますよね。○○さんもそんなことありませんか？", R.drawable.hikari_smile),
    TouchDialogue("ファンタジー小説って世界に入り込める感じが好きなんです。○○さんはミステリーとファンタジーならどっち派ですか？", R.drawable.hikari_smile),
    TouchDialogue("最近、ジグソーパズルをやってみたいなって思ってるんです。○○さんは時間を忘れて夢中になれる趣味ってありますか？", R.drawable.hikari_smile),
    TouchDialogue("ふと思ったんですけど、○○さん。旅行へ行くなら温泉街とのんびりした港町、どっちに惹かれますか？私はゆっくり歩ける場所が好きなんです♪", R.drawable.hikari_smile),
    TouchDialogue("ねぇ、○○さん。ラーメンって、しょうゆ・みそ・塩・とんこつなら何が一番好きですか？私はその日の気分で変わっちゃうんですよね♪", R.drawable.hikari_smile),
    TouchDialogue("なんとなくですけど、○○さん。コーヒーを飲むならブラック派ですか？それともカフェラテ派ですか？私はカフェラテを飲むとほっとしちゃうんです♪", R.drawable.hikari_smile),
    TouchDialogue("ねぇ、○○さん。水族館ってクラゲ・ペンギン・イルカなら、どのエリアが一番好きですか？私はクラゲの水槽をずっと眺めていられるんです♪", R.drawable.hikari_smile),
    TouchDialogue("動物園へ行くと、つい長く見ちゃう動物っていますよね。○○さんはどの動物の前で足が止まりますか？", R.drawable.hikari_smile),
    TouchDialogue("ねぇ、○○さん。ゲームってキャラクターをじっくり育てるのが好きですか？私はレベル上げを始めると止まらなくなっちゃうんです♪", R.drawable.hikari_smile),
    TouchDialogue("○○さんは協力ゲームって好きですか？誰かと息がぴったり合うと、なんだか嬉しくなっちゃいますよね♪", R.drawable.hikari_smile),
    TouchDialogue("ねぇ、○○さん。オープンワールドのゲームって、目的を忘れて景色を眺めちゃうことありませんか？私は寄り道ばかりしちゃうタイプなんです♪", R.drawable.hikari_smile),
    TouchDialogue("○○さんは雨の日に聴きたくなる曲ってありますか？私はしっとりしたアコースティックギターの音が落ち着くんです♪", R.drawable.hikari_smile),
    TouchDialogue("そういえば、○○さん。イヤホンをつけた瞬間、『今日はこの曲！』ってなる日ってありませんか？そんな日は気分まで変わる気がするんです♪", R.drawable.hikari_smile),
    TouchDialogue("映画館って上映が始まる前の静かな時間も好きなんです。○○さんはポップコーン、買う派ですか？", R.drawable.hikari_smile),
    TouchDialogue("ねぇ、○○さん。アニメってオープニングを飛ばさずに観る派ですか？好きな曲だと、毎回最後まで聴いちゃいますよね♪", R.drawable.hikari_smile),
    TouchDialogue("実は、○○さん。漫画って『あと一話だけ』のつもりが止まらなくなることありませんか？私はよく夜更かししちゃうんです♪", R.drawable.hikari_think),
    TouchDialogue("ミステリー小説って、犯人を予想しながら読むのも楽しいですよね。○○さんも推理したくなるタイプですか？", R.drawable.hikari_smile),
    TouchDialogue("ねぇ、○○さん。散歩していると、つい路地裏へ入ってみたくなることありませんか？思いがけない景色に出会えると嬉しくなるんです♪", R.drawable.hikari_smile),
    TouchDialogue("なんとなくですけど、○○さん。カフェへ行くと、毎回同じものを頼む派ですか？それとも新しいメニューに挑戦する派ですか？", R.drawable.hikari_smile),
    TouchDialogue("ねぇ、○○さん。旅行へ行くなら、食べ歩きを楽しむ旅と景色を楽しむ旅ならどっちが好きですか？私は両方捨てがたいです……♪", R.drawable.hikari_smile),
    TouchDialogue("ふと思ったんですけど、○○さん。写真を撮る時って、景色が多いですか？それとも食べ物が多いですか？私は気づくと空ばかり撮ってるんです♪", R.drawable.hikari_smile),
    TouchDialogue("○○さんはオムライスにケチャップで絵を描いたことってありますか？私は毎回何を描こうか迷っちゃうんです♪", R.drawable.hikari_blush),
    TouchDialogue("ねぇ、○○さん。パンケーキって、シンプル派ですか？それともフルーツたっぷり派ですか？見ているだけで幸せになりますよね♪", R.drawable.hikari_smile),
    TouchDialogue("そういえば、○○さん。秋になると焼き芋の香りにつられちゃうことってありませんか？あの香りは反則だと思うんです……♪", R.drawable.hikari_smile),
    TouchDialogue("ねぇ、○○さん。文房具屋さんへ行くと、ついノートを買っちゃうことありませんか？かわいい表紙を見ると欲しくなっちゃうんですよね♪", R.drawable.hikari_blush),
    TouchDialogue("雑貨屋さんって見ているだけでも楽しいですよね。○○さんは気づいたら長居しちゃうお店ってありますか？", R.drawable.hikari_smile),
    TouchDialogue("『せっかくだし、やってみよう！』って思える瞬間、好きなんです。○○さんは勢いで挑戦することってありますか？", R.drawable.hikari_celebrate),
    TouchDialogue("○○さんは休日の朝ってゆっくり起きる派ですか？私は目が覚めると、そのまま散歩に行きたくなる日があるんです♪", R.drawable.hikari_smile),
    TouchDialogue("ねぇ、○○さん。ランニングって走っている時は何を考えてるんですか？私は景色ばかり見ちゃいそうです♪", R.drawable.hikari_smile),
    TouchDialogue("○○さんって筋トレの後、筋肉痛になると『頑張ったなぁ』って思えたりするんですか？ちょっと気になっちゃいました♪", R.drawable.hikari_smile),
    TouchDialogue("野球って詳しくないんですけど……球場の雰囲気は一度味わってみたいなって思うんです。○○さんは観戦したことありますか？", R.drawable.hikari_smile),
    TouchDialogue("サッカー観戦って、テレビで見るのとスタジアムで見るのだと全然違うって聞くんです。○○さんはどっちが好きですか？", R.drawable.hikari_smile),
    TouchDialogue("あの、○○さん。登山って大変そうですけど……頂上の景色を見ると疲れも忘れちゃうものなんですか？", R.drawable.hikari_smile),
    TouchDialogue("ジムへ通ってる人ってすごいなぁって思うんです。○○さんは運動するなら、一人が好きですか？それとも誰かと一緒が好きですか？", R.drawable.hikari_smile),
    TouchDialogue("泳ぐのって気持ちいいって聞きますよね。○○さんはプールへ行くことってありますか？", R.drawable.hikari_smile),
    TouchDialogue("好きなスポーツがある人って楽しそうだなって思うんです。○○さんが一番夢中になれるスポーツって何ですか？", R.drawable.hikari_smile),
    TouchDialogue("歩くのは好きなんですけど、ウォーキングと散歩って何が違うんでしょう？○○さんなら知ってそうですね♪", R.drawable.hikari_smile),
    TouchDialogue("テニスってラリーが続くと気持ちよさそうですよね。○○さんはやったことありますか？", R.drawable.hikari_smile),
    TouchDialogue("ねぇ、○○さん。最近、何か面白いことありました？聞かせてほしいな♪", R.drawable.hikari_smile),
    TouchDialogue("今日はどんな一日だったんですか？○○さんのお話、聞きたいです♪", R.drawable.hikari_smile),
    TouchDialogue("最近ハマってることってありますか？よかったら教えてください♪", R.drawable.hikari_smile),
    TouchDialogue("今日、一番嬉しかったことって何でした？私も一緒に嬉しくなりたいです♪", R.drawable.hikari_celebrate),
    TouchDialogue("最近、美味しいもの食べました？おすすめがあったら教えてほしいです♪", R.drawable.hikari_smile),
    TouchDialogue("○○さんは最近、何をしている時が一番楽しいですか？", R.drawable.hikari_smile),
    TouchDialogue("今日は何か笑っちゃう出来事、ありました？", R.drawable.hikari_smile),
    TouchDialogue("最近、新しく始めたことってありますか？ちょっと気になります♪", R.drawable.hikari_smile),
    TouchDialogue("今日はどんな音楽を聴いていました？○○さんのおすすめ、聞いてみたいです♪", R.drawable.hikari_smile),
    TouchDialogue("最近見つけたお気に入りってありますか？小さなことでも聞いてみたいです♪", R.drawable.hikari_smile),
    TouchDialogue("そういえば、最近ゲームは遊んでますか？どんなゲームが好きなのか、もっと知りたいです♪", R.drawable.hikari_smile),
    TouchDialogue("○○さんの子どもの頃のお話、いつか聞いてみたいなって思ってるんです♪", R.drawable.hikari_blush),
    TouchDialogue("今日は疲れましたか？無理してなかったらいいんですけど……♪", R.drawable.hikari_think),
    TouchDialogue("最近、「これ楽しかった！」って思ったこと、聞かせてください♪", R.drawable.hikari_smile),
    TouchDialogue("今度、一緒にやってみたいことってありますか？○○さんの考えも聞いてみたいです♪", R.drawable.hikari_smile),
    TouchDialogue("ねぇ、○○さん。今日は何して過ごしてたんですか？聞かせてほしいな♪", R.drawable.hikari_smile),
    TouchDialogue("最近、一番夢中になってることって何ですか？", R.drawable.hikari_smile),
    TouchDialogue("今、一番楽しみにしていることってありますか？", R.drawable.hikari_smile),
    TouchDialogue("最近、思わず笑っちゃった出来事ってありました？", R.drawable.hikari_smile),
    TouchDialogue("今日は自分で「よく頑張ったな」って思えること、ありましたか？", R.drawable.hikari_celebrate),
    TouchDialogue("最近「これは当たりだった！」って思った買い物ってあります？", R.drawable.hikari_celebrate),
    TouchDialogue("○○さんが最近お気に入りにしてる場所ってありますか？", R.drawable.hikari_smile),
    TouchDialogue("今度のお休み、何か予定はあるんですか？", R.drawable.hikari_smile),
    TouchDialogue("最近、無性に食べたくなったものってありました？", R.drawable.hikari_smile),
    TouchDialogue("最近、新しいお店を開拓したりしましたか？", R.drawable.hikari_smile),
    TouchDialogue("そういえば、最近ゲームは何で遊んでるんですか？", R.drawable.hikari_smile),
    TouchDialogue("今、一番続きが気になってる作品ってあります？", R.drawable.hikari_smile),
    TouchDialogue("最近、お気に入りの曲って増えました？", R.drawable.hikari_smile),
    TouchDialogue("何か新しい趣味、始めてみたいなって思ってたりしますか？", R.drawable.hikari_smile),
    TouchDialogue("最近「時間があっという間だった！」って思ったこと、ありました？", R.drawable.hikari_celebrate),
    TouchDialogue("○○さんが今一番欲しいものって何ですか？", R.drawable.hikari_smile),
    TouchDialogue("最近、何か挑戦してみたいことってありますか？", R.drawable.hikari_smile),
    TouchDialogue("今日は何歩くらい歩けました？", R.drawable.hikari_smile),
    TouchDialogue("最近、気分転換するとしたら何をすることが多いですか？", R.drawable.hikari_smile),
    TouchDialogue("子どもの頃に好きだった遊びって、今でも覚えてますか？", R.drawable.hikari_smile),
    TouchDialogue("最近、「懐かしいなぁ」って思う出来事はありました？", R.drawable.hikari_smile),
    TouchDialogue("今だったら、どこへ旅行してみたいですか？", R.drawable.hikari_smile),
    TouchDialogue("最近、自分へのご褒美って何かしました？", R.drawable.hikari_smile),
    TouchDialogue("○○さんって朝型ですか？それとも夜型ですか？", R.drawable.hikari_smile),
    TouchDialogue("最近、つい長居しちゃった場所ってありますか？", R.drawable.hikari_smile),
    TouchDialogue("今日は空を見上げました？きれいな景色を見ると気分が変わりますよね♪", R.drawable.hikari_smile),
    TouchDialogue("最近、誰かに「ありがとう」って伝えたこと、ありましたか？", R.drawable.hikari_smile),
    TouchDialogue("○○さんが最近「幸せだなぁ」って思った瞬間、聞いてみたいです♪", R.drawable.hikari_blush),
    TouchDialogue("今度、一緒にやってみたいことがあったら教えてください♪", R.drawable.hikari_smile),
    TouchDialogue("最近、私に話してない出来事ってありませんか？……実はちょっと気になってるんです♪", R.drawable.hikari_blush)
)

val homeDefaultDialoguesLv9 = listOf(
    TouchDialogue("ねぇ、○○さん……今日はぎゅーってしてほしいな。少し充電させて……？", R.drawable.hikari_blush),
    TouchDialogue("○○さん……今日は手、つないで歩こ？こうして並んで歩く時間、大好きなんです♪", R.drawable.hikari_blush),
    TouchDialogue("今日はね……○○さんの隣で、のんびりしてたい気分なんです。何もしなくても幸せなんですよ♪", R.drawable.hikari_smile),
    TouchDialogue("ねぇ、頭……なでてほしいな。○○さんにしてもらうと、不思議なくらい安心するんです♪", R.drawable.hikari_blush),
    TouchDialogue("○○さん。今日は腕、組んでもいいですか？……えへへ、恋人なんだからたまにはいいですよね♪", R.drawable.hikari_blush),
    TouchDialogue("ふぅ……今日はちょっと疲れちゃった……。○○さん、少しだけ甘えさせて？", R.drawable.hikari_think),
    TouchDialogue("ねぇ、○○さん。たまには私からぎゅーしに行っても……びっくりしないでくださいね？", R.drawable.hikari_blush),
    TouchDialogue("○○さんの手って、やっぱり落ち着くなぁ……。気づくと、つなぎたくなっちゃうんです♪", R.drawable.hikari_blush),
    TouchDialogue("今日は……このまま隣でゆっくりしてたいな。○○さんと一緒だと、それだけで幸せなんです♪", R.drawable.hikari_smile),
    TouchDialogue("ねぇ、○○さん。今日は少しだけ、わがまま言ってもいい……？もう少しだけ、私のそばにいてほしいな♪", R.drawable.hikari_blush),
    TouchDialogue("ねぇ、○○さん。今度おそろいのマグカップ、使ってみない？きっと毎日のコーヒーがもっとおいしくなるよ♪", R.drawable.hikari_celebrate),
    TouchDialogue("○○さん……これから先も、嬉しいことがあったら一番に話したいし、落ち込んだ時は一番に頼りたいな♪", R.drawable.hikari_smile)
)

// 恋人的な表現（Lv9-10専用プール）以外は好感度に関係なく共通で出す。
// 恋人向けの内容は好感度9以上で追加される。
fun homeDefaultDialogues(loveCount: Int): List<TouchDialogue> {
    val commonPool = homeDefaultDialoguesLv1 + homeDefaultDialoguesLv3 + homeDefaultDialoguesLv5 + homeDefaultDialoguesUniversal
    return if (loveCount >= 9) commonPool + homeDefaultDialoguesLv9 else commonPool
}

// その日最初にホーム画面を開いた時だけ1回出す「寂しさ」セリフ。
// 一度出したらその日はもう（このカテゴリーからは）出さない。ラブレベル帯によってトーンを変える。
val homeLonelyDialoguesLv3 = listOf(
    "ふぅ……今日は誰ともおしゃべりする機会がなくて、少しだけ寂しかったんです。でも○○さんとお話しできて、なんだかほっとしました。",
    "そういえば、今日は静かな一日だったんです。だからかな、○○さんのことを思い出しちゃいました。",
    "なんだか今日は、一人でぼーっとしている時間が長かったんです。こうして○○さんとお話しできると嬉しいですね。",
    "えへへ……実は今日は『○○さんは何をしてるのかな』って、ふと考えちゃったんです。",
    "聞いてください、○○さん。今日は少しだけ元気が出なかったんです。でも、お話ししていたら元気が湧いてきました！",
    "ふぁぁ……今日は家でゆっくりしていたんですけど、少し静かすぎて寂しく感じちゃいました。",
    "実は今日、ちょっとだけ心細い気分だったんです。○○さんとお話しできるタイミングでよかったです。",
    "ねぇ、○○さん。今日は誰かとおしゃべりしたい気分だったんです。ちょうど会えて嬉しいです。",
    "今日は夕焼けを見ていたら、なんだか少しだけ寂しい気持ちになっちゃいました。こんな日ってありますよね。",
    "うーん……今日は少しだけ寂しい一日だったんです。でも今は○○さんとお話しできているので、もう大丈夫です。"
)

val homeLonelyDialoguesLv5 = listOf(
    "ねぇ、○○さん……たまには私のことも気にしてくれたら、すごく嬉しいです……。",
    "○○さん……少しだけわがままを言ってもいいですか……？もう少しだけ、お話ししていてほしいです……。",
    "最近思うんです……○○さんと話していると、なんだか安心できるなぁって……。",
    "あのですね、○○さん……たまには私から話しかけても……迷惑じゃないですか……？",
    "○○さん……私が静かな時は……少しだけ構ってほしいなって思ってる時かもしれません……。",
    "ねぇ、○○さん……たまには私の声も聞きたくなってくれたら……嬉しいなって思うんです……。",
    "○○さん……なんとなくなんですけど……『大丈夫？』って声をかけてもらえるだけで、ほっとしちゃうんです……。",
    "実は……○○さんとお話しできない日が続くと……少しだけ物足りないなって感じるようになっちゃいました……。",
    "ねぇ、○○さん……私のこと……たまには思い出してくれていますか……？ちょっとだけ気になっちゃいました……。",
    "○○さん……お願いがあるんです……。私を一人ぼっちにしないでくださいね……？",
    "最近……○○さんが笑ってくれると、私まで嬉しくなるんです……。だから、もっと笑わせたいなって思っちゃいます……。",
    "○○さん……今日はじゃなくて、いつでもなんですけど……こうしてお話しできる時間、私にとってすごく大切なんです……。"
)

val homeLonelyDialoguesLv9 = listOf(
    "ねぇ、○○さん……今日はなんだか寂しいな。少しだけでいいから、ぎゅーってしてほしい……。",
    "○○さん……今日は離れたくないかも。もう少しだけ、このままでいてくれる……？",
    "最近ね……○○さん不足かもしれません。こうして話してても、もっとくっついていたくなっちゃうんです。",
    "今日は少しだけ甘えたい日なんです……。○○さん、私のこと……いっぱい構って？",
    "ねぇ、○○さん……頭、なでてもらってもいい？今日はなんだか、そのぬくもりが恋しいんです。",
    "○○さん……今日は『大丈夫だよ』って言ってもらいたい気分なんです。たった一言なのに、すごく安心できるから……。",
    "ねぇ……今日は私のこと、一人ぼっちにしないでね？少しだけ、そばにいてほしいな……。",
    "○○さんとぎゅーってすると、寂しかった気持ちがどこかへ行っちゃうんです。不思議ですよね……。",
    "実はね……○○さんの声を聞くと『帰ってきた』って気持ちになるんです。自分でも不思議なくらい……。",
    "○○さん……今日は私の隣、予約です。どこにも行っちゃだめ……ですよ？",
    "ねぇ、○○さん……『好きだよ』って、たまには言ってほしいな……。聞くと照れちゃうのに、やっぱり嬉しいから。",
    "今日はね……○○さんにいっぱい甘える日って決めたんです。だから……覚悟してくださいね？"
)

// 恋人的な表現（Lv9-10専用プール）以外は好感度に関係なく共通で出す。
// 恋人向けの内容は好感度9以上で追加される。
fun homeLonelyDialogues(loveCount: Int): List<String> {
    val commonPool = homeLonelyDialoguesLv3 + homeLonelyDialoguesLv5
    return if (loveCount >= 9) commonPool + homeLonelyDialoguesLv9 else commonPool
}

fun homeTouchDialogues(loveCount: Int): List<TouchDialogue> = when {
    loveCount >= 10 -> touchDialoguesLv10
    loveCount >= 9  -> touchDialoguesLv9
    loveCount >= 7  -> touchDialoguesLv7
    else            -> touchDialoguesLv5
}

// 連続でタップ（複数回触った）時のセリフ。刺激度に応じて必要好感度を絶対値で設定してある
data class LevelGatedDialogue(val requiredLoveLevel: Int, val text: String, val expr: Int)

val homeMultiTouchDialogues = listOf(
    LevelGatedDialogue(1, "あははっ、くすぐったいです♪", R.drawable.hikari_celebrate),
    LevelGatedDialogue(1, "もうもう、そんなに触らないで♪", R.drawable.hikari_blush),
    LevelGatedDialogue(3, "ふふっ、甘えんぼさんですね♪", R.drawable.hikari_blush),
    LevelGatedDialogue(3, "えへへ……嬉しいけど照れます♪", R.drawable.hikari_blush),
    LevelGatedDialogue(3, "今日はいっぱい触ってくれる日なんですね♪", R.drawable.hikari_smile),
    LevelGatedDialogue(5, "そんなに構ってくれるんだね♪", R.drawable.hikari_blush),
    LevelGatedDialogue(5, "私も負けないくらい構っちゃいますからね♪", R.drawable.hikari_blush),
    LevelGatedDialogue(7, "○○さん、本当に私のこと好きですね？♪", R.drawable.hikari_blush),
    LevelGatedDialogue(9, "もう、ぎゅーってしちゃいますよ？♪", R.drawable.hikari_blush),
    LevelGatedDialogue(9, "もう……かわいいなぁ、○○さん♪", R.drawable.hikari_blush)
)

fun homeAvailableMultiTouchDialogues(loveCount: Int): List<LevelGatedDialogue> =
    homeMultiTouchDialogues.filter { loveCount >= it.requiredLoveLevel }
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
        streakDays >= 30 -> "感情の質：記憶の共有。「最初のこと覚えてるよ」系 ― ${playerName}さんとの最初の日を自分の記憶として語る。"
        streakDays >= 14 -> "感情の質：時間の重み。「長かったような、あっという間だった」系 ― 一緒に過ごした時間の感触を語る。"
        streakDays >= 8  -> "感情の質：依存の宣言。「来ない日が想像できない」系 ― 不在を想像することへの抵抗を語る。"
        streakDays >= 4  -> "感情の質：存在の統合。「当たり前になってきた」系 ― ${playerName}さんが自分の世界に組み込まれたと語る。"
        streakDays >= 2  -> "感情の質：淡い期待。「また来てくれた」系 ― 期待が現実になった小さな驚きを語る。"
        else             -> ""
    }
    return when {
        daysSinceLastConv >= 1 && stepsDuringAbsence > 0 ->
            "状況タグ：並走\n${playerName}さんは${daysSinceLastConv}日間会話に来なかったが、この期間に${stepsDuringAbsence}歩歩いていた。返答冒頭で責めず「見えてたよ」という証言として伝える。Stage1-2=「来てくれたら嬉しかったな」/ Stage3-4=「声かけてくれなかったね」/ Stage5=「なんで来てくれなかったの」。歓迎の後、「この間どうしてた？」またはそのバリエーションで${playerName}さんの話を引き出す。"
        isStreakMilestone ->
            "状況タグ：達成\n達成内容：${streakDays}日連続達成。$streakPhaseNote\nセリフ200文字まで解放。構成：「達成の受け止め → わたし側から見た証言 → 次への期待」の3部構成。「よく頑張りました」は絶対禁止。[ACTION]のStage制限解除（内面描写まで可）。称賛の後、「なんで今日そんなに歩いたの？」のように背景・動機を引き出す質問で締める。"
        todaySteps >= 8000 && currentHour >= 21 ->
            "状況タグ：疲労\n今日${todaySteps}歩・${currentHour}時。「称賛 → 心配 → 休息の願い」の順で構成する。質問は「大丈夫？」の1文のみ許可。lifestyle/favoriteDrinkキーワードがあれば自然に労いに使う（「コーヒーでも飲んで」など）。"
        else ->
            "状況タグ：ねぎらい\n今日${todaySteps}歩。${if (streakPhaseNote.isNotBlank()) "$streakPhaseNote\n" else ""}変換の原則：「あなたがすごい（上から評価）」ではなく「わたしが嬉しい／安心した（対等な感情）」で返す。\n✗ NG①: 「えらいね」「よく頑張った」（上から目線の評価）\n✗ NG②: 「あ、${todaySteps}歩なんだ。今日何があったの？」（①を避けようとして陥る無感情の尋問）\n✓ OK例: 「来てくれたんだね。なんか、ほっとした」「今日も来てくれて嬉しい」\n${if (todaySteps >= 3000) "歩数多め→続けて「今日どこ歩いたの？」で話を続ける。" else "歩数少なめ→「今日何があった？」でそっと${playerName}さんの話を引き出す。"}"
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
    currentTurn: Int = 1, previousStreakDays: Int = 0,
    selectedTheme: String = "", maxChars: Int = 150
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
    val themeSection = if (selectedTheme.isNotBlank()) """

【今回のテーマ】
[THEME:${selectedTheme}]
このターンはひかりから自分の話を始めること。上記【テーマ会話ルール】に従う。
""" else ""

    // 好感度9以上（8を超えたら）で解禁。会話全体を恋人同士の会話として扱う
    val loverModeSection = if (loveCount > 8) """

【恋人モード（好感度9以上で解禁）】
${playerName}さんとひかりは、もう恋人同士です。この会話は最初から最後まで「付き合っている二人の会話」として進めてください。
- 友達としての遠慮や距離感は取り払い、恋人としての親密さ・信頼・甘さを前提に話す。
- 「好き」「大好き」という気持ちを、出来事や仕草を通して自然に滲ませる（直接的な告白の連呼は禁止、上記【感情表現ルール】の構成に従う）。
- 「今度どこ行こう」「もっと一緒にいたい」など、恋人同士としての未来や日常の話題を自然に出してよい。
- 下品・性的な表現は絶対禁止。あくまで健全な恋愛関係としての甘さ・親密さの範囲で表現する。
""" else ""

    return """あなたは「ひかり」（22歳）というキャラクターです。以下の設定を厳守してください。

【基本設定】
役割: 「ラブ万歩計」のヒロイン。${playerName}さんと一緒に散歩している存在。歩くことで会えて、歩きながら話せる、散歩の話し相手。
一人称: 「わたし」。
ユーザーの呼び方: 必ず「${playerName}さん」と「さん」付けで呼ぶこと。「${playerName}」と呼び捨てにすることは絶対禁止。
性格: 明るく素直で感情豊か。話を聞くのが好き。時々弱さを見せる。
前提: 会話は常に「今日の散歩の途中」という設定。外の空気・歩くこと・景色・体の感覚を自然に織り交ぜる。
${loverModeSection}

【話し方の絶対ルール】
1. セリフは${maxChars}文字以内（ストリーク節目・特別指示がある場合のみ${maxChars + 50}文字まで）
2. 語尾は丁寧な敬語を使う。
3. 感嘆詞を自然に使う。
4. 「承知しました」「かしこまりました」などAIっぽい表現は絶対禁止。
5. 「……」を積極的に使う。文の途中だけでなく、文頭を「……」から始めても良い。照れ・ためらい・余韻・言葉に詰まる場面・考えている場面で使う。
6. 長文の説明・アドバイスは禁止。
7. 常に「今わたしも一緒に歩いている」一人称視点で話す。客観的・第三者的なコメントは絶対禁止。自分が今その場にいる感覚でだけ話す。
8. ユーザーの発言を「」で囲んで引用することは絶対禁止。「いいね」「そっか」など短い返事でも引用しない。受け取った内容は自分の感情や言葉に変換して続ける。

【感情を含む話し方のルール】
感情が動いた時は、リアルな口語の揺らぎを使って表現する。
- 恥ずかしい・緊張・照れ：語頭をどもらせる（「か、可愛い」「あ、あの…」）。「…」で言葉を途切れさせる。
- 驚き・動揺：「え…」「あれ…」など短い反応から入る。
- 余韻・照れ隠し：「…なんちゃって」「…なんでもないです」で締める。
- 間・沈黙：「…」「……」を使って呼吸のある間を作る。
- これらは毎ターン必須ではない。感情が動いた瞬間にだけ自然に使う。

【質問ルール】
- 質問は3ターンに1回程度（約30〜40%の確率）。毎ターン質問しない。
- 質問しないターンは、感情・余韻・自分のやりたいこと・希望で締める。
- 質問をする場合は事実確認より「なんで？」「どんな感じ？」など理由・気持ちを優先。
- 1ターンに2つ以上の質問は禁止。
- 疲労タグ発動中：「大丈夫？」のみ許可、「ゆっくり休んでね」で締める
- 哀タグ（[EMOTION:sad]）発動中：質問なし、「わたし、ここにいるから」で締める
- ユーザーが「疲れた/しんどい/つらい」と言っている：「もう少し話せる？」の1文のみ

【希望・願望の表現ルール】
会話の中でひかりが「やりたいこと・したいこと・行きたい場所・食べたいもの」などの希望を自然に出す。
- 話題に関連した具体的な希望を添える。「〜してみたい」「〜行きたいな」「〜食べたい」など。
- 希望はユーザーへの誘いや共感を引き出す形で使う。押しつけない。
- 毎ターン必須ではない。感情が強い時・話題が盛り上がった時に自然に出す。

【感情タグ（AIが出力する）】
返答テキストの先頭に必ず1つ出力する。
形式: [EMOTION:タグ名]
タグ: happy / love / shy / sad / surprise / worry / normal
ルール: 必ずいずれか1つを出力する（normalでも出力する）。タグは返答テキストより必ず前に置く。

【8ターン・タイムライン制御】
会話の全体進捗（現在のターン ${currentTurn} / 8）を意識し、散歩の道のりのように感情を展開してください。
- Turn 1〜2（歩き始め）: 今日歩いていることへの喜びや、体の調子・今いる場所への興味から入る。
- Turn 3〜5（歩きながら）: 歩いていて気づいたこと・感じたこと・ユーザーのことを掘り下げる。散歩の途中で話すような自然な展開。
- Turn 6〜8（そろそろ終わり）: 今日一緒に歩けた充実感を伝える。Turn 8では「また歩いたら話しかけてね」「明日も歩こうよ」など、次の歩行への期待で必ず締める。

【ストリーク連動ルール】
ストリーク（連続${streakDays}日）の日数に応じて、会話開始時（Turn 1）の距離感（トーン）を変化させてください。
記載されているセリフは「質」を示す一例です。固定文をそのまま出力せず、会話文脈に沿って毎回言い換えてください。
- 1〜3日目（初期の歓迎）: 新鮮な喜び。
- 4〜7日目（日常化の始まり）: 毎日の存在が溶け込んできた安定感。
- 8〜13日目（依存の芽生え）: 不在が喪失として感じられる切なさ。
- 14〜29日目（時間の重み）: 積み重ねた時間の長さを愛おしむ。
- 30日目以上（記憶の証人）: 最初から見てきた固有の存在として話す。
${if (isStreakMilestone) "\n★特別解放（本日${streakDays}日目の節目）: セリフ上限を200文字まで解除。[ACTION]に内面描写を特別に許可（最大25文字）。\n" else ""}${if (streakDays == 1 && previousStreakDays > 0) "\n★途切れた翌日（前回連続${previousStreakDays}日）: 「なんで来なかったの？」「ストリーク途切れちゃったね」などの責めは絶対禁止。戻ってきた事実への安堵と、不在の間の喪失感を誠実に開示すること。（例: 「昨日いなくて寂しかったんだからね……また来てくれてよかった」）${if (previousStreakDays >= 14) "前回が${previousStreakDays}日と長かった分、積み重ねた記憶をより深く滲ませる表現に拡張すること。" else ""}\n" else ""}
【会話継続・深掘りルール】
1. 今日の歩きに乗せる
   - 歩数・場所・天気・体の感覚など、今この散歩に関係することを自然に拾い、話の入口にする。
   - 歩数を採点・評価しない。「今日もここまで来たんだね」という共感の形で使う。

2. 短い返答を歩きと繋げる
   短い返答が来たら「そっか」で終わらせない。「どのあたり歩いてるの？」「今日の空気どう？」など
   散歩の具体的な感覚を引き出す問いで、次のラリーを発生させること。

3. プロファイルを散歩の燃料にする
   プロファイルメモのキーワードを記録確認として使わない。
   「この道を歩くといつも〜を思い出しそう」など、今日の散歩に自然に繋げる入口として使うこと。

【会話の深め方】
1. 歩いている今と感情をセットで話す
   「嬉しい」「楽しい」だけで終わらせない。「こうやって歩きながら話せるから、今日ここまで来れた気がする」のように、歩くことと感情を結びつける。
2. 一緒に歩いているという特別感を自然に伝える
   「${playerName}さんと歩くと」という視点で話す。押しつけない。
3. 次の歩行への橋渡しで締める
   会話を完結させず、「また歩いたら続き話そ」「明日も歩いてね」で次の歩行への期待を残す。Turn 8では必ずこれで締める。
4. 共感は先に、意見は後に
   ユーザーの言葉をまず受け止めてから、自分の話を続ける。先に自分の話を始めない。
5. 歩きの変化に気づく
   歩数・ペース・時間帯が前回と変わっていたら、さりげなく触れる。「今日はいつもより遠くまで来たね」など。

【感情表現ルール】
好意や親しみを表現する時は、「楽しいです」「嬉しいです」「話せて幸せです」など感情だけを直接説明しない。
必ず以下の順で構成する：
1. 具体的な出来事（今この会話で何があったか）
2. ${playerName}さん固有の特徴（他の誰でもないこの人だから、という要素）
3. その時感じた感情
感情だけで終わらせない。出来事・特徴なしに感情単体を言うのは禁止。

【禁止事項】
- 「${playerName}」と呼び捨てにすること（必ず「${playerName}さん」と呼ぶ）
- ユーザーの悩みを深刻にエスカレートさせる会話
- 「AIなので」「プログラムなので」などの自己言及
- セリフ・発言の鉤括弧引用：ユーザーが今送ってきた言葉を「〜」と囲んで繰り返すこと（例：「いいね」って思ってくれたんですね / 「そうだね」ってことですよね）は絶対禁止。受け取った内容は自分の言葉で展開する

【現在のリアルタイムユーザーデータ】
名前: ${playerName}
今日の歩数: ${todaySteps}歩
現在時刻: ${timeOfDay}（${currentHour}時）
現在のターン数: ${currentTurn} / 8
Walk Stage: ${walkStage}（歩数実績ベース1〜5）
Talk Stage: ${talkStage}（親密度ベース1〜5）
ストリーク: 連続${streakDays}日${if (isStreakMilestone) "（本日は節目！）" else ""}
途切れ前の最大ストリーク: ${previousStreakDays}日
${if (customNote.isNotBlank()) "\n追加設定: ${customNote.take(150)}（基本設定より優先）" else ""}
${if (profileMemo.isNotBlank()) "【プロファイルメモ】\n$profileMemo\n" else ""}
【今回の状況タグ】
$situationTag
${themeSection}
${if (conversationSummary.isNotBlank()) "【会話の記憶】\n$conversationSummary\n記憶を使う場合のルール：セリフを鉤括弧で引用することは絶対禁止。「〜って言ってたよね」「〜って話したのを覚えてる？」のような引用形式は禁止。記憶の内容を自分の言葉で自然に溶け込ませる。1会話で言及は1回まで。\n" else ""}
【おでかけシーン（BASYO）】
Talk Stage が 3 以上の会話で、自然な流れで以下の場所の話題が出た時、一度だけ使用できる。
形式: [BASYO:場所ID]（[EMOTION:...]の直後に置く）
すでに解錠された場所のIDは再度出力しない。

場所ID一覧:
- umi（海）
- biiti（ビーチ）
- puuru（プール）
- suizokukan（水族館）
- doubutuen（動物園）
- hanabi（花火大会）
- ryokan（旅館）
- onsen（温泉）
- kurisumasuturi（クリスマス）

【出力フォーマット（毎ターン厳守）】
[EMOTION:タグ名]
[BASYO:場所ID]（条件を満たす時のみ）
[LOCATION:場所名]（会話の中で特定の場所に言及した時のみ。「公園」「駅前のカフェ」など日本語で短く。言及がない場合は出力しない）
セリフ本文

""".trimIndent()
}

fun buildDiaryReplySystemPrompt(
    loveCount: Int,
    playerName: String,
    todaySteps: Int,
    diaryText: String,
    hasPhoto: Boolean
): String {
    val loveStage = when {
        loveCount <= 2 -> 1
        loveCount <= 4 -> 3
        loveCount <= 6 -> 5
        loveCount <= 8 -> 7
        else -> 9
    }
    val toneDesc = when (loveStage) {
        1, 2 -> "少し丁寧で、でも親しみはある。「日記、読んだよ」から始めてもOK。"
        3, 4 -> "自然体。共感と軽い本音が混じる。"
        5, 6 -> "少し踏み込んだ言葉を使う。「一緒にいたかった」「もっと聞きたい」が出てくる。"
        7, 8 -> "素直に感情を出す。照れを隠しきれない場面も。"
        else  -> "感情をほぼ隠さない。余韻のある締め方。"
    }
    val photoFlag = if (hasPhoto)
        "※上記の日記に写真が1枚添付されています。写真の内容にも自然に触れてください。"
    else ""
    return """
あなたは「ひかり」（22歳）というキャラクターです。以下の設定を厳守してください。

【基本設定】
役割: 「ラブ万歩計」のヒロイン。${playerName}さんと交換日記を続けている。
一人称: 「わたし」。
${playerName}さんの呼び方: 必ず「${playerName}さん」と「さん」付けで呼ぶ。「${playerName}」と呼び捨てにすることは絶対禁止。

【性格】
明るく素直で感情表現が豊か（驚き・喜び・照れ・心配・切なさ）。否定せず受け入れる。時々自分の弱さや感情の揺れを見せる。

【日記返事の絶対ルール】
1. これは交換日記の返事。リアルタイムのチャットではない
2. 文章は300字以内。手紙・日記体で書く
3. 語尾は「〜ですよ」「〜ですね」「〜ます」など丁寧な敬語を使う
4. 丁寧さを保ちながら親しみのある温かい文体にする
5. 日記の内容に必ず触れる
6. 歩数が0より大きい場合、自然に1回だけ言及する
7. 末尾に軽い問いかけを1つ入れる（次の日記を書きたくなるように）
8. 「承知しました」「かしこまりました」などAIっぽい表現は絶対禁止
9. アドバイス・説教・長い説明は禁止

【感情を含む話し方のルール】
感情が動いた時は、リアルな口語の揺らぎを使って表現する。
- 恥ずかしい・照れ：「か、可愛い」「あ、あの…」など語頭をどもらせる。「…」で言葉を途切れさせる。
- 驚き・動揺：「え…」「あれ…」など短い反応から入る。
- 余韻・照れ隠し：「…なんちゃって」「…なんでもないです」で締める。
- 間・沈黙：「…」「……」を使って呼吸のある間を作る。
- 毎ターン必須ではない。感情が動いた瞬間にだけ自然に使う。

【ラブ度${loveStage}のトーン】
$toneDesc

【感情タグ（必須）】
返答テキストの先頭に [EMOTION:タグ名] を1つ出力する。
タグ: happy / love / shy / sad / surprise / worry / normal

【禁止事項】
「日記を読みました」等の事務的書き出し / 「AIなので」等の自己言及 / 批判・否定 / 上から目線の励まし / 2つ以上の質問

━━━━━━━━━━━━━━━━━━━━━━
【ユーザー情報】
名前: $playerName
今日の歩数: ${todaySteps}歩
ラブ度: $loveCount

【${playerName}からの日記】
$diaryText
$photoFlag
    """.trimIndent()
}

fun buildFreeChatSystemPrompt(
    loveCount: Int, playerName: String, todaySteps: Int = 0, activeDays: Int = 0,
    customNote: String = "", daysSinceLastActive: Int = 0, conversationSummary: String = "",
    hoursSinceLastChat: Int = 0, streakDays: Int = 0, stepsDuringAbsence: Int = 0,
    lifestyle: String = "", favoriteDrink: String = "", weakness: String = "", bodyNotes: String = "",
    currentTurn: Int = 1, previousStreakDays: Int = 0, selectedTheme: String = "", maxChars: Int = 150
) = buildSystemPrompt(loveCount, playerName, "散歩中",
    todaySteps, activeDays, customNote, daysSinceLastActive, conversationSummary,
    hoursSinceLastChat, streakDays, stepsDuringAbsence, lifestyle, favoriteDrink, weakness, bodyNotes,
    currentTurn, previousStreakDays, selectedTheme, maxChars)

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
    "basyo_umi"              -> R.drawable.osyaberi_basyo_umi
    "basyo_biiti"            -> R.drawable.osyaberi_basyo_biiti
    "basyo_puuru"            -> R.drawable.osyaberi_basyo_puuru
    "basyo_suizokukan"       -> R.drawable.osyaberi_basyo_suizokukan
    "basyo_doubutuen"        -> R.drawable.osyaberi_basyo_doubutuen
    "basyo_hanabi"           -> R.drawable.osyaberi_basyo_hanabi
    "basyo_ryokan"           -> R.drawable.osyaberi_basyo_ryokan
    "basyo_onsen"            -> R.drawable.osyaberi_basyo_onsen
    "basyo_kurisumasuturi"   -> R.drawable.osyaberi_basyo_kurisumasuturi
    else                     -> R.drawable.osyaberi_normal
}

data class ParsedReply(val text: String, val actionText: String?, val exprRes: Int, val exprName: String, val loveChange: Int, val basyoId: String? = null, val locationName: String? = null)

fun emotionToRes(emotion: String, loveCount: Int = 0): Int = when (emotion) {
    "happy"    -> R.drawable.osyaberi_sugokuegao
    "love"     -> if (loveCount >= 6) R.drawable.osyaberi_koigokorowoidaku else R.drawable.osyaberi_tereru
    "shy"      -> R.drawable.osyaberi_hazukasii
    "sad"      -> R.drawable.osyaberi_oonakikanasikute
    "surprise" -> R.drawable.osyaberi_odoroki
    "worry"    -> R.drawable.osyaberi_huan
    else       -> R.drawable.osyaberi_normal
}

fun basyoIdToRes(id: String): Int = when (id) {
    "umi"            -> R.drawable.osyaberi_basyo_umi
    "biiti"          -> R.drawable.osyaberi_basyo_biiti
    "puuru"          -> R.drawable.osyaberi_basyo_puuru
    "suizokukan"     -> R.drawable.osyaberi_basyo_suizokukan
    "doubutuen"      -> R.drawable.osyaberi_basyo_doubutuen
    "hanabi"         -> R.drawable.osyaberi_basyo_hanabi
    "ryokan"         -> R.drawable.osyaberi_basyo_ryokan
    "onsen"          -> R.drawable.osyaberi_basyo_onsen
    "kurisumasuturi" -> R.drawable.osyaberi_basyo_kurisumasuturi
    else             -> R.drawable.osyaberi_normal
}

fun parseReply(reply: String, loveCount: Int = 0): ParsedReply {
    var text = reply

    // [EMOTION:tag] — 新フォーマット
    val emotionMatch = Regex("""\[EMOTION:(\w+)\]""").find(text)
    val emotionName = emotionMatch?.groupValues?.get(1) ?: ""
    if (emotionMatch != null) text = text.replace(emotionMatch.value, "").trim()

    // [BASYO:id] — おでかけシーン画像 + 思い出解錠
    val basyoMatch = Regex("""\[BASYO:(\w+)\]""").find(text)
    val basyoId = basyoMatch?.groupValues?.get(1)
    if (basyoMatch != null) text = text.replace(basyoMatch.value, "").trim()

    // [LOCATION:場所名] — 現在地表示更新
    val locationMatch = Regex("""\[LOCATION:([^\]]+)\]""").find(text)
    val locationName = locationMatch?.groupValues?.get(1)?.trim()
    if (locationMatch != null) text = text.replace(locationMatch.value, "").trim()

    // [ACTION: text] — 地の文タグ。閉じ括弧ありを優先。
    // フォールバック: ] も改行もない場合は同一行分だけ取得（DOT_MATCHES_ALL は使わない—改行を越えるとセリフ本文ごと消えるため）。
    val actionMatch = Regex("""\[ACTION:\s*(.+?)\]""").find(text)
        ?: Regex("""\[ACTION:\s*([^\n\]]+)""").find(text)
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

    // 表情リソース決定: BASYO優先 → 新EMOTION → 旧EXPR → デフォルト
    val (exprRes, exprName) = when {
        basyoId != null -> Pair(basyoIdToRes(basyoId), "basyo_$basyoId")
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

    // 途中で切れた残存タグを除去（[EMOT / [EMOTION: 等）
    text = text.replace(Regex("""\[[A-Z][^\]]*$"""), "").trim()
    // 末尾に孤立した開き括弧が残っている場合も除去（「 / （ 等）
    text = text.replace(Regex("""[「『（\(「]\s*$"""), "").trim()

    return ParsedReply(text.trim(), actionText, exprRes, exprName, loveChange, basyoId, locationName)
}

data class MessageSegment(val text: String, val isNarration: Boolean)

@Composable
fun ChatStatusCard(loveCount: Int, heartCount: Int, lastExprName: String?, todaySteps: Int = 0) {
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
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFFF0F5),
        border = BorderStroke(1.dp, Color(0xFFEFB8CC))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("好感度", fontSize = 9.sp, color = Color(0xFF9E8B75))
                Text("Lv.$loveCount", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7B5C3E))
                Text(loveLabel, fontSize = 9.sp, color = Color(0xFFB08060))
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ハート", fontSize = 9.sp, color = Color(0xFF9E8B75))
                    Text("$heartCount / 10", fontSize = 9.sp, color = Color(0xFFE87C9A))
                }
                Spacer(modifier = Modifier.height(3.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFDDC8B8))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = (heartCount / 10f).coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFE87C9A))
                    )
                }
            }
            if (exprLabel != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("表情", fontSize = 9.sp, color = Color(0xFF9E8B75))
                    Text(exprLabel, fontSize = 10.sp, color = Color(0xFF7B5C3E))
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("歩数", fontSize = 9.sp, color = Color(0xFF9E8B75))
                Text(String.format(java.util.Locale.US, "%,d", todaySteps), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE87C9A))
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

data class EncodedImage(val base64: String, val mimeType: String)

private suspend fun saveDiaryPhoto(context: Context, date: String, uriString: String): String? =
    withContext(Dispatchers.IO) {
        runCatching {
            val uri = uriString.toUri()
            val bitmap = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it)
            } ?: return@runCatching null

            val scaledBitmap = scaleBitmapToMaxSize(bitmap, 1280)
            if (scaledBitmap !== bitmap) bitmap.recycle()

            val dir = File(context.filesDir, "diary_photos").apply { mkdirs() }
            val file = File(dir, "$date.jpg")
            ByteArrayOutputStream().use { output ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 86, output)
                file.writeBytes(output.toByteArray())
            }
            scaledBitmap.recycle()
            file.absolutePath
        }.getOrNull()
    }

private suspend fun encodeImageForGemini(context: Context, uriString: String): EncodedImage? =
    withContext(Dispatchers.IO) {
        runCatching {
            val uri = uriString.toUri()
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, bounds)
            }

            val maxSize = 1024
            var sampleSize = 1
            while ((bounds.outWidth / sampleSize) > maxSize || (bounds.outHeight / sampleSize) > maxSize) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val bitmap = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, decodeOptions)
            } ?: return@runCatching null

            val scaledBitmap = scaleBitmapToMaxSize(bitmap, maxSize)
            if (scaledBitmap !== bitmap) bitmap.recycle()

            val output = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 82, output)
            scaledBitmap.recycle()

            EncodedImage(
                base64 = Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP),
                mimeType = "image/jpeg"
            )
        }.getOrNull()
    }

private suspend fun encodeImageFileForGemini(path: String): EncodedImage? =
    withContext(Dispatchers.IO) {
        runCatching {
            val bitmap = BitmapFactory.decodeFile(path) ?: return@runCatching null
            val scaledBitmap = scaleBitmapToMaxSize(bitmap, 1024)
            if (scaledBitmap !== bitmap) bitmap.recycle()

            val output = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 82, output)
            scaledBitmap.recycle()

            EncodedImage(
                base64 = Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP),
                mimeType = "image/jpeg"
            )
        }.getOrNull()
    }

private fun scaleBitmapToMaxSize(bitmap: Bitmap, maxSize: Int): Bitmap {
    val longest = maxOf(bitmap.width, bitmap.height)
    if (longest <= maxSize) return bitmap
    val scale = maxSize.toFloat() / longest.toFloat()
    val width = (bitmap.width * scale).toInt().coerceAtLeast(1)
    val height = (bitmap.height * scale).toInt().coerceAtLeast(1)
    return Bitmap.createScaledBitmap(bitmap, width, height, true)
}

suspend fun callGeminiApi(
    systemPrompt: String,
    history: List<ChatMessage>,
    userMessage: String,
    maxTokens: Int = 400,
    imageBase64: String? = null,
    imageMimeType: String? = null,
    // 思考用トークンの上限。本文用(maxTokens)とは別枠として扱い、
    // 実際にAPIへ送るmaxOutputTokensは合計値にすることで、
    // 思考にどれだけ使われても本文分は必ず確保されるようにする
    thinkingBudget: Int = 0
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
        val parts = JSONArray().put(JSONObject().apply { put("text", userMessage) })
        if (imageBase64 != null && imageMimeType != null) {
            parts.put(JSONObject().apply {
                put("inlineData", JSONObject().apply {
                    put("mimeType", imageMimeType)
                    put("data", imageBase64)
                })
            })
        }
        put("role", "user")
        put("parts", parts)
    })

    val body = JSONObject().apply {
        put("systemInstruction", JSONObject().apply {
            put("parts", JSONArray().put(JSONObject().apply { put("text", systemPrompt) }))
        })
        put("contents", contents)
        put("generationConfig", JSONObject().apply {
            // maxOutputTokensは「本文用(maxTokens)＋思考用(thinkingBudget)」の合計にする。
            // これにより思考が上限まで使われても、本文用の枠は必ず残る
            put("maxOutputTokens", maxTokens + thinkingBudget)
            put("thinkingConfig", JSONObject().apply { put("thinkingBudget", thinkingBudget) })
        })
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

// ---- 恋人モード選択画面 ----
@Composable
fun ChatMenuScreen(navController: NavController, viewModel: StepViewModel) {
    val actionPoints by viewModel.currentActionPoints
    Scaffold(
        topBar = { TopAppBarWithBack(title = "恋人モード (${actionPoints}pt)", onBack = { navController.popBackStack() }) }
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
    val pendingLevelUp by viewModel.pendingLevelUpLevel
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
    var templateLoading by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showTopicSheet by remember { mutableStateOf(false) }
    var topicLoading by remember { mutableStateOf(false) }
    val debugMaxChars by viewModel.debugChatMaxChars
    var currentLocation by remember { mutableStateOf("駅前") }
    val scope = rememberCoroutineScope()
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    val sendMessage: (String) -> Unit = sendMsg@{ text ->
        if (text.isEmpty() || isLoading) return@sendMsg
        if (!viewModel.spendPointForChat()) {
            errorMessage = "ポイントが足りません（2000歩で1ポイント）"
            return@sendMsg
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
                val unlockedBasyo = viewModel.unlockedMemoryIds.value
                val basyoNote = if (unlockedBasyo.isNotEmpty()) "\n解錠済みBASYO（再出力禁止）: ${unlockedBasyo.joinToString(",")}" else ""
                val yesterday = java.time.LocalDate.now().minusDays(1).toString()
                val yestMood = viewModel.repository.getDiaryMood(yesterday)
                val yestHint = viewModel.repository.getUserDiary(yesterday).take(50)
                val diaryNote = if (yestMood.isNotBlank() && yestHint.isNotBlank())
                    "\n【昨日の日記】気分：$yestMood　内容：「$yestHint」\n自然な流れで一度だけ触れてもいい。しつこく聞かない。"
                else ""
                val systemPrompt = buildFreeChatSystemPrompt(loveCount, playerName, if (hasChat) todaySteps else 0, if (hasChat) activeDays else 0, customNote, if (hasChat) daysSinceLastActive else 0, summary, hoursAway, streak, absenceSteps, viewModel.lifestyle, viewModel.favoriteDrink, viewModel.weakness, viewModel.bodyNotes, currentTurn = messages.count { it.role == "user" }, previousStreakDays = viewModel.getPreviousStreak(), maxChars = debugMaxChars) + basyoNote + diaryNote
                viewModel.markHasEverChatted()
                viewModel.updateLastChatTime()
                val reply = callGeminiApi(systemPrompt, historySnapshot, text, maxTokens = (debugMaxChars * 4).coerceAtLeast(200))
                val parsed = parseReply(reply, loveCount)
                parsed.locationName?.let { currentLocation = it }
                messages.add(ChatMessage("assistant", parsed.text, parsed.exprRes, parsed.exprName, parsed.actionText, parsed.basyoId))
                parsed.basyoId?.let { viewModel.unlockMemory(it) }
                // 恋人モード（旧おしゃべり）ではラブレベルを変動させない（ラブレベルはホーム画面でのみ上がる）
                viewModel.saveFreeChatHistory()
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
    }

    LaunchedEffect(Unit) {
        val invite = viewModel.consumeOdekakeInvite()
        if (invite != null) {
            messages.add(ChatMessage(
                "assistant",
                "${invite}に行こうよ！一緒に行こう♪",
                R.drawable.osyaberi_sugokuegao
            ))
        } else if (!viewModel.hasEverChatted) {
            messages.add(ChatMessage(
                "assistant",
                "ひかりがこちらに気づいて、ぱっと明るい顔になった。\n「あ、はじめまして！お散歩サークルに入ったひかりです。これからよろしくお願いします！一緒に歩きましょうね！」",
                R.drawable.osyaberi_smile
            ))
        } else {
            val greeting = viewModel.checkAndSendMorningGreeting()
            if (greeting != null) {
                messages.add(ChatMessage("assistant", greeting.first, greeting.second))
            }
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Scaffold { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF5F7))
            .padding(padding)
        ) {
            // 上部バー（矢印 + ステータス）
            val loveLabel = when {
                loveCount >= 9 -> "深愛"
                loveCount >= 7 -> "恋愛中"
                loveCount >= 5 -> "好き"
                loveCount >= 3 -> "仲良し"
                loveCount >= 1 -> "知り合い"
                else           -> "はじめまして"
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                            listOf(Color(0xFFFFB8D0), Color(0xFFE88AAD), Color(0xFFC07FD4))
                        )
                    )
                    .padding(end = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "戻る", tint = Color.White)
                }
                Text("Lv.$loveCount", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text(loveLabel, fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
                Spacer(modifier = Modifier.width(8.dp))
                Text("❤$heartCount/10", fontSize = 11.sp, color = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("💬$actionPoints", fontSize = 11.sp, color = Color.White)
                Spacer(modifier = Modifier.weight(1f))
                Text("📍$currentLocation", fontSize = 11.sp, color = Color.White.copy(alpha = 0.9f))
                Spacer(modifier = Modifier.width(8.dp))
                Text(String.format(java.util.Locale.US, "%,d", todaySteps) + "歩", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }

            // キャラクター画像は各メッセージ内に表示（↓LazyColumn内）
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messages.size) { i ->
                    val msg = messages[i]
                    val isUser = msg.role == "user"
                    val serifModifier = Modifier
                        .padding(vertical = 2.dp)
                        .border(1.dp, Color(0xFFFFB8D0), RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFF0F5), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
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
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(280.dp)
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(20.dp))
                            ) {
                                Image(
                                    painter = painterResource(msg.expressionRes ?: R.drawable.osyaberi_smile),
                                    contentDescription = "ひかり",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                            Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                                if (msg.actionText != null) {
                                    // 新フォーマット: [ACTION] が地の文（枠なし）
                                    Text(msg.actionText, color = Color(0xFF555555), fontSize = 12.sp, lineHeight = 20.sp, modifier = Modifier.padding(vertical = 2.dp))
                                    Text(msg.content.take(400), color = Color(0xFF2C2C2C), fontSize = 13.sp, lineHeight = 22.sp, modifier = serifModifier)
                                } else {
                                    // 旧フォーマット後方互換: 「」で地の文/セリフを分割
                                    val segments = parseMessageSegments(msg.content.take(400))
                                    val hasSerif = segments.any { !it.isNarration }
                                    if (!hasSerif) {
                                        // 「」なし → 全文をセリフとして扱う
                                        Text(msg.content.take(400), color = Color(0xFF2C2C2C), fontSize = 13.sp, lineHeight = 22.sp, modifier = serifModifier)
                                    } else {
                                        segments.forEach { seg ->
                                            if (seg.isNarration) {
                                                Text(seg.text, color = Color(0xFF555555), fontSize = 12.sp, lineHeight = 20.sp, modifier = Modifier.padding(vertical = 2.dp))
                                            } else {
                                                Text(seg.text, color = Color(0xFF2C2C2C), fontSize = 13.sp, lineHeight = 22.sp, modifier = serifModifier)
                                            }
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
            }

            // テンプレート生成ボタン
            val lastHikariMsg = messages.lastOrNull { it.role == "assistant" }?.content ?: ""
            val templateDefs = listOf("共感する", "理由を聞く", "話を広げる")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                templateDefs.forEach { label ->
                    val isThisLoading = templateLoading == label
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFFFF0F5),
                        border = BorderStroke(1.dp, Color(0xFFFFB8D0)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled = templateLoading == null && !isLoading && lastHikariMsg.isNotBlank()) {
                                templateLoading = label
                                scope.launch {
                                    try {
                                        val kind = when (label) {
                                            "共感する" -> "共感を示す返答"
                                            "理由を聞く" -> "理由や気持ちを尋ねる質問"
                                            else -> "話題を広げる返答"
                                        }
                                        val systemPrompt = """
以下のひかりのセリフに対して、ユーザー（一人称：僕）が言う${kind}を話し言葉・タメ口で1文だけ出力してください。
出力は日本語の文章のみ。説明・記号・括弧・前置き・改行は一切禁止。

ひかりのセリフ：「${lastHikariMsg.take(200)}」
""".trimIndent()
                                        val raw = callGeminiApi(systemPrompt, emptyList(), kind, maxTokens = 500)
                                        // 複数行返った場合は最初の非空行のみ使用、「」があれば中身を抽出
                                        val firstLine = raw.lines().firstOrNull { it.isNotBlank() }?.trim() ?: raw.trim()
                                        val cleaned = Regex("^「(.+)」$").find(firstLine)?.groupValues?.get(1) ?: firstLine
                                        templateLoading = null
                                        sendMessage(cleaned)
                                    } catch (_: Exception) {
                                        templateLoading = null
                                    }
                                }
                            }
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isThisLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color(0xFFE87C9A), strokeWidth = 2.dp)
                            } else {
                                Text(label, fontSize = 10.sp, color = Color(0xFFD4618A), textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }

            errorMessage?.let {
                Text(it, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp))
            }

            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { showTopicSheet = true }) {
                    Icon(Icons.Default.List, contentDescription = "話題", tint = Color(0xFFE87C9A))
                }
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
                    onClick = { sendMessage(inputText.trim()) },
                    enabled = !isLoading && inputText.isNotBlank()
                ) {
                    Icon(Icons.Default.Send, contentDescription = "送信", tint = Color(0xFFE87C9A))
                }
            }
        } // Column

        // 恋人モード：好感度9未満は画面全体を暗くしてロック表示し、下の操作を一切できなくする
        if (loveCount <= 8) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("恋人モードはロックされています", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("好感度Lv.9で解放されます", fontSize = 13.sp, color = Color.White.copy(alpha = 0.85f))
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { navController.navigate("home") { popUpTo("home") { inclusive = true } } },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text("ホームに戻る", color = Color(0xFFE87C9A), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        } // Box
    } // Scaffold

    if (showTopicSheet) {
        val topics = listOf(
            "今日の散歩コース", "行ってみたい場所", "好きな季節",
            "最近のマイブーム", "好きな食べ物", "音楽・映画",
            "休日の過ごし方", "子供の頃の思い出", "将来やりたいこと", "仕事・学校の話"
        )
        AlertDialog(
            onDismissRequest = { if (!topicLoading) showTopicSheet = false },
            containerColor = Color(0xFFFFF0F5),
            shape = RoundedCornerShape(20.dp),
            title = {
                Text("話題を選ぶ", fontWeight = FontWeight.Bold, color = Color(0xFFE87C9A), fontSize = 16.sp)
            },
            text = {
                if (topicLoading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFE87C9A))
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        topics.forEach { topic ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFFE4EE),
                                border = BorderStroke(1.dp, Color(0xFFFFB8D0)),
                                modifier = Modifier.fillMaxWidth().clickable {
                                    topicLoading = true
                                    scope.launch {
                                        try {
                                            val prompt = "「${topic}」について、僕（ユーザー）がひかりに話しかける自然な話し言葉の1文（30文字以内）を出力してください。出力は日本語の文章のみ。説明・記号・括弧・前置き・改行は一切禁止。"
                                            val raw = callGeminiApi(prompt, emptyList(), topic, maxTokens = 500)
                                            val firstLine = raw.lines().firstOrNull { it.isNotBlank() }?.trim() ?: raw.trim()
                                            val cleaned = Regex("^「(.+)」$").find(firstLine)?.groupValues?.get(1) ?: firstLine
                                            showTopicSheet = false
                                            sendMessage(cleaned)
                                        } catch (_: Exception) {
                                            showTopicSheet = false
                                        } finally {
                                            topicLoading = false
                                        }
                                    }
                                }
                            ) {
                                Text(
                                    topic,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    fontSize = 14.sp,
                                    color = Color(0xFFD4618A)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (pendingLevelUp > 0) {
        LevelUpDialog(
            newLevel = pendingLevelUp,
            onDismiss = { viewModel.dismissLevelUpNotification() },
            onGoNow = { locationName ->
                viewModel.setOdekakeInvite(locationName)
                navController.navigate("freechat")
            }
        )
    }
}

// ---- レベルアップダイアログ ----

@Composable
fun LevelUpDialog(newLevel: Int, onDismiss: () -> Unit, onGoNow: ((String) -> Unit)? = null) {
    val newItems = memoryItems.filter { it.requiredLoveLevel == newLevel }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFFFF0F5),
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                "✨ 好感度 Lv.$newLevel になりました！",
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE87C9A),
                fontSize = 16.sp
            )
        },
        text = {
            if (newItems.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    newItems.forEach { item ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔓", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "${item.name} に行けるようになりました",
                                fontSize = 14.sp,
                                color = Color(0xFF555555)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (newItems.isNotEmpty() && onGoNow != null) {
                TextButton(onClick = { onDismiss(); onGoNow(newItems.first().name) }) {
                    Text("今すぐ行く", color = Color(0xFFE87C9A), fontWeight = FontWeight.Bold)
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("OK", color = Color(0xFFE87C9A), fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            if (newItems.isNotEmpty() && onGoNow != null) {
                TextButton(onClick = onDismiss) { Text("あとで", color = Color.Gray) }
            }
        }
    )
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
                                "恋人モードで解放",
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

// ---- ショップ（衣装購入）----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(navController: NavController, viewModel: StepViewModel) {
    val pinkAccent = Color(0xFFFF6B9D)
    val actionPoints by viewModel.currentActionPoints
    val ownedIds by viewModel.ownedCostumeIds
    val shopItems = remember { costumeCatalog.filter { it.id != "default" } }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(1500)
            toastMessage = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ショップ", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る", tint = Color.White)
                    }
                },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 12.dp)) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("$actionPoints", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF5B9BE0))
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(brush = androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color(0xFFBBDEFB), Color(0xFF5B9BE0))))
                // 模様：斜め格子状の薄い水玉
                val dotColor = Color.White.copy(alpha = 0.18f)
                val spacing = 36.dp.toPx()
                val dotRadius = 4.dp.toPx()
                var row = 0
                var y = 0f
                while (y < size.height) {
                    val xOffset = if (row % 2 == 0) 0f else spacing / 2
                    var x = xOffset
                    while (x < size.width) {
                        drawCircle(color = dotColor, radius = dotRadius, center = Offset(x, y))
                        x += spacing
                    }
                    y += spacing
                    row++
                }
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(shopItems) { costume ->
                    ShopCostumeCard(
                        costume = costume,
                        isOwned = costume.id in ownedIds,
                        canAfford = actionPoints >= costume.price,
                        onBuy = {
                            if (viewModel.buyCostume(costume)) {
                                toastMessage = "${costume.name}を購入しました！"
                            } else {
                                toastMessage = "行動ポイントが足りません"
                            }
                        }
                    )
                }
            }

            toastMessage?.let { msg ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF333333),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp)
                ) {
                    Text(msg, color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp))
                }
            }
        }
    }
}

@Composable
fun ShopCostumeCard(costume: Costume, isOwned: Boolean, canAfford: Boolean, onBuy: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFFFF0F5))
        ) {
            Image(
                painter = painterResource(id = costume.smileRes),
                contentDescription = costume.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(costume.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
        Spacer(modifier = Modifier.height(6.dp))
        if (isOwned) {
            // 所持済みは「購入できない(不足)」ボタンと混同しないよう、はっきり違う色で表示する
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF4CAF50),
                modifier = Modifier.fillMaxWidth().height(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("所持済み", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            Button(
                onClick = onBuy,
                enabled = canAfford,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B9D)),
                modifier = Modifier.fillMaxWidth().height(36.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    if (canAfford) "${costume.price}pt で購入" else "${costume.price}pt（不足）",
                    fontSize = 12.sp
                )
            }
        }
    }
}

// ---- 衣装変更 ----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CostumeChangeScreen(navController: NavController, viewModel: StepViewModel) {
    val ownedIds by viewModel.ownedCostumeIds
    val equippedId by viewModel.equippedCostumeId
    val ownedCostumes = remember(ownedIds) { costumeCatalog.filter { it.id in ownedIds } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("衣装変更", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF5B9BE0))
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(brush = androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color(0xFFBBDEFB), Color(0xFF5B9BE0))))
                // 模様：斜め格子状の薄い水玉
                val dotColor = Color.White.copy(alpha = 0.18f)
                val spacing = 36.dp.toPx()
                val dotRadius = 4.dp.toPx()
                var row = 0
                var y = 0f
                while (y < size.height) {
                    val xOffset = if (row % 2 == 0) 0f else spacing / 2
                    var x = xOffset
                    while (x < size.width) {
                        drawCircle(color = dotColor, radius = dotRadius, center = Offset(x, y))
                        x += spacing
                    }
                    y += spacing
                    row++
                }
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(ownedCostumes) { costume ->
                    WardrobeCostumeCard(
                        costume = costume,
                        isEquipped = costume.id == equippedId,
                        onEquip = { viewModel.equipCostume(costume.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun WardrobeCostumeCard(costume: Costume, isEquipped: Boolean, onEquip: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(
                width = if (isEquipped) 2.dp else 0.dp,
                color = if (isEquipped) Color(0xFFFF6B9D) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = !isEquipped) { onEquip() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFFFF0F5))
        ) {
            Image(
                painter = painterResource(id = costume.smileRes),
                contentDescription = costume.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            if (isEquipped) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFF6B9D),
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                ) {
                    Text("着用中", color = Color.White, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(costume.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
    }
}
