# SelfAlarm - 2rd Group Project

## Nhóm thực hiện

- Lê Thanh Phong - 22110198 (Quản lý, thông báo sự kiện, đăng nhập Google, đồng bộ với Google Calendar)
- Nguyễn Đăng Quang - 22110211 (Các chức năng liên quan đến danh bạ, tin nhắn, blacklist và tối ưu hóa theo phần trăm pin)
- Trần Đinh Gia Bảo - 22110111 (Các chức năng liên quan đến phần phát nhạc)

## Tính năng chính

### 1. Cài đặt sự kiện, lịch hẹn và thông báo nhắc nhở

- **Tích hợp API Google Calendar**: Có thể đồng bộ hóa với Google Calendar
- **Quản lý sự kiện**: Thêm, sửa, xóa các sự kiện với tiêu đề, mô tả và thời gian
- **Hệ thống tự động thông báo, nhắc nhở sự kiện**: Đặt thời gian bắt đầu, kết thúc với nhiều tùy
  chọn (phút, giờ, ngày) để có thể thông báo khi sắp đến sự kiện
- **Giao diện lịch trực quan, dễ sử dụng**
- **Thông báo chi tiết**: Nhận thông báo đầy đủ thông tin về sự kiện sắp diễn ra

### 2. Trình phát nhạc tích hợp

- **Thêm nhạc**: Hỗ trợ thêm nhạc từ file tải về
- **Phát nhạc khi chạy nền**: Phát nhạc trong nền với các điều khiển thông báo đầy đủ (
  phát/tạm dừng/bài
  tiếp theo/bài trước)
- **Điều khiển đa dạng**: Hỗ trợ các nút điều khiển trên thanh thông báo và màn hình khóa
- **Tự động tạm dừng**: Tự động dừng nhạc khi ngắt kết nối tai nghe

### 3. Quản lý Cuộc gọi và SMS

- **Danh sách đen**: Cho phép chặn cuộc gọi và tin nhắn từ các số điện thoại không muốn liên lạc
- **Quản lý tin nhắn**: Theo dõi lịch sử tin nhắn với thông tin nội dung, thời gian.
- **Tự động nhận diện**: Hiển thị tên liên hệ nếu số điện thoại đã được lưu trong danh bạ

### 4. Tối ưu hóa pin thông minh

- **Theo dõi dung lượng pin thời gian thực**: Theo dõi liên tục mức pin
- **Tối ưu theo mức pin**: Tự động điều chỉnh các cài đặt hệ thống như độ sáng màn hình dựa trên mức
  pin hiện tại

### 5. Tích hợp Firebase

- **Lưu trữ thông tin SMS**
- **Lưu trữ file âm nhạc**

## Kiến trúc kỹ thuật

### 1. Controller Layer

- **Broadcast Receivers**: Lắng nghe và xử lý các sự kiện hệ thống Android
    - `HeadphoneReceiver`: Phát hiện kết nối/ngắt kết nối tai nghe
    - `MusicNotificationReceiver`: Điều khiển nhạc (Play, Next, Previous) trực tiếp từ thông báo
    - `SystemBroadcastReceiver`: Giám sát trạng thái, dung lượng pin
    - `CallReceiver`: Đón nhận và xử lý cuộc gọi đến
    - `SmsReceiver`: Xử lý tin nhắn SMS
    - `TaskReminderReceiver`: Xử lý và hiển thị lời thông báo nhắc nhở cho sự kiện

### 2. Service Layer

- `CalendarService`: Quản lý CRUD sự kiện
- `SystemSettingsService`: Theo dõi và điều chỉnh các thiết lập hệ thống của thiết bị dựa trên trạng
  thái pin
- `CallScreenerService`: Quản lý danh cuộc gọi/tin nhắn có bị chăn hay không
- `MusicService`: Dịch vụ phát nhạc nền với điều khiển thông báo
- `ReminderService`: Xử lý và hiển thị lời nhắc cho sự kiện

### 3. Data Layer

- **Models**: Định nghĩa cấu trúc dữ liệu cho ứng dụng
    - `TaskModel`: Thông tin sự kiện lịch
    - `BlacklistedNumber`: Thông tin số điện thoại trong Black List
    - `MessageModel`: Thông tin tin nhắn
    - `SongModel`: Thông tin bài hát.
- **Data Source**:
    - `TaskContentProvider`: Quản lý database SQLite để có thể thêm, sửa, xóa sự kiện.

### 4. Utilities

- `AudioUtils`:Lấy đường dẫn tệp âm thanh từ một Uri
- `BlacklistHelper`: Kiểm tra xem một số điện thoại có nằm trong blacklist
- `ServiceUtils`: Kiểm tra xem một service có đang chạy
- `NotificationHelper`: Tạo và quản lý thông báo
- `GoogleSignInManager`: Hỗ trợ đăng nhập, đăng xuất Google
- `GoogleCalendarManager`: Hỗ trợ kết nối, đồng bộ với Google Calendar
- `UploadFile`: Upload file lên Firebase

