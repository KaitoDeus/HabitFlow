# Tài liệu Tab: Cài đặt (Settings)

## 1. Tổng quan UI
Màn hình này cho phép người dùng tùy chỉnh ứng dụng, quản lý dữ liệu cá nhân và thiết lập lời chào.

### Các thành phần chính:
- **Sao lưu/Khôi phục:** Các nút "Xuất JSON" và "Khôi phục JSON".
- **Thông báo:** Nút xin quyền và Switch bật/tắt thông báo.
- **Giao diện:** Switch "Chế độ sáng/tối". Mặc định là Dark Mode.
- **Lời chào:** Ô nhập văn bản để thay đổi thông điệp chào mừng khi mở app.

## 2. Các hàm và Logic chính

### Quản lý Giao diện (Theme)
- **Mặc định:** `AppTheme.DARK`.
- **Hàm gọi:** `viewModel.onThemeSelected(newTheme)`.
- **Lưu trữ:** Lưu vào DataStore thông qua `PreferencesRepository`.

### Sao lưu và Khôi phục
1. **Xuất JSON:**
   - **Hàm:** `mainViewModel.exportJson()`.
   - **Cơ chế:** Chuyển đổi toàn bộ Database (habits, occurrences, goals, reminders, userStats) thành một chuỗi JSON bằng thư viện `kotlinx.serialization`.
2. **Khôi phục JSON:**
   - **Hàm:** `mainViewModel.restoreJson(text)`.
   - **Cơ chế:** Giải mã chuỗi JSON và ghi đè vào Database trong một Transaction (`db.withTransaction`). Toàn bộ dữ liệu cũ sẽ bị xóa trước khi nạp mới.

### Lời chào tùy chỉnh (Greeting Message)
- **Mặc định:** "Ngày mới lại bắt đầu rồi".
- **Hành động:** Khi người dùng thay đổi text, nút "Lưu" sẽ hiện ra. Nhấn "Lưu" gọi `viewModel.onGreetingChanged(tempGreeting)`.
- **Hiển thị:** Tin nhắn này được hiển thị dưới dạng **Toast** trong `MainActivity` ngay khi dữ liệu cấu hình được tải thành công.

## 3. Thành phần kỹ thuật
- **`SettingsViewModel`:** Quản lý `uiState` dựa trên luồng dữ liệu từ `PreferencesRepository`.
- **`ActivityResultContracts`:** Sử dụng `CreateDocument` và `OpenDocument` để tương tác với trình quản lý tệp của Android.
