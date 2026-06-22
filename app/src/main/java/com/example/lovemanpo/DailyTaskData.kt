package com.example.lovemanpo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

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
