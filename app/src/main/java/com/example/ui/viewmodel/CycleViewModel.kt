package com.example.ui.viewmodel

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.MainActivity
import com.example.R
import com.example.data.db.AppDatabase
import com.example.data.model.CycleConfig
import com.example.data.model.CycleInsight
import com.example.data.model.DailyLog
import com.example.data.model.PeriodLog
import com.example.data.repository.CycleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class CycleViewModel(
    private val application: Application,
    private val repository: CycleRepository
) : AndroidViewModel(application) {

    // Global Date Formatter
    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // Database UI States
    val periodLogs: StateFlow<List<PeriodLog>> = repository.allPeriodLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyLogs: StateFlow<List<DailyLog>> = repository.allDailyLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cycleConfig: StateFlow<CycleConfig> = repository.cycleConfig
        .map { it ?: CycleConfig() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            CycleConfig() // default configuration
        )

    val cycleInsight: StateFlow<CycleInsight?> = repository.cycleInsight
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current date selected inside the Log/Calendar UI
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    // Active daily log for the selected date
    private val _selectedDailyLog = MutableStateFlow<DailyLog?>(null)
    val selectedDailyLog: StateFlow<DailyLog?> = _selectedDailyLog.asStateFlow()

    // Loading states for AI Insights
    private val _isAILoading = MutableStateFlow(false)
    val isAILoading: StateFlow<Boolean> = _isAILoading.asStateFlow()

    init {
        // Observe selectedDate and pull corresponding daily logs
        viewModelScope.launch {
            selectedDate.collect { dt ->
                val dateStr = dt.format(dateFormatter)
                _selectedDailyLog.value = repository.getDailyLogForDateNonFlow(dateStr)
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        viewModelScope.launch {
            val dateStr = date.format(dateFormatter)
            _selectedDailyLog.value = repository.getDailyLogForDateNonFlow(dateStr)
        }
    }

    // Insert or toggle a period
    fun startPeriod(startDate: LocalDate, intensity: String) {
        viewModelScope.launch {
            val dateStr = startDate.format(dateFormatter)
            // check if there's already an ongoing period
            val ongoing = repository.getOngoingPeriodLog()
            if (ongoing == null) {
                repository.insertPeriodLog(
                    PeriodLog(startDate = dateStr, endDate = null, intensity = intensity)
                )
            } else {
                // If there's an ongoing period, update its endDate or don't double start
            }
        }
    }

    fun endPeriod(endDate: LocalDate) {
        viewModelScope.launch {
            val ongoing = repository.getOngoingPeriodLog()
            if (ongoing != null) {
                val endDateStr = endDate.format(dateFormatter)
                // make sure endDate is after or equal to startDate
                val start = LocalDate.parse(ongoing.startDate, dateFormatter)
                if (!endDate.isBefore(start)) {
                    repository.updatePeriodLog(ongoing.copy(endDate = endDateStr))
                }
            }
        }
    }

    fun deletePeriod(log: PeriodLog) {
        viewModelScope.launch {
            repository.deletePeriodLog(log)
        }
    }

    // Save Daily Symptoms Log
    fun saveDailySymptomLog(
        flow: String,
        mood: String,
        symptoms: List<String>,
        temperature: Double?,
        notes: String,
        waterIntakeMl: Int
    ) {
        viewModelScope.launch {
            val dateStr = selectedDate.value.format(dateFormatter)
            val log = DailyLog(
                date = dateStr,
                flow = flow,
                mood = mood,
                symptoms = symptoms.joinToString(","),
                temperature = temperature,
                notes = notes,
                waterIntakeMl = waterIntakeMl
            )
            repository.insertDailyLog(log)
            _selectedDailyLog.value = log
        }
    }

    // Save Configuration
    fun saveConfig(config: CycleConfig) {
        viewModelScope.launch {
            repository.saveCycleConfig(config)
        }
    }

    // Trigger AI report request via Gemini REST API
    fun generateAICycleReport() {
        viewModelScope.launch {
            _isAILoading.value = true
            try {
                val config = cycleConfig.value
                val periods = periodLogs.value.take(4)
                val recentLogs = dailyLogs.value.take(15)

                val cycleDaysStr = "${config.averageCycleDays} days"
                val periodDaysStr = "${config.averagePeriodDays} days"

                val periodsFormatted = periods.joinToString("; ") { p ->
                    "Start: ${p.startDate}, End: ${p.endDate ?: "Ongoing"} (Flow: ${p.intensity})"
                }.ifEmpty { "No period logs recorded yet." }

                val logsFormatted = recentLogs.joinToString("; ") { l ->
                    "Date ${l.date}: Flow=${l.flow}, Mood=${l.mood}, Symptoms=${l.symptoms}, Temp=${l.temperature ?: "N/A"}C, Notes='${l.notes}'"
                }.ifEmpty { "No symptoms logged recently." }

                val prompt = """
                    Write a clinical, encouraging menstrual and fertility profile based on this data:
                    - Est. average cycle length: $cycleDaysStr
                    - Est. average period length: $periodDaysStr
                    
                    Logged Cycle Events:
                    $periodsFormatted
                    
                    Recent Daily Status Logs:
                    $logsFormatted
                    
                    Please format your advice specifically with headers, bullet points, and clean space. Analyze:
                    1. Active Cycle Phase & Estrogen/Progesterone tracking predictions.
                    2. Customized Lifestyle & Fitness Tips (Is high-intensity exercise recommended right now?).
                    3. Diet & Energy supportive foods (what foods would help soothe active symptoms?).
                    4. Fertile window predictions & pregnancy planning/avoidance notes based on their current phase.
                    
                    Always formulate with warmth, empathy, and medically clean language.
                """.trimIndent()

                repository.fetchAndSaveGeminiInsight(prompt)
            } catch (e: Exception) {
                repository.saveCycleInsight(
                    CycleInsight(
                        insightText = "Error compiling AI report: ${e.localizedMessage}. Please try again later.",
                        category = "System Error"
                    )
                )
            } finally {
                _isAILoading.value = false
            }
        }
    }

    // Helper: System Notification Creator (for Reminders simulation or testing)
    fun triggerInstantReminderNotification() {
        val context = application.applicationContext
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "cycle_reminder_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(
                channelId,
                "Cycle Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily reminders for log logging and fertility updates."
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(mChannel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val config = cycleConfig.value
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Safe system icon
            .setContentTitle("My Cycle Tracker Active Reminder")
            .setContentText(config.notificationText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }

    // Math Functions: Dynamic Cycle Metrics Calculations based on database
    fun calculateCycleStatus(): CycleStatus {
        val periods = periodLogs.value
        val config = cycleConfig.value

        if (periods.isEmpty()) {
            return CycleStatus(
                phase = "Onboarding",
                phaseProgress = 0.5f,
                daysRemaining = config.averageCycleDays,
                cycleDay = 1,
                ovulationDate = LocalDate.now().plusDays(14),
                fertileStart = LocalDate.now().plusDays(10),
                fertileEnd = LocalDate.now().plusDays(15),
                nextPeriodDate = LocalDate.now().plusDays(config.averageCycleDays.toLong()),
                estrogenLevel = 0.2f,
                progesteroneLevel = 0.2f,
                description = "Log your period to unlock precision fertility and cycle charting."
            )
        }

        // Get the latest period starting date
        val latestLog = periods.first()
        val latestStart = try {
            LocalDate.parse(latestLog.startDate, dateFormatter)
        } catch (e: Exception) {
            LocalDate.now()
        }

        val today = LocalDate.now()
        val daysSincePeriodStart = ChronoUnit.DAYS.between(latestStart, today).toInt()

        // Normalize cycle day to fit averageCycleDays
        val cycleLen = config.averageCycleDays
        val currentCycleDay = (daysSincePeriodStart % cycleLen) + 1

        val nextPeriod = latestStart.plusDays(cycleLen.toLong())
        val daysToNextPeriod = ChronoUnit.DAYS.between(today, nextPeriod).toInt()

        // Predict ovulation and fertile range
        val pOvulation = nextPeriod.minusDays(14)
        val pFertileStart = pOvulation.minusDays(5)
        val pFertileEnd = pOvulation.plusDays(1)

        // Calculate phase name and hormonal curve estimates for visual aesthetics
        val phase: String
        val progress: Float
        val estrogen: Float
        val progesterone: Float
        val phaseDesc: String

        val periodDays = config.averagePeriodDays

        when {
            currentCycleDay in 1..periodDays -> {
                phase = "Menstrual Phase"
                progress = currentCycleDay.toFloat() / periodDays
                estrogen = 0.1f + (progress * 0.1f) // low, slowly rising
                progesterone = 0.05f
                phaseDesc = "Estrogen is low. Focus on rest, hydration, and nurturing ingredients like warm soups."
            }
            currentCycleDay in (periodDays + 1)..11 -> {
                phase = "Follicular Phase"
                val phaseLen = 11 - periodDays
                progress = (currentCycleDay - periodDays).toFloat() / phaseLen
                estrogen = 0.2f + (progress * 0.6f) // rising steeply
                progesterone = 0.1f
                phaseDesc = "Estrogen is surging! Your physical energy is rising. Great time for cardio and planning."
            }
            currentCycleDay in 12..16 -> {
                phase = "Ovulatory Phase"
                progress = (currentCycleDay - 11).toFloat() / 5
                estrogen = 0.8f + (1f - Math.abs(currentCycleDay - 14) / 3f) * 0.2f // peak on day 14
                progesterone = 0.2f + (progress * 0.3f)
                phaseDesc = "Your Peak Fertile Window! Estrogen peaks, triggering ovulation. Energy is at its peak."
            }
            else -> {
                phase = "Luteal Phase"
                val lutealLen = cycleLen - 16
                progress = (currentCycleDay - 16).toFloat() / lutealLen
                estrogen = 0.4f * (1f - progress) // drops
                progesterone = 0.8f * (1f - Math.abs(progress - 0.5f) * 2f).coerceAtLeast(0.1f) // peaks mid-phase
                phaseDesc = "Progesterone peaks. You may feel calmer or experience PMS. Gentle workouts and fiber-rich meals help."
            }
        }

        return CycleStatus(
            phase = phase,
            phaseProgress = progress.coerceIn(0f, 1f),
            daysRemaining = if (daysToNextPeriod < 0) 0 else daysToNextPeriod,
            cycleDay = currentCycleDay,
            ovulationDate = pOvulation,
            fertileStart = pFertileStart,
            fertileEnd = pFertileEnd,
            nextPeriodDate = nextPeriod,
            estrogenLevel = estrogen,
            progesteroneLevel = progesterone,
            description = phaseDesc
        )
    }
}

// Data holder for live UI status representation
data class CycleStatus(
    val phase: String,
    val phaseProgress: Float,
    val daysRemaining: Int,
    val cycleDay: Int,
    val ovulationDate: LocalDate,
    val fertileStart: LocalDate,
    val fertileEnd: LocalDate,
    val nextPeriodDate: LocalDate,
    val estrogenLevel: Float,
    val progesteroneLevel: Float,
    val description: String
)

// ViewModel Factory
class CycleViewModelFactory(
    private val application: Application,
    private val repository: CycleRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CycleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CycleViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
