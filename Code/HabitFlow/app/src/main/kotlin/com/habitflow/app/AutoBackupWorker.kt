package com.habitflow.app

import android.content.Context
import androidx.work.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class AutoBackupWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val database = HabitFlowDatabase.get(applicationContext)
            val backupManager = BackupManager(applicationContext, database)
            val jsonText = backupManager.exportBackup()

            val backupDir = File(applicationContext.filesDir, "auto_backups")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val backupFile = File(backupDir, "habitflow_autobackup_$timestamp.json")
            backupFile.writeText(jsonText, Charsets.UTF_8)

            // Giới hạn lưu tối đa 5 bản sao lưu tự động gần nhất để tối ưu bộ nhớ
            val existingBackups = backupDir.listFiles()?.sortedByDescending { it.lastModified() }
            if (existingBackups != null && existingBackups.size > 5) {
                existingBackups.drop(5).forEach { it.delete() }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "habitflow_auto_backup_work"

        fun schedulePeriodic(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            val request = PeriodicWorkRequestBuilder<AutoBackupWorker>(7, TimeUnit.DAYS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
