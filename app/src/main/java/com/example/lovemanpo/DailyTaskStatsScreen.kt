package com.example.lovemanpo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// Nintendo 2001 palette (same as DailyTaskScreen)
private val SCarbon = Color(0xFF21242e)
private val SGold = Color(0xFFe48600)
private val SAmber = Color(0xFFecab37)
private val SSignal = Color(0xFFf68d1f)
private val SCanvas = Color(0xFF7a8aba)
private val SCanvasSoft = Color(0xFF9fbee7)
private val SChrome = Color(0xFF3d4f97)
private val SMutedIndigo = Color(0xFF60619c)
private val SPlatinum = Color(0xFFdedede)
private val SSurface = Color(0xFFffffff)
private val SPeriwinkle = Color(0xFF8ba1d4)
private val SOnPrimary = Color(0xFFffffff)
private val SInkSoft = Color(0xFF3d4f97)
private val SRed = Color(0xFFe60012)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyTaskStatsScreen(navController: NavController, viewModel: DailyTaskViewModel) {
    val statPeriod by viewModel.statPeriod.collectAsState()
    val executionRates by viewModel.executionRates.collectAsState()
    val customStart by viewModel.customStart.collectAsState()
    val customEnd by viewModel.customEnd.collectAsState()

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadExecutionRates() }
    LaunchedEffect(statPeriod) { viewModel.loadExecutionRates() }

    val dateFormatter = DateTimeFormatter.ofPattern("MM/dd", Locale.JAPANESE)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SCanvas)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top nav bar ───────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SCarbon)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clickable { navController.popBackStack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "戻る",
                            tint = SCanvasSoft,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "実行率",
                        color = SGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .align(Alignment.BottomCenter)
                        .background(SChrome)
                )
            }

            // ── Period selector (subnav strip) ────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SCanvasSoft)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StatPeriod.entries.forEach { period ->
                        val isSelected = statPeriod == period
                        Box(
                            modifier = Modifier
                                .height(26.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isSelected) SCarbon else SPeriwinkle)
                                .clickable { viewModel.setStatPeriod(period) }
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = period.label,
                                color = if (isSelected) SGold else SCarbon,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            // ── Custom date range picker ───────────────────────────────────
            if (statPeriod == StatPeriod.CUSTOM) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SPeriwinkle)
                        .border(1.dp, SChrome)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "期間：",
                            color = SCarbon,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        // Start date button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(2.dp))
                                .background(SSurface)
                                .border(1.dp, SChrome, RoundedCornerShape(2.dp))
                                .clickable { showStartPicker = true }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = customStart.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")),
                                color = SCarbon,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text("〜", color = SCarbon, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        // End date button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(2.dp))
                                .background(SSurface)
                                .border(1.dp, SChrome, RoundedCornerShape(2.dp))
                                .clickable { showEndPicker = true }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = customEnd.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")),
                                color = SCarbon,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        // Apply button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(2.dp))
                                .background(SSignal)
                                .clickable { viewModel.loadExecutionRates() }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "適用",
                                color = SOnPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // ── Section label ─────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SCanvas)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                val periodLabel = when (statPeriod) {
                    StatPeriod.WEEK7 -> "直近7日間"
                    StatPeriod.MONTH30 -> "直近30日間"
                    StatPeriod.ALL_TIME -> "全期間"
                    StatPeriod.CUSTOM -> "${customStart.format(dateFormatter)} 〜 ${customEnd.format(dateFormatter)}"
                }
                Text(
                    "≡ EXECUTION RATE  $periodLabel",
                    color = SCarbon,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            // ── Task execution rate list ──────────────────────────────────
            if (executionRates.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "タスクがありません",
                        color = SMutedIndigo,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(executionRates, key = { it.task.id }) { item ->
                        TaskRateCard(item)
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }

            Spacer(
                modifier = Modifier
                    .navigationBarsPadding()
                    .height(0.dp)
            )
        }
    }

    // ── Date pickers ──────────────────────────────────────────────────────
    if (showStartPicker) {
        DatePickerDialog(
            title = "開始日",
            initial = customStart,
            onConfirm = { date ->
                val end = if (date.isAfter(customEnd)) date else customEnd
                viewModel.setCustomRange(date, end)
                showStartPicker = false
            },
            onDismiss = { showStartPicker = false }
        )
    }

    if (showEndPicker) {
        DatePickerDialog(
            title = "終了日",
            initial = customEnd,
            onConfirm = { date ->
                val start = if (date.isBefore(customStart)) date else customStart
                viewModel.setCustomRange(start, date)
                showEndPicker = false
            },
            onDismiss = { showEndPicker = false }
        )
    }
}

@Composable
private fun TaskRateCard(item: TaskExecutionRate) {
    val ratePercent = (item.rate * 100).toInt()
    val barColor = when {
        ratePercent >= 80 -> SSignal
        ratePercent >= 50 -> SAmber
        else -> SMutedIndigo
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(4.dp))
            .background(SPlatinum, RoundedCornerShape(4.dp))
            .border(1.dp, SChrome.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Task title + rate number
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.task.title,
                    color = SCarbon,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Days count
                    Text(
                        "${item.completedDays} / ${item.totalDays}日",
                        color = SInkSoft,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    // Rate badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(2.dp))
                            .background(barColor)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            "$ratePercent%",
                            color = SOnPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(SMutedIndigo.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(item.rate.coerceIn(0f, 1f))
                        .background(barColor, RoundedCornerShape(2.dp))
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    title: String,
    initial: LocalDate,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initial
            .toEpochDay() * 86_400_000L + java.util.TimeZone.getDefault().getOffset(
                initial.toEpochDay() * 86_400_000L
            )
    )

    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { millis ->
                    val days = millis / 86_400_000L
                    val date = LocalDate.ofEpochDay(days)
                    onConfirm(date)
                } ?: onDismiss()
            }) {
                Text("決定", color = SSignal, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル", color = SMutedIndigo)
            }
        }
    ) {
        DatePicker(
            state = state,
            title = {
                Text(
                    title,
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp),
                    color = SCarbon,
                    fontWeight = FontWeight.Bold
                )
            }
        )
    }
}
