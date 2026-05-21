package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.CycleStatus
import com.example.ui.viewmodel.CycleViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

// Custom Cycle Color Palette
val SoftPeriodRed = Color(0xFF8C4A60) // Align to FrostedBrandAccent
val SoftFertilePurple = Color(0xFF9E70FD)
val SoftFollicularGreen = Color(0xFF4BD1A0)
val SoftLutealAmber = Color(0xFFFFB236)
val SubtleBlushBg = Color(0x73FFFFFF) // Light Glass Background
val SoftGray = Color(0x338C4A60)

@Composable
fun TrackerScreen(
    viewModel: CycleViewModel,
    modifier: Modifier = Modifier
) {
    val periodLogs by viewModel.periodLogs.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val status = viewModel.calculateCycleStatus()

    var activeMonth by remember { mutableStateOf(YearMonth.now()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            TrackerHeader(status = status)
        }

        // Circular Phase Progress Ring (The main glass card)
        item {
            CycleProgressRing(status = status)
        }

        // Hormone Trackers Profile
        item {
            HormoneGaugeCard(status = status)
        }

        // Action: Start/End period fast logs
        item {
            QuickLogPeriodController(
                status = status,
                selectedDate = selectedDate,
                onStartPeriod = { viewModel.startPeriod(selectedDate, "Medium") },
                onEndPeriod = { viewModel.endPeriod(selectedDate) }
            )
        }

        // Calendar component
        item {
            MonthCalendar(
                selectedDate = selectedDate,
                activeMonth = activeMonth,
                status = status,
                onDateSelected = { viewModel.selectDate(it) },
                onMonthChange = { activeMonth = it }
            )
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
fun TrackerHeader(status: CycleStatus) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Profile Icon wrapper
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(FrostedBrandAccent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Avatar",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            // Welcome & Status
            Column {
                Text(
                    text = "Hi, Elena",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkSlateText
                )
                Text(
                    text = "Day ${status.cycleDay} • ${status.phase}",
                    fontSize = 12.sp,
                    color = FrostedBrandAccent,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        IconButton(
            onClick = {},
            modifier = Modifier
                .clip(CircleShape)
                .background(LightGlassBg)
                .border(1.dp, LightGlassBorder, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = "Notifications",
                tint = DarkSlateText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun CycleProgressRing(status: CycleStatus) {
    val phaseColor = when (status.phase) {
        "Menstrual Phase" -> SoftPeriodRed
        "Follicular Phase" -> SoftFollicularGreen
        "Ovulatory Phase" -> SoftFertilePurple
        else -> SoftLutealAmber
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.15f)
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        // Decorative Background mesh gradient to establish high visual depth
        Box(
            modifier = Modifier
                .fillMaxSize(0.9f)
                .clip(RoundedCornerShape(32.dp))
                .background(
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(
                            FrostedGradientStart.copy(alpha = 0.35f),
                            FrostedGradientEnd.copy(alpha = 0.35f)
                        )
                    )
                )
        )

        // The Glass Circle Circular container
        Box(
            modifier = Modifier
                .size(232.dp)
                .clip(CircleShape)
                .background(LightGlassBg)
                .border(1.5.dp, LightGlassBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Draw accurate ring on edge of the glass sphere
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                // outer path
                drawArc(
                    color = Color.White.copy(alpha = 0.3f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8.dp.toPx())
                )
                // progressive sweep
                drawArc(
                    color = phaseColor,
                    startAngle = -90f,
                    sweepAngle = status.phaseProgress * 360f,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 8.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "NEXT PERIOD",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = FrostedBrandAccent,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${status.daysRemaining}",
                    fontSize = 68.sp,
                    fontWeight = FontWeight.Light,
                    color = DarkSlateText,
                    lineHeight = 68.sp
                )
                Text(
                    text = if (status.daysRemaining == 1) "Day" else "Days",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = WarmGrayBody
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(FrostedBrandAccent)
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when (status.phase) {
                            "Menstrual Phase" -> "Flow Active"
                            "Ovulatory Phase" -> "High Fertility"
                            "Follicular Phase" -> "High Energy"
                            else -> "Stable Luteal"
                        },
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun HormoneGaugeCard(status: CycleStatus) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, LightGlassBorder, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = LightGlassBg)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Hormonal Activity Predictions",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = DarkSlateText
            )

            // Estrogen bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Estrogen", fontSize = 13.sp, color = WarmGrayBody)
                    Text(
                        text = "${(status.estrogenLevel * 100).toInt()}%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftPeriodRed
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { status.estrogenLevel },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = SoftPeriodRed,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
            }

            // Progesterone bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Progesterone", fontSize = 13.sp, color = WarmGrayBody)
                    Text(
                        text = "${(status.progesteroneLevel * 100).toInt()}%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftFertilePurple
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { status.progesteroneLevel },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = SoftFertilePurple,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
fun QuickLogPeriodController(
    status: CycleStatus,
    selectedDate: LocalDate,
    onStartPeriod: () -> Unit,
    onEndPeriod: () -> Unit
) {
    val ongoingPeriod = status.phase == "Menstrual Phase"
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, LightGlassBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightGlassBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (ongoingPeriod) "Period currently active" else "Is period started today?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = DarkSlateText
                )
                Text(
                    text = "Selected Date: ${selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}",
                    fontSize = 13.sp,
                    color = WarmGrayBody
                )
            }

            if (ongoingPeriod) {
                Button(
                    onClick = onEndPeriod,
                    colors = ButtonDefaults.buttonColors(containerColor = FrostedBrandAccent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("end_period_button")
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Done")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "End Flow", fontWeight = FontWeight.Bold)
                }
            } else {
                OutlinedButton(
                    onClick = onStartPeriod,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = FrostedBrandAccent),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, FrostedBrandAccent),
                    modifier = Modifier.testTag("start_period_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Start Flow", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MonthCalendar(
    selectedDate: LocalDate,
    activeMonth: YearMonth,
    status: CycleStatus,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChange: (YearMonth) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, LightGlassBorder, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = LightGlassBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Calendar Month Navigation Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onMonthChange(activeMonth.minusMonths(1)) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous Month",
                        tint = DarkSlateText
                    )
                }

                Text(
                    text = activeMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + activeMonth.year,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = DarkSlateText
                )

                IconButton(onClick = { onMonthChange(activeMonth.plusMonths(1)) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next Month",
                        tint = DarkSlateText
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Weekday Headings (Sun, Mon, Tue, etc.)
            Row(modifier = Modifier.fillMaxWidth()) {
                val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarmGrayBody
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Days Grid
            val firstDayOfMonth = activeMonth.atDay(1)
            val totalDays = activeMonth.lengthOfMonth()
            val dayOfWeekOffset = firstDayOfMonth.dayOfWeek.value % 7 // Sunday is index 0 in our headers

            var currentDayNum = 1
            val weeksCount = Math.ceil((totalDays + dayOfWeekOffset) / 7.0).toInt()

            for (w in 0 until weeksCount) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (d in 0..6) {
                        val cellIndex = w * 7 + d
                        if (cellIndex < dayOfWeekOffset || currentDayNum > totalDays) {
                            Spacer(modifier = Modifier.weight(1f))
                        } else {
                            val thisDayNum = currentDayNum
                            val thisLocalDate = activeMonth.atDay(thisDayNum)
                            val isSelected = thisLocalDate == selectedDate

                            // Predictions highlight calculations
                            val isPredictedPeriod = isDateInPredictedPeriod(thisLocalDate, status.nextPeriodDate, status.fertileStart)
                            val isPredictedFertile = isDateInPredictedFertile(thisLocalDate, status.fertileStart, status.fertileEnd)

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(3.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> SoftPeriodRed
                                            isPredictedPeriod -> SoftPeriodRed.copy(alpha = 0.15f)
                                            isPredictedFertile -> SoftFertilePurple.copy(alpha = 0.15f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .clickable { onDateSelected(thisLocalDate) }
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$thisDayNum",
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 14.sp,
                                        color = when {
                                            isSelected -> Color.White
                                            isPredictedPeriod -> SoftPeriodRed
                                            isPredictedFertile -> SoftFertilePurple
                                            else -> DarkSlateText
                                        }
                                    )
                                    // Cute underlying tiny dot
                                    if (!isSelected && (isPredictedPeriod || isPredictedFertile)) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(if (isPredictedPeriod) SoftPeriodRed else SoftFertilePurple)
                                        )
                                    }
                                }
                            }
                            currentDayNum++
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Calendar Legend Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(SoftPeriodRed.copy(alpha = 0.2f))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Predicted Period", fontSize = 11.sp, color = Color.Gray)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(SoftFertilePurple.copy(alpha = 0.2f))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Fertile Window", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }
    }
}

// Predict matches safely
fun isDateInPredictedPeriod(date: LocalDate, nextPeriod: LocalDate, fertileStart: LocalDate): Boolean {
    // 5 days predicted period starting nextPeriod date
    val diff = ChronoUnit.DAYS.between(nextPeriod, date)
    return diff in 0..4
}

fun isDateInPredictedFertile(date: LocalDate, fertileStart: LocalDate, fertileEnd: LocalDate): Boolean {
    return !date.isBefore(fertileStart) && !date.isAfter(fertileEnd)
}
