# Tài liệu Tab: Thói quen (Habits)

## 1. Tổng quan UI
Màn hình này cho phép người dùng quản lý danh sách thói quen tổng thể: thêm mới, xem danh sách và lưu trữ/xóa thói quen.

### Các thành phần chính:
- **Form thêm mới:** Các ô nhập Tên thói quen và Mô tả.
- **Chọn ngày lặp lại (Lặp lại):** Sử dụng `FlowRow` hiển thị các chip từ T2 đến CN.
- **Chọn giờ (Time Picker):** Ô chọn giờ thực hiện (Wheel Picker giả lập).
- **Nút "Thêm thói quen":** Nằm ở giữa (centered) để xác nhận tạo mới.
- **Khung danh sách:** Danh sách các thói quen đang hoạt động nằm trong một `Card` màu `secondaryContainer` để dễ nhận diện.

## 2. Các hàm và Logic chính

### Tạo thói quen mới (`HabitsScreen` trong `FeatureHabit.kt`)
- **Xử lý ngày lặp:** `selectedDays` là một `Set<Int>` (1=T2, ..., 7=CN). Khi lưu, nó được chuyển thành chuỗi CSV (ví dụ: "1,3,5").
- **Lưu dữ liệu:** Gọi `vm.addHabit(name, description, scheduledDays, time)`.
- **Hàm trong ViewModel:**
  ```kotlin
  fun addHabit(name: String, description: String, scheduledDays: String, scheduledTime: String?) {
      repository.addHabit(name, description, scheduledDays, scheduledTime)
      updateWidget() // Cập nhật widget ngay lập tức
  }
  ```
- **Thông báo xác nhận:** Sử dụng `Toast` để hiển thị "Đã thêm thói quen: [Tên]" ngay sau khi tạo.
- **ID:** Tự động tạo bằng `UUID.randomUUID().toString()`.
- **Ngày tạo:** Lưu `System.currentTimeMillis()` vào trường `createdAt`.

### Quản lý danh sách
1. **Hiển thị ngày lặp:** Chuyển đổi chuỗi "1,2" thành text "Thứ 2, Thứ 3". Nếu trống thì hiển thị "Hàng ngày".
2. **Lưu trữ (Archive):**
   - **Hàm gọi:** `vm.archiveHabit(habit.id)`
   - **Tác động:** Đặt `archived = true` trong DB. Thói quen sẽ biến mất khỏi tab Hôm nay và danh sách hoạt động nhưng vẫn còn trong lịch sử thống kê.
3. **Xóa (Delete):**
   - **Hàm gọi:** `vm.deleteHabit(habit.id)`
   - **Tác động:** Xóa hoàn toàn khỏi Database (bao gồm cả các Occurrence liên quan do ràng buộc `CASCADE`). Có Dialog xác nhận trước khi xóa.

## 3. Cấu trúc Dữ liệu
- **`HabitEntity`:** Chứa thông tin cấu hình của thói quen.
- **Khung chứa danh sách:** Được thiết kế với `weight(1f)` và `LazyColumn` để hỗ trợ cuộn khi danh sách dài.
