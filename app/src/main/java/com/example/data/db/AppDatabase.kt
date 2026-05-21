package com.example.data.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.data.model.CycleConfig
import com.example.data.model.CycleInsight
import com.example.data.model.DailyLog
import com.example.data.model.PeriodLog
import kotlinx.coroutines.flow.Flow

@Dao
interface PeriodLogDao {
    @Query("SELECT * FROM period_logs ORDER BY startDate DESC")
    fun getAllPeriodLogs(): Flow<List<PeriodLog>>

    @Query("SELECT * FROM period_logs WHERE endDate IS NULL LIMIT 1")
    suspend fun getOngoingPeriodLog(): PeriodLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPeriodLog(log: PeriodLog): Long

    @Update
    suspend fun updatePeriodLog(log: PeriodLog)

    @Delete
    suspend fun deletePeriodLog(log: PeriodLog)

    @Query("DELETE FROM period_logs WHERE id = :id")
    suspend fun deletePeriodLogById(id: Int)
}

@Dao
interface DailyLogDao {
    @Query("SELECT * FROM daily_logs ORDER BY date DESC")
    fun getAllDailyLogs(): Flow<List<DailyLog>>

    @Query("SELECT * FROM daily_logs WHERE date = :date LIMIT 1")
    fun getDailyLogForDate(date: String): Flow<DailyLog?>

    @Query("SELECT * FROM daily_logs WHERE date = :date LIMIT 1")
    suspend fun getDailyLogForDateNonFlow(date: String): DailyLog?

    @Query("SELECT * FROM daily_logs WHERE date IN (:dates)")
    fun getDailyLogsForDates(dates: List<String>): Flow<List<DailyLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyLog(log: DailyLog)

    @Delete
    suspend fun deleteDailyLog(log: DailyLog)
}

@Dao
interface CycleConfigDao {
    @Query("SELECT * FROM cycle_configs WHERE id = 1")
    fun getCycleConfigFlow(): Flow<CycleConfig?>

    @Query("SELECT * FROM cycle_configs WHERE id = 1")
    suspend fun getCycleConfigNonFlow(): CycleConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCycleConfig(config: CycleConfig)
}

@Dao
interface CycleInsightDao {
    @Query("SELECT * FROM cycle_insights WHERE id = 1")
    fun getCycleInsightFlow(): Flow<CycleInsight?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCycleInsight(insight: CycleInsight)
}

@Database(
    entities = [
        PeriodLog::class,
        DailyLog::class,
        CycleConfig::class,
        CycleInsight::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun periodLogDao(): PeriodLogDao
    abstract fun dailyLogDao(): DailyLogDao
    abstract fun cycleConfigDao(): CycleConfigDao
    abstract fun cycleInsightDao(): CycleInsightDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cycle_tracker_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
