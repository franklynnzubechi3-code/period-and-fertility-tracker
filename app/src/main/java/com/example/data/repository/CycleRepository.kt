package com.example.data.repository

import com.example.BuildConfig
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import com.example.data.db.CycleConfigDao
import com.example.data.db.CycleInsightDao
import com.example.data.db.DailyLogDao
import com.example.data.db.PeriodLogDao
import com.example.data.model.CycleConfig
import com.example.data.model.CycleInsight
import com.example.data.model.DailyLog
import com.example.data.model.PeriodLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CycleRepository(
    private val periodLogDao: PeriodLogDao,
    private val dailyLogDao: DailyLogDao,
    private val cycleConfigDao: CycleConfigDao,
    private val cycleInsightDao: CycleInsightDao
) {
    // Flows API for reactive UI updates
    val allPeriodLogs: Flow<List<PeriodLog>> = periodLogDao.getAllPeriodLogs()
    val allDailyLogs: Flow<List<DailyLog>> = dailyLogDao.getAllDailyLogs()
    val cycleConfig: Flow<CycleConfig?> = cycleConfigDao.getCycleConfigFlow()
    val cycleInsight: Flow<CycleInsight?> = cycleInsightDao.getCycleInsightFlow()

    fun getDailyLogForDate(date: String): Flow<DailyLog?> = dailyLogDao.getDailyLogForDate(date)

    suspend fun getDailyLogForDateNonFlow(date: String): DailyLog? = withContext(Dispatchers.IO) {
        dailyLogDao.getDailyLogForDateNonFlow(date)
    }

    suspend fun getOngoingPeriodLog(): PeriodLog? = withContext(Dispatchers.IO) {
        periodLogDao.getOngoingPeriodLog()
    }

    suspend fun getCycleConfigNonFlow(): CycleConfig? = withContext(Dispatchers.IO) {
        cycleConfigDao.getCycleConfigNonFlow()
    }

    // Insertions and mutations
    suspend fun insertPeriodLog(log: PeriodLog): Long = withContext(Dispatchers.IO) {
        periodLogDao.insertPeriodLog(log)
    }

    suspend fun updatePeriodLog(log: PeriodLog) = withContext(Dispatchers.IO) {
        periodLogDao.updatePeriodLog(log)
    }

    suspend fun deletePeriodLog(log: PeriodLog) = withContext(Dispatchers.IO) {
        periodLogDao.deletePeriodLog(log)
    }

    suspend fun deletePeriodLogById(id: Int) = withContext(Dispatchers.IO) {
        periodLogDao.deletePeriodLogById(id)
    }

    suspend fun insertDailyLog(log: DailyLog) = withContext(Dispatchers.IO) {
        dailyLogDao.insertDailyLog(log)
    }

    suspend fun deleteDailyLog(log: DailyLog) = withContext(Dispatchers.IO) {
        dailyLogDao.deleteDailyLog(log)
    }

    suspend fun saveCycleConfig(config: CycleConfig) = withContext(Dispatchers.IO) {
        cycleConfigDao.saveCycleConfig(config)
    }

    suspend fun saveCycleInsight(insight: CycleInsight) = withContext(Dispatchers.IO) {
        cycleInsightDao.saveCycleInsight(insight)
    }

    // Remote integration with Gemini AI
    suspend fun fetchAndSaveGeminiInsight(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            val placeholder = """
                🌸 **Cycle Insight Demo Mode**
                Please enter a valid Gemini API Key in your AI Studio secrets to enable real AI insights!

                🔹 **Current Cycle Status**:
                Based on your logged period start date, you are likely in your **Follicular Phase**. Essential estrogen hormone levels are rising. This phase typically supports higher energy levels and creativity.

                🔹 **Hormonal Recommendations**:
                - **Nutrition**: Incorporate lean proteins, physical energy-supporting carbohydrates (like oats and quinoa), and fermented foods to boost digestive metabolism.
                - **Physical Activity**: Great time for intense workouts, strength training, or high-intensity interval training (HIIT), as you may feel a natural surge of energy!
                - **Fertility & Planning**: Your fertility is transitioning from low to medium towards ovulation. Monitor physical symptoms (like cervical mucus changes) if planning or avoiding pregnancy.
                
                *Please note: This is a demo placeholder. Add API Key secrets for live expert analysis.*
            """.trimIndent()
            saveCycleInsight(CycleInsight(insightText = placeholder, category = "Sample Info"))
            return@withContext placeholder
        }

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            systemInstruction = Content(
                parts = listOf(Part(text = "You are a professional, highly supporting, clinical menstrual cycle and fertility health advisor. Generate useful health forecasts, lifestyle and diet guidance, emotional and physical advice, and safety highlights tailored to the user's logged period cycles and symptoms. Keep suggestions encouraging, beautifully structured with bullet-points and dividers, and strictly split into sections like Phase Profile, Personalized Wellness Action, Dietary Suggestions, and Fertility Window Guidance. Avoid using diagnostic medical absolute claims; use constructive recommendations instead."))
            )
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val insight = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "No valid advice response from Gemini API model. Please try logging more updates."
            
            saveCycleInsight(CycleInsight(insightText = insight, category = "Personalized AI Insights"))
            insight
        } catch (e: Exception) {
            val errorMsg = "Unable to connect to Gemini API: ${e.message ?: "Unknown Connection Error"}. Showing offline predictions instead."
            saveCycleInsight(CycleInsight(insightText = errorMsg, category = "Error Cache"))
            errorMsg
        }
    }
}
