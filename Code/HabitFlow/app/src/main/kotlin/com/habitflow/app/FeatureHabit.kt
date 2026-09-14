package com.habitflow.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun HabitsScreen(vm: MainViewModel) {
    val habits by vm.habits.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var scheduledTime by remember { mutableStateOf("") }
    var selectedDays by remember { mutableStateOf(setOf<Int>()) }
    var showTimePicker by remember { mutableStateOf(false) }
    var habitToDelete by remember { mutableStateOf<HabitEntity?>(null) }

    habitToDelete?.let { habit ->
        AlertDialog(
            onDismissRequest = { habitToDelete = null },
            shape = RoundedCornerShape(24.dp),
            title = { Text("Xóa thói quen?", fontWeight = FontWeight.Bold) },
            text = { Text("\"${habit.name}\" sẽ bị xóa vĩnh viễn và không thể hoàn tác.") },
            confirmButton = {
                Button(
                    onClick = {
                        vm.deleteHabit(habit.id)
                        habitToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Xóa") }
            },
            dismissButton = { TextButton(onClick = { habitToDelete = null }) { Text("Hủy") } }
        )
    }

    if (showTimePicker) {
        WheelTimePickerDialog(
            initialTime = scheduledTime,
            onConfirm = {
                scheduledTime = it
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false },
            onClear = {
                scheduledTime = ""
                showTimePicker = false
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ScreenHeader(
                eyebrow = "Xây nền nếp",
                title = "Thói quen",
                subtitle = "Tạo lịch lặp lại rõ ràng, sau đó chỉ cần tập trung hoàn thành từng ngày."
            )
        }

        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Thói quen mới", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Tên thói quen") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Mô tả ngắn") },
                        minLines = 2,
                        maxLines = 3,
                        shape = RoundedCornerShape(14.dp)
                    )

                    Text("Lặp lại", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN").forEachIndexed { index, label ->
                            val day = index + 1
                            FilterChip(
                                selected = day in selectedDays,
                                onClick = {
                                    selectedDays = if (day in selectedDays) selectedDays - day else selectedDays + day
                                },
                                label = { Text(label) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                    Text(
                        if (selectedDays.isEmpty()) "Không chọn ngày = lặp hàng ngày" else "Đã chọn ${selectedDays.size} ngày/tuần",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { showTimePicker = true },
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                                Text("◷", modifier = Modifier.padding(8.dp), color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Giờ thực hiện", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                Text(
                                    if (scheduledTime.isBlank()) "Không bắt buộc" else scheduledTime,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (scheduledTime.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                                )
                            }
                            Text("Chọn", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                val habitName = name.trim()
                                vm.addHabit(
                                    name = habitName,
                                    description = description.trim(),
                                    scheduledDays = selectedDays.sorted().joinToString(","),
                                    scheduledTime = scheduledTime.trim().ifBlank { null }
                                )
                                android.widget.Toast.makeText(context, "Đã thêm thói quen: $habitName", android.widget.Toast.LENGTH_SHORT).show()
                                name = ""
                                description = ""
                                scheduledTime = ""
                                selectedDays = emptySet()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        enabled = name.isNotBlank()
                    ) { Text("Thêm thói quen") }
                }
            }
        }

        if (habits.isEmpty()) {
            item {
                EmptyCompactCard(
                    icon = "🌿",
                    title = "Danh sách đang trống",
                    subtitle = "Thêm một thói quen ở phía trên để bắt đầu."
                )
            }
        } else {
            item { SectionTitle("Đang hoạt động", "${habits.size} thói quen") }
            items(habits, key = { it.id }) { habit ->
                HabitManageCard(
                    habit = habit,
                    onArchive = { vm.archiveHabit(habit.id) },
                    onDelete = { habitToDelete = habit }
                )
            }
        }
    }
}

@Composable
fun EmptyCompactCard(icon: String, title: String, subtitle: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun HabitManageCard(
    habit: HabitEntity,
    onArchive: () -> Unit,
    onDelete: () -> Unit
) {
    val displayDays = remember(habit.scheduledDays) {
        if (habit.scheduledDays.isEmpty()) {
            "Hàng ngày"
        } else {
            habit.scheduledDays.split(",").joinToString(" • ") { value ->
                when (value.toIntOrNull()) {
                    1 -> "T2"
                    2 -> "T3"
                    3 -> "T4"
                    4 -> "T5"
                    5 -> "T6"
                    6 -> "T7"
                    7 -> "CN"
                    else -> ""
                }
            }
        }
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(17.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(habit.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (habit.description.isNotBlank()) {
                        Text(habit.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                habit.scheduledTime?.let {
                    Surface(shape = RoundedCornerShape(999.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                        Text("◷ $it", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)) {
                Text(displayDays, modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp), style = MaterialTheme.typography.labelMedium)
            }
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onArchive) { Text("Lưu trữ") }
                TextButton(onClick = onDelete) { Text("Xóa", color = MaterialTheme.colorScheme.error) }
            }
        }
    }
}

@Composable
fun WheelTimePickerDialog(
    initialTime: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    onClear: () -> Unit
) {
    val initialHour = initialTime.split(":").getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 8
    val initialMinute = initialTime.split(":").getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
    var selectedHour by remember { mutableIntStateOf(initialHour) }
    var selectedMinute by remember { mutableIntStateOf(initialMinute) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(26.dp),
        title = { Text("Chọn giờ thực hiện", fontWeight = FontWeight.Bold) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Cuộn để chọn giờ và phút", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        WheelPicker(
                            items = (0..23).map { it.toString().padStart(2, '0') },
                            initialIndex = initialHour,
                            onItemSelected = { selectedHour = it }
                        )
                        Text(":", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(horizontal = 8.dp), fontWeight = FontWeight.Bold)
                        WheelPicker(
                            items = (0..59).map { it.toString().padStart(2, '0') },
                            initialIndex = initialMinute,
                            onItemSelected = { selectedMinute = it }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm("${selectedHour.toString().padStart(2, '0')}:${selectedMinute.toString().padStart(2, '0')}")
                },
                shape = RoundedCornerShape(12.dp)
            ) { Text("Xác nhận") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onClear) { Text("Xóa giờ", color = MaterialTheme.colorScheme.error) }
                TextButton(onClick = onDismiss) { Text("Hủy") }
            }
        }
    )
}

@Composable
fun WheelPicker(
    items: List<String>,
    initialIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    val safeInitialIndex = initialIndex.coerceIn(0, items.lastIndex)
    val pagerState = rememberPagerState(initialPage = safeInitialIndex, pageCount = { items.size })

    LaunchedEffect(pagerState.currentPage) {
        onItemSelected(pagerState.currentPage)
    }

    Box(contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .width(64.dp)
                .height(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
        )
        VerticalPager(
            state = pagerState,
            modifier = Modifier.height(150.dp).width(64.dp),
            contentPadding = PaddingValues(vertical = 54.dp)
        ) { page ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = items[page],
                    style = if (pagerState.currentPage == page) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyMedium,
                    fontWeight = if (pagerState.currentPage == page) FontWeight.Bold else FontWeight.Normal,
                    color = if (pagerState.currentPage == page) MaterialTheme.colorScheme.primary else Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
