# M6 Thermal Power Plant — Backend API

## Chạy lần đầu

1. `cp .env.example .env`
2. Mở `.env`, điền `DB_PASSWORD` = mật khẩu MySQL trên máy bạn
3. Bấm **Run** trong IntelliJ

Mọi giá trị nhạy cảm đọc từ `.env` ở gốc repo (`spring.config.import` trong
`application.properties`). File `.env` không được commit, nên mỗi máy giữ mật
khẩu MySQL của riêng mình mà không đụng nhau khi merge.

### App dừng với lỗi `Access denied for user 'root'@'localhost'`?

Nhiều khả năng bạn **chưa tạo file `.env`**. Spring Boot không báo lỗi rõ ràng
khi thiếu biến — nó truyền nguyên chuỗi `${DB_PASSWORD}` xuống làm mật khẩu, và
MySQL từ chối. Kiểm tra:

- File `.env` có tồn tại ở gốc repo (`M6_THERMAL_POWER_PLANT_API/.env`) không
- Working directory trong Run Configuration của IntelliJ có trỏ đúng vào thư mục
  đó không — Spring tìm `.env` theo working directory, không theo vị trí file jar
- Giá trị `DB_PASSWORD` trong `.env` có đúng mật khẩu MySQL máy bạn không

**Không bao giờ commit file `.env`.** Thêm biến môi trường mới thì cập nhật
`.env.example` trong cùng PR, kèm mô tả và giá trị giả — không kèm giá trị thật.

## Ghi chú DB

Nhớ sau khi chạy dự án thì phải copy 2 file MySQL script ở thư mục resources/db
và chạy để tạo cột active_flag để làm composite unique code cho entity.
