# Tài liệu Tab: Hôm nay (Today)

## 1. Tổng quan UI
Màn hình "Hôm nay" là trung tâm tương tác hàng ngày của người dùng, nơi hiển thị danh sách các thói quen cần thực hiện trong ngày và tiến trình thăng cấp (gamification).

### Các thành phần chính:
- **Thẻ Cấp độ (XP Header):** Hiển thị Level hiện tại, thanh tiến trình XP, và số lượng thẻ "Đóng băng" (❄️). Thẻ này có thể nhấn vào để xem chi tiết.
- **Dòng tiêu đề ngày:** Hiển thị ngày hiện tại (có tính đến `testOffset`).
- **Nút "next day(test)":** Nút màu đỏ ở góc phải dùng để giả lập chuyển sang ngày tiếp theo (phục vụ test).
- **Danh sách thói quen:** Hiển thị các thói quen được lập lịch cho ngày hôm nay.
- **Mục "Ngày mai":** Hiển thị xem trước các thói quen của ngày kế tiếp.
- **Hiệu ứng Pháo hoa (Confetti):** Khi nhấn "Xong", hiệu ứng bùng nổ sẽ xuất hiện trên màn hình.
- **Bố cục Thích ứng (Adaptive Layout):** Spacing giữa các thẻ thói quen tự động tăng lên (24dp) và thẻ có độ cao tối thiểu lớn hơn (130dp) khi danh sách chỉ có 1-2 thói quen.

## 2. Các hàm và Logic chính

### Xử lý dữ liệu hiển thị (`TodayScreen` trong `MainActivity.kt`)
- **Lọc thói quen:** Thói quen được lọc dựa trên `currentDayOfWeek` và chuỗi `scheduledDays` (ví dụ: "1,2,3"). Nếu `scheduledDays` trống, thói quen được coi là hàng ngày.
- **Xác định trạng thái:** Sử dụng `todayOccurrences` (Map habitId -> OccurrenceEntity) để biết thói quen đã được xử lý chưa.

### Các hành động (Buttons)
1. **Nút "Xong" (Complete):**
   - **Hàm gọi:** `vm.mark(habit.id, OccurrenceStatus.COMPLETED)`
   - **Tác động:** Tạo bản ghi hoàn thành trong DB. Cộng XP dựa trên công thức `10 + (streak * 1.5)`. Kích hoạt hiệu ứng Confetti (`showConfetti = true`).
   - **Giao diện:** Sử dụng `AnimatedVisibility` để tạo hiệu ứng ẩn/hiện mượt mà cho thẻ.
2. **Nút "Bỏ qua" (Skip):**
   - **Hàm gọi:** `vm.mark(habit.id, OccurrenceStatus.SKIPPED)`
   - **Tác động:** Đánh dấu bỏ qua ngày hôm đó mà không ảnh hưởng tiêu cực đến streak (tùy thuộc vào việc có dùng thẻ Skip hay không).
3. **Nút ❄️ (Dùng thẻ Đóng băng):**
   - **Hàm gọi:** `vm.useStreakFreeze(habit.id)`
   - **Điều kiện:** Chỉ hiện khi `userStats.streakFreezes > 0`.
   - **Tác động:** Giảm 1 thẻ ❄️, đánh dấu trạng thái `FROZEN`.
4. **Nút ⏭️ (Dùng thẻ Bỏ qua):**
   - **Hàm gọi:** `vm.useSkip(habit.id)`
   - **Điều kiện:** Chỉ hiện khi `userStats.skipsAvailable > 0`.
   - **Tác động:** Giảm 1 thẻ ⏭️, đánh dấu trạng thái `SKIPPED`.
5. **Nút "Thay đổi" (Change):**
   - **Hàm gọi:** `vm.unmark(habit.id, todayEpochDay)`
   - **Tác động:** Xóa bản ghi occurrence của ngày hôm đó để người dùng chọn lại.

## 3. Tương tác với ViewModel/Repository
- **`MainViewModel.testDateOffset`:** Theo dõi số ngày lệch so với thực tế để phục vụ test.
- **`HabitRepository.mark(...)`:** Hàm chính để lưu trạng thái vào Room Database.
- **`GamificationManager.processCompletion(...)`:** Tính toán XP và phần thưởng ngay sau khi nhấn "Xong".
