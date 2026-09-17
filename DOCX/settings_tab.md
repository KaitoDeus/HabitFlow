# Tài liệu Tab: Cài đặt (Settings & Utilities)

Tài liệu mô tả chi tiết phân hệ Cài đặt, Cá nhân hóa giao diện, Sao lưu dữ liệu và Quản lý nhắc nhở (do Thành viên 6 phụ trách).

---

## 1. Tổng quan Giao diện (UI Overview)
Màn hình Cài đặt (`SettingsScreen`) cung cấp trung tâm điều khiển cho người dùng tùy biến ứng dụng và bảo vệ dữ liệu:
- **Sao lưu & Dữ liệu:**
  - Lối tắt vào màn hình chuyên sâu `BackupRestoreScreen`.
  - Nút thao tác nhanh: **Xuất JSON**, **Khôi phục**, **Chia sẻ sao lưu**, **Xuất báo cáo CSV**.
  - Công tắc **Tự động sao lưu định kỳ (7 ngày)** chạy ngầm.
- **Nhắc nhở thói quen:**
  - Nút **Thử chuông** để kiểm tra thông báo và thao tác nhanh.
  - Danh sách các lịch nhắc nhở đang hoạt động kèm nút **Chỉnh sửa** và **+ Thêm**.
- **Hệ thống & Cá nhân hóa:**
  - **Chế độ Sáng/Tối:** Bật/Tắt chế độ tối (Dark Mode).
  - **Bảng màu chủ đạo (Accent Color):** Bộ chọn màu trực quan `ColorThemeSelector` (Xanh lá, Xanh biển, Tím, Cam ấm, Tự động - Material You).
  - **Cấp quyền thông báo & Rung:** Công tắc cấp quyền thông báo, rung khi nhắc nhở, rung phản hồi xúc giác (Haptic). Tự động kích hoạt hộp thoại yêu cầu cấp quyền khi mở ứng dụng lần đầu.
  - **Lời chào khi mở app:** Ô nhập thông điệp tùy chỉnh hiển thị dạng Toast khi khởi động.

---

## 2. Kiến trúc & Phân tách Module (Architecture)

Phân hệ được tách thành các file chuyên biệt theo nguyên tắc đơn nhiệm (Single Responsibility):

| Tên File | Vai trò đảm nhiệm |
| :--- | :--- |
| [`UserPreferences.kt`](../Code/HabitFlow/app/src/main/kotlin/com/habitflow/app/UserPreferences.kt) | Quản lý tầng DataStore Preferences: `AppTheme`, `AppColorTheme`, `UserPreferences`, `UserPreferencesDataSource`. |
| [`FeatureSettings.kt`](../Code/HabitFlow/app/src/main/kotlin/com/habitflow/app/FeatureSettings.kt) | `SettingsUiState`, `SettingsViewModel`, `ColorThemeSelector`, `ThemeToggleRow`, `SettingsScreen`. |
| [`FeatureBackup.kt`](../Code/HabitFlow/app/src/main/kotlin/com/habitflow/app/FeatureBackup.kt) | `BackupManager` (JSON), `BackupSharer` (FileProvider), `ReportExporter` (CSV UTF-8 BOM) và `BackupRestoreScreen`. |
| [`AutoBackupWorker.kt`](../Code/HabitFlow/app/src/main/kotlin/com/habitflow/app/AutoBackupWorker.kt) | `CoroutineWorker` tự động sao lưu dữ liệu mỗi 7 ngày qua `WorkManager` (giữ tối đa 5 bản gần nhất). |
| [`Reminder.kt`](../Code/HabitFlow/app/src/main/kotlin/com/habitflow/app/Reminder.kt) | `ReminderScheduler`, `AlarmReceiver`, `NotificationActionReceiver` (Quick Actions) và `ReminderEditorDialog`. |

---

## 3. Các Logic Kỹ thuật Chính

### 🎨 Tùy biến Bảng màu Chủ đề (Dynamic Theming)
- **Enum:** `AppColorTheme { GREEN, BLUE, PURPLE, ORANGE, DYNAMIC }`.
- **Material You (Android 12+):** Khi chọn `DYNAMIC`, ứng dụng dùng `dynamicDarkColorScheme()` / `dynamicLightColorScheme()` để tự động hòa sắc theo hình nền máy.
- **Áp dụng toàn cục:** `MainActivity` quan sát `userPreferences.colorTheme` để tái cấu trúc `MaterialTheme(colorScheme)` ngay lập tức.

### 💾 Sao lưu, Chia sẻ & Xuất Báo Cáo
1. **Chia sẻ nhanh qua Intent (`BackupSharer`):**
   - Sử dụng `FileProvider` tạo Content URI an toàn (`content://.../backup_cache/...`).
   - Gọi `Intent.ACTION_SEND` với cờ `FLAG_GRANT_READ_URI_PERMISSION`, cho phép gửi file sao lưu thẳng qua Zalo, Drive, Gmail mà không cần quyền nguy hiểm.
2. **Xuất báo cáo CSV chuẩn Excel (`ReportExporter`):**
   - Bổ sung **UTF-8 BOM** (`\uFEFF`) ở đầu chuỗi dữ liệu để Microsoft Excel (Windows/macOS) hiển thị tiếng Việt có dấu chuẩn 100%.
3. **Tự động sao lưu định kỳ (`AutoBackupWorker`):**
   - Đăng ký `PeriodicWorkRequestBuilder` chu kỳ 7 ngày với điều kiện `setRequiresBatteryNotLow(true)`.
   - Cơ chế tự dọn dẹp: Quét thư mục `auto_backups/` và chỉ giữ lại 5 bản sao lưu gần nhất.

### 🔔 Thông báo & Nút Thao Tác Nhanh (Notification Quick Actions)
- `NotificationActionReceiver` xử lý 2 nút bấm ngay trên thông báo mà không cần mở ứng dụng:
  - **`[✓ Hoàn thành]`**: Đánh dấu hoàn thành thói quen hôm nay trong Room DB, cộng XP, tăng Streak và hủy thông báo.
  - **`[⏱ Hoãn 10 phút]`**: Lên lịch nhắc lại sau 10 phút qua `AlarmManager`.
