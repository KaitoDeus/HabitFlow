package com.habitflow.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

enum class GoalFilterTab { ACTIVE, COMPLETED, EXPIRED, ALL }
enum class GoalDeadlineOption { THIS_WEEK, THIS_MONTH, CUSTOM }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(vm: MainViewModel) {
    val goals by vm.goals.collectAsStateWithLifecycle()
    val habits by vm.habits.collectAsStateWithLifecycle()
    val testOffset by vm.testDateOffset.collectAsStateWithLifecycle()

    val currentEpochDay = remember(testOffset) { LocalDate.now().plusDays(testOffset).toEpochDay() }

    // --- FORM STATES ---
    var name by rememberSaveable { mutableStateOf("") }
    var target by rememberSaveable { mutableStateOf("10") }
    var unit by rememberSaveable { mutableStateOf("lần") }
    var type by rememberSaveable { mutableStateOf(GoalMetricType.OCCURRENCE_COUNT) }

    var deadlineOption by rememberSaveable { mutableStateOf(GoalDeadlineOption.THIS_WEEK) }
    var startEpochDay by rememberSaveable { mutableLongStateOf(currentEpochDay) }
    var endEpochDay by rememberSaveable { mutableLongStateOf(currentEpochDay + 7) }
    var datePickerTarget by rememberSaveable { mutableStateOf<String?>(null) }

    var linkedHabitId by rememberSaveable { mutableStateOf<String?>(null) }
    var contribution by rememberSaveable { mutableStateOf("1") }
    var expandedHabitMenu by remember { mutableStateOf(false) }

    // State Bộ Lọc Danh Sách
    var selectedTab by rememberSaveable { mutableStateOf(GoalFilterTab.ACTIVE) }

    // Điều kiện để dùng nút tạo mục tiêu
    val targetVal = target.toDoubleOrNull()
    val contribVal = contribution.toDoubleOrNull()
    val isFormValid = remember(name, targetVal, linkedHabitId, contribVal) {
        name.isNotBlank() && targetVal != null && targetVal > 0 && (linkedHabitId == null || (contribVal != null && contribVal > 0))
    }

    // State khóa bấm điểm danh theo ngày test
    var updatedGoalIdsToday by rememberSaveable { mutableStateOf(setOf<String>()) }
    var lastRecordedEpochDay by rememberSaveable { mutableLongStateOf(currentEpochDay) }

    LaunchedEffect(currentEpochDay) {
        if (currentEpochDay != lastRecordedEpochDay) {
            updatedGoalIdsToday = emptySet()
            lastRecordedEpochDay = currentEpochDay
        }
    }

    LaunchedEffect(startEpochDay, deadlineOption) {
        if (deadlineOption == GoalDeadlineOption.THIS_WEEK) {
            endEpochDay = startEpochDay + 7
        } else if (deadlineOption == GoalDeadlineOption.THIS_MONTH) {
            endEpochDay = startEpochDay + 30
        }
    }

    // Mục tiêu mới nhất đứng đầu
    val filteredGoals = remember(goals, selectedTab, currentEpochDay) {
        val list = when (selectedTab) {
            GoalFilterTab.ACTIVE -> goals.filter {
                it.currentValue < it.targetValue && (it.endEpochDay == null || it.endEpochDay >= currentEpochDay)
            }
            GoalFilterTab.COMPLETED -> goals.filter { it.currentValue >= it.targetValue }
            GoalFilterTab.EXPIRED -> goals.filter {
                it.currentValue < it.targetValue && (it.endEpochDay != null && it.endEpochDay < currentEpochDay)
            }
            GoalFilterTab.ALL -> goals
        }
        list.reversed()
    }

