# Tài liệu Tab: Mục tiêu (Goals)

## 1. Tổng quan UI
Màn hình "Mục tiêu" giúp người dùng thiết lập và theo dõi các mục tiêu dài hạn hoặc định lượng (ví dụ: Đọc 10 cuốn sách, Chạy 100km).

### Các thành phần chính:
- **Form tạo mục tiêu:** Nhập tên và giá trị mục tiêu (số lượng).
- **Loại mục tiêu (GoalMetricType):**
  - **Theo số lần (OCCURRENCE_COUNT):** Đếm số lần thực hiện.
  - **Theo giá trị (ACCUMULATED_VALUE):** Cộng dồn các giá trị nhập vào.
- **Danh sách mục tiêu:** Hiển thị dưới dạng các thẻ (Card) có thanh tiến trình (`LinearProgressIndicator`).

## 2. Các hàm và Logic chính

### Tạo mục tiêu (`GoalsScreen` trong `MainActivity.kt`)
- **Hàm gọi:** `vm.addGoal(name, targetValue, type)`
- **Logic lưu trữ:**
  - `startEpochDay`: Lưu ngày bắt đầu (ngày hiện tại).
  - `unit`: Tự động gán là "lần" hoặc "đơn vị" tùy loại mục tiêu.
  - `currentValue`: Khởi tạo bằng 0.0.

### Cập nhật tiến độ
- **Nút "+1 tiến độ":**
  - **Hàm gọi:** `vm.addGoalProgress(goal, 1.0)`
  - **Logic trong Repository:** 
    ```kotlin
    db.goalDao().upsert(goal.copy(currentValue = (goal.currentValue + value).coerceAtMost(goal.targetValue)))
    ```
  - **Tác động:** Tăng giá trị hiện tại nhưng không vượt quá giá trị mục tiêu.

## 3. Tương tác Dữ liệu
- **`GoalEntity`:** Thực thể Room lưu trữ thông tin mục tiêu.
- **Lọc dữ liệu:** `goalDao().observeActive()` chỉ lấy các mục tiêu chưa bị `archived`.
- **Tiến trình:** Được tính bằng `(currentValue / targetValue)` để hiển thị lên UI.
