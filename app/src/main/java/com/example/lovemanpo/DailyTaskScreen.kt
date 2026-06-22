package com.example.lovemanpo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavController
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// Nintendo 2001 palette
private val N64Carbon = Color(0xFF21242e)
private val N64Gold = Color(0xFFe48600)
private val N64Amber = Color(0xFFecab37)
private val N64Signal = Color(0xFFf68d1f)
private val N64Canvas = Color(0xFF7a8aba)
private val N64CanvasSoft = Color(0xFF9fbee7)
private val N64Chrome = Color(0xFF3d4f97)
private val N64MutedIndigo = Color(0xFF60619c)
private val N64Platinum = Color(0xFFdedede)
private val N64Surface = Color(0xFFffffff)
private val N64Periwinkle = Color(0xFF8ba1d4)
private val N64Lavender = Color(0xFFacace7)
private val N64OnPrimary = Color(0xFFffffff)
private val N64InkSoft = Color(0xFF3d4f97)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyTaskScreen(navController: NavController, viewModel: DailyTaskViewModel) {
    val tasksWithStatus by viewModel.tasksWithStatus.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var taskToDelete by remember { mutableStateOf<DailyTask?>(null) }

    val today = LocalDate.now()
    val dateStr = today.format(DateTimeFormatter.ofPattern("yyyy/MM/dd (EEE)", Locale.JAPANESE))
    val completedCount = tasksWithStatus.count { it.isCompleted }
    val totalCount = tasksWithStatus.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    Box(modifier = Modifier
        .fillMaxSize()
        .background(N64Canvas)
        .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top nav bar (carbon slab) ──────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(N64Carbon)
                    .padding(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clickable { navController.popBackStack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "戻る",
                            tint = N64CanvasSoft,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Title
                    Text(
                        text = "毎日やること",
                        color = N64Gold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.weight(1f)
                    )

                    // Add button (amber chip)
                    Box(
                        modifier = Modifier
                            .height(28.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(N64Amber)
                            .clickable { showAddDialog = true }
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = N64Carbon,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                "ADD",
                                color = N64Carbon,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                // Chrome-indigo bevel line at bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .align(Alignment.BottomCenter)
                        .background(N64Chrome)
                )
            }

            // ── Sub-nav strip (pale sky) ───────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(N64CanvasSoft)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = dateStr,
                    color = N64Carbon,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            // ── Progress panel (periwinkle raised plate) ───────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(4.dp))
                        .background(N64Periwinkle, RoundedCornerShape(4.dp))
                        .border(1.dp, N64Chrome, RoundedCornerShape(4.dp))
                        .padding(10.dp)
                ) {
                    // Section label
                    Text(
                        text = "≡ TODAY'S PROGRESS",
                        color = N64Carbon,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Progress bar
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(N64MutedIndigo)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progress)
                                    .background(N64Signal, RoundedCornerShape(2.dp))
                            )
                        }
                        Text(
                            text = "$completedCount / $totalCount 完了",
                            color = N64OnPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ── Section label bar ─────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(N64Canvas)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .border(
                        width = 0.dp,
                        color = Color.Transparent
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "≡ CHECKLIST",
                        color = N64Carbon,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    if (totalCount > 0) {
                        Text(
                            text = "${totalCount}件",
                            color = N64InkSoft,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ── Task list ─────────────────────────────────────────────────
            if (tasksWithStatus.isEmpty()) {
                NintendoEmptyState(onAddClick = { showAddDialog = true })
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(tasksWithStatus, key = { it.task.id }) { item ->
                        NintendoTaskRow(
                            item = item,
                            onToggle = {
                                viewModel.toggleCompletion(item.task.id, item.isCompleted)
                            },
                            onDelete = { taskToDelete = item.task }
                        )
                        // dotted divider
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(N64MutedIndigo.copy(alpha = 0.4f))
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            // ── Add task button (submit style) ────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(N64Carbon)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(N64Signal)
                        .clickable { showAddDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = N64OnPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "タスクを追加する",
                            color = N64OnPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier
                .navigationBarsPadding()
                .height(0.dp))
        }
    }

    // ── Add task dialog ───────────────────────────────────────────────────
    if (showAddDialog) {
        NintendoAddTaskDialog(
            onConfirm = { title ->
                if (title.isNotBlank()) viewModel.addTask(title)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    // ── Delete confirm dialog ─────────────────────────────────────────────
    taskToDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            containerColor = N64Surface,
            title = {
                Text(
                    "DELETE TASK",
                    color = N64Carbon,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            },
            text = {
                Text(
                    "「${task.title}」を削除しますか？",
                    color = N64Carbon,
                    fontSize = 12.sp
                )
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFFe60012))
                        .clickable {
                            viewModel.deleteTask(task)
                            taskToDelete = null
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("削除", color = N64OnPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .background(N64Carbon)
                        .clickable { taskToDelete = null }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("キャンセル", color = N64OnPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun NintendoTaskRow(
    item: DailyTaskWithStatus,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(N64Platinum)
            .clickable { onToggle() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Completion indicator (round signal-orange badge)
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (item.isCompleted) N64Signal else N64MutedIndigo.copy(alpha = 0.3f))
                    .border(1.dp, if (item.isCompleted) N64Signal else N64MutedIndigo, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (item.isCompleted) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = N64OnPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Task title
            Text(
                text = item.task.title,
                color = if (item.isCompleted) N64InkSoft else N64Carbon,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Arrow chip (signal orange) or delete icon
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Arrow chip
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (item.isCompleted) N64Signal else N64Amber),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (item.isCompleted) Icons.Default.Check
                        else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = N64Carbon,
                        modifier = Modifier.size(12.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                // Delete
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clickable { onDelete() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "削除",
                        tint = N64MutedIndigo,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NintendoEmptyState(onAddClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(4.dp))
                    .background(N64CanvasSoft, RoundedCornerShape(4.dp))
                    .border(1.dp, N64Chrome, RoundedCornerShape(4.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "NO TASKS",
                        color = N64Chrome,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "毎日やることを追加してみましょう",
                        color = N64InkSoft,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(N64Signal)
                            .clickable { onAddClick() }
                            .size(44.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = N64OnPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NintendoAddTaskDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = N64Surface,
        shape = RoundedCornerShape(4.dp),
        title = {
            Text(
                "≡ ADD TASK",
                color = N64Carbon,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        },
        text = {
            Column {
                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(N64Platinum)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "タスク名",
                    color = N64Carbon,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    placeholder = { Text("例：歯磨き、運動、水を飲む", fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onConfirm(text) }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = N64Chrome,
                        unfocusedBorderColor = N64MutedIndigo,
                        focusedTextColor = N64Carbon,
                        unfocusedTextColor = N64Carbon
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (text.isNotBlank()) N64Signal else N64Platinum)
                    .clickable(enabled = text.isNotBlank()) { onConfirm(text) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    "追加",
                    color = if (text.isNotBlank()) N64OnPrimary else N64MutedIndigo,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        },
        dismissButton = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(2.dp))
                    .background(N64Carbon)
                    .clickable { onDismiss() }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    "キャンセル",
                    color = N64CanvasSoft,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}
