package com.habitflow.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UserPreferencesTest {

    @Test
    fun testDefaultPreferences() {
        val prefs = UserPreferences()
        assertEquals(AppTheme.SYSTEM, prefs.appTheme)
        assertEquals(AppColorTheme.GREEN, prefs.colorTheme)
        assertTrue(prefs.isNotificationEnabled)
        assertTrue(prefs.isReminderVibrateEnabled)
        assertTrue(prefs.isHapticEnabled)
        org.junit.Assert.assertFalse(prefs.isAutoBackupEnabled)
    }

    @Test
    fun testThemeEnumValues() {
        val themes = AppTheme.entries
        assertEquals(3, themes.size)
        assertTrue(themes.contains(AppTheme.SYSTEM))
        assertTrue(themes.contains(AppTheme.LIGHT))
        assertTrue(themes.contains(AppTheme.DARK))
    }

    @Test
    fun testColorThemeEnumValues() {
        val colorThemes = AppColorTheme.entries
        assertEquals(5, colorThemes.size)
        assertTrue(colorThemes.contains(AppColorTheme.GREEN))
        assertTrue(colorThemes.contains(AppColorTheme.BLUE))
        assertTrue(colorThemes.contains(AppColorTheme.PURPLE))
        assertTrue(colorThemes.contains(AppColorTheme.ORANGE))
        assertTrue(colorThemes.contains(AppColorTheme.DYNAMIC))
    }

    @Test
    fun testAutoBackupWorkNameConstant() {
        assertEquals("habitflow_auto_backup_work", AutoBackupWorker.WORK_NAME)
    }

    @Test
    fun testWidgetActionCallbackKey() {
        assertEquals("habit_id", ToggleHabitActionCallback.habitIdKey.name)
    }
}
