# HabitFlow - Ứng Dụng Theo Dõi Thói Quen & Mục Tiêu

Ứng dụng Habit and Goal Tracker hiện đại được xây dựng bằng **Kotlin**, **Jetpack Compose**, **Room Database**, **WorkManager** và **Jetpack Glance**.

---

## Chức năng nổi bật

- **Quản lý Thói quen & Mục tiêu:**
  - Tạo, theo dõi, lưu trữ và khôi phục thói quen hàng ngày.
  - Ghi nhận trạng thái: Hoàn thành, Bỏ qua, Bỏ lỡ và Đóng băng chuỗi (Streak Freeze).
  - Liên kết thói quen trực tiếp với tiến độ mục tiêu dài hạn.
- **Hệ thống Gamification & Trải nghiệm:**
  - Hệ thống cấp độ RPG: Tích lũy điểm kinh nghiệm (XP), thăng cấp, duy trì chuỗi Streak.
  - Phản hồi rung xúc giác (Haptic Feedback) khi hoàn thành.
  - Hiệu ứng pháo hoa ăn mừng (Confetti Canvas Animation) khi hoàn thành 100% mục tiêu ngày hoặc lên cấp.
- **Cá nhân hóa Giao diện (Theming):**
  - Hỗ trợ chế độ Sáng / Tối (Light / Dark Mode).
  - Tùy biến 5 bộ màu: Xanh lá, Xanh biển, Tím, Cam ấm.
  - Tích hợp công nghệ **Material You Dynamic Color** (Android 12+), tự động đổi màu theo hình nền máy.
- **Tiện ích Màn hình chính (Jetpack Glance Widget):**
  - Hiển thị danh sách thói quen và tiến độ hôm nay trực quan ngoài Home Screen.
  - **Tương tác 1-chạm không cần mở app (`ActionCallback`):** Chạm trực tiếp trên widget để đánh dấu hoàn thành/hủy, tự động cập nhật Room DB và tính lại XP/Streak tức thì.
- **Sao lưu, Chia sẻ & Báo cáo:**
  - Sao lưu và khôi phục toàn bộ cơ sở dữ liệu qua file JSON.
  - Chia sẻ file sao lưu nhanh qua Zalo, Drive, Gmail với **Android FileProvider** an toàn.
  - **Xuất báo cáo CSV:** Hỗ trợ chuẩn **UTF-8 BOM** mở trên Microsoft Excel không bị lỗi font tiếng Việt.
  - **Tự động sao lưu định kỳ:** Sử dụng **WorkManager** tự động sao lưu dữ liệu mỗi tuần và duy trì 5 bản gần nhất.
- **Thông báo Thông minh:**
  - Lên lịch nhắc nhở chính xác với `AlarmManager`.
  - Nút bấm thao tác nhanh ngay trên thông báo: **`[✓ Hoàn thành]`** và **`[⏱ Hoãn 10 phút]`**.

---

## Cách chạy dự án

1. Mở thư mục `Code/HabitFlow` trong Android Studio (Ladybug hoặc mới hơn).
2. Chọn Gradle JDK 17.
3. Chờ Gradle Sync hoàn tất.
4. Chọn thiết bị ảo hoặc máy thật (khuyến nghị Android 12+ để trải nghiệm trọn vẹn Material You).
5. Nhấn **Run `app`** (`Shift + F10`).

> [!NOTE]
> Dự án sử dụng `compileSdk 35`. Đảm bảo đã cài đặt Android SDK Platform 35 trong Android Studio SDK Manager.
