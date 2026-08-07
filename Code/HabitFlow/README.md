# HabitFlow

Ứng dụng Habit and Goal Tracker dùng Android Studio, Kotlin, Jetpack Compose và Room.

## Chức năng hiện có

- Tạo và lưu trữ thói quen.
- Ghi nhận Completed, Missed và Skipped theo ngày.
- Tính current streak, longest streak và completion rate.
- Tạo mục tiêu theo số lần hoặc giá trị tích lũy.
- Hiển thị tiến độ mục tiêu và thống kê tổng quan.
- Xuất toàn bộ dữ liệu sang JSON và khôi phục trong Room transaction.
- Khung AlarmManager, Notification receiver và Boot receiver.

## Cách chạy

1. Mở thư mục `HabitFlow_Fixed` trong Android Studio.
2. Chọn Gradle JDK 17.
3. Chờ Gradle Sync hoàn tất.
4. Chọn máy ảo Pixel 8.
5. Nhấn Run `app`.

Project dùng `compileSdk 35`. Nếu Android Studio báo thiếu SDK, mở Tools > SDK Manager và cài Android SDK Platform 35.

## Khi thay thế project cũ

Nên mở project này trong một cửa sổ Android Studio mới, không chép đè từng file vào project cũ. Sau khi project mới chạy ổn định, bạn mới chuyển các thay đổi cá nhân sang.
