package com.daymode.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.daymode.app.data.BlocksRepository
import com.daymode.app.data.HabitPeriod
import com.daymode.app.data.HabitsRepository
import com.daymode.app.data.SettingsRepository
import com.daymode.app.data.ThemeMode
import com.daymode.app.data.completedIdsForDate
import com.daymode.app.ui.legal.LegalDocId
import com.daymode.app.ui.legal.LegalDocumentScreen
import com.daymode.app.ui.onboarding.OnboardingScreen
import com.daymode.app.ui.screens.EveningScreen
import com.daymode.app.ui.screens.MorningScreen
import com.daymode.app.ui.screens.PomodoroScreen
import com.daymode.app.ui.screens.SettingsScreen
import com.daymode.app.ui.screens.TodayScreen
import com.daymode.app.ui.theme.DayModeTheme

private enum class MainDest(val route: String, val label: String) {
    Today("today", "Сегодня"),
    Morning("morning", "Утро"),
    Evening("evening", "Вечер"),
    Focus("focus", "Фокус"),
    Settings("settings", "Ещё"),
}

private object LegalRoutes {
    const val ABOUT = "legal/about"
    const val PRIVACY = "legal/privacy"
    const val TERMS = "legal/terms"

    fun forDoc(id: LegalDocId): String = when (id) {
        LegalDocId.ABOUT -> ABOUT
        LegalDocId.PRIVACY -> PRIVACY
        LegalDocId.TERMS -> TERMS
    }
}

@Composable
fun DayModeApp() {
    val appContext = LocalContext.current.applicationContext
    val viewModel: DayModeViewModel = viewModel(
        factory = DayModeViewModel.factory(
            SettingsRepository(appContext),
            BlocksRepository(appContext),
            HabitsRepository(appContext),
        ),
    )
    val settings by viewModel.settings.collectAsState()
    val blocks by viewModel.blocks.collectAsState()
    val habits by viewModel.habits.collectAsState()
    val completions by viewModel.completions.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    val completedForSelectedDay = remember(completions, selectedDate) {
        completedIdsForDate(completions, selectedDate)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    LaunchedEffect(settings?.onboardingDone) {
        if (settings?.onboardingDone != true) return@LaunchedEffect
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    DayModeTheme(themeMode = settings?.themeMode ?: ThemeMode.LIGHT) {
        Surface(modifier = Modifier.fillMaxSize()) {
            val loaded = settings
            if (loaded == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Surface
            }
            if (!loaded.onboardingDone) {
                OnboardingScreen(
                    initial = loaded,
                    onFinished = { viewModel.completeOnboarding(it) },
                )
                return@Surface
            }

            val navController = rememberNavController()
            val backStack by navController.currentBackStackEntryAsState()
            val currentRoute = backStack?.destination?.route

            val hideBottomBar = currentRoute?.startsWith("legal/") == true

            Scaffold(
                bottomBar = {
                    if (!hideBottomBar) {
                        NavigationBar {
                            MainDest.entries.forEach { dest ->
                                NavigationBarItem(
                                    selected = currentRoute == dest.route,
                                    onClick = {
                                        navController.navigate(dest.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = when (dest) {
                                                MainDest.Today -> Icons.Outlined.Today
                                                MainDest.Morning -> Icons.Outlined.WbSunny
                                                MainDest.Evening -> Icons.Outlined.Bedtime
                                                MainDest.Focus -> Icons.Outlined.Timer
                                                MainDest.Settings -> Icons.Outlined.Settings
                                            },
                                            contentDescription = dest.label,
                                        )
                                    },
                                    label = { Text(dest.label) },
                                )
                            }
                        }
                    }
                },
            ) { padding ->
                NavHost(
                    navController = navController,
                    startDestination = MainDest.Today.route,
                    modifier = Modifier.padding(padding),
                ) {
                    composable(MainDest.Today.route) {
                        TodayScreen(
                            settings = loaded,
                            blocks = blocks,
                            selectedDate = selectedDate,
                            onPreviousDay = { viewModel.shiftSelectedDate(-1) },
                            onNextDay = { viewModel.shiftSelectedDate(1) },
                            onResetToToday = viewModel::resetSelectedDateToToday,
                            onSaveSettings = viewModel::updateSettings,
                            onSaveBlock = viewModel::saveBlock,
                            onDeleteBlock = viewModel::deleteBlock,
                        )
                    }
                    composable(MainDest.Morning.route) {
                        MorningScreen(
                            habits = habits,
                            completions = completions,
                            selectedDate = selectedDate,
                            completedIdsForSelectedDay = completedForSelectedDay,
                            onPreviousDay = { viewModel.shiftSelectedDate(-1) },
                            onNextDay = { viewModel.shiftSelectedDate(1) },
                            onResetToToday = viewModel::resetSelectedDateToToday,
                            onAdd = { viewModel.addHabit(HabitPeriod.MORNING, it) },
                            onToggle = { viewModel.toggleHabit(it, selectedDate) },
                            onDelete = viewModel::deleteHabit,
                        )
                    }
                    composable(MainDest.Evening.route) {
                        EveningScreen(
                            settings = loaded,
                            habits = habits,
                            completions = completions,
                            selectedDate = selectedDate,
                            completedIdsForSelectedDay = completedForSelectedDay,
                            onPreviousDay = { viewModel.shiftSelectedDate(-1) },
                            onNextDay = { viewModel.shiftSelectedDate(1) },
                            onResetToToday = viewModel::resetSelectedDateToToday,
                            onAdd = { viewModel.addHabit(HabitPeriod.EVENING, it) },
                            onToggle = { viewModel.toggleHabit(it, selectedDate) },
                            onDelete = viewModel::deleteHabit,
                        )
                    }
                    composable(MainDest.Focus.route) {
                        PomodoroScreen()
                    }
                    composable(MainDest.Settings.route) {
                        SettingsScreen(
                            settings = loaded,
                            onThemeChange = viewModel::setTheme,
                            onOpenLegal = { doc ->
                                navController.navigate(LegalRoutes.forDoc(doc))
                            },
                        )
                    }
                    composable(LegalRoutes.ABOUT) {
                        LegalDocumentScreen(
                            docId = LegalDocId.ABOUT,
                            onBack = { navController.popBackStack() },
                        )
                    }
                    composable(LegalRoutes.PRIVACY) {
                        LegalDocumentScreen(
                            docId = LegalDocId.PRIVACY,
                            onBack = { navController.popBackStack() },
                        )
                    }
                    composable(LegalRoutes.TERMS) {
                        LegalDocumentScreen(
                            docId = LegalDocId.TERMS,
                            onBack = { navController.popBackStack() },
                        )
                    }
                }
            }
        }
    }
}
