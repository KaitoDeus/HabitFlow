package com.habitflow.app

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// 1. CÁC ENUM VÀ MODEL CẤU HÌNH GIAO DIỆN & CÀI ĐẶT
enum class AppTheme { SYSTEM, LIGHT, DARK }

enum class AppColorTheme { GREEN, BLUE, PURPLE, ORANGE, DYNAMIC }

data class UserPreferences(
    val appTheme: AppTheme = AppTheme.SYSTEM,
    val colorTheme: AppColorTheme = AppColorTheme.GREEN,
    val isNotificationEnabled: Boolean = true,
    val isReminderVibrateEnabled: Boolean = true,
    val isHapticEnabled: Boolean = true,
    val isAutoBackupEnabled: Boolean = false,
    val greetingMessage: String = ""
)

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

// 2. DATASTORE QUẢN LÝ LƯU TRỮ VÀ ĐỒNG BỘ CÀI ĐẶT
class UserPreferencesDataSource(private val context: Context) {
    private object Keys {
        val APP_THEME = stringPreferencesKey("app_theme")
        val COLOR_THEME = stringPreferencesKey("color_theme")
        val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
        val REMINDER_VIBRATE_ENABLED = booleanPreferencesKey("reminder_vibrate_enabled")
        val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        val AUTO_BACKUP_ENABLED = booleanPreferencesKey("auto_backup_enabled")
        val GREETING_MESSAGE = stringPreferencesKey("greeting_message")
    }

    val userPreferencesStream: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            val themeName = preferences[Keys.APP_THEME] ?: AppTheme.SYSTEM.name
            val theme = try { AppTheme.valueOf(themeName) } catch (e: Exception) { AppTheme.SYSTEM }
            val colorThemeName = preferences[Keys.COLOR_THEME] ?: AppColorTheme.GREEN.name
            val colorTheme = try { AppColorTheme.valueOf(colorThemeName) } catch (e: Exception) { AppColorTheme.GREEN }
            val isNotificationEnabled = preferences[Keys.NOTIFICATION_ENABLED] ?: true
            val isReminderVibrateEnabled = preferences[Keys.REMINDER_VIBRATE_ENABLED] ?: true
            val isHapticEnabled = preferences[Keys.HAPTIC_ENABLED] ?: true
            val isAutoBackupEnabled = preferences[Keys.AUTO_BACKUP_ENABLED] ?: false
            val greetingMessage = preferences[Keys.GREETING_MESSAGE] ?: "Ngày mới lại bắt đầu rồi"

            UserPreferences(
                appTheme = theme,
                colorTheme = colorTheme,
                isNotificationEnabled = isNotificationEnabled,
                isReminderVibrateEnabled = isReminderVibrateEnabled,
                isHapticEnabled = isHapticEnabled,
                isAutoBackupEnabled = isAutoBackupEnabled,
                greetingMessage = greetingMessage
            )
        }

    suspend fun updateAppTheme(theme: AppTheme) {
        context.dataStore.edit { it[Keys.APP_THEME] = theme.name }
    }

    suspend fun updateColorTheme(colorTheme: AppColorTheme) {
        context.dataStore.edit { it[Keys.COLOR_THEME] = colorTheme.name }
    }

    suspend fun setNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATION_ENABLED] = enabled }
    }

    suspend fun setReminderVibrateEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.REMINDER_VIBRATE_ENABLED] = enabled }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HAPTIC_ENABLED] = enabled }
    }

    suspend fun setAutoBackupEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_BACKUP_ENABLED] = enabled }
    }

    suspend fun updateGreetingMessage(greeting: String) {
        context.dataStore.edit { it[Keys.GREETING_MESSAGE] = greeting }
    }
}
