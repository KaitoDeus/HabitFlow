# Tài liệu: Lưu trữ Dữ liệu và Logic Nghiệp vụ

---

## 1. Cơ sở dữ liệu Room (`Database.kt`)
Ứng dụng sử dụng Room Database để lưu trữ dữ liệu bền vững.

### Các bảng chính (`Models.kt`):
- **`habits`:** Lưu cấu hình thói quen (tên, mô tả, ngày lặp, giờ, ngày tạo).
- **`occurrences`:** Lưu lịch sử thực hiện. Khóa chính là cặp `(habitId, scheduledEpochDay)`.
- **`goals`:** Lưu các mục tiêu dài hạn.
- **`user_stats`:** Lưu XP, Level, số lượng thẻ Đóng băng (❄️) và thẻ Bỏ qua (⏭️).
- **`reminders`:** Lưu cấu hình nhắc nhở thông báo.

### Ràng buộc:
- `OccurrenceEntity` có Foreign Key tới `HabitEntity` với `onDelete = CASCADE`. Khi xóa thói quen, lịch sử liên quan sẽ tự động bị xóa.

---

## 2. Lưu trữ Cấu hình DataStore (`UserPreferences.kt`)
Sử dụng `Preferences DataStore` để lưu trữ cài đặt bất đồng bộ và an toàn luồng:
- **Tên DataStore:** `user_preferences`.
- **Các trường quản lý:**
  - `app_theme`: Chế độ giao diện (SYSTEM, LIGHT, DARK).
  - `color_theme`: Bảng màu chủ đề (GREEN, BLUE, PURPLE, ORANGE, DYNAMIC).
  - `notification_enabled`: Bật/Tắt thông báo nhắc nhở chung.
  - `reminder_vibrate_enabled`: Bật/Tắt rung khi có thông báo nhắc nhở.
  - `haptic_enabled`: Bật/Tắt phản hồi rung xúc giác khi hoàn thành thói quen.
  - `auto_backup_enabled`: Bật/Tắt tự động sao lưu định kỳ qua WorkManager.
  - `greeting_message`: Thông điệp lời chào tùy chỉnh khi mở ứng dụng.

---

## 3. Logic Gamification (`GamificationManager.kt`)
Bộ não điều khiển hệ thống RPG của ứng dụng:

### Công thức XP:
- **Khi hoàn thành:** `10 + (currentStreak * 1.5)`.
  - *Ví dụ:* Chuỗi 10 ngày sẽ nhận được `10 + 15 = 25 XP`.
- **Lên cấp:** Yêu cầu XP = `level^2 * 100`.

### Hệ thống Phần thưởng:
- **Thẻ Đóng băng (❄️):** Tặng 1 thẻ sau mỗi 7 ngày duy trì chuỗi (streak).
- **Thẻ Bỏ qua (⏭️):** Tặng 1 thẻ mỗi khi lên cấp chia hết cho 3 (Cấp 3, 6, 9...).
- **Milestones:**
  - Cấp chia hết cho 5: Tặng cả 2 loại thẻ.
  - Các cấp khác: Tặng 50 XP Bonus.

---

## 4. Lớp Repository (`Repository.kt`)
Đóng vai trò là nguồn dữ liệu duy nhất (Single Source of Truth), điều phối giữa Room DB và DataStore.
- Sử dụng `Flow` để cung cấp dữ liệu thời gian thực cho UI.
- Đảm bảo tính toàn vẹn dữ liệu thông qua các phương thức `suspend` và Transaction.

---

## 5. Tiện ích Màn hình chủ (Jetpack Glance Widget - `HabitWidget.kt`)
Cung cấp App Widget ngoài màn hình chính Android với kích thước tương thích linh hoạt:

### Cấu trúc Widget:
- **Bên trái:** Vòng tròn tiến độ hiển thị tỷ lệ % hoàn thành thói quen trong ngày.
- **Bên phải:** Danh sách các thói quen hôm nay kèm nút tròn đánh dấu.
- **Tương tác 1-chạm không cần mở app (`ToggleHabitActionCallback`):**
  - Người dùng chạm trực tiếp vào nút tròn trên widget để hoàn thành/hủy thói quen.
  - `ToggleHabitActionCallback` chạy ngầm, ghi nhận trạng thái vào Room DB, tính XP/Streak, cập nhật tiến độ mục tiêu liên kết và gọi `HabitWidget().update()` làm mới widget tức thì.

---

## 6. Tác vụ Chạy Ngầm Định Kỳ (`AutoBackupWorker.kt`)
- Sử dụng thư viện **Android Jetpack WorkManager** (`CoroutineWorker`).
- **Chu kỳ:** 7 ngày một lần với ràng buộc pin không yếu (`setRequiresBatteryNotLow`).
- **Thư mục lưu trữ:** Thư mục an toàn nội bộ `files/auto_backups/`.
- **Chính sách dọn dẹp:** Tự động sắp xếp theo thời gian và chỉ lưu trữ tối đa **5 bản sao lưu mới nhất** nhằm tối ưu hóa bộ nhớ thiết bị.
