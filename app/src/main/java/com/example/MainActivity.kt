package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.data.db.AppDatabase
import com.example.data.repository.CycleRepository
import com.example.ui.screens.InsightsScreen
import com.example.ui.screens.LogScreen
import com.example.ui.screens.TrackerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.FrostedBgColor
import com.example.ui.theme.FrostedBrandAccent
import com.example.ui.theme.WarmGrayBody
import com.example.ui.viewmodel.CycleViewModel
import com.example.ui.viewmodel.CycleViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Local dependency setup (Constructor-less simple injection pattern)
        val database = AppDatabase.getDatabase(this)
        val repository = CycleRepository(
            periodLogDao = database.periodLogDao(),
            dailyLogDao = database.dailyLogDao(),
            cycleConfigDao = database.cycleConfigDao(),
            cycleInsightDao = database.cycleInsightDao()
        )
        val factory = CycleViewModelFactory(application, repository)
        val viewModel = ViewModelProvider(this, factory)[CycleViewModel::class.java]

        setContent {
            MyApplicationTheme {
                MainAppContainer(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContainer(viewModel: CycleViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        TabInfo("Tracker", Icons.Default.CalendarMonth, "tracker_tab"),
        TabInfo("Daily Log", Icons.Default.EditNote, "log_tab"),
        TabInfo("Aura AI", Icons.Default.AutoAwesome, "insights_tab")
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = Color.White.copy(alpha = 0.9f),
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("bottom_nav_bar"),
                tonalElevation = 6.dp
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontSize = 11.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = FrostedBrandAccent,
                            selectedTextColor = FrostedBrandAccent,
                            indicatorColor = FrostedBrandAccent.copy(alpha = 0.12f),
                            unselectedIconColor = WarmGrayBody,
                            unselectedTextColor = WarmGrayBody
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(FrostedBgColor)
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> TrackerScreen(viewModel = viewModel)
                1 -> LogScreen(viewModel = viewModel)
                2 -> InsightsScreen(viewModel = viewModel)
            }
        }
    }
}

data class TabInfo(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val testTag: String
)
