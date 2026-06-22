package com.example.lovemanpo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Entity(tableName = "daily_tasks")
data class DailyTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val sortOrder: Int = 0
)

@Entity(tableName = "daily_task_completions", primaryKeys = ["taskId", "date"])
data class DailyTaskCompletion(
    val taskId: Long,
    val date: String
)

data class DailyTaskWithStatus(
    val task: DailyTask,
    val isCompleted: Boolean
)

enum class StatPeriod(val label: String) {
    WEEK7("7日"),
    MONTH30("30日"),
    ALL_TIME("全期間"),
    CUSTOM("任意")
}

data class TaskExecutionRate(
    val task: DailyTask,
    val completedDays: Int,
    val totalDays: Int,
    val rate: Float
)

@Dao
interface DailyTaskDao {
    @Query("SELECT * FROM daily_tasks ORDER BY sortOrder ASC, id ASC")
    fun getAllTasksFlow(): Flow<List<DailyTask>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTask(task: DailyTask): Long

    @Delete
    suspend fun deleteTask(task: DailyTask)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markComplete(completion: DailyTaskCompletion)

    @Query("DELETE FROM daily_task_completions WHERE taskId = :taskId AND date = :date")
    suspend fun markIncomplete(taskId: Long, date: String)

    @Query("SELECT taskId FROM daily_task_completions WHERE date = :date")
    fun getCompletedTaskIdsForDateFlow(date: String): Flow<List<Long>>

    @Query("SELECT COUNT(*) FROM daily_task_completions WHERE taskId = :taskId AND date >= :startDate AND date <= :endDate")
    suspend fun getCompletionCountInRange(taskId: Long, startDate: String, endDate: String): Int

    @Query("SELECT MIN(date) FROM daily_task_completions WHERE taskId = :taskId")
    suspend fun getFirstCompletionDate(taskId: Long): String?

    @Query("SELECT MIN(date) FROM daily_task_completions")
    suspend fun getEarliestCompletionDate(): String?
}

class DailyTaskViewModel(private val dao: DailyTaskDao) : ViewModel() {
    private val today = LocalDate.now().toString()

    val tasksWithStatus = dao.getAllTasksFlow()
        .combine(dao.getCompletedTaskIdsForDateFlow(today)) { tasks, completedIds ->
            tasks.map { task ->
                DailyTaskWithStatus(task, completedIds.contains(task.id))
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Stats state
    private val _statPeriod = MutableStateFlow(StatPeriod.WEEK7)
    val statPeriod: StateFlow<StatPeriod> = _statPeriod.asStateFlow()

    private val _customStart = MutableStateFlow<LocalDate>(LocalDate.now().minusDays(6))
    val customStart: StateFlow<LocalDate> = _customStart.asStateFlow()

    private val _customEnd = MutableStateFlow<LocalDate>(LocalDate.now())
    val customEnd: StateFlow<LocalDate> = _customEnd.asStateFlow()

    private val _executionRates = MutableStateFlow<List<TaskExecutionRate>>(emptyList())
    val executionRates: StateFlow<List<TaskExecutionRate>> = _executionRates.asStateFlow()

    fun setStatPeriod(period: StatPeriod) {
        _statPeriod.value = period
        loadExecutionRates()
    }

    fun setCustomRange(start: LocalDate, end: LocalDate) {
        _customStart.value = start
        _customEnd.value = end
        if (_statPeriod.value == StatPeriod.CUSTOM) loadExecutionRates()
    }

    fun loadExecutionRates() {
        viewModelScope.launch {
            val todayDate = LocalDate.now()
            val tasks = dao.getAllTasksFlow().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList()).value
            if (tasks.isEmpty()) { _executionRates.value = emptyList(); return@launch }

            val rates = tasks.map { task ->
                val (startDate, endDate, totalDays) = when (_statPeriod.value) {
                    StatPeriod.WEEK7 -> {
                        val start = todayDate.minusDays(6)
                        Triple(start.toString(), todayDate.toString(), 7L)
                    }
                    StatPeriod.MONTH30 -> {
                        val start = todayDate.minusDays(29)
                        Triple(start.toString(), todayDate.toString(), 30L)
                    }
                    StatPeriod.ALL_TIME -> {
                        val firstDate = dao.getFirstCompletionDate(task.id)
                            ?.let { LocalDate.parse(it) }
                            ?: todayDate
                        val days = ChronoUnit.DAYS.between(firstDate, todayDate) + 1
                        Triple(firstDate.toString(), todayDate.toString(), days)
                    }
                    StatPeriod.CUSTOM -> {
                        val start = _customStart.value
                        val end = _customEnd.value.let { if (it.isAfter(todayDate)) todayDate else it }
                        val days = ChronoUnit.DAYS.between(start, end) + 1
                        Triple(start.toString(), end.toString(), days.coerceAtLeast(1))
                    }
                }
                val completed = dao.getCompletionCountInRange(task.id, startDate, endDate)
                val total = totalDays.toInt().coerceAtLeast(1)
                TaskExecutionRate(
                    task = task,
                    completedDays = completed,
                    totalDays = total,
                    rate = completed.toFloat() / total
                )
            }
            _executionRates.value = rates
        }
    }

    fun addTask(title: String) {
        viewModelScope.launch {
            dao.insertTask(DailyTask(title = title))
        }
    }

    fun deleteTask(task: DailyTask) {
        viewModelScope.launch {
            dao.deleteTask(task)
        }
    }

    fun toggleCompletion(taskId: Long, isCurrentlyCompleted: Boolean) {
        viewModelScope.launch {
            if (isCurrentlyCompleted) {
                dao.markIncomplete(taskId, today)
            } else {
                dao.markComplete(DailyTaskCompletion(taskId, today))
            }
        }
    }
}

class DailyTaskViewModelFactory(private val dao: DailyTaskDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DailyTaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DailyTaskViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}
