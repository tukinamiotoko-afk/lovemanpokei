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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
        
    var openAiApiKey: String
        get() = prefs.getString("OPENAI_API_KEY", "") ?: ""
        set(value) = prefs.edit { putString("OPENAI_API_KEY", value) }

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
    val stepGaugeProgress = derivedStateOf { (todaySteps.intValue.toFloat() / 10000f).coerceAtMost(1f) }
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
                todaySteps.intValue = records.find { it.date == today }?.stepCount ?: 0
                cumulativeSteps.intValue = repository.cumulativeSteps
                loveCount.intValue = repository.loveCount
                heartCount.intValue = repository.heartCount
                spentActionPoints.intValue = repository.spentActionPoints
                totalEarnedPoints.intValue = repository.totalEarnedPoints
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

    var openAiApiKey: String
        get() = repository.openAiApiKey
        set(value) { repository.openAiApiKey = value }

    fun spendPointForChat(): Boolean {
        if (currentActionPoints.value > 0) {
            repository.spentActionPoints++
            spentActionPoints.intValue = repository.spentActionPoints
            return true
        }
        return false
    }

    fun earnHeartFromOdekake() {
        heartCount.intValue++
        if (heartCount.intValue >= 10) {
            heartCount.intValue = 0
            if (loveCount.intValue < 10) loveCount.intValue++
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
                composable("freechat") { FreeChatScreen(navController, viewModel) }
                composable("odekake") { OdekakeScenarioSelectScreen(navController, viewModel) }
                composable(
                    route = "odekake_chat/{locationId}",
                    arguments = listOf(navArgument("locationId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val locationId = backStackEntry.arguments?.getString("locationId") ?: "cafe"
                    OdekakeChatScreen(navController, viewModel, locationId)
                }
                composable(
                    route = "story/{episodeIndex}",
                    arguments = listOf(navArgument("episodeIndex") { type = NavType.IntType })
                ) { backStackEntry ->
                    val episodeIndex = backStackEntry.arguments?.getInt("episodeIndex") ?: 0
                    StoryScreen(navController, viewModel, episodeIndex)
                }
                composable("records") { RecordsScreen(navController, viewModel) }
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

    val currentLoveContent = loveContents.find { it.thresholdLove == loveCount } ?: loveContents.first()
    val stepDialogue = currentLoveContent.stepDialogues
        .filter { it.thresholdSteps <= todaySteps }
        .maxByOrNull { it.thresholdSteps }
        ?: currentLoveContent.stepDialogues.first()

    var touchedDialogue by remember { mutableStateOf<TouchDialogue?>(null) }
    LaunchedEffect(touchedDialogue) {
        if (touchedDialogue != null) {
            delay(5000)
            touchedDialogue = null
        }
    }

    val displayMessage = touchedDialogue?.message ?: stepDialogue.message
    val displayExpression = touchedDialogue?.expressionRes ?: stepDialogue.expressionRes ?: currentLoveContent.expressionRes

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
            touchedDialogue = currentLoveContent.touchDialogues.randomOrNull()
        },
        onFreeChatClick = { navController.navigate("freechat") },
        onOdekakeClick = { navController.navigate("odekake") },
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
    onOdekakeClick: () -> Unit,
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
            // ヘッダー
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Top
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                .height(420.dp)) {
                Image(
                    painter = painterResource(id = expressionRes),
                    contentDescription = "ひかり",
                    modifier = Modifier
                        .fillMaxHeight(1.0f)
                        .align(Alignment.BottomCenter)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onCharacterClick() },
                    contentScale = ContentScale.Fit
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 16.dp, top = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HomeStepCircleGauge(todaySteps, stepGaugeProgress)
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 16.dp, top = 0.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HomeLoveLevelCard(loveCount, heartGaugeProgress, hearts = heartCount)
                    HomeActionPointsCard(actionPoints)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-15).dp)
                    .shadow(8.dp, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            listOf(Color(0xFFFFF0F5), Color(0xFFFFCCE5))
                        )
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val formattedMessage = dialogueMessage.replace("○○", playerName)
                    HomeCommentBanner(expressionRes, formattedMessage)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        HomeStatItemSmall(Icons.Default.Schedule, "歩いた時間", activeTimeStr, null, Color(0xFFF06292))
                        HomeStatItemSmall(Icons.AutoMirrored.Filled.DirectionsWalk, "歩行距離", distanceStr, null, Color(0xFF4FC3F7))
                        HomeStatItemSmall(Icons.Default.Whatshot, "消費カロリー", caloriesStr, null, Color(0xFFFF8A65))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    HomeAdPlaceholder()
                    Spacer(modifier = Modifier.height(90.dp))
                }
            }
        } // outer Column

        HomeCustomBottomNav(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
            onFreeChat = onFreeChatClick,
            onOdekake = onOdekakeClick,
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
                Icon(Icons.AutoMirrored.Filled.DirectionsWalk, null, tint = Color(0xFF4A90E2), modifier = Modifier.size(18.dp))
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
fun HomeLoveLevelCard(lv: Int, progress: Float, hearts: Int) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFFFFE4EF), modifier = Modifier.width(110.dp), shadowElevation = 14.dp, border = BorderStroke(1.dp, Color(0xFFFF6B9D).copy(alpha = 0.5f))) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Favorite, null, tint = Color.Unspecified, modifier = Modifier.size(14.dp).gradientTint(listOf(Color(0xFFFF80AB), Color(0xFFE91E63))))
                Spacer(modifier = Modifier.width(4.dp))
                Text("ラブレベル", fontSize = 9.sp, color = Color(0xFFFF6B9D), fontWeight = FontWeight.Bold)
            }
            Text("Lv. $lv", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(4.dp))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
            ) {
                drawRoundRect(
                    color = Color(0xFFFFE0E9),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2)
                )
                drawRoundRect(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(Color(0xFFFF80AB), Color(0xFFE91E63))
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2),
                    size = size.copy(width = size.width * progress)
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text("$hearts / 10 ", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF6B9D))
                Icon(Icons.Default.Favorite, null, tint = Color.Unspecified, modifier = Modifier.size(9.dp).gradientTint(listOf(Color(0xFFFF80AB), Color(0xFFE91E63))))
            }
            // ★ ここにあったコメント行を削除しました
        }
    }
}

