package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.CycleViewModel
import com.example.ui.theme.*
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogScreen(
    viewModel: CycleViewModel,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val dailyLog by viewModel.selectedDailyLog.collectAsState()
    val focusManager = LocalFocusManager.current

    // Screen-local symptom log inputs
    var flow by remember { mutableStateOf("None") }
    var selectedMood by remember { mutableStateOf("") }
    var selectedSymptoms by remember { mutableStateOf<List<String>>(emptyList()) }
    var tempInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }
    var waterMl by remember { mutableStateOf(0) }

    // Synchronize inputs when dailyLog loads or changes based on chosen date
    LaunchedEffect(dailyLog) {
        if (dailyLog != null) {
            flow = dailyLog!!.flow
            selectedMood = dailyLog!!.mood
            selectedSymptoms = if (dailyLog!!.symptoms.isEmpty()) emptyList() else dailyLog!!.symptoms.split(",")
            tempInput = dailyLog!!.temperature?.toString() ?: ""
            notesInput = dailyLog!!.notes
            waterMl = dailyLog!!.waterIntakeMl
        } else {
            // Reset to default empty state
            flow = "None"
            selectedMood = ""
            selectedSymptoms = emptyList()
            tempInput = ""
            notesInput = ""
            waterMl = 0
        }
    }

    val totalPredefinedSymptoms = listOf(
        "Cramps" to "💥",
        "Bloating" to "🎈",
        "Headache" to "🤕",
        "Backache" to "🩹",
        "Acne" to "✨",
        "Tender Breasts" to "🌸",
        "Fatigue" to "😴",
        "Nausea" to "🤢"
    )

    val moodsList = listOf(
        "Joyful" to "😊",
        "Calm" to "😌",
        "Anxious" to "😰",
        "Sad" to "😢",
        "Energetic" to "⚡",
        "Tired" to "😴"
    )

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
                text = "Log Symptoms",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = DarkSlateText
            )
            Text(
                text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")),
                fontSize = 15.sp,
                color = FrostedBrandAccent,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Section 1: Flow selector
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, LightGlassBorder, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = LightGlassBg)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Flow Intensity",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = DarkSlateText
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("None", "Light", "Medium", "Heavy").forEach { level ->
                            val isSelected = flow == level
                            val btnColor = if (isSelected) FrostedBrandAccent else Color.White.copy(alpha = 0.4f)
                            val textColor = if (isSelected) Color.White else DarkSlateText
                            val borderCol = if (isSelected) FrostedBrandAccent else LightGlassBorder

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(45.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(btnColor)
                                    .border(1.dp, borderCol, RoundedCornerShape(12.dp))
                                    .clickable { flow = level }
                                    .testTag("flow_$level")
                            ) {
                                Text(
                                    text = level,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Mood
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, LightGlassBorder, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = LightGlassBg)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Current Mood",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = DarkSlateText
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        moodsList.forEach { (moodName, emoji) ->
                            val isSelected = selectedMood == moodName
                            val cardBg = if (isSelected) FrostedBrandAccent.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.4f)
                            val borderCol = if (isSelected) FrostedBrandAccent else LightGlassBorder

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(cardBg)
                                    .border(1.dp, borderCol, RoundedCornerShape(16.dp))
                                    .clickable { selectedMood = if (isSelected) "" else moodName }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                    .testTag("mood_$moodName"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = emoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = moodName,
                                    fontSize = 13.sp,
                                    color = DarkSlateText,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Physical Symptoms list
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, LightGlassBorder, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = LightGlassBg)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Physical Symptoms",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = DarkSlateText
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        totalPredefinedSymptoms.forEach { (sympName, emoji) ->
                            val isSelected = selectedSymptoms.contains(sympName)
                            val cardBg = if (isSelected) SoftFertilePurple.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.4f)
                            val borderCol = if (isSelected) SoftFertilePurple else LightGlassBorder

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(cardBg)
                                    .border(1.dp, borderCol, RoundedCornerShape(16.dp))
                                    .clickable {
                                        selectedSymptoms = if (isSelected) {
                                            selectedSymptoms.filterNot { it == sympName }
                                        } else {
                                            selectedSymptoms + sympName
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("symptom_$sympName"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = emoji, fontSize = 15.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = sympName,
                                    fontSize = 13.sp,
                                    color = DarkSlateText,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 4: Water & Temperature metrics
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Basal Body Temperature
                Card(
                    modifier = Modifier
                        .weight(1.5f)
                        .border(1.dp, LightGlassBorder, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = LightGlassBg)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Thermostat,
                                contentDescription = "Temp",
                                tint = FrostedBrandAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Base Temp (°C)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = DarkSlateText
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = tempInput,
                            onValueChange = { tempInput = it },
                            placeholder = { Text("e.g. 36.6", fontSize = 12.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FrostedBrandAccent,
                                unfocusedBorderColor = LightGlassBorder,
                                focusedContainerColor = Color.White.copy(alpha = 0.5f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.3f),
                                focusedTextColor = DarkSlateText,
                                unfocusedTextColor = DarkSlateText
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("temp_text_field")
                        )
                    }
                }

                // Water Intake
                Card(
                    modifier = Modifier
                        .weight(1.5f)
                        .border(1.dp, LightGlassBorder, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = LightGlassBg)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalDrink,
                                contentDescription = "Water",
                                tint = SoftFertilePurple,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Water Log",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = DarkSlateText
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$waterMl ml",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkSlateText,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { if (waterMl >= 250) waterMl -= 250 else waterMl = 0 },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.5f))
                                    .border(1.dp, LightGlassBorder, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Minus",
                                    tint = DarkSlateText,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            IconButton(
                                onClick = { waterMl += 250 },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.5f))
                                    .border(1.dp, LightGlassBorder, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Plus",
                                    tint = DarkSlateText,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 5: Log Comments/Notes
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, LightGlassBorder, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = LightGlassBg)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Notes & Personal Thoughts",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = DarkSlateText
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        placeholder = { Text("How do you feel today? Any custom notes...", fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("notes_text_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FrostedBrandAccent,
                            unfocusedBorderColor = LightGlassBorder,
                            focusedContainerColor = Color.White.copy(alpha = 0.5f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.3f),
                            focusedTextColor = DarkSlateText,
                            unfocusedTextColor = DarkSlateText
                        )
                    )
                }
            }
        }

        // Save Button Action
        item {
            Button(
                onClick = {
                    focusManager.clearFocus()
                    val temp = tempInput.toDoubleOrNull()
                    viewModel.saveDailySymptomLog(
                        flow = flow,
                        mood = selectedMood,
                        symptoms = selectedSymptoms,
                        temperature = temp,
                        notes = notesInput,
                        waterIntakeMl = waterMl
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_log_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FrostedBrandAccent)
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = "Save", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Updates",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