    if (datePickerTarget != null) {
        val initialMillis = if (datePickerTarget == "START") startEpochDay * 86400000L else endEpochDay * 86400000L
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

        DatePickerDialog(
            onDismissRequest = { datePickerTarget = null },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedMillis ->
                        val pickedEpoch = selectedMillis / 86400000L
                        if (datePickerTarget == "START") {
                            startEpochDay = pickedEpoch
                            if (endEpochDay < startEpochDay) endEpochDay = startEpochDay + 1
                        } else {
                            endEpochDay = pickedEpoch
                        }
                    }
                    datePickerTarget = null
                }) { Text("Xác nhận") }
            },
            dismissButton = {
                TextButton(onClick = { datePickerTarget = null }) { Text("Hủy") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ScreenHeader(
                eyebrow = "Thử thách",
                title = "Mục tiêu",
                subtitle = "Đừng để những dự định, chỉ nằm trong suy nghĩ."
            )
        }

        // Form tạo mục tiêu
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Tạo mục tiêu mới",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Tên mục tiêu") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = target,
                            onValueChange = { target = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("Mục tiêu") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("Đơn vị") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Loại mục tiêu", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = type == GoalMetricType.OCCURRENCE_COUNT,
                                onClick = {
                                    type = GoalMetricType.OCCURRENCE_COUNT
                                    unit = "lần"
                                },
                                label = { Text("Số lần") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = type == GoalMetricType.ACCUMULATED_VALUE,
                                onClick = {
                                    type = GoalMetricType.ACCUMULATED_VALUE
                                },
                                label = { Text("Tích lũy") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Thời hạn thực hiện", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = deadlineOption == GoalDeadlineOption.THIS_WEEK,
                                onClick = { deadlineOption = GoalDeadlineOption.THIS_WEEK },
                                label = { Text("Tuần") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = deadlineOption == GoalDeadlineOption.THIS_MONTH,
                                onClick = { deadlineOption = GoalDeadlineOption.THIS_MONTH },
                                label = { Text("Tháng") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = deadlineOption == GoalDeadlineOption.CUSTOM,
                                onClick = { deadlineOption = GoalDeadlineOption.CUSTOM },
                                label = { Text("Tùy chỉnh") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { datePickerTarget = "START" }) {
                                Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Từ ngày: ${formatEpochDay(startEpochDay)}", style = MaterialTheme.typography.bodySmall)
                            }

                            TextButton(
                                onClick = { datePickerTarget = "END" },
                                enabled = deadlineOption == GoalDeadlineOption.CUSTOM
                            ) {
                                Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Đến ngày: ${formatEpochDay(endEpochDay)}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Liên kết Thói quen (Tùy chọn)", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)

                        ExposedDropdownMenuBox(
                            expanded = expandedHabitMenu,
                            onExpandedChange = { expandedHabitMenu = !expandedHabitMenu }
                        ) {
                            val selectedHabitName = habits.find { it.id == linkedHabitId }?.name ?: "Không liên kết"
                            OutlinedTextField(
                                value = "🔗 $selectedHabitName",
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                                shape = RoundedCornerShape(14.dp),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedHabitMenu) }
                            )
                            ExposedDropdownMenu(
                                expanded = expandedHabitMenu,
                                onDismissRequest = { expandedHabitMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Không liên kết") },
                                    onClick = { linkedHabitId = null; expandedHabitMenu = false }
                                )
                                habits.forEach { habit ->
                                    DropdownMenuItem(
                                        text = { Text(habit.name) },
                                        onClick = { linkedHabitId = habit.id; expandedHabitMenu = false }
                                    )
                                }
                            }
                        }
                    }

                    if (linkedHabitId != null) {
                        OutlinedTextField(
                            value = contribution,
                            onValueChange = { contribution = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Tăng tiến độ thông qua thói quen đã liên kết") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    Button(
                        onClick = {
                            if (isFormValid) {
                                vm.addGoal(
                                    name = name,
                                    target = targetVal!!,
                                    type = type,
                                    periodType = GoalPeriodType.CUSTOM,
                                    unit = unit,
                                    startEpochDay = startEpochDay,
                                    endEpochDay = endEpochDay,
                                    linkedHabitId = linkedHabitId,
                                    contributionValue = contribVal ?: 1.0
                                )
                                // Reset form về mặc định
                                name = ""
                                linkedHabitId = null
                                contribution = "1"
                                target = "10"

                                selectedTab = GoalFilterTab.ACTIVE
                            }
                        },
                        enabled = isFormValid,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Thêm mục tiêu", modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }

        // Bộ lọc danh sách mục tiêu
        if (goals.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val headerTitle = when (selectedTab) {
                        GoalFilterTab.ACTIVE -> "Đang thực hiện"
                        GoalFilterTab.COMPLETED -> "Đã hoàn thành 🎉"
                        GoalFilterTab.EXPIRED -> "Quá hạn ⏳"
                        GoalFilterTab.ALL -> "Tất cả mục tiêu"
                    }

                    Text(
                        text = "$headerTitle (${filteredGoals.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    var filterMenuExpanded by remember { mutableStateOf(false) }
                    Box {
                        FilledTonalButton(
                            onClick = { filterMenuExpanded = true },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.List, contentDescription = "Filter Icon", modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = when (selectedTab) {
                                    GoalFilterTab.ACTIVE -> "Đang thực hiện"
                                    GoalFilterTab.COMPLETED -> "Đã hoàn thành"
                                    GoalFilterTab.EXPIRED -> "Quá hạn"
                                    GoalFilterTab.ALL -> "Tất cả"
                                },
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        DropdownMenu(
                            expanded = filterMenuExpanded,
                            onDismissRequest = { filterMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Đang thực hiện") },
                                onClick = { selectedTab = GoalFilterTab.ACTIVE; filterMenuExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Đã hoàn thành") },
                                onClick = { selectedTab = GoalFilterTab.COMPLETED; filterMenuExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Quá hạn") },
                                onClick = { selectedTab = GoalFilterTab.EXPIRED; filterMenuExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Tất cả") },
                                onClick = { selectedTab = GoalFilterTab.ALL; filterMenuExpanded = false }
                            )
                        }
                    }
                }
            }

            if (filteredGoals.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (selectedTab) {
                                GoalFilterTab.ACTIVE -> "Không có mục tiêu nào đang thực hiện 🚀"
                                GoalFilterTab.COMPLETED -> "Chưa hoàn thành mục tiêu nào! 💪"
                                GoalFilterTab.EXPIRED -> "Tuyệt vời! Không có mục tiêu quá hạn 🎉"
                                GoalFilterTab.ALL -> "Danh sách trống"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredGoals, key = { it.id }) { goal ->
                    val isUpdatedToday = updatedGoalIdsToday.contains(goal.id)
                    GoalCardItem(
                        goal = goal,
                        currentEpochDay = currentEpochDay,
                        linkedHabitName = habits.find { it.id == goal.linkedHabitId }?.name,
                        isUpdatedToday = isUpdatedToday,
                        onAddProgress = { amount ->
                            vm.addGoalProgress(goal, amount)
                            updatedGoalIdsToday = updatedGoalIdsToday + goal.id
                        },
                        onDeleteGoal = { vm.deleteGoal(goal.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalCardItem(
    goal: GoalEntity,
    currentEpochDay: Long,
    linkedHabitName: String?,
    isUpdatedToday: Boolean,
    onAddProgress: (Double) -> Unit,
    onDeleteGoal: () -> Unit
) {
    val isCompleted = goal.currentValue >= goal.targetValue
    val isExpired = !isCompleted && goal.endEpochDay != null && goal.endEpochDay < currentEpochDay
    val progressRatio = (goal.currentValue / goal.targetValue).toFloat().coerceIn(0f, 1f)
    var customAddAmount by remember { mutableStateOf("1") }

    // State quản lý Dialog xác nhận xóa
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // --- DIALOG XÁC NHẬN XÓA ---
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "Xóa mục tiêu?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Mục tiêu \"${goal.name}\" sẽ bị xóa vĩnh viễn và không thể khôi phục.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteGoal()
                    }
                ) {
                    Text("Xóa", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Hủy")
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    val cardBgColor = when {
        isCompleted -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        isExpired -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = when {
        isCompleted -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        isExpired -> MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = cardBgColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // HÀNG 1: Tên + Tag trạng thái duy nhất
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    linkedHabitName?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "⚡ Tự động qua: $it",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = when {
                        isCompleted -> MaterialTheme.colorScheme.primaryContainer
                        isExpired -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = when {
                            isCompleted -> "Đã hoàn thành 🎉"
                            isExpired -> "Quá hạn ⏳"
                            else -> "Hạn: ${formatEpochDay(goal.endEpochDay)}"
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = when {
                            isCompleted -> MaterialTheme.colorScheme.onPrimaryContainer
                            isExpired -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // HÀNG 2: Thanh tiến độ
            LinearProgressIndicator(
                progress = { progressRatio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = if (isExpired) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )

            // HÀNG 3: Tiến độ con số + Nút cộng
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${goal.currentValue.toInt()} / ${goal.targetValue.toInt()} ${goal.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )

                if (!isCompleted && !isExpired && goal.linkedHabitId == null) {
                    if (isUpdatedToday) {
                        SuggestionChip(
                            onClick = { },
                            label = { Text("Đã cộng hôm nay ✅") },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    } else {
                        if (goal.metricType == GoalMetricType.OCCURRENCE_COUNT) {
                            Button(
                                onClick = { onAddProgress(1.0) },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("+1 ${goal.unit}")
                            }
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = customAddAmount,
                                    onValueChange = { customAddAmount = it },
                                    modifier = Modifier.width(65.dp).height(44.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                Button(
                                    onClick = {
                                        val amount = customAddAmount.toDoubleOrNull() ?: 1.0
                                        onAddProgress(amount)
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text("+ Thêm")
                                }
                            }
                        }
                    }
                }
            }

            // HÀNG 4: Nút Xóa (Kích hoạt Dialog hỏi lại)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { showDeleteConfirmDialog = true },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        "Xóa mục tiêu",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

private fun formatEpochDay(epochDay: Long?): String {
    if (epochDay == null) return "N/A"
    val millis = epochDay * 86400000L
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(millis))
}