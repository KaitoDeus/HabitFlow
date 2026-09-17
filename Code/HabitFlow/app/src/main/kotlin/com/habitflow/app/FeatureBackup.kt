package com.habitflow.app

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.room.withTransaction
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// 1. KẾT QUẢ KIỂM THỰC DỮ LIỆU SAO LƯU
sealed class ValidationResult {
    data object Valid : ValidationResult()
    data class Invalid(val reason: String) : ValidationResult()
}

// 2. BỘ KIỂM THỰC AN TOÀN (BACKUP VALIDATOR)
object BackupValidator {
    const val CURRENT_SUPPORTED_VERSION = 2

    fun validate(data: BackupData): ValidationResult {
        // 1. Kiểm tra tính tương thích phiên bản schema
        if (data.version > CURRENT_SUPPORTED_VERSION) {
            return ValidationResult.Invalid(
                "Phiên bản tệp sao lưu (v${data.version}) mới hơn phiên bản ứng dụng hiện tại (v$CURRENT_SUPPORTED_VERSION). Vui lòng cập nhật ứng dụng!"
            )
        }

        // 2. Kiểm tra danh sách thói quen: không được để trống ID hoặc Tên
        data.habits.forEach { habit ->
            if (habit.id.isBlank()) {
                return ValidationResult.Invalid("Dữ liệu thói quen chứa ID không hợp lệ (trống).")
            }
            if (habit.name.isBlank()) {
                return ValidationResult.Invalid("Tên thói quen không được để trống.")
            }
        }

        // 3. Kiểm tra tính toàn vẹn khóa ngoại (Foreign Key integrity)
        val habitIds = data.habits.map { it.id }.toSet()

        data.occurrences.forEach { occurrence ->
            if (occurrence.habitId !in habitIds) {
                return ValidationResult.Invalid(
                    "Lịch sử thực hiện chứa dữ liệu không thuộc bất kỳ thói quen nào trong bản sao lưu."
                )
            }
        }

        data.reminders.forEach { reminder ->
            if (reminder.habitId !in habitIds) {
                return ValidationResult.Invalid(
                    "Báo thức nhắc nhở chứa liên kết đến thói quen không tồn tại."
                )
            }
            if (reminder.hour !in 0..23 || reminder.minute !in 0..59) {
                return ValidationResult.Invalid(
                    "Giờ nhắc nhở không hợp lệ (${reminder.hour}:${reminder.minute})."
                )
            }
        }

        return ValidationResult.Valid
    }
}

// 3. QUẢN LÝ QUY TRÌNH SAO LƯU & KHÔI PHỤC
class BackupManager(
    private val context: Context,
    private val database: HabitFlowDatabase
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * Xuất toàn bộ dữ liệu Room DB thành chuỗi JSON
     */
    suspend fun exportBackup(): String {
        val backupData = BackupData(
            version = BackupValidator.CURRENT_SUPPORTED_VERSION,
            exportedAt = System.currentTimeMillis(),
            habits = database.habitDao().all(),
            occurrences = database.occurrenceDao().all(),
            goals = database.goalDao().all(),
            reminders = database.reminderDao().all(),
            userStats = database.userStatsDao().get()
        )
        return json.encodeToString(backupData)
    }

    /**
     * Khôi phục chuỗi JSON vào Room DB có bảo vệ Transaction Rollback
     * và tự động lên lịch lại toàn bộ báo thức với Android AlarmManager
     */
    suspend fun restoreBackup(jsonText: String) {
        val backupData = json.decodeFromString<BackupData>(jsonText)

        // 1. Kiểm tra tính toàn vẹn dữ liệu
        when (val validation = BackupValidator.validate(backupData)) {
            is ValidationResult.Invalid -> error(validation.reason)
            is ValidationResult.Valid -> {
                // 2. Thực thi Transaction nạp dữ liệu an toàn (ACID - Rollback nếu lỗi)
                database.withTransaction {
                    database.userStatsDao().clear()
                    database.reminderDao().clear()
                    database.goalDao().clear()
                    database.occurrenceDao().clear()
                    database.habitDao().clear()

                    database.habitDao().upsertAll(backupData.habits)
                    database.occurrenceDao().upsertAll(backupData.occurrences)
                    database.goalDao().upsertAll(backupData.goals)
                    database.reminderDao().upsertAll(backupData.reminders)
                    backupData.userStats?.let { database.userStatsDao().upsert(it) }
                }

                // 3. Tự động lên lịch lại tất cả báo thức đang bật sau khi khôi phục
                val habitMap = backupData.habits.associateBy { it.id }
                backupData.reminders.filter { it.enabled }.forEach { reminder ->
                    val habitName = habitMap[reminder.habitId]?.name ?: "Thói quen hàng ngày"
                    ReminderScheduler.schedule(context, reminder, habitName)
                }
            }
        }
    }
}

// 4. CHIA SẺ BẢN SAO LƯU NHANH QUA INTENT (FILEPROVIDER)
object BackupSharer {
    suspend fun shareBackup(context: Context, database: HabitFlowDatabase) {
        val backupManager = BackupManager(context, database)
        val jsonText = backupManager.exportBackup()

        val fileName = "habitflow_backup_${System.currentTimeMillis() / 1000}.json"
        val cacheFile = java.io.File(context.cacheDir, fileName)
        cacheFile.writeText(jsonText, Charsets.UTF_8)

        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            cacheFile
        )

        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            clipData = android.content.ClipData.newRawUri(null, uri)
            putExtra(android.content.Intent.EXTRA_SUBJECT, "Bản sao lưu HabitFlow")
            putExtra(
                android.content.Intent.EXTRA_TEXT,
                "Đính kèm bản sao lưu dữ liệu HabitFlow ngày " +
                    java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
            )
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = android.content.Intent.createChooser(shareIntent, "Chia sẻ bản sao lưu HabitFlow qua...")
        chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(chooser)
    }
}

// 5. XUẤT BÁO CÁO TIẾN ĐỘ THÓI QUEN RA FILE CSV (EXCEL)
object ReportExporter {
    suspend fun generateCsvReport(database: HabitFlowDatabase): String {
        val habits = database.habitDao().all().associateBy { it.id }
        val occurrences = database.occurrenceDao().all()

        val sb = StringBuilder()
        // Ký tự BOM (\uFEFF) giúp Microsoft Excel hiển thị tiếng Việt UTF-8 chính xác
        sb.append('\uFEFF')
        sb.append("Tên thói quen,Ngày thực hiện,Trạng thái,Giá trị hoàn thành,Ghi chú\n")

        val dateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")

        occurrences.sortedByDescending { it.scheduledEpochDay }.forEach { occ ->
            val habitName = (habits[occ.habitId]?.name ?: "Thói quen ID: ${occ.habitId}").replace("\"", "\"\"")
            val dateStr = java.time.LocalDate.ofEpochDay(occ.scheduledEpochDay).format(dateFormatter)
            val statusStr = when (occ.status) {
                OccurrenceStatus.COMPLETED -> "Hoàn thành"
                OccurrenceStatus.SKIPPED -> "Bỏ qua"
                OccurrenceStatus.MISSED -> "Bỏ lỡ"
                OccurrenceStatus.FROZEN -> "Đóng băng"
                OccurrenceStatus.PENDING -> "Chưa thực hiện"
            }
            val valueStr = occ.completedValue?.toString() ?: ""
            val noteStr = (occ.note ?: "").replace("\"", "\"\"")

            sb.append("\"$habitName\",")
            sb.append("\"$dateStr\",")
            sb.append("\"$statusStr\",")
            sb.append("\"$valueStr\",")
            sb.append("\"$noteStr\"\n")
        }
        return sb.toString()
    }

    suspend fun shareCsvReport(context: Context, database: HabitFlowDatabase) {
        val csvText = generateCsvReport(database)
        val fileName = "habitflow_report_${System.currentTimeMillis() / 1000}.csv"
        val cacheFile = java.io.File(context.cacheDir, fileName)
        cacheFile.writeText(csvText, Charsets.UTF_8)

        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            cacheFile
        )

        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            clipData = android.content.ClipData.newRawUri(null, uri)
            putExtra(android.content.Intent.EXTRA_SUBJECT, "Báo cáo tiến độ thói quen HabitFlow")
            putExtra(
                android.content.Intent.EXTRA_TEXT,
                "Đính kèm báo cáo tiến độ thói quen định dạng CSV (Excel) xuất từ HabitFlow."
            )
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = android.content.Intent.createChooser(shareIntent, "Xuất báo cáo CSV qua...")
        chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(chooser)
    }
}

// 5. MÀN HÌNH SAO LƯU & KHÔI PHỤC CHUYÊN SÂU
@Composable
fun BackupRestoreScreen(
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { HabitFlowDatabase.get(context) }
    val backupManager = remember { BackupManager(context, database) }

    var statusMessage by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(true) }
    var showConfirmRestoreDialog by remember { mutableStateOf(false) }
    var selectedRestoreUri by remember { mutableStateOf<Uri?>(null) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val jsonText = backupManager.exportBackup()
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.write(jsonText.toByteArray())
                    }
                    val timestamp = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault()).format(Date())
                    statusMessage = "Đã xuất dữ liệu sao lưu thành công lúc $timestamp"
                    isSuccess = true
                } catch (e: Exception) {
                    statusMessage = "Xuất dữ liệu thất bại: ${e.message}"
                    isSuccess = false
                }
            }
        }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            selectedRestoreUri = uri
            showConfirmRestoreDialog = true
        }
    }

    if (showConfirmRestoreDialog && selectedRestoreUri != null) {
        AlertDialog(
            onDismissRequest = { showConfirmRestoreDialog = false },
            title = { Text("Xác nhận khôi phục dữ liệu") },
            text = {
                Text("Quá trình khôi phục sẽ ghi đè toàn bộ dữ liệu hiện tại bằng dữ liệu từ file sao lưu. Bạn có chắc chắn muốn tiếp tục không?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val uri = selectedRestoreUri!!
                        showConfirmRestoreDialog = false
                        scope.launch {
                            try {
                                val jsonText = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                                    ?: error("Không đọc được tệp sao lưu")
                                backupManager.restoreBackup(jsonText)
                                statusMessage = "Khôi phục dữ liệu thành công! Toàn bộ thói quen và báo thức đã được cập nhật."
                                isSuccess = true
                            } catch (e: Exception) {
                                statusMessage = "Khôi phục thất bại: ${e.message}"
                                isSuccess = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Đồng ý ghi đè")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmRestoreDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Sao lưu & Khôi phục", style = MaterialTheme.typography.headlineMedium)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Xuất bản sao lưu (Export JSON)", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Trích xuất toàn bộ danh sách thói quen, lịch sử thực hiện, mục tiêu và giờ báo thức thành file JSON an toàn.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(
                    onClick = { createDocumentLauncher.launch("habitflow_backup_${System.currentTimeMillis() / 1000}.json") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Xuất file sao lưu")
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Chia sẻ bản sao lưu nhanh (FileProvider)", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Gửi file sao lưu trực tiếp qua ứng dụng khác (Zalo, Gmail, Drive) một cách an toàn và tiện lợi.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                BackupSharer.shareBackup(context, database)
                            } catch (e: Exception) {
                                statusMessage = "Chia sẻ thất bại: ${e.message}"
                                isSuccess = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Chia sẻ file sao lưu")
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Xuất báo cáo tiến độ (CSV / Excel)", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Trích xuất bảng lịch sử thực hiện các thói quen dưới định dạng CSV hỗ trợ tiếng Việt có dấu trong Excel.",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            try {
                                ReportExporter.shareCsvReport(context, database)
                            } catch (e: Exception) {
                                statusMessage = "Xuất báo cáo thất bại: ${e.message}"
                                isSuccess = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Xuất & Chia sẻ báo cáo CSV")
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Khôi phục từ bản sao lưu (Restore JSON)", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Nạp lại toàn bộ dữ liệu từ file sao lưu JSON trước đó. Hệ thống sẽ tự động kiểm tra tính toàn vẹn và lập lịch lại các báo thức.",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedButton(
                    onClick = { openDocumentLauncher.launch(arrayOf("application/json", "text/plain")) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Chọn file sao lưu để khôi phục")
                }
            }
        }

        if (statusMessage.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSuccess) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = statusMessage,
                    modifier = Modifier.padding(16.dp),
                    color = if (isSuccess) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
