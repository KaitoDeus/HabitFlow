package com.habitflow.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun HabitsScreen(vm: MainViewModel) {
    val habits by vm.habits.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var scheduledTime by remember { mutableStateOf("") }
    var habitToDelete by remember { mutableStateOf<HabitEntity?>(null) }

    if (habitToDelete != null) {
        AlertDialog(
            onDismissRequest = { habitToDelete = null },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc chắn muốn xóa thói quen \"${habitToDelete?.name}\" không? Hành động này không thể hoàn tác.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        habitToDelete?.let { vm.deleteHabit(it.id) }
                        habitToDelete = null
                    }
                ) {
                    Text("Xóa", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { habitToDelete = null }) {
                    Text("Hủy")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Thói quen", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Tên thói quen") }
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Mô tả") }
        )

        val daysOfWeek = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
        var selectedDays by remember { mutableStateOf(setOf<Int>()) }

        Text("Lặp lại:", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            daysOfWeek.forEachIndexed { index, day ->
                val dayNum = index + 1 // 1=Mon, ..., 7=Sun
                FilterChip(
                    selected = selectedDays.contains(dayNum),
                    onClick = {
                        selectedDays = if (selectedDays.contains(dayNum)) {
                            selectedDays - dayNum
                        } else {
                            selectedDays + dayNum
                        }
                    },
                    label = { Text(day) }
                )
            }
        }

        var showTimePicker by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { showTimePicker = true }
                .padding(16.dp)
        ) {
            Text(
                text = if (scheduledTime.isBlank()) "Chọn giờ thực hiện (Không bắt buộc)" else "Giờ thực hiện: $scheduledTime",
                color = if (scheduledTime.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
            )
        }

        if (showTimePicker) {
            WheelTimePickerDialog(
                initialTime = scheduledTime,
                onConfirm = { time ->
                    scheduledTime = time
                    showTimePicker = false
                },
                onDismiss = { showTimePicker = false },
                onClear = {
                    scheduledTime = ""
                    showTimePicker = false
                }
            )
        }

        Button(
            onClick = {
                if (name.isNotBlank()) {
                    val scheduledDays = selectedDays.sorted().joinToString(",")
                    val time = scheduledTime.trim().ifBlank { null }
                    vm.addHabit(name, description, scheduledDays, time)
                    name = ""
                    description = ""
                    scheduledTime = ""
                    selectedDays = emptySet()
                }
            },
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text("Thêm thói quen")
        }

        LazyColumn {
            items(habits, key = { it.id }) { habit ->
                // Optimization: Pre-calculate display text for days
                val displayDays = remember(habit.scheduledDays) {
                    if (habit.scheduledDays.isEmpty()) {
                        "Hàng ngày"
                    } else {
                        habit.scheduledDays.split(",").joinToString(", ") { d ->
                            when (d.toInt()) {
                                1 -> "Thứ 2"
                                2 -> "Thứ 3"
                                3 -> "Thứ 4"
                                4 -> "Thứ 5"
                                5 -> "Thứ 6"
                                6 -> "Thứ 7"
                                7 -> "Chủ Nhật"
                                else -> ""
                            }
                        }
                    }
                }
                ListItem(
                    headlineContent = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(habit.name)
                            if (habit.scheduledTime != null) {
                                Spacer(Modifier.width(8.dp))
                                SuggestionChip(
                                    onClick = {}, 
                                    label = { Text(habit.scheduledTime, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    },
                    supportingContent = { 
                        Column {
                            if (habit.description.isNotBlank()) {
                                Text(habit.description)
                            }
                            Text(displayDays, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                        }
                    },
                    trailingContent = {
                        Row {
                            TextButton(onClick = { vm.archiveHabit(habit.id) }) {
                                Text("Lưu trữ")
                            }
                            IconButton(onClick = { habitToDelete = habit }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Xóa",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                )
                HorizontalDivider()
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
    val initialHour = initialTime.split(":").getOrNull(0)?.toIntOrNull() ?: 8
    val initialMinute = initialTime.split(":").getOrNull(1)?.toIntOrNull() ?: 0

    var selectedHour by remember { mutableIntStateOf(initialHour) }
    var selectedMinute by remember { mutableIntStateOf(initialMinute) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chọn giờ") },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                WheelPicker(
                    items = (0..23).map { it.toString().padStart(2, '0') },
                    initialIndex = initialHour,
                    onItemSelected = { selectedHour = it }
                )
                Text(":", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(horizontal = 8.dp))
                WheelPicker(
                    items = (0..59).map { it.toString().padStart(2, '0') },
                    initialIndex = initialMinute,
                    onItemSelected = { selectedMinute = it }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { 
                onConfirm("${selectedHour.toString().padStart(2, '0')}:${selectedMinute.toString().padStart(2, '0')}") 
            }) {
                Text("Xác nhận")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onClear) {
                    Text("Xóa giờ", color = MaterialTheme.colorScheme.error)
                }
                TextButton(onClick = onDismiss) {
                    Text("Hủy")
                }
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
    val pagerState = rememberPagerState(initialPage = initialIndex, pageCount = { items.size })

    LaunchedEffect(pagerState.currentPage) {
        onItemSelected(pagerState.currentPage)
    }

    VerticalPager(
        state = pagerState,
        modifier = Modifier.height(150.dp).width(60.dp),
        contentPadding = PaddingValues(vertical = 60.dp)
    ) { page ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = items[page],
                style = if (pagerState.currentPage == page) {
                    MaterialTheme.typography.headlineMedium
                } else {
                    MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                },
                textAlign = TextAlign.Center
            )
        }
    }
}
