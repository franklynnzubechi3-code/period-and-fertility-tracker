package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "period_logs")
data class PeriodLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startDate: String, // ISO Format "yyyy-MM-dd"
    val endDate: String?,  // null if the period is currently ongoing
    val intensity: String, // "Light", "Medium", "Heavy"
    val notes: String? = null
)

@Entity(tableName = "daily_logs")
data class DailyLog(
    @PrimaryKey val date: String, // ISO Format "yyyy-MM-dd"
    val flow: String = "None",    // "None", "Light", "Medium", "Heavy"
    val mood: String = "",        // e.g. "Happy", "Anxious", "Calm", "Tired", "Sad", "Energetic"
    val symptoms: String = "",    // Comma-separated list, e.g. "Cramps,Bloating,Headache"
    val temperature: Double? = null, // Basal body temperature
    val notes: String = "",
    val waterIntakeMl: Int = 0
)

@Entity(tableName = "cycle_configs")
data class CycleConfig(
    @PrimaryKey val id: Int = 1,
    val averageCycleDays: Int = 28,
    val averagePeriodDays: Int = 5,
    val remindersEnabled: Boolean = true,
    val reminderHour: Int = 9,
    val reminderMinute: Int = 0,
    val notificationText: String = "Daily log reminder: time to note down your symptoms."
)

@Entity(tableName = "cycle_insights")
data class CycleInsight(
    @PrimaryKey val id: Int = 1,
    val insightText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val category: String = "General"
)