### 5. View Layer

- **Fragments**
    - `HomeFragment`: Giao điện lịch
    - `MusicFragment`: Giao diện nhạc
        - `AddMusicFragment`: Quản lý thêm nhạc
        - `MusicChildMainFragment`: Hiển thị danh sách nhạc
        - `MusicPlayerDetailChildFragment`:Hiển thị thao tác với nhạc
    - `PhoneFragment`: Hiển thị danh sách thông tin liên lạc
    - `MessageFragment`: Hiển thị danh sách những tin nhắn từng nhận được
- **Activities**
    - `BlacklistActivity`: Giao diện quản lý số điện thoại bị chặn
    - `ChatActivity`: Giao diện nội dung tin nhắn giữa 2 người
    - `NewMessageActivity`: Giao diện gửi tin nhắn mới cho người khác
    - `EventDetailActivity`: Giao diện hiển thị các sự kiện trong ngày
- **Adapters**
    - `BlacklistAdapter`, `ContactAdapter`,`PhoneAdapter`, `ChatAdapter`, `EventAdapter`,
      `SongRecylerAdapter`,: Hiển thị danh sách
    - `CalendarAdapter`: Hiển thị ngày trong tháng
    - `MessageAdapter`: Hiển thị nội dung tin nhắn giữa 2 người

### 6. ViewModel Layer

- `ShareSongViewModel`: Quản lý thông tin liên quan đến bài hát

## Cài đặt và triển khai

### Yêu cầu hệ thống

- Android 13 (API level 33) trở lên
- Kết nối internet cho tính năng đồng bộ Google Calendar và phát nhạc

### Xin cấp các quyền

- `READ_PHONE_STATE`: Đọc trạng thái cuộc gọi.
- `PROCESS_OUTGOING_CALLS`: Quản lý cuộc gọi đi.
- `READ_CALL_LOG`: Đọc nhật ký cuộc gọi.
- `READ_SMS`: Đọc tin nhắn SMS.
- `RECEIVE_SMS`: Nhận tin nhắn SMS.
- `INTERNET`: Truy cập Internet.
- `ACCESS_NETWORK_STATE`: Kiểm tra trạng thái kết nối mạng.
- `FOREGROUND_SERVICE`: Chạy dịch vụ nền.
- `WRITE_SETTINGS`: Cấp quyền thay đổi các cài đặt hệ thống như độ sáng màn hình.
- `READ_CONTACTS`: Đọc danh bạ.
- `CALL_PHONE`: Gọi điện thoại.
- `ANSWER_PHONE_CALLS`: Trả lời cuộc gọi.
- `SEND_SMS`: Gửi tin nhắn SMS.
- `RECEIVE_MMS`: Nhận MMS.
- `POST_NOTIFICATIONS`: Gửi thông báo.
- `ACCESS_WIFI_STATE`: Kiểm tra trạng thái kết nối Wi-Fi.
- `CHANGE_WIFI_STATE`: Thay đổi trạng thái Wi-Fi.
- `SCHEDULE_EXACT_ALARM`: Lập lịch báo thức chính xác.
- `USE_EXACT_ALARM`

### Hướng dẫn cài đặt

1. Clone repository
2. Mở dự án trong Android Studio
3. Cấu hình Gradle và tải các phụ thuộc
4. Tạo OAuth 2.0 Client ID trên Google Cloud Console
    - File GoogleSignInMannager và set Key đã tạo.
   ```java
    // Đưa key vào chỗ BuildConfig.GOOGLE_API_KEY
    public GoogleSignInManager(Context context) {
    this.context = context;
    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(new com.google.android.gms.common.api.Scope(CalendarScopes.CALENDAR))
            .requestIdToken(BuildConfig.GOOGLE_API_KEY)
            .build();

    googleSignInClient = GoogleSignIn.getClient(context, gso);
    }
    ``` 
5. Build và cài đặt ứng dụng trên thiết bị

## Phân tích chi tiết mã nguồn

### Cấu trúc package và tổ chức mã nguồn

Tổ chức cấu trúc mã nguồn như sau:

```
hcmute.edu.vn.linhvalocvabao.selfalarmproject
├── controller
│   ├── receivers 
│   └── services
├── models 
├── utils
├── views
│   ├── adapters
│   ├── fragments
│   ├── activities
│   └── viewmodels
│   └── MainActivity.java
```

### Demo

[Click here to view the video on Google Drive](https://drive.google.com/file/d/1V1RhWdT8_7uXCgZBA4_sH_p9Z7YyTNU-/view?usp=sharing)