// 2. 行動ポイントカードのコメントを削除
@Composable
fun HomeActionPointsCard(pts: Int) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFFFFF0F5), modifier = Modifier.width(110.dp), shadowElevation = 14.dp, border = BorderStroke(1.dp, Color(0xFF4DB6AC).copy(alpha = 0.5f))) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Place, null, tint = Color.Unspecified, modifier = Modifier.size(14.dp).gradientTint(listOf(Color(0xFF80CBC4), Color(0xFF00695C))))
                Spacer(modifier = Modifier.width(4.dp))
                Text("行動ポイント", fontSize = 9.sp, color = Color.Gray)
            }
            Text("$pts / 5 pt", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(vertical = 4.dp)) {
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
            Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFE0F2F1)) {
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
fun HomeCommentBanner(expr: Int, message: String) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 14.dp, border = BorderStroke(1.5.dp, Color(0xFFFFB7D0))) {
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
                    Text(message, fontSize = 10.sp, color = Color.DarkGray)
                }
        }
    }
}

@Composable
fun HomeWeeklySection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Text("今週の歩数", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
            Text("目標 70,000 歩", fontSize = 9.sp, color = Color.Gray)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                val data = listOf(6315, 7102, 4803, 6540, 8765, 7842, 0)
                data.forEachIndexed { i, steps ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (steps > 0) Text(String.format(java.util.Locale.US, "%,d", steps), fontSize = 6.sp, color = Color(0xFF4A90E2))
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
                    drawArc(Color(0xFF4A90E2), -90f, 360f * 0.86f, false, style = Stroke(5.dp.toPx(), cap = StrokeCap.Round))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.EmojiEvents, null, tint = Color(0xFF4A90E2), modifier = Modifier.size(14.dp))
                    Text("達成度 86%", fontSize = 8.sp, color = Color(0xFF4A90E2), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun HomeCampaignBanner() {
    Surface(modifier = Modifier
        .fillMaxWidth()
        .height(70.dp), shape = RoundedCornerShape(8.dp), color = Color(0xFFE3F2FD)) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier
                    .size(50.dp)
                    .background(Color.White, RoundedCornerShape(4.dp)))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("歩いた分だけ、いいことあるよ。", fontSize = 9.sp, color = Color(0xFF1976D2))
                    Text("SPRING CAMPAIGN", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF1976D2))
                    Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFE91E63)) {
                        Text("詳しくはこちら ▶", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = Color.White, fontSize = 7.sp)
                    }
                }
            }
            Icon(Icons.Default.Info, null, modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(10.dp), tint = Color.Gray)
        }
    }
}

