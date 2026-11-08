package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryStatsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SkipGuideScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.VipViewModel

enum class AppScreen {
    DASHBOARD,
    HISTORY,
    GUIDE,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    private val viewModel: VipViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check if opened from notification for a specific membership
        val targetMembershipId = intent?.getLongExtra("EXTRA_MEMBERSHIP_ID", -1L) ?: -1L
        if (targetMembershipId != -1L) {
            // Find and set detail membership
            viewModel.allMemberships.value.find { it.id == targetMembershipId }?.let {
                viewModel.selectedMembershipForDetail.value = it
            }
        }

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }

                    when (currentScreen) {
                        AppScreen.DASHBOARD -> {
                            DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToHistory = { currentScreen = AppScreen.HISTORY },
                                onNavigateToGuide = { currentScreen = AppScreen.GUIDE },
                                onNavigateToSettings = { currentScreen = AppScreen.SETTINGS }
                            )
                        }

                        AppScreen.HISTORY -> {
                            BackHandler { currentScreen = AppScreen.DASHBOARD }
                            HistoryStatsScreen(
                                viewModel = viewModel,
                                onBack = { currentScreen = AppScreen.DASHBOARD }
                            )
                        }

                        AppScreen.GUIDE -> {
                            BackHandler { currentScreen = AppScreen.DASHBOARD }
                            SkipGuideScreen(
                                onBack = { currentScreen = AppScreen.DASHBOARD }
                            )
                        }

                        AppScreen.SETTINGS -> {
                            BackHandler { currentScreen = AppScreen.DASHBOARD }
                            SettingsScreen(
                                viewModel = viewModel,
                                onBack = { currentScreen = AppScreen.DASHBOARD }
                            )
                        }
                    }
                }
            }
        }
    }
}
