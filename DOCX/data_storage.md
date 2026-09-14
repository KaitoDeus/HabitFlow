# Tài liệu: Lưu trữ Dữ liệu và Logic Nghiệp vụ

## 1. Cơ sở dữ liệu Room (`Database.kt`)
Ứng dụng sử dụng Room Database để lưu trữ dữ liệu bền vững. Phiên bản hiện tại: **5**.

### Các bảng chính (`Models.kt`):
- **`habits`:** Lưu cấu hình thói quen (tên, mô tả, ngày lặp, giờ, ngày tạo).
- **`occurrences`:** Lưu lịch sử thực hiện. Khóa chính là cặp `(habitId, scheduledEpochDay)`.
- **`goals`:** Lưu các mục tiêu dài hạn.
- **`user_stats`:** Lưu XP, Level, số lượng thẻ Đóng băng (❄️) và thẻ Bỏ qua (⏭️).
- **`reminders`:** Lưu cấu hình nhắc nhở thông báo.

### Ràng buộc:
- `OccurrenceEntity` có Foreign Key tới `HabitEntity` với `onDelete = CASCADE`. Khi xóa thói quen, lịch sử liên quan sẽ tự động bị xóa.

## 2. Lưu trữ cấu hình (`DataStore`)
Sử dụng `Preferences DataStore` để lưu các cài đặt nhẹ:
- **File:** `user_preferences`.
- **Các trường:** `app_theme`, `dynamic_color`, `notification_enabled`, `greeting_message`.

## 3. Logic Gamification (`GamificationManager.kt`)
Đây là bộ não điều khiển hệ thống RPG của ứng dụng.

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

## 4. Lớp Repository (`HabitRepository.kt`)
Đóng vai trò là nguồn dữ liệu duy nhất (Single Source of Truth), điều phối giữa Room DB và DataStore.
- Sử dụng `Flow` để cung cấp dữ liệu thời gian thực cho UI.
- Đảm bảo tính toàn vẹn dữ liệu thông qua các phương thức `suspend` và Transaction.

## 5. Tiện ích Màn hình chủ (Home Screen Widget)
Ứng dụng sử dụng **Jetpack Glance** để cung cấp Widget kích thước 5x2.

### Cấu trúc Widget:
- **Bên trái:** Vòng tròn tiến độ (Xanh Neon) hiển thị tỷ lệ % hoàn thành thói quen trong ngày.
- **Bên phải:** Danh sách các thói quen hôm nay (tối đa 5 thói quen).
- **Cơ chế co giãn (Flexible Bars):** Các thói quen trong danh sách sử dụng `defaultWeight()` để chia đều chiều cao của widget.

### Cơ chế cập nhật:
- **Tự động Sync:** Hàm `updateWidget()` trong `MainViewModel` được gọi mỗi khi có sự thay đổi dữ liệu (đánh dấu hoàn thành, thêm mới, xóa, hoặc chuyển ngày test).
- **Thành phần kỹ thuật:** 
    - `HabitWidget`: Định nghĩa giao diện bằng Glance Compose.
    - `HabitWidgetReceiver`: Quản lý việc cập nhật từ hệ thống Android.