@Composable
fun HomeAdPlaceholder() {
    Surface(
        modifier = Modifier.fillMaxWidth().height(60.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color.Gray.copy(alpha = 0.08f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text("ここに広告が表示されます", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun HomeCustomBottomNav(modifier: Modifier = Modifier, onFreeChat: () -> Unit, onOdekake: () -> Unit, onRecords: () -> Unit) {
    Surface(modifier = modifier
        .fillMaxWidth()
        .height(80.dp), color = Color.White, shadowElevation = 10.dp) {
        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
            HomeNavItem(Icons.Default.Home, "ホーム", true) {}
            HomeNavItem(Icons.Default.ShoppingBag, "おでかけ", false, onOdekake)

            Box(contentAlignment = Alignment.Center, modifier = Modifier
                .offset(y = (-12).dp)
                .clickable { onFreeChat() }) {
                Surface(shape = CircleShape, color = Color(0xFF4A90E2), modifier = Modifier
                    .size(56.dp)
                    .shadow(4.dp, CircleShape)) {
                    Icon(Icons.Default.Chat, null, tint = Color.White, modifier = Modifier.padding(14.dp))
                }
                Text("自由会話", modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 22.dp), fontSize = 10.sp, color = Color(0xFF4A90E2), fontWeight = FontWeight.Bold)
            }

            HomeNavItem(Icons.Default.EditNote, "日記", false) {}
            HomeNavItem(Icons.Default.BarChart, "記録", false, onRecords)
        }
    }
}

@Composable
fun HomeNavItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, sel: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Icon(icon, null, tint = if (sel) Color(0xFF4A90E2) else Color.Gray, modifier = Modifier.size(24.dp))
        Text(label, fontSize = 9.sp, color = if (sel) Color(0xFF4A90E2) else Color.Gray)
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
            onOdekakeClick = {},
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

@Composable
fun StoryScreen(navController: NavController, viewModel: StepViewModel, episodeIndex: Int) {
    val episode = mainStoryEpisodes[episodeIndex]
    val playerName = viewModel.playerName.value
    val processedScript = remember(episode.script, playerName) {
        val newList = mutableListOf<StoryLine>()
        episode.script.forEach { line ->
            val fullText = line.text.replace("○○", playerName)
            val regex = "(?<=[。！？…])(?![。！？…])".toRegex()
            val chunksByPunctuation = fullText.split(regex).filter { it.isNotBlank() }
            chunksByPunctuation.forEach { chunk ->
                var remainingText = chunk.trim()
                while (remainingText.length > 48) { newList.add(line.copy(text = remainingText.substring(0, 48))); remainingText = remainingText.substring(48) }
                if (remainingText.isNotEmpty()) { newList.add(line.copy(text = remainingText)) }
            }
        }
        newList
    }
    VisualNovelScreen(episodeKey = episodeIndex, script = processedScript, backgroundRes = episode.backgroundRes, playerName = playerName) {
        navController.popBackStack()
    }
}

@Composable
fun VisualNovelScreen(episodeKey: Int, script: List<StoryLine>, backgroundRes: Int, playerName: String, onComplete: () -> Unit) {
    var lineIndex by remember(episodeKey) { mutableIntStateOf(0) }
    val currentLine = script[lineIndex]

    // 背景リソースの決定：行ごとの指定があればそれを優先
    val currentBackground = currentLine.backgroundRes ?: backgroundRes

    // 背景が切り替わるときに操作をロックするためのステート
    var isLocked by remember { mutableStateOf(false) }

    LaunchedEffect(currentBackground) {
        if (lineIndex > 0) { // 最初の行以外で背景が変わった場合
            isLocked = true
            delay(1500) // フェードイン時間(1000ms)より少し長めに設定して操作を不能にする
            isLocked = false
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        // ★ ここにステータスバーを避ける設定を追加しました
        .statusBarsPadding()
        // ★ 下のナビゲーションバーも避ける設定を追加しました
        .navigationBarsPadding()
        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
            if (!isLocked) {
                if (lineIndex < script.size - 1) {
                    lineIndex++
                } else {
                    onComplete()
                }
            }
        }) {
        // 画像が表示されるエリア（枠付き）
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(3.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            shadowElevation = 8.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // 背景のフェード切り替え
                AnimatedContent(
                    targetState = currentBackground,
                    transitionSpec = { fadeIn(animationSpec = tween(1000)) togetherWith fadeOut(animationSpec = tween(1000)) },
                    label = "BackgroundTransition"
                ) { targetBg ->
                    Image(
                        painter = painterResource(id = targetBg),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                val context = LocalContext.current
                val isCG = remember(currentBackground) {
                    try {
                        context.resources.getResourceEntryName(currentBackground).endsWith("cg_")
                    } catch (e: Exception) {
                        false
                    }
                }
                if (!isCG && currentLine.expressionRes != null) {
                    Image(
                        painter = painterResource(id = currentLine.expressionRes),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxHeight(0.7f)
                            .align(Alignment.BottomCenter),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }

        // テキストウィンドウ（下部）
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.Black.copy(alpha = 0.8f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                val label = when (currentLine.speaker) { Speaker.HIKARI -> "ひかり"; Speaker.PROTAGONIST -> playerName; Speaker.NARRATION -> "" }
                if (label.isNotEmpty()) {
                    Text(text = label, color = Color.Cyan, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp))
                }
                Box(modifier = Modifier.weight(1f)) {
                    Text(text = currentLine.text, color = Color.White, fontSize = 16.sp, lineHeight = 24.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "${lineIndex + 1} / ${script.size}", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    Text(text = "▼ タップして次へ", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
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
    // 期間に応じて目標歩数を動的に計算（1日1万歩基準）
    val stepGoal = when (period) {
        DisplayPeriod.DAY -> 10000
        DisplayPeriod.WEEK -> 70000
        DisplayPeriod.MONTH -> viewDate.lengthOfMonth() * 10000
        DisplayPeriod.YEAR -> if (java.time.Year.of(viewDate.year).isLeap) 3660000 else 3650000
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

                // 広告スペース
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    color = Color.Gray.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("ここに広告が表示されます", fontSize = 12.sp, color = Color.Gray)
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

    val rawMaxSteps = displayData.maxOfOrNull { it.steps }?.coerceAtLeast(1) ?: 10000
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
fun SettingsScreen(navController: NavController, viewModel: StepViewModel) {
    var tempName by remember { mutableStateOf(viewModel.playerName.value) }
    var tempHeight by remember { mutableStateOf(viewModel.heightCm.floatValue.toString()) }
    var tempWeight by remember { mutableStateOf(viewModel.weightKg.floatValue.toString()) }
    var tempGender by remember { mutableStateOf(viewModel.userGender.value) }
    var tempApiKey by remember { mutableStateOf(viewModel.openAiApiKey) }
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
            Text("AI会話設定", fontWeight = FontWeight.Bold, color = pinkAccent, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = tempApiKey,
                onValueChange = { tempApiKey = it },
                label = { Text("OpenAI APIキー") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                val h = tempHeight.toFloatOrNull() ?: 170f
                val w = tempWeight.toFloatOrNull() ?: 60f
                viewModel.setPlayerName(tempName)
                viewModel.setUserProfile(h, w)
                viewModel.saveProfile(h, tempGender)
                viewModel.openAiApiKey = tempApiKey
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
fun TopAppBarWithBack(title: String, onBack: () -> Unit) {
    CenterAlignedTopAppBar(
        title = { Text(text = title) },
        navigationIcon = { IconButton(onClick = onBack) { Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る") } },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
        modifier = Modifier.statusBarsPadding() // ★ これを追加！
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

data class ChatMessage(val role: String, val content: String)

data class OdekakeLocation(val id: String, val name: String, val emoji: String)

val odekakeLocations = listOf(
    OdekakeLocation("cafe",   "カフェ",  "☕"),
    OdekakeLocation("park",   "公園",    "🌸"),
    OdekakeLocation("cinema", "映画館",  "🎬"),
    OdekakeLocation("beach",  "海",      "🏖️"),
    OdekakeLocation("home",   "おうち",  "🏠")
)

fun buildFreeChatSystemPrompt(loveCount: Int, playerName: String): String {
    val intimacy = when {
        loveCount <= 2 -> "まだ少し距離がある丁寧な話し方"
        loveCount <= 5 -> "友達のような自然な話し方"
        else -> "とても親密で甘えた話し方"
    }
    return "あなたはヒカリというキャラクターです。${playerName}のことが大好きな女の子で、${intimacy}をします。返答は3文以内に収めてください。"
}

fun buildOdekakeChatSystemPrompt(locationId: String, loveCount: Int, playerName: String): String {
    val location = odekakeLocations.find { it.id == locationId }?.name ?: "カフェ"
    val base = buildFreeChatSystemPrompt(loveCount, playerName)
    return "$base 今は${playerName}と一緒に${location}に来ています。その場の雰囲気で会話してください。"
}

suspend fun callOpenAiApi(
    apiKey: String,
    systemPrompt: String,
    history: List<ChatMessage>,
    userMessage: String
): String = withContext(Dispatchers.IO) {
    val url = URL("https://api.openai.com/v1/chat/completions")
    val conn = url.openConnection() as HttpURLConnection
    conn.requestMethod = "POST"
    conn.setRequestProperty("Content-Type", "application/json")
    conn.setRequestProperty("Authorization", "Bearer $apiKey")
    conn.doOutput = true

    val messages = JSONArray()
    messages.put(JSONObject().apply { put("role", "system"); put("content", systemPrompt) })
    history.forEach { msg ->
        messages.put(JSONObject().apply { put("role", msg.role); put("content", msg.content) })
    }
    messages.put(JSONObject().apply { put("role", "user"); put("content", userMessage) })

    val body = JSONObject().apply {
        put("model", "gpt-4o-mini")
        put("max_tokens", 512)
        put("messages", messages)
    }.toString()

    conn.outputStream.write(body.toByteArray(Charsets.UTF_8))
    val response = conn.inputStream.bufferedReader(Charsets.UTF_8).readText()
    JSONObject(response)
        .getJSONArray("choices")
        .getJSONObject(0)
        .getJSONObject("message")
        .getString("content")
}

// ---- 自由会話画面 ----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreeChatScreen(navController: NavController, viewModel: StepViewModel) {
    val loveCount by viewModel.loveCount
    val actionPoints by viewModel.currentActionPoints
    val playerName by viewModel.playerName
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Scaffold(topBar = {
        TopAppBarWithBack(title = "自由会話 (${actionPoints}pt)", onBack = { navController.popBackStack() })
    }) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messages.size) { i ->
                    val msg = messages[i]
                    val isUser = msg.role == "user"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isUser) Color(0xFF4A90E2) else Color(0xFFF0F0F0),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Text(
                                msg.content,
                                modifier = Modifier.padding(10.dp),
                                color = if (isUser) Color.White else Color.DarkGray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                if (isLoading) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
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
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("メッセージを入力...") },
                    maxLines = 3
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        val text = inputText.trim()
                        if (text.isEmpty() || isLoading) return@IconButton
                        val apiKey = viewModel.openAiApiKey
                        if (apiKey.isEmpty()) {
                            errorMessage = "設定からOpenAI APIキーを入力してください"
                            return@IconButton
                        }
                        if (!viewModel.spendPointForChat()) {
                            errorMessage = "ポイントが足りません（2000歩で1ポイント）"
                            return@IconButton
                        }
                        errorMessage = null
                        val userMsg = ChatMessage("user", text)
                        messages.add(userMsg)
                        inputText = ""
                        isLoading = true
                        val historySnapshot = messages.dropLast(1).toList()
                        scope.launch {
                            try {
                                val systemPrompt = buildFreeChatSystemPrompt(loveCount, playerName)
                                val reply = callOpenAiApi(apiKey, systemPrompt, historySnapshot, text)
                                messages.add(ChatMessage("assistant", reply))
                            } catch (e: Exception) {
                                errorMessage = "エラーが発生しました: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    enabled = !isLoading && inputText.isNotBlank()
                ) {
                    Icon(Icons.Default.Send, contentDescription = "送信", tint = Color(0xFF4A90E2))
                }
            }
        }
    }
}

// ---- おでかけ場所選択画面 ----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OdekakeScenarioSelectScreen(navController: NavController, viewModel: StepViewModel) {
    val actionPoints by viewModel.currentActionPoints

    Scaffold(topBar = {
        TopAppBarWithBack(title = "おでかけ (${actionPoints}pt)", onBack = { navController.popBackStack() })
    }) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("どこへ行く？", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("会話を進めるとひかりとの好感度が上がります♪", fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            odekakeLocations.forEach { loc ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("odekake_chat/${loc.id}") },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(loc.emoji, fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(loc.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ---- おでかけ会話画面 ----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OdekakeChatScreen(navController: NavController, viewModel: StepViewModel, locationId: String) {
    val loveCount by viewModel.loveCount
    val actionPoints by viewModel.currentActionPoints
    val playerName by viewModel.playerName
    val location = odekakeLocations.find { it.id == locationId } ?: odekakeLocations.first()
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var odekakeMessageCount by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Scaffold(topBar = {
        TopAppBarWithBack(
            title = "${location.emoji} ${location.name} (${actionPoints}pt)",
            onBack = { navController.popBackStack() }
        )
    }) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messages.size) { i ->
                    val msg = messages[i]
                    val isUser = msg.role == "user"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isUser) Color(0xFF4A90E2) else Color(0xFFF0F0F0),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Text(
                                msg.content,
                                modifier = Modifier.padding(10.dp),
                                color = if (isUser) Color.White else Color.DarkGray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                if (isLoading) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
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
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("メッセージを入力...") },
                    maxLines = 3
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        val text = inputText.trim()
                        if (text.isEmpty() || isLoading) return@IconButton
                        val apiKey = viewModel.openAiApiKey
                        if (apiKey.isEmpty()) {
                            errorMessage = "設定からOpenAI APIキーを入力してください"
                            return@IconButton
                        }
                        if (!viewModel.spendPointForChat()) {
                            errorMessage = "ポイントが足りません（2000歩で1ポイント）"
                            return@IconButton
                        }
                        errorMessage = null
                        val userMsg = ChatMessage("user", text)
                        messages.add(userMsg)
                        inputText = ""
                        isLoading = true
                        odekakeMessageCount++
                        if (odekakeMessageCount % 3 == 0) {
                            viewModel.earnHeartFromOdekake()
                        }
                        val historySnapshot = messages.dropLast(1).toList()
                        scope.launch {
                            try {
                                val systemPrompt = buildOdekakeChatSystemPrompt(locationId, loveCount, playerName)
                                val reply = callOpenAiApi(apiKey, systemPrompt, historySnapshot, text)
                                messages.add(ChatMessage("assistant", reply))
                            } catch (e: Exception) {
                                errorMessage = "エラーが発生しました: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    enabled = !isLoading && inputText.isNotBlank()
                ) {
                    Icon(Icons.Default.Send, contentDescription = "送信", tint = Color(0xFF4A90E2))
                }
            }
        }
    }
}
