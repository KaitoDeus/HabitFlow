package com.habitflow.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// 1. VIEWMODEL & TRẠNG THÁI CÀI ĐẶT (SETTINGS UI STATE)
sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data class Success(val userPreferences: UserPreferences) : SettingsUiState
}

class SettingsViewModel(
    private val preferencesDataSource: UserPreferencesDataSource
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = preferencesDataSource.userPreferencesStream
        .map { SettingsUiState.Success(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsUiState.Loading
        )

    fun onThemeSelected(theme: AppTheme) {
        viewModelScope.launch { preferencesDataSource.updateAppTheme(theme) }
    }

    fun onColorThemeSelected(colorTheme: AppColorTheme) {
        viewModelScope.launch { preferencesDataSource.updateColorTheme(colorTheme) }
    }

    fun onNotificationToggled(enabled: Boolean) {
        viewModelScope.launch { preferencesDataSource.setNotificationEnabled(enabled) }
    }

    fun onReminderVibrateToggled(enabled: Boolean) {
        viewModelScope.launch { preferencesDataSource.setReminderVibrateEnabled(enabled) }
    }

    fun onHapticToggled(enabled: Boolean) {
        viewModelScope.launch { preferencesDataSource.setHapticEnabled(enabled) }
    }

    fun onAutoBackupToggled(context: Context, enabled: Boolean) {
        viewModelScope.launch {
            preferencesDataSource.setAutoBackupEnabled(enabled)
            if (enabled) {
                AutoBackupWorker.schedulePeriodic(context)
            } else {
                AutoBackupWorker.cancel(context)
            }
        }
    }

    fun onGreetingChanged(greeting: String) {
        viewModelScope.launch { preferencesDataSource.updateGreetingMessage(greeting) }
    }
}

// 2. COMPOSABLE: BỘ CHỌN MÀU SẮC & CHẾ ĐỘ SÁNG / TỐI
@Composable
fun ColorThemeSelector(
    currentColorTheme: AppColorTheme,
    onColorThemeSelected: (AppColorTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Tông màu chủ đạo (Accent Color)", style = MaterialTheme.typography.bodyMedium)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val themes = listOf(
                Triple(AppColorTheme.GREEN, "Xanh lá", Color(0xFF39FF14)),
                Triple(AppColorTheme.BLUE, "Xanh biển", Color(0xFF00E5FF)),
                Triple(AppColorTheme.PURPLE, "Tím tím", Color(0xFFD0BCFF)),
                Triple(AppColorTheme.ORANGE, "Cam ấm", Color(0xFFFF9100)),
                Triple(AppColorTheme.DYNAMIC, "Tự động", Color(0xFF888888))
            )
            themes.forEach { (theme, label, color) ->
                val isSelected = currentColorTheme == theme
                FilterChip(
                    selected = isSelected,
                    onClick = { onColorThemeSelected(theme) },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun ThemeToggleRow(
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = when (currentTheme) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        AppTheme.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Chế độ sáng/tối")
        Switch(
            checked = isDark,
            onCheckedChange = { checked ->
                onThemeSelected(if (checked) AppTheme.DARK else AppTheme.LIGHT)
            }
        )
    }
}

// 3. MÀN HÌNH CÀI ĐẶT CHÍNH (SETTINGS SCREEN)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf("") }
    var showBackupRestoreScreen by remember { mutableStateOf(false) }

    val database = remember { HabitFlowDatabase.get(context) }
    val backupManager = remember { BackupManager(context, database) }

    var selectedReminderForEdit by remember { mutableStateOf<ReminderEntity?>(null) }
    var showReminderDialog by remember { mutableStateOf(false) }

    val remindersFlow = remember { database.reminderDao().observeAllEnabled() }
    val activeReminders by remindersFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val habits by mainViewModel.habits.collectAsStateWithLifecycle()

    if (showBackupRestoreScreen) {
        Column(modifier = modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { showBackupRestoreScreen = false }) {
                    Text("← Quay lại Cài đặt")
                }
            }
            BackupRestoreScreen(
                onNavigateBack = { showBackupRestoreScreen = false }
            )
        }
        return
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val createDocument = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val json = backupManager.exportBackup()
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.write(json.toByteArray())
                    }
                    message = "Đã xuất dữ liệu sao lưu thành công"
                } catch (e: Exception) {
                    message = e.message ?: "Xuất thất bại"
                }
            }
        }
    }

    val openDocument = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                        ?: error("Không đọc được tệp")
                    backupManager.restoreBackup(text)
                    message = "Đã khôi phục dữ liệu thành công"
                } catch (e: Exception) {
                    message = e.message ?: "Khôi phục thất bại"
                }
            }
        }
    }

    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onNotificationToggled(granted)
        message = if (granted) "Đã cấp quyền thông báo thành công" else "Ứng dụng chưa được cấp quyền gửi thông báo"
    }

    if (showReminderDialog) {
        val habitToEdit = habits.firstOrNull { it.id == selectedReminderForEdit?.habitId } ?: habits.firstOrNull()
        if (habitToEdit != null) {
            ReminderEditorDialog(
                habitId = habitToEdit.id,
                habitName = habitToEdit.name,
                existingReminder = selectedReminderForEdit,
                onDismiss = {
                    showReminderDialog = false
                    selectedReminderForEdit = null
                },
                onSave = { reminder ->
                    scope.launch {
                        database.reminderDao().upsert(reminder)
                        if (reminder.enabled) {
                            ReminderScheduler.schedule(context, reminder, habitToEdit.name)
                        } else {
                            ReminderScheduler.cancel(context, reminder)
                        }
                        message = "Đã lưu giờ nhắc nhở cho: ${habitToEdit.name}"
                    }
                },
                onDelete = { reminderId ->
                    scope.launch {
                        selectedReminderForEdit?.let { ReminderScheduler.cancel(context, it) }
                        database.reminderDao().delete(reminderId)
                        message = "Đã xóa nhắc nhở"
                    }
                }
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Cài đặt", style = MaterialTheme.typography.headlineMedium)

        // 1. Sao lưu & Dữ liệu
        Text("Sao lưu & Dữ liệu", style = MaterialTheme.typography.titleMedium)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showBackupRestoreScreen = true },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Quản lý sao lưu & khôi phục", style = MaterialTheme.typography.titleMedium)
                    Text("Xuất hoặc nạp file sao lưu JSON an toàn cho dữ liệu của bạn", style = MaterialTheme.typography.bodySmall)
                }
                Text("→", style = MaterialTheme.typography.titleMedium)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { createDocument.launch("habitflow_backup.json") },
                modifier = Modifier.weight(1f)
            ) {
                Text("Xuất JSON")
            }

            OutlinedButton(
                onClick = { openDocument.launch(arrayOf("application/json", "text/plain")) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Khôi phục")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    scope.launch {
                        try {
                            BackupSharer.shareBackup(context, database)
                        } catch (e: Exception) {
                            message = "Chia sẻ thất bại: ${e.message}"
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Chia sẻ sao lưu")
            }

            OutlinedButton(
                onClick = {
                    scope.launch {
                        try {
                            ReportExporter.shareCsvReport(context, database)
                        } catch (e: Exception) {
                            message = "Xuất báo cáo thất bại: ${e.message}"
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Xuất báo cáo CSV")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Tự động sao lưu định kỳ (7 ngày)", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Tự động trích xuất bản sao lưu vào bộ nhớ máy mỗi tuần",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = (uiState as? SettingsUiState.Success)?.userPreferences?.isAutoBackupEnabled ?: false,
                onCheckedChange = { enabled ->
                    viewModel.onAutoBackupToggled(context, enabled)
                }
            )
        }

        if (message.isNotBlank()) {
            Text(message, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // 2. Nhắc nhở thói quen
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Nhắc nhở thói quen", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    val habit = habits.firstOrNull()
                    val isVibrate = (uiState as? SettingsUiState.Success)?.userPreferences?.isReminderVibrateEnabled ?: true
                    NotificationHelper.showNotification(
                        context = context,
                        notificationId = 888,
                        habitId = habit?.id ?: "",
                        habitName = habit?.name ?: "Đọc sách 30 phút",
                        note = "Đã đến giờ thực hiện thói quen của bạn!",
                        vibrate = isVibrate
                    )
                    message = "Đã gửi thông báo nhắc nhở thử nghiệm"
                }) {
                    Text("Thử chuông")
                }
                if (habits.isNotEmpty()) {
                    Button(onClick = {
                        selectedReminderForEdit = null
                        showReminderDialog = true
                    }) {
                        Text("+ Thêm")
                    }
                }
            }
        }

        if (habits.isEmpty()) {
            Text("Chưa có thói quen nào để tạo lịch nhắc nhở.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else if (activeReminders.isEmpty()) {
            Text("Hiện chưa có nhắc nhở nào đang hoạt động.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            activeReminders.forEach { reminder ->
                val habit = habits.firstOrNull { it.id == reminder.habitId }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedReminderForEdit = reminder
                            showReminderDialog = true
                        },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = habit?.name ?: "Thói quen",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Thời gian: %02d:%02d".format(reminder.hour, reminder.minute),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text("Chỉnh sửa", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // 3. Hệ thống & Cá nhân hóa giao diện
        Text("Hệ thống", style = MaterialTheme.typography.titleMedium)

        when (val state = uiState) {
            is SettingsUiState.Loading -> {
                CircularProgressIndicator()
            }
            is SettingsUiState.Success -> {
                val prefs = state.userPreferences

                ThemeToggleRow(
                    currentTheme = prefs.appTheme,
                    onThemeSelected = { newTheme ->
                        viewModel.onThemeSelected(newTheme)
                    }
                )

                ColorThemeSelector(
                    currentColorTheme = prefs.colorTheme,
                    onColorThemeSelected = { newColorTheme ->
                        viewModel.onColorThemeSelected(newColorTheme)
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Cấp quyền thông báo")
                    Switch(
                        checked = if (Build.VERSION.SDK_INT >= 33) {
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED && prefs.isNotificationEnabled
                        } else {
                            prefs.isNotificationEnabled
                        },
                        onCheckedChange = { enabled ->
                            if (enabled && Build.VERSION.SDK_INT >= 33) {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                                if (!hasPermission) {
                                    notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    return@Switch
                                }
                            }
                            viewModel.onNotificationToggled(enabled)
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Rung khi có thông báo nhắc nhở")
                    Switch(
                        checked = prefs.isReminderVibrateEnabled,
                        onCheckedChange = { enabled ->
                            viewModel.onReminderVibrateToggled(enabled)
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Rung phản hồi khi hoàn thành")
                    Switch(
                        checked = prefs.isHapticEnabled,
                        onCheckedChange = { enabled ->
                            viewModel.onHapticToggled(enabled)
                        }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text("Lời chào khi mở ứng dụng", style = MaterialTheme.typography.titleMedium)

                var tempGreeting by remember(prefs.greetingMessage) { mutableStateOf(prefs.greetingMessage) }

                OutlinedTextField(
                    value = tempGreeting,
                    onValueChange = { tempGreeting = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Lời chào") },
                    trailingIcon = {
                        if (tempGreeting != prefs.greetingMessage) {
                            TextButton(onClick = { viewModel.onGreetingChanged(tempGreeting) }) {
                                Text("Lưu")
                            }
                        }
                    }
                )
            }
        }
    }
}
