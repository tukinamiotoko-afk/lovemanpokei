package com.example.lovemanpo

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "steps")
data class StepRecord(
    @PrimaryKey val date: String,
    val stepCount: Int,
    val activeTimeMillis: Long = 0L
)

@Entity(tableName = "hourly_steps", primaryKeys = ["date", "hour"])
data class HourlyStepRecord(
    val date: String,
    val hour: Int,
    val stepCount: Int,
    val activeTimeMillis: Long = 0L
)

@Dao
interface StepDao {
    @Upsert
    suspend fun upsert(record: StepRecord)

    @Query("SELECT * FROM steps")
    fun getAllRecordsFlow(): Flow<List<StepRecord>>

    @Query("SELECT * FROM steps")
    suspend fun getAllRecords(): List<StepRecord>

    @Upsert
    suspend fun upsertHourly(record: HourlyStepRecord)

    @Query("SELECT * FROM hourly_steps WHERE date = :date")
    suspend fun getHourlyRecordsForDay(date: String): List<HourlyStepRecord>

    @Query("SELECT * FROM hourly_steps WHERE date = :date")
    fun getHourlyRecordsForDayFlow(date: String): Flow<List<HourlyStepRecord>>
}

@Database(entities = [StepRecord::class, HourlyStepRecord::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stepDao(): StepDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "love_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
