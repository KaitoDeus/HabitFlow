package com.habitflow.app
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habitflow.app.feature.statistics.StatisticsViewModel
import com.habitflow.app.feature.statistics.StatisticsViewModelFactory
import com.habitflow.app.feature.statistics.StatisticsScreen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { HabitFlowApp(viewModel) }
    }
}

@Composable
fun HabitFlowApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    val settingsViewModel: SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(UserPreferencesDataSource(context.applicationContext)) as T
            }
        }
    )
    val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    
    var hasShownGreeting by remember { mutableStateOf(false) }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        settingsViewModel.onNotificationToggled(granted)
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33) {
            val isGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!isGranted) {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(settingsUiState) {
        if (!hasShownGreeting && settingsUiState is SettingsUiState.Success) {
            val prefs = (settingsUiState as SettingsUiState.Success).userPreferences
            if (prefs.greetingMessage.isNotBlank()) {
                android.widget.Toast.makeText(context, prefs.greetingMessage, android.widget.Toast.LENGTH_SHORT).show()
                hasShownGreeting = true
            }
        }
    }

    val isDarkTheme = when (val state = settingsUiState) {
        is SettingsUiState.Success -> {
            when (state.userPreferences.appTheme) {
                AppTheme.DARK -> true
                AppTheme.LIGHT -> false
                AppTheme.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
            }
        }
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    val colorTheme = when (val state = settingsUiState) {
        is SettingsUiState.Success -> state.userPreferences.colorTheme
        else -> AppColorTheme.GREEN
    }

    val colorScheme = when {
        colorTheme == AppColorTheme.DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDarkTheme -> {
            val (prim, primCont, onPrimCont) = when (colorTheme) {
                AppColorTheme.BLUE -> Triple(Color(0xFF00E5FF), Color(0xFF004D61), Color(0xFFB8EAFF))
                AppColorTheme.PURPLE -> Triple(Color(0xFFD0BCFF), Color(0xFF4F378B), Color(0xFFEADDFF))
                AppColorTheme.ORANGE -> Triple(Color(0xFFFF9100), Color(0xFF5B3000), Color(0xFFFFDCC2))
                else -> Triple(Color(0xFF39FF14), Color(0xFF1B3D06), Color(0xFFC3FFB2))
            }
            darkColorScheme(
                primary = prim,
                onPrimary = Color.Black,
                primaryContainer = primCont,
                onPrimaryContainer = onPrimCont,
                secondaryContainer = Color(0xFF2C2C2E),
                onSecondaryContainer = Color.White,
                tertiaryContainer = Color(0xFF3D2F06),
                onTertiaryContainer = Color(0xFFFFE1AC),
                errorContainer = Color(0xFF420B0B),
                onErrorContainer = Color(0xFFFFDAD6),
                surface = Color(0xFF0F0F0F),
                onSurface = Color.White,
                surfaceVariant = Color(0xFF252525),
                onSurfaceVariant = Color(0xFFCACACA)
            )
        }
        else -> {
            val (prim, primCont, onPrimCont) = when (colorTheme) {
                AppColorTheme.BLUE -> Triple(Color(0xFF00687A), Color(0xFF9EEFFE), Color(0xFF001F26))
                AppColorTheme.PURPLE -> Triple(Color(0xFF6750A4), Color(0xFFEADDFF), Color(0xFF21005D))
                AppColorTheme.ORANGE -> Triple(Color(0xFF944A00), Color(0xFFFFDCC2), Color(0xFF301400))
                else -> Triple(Color(0xFF2E7D32), Color(0xFFC8E6C9), Color(0xFF00390A))
            }
            lightColorScheme(
                primary = prim,
                primaryContainer = primCont,
                onPrimaryContainer = onPrimCont,
                secondaryContainer = Color(0xFFF0F0F0),
                tertiaryContainer = Color(0xFFFFF9C4),
                errorContainer = Color(0xFFFFDAD6)
            )
        }
    }

    val view = androidx.compose.ui.platform.LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (context as? android.app.Activity)?.window
            if (window != null) {
                androidx.core.view.WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkTheme
            }
        }
    }

    MaterialTheme(colorScheme = colorScheme) {
        val navItems = listOf(
            NavItem("✓", "Hôm nay"),
            NavItem("◎", "Thói quen"),
            NavItem("◆", "Mục tiêu"),
            NavItem("▥", "Thống kê"),
            NavItem("⚙", "Cài đặt")
        )
        // Quản lý Pager State cho vuốt ngang
        val pagerState = androidx.compose.foundation.pager.rememberPagerState(
            initialPage = 0,
            pageCount = { navItems.size }
        )
        val coroutineScope = rememberCoroutineScope()
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    shadowElevation = 10.dp
                ) {
                    NavigationBar(
                        modifier = Modifier.navigationBarsPadding(),
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp
                    ) {
                        navItems.forEachIndexed { index, item ->
                            NavigationBarItem(
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                                icon = {
                                    Text(
                                        item.icon,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                label = { Text(item.label, maxLines = 1) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            val isHapticEnabled = (settingsUiState as? SettingsUiState.Success)?.userPreferences?.isHapticEnabled ?: true

            androidx.compose.foundation.pager.HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) { page ->
                when (page) {
                    0 -> TodayScreen(
                        vm = viewModel,
                        isHapticEnabled = isHapticEnabled,
                        onNavigateToHabits = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        }
                    )
                    1 -> HabitsScreen(viewModel)
                    2 -> GoalsScreen(viewModel)
                    3 -> StatisticsScreen(viewModel, onNavigateToToday = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    })
                    else -> SettingsScreen(
                        viewModel = settingsViewModel,
                        mainViewModel = viewModel
                    )
                }
            }
        }
    }
}
data class NavItem(val icon: String, val label: String)

@Composable
fun ScreenHeader(
    eyebrow: String,
    title: String,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Column(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(eyebrow, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            }
            trailing?.invoke()
        }
        subtitle?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TodayScreen(
    vm: MainViewModel,
    isHapticEnabled: Boolean = true,
    onNavigateToHabits: () -> Unit
) {
    val habits by vm.habits.collectAsStateWithLifecycle()
    val occurrences by vm.occurrences.collectAsStateWithLifecycle()
    val userStats by vm.userStats.collectAsStateWithLifecycle()
    val testOffset by vm.testDateOffset.collectAsStateWithLifecycle()
    var showLevelDetail by remember { mutableStateOf(false) }

    val haptic = LocalHapticFeedback.current
    var prevLevel by remember { mutableStateOf<Int?>(null) }
    var prevCompletedAll by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }

    val today = remember(testOffset) { LocalDate.now().plusDays(testOffset) }
    val todayEpochDay = remember(today) { today.toEpochDay() }
    val currentDayOfWeek = remember(today) { today.dayOfWeek.value }
    val tomorrow = remember(today) { today.plusDays(1) }
    val tomorrowDayOfWeek = remember(tomorrow) { tomorrow.dayOfWeek.value }

    val filteredHabits = remember(habits, currentDayOfWeek) {
        habits.filter { it.scheduledDays.isEmpty() || it.scheduledDays.split(",").contains(currentDayOfWeek.toString()) }
    }
    val tomorrowHabits = remember(habits, tomorrowDayOfWeek) {
        habits.filter { it.scheduledDays.isEmpty() || it.scheduledDays.split(",").contains(tomorrowDayOfWeek.toString()) }
    }
    val todayOccurrences = remember(occurrences, todayEpochDay) {
        occurrences.filter { it.scheduledEpochDay == todayEpochDay }.associateBy { it.habitId }
    }
    val completedToday = filteredHabits.count {
        todayOccurrences[it.id]?.status == OccurrenceStatus.COMPLETED
    }
    val dailyProgress = if (filteredHabits.isEmpty()) 0f else completedToday.toFloat() / filteredHabits.size
    val dateText = remember(today) {
        today.format(DateTimeFormatter.ofPattern("EEEE, dd 'tháng' MM", Locale("vi")))
            .replaceFirstChar { it.uppercase() }
    }

    val isAllCompleted = filteredHabits.isNotEmpty() && completedToday == filteredHabits.size

    LaunchedEffect(userStats?.level) {
        val currentLvl = userStats?.level
        if (prevLevel != null && currentLvl != null && currentLvl > prevLevel!!) {
            showConfetti = true
        }
        prevLevel = currentLvl
    }

    LaunchedEffect(isAllCompleted) {
        if (isAllCompleted && !prevCompletedAll) {
            showConfetti = true
        }
        prevCompletedAll = isAllCompleted
    }

    if (showLevelDetail && userStats != null) {
        LevelDetailDialog(
            stats = userStats!!,
            onSkipLevel = { vm.skipLevel() },
            onDismiss = { showLevelDetail = false }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(if (filteredHabits.size <= 2 && filteredHabits.isNotEmpty()) 24.dp else 14.dp)
        ) {
            item {
                ScreenHeader(
                    eyebrow = dateText,
                    title = "Hôm nay",
                    subtitle = if (filteredHabits.isEmpty()) "Một ngày nhẹ nhàng cũng là một ngày có tiến bộ." else "$completedToday/${filteredHabits.size} thói quen đã hoàn thành",
                    trailing = {
                        TextButton(onClick = vm::advanceTestDay) {
                            Text("+1 ngày test", color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
            }

            userStats?.let { stats ->
                item {
                    LevelHeroCard(
                        stats = stats,
                        dailyProgress = dailyProgress,
                        completedToday = completedToday,
                        totalToday = filteredHabits.size,
                        onClick = { showLevelDetail = true }
                    )
                }
            }

            if (filteredHabits.isEmpty()) {
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🌱", style = MaterialTheme.typography.displaySmall)
                            Spacer(Modifier.height(10.dp))
                            Text("Chưa có việc cần làm hôm nay", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Tạo thói quen đầu tiên để HabitFlow bắt đầu theo dõi tiến độ cho bạn.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = onNavigateToHabits,
                                shape = RoundedCornerShape(14.dp)
                            ) { Text("Thêm thói quen") }
                        }
                    }
                }
            } else {
                item { SectionTitle("Việc cần làm", "Tập trung vào từng việc nhỏ") }
                items(filteredHabits, key = { it.id }) { habit ->
                    HabitTodayCard(
                        habit = habit,
                        occurrence = todayOccurrences[habit.id],
                        streakFreezes = userStats?.streakFreezes ?: 0,
                        skipsAvailable = userStats?.skipsAvailable ?: 0,
                        onComplete = {
                            if (isHapticEnabled) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            vm.mark(habit.id, OccurrenceStatus.COMPLETED)
                        },
                        onSkip = { vm.mark(habit.id, OccurrenceStatus.SKIPPED) },
                        onFreeze = { vm.useStreakFreeze(habit.id) },
                        onUseSkip = { vm.useSkip(habit.id) },
                        onReset = { vm.unmark(habit.id, todayEpochDay) },
                        modifier = if (filteredHabits.size <= 2) Modifier.heightIn(min = 130.dp) else Modifier
                    )
                }
            }

            if (tomorrowHabits.isNotEmpty()) {
                item { SectionTitle("Ngày mai", "Chuẩn bị trước để giữ nhịp") }
                items(tomorrowHabits, key = { "tomorrow_${it.id}" }) { habit ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                    ) {
                        Row(
                            Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                                Text("→", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(habit.name, fontWeight = FontWeight.SemiBold)
                                if (habit.description.isNotBlank()) {
                                    Text(habit.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            habit.scheduledTime?.let { TimePill(it) }
                        }
                    }
                }
            }
        }

        ConfettiEffect(
            visible = showConfetti,
            onFinished = { showConfetti = false }
        )
    }
}

@Composable
private fun LevelHeroCard(
    stats: UserStatsEntity,
    dailyProgress: Float,
    completedToday: Int,
    totalToday: Int,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(54.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(stats.level.toString(), color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text("Cấp độ ${stats.level}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    val xpForNext = GamificationManager.getXpForNextLevel(stats.level)
                    val xpForCurrent = GamificationManager.getXpForNextLevel(stats.level - 1)
                    val progress = if (xpForNext > xpForCurrent) (stats.xp - xpForCurrent).toFloat() / (xpForNext - xpForCurrent).toFloat() else 0f
                    
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
                    )
                    Text("${stats.xp} XP", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                }
            }
            
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Tiến độ ngày",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            "$completedToday / $totalToday",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            "${(dailyProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { dailyProgress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 4.dp,
                        strokeCap = StrokeCap.Round
                    )
                    if (dailyProgress >= 1f) Text("✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun HabitTodayCard(
    habit: HabitEntity,
    occurrence: OccurrenceEntity?,
    streakFreezes: Int,
    skipsAvailable: Int,
    onComplete: () -> Unit,
    onSkip: () -> Unit,
    onFreeze: () -> Unit,
    onUseSkip: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val container = when (occurrence?.status) {
        OccurrenceStatus.COMPLETED -> MaterialTheme.colorScheme.secondaryContainer
        OccurrenceStatus.SKIPPED -> MaterialTheme.colorScheme.tertiaryContainer
        OccurrenceStatus.FROZEN -> MaterialTheme.colorScheme.primaryContainer
        OccurrenceStatus.MISSED -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surface
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = container),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (occurrence == null) 2.dp else 0.dp)
    ) {
        Column(Modifier.padding(17.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(38.dp),
                    shape = CircleShape,
                    color = if (occurrence?.status == OccurrenceStatus.COMPLETED) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            if (occurrence?.status == OccurrenceStatus.COMPLETED) "✓" else "•",
                            color = if (occurrence?.status == OccurrenceStatus.COMPLETED) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(habit.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (habit.description.isNotBlank()) {
                        Text(habit.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                habit.scheduledTime?.let { TimePill(it) }
            }

            Spacer(Modifier.height(14.dp))
            if (occurrence != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusPill(occurrence.status)
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onReset) { Text("Thay đổi") }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onComplete,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Hoàn thành") }
                    OutlinedButton(
                        onClick = onSkip,
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Bỏ qua") }
                }
                if (streakFreezes > 0 || skipsAvailable > 0) {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (streakFreezes > 0) {
                            AssistChip(onClick = onFreeze, label = { Text("❄ Đóng băng") })
                        }
                        if (skipsAvailable > 0) {
                            AssistChip(onClick = onUseSkip, label = { Text("⏭ Vé bỏ qua") })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusPill(status: OccurrenceStatus) {
    val (text, color) = when (status) {
        OccurrenceStatus.COMPLETED -> "Đã xong" to MaterialTheme.colorScheme.primary
        OccurrenceStatus.SKIPPED -> "Đã bỏ qua" to MaterialTheme.colorScheme.tertiary
        OccurrenceStatus.FROZEN -> "Đã đóng băng" to MaterialTheme.colorScheme.primary
        OccurrenceStatus.MISSED -> "Bỏ lỡ" to MaterialTheme.colorScheme.error
        OccurrenceStatus.PENDING -> "Chờ" to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.15f)) {
        Text(text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TimePill(time: String) {
    Surface(shape = RoundedCornerShape(999.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Text("◷ $time", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall)
    }
}



@Composable
fun SectionTitle(title: String, subtitle: String? = null) {
    Column(Modifier.padding(top = 10.dp, bottom = 4.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        subtitle?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
private fun StatisticsScreen(vm: MainViewModel, onNavigateToToday: () -> Unit) {
    val stats by vm.stats.collectAsStateWithLifecycle()
    val habits by vm.habits.collectAsStateWithLifecycle()
    val occurrences by vm.occurrences.collectAsStateWithLifecycle()
    val testOffset by vm.testDateOffset.collectAsStateWithLifecycle()
    val today = remember(testOffset) { LocalDate.now().plusDays(testOffset) }
    var selectedYearMonth by remember { mutableStateOf(YearMonth.from(today)) }
    
    var selectedDateDetail by remember { mutableStateOf<LocalDate?>(null) }

    if (selectedDateDetail != null) {
        DayDetailDialog(
            date = selectedDateDetail!!,
            today = today,
            habits = habits,
            occurrences = occurrences,
            onDismiss = { selectedDateDetail = null }
        )
    }

    val daysInMonth = selectedYearMonth.lengthOfMonth()
    val firstDayOfWeek = selectedYearMonth.atDay(1).dayOfWeek.value
    val habitsByDayOfWeek = remember(habits) {
        (1..7).associateWith { dayNum ->
            habits.filter { it.scheduledDays.isEmpty() || it.scheduledDays.split(",").contains(dayNum.toString()) }
        }
    }
    val occurrencesByEpochDay = remember(occurrences) { occurrences.groupBy { it.scheduledEpochDay } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ScreenHeader(
                eyebrow = "Tổng quan",
                title = "Thống kê",
                subtitle = "Nhìn lại nhịp duy trì và chuỗi tiến bộ của bạn."
            )
        }
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { (stats.completionRate / 100.0).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier.size(78.dp),
                            strokeWidth = 8.dp,
                            trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                        )
                        Text("${stats.completionRate.toInt()}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(18.dp))
                    Column {
                        Text("Tỷ lệ hoàn thành", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Text("${stats.completed} lần hoàn thành", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Tuần ${stats.weeklyCompletionRate.toInt()}% • Tháng ${stats.monthlyCompletionRate.toInt()}%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f))
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Hoàn thành", stats.completed.toString(), "✓", Modifier.weight(1f))
                StatCard("Bỏ lỡ", stats.missed.toString(), "×", Modifier.weight(1f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Bỏ qua", stats.skipped.toString(), "↷", Modifier.weight(1f))
                StatCard("Đóng băng", stats.frozen.toString(), "❄", Modifier.weight(1f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Chuỗi hiện tại", stats.currentStreak.toString(), "🔥", Modifier.weight(1f))
                StatCard("Dài nhất", stats.longestStreak.toString(), "★", Modifier.weight(1f))
            }
        }
        item {
            CalendarCard(
                selectedYearMonth = selectedYearMonth,
                today = today,
                daysInMonth = daysInMonth,
                firstDayOfWeek = firstDayOfWeek,
                habitsByDayOfWeek = habitsByDayOfWeek,
                occurrencesByEpochDay = occurrencesByEpochDay,
                onPrevious = { selectedYearMonth = selectedYearMonth.minusMonths(1) },
                onNext = { selectedYearMonth = selectedYearMonth.plusMonths(1) },
                onDateClick = { date ->
                    if (date == today) {
                        onNavigateToToday()
                    } else {
                        selectedDateDetail = date
                    }
                }
            )
        }
    }
}

@Composable
private fun DayDetailDialog(
    date: LocalDate,
    today: LocalDate,
    habits: List<HabitEntity>,
    occurrences: List<OccurrenceEntity>,
    onDismiss: () -> Unit
) {
    val epochDay = date.toEpochDay()
    val dayOfWeek = date.dayOfWeek.value
    val scheduledHabits = habits.filter { habit ->
        val isScheduled = habit.scheduledDays.isEmpty() || habit.scheduledDays.split(",").contains(dayOfWeek.toString())
        val isCreated = (habit.createdAt / 86400000L) <= epochDay
        isScheduled && isCreated
    }
    val dayOccurrences = occurrences.filter { it.scheduledEpochDay == epochDay }.associateBy { it.habitId }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(
                text = date.format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", Locale("vi"))).replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
        },
        text = {
            if (scheduledHabits.isEmpty()) {
                Text(
                    text = "Không có thói quen nào được lên lịch cho ngày này.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(scheduledHabits) { habit ->
                        val occurrence = dayOccurrences[habit.id]
                        val isFuture = epochDay > today.toEpochDay()
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(36.dp),
                                    shape = CircleShape,
                                    color = when (occurrence?.status) {
                                        OccurrenceStatus.COMPLETED -> MaterialTheme.colorScheme.primaryContainer
                                        OccurrenceStatus.SKIPPED -> MaterialTheme.colorScheme.tertiaryContainer
                                        OccurrenceStatus.FROZEN -> MaterialTheme.colorScheme.primaryContainer
                                        OccurrenceStatus.MISSED -> MaterialTheme.colorScheme.errorContainer
                                        else -> if (isFuture) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant
                                    }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = when (occurrence?.status) {
                                                OccurrenceStatus.COMPLETED -> "✓"
                                                OccurrenceStatus.SKIPPED -> "↷"
                                                OccurrenceStatus.FROZEN -> "❄"
                                                OccurrenceStatus.MISSED -> "×"
                                                else -> if (isFuture) "⌛" else "○"
                                            },
                                            color = when (occurrence?.status) {
                                                OccurrenceStatus.COMPLETED -> MaterialTheme.colorScheme.primary
                                                OccurrenceStatus.SKIPPED -> MaterialTheme.colorScheme.tertiary
                                                OccurrenceStatus.FROZEN -> MaterialTheme.colorScheme.primary
                                                OccurrenceStatus.MISSED -> MaterialTheme.colorScheme.error
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(habit.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                    val statusText = when (occurrence?.status) {
                                        OccurrenceStatus.COMPLETED -> "Đã hoàn thành"
                                        OccurrenceStatus.SKIPPED -> "Đã bỏ qua"
                                        OccurrenceStatus.FROZEN -> "Đã đóng băng"
                                        OccurrenceStatus.MISSED -> "Bỏ lỡ"
                                        else -> if (isFuture) "Sắp tới" else "Chưa ghi nhận"
                                    }
                                    Text(
                                        text = statusText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Đóng") }
        }
    )
}

@Composable
private fun StatCard(label: String, value: String, icon: String, modifier: Modifier = Modifier) {
    ElevatedCard(modifier, shape = RoundedCornerShape(20.dp)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(36.dp)) {
                Box(contentAlignment = Alignment.Center) { Text(icon, style = MaterialTheme.typography.titleMedium) }
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun CalendarCard(
    selectedYearMonth: YearMonth,
    today: LocalDate,
    daysInMonth: Int,
    firstDayOfWeek: Int,
    habitsByDayOfWeek: Map<Int, List<HabitEntity>>,
    occurrencesByEpochDay: Map<Long, List<OccurrenceEntity>>,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onDateClick: (LocalDate) -> Unit
) {
    val days = remember(selectedYearMonth, firstDayOfWeek, daysInMonth) {
        buildList<LocalDate?> {
            repeat(firstDayOfWeek - 1) { add(null) }
            for (day in 1..daysInMonth) add(selectedYearMonth.atDay(day))
        }
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Lịch duy trì", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        selectedYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("vi"))).replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FilledTonalButton(onClick = onPrevious, contentPadding = PaddingValues(horizontal = 12.dp)) { Text("‹") }
                Spacer(Modifier.width(6.dp))
                FilledTonalButton(onClick = onNext, contentPadding = PaddingValues(horizontal = 12.dp)) { Text("›") }
            }

            Row(Modifier.fillMaxWidth()) {
                listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN").forEach { day ->
                    Text(day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            days.chunked(7).forEach { week ->
                Row(Modifier.fillMaxWidth()) {
                    week.forEach { date ->
                        CalendarDayCell(
                            date = date,
                            today = today,
                            habitsByDayOfWeek = habitsByDayOfWeek,
                            occurrencesByEpochDay = occurrencesByEpochDay,
                            modifier = Modifier.weight(1f),
                            onClick = onDateClick
                        )
                    }
                    repeat(7 - week.size) { Spacer(Modifier.weight(1f).aspectRatio(1f)) }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LegendDot(MaterialTheme.colorScheme.secondaryContainer, "Tốt")
                LegendDot(MaterialTheme.colorScheme.tertiaryContainer, "Một phần")
                LegendDot(MaterialTheme.colorScheme.errorContainer, "Chưa đạt")
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate?,
    today: LocalDate,
    habitsByDayOfWeek: Map<Int, List<HabitEntity>>,
    occurrencesByEpochDay: Map<Long, List<OccurrenceEntity>>,
    modifier: Modifier = Modifier,
    onClick: (LocalDate) -> Unit
) {
    Box(modifier = modifier.aspectRatio(1f).padding(2.dp), contentAlignment = Alignment.Center) {
        if (date != null) {
            val epochDay = date.toEpochDay()
            val dayOccurrences = occurrencesByEpochDay[epochDay] ?: emptyList()
            val dayHabits = habitsByDayOfWeek[date.dayOfWeek.value]?.filter { it.createdAt / 86400000 <= epochDay } ?: emptyList()
            val completed = dayOccurrences.count { it.status == OccurrenceStatus.COMPLETED }
            val frozen = dayOccurrences.any { it.status == OccurrenceStatus.FROZEN }
            val total = dayHabits.size
            val future = epochDay > today.toEpochDay()
            val color = when {
                total == 0 || future -> MaterialTheme.colorScheme.surfaceVariant
                frozen -> MaterialTheme.colorScheme.primaryContainer
                completed * 2 >= total -> MaterialTheme.colorScheme.secondaryContainer
                completed > 0 -> MaterialTheme.colorScheme.tertiaryContainer
                date == today -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.errorContainer
            }
            Surface(
                modifier = Modifier.fillMaxSize().clickable { onClick(date) },
                shape = RoundedCornerShape(10.dp),
                color = color,
                border = if (date == today) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (date == today) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.offset(y = (-2).dp)
                    )
                    if (total > 0) {
                        if (total > 6) {
                            Text(
                                text = "—",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = LocalContentColor.current.copy(alpha = 0.6f)
                            )
                        } else {
                            val rows = if (total > 3) 2 else 1
                            Column(
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.offset(y = (-1).dp)
                            ) {
                                repeat(rows) { rowIndex ->
                                    val dotsInRow = if (rows == 1) total else if (rowIndex == 0) 3 else total - 3
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        repeat(dotsInRow) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .background(LocalContentColor.current.copy(alpha = 0.6f), CircleShape)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun LevelDetailDialog(
    stats: UserStatsEntity,
    onSkipLevel: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Trạng thái của bạn", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                IconButton(onClick = onSkipLevel) { Text("⚡", color = MaterialTheme.colorScheme.error) }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                val xpForNext = GamificationManager.getXpForNextLevel(stats.level)
                val xpForCurrent = GamificationManager.getXpForNextLevel(stats.level - 1)
                val progress = if (xpForNext > xpForCurrent) (stats.xp - xpForCurrent).toFloat() / (xpForNext - xpForCurrent).toFloat() else 0f

                Column {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tiến trình Cấp ${stats.level}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Text("${stats.xp} / $xpForNext XP", style = MaterialTheme.typography.labelMedium)
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                        strokeCap = StrokeCap.Round
                    )
                }

                Column {
                    Text("Kho kỹ năng", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SkillItem("❄️ Đóng băng", stats.streakFreezes.toString(), Modifier.weight(1f))
                        SkillItem("⏭️ Vé bỏ qua", stats.skipsAvailable.toString(), Modifier.weight(1f))
                    }
                }

                Column {
                    Text("Phần thưởng tiếp theo", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    GamificationManager.getUpcomingRewards(stats.level).forEach { reward ->
                        Text("• $reward", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) { Text("Đóng") }
        }
    )
}

@Composable
private fun SkillItem(label: String, count: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = modifier
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(count, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        }
    }
}
