package com.habitflow.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
    val goals by vm.goals.collectAsStateWithLifecycle(initialValue = emptyList())
    val habits by vm.habits.collectAsStateWithLifecycle(initialValue = emptyList())
    val testOffset by vm.testDateOffset.collectAsStateWithLifecycle(initialValue = 0)

    val currentEpochDay = remember(testOffset) { LocalDate.now().plusDays(testOffset.toLong()).toEpochDay() }

    var showAddGoalSheet by rememberSaveable { mutableStateOf(false) }
    var selectedTab by rememberSaveable { mutableStateOf(GoalFilterTab.ACTIVE) }

    // State quản lý mục tiêu đang xem/chỉnh sửa
    var selectedGoalForEdit by remember { mutableStateOf<GoalEntity?>(null) }

    // Đưa mục tiêu mới nhất lên đầu
    val filteredGoals = remember(goals, selectedTab, currentEpochDay) {
        when (selectedTab) {
            GoalFilterTab.ACTIVE -> goals.filter {
                it.currentValue < it.targetValue && (it.endEpochDay == null || it.endEpochDay >= currentEpochDay)
            }
            GoalFilterTab.COMPLETED -> goals.filter { it.currentValue >= it.targetValue }
            GoalFilterTab.EXPIRED -> goals.filter {
                it.currentValue < it.targetValue && (it.endEpochDay != null && it.endEpochDay < currentEpochDay)
            }
            GoalFilterTab.ALL -> goals
        }
    }

    // Chi tiết mục tiêu và chỉnh sửa
    selectedGoalForEdit?.let { goal ->
        GoalDetailEditDialog(
            goal = goal,
            habits = habits,
            currentEpochDay = currentEpochDay,
            onDismiss = { selectedGoalForEdit = null },
            onSaveUpdate = { updatedGoal ->
                vm.updateGoal(updatedGoal)
                selectedGoalForEdit = null
            }
        )
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddGoalSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tạo mục tiêu")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 18.dp,
                    end = 18.dp,
                    top = 0.dp,
                    bottom = paddingValues.calculateBottomPadding()
                ),
            contentPadding = PaddingValues(
                top = 10.dp,
                bottom = 320.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                ScreenHeader(
                    eyebrow = "Thử thách",
                    title = "Mục tiêu",
                    subtitle = "Đừng để những dự định, chỉ nằm trong suy nghĩ."
                )
            }
            // Phần thống kê mục tiêu
            item {
                GoalSummaryCard(goals = goals, todayEpochDay = currentEpochDay)
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp, bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val headerTitle = when (selectedTab) {
                        GoalFilterTab.ACTIVE -> "Đang thực hiện"
                        GoalFilterTab.COMPLETED -> "Đã hoàn thành"
                        GoalFilterTab.EXPIRED -> "Quá hạn"
                        GoalFilterTab.ALL -> "Tất cả"
                    }

                    Text(
                        text = "$headerTitle (${filteredGoals.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    var filterMenuExpanded by remember { mutableStateOf(false) }
                    Box {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.clip(RoundedCornerShape(12.dp))
                        ) {
                            IconButton(
                                onClick = { filterMenuExpanded = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.List,
                                    contentDescription = "Lọc",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = filterMenuExpanded,
                            onDismissRequest = { filterMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("🚀 Đang thực hiện") },
                                onClick = { selectedTab = GoalFilterTab.ACTIVE; filterMenuExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("🎉 Đã hoàn thành") },
                                onClick = { selectedTab = GoalFilterTab.COMPLETED; filterMenuExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("⏳ Quá hạn") },
                                onClick = { selectedTab = GoalFilterTab.EXPIRED; filterMenuExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("📌 Tất cả mục tiêu") },
                                onClick = { selectedTab = GoalFilterTab.ALL; filterMenuExpanded = false }
                            )
                        }
                    }
                }
            }

            if (filteredGoals.isEmpty()) {
                item {
                    val (icon, title, subtitle) = when (selectedTab) {
                        GoalFilterTab.ACTIVE -> Triple("🎯", "Chưa có mục tiêu", "Bấm nút (+) góc dưới để đặt mục tiêu mới.")
                        GoalFilterTab.COMPLETED -> Triple("🎉", "Chưa hoàn thành mục tiêu nào", "Cố gắng kiên trì thực hiện nhé!")
                        GoalFilterTab.EXPIRED -> Triple("✨", "Không có mục tiêu quá hạn", "Bạn đang quản lý thời gian rất tốt.")
                        GoalFilterTab.ALL -> Triple("📌", "Danh sách mục tiêu trống", "Thêm mục tiêu đầu tiên ngay nào.")
                    }
                    EmptyCompactCard(
                        icon = icon,
                        title = title,
                        subtitle = subtitle
                    )
                }
            } else {
                items(filteredGoals, key = { it.id }) { goal ->
                    val isUpdatedToday = goal.lastUpdatedEpochDay == currentEpochDay
                    GoalManageCard(
                        goal = goal,
                        currentEpochDay = currentEpochDay,
                        linkedHabitName = habits.find { it.id == goal.linkedHabitId }?.name,
                        isUpdatedToday = isUpdatedToday,
                        onAddProgress = { amount ->
                            vm.addGoalProgress(goal, amount)
                        },
                        onDeleteGoal = { vm.deleteGoal(goal.id) },
                        onClick = { selectedGoalForEdit = goal }
                    )
                }
            }
        }
    }

    if (showAddGoalSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddGoalSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            AddGoalBottomSheetContent(
                currentEpochDay = currentEpochDay,
                habits = habits,
                onAddGoal = { newGoal ->
                    vm.addGoal(
                        name = newGoal.name,
                        target = newGoal.target,
                        type = newGoal.type,
                        periodType = GoalPeriodType.CUSTOM,
                        unit = newGoal.unit,
                        startEpochDay = newGoal.startEpochDay,
                        endEpochDay = newGoal.endEpochDay,
                        linkedHabitId = newGoal.linkedHabitId,
                        contributionValue = newGoal.contributionValue
                    )
                    selectedTab = GoalFilterTab.ACTIVE
                    showAddGoalSheet = false
                }
            )
        }
    }
}
@Composable
fun GoalSummaryCard(goals: List<GoalEntity>, todayEpochDay: Long = LocalDate.now().toEpochDay()) {
    val totalGoals = goals.size
    val completedGoals = goals.count { it.currentValue >= it.targetValue }

    val activeGoals = goals.count { goal ->
        goal.currentValue < goal.targetValue && (goal.endEpochDay == null || goal.endEpochDay >= todayEpochDay)
    }
    val overdueGoals = goals.count { goal ->
        goal.currentValue < goal.targetValue && (goal.endEpochDay != null && goal.endEpochDay < todayEpochDay)
    }

    // Tính trung bình % tiến độ của tất cả mục tiêu
    val overallProgress = if (totalGoals > 0) {
        goals.sumOf { (it.currentValue / it.targetValue).coerceAtMost(1.0) }.toFloat() / totalGoals
    } else 0f

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Tiêu đề + Phần trăm tiến độ thực tế
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TỔNG QUAN MỤC TIÊU",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${(overallProgress * 100).toInt()}% Tiến độ",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Thanh tiến độ tổng hợp
            LinearProgressIndicator(
                progress = { overallProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )

            // Thống kê
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryMetricBadge(
                    label = "Cần làm",
                    value = activeGoals.toString(),
                    color = MaterialTheme.colorScheme.primary,
                    bgColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                )
                SummaryMetricBadge(
                    label = "Hoàn thành",
                    value = completedGoals.toString(),
                    color = MaterialTheme.colorScheme.tertiary,
                    bgColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                )
                SummaryMetricBadge(
                    label = "Quá hạn",
                    value = overdueGoals.toString(),
                    color = MaterialTheme.colorScheme.error,
                    bgColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                )
                SummaryMetricBadge(
                    label = "Tổng số",
                    value = totalGoals.toString(),
                    color = MaterialTheme.colorScheme.onSurface,
                    bgColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
@Composable
private fun SummaryMetricBadge(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    bgColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = bgColor
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun GoalManageCard(
    goal: GoalEntity,
    currentEpochDay: Long,
    linkedHabitName: String?,
    isUpdatedToday: Boolean,
    onAddProgress: (Double) -> Unit,
    onDeleteGoal: () -> Unit,
    onClick: () -> Unit
) {
    val isCompleted = goal.currentValue >= goal.targetValue
    val isExpired = !isCompleted && goal.endEpochDay != null && goal.endEpochDay < currentEpochDay
    val progressRatio = (goal.currentValue / goal.targetValue).toFloat().coerceIn(0f, 1f)
    var customAddAmount by remember { mutableStateOf("1") }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            shape = RoundedCornerShape(24.dp),
            title = { Text("Xóa mục tiêu?", fontWeight = FontWeight.Bold) },
            text = { Text("\"${goal.name}\" sẽ bị xóa vĩnh viễn và không thể khôi phục.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteGoal()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Xóa") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) { Text("Hủy") }
            }
        )
    }

    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = when {
                        isCompleted -> MaterialTheme.colorScheme.primaryContainer
                        isExpired -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.secondaryContainer
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isCompleted) "🎉" else if (isExpired) "⏳" else "🎯",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    linkedHabitName?.let {
                        Text(
                            text = "⚡ Liên kết: $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = when {
                        isCompleted -> MaterialTheme.colorScheme.primaryContainer
                        isExpired -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = when {
                            isCompleted -> "Hoàn thành"
                            isExpired -> "Quá hạn"
                            else -> "Hạn: ${formatEpochDay(goal.endEpochDay)}"
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = when {
                            isCompleted -> MaterialTheme.colorScheme.onPrimaryContainer
                            isExpired -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            // Thanh Tiến Độ
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val locale = Locale.getDefault()
                    val formattedCurrent = if (goal.currentValue % 1.0 == 0.0) goal.currentValue.toInt().toString() else String.format(locale, "%.1f", goal.currentValue)
                    val formattedTarget = if (goal.targetValue % 1.0 == 0.0) goal.targetValue.toInt().toString() else String.format(locale, "%.1f", goal.targetValue)

                    Text(
                        text = "$formattedCurrent / $formattedTarget ${goal.unit}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )

                    if (!isCompleted && !isExpired && goal.linkedHabitId == null) {
                        if (isUpdatedToday) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Đã tích hôm nay ",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "✓",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            if (goal.metricType == GoalMetricType.OCCURRENCE_COUNT) {
                                Button(
                                    onClick = { onAddProgress(1.0) },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text("+1 ${goal.unit}", style = MaterialTheme.typography.labelMedium)
                                }
                            } else {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(60.dp)
                                            .height(36.dp)
                                            .border(
                                                1.dp,
                                                MaterialTheme.colorScheme.outline,
                                                RoundedCornerShape(10.dp)
                                            )
                                            .background(
                                                MaterialTheme.colorScheme.surface,
                                                RoundedCornerShape(10.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        BasicTextField(
                                            value = customAddAmount,
                                            onValueChange = { customAddAmount = it },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            singleLine = true,
                                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                                textAlign = TextAlign.Center,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            val amount = customAddAmount.toDoubleOrNull() ?: 1.0
                                            onAddProgress(amount)
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text("+ Thêm", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { showDeleteConfirmDialog = true },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text(
                        "Xóa mục tiêu",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalDetailEditDialog(
    goal: GoalEntity,
    habits: List<HabitEntity>,
    currentEpochDay: Long,
    onDismiss: () -> Unit,
    onSaveUpdate: (GoalEntity) -> Unit
) {
    // Điều kiện để chỉnh sửa goal
    val isCompleted = goal.currentValue >= goal.targetValue
    val isExpired = !isCompleted && goal.endEpochDay != null && goal.endEpochDay < currentEpochDay
    val isEditable = !isCompleted && !isExpired

    var name by rememberSaveable(goal) { mutableStateOf(goal.name) }
    var target by rememberSaveable(goal) { mutableStateOf(if (goal.targetValue % 1.0 == 0.0) goal.targetValue.toInt().toString() else goal.targetValue.toString()) }
    var unit by rememberSaveable(goal) { mutableStateOf(goal.unit) }

    var startEpochDay by rememberSaveable(goal) { mutableLongStateOf(goal.startEpochDay) }
    var endEpochDay by rememberSaveable(goal) { mutableLongStateOf(goal.endEpochDay ?: (currentEpochDay + 7)) }
    var datePickerTarget by rememberSaveable { mutableStateOf<String?>(null) }

    var linkedHabitId by rememberSaveable(goal) { mutableStateOf(goal.linkedHabitId) }
    var contribution by rememberSaveable(goal) { mutableStateOf(if (goal.contributionValue % 1.0 == 0.0) goal.contributionValue.toInt().toString() else goal.contributionValue.toString()) }
    var expandedHabitMenu by remember { mutableStateOf(false) }

    val targetVal = target.toDoubleOrNull()
    val contribVal = contribution.toDoubleOrNull()
    val isFormValid = remember(name, targetVal, linkedHabitId, contribVal) {
        name.isNotBlank() && targetVal != null && targetVal > 0 && (linkedHabitId == null || (contribVal != null && contribVal > 0))
    }

    if (datePickerTarget != null && isEditable) {
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

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = if (isEditable) "Chỉnh sửa mục tiêu" else "Chi tiết mục tiêu",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (isEditable) name = it },
                    readOnly = !isEditable,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Tên mục tiêu") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = target,
                        onValueChange = { if (isEditable) target = it },
                        readOnly = !isEditable,
                        modifier = Modifier.weight(1f),
                        label = { Text("Mục tiêu") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { if (isEditable) unit = it },
                        readOnly = !isEditable,
                        modifier = Modifier.weight(1f),
                        label = { Text("Đơn vị") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { if (isEditable) datePickerTarget = "START" },
                        enabled = isEditable
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Từ: ${formatEpochDay(startEpochDay)}", style = MaterialTheme.typography.bodySmall)
                    }

                    TextButton(
                        onClick = { if (isEditable) datePickerTarget = "END" },
                        enabled = isEditable
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Đến: ${formatEpochDay(endEpochDay)}", style = MaterialTheme.typography.bodySmall)
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = expandedHabitMenu && isEditable,
                    onExpandedChange = { if (isEditable) expandedHabitMenu = !expandedHabitMenu }
                ) {
                    val selectedHabitName = habits.find { it.id == linkedHabitId }?.name ?: "Không liên kết"
                    OutlinedTextField(
                        value = "🔗 $selectedHabitName",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                        shape = RoundedCornerShape(14.dp),
                        trailingIcon = {
                            if (isEditable) ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedHabitMenu)
                        }
                    )
                    if (isEditable) {
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
                        onValueChange = { if (isEditable) contribution = it },
                        readOnly = !isEditable,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Điểm cộng mỗi lần hoàn thành thói quen") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }
        },
        confirmButton = {
            if (isEditable) {
                Button(
                    onClick = {
                        if (isFormValid) {
                            val updatedGoal = goal.copy(
                                name = name.trim(),
                                targetValue = targetVal!!,
                                unit = unit.trim(),
                                startEpochDay = startEpochDay,
                                endEpochDay = endEpochDay,
                                linkedHabitId = linkedHabitId,
                                contributionValue = contribVal ?: 1.0
                            )
                            onSaveUpdate(updatedGoal)
                        }
                    },
                    enabled = isFormValid,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Lưu thay đổi")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isEditable) "Hủy" else "Đóng")
            }
        }
    )
}

private data class CreateGoalData(
    val name: String,
    val target: Double,
    val unit: String,
    val type: GoalMetricType,
    val startEpochDay: Long,
    val endEpochDay: Long,
    val linkedHabitId: String?,
    val contributionValue: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddGoalBottomSheetContent(
    currentEpochDay: Long,
    habits: List<HabitEntity>,
    onAddGoal: (CreateGoalData) -> Unit
) {
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

    val targetVal = target.toDoubleOrNull()
    val contribVal = contribution.toDoubleOrNull()
    val isFormValid = remember(name, targetVal, linkedHabitId, contribVal) {
        name.isNotBlank() && targetVal != null && targetVal > 0 && (linkedHabitId == null || (contribVal != null && contribVal > 0))
    }

    LaunchedEffect(startEpochDay, deadlineOption) {
        if (deadlineOption == GoalDeadlineOption.THIS_WEEK) {
            endEpochDay = startEpochDay + 7
        } else if (deadlineOption == GoalDeadlineOption.THIS_MONTH) {
            endEpochDay = startEpochDay + 30
        }
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Mục tiêu mới",
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
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = type == GoalMetricType.ACCUMULATED_VALUE,
                    onClick = { type = GoalMetricType.ACCUMULATED_VALUE },
                    label = { Text("Tích lũy") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Thời hạn", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = deadlineOption == GoalDeadlineOption.THIS_WEEK,
                    onClick = { deadlineOption = GoalDeadlineOption.THIS_WEEK },
                    label = { Text("Tuần") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = deadlineOption == GoalDeadlineOption.THIS_MONTH,
                    onClick = { deadlineOption = GoalDeadlineOption.THIS_MONTH },
                    label = { Text("Tháng") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = deadlineOption == GoalDeadlineOption.CUSTOM,
                    onClick = { deadlineOption = GoalDeadlineOption.CUSTOM },
                    label = { Text("Tùy chỉnh") },
                    shape = RoundedCornerShape(12.dp),
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
                    Text("Từ: ${formatEpochDay(startEpochDay)}", style = MaterialTheme.typography.bodySmall)
                }

                TextButton(
                    onClick = { datePickerTarget = "END" },
                    enabled = deadlineOption == GoalDeadlineOption.CUSTOM
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Đến: ${formatEpochDay(endEpochDay)}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Liên kết Thói quen", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)

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
                label = { Text("Điểm cộng mỗi lần hoàn thành thói quen") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )
        }

        Spacer(Modifier.height(4.dp))

        Button(
            onClick = {
                if (isFormValid) {
                    onAddGoal(
                        CreateGoalData(
                            name = name,
                            target = targetVal!!,
                            unit = unit,
                            type = type,
                            startEpochDay = startEpochDay,
                            endEpochDay = endEpochDay,
                            linkedHabitId = linkedHabitId,
                            contributionValue = contribVal ?: 1.0
                        )
                    )
                }
            },
            enabled = isFormValid,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Thêm mục tiêu")
        }
    }
}

private fun formatEpochDay(epochDay: Long?): String {
    if (epochDay == null) return "N/A"
    val millis = epochDay * 86400000L
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(millis))
}