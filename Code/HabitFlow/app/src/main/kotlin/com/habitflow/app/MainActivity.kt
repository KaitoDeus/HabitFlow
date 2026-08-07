package com.habitflow.app

import java.time.LocalDate
import android.Manifest
import androidx.compose.runtime.saveable.rememberSaveable
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.format.DateTimeFormatter
import java.time.YearMonth
import java.time.DayOfWeek
import java.util.Locale
import kotlinx.coroutines.launch
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HabitFlowApp(viewModel) }
    }
}

@Composable
fun HabitFlowApp(viewModel: MainViewModel) {
    MaterialTheme {
        var tab by rememberSaveable { mutableIntStateOf(0) }
        val labels = listOf("Hôm nay", "Thói quen", "Mục tiêu", "Thống kê", "Cài đặt")
        Scaffold(bottomBar = {
            NavigationBar {
                labels.forEachIndexed { index, label ->
                    NavigationBarItem(selected = tab == index, onClick = { tab = index },
                        icon = { Text(label.take(1)) }, label = { Text(label) })
                }
            }
        }) { padding ->
            Box(Modifier.padding(padding)) {
                when (tab) {
                    0 -> TodayScreen(viewModel)
                    1 -> HabitsScreen(viewModel)
                    2 -> GoalsScreen(viewModel)
                    3 -> StatisticsScreen(viewModel)
                    else -> SettingsScreen(viewModel)
                }
            }
        }
    }
}

@Composable
private fun TodayScreen(vm: MainViewModel) {
    val habits by vm.habits.collectAsStateWithLifecycle()
    val occurrences by vm.occurrences.collectAsStateWithLifecycle()

    // Lấy ngày hôm nay và lọc thói quen
    val today = remember { LocalDate.now() }
    val todayEpochDay = remember { today.toEpochDay() }
    val currentDayOfWeek = remember { today.dayOfWeek.value }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", Locale("vi")) }
    val dateString = remember { today.format(dateFormatter) }.replaceFirstChar { it.uppercase() }

    val tomorrow = remember { today.plusDays(1) }
    val tomorrowDayOfWeek = remember { tomorrow.dayOfWeek.value }

    val filteredHabits = remember(habits, currentDayOfWeek) {
        habits.filter { habit ->
            habit.scheduledDays.isEmpty() || 
            habit.scheduledDays.split(",").contains(currentDayOfWeek.toString())
        }
    }

    val tomorrowHabits = remember(habits, tomorrowDayOfWeek) {
        habits.filter { habit ->
            habit.scheduledDays.isEmpty() || 
            habit.scheduledDays.split(",").contains(tomorrowDayOfWeek.toString())
        }
    }

    // Optimization: Pre-filter today's occurrences
    val todayOccurrences = remember(occurrences, todayEpochDay) {
        occurrences.filter { it.scheduledEpochDay == todayEpochDay }.associateBy { it.habitId }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(dateString, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.secondary)
        Text("Hôm nay", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))

        if (filteredHabits.isEmpty()) {
            Text("Không có thói quen nào cho hôm nay.")
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filteredHabits, key = { it.id }) { habit ->
                val todayOccurrence = todayOccurrences[habit.id]

                val cardColor = when (todayOccurrence?.status) {
                    OccurrenceStatus.COMPLETED -> Color(0xFFC8E6C9)
                    OccurrenceStatus.SKIPPED -> Color(0xFFE1BEE7)
                    OccurrenceStatus.MISSED -> Color(0xFFFF0000)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(habit.name, style = MaterialTheme.typography.titleMedium)
                            if (habit.scheduledTime != null) {
                                Spacer(Modifier.width(8.dp))
                                Text(habit.scheduledTime, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                        if (habit.description.isNotBlank()) {
                            Text(habit.description)
                        }

                        // Hiển thị trạng thái nếu hôm nay đã bấm nút
                        if (todayOccurrence != null) {
                            val statusText = when (todayOccurrence.status) {
                                OccurrenceStatus.COMPLETED -> "Đã hoàn thành"
                                OccurrenceStatus.SKIPPED -> "Đã bỏ qua"
                                OccurrenceStatus.MISSED -> "Đã bỏ lỡ"
                                OccurrenceStatus.PENDING -> "Đang chờ"
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                TextButton(onClick = { vm.unmark(habit.id, todayEpochDay) }) {
                                    Text("Thay đổi")
                                }
                            }
                        } else {
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { vm.mark(habit.id, OccurrenceStatus.COMPLETED) }) {
                                    Text("Xong")
                                }
                                OutlinedButton(onClick = { vm.mark(habit.id, OccurrenceStatus.SKIPPED) }) {
                                    Text("Bỏ qua")
                                }
                                OutlinedButton(onClick = { vm.mark(habit.id, OccurrenceStatus.MISSED) }) {
                                    Text("Bỏ lỡ")
                                }
                            }
                        }
                    }
                }
            }

            if (tomorrowHabits.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(16.dp))
                    Text("Ngày mai", style = MaterialTheme.typography.titleMedium)
                }
                items(tomorrowHabits, key = { "tomorrow_${it.id}" }) { habit ->
                    OutlinedCard(Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(habit.name, style = MaterialTheme.typography.bodyLarge)
                            if (habit.scheduledTime != null) {
                                Spacer(Modifier.width(8.dp))
                                Text(habit.scheduledTime, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}



@Composable
private fun GoalsScreen(vm: MainViewModel) {
    val goals by vm.goals.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("10") }
    var type by remember { mutableStateOf(GoalMetricType.OCCURRENCE_COUNT) }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Mục tiêu", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Tên mục tiêu") })
        OutlinedTextField(target, { target = it }, Modifier.fillMaxWidth(), label = { Text("Giá trị mục tiêu") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
        Row {
            FilterChip(selected = type == GoalMetricType.OCCURRENCE_COUNT, onClick = { type = GoalMetricType.OCCURRENCE_COUNT }, label = { Text("Theo số lần") })
            Spacer(Modifier.width(8.dp))
            FilterChip(selected = type == GoalMetricType.ACCUMULATED_VALUE, onClick = { type = GoalMetricType.ACCUMULATED_VALUE }, label = { Text("Theo giá trị") })
        }
        Button(onClick = { target.toDoubleOrNull()?.let { if (name.isNotBlank() && it > 0) { vm.addGoal(name, it, type); name = "" } } }, Modifier.padding(vertical = 8.dp)) { Text("Tạo mục tiêu") }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(goals, key = { it.id }) { goal ->
                Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) {
                    Text(goal.name, style = MaterialTheme.typography.titleMedium)
                    LinearProgressIndicator(progress = { (goal.currentValue / goal.targetValue).toFloat().coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
                    Text("${goal.currentValue} / ${goal.targetValue} ${goal.unit}")
                    TextButton(onClick = { vm.addGoalProgress(goal, 1.0) }) { Text("+1 tiến độ") }
                } }
            }
        }
    }
}

@Composable
private fun StatisticsScreen(vm: MainViewModel) {
    val stats by vm.stats.collectAsStateWithLifecycle()
    val habits by vm.habits.collectAsStateWithLifecycle()
    val occurrences by vm.occurrences.collectAsStateWithLifecycle()

    val today = remember { LocalDate.now() }
    val yearMonth = remember { YearMonth.from(today) }
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = yearMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value // 1 (Mon) to 7 (Sun)

    // Optimization: Pre-calculate habits for each day of week
    val habitsByDayOfWeek = remember(habits) {
        (1..7).associateWith { dayNum ->
            habits.filter { habit ->
                habit.scheduledDays.isEmpty() || 
                habit.scheduledDays.split(",").contains(dayNum.toString())
            }
        }
    }

    // Optimization: Pre-calculate occurrences for each epoch day
    val occurrencesByEpochDay = remember(occurrences) {
        occurrences.groupBy { it.scheduledEpochDay }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Thống kê", style = MaterialTheme.typography.headlineMedium)
        
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("vi"))).replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleMedium)
                
                // Grid header: T2 to CN
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN").forEach { day ->
                        Text(day, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }

                // Calendar Grid
                val days = remember(yearMonth, firstDayOfWeek, daysInMonth) {
                    val list = mutableListOf<LocalDate?>()
                    for (i in 1 until firstDayOfWeek) { list.add(null) }
                    for (i in 1..daysInMonth) { list.add(yearMonth.atDay(i)) }
                    list
                }
                
                val chunks = remember(days) { days.chunked(7) }
                chunks.forEach { week ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        week.forEach { date ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (date != null) {
                                    val epochDay = date.toEpochDay()
                                    val dayOccurrences = occurrencesByEpochDay[epochDay] ?: emptyList()
                                    val dayHabits = habitsByDayOfWeek[date.dayOfWeek.value] ?: emptyList()
                                    
                                    val completed = dayOccurrences.count { it.status == OccurrenceStatus.COMPLETED }
                                    val total = dayHabits.size
                                    
                                    val color = when {
                                        total == 0 -> MaterialTheme.colorScheme.surfaceVariant
                                        completed == 0 -> Color(0xFFFFCDD2) // Light red
                                        completed >= total -> Color(0xFFC8E6C9) // Green
                                        else -> Color(0xFFFFF9C4) // Yellow
                                    }

                                    Surface(
                                        modifier = Modifier.fillMaxSize(),
                                        color = color,
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                                        border = if (date == today) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
                        if (week.size < 7) {
                            repeat(7 - week.size) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        Text("Tỷ lệ hoàn thành: ${"%.1f".format(stats.completionRate)}%")
        LinearProgressIndicator(progress = { (stats.completionRate / 100.0).toFloat() }, modifier = Modifier.fillMaxWidth())
        
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Hoàn thành", stats.completed.toString(), Modifier.weight(1f))
            StatCard("Bỏ lỡ", stats.missed.toString(), Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Bỏ qua", stats.skipped.toString(), Modifier.weight(1f))
            StatCard("Chuỗi hiện tại", stats.currentStreak.toString(), Modifier.weight(1f))
        }
        StatCard("Chuỗi dài nhất", stats.longestStreak.toString(), Modifier.fillMaxWidth())
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
private fun SettingsScreen(vm: MainViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf("") }
    val createDocument = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) scope.launch {
            runCatching { context.contentResolver.openOutputStream(uri)?.use { it.write(vm.exportJson().toByteArray()) } }
                .onSuccess { message = "Đã xuất dữ liệu" }.onFailure { message = it.message ?: "Xuất thất bại" }
        }
    }
    val openDocument = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            runCatching { val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: error("Không đọc được tệp"); vm.restoreJson(text) }
                .onSuccess { message = "Đã khôi phục dữ liệu" }.onFailure { message = it.message ?: "Khôi phục thất bại" }
        }
    }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> message = if (granted) "Đã cấp quyền thông báo" else "Chưa cấp quyền thông báo" }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Cài đặt", style = MaterialTheme.typography.headlineMedium)
        Button(onClick = { createDocument.launch("habitflow-backup.json") }) { Text("Xuất JSON") }
        OutlinedButton(onClick = { openDocument.launch(arrayOf("application/json", "text/plain")) }) { Text("Khôi phục JSON") }
        OutlinedButton(onClick = { if (Build.VERSION.SDK_INT >= 33) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS) else message = "Thiết bị không cần cấp quyền runtime" }) { Text("Cấp quyền thông báo") }
        if (message.isNotBlank()) Text(message)
    }
}
