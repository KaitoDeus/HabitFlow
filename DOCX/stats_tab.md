# Tài liệu Tab: Thống kê (Statistics)

## 1. Tổng quan UI
Màn hình "Thống kê" cung cấp cái nhìn trực quan về quá trình thực hiện thói quen thông qua biểu đồ lịch và các chỉ số hiệu suất.

### Các thành phần chính:
- **Biểu đồ Lịch (Calendar Grid):** Hiển thị các ngày trong tháng. Có các nút mũi tên (< / >) để chuyển đổi giữa các tháng.
- **Các chỉ số (StatCards):** Hoàn thành, Bỏ lỡ, Bỏ qua, Đóng băng, Chuỗi hiện tại, Chuỗi dài nhất, Tỷ lệ theo tuần/tháng.

## 2. Các hàm và Logic chính

### Logic hiển thị Lịch (`StatisticsScreen` trong `MainActivity.kt`)
- **Điều hướng tháng:** Sử dụng `selectedYearMonth` (kiểu `YearMonth`). Khi nhấn nút mũi tên, gọi `plusMonths(1)` hoặc `minusMonths(1)`.
- **Lọc dữ liệu theo ngày:** Với mỗi ô ngày trong lịch:
  1. Lấy danh sách `dayHabits` có hiệu lực (đã được tạo trước hoặc trong ngày đó).
  2. Lấy danh sách `dayOccurrences` của ngày đó từ DB.
  3. Tính toán số lượng hoàn thành (`COMPLETED`).

### Quy tắc tô màu (Color Coding)
- **Ngày tương lai:** Màu nền mặc định (`surfaceVariant`).
- **Đóng băng (Frozen):** Ưu tiên hiển thị màu **Xanh biển** (`0xFF2196F3`) nếu có bất kỳ thói quen nào được đóng băng.
- **Hoàn thành (Green):** Nếu số thói quen hoàn thành >= 50% tổng số thói quen của ngày đó (`completed * 2 >= total`).
- **Tiến độ một phần (Yellow):** Nếu có ít nhất 1 thói quen hoàn thành nhưng tỷ lệ < 50%.
- **Bỏ lỡ (Light Red):** Nếu không có thói quen nào hoàn thành (0%) và không phải ngày tương lai.

### Tính toán chỉ số (`HabitStatisticsCalculator.kt`)
- **Hàm `calculate(items, todayEpochDay)`:**
  - **Streak:** Tính toán bằng cách duyệt ngược danh sách occurrences (`ordered.asReversed()`).
  - **Tỷ lệ hoàn thành:** `completed / (completed + missed)`.
  - **Weekly/Monthly Rate:** Lọc occurrences trong 7 hoặc 30 ngày gần nhất so với `todayEpochDay`.

## 3. Tương tác với ViewModel
- **`vm.stats`:** Một `StateFlow` kết hợp từ occurrences, habits và `testDateOffset`. Tự động tính toán lại khi bất kỳ dữ liệu nào thay đổi.
