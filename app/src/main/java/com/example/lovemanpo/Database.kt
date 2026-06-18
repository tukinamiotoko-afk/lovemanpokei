package com.example.lovemanpo

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String,
    val content: String,
    val gameDate: String,   // "YYYY-MM-DD" based on 5AM cutoff
    val timestamp: Long
)

@Dao
interface ChatMessageDao {
    @Insert
    suspend fun insert(message: ChatMessageEntity): Long

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    suspend fun getAll(): List<ChatMessageEntity>

    @Query("DELETE FROM chat_messages")
    suspend fun deleteAll()
}

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

@Database(entities = [StepRecord::class, HourlyStepRecord::class, ChatMessageEntity::class], version = 4)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stepDao(): StepDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS chat_messages " +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "role TEXT NOT NULL, content TEXT NOT NULL, " +
                    "gameDate TEXT NOT NULL, timestamp INTEGER NOT NULL)"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "love_db"
                )
                .addMigrations(MIGRATION_3_4)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
