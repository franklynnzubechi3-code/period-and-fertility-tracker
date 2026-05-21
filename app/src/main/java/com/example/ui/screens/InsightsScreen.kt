package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SendTimeExtension
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.CycleViewModel
import com.example.ui.theme.*

@Composable
fun InsightsScreen(
    viewModel: CycleViewModel,
    modifier: Modifier = Modifier
) {
    val insight by viewModel.cycleInsight.collectAsState()
    val config by viewModel.cycleConfig.collectAsState()
    val isAILoading by viewModel.isAILoading.collectAsState()

    var reminderMsgInput by remember(config.notificationText) { mutableStateOf(config.notificationText) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Health Insights",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = DarkSlateText
            )
            Text(
                text = "AI recommendations & reminder controllers",
                fontSize = 14.sp,
                color = WarmGrayBody,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Section 1: Gemini AI Insights Report View container
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, LightGlassBorder, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = LightGlassBg)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI",
                                tint = FrostedBrandAccent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Aura AI Advisor",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkSlateText
                            )
                        }

                        if (isAILoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = FrostedBrandAccent,
                                strokeWidth = 2.dp
                            )
                        }
                    }

                    Text(
                        text = "Get expert level summaries containing predictions, wellness actions, diet support, and cycle forecasts tailored strictly to your symptom recordings.",
                        fontSize = 13.sp,
                        color = DarkSlateText.copy(alpha = 0.8f)
                    )

                    Button(
                        onClick = { viewModel.generateAICycleReport() },
                        enabled = !isAILoading,
                        colors = ButtonDefaults.buttonColors(containerColor = FrostedBrandAccent),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("refresh_ai_insights_button")
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI Sparkle", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isAILoading) "Analyzing Patterns..." else "Analyze Cycle with Gemini",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    insight?.let { ins ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.5f))
                                .border(1.dp, LightGlassBorder, RoundedCornerShape(16.dp))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                  ) {
                                    Text(
                                        text = "Analysis: ${ins.category}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SoftFertilePurple
                                    )
                                    Text(
                                        text = "Processed Live",
                                        fontSize = 11.sp,
                                        color = WarmGrayBody
                                    )
                                }
                                Text(
                                    text = ins.insightText,
                                    fontSize = 13.sp,
                                    color = DarkSlateText,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    } ?: Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.3f))
                            .border(1.dp, LightGlassBorder, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No clinical advice generated yet. Standard forecast calculations are active. Tap button to activate Gemini analysis.",
                            fontSize = 12.sp,
                            color = WarmGrayBody,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Section 2: Cycle Constants / Calibration Settings
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, LightGlassBorder, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = LightGlassBg)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = DarkSlateText,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cycle Calibration",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = DarkSlateText
                        )
                    }

                    // Average Cycle Length Incremental
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Cycle Length",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = DarkSlateText
                            )
                            Text(
                                text = "Standard interval from period start to start",
                                fontSize = 11.sp,
                                color = WarmGrayBody
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    if (config.averageCycleDays > 20) {
                                        viewModel.saveConfig(config.copy(averageCycleDays = config.averageCycleDays - 1))
                                    }
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.5f))
                                    .border(1.dp, LightGlassBorder, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Minus",
                                    tint = DarkSlateText
                                )
                            }

                            Text(
                                text = "${config.averageCycleDays} days",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkSlateText,
                                modifier = Modifier.width(60.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            IconButton(
                                onClick = {
                                    if (config.averageCycleDays < 45) {
                                        viewModel.saveConfig(config.copy(averageCycleDays = config.averageCycleDays + 1))
                                    }
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.5f))
                                    .border(1.dp, LightGlassBorder, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Plus",
                                    tint = DarkSlateText
                                )
                            }
                        }
                    }

                    // Average Period Length Incremental
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Period Flow Duration",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = DarkSlateText
                            )
                            Text(
                                text = "Typical flow length in days",
                                fontSize = 11.sp,
                                color = WarmGrayBody
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    if (config.averagePeriodDays > 3) {
                                        viewModel.saveConfig(config.copy(averagePeriodDays = config.averagePeriodDays - 1))
                                    }
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.5f))
                                    .border(1.dp, LightGlassBorder, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Minus",
                                    tint = DarkSlateText
                                )
                            }

                            Text(
                                text = "${config.averagePeriodDays} days",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkSlateText,
                                modifier = Modifier.width(60.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            IconButton(
                                onClick = {
                                    if (config.averagePeriodDays < 11) {
                                        viewModel.saveConfig(config.copy(averagePeriodDays = config.averagePeriodDays + 1))
                                    }
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.5f))
                                    .border(1.dp, LightGlassBorder, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Plus",
                                    tint = DarkSlateText
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Notification Alarms Manager Configuration
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, LightGlassBorder, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = LightGlassBg)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Reminders",
                                tint = FrostedBrandAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Daily App Reminders",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = DarkSlateText
                            )
                        }

                        Switch(
                            checked = config.remindersEnabled,
                            onCheckedChange = { viewModel.saveConfig(config.copy(remindersEnabled = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = FrostedBrandAccent,
                                uncheckedThumbColor = WarmGrayBody,
                                uncheckedTrackColor = Color.White.copy(alpha = 0.3f),
                                uncheckedBorderColor = LightGlassBorder
                            ),
                            modifier = Modifier.testTag("reminder_active_switch")
                        )
                    }

                    AnimatedVisibility(visible = config.remindersEnabled) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Reminders notification message:",
                                fontSize = 13.sp,
                                color = WarmGrayBody
                            )

                            OutlinedTextField(
                                value = reminderMsgInput,
                                onValueChange = {
                                    reminderMsgInput = it
                                    viewModel.saveConfig(config.copy(notificationText = it))
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("reminder_msg_input"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = FrostedBrandAccent,
                                    unfocusedBorderColor = LightGlassBorder,
                                    focusedContainerColor = Color.White.copy(alpha = 0.5f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.3f),
                                    focusedTextColor = DarkSlateText,
                                    unfocusedTextColor = DarkSlateText
                                )
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Button(
                                onClick = { viewModel.triggerInstantReminderNotification() },
                                colors = ButtonDefaults.buttonColors(containerColor = SoftFertilePurple),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("test_notification_button")
                            ) {
                                Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Sound Speaker", tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Test Active Notification Now", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
