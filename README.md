# M6 Thermal Power Plant — Backend API

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen)
![MySQL](https://img.shields.io/badge/MySQL-8-blue)
![Gradle](https://img.shields.io/badge/Gradle-build-informational)

API backend cho hệ thống quản lý bảo trì – vận hành nhà máy nhiệt điện (SCMS):
phiếu công tác, sửa chữa thiết bị, quản lý vật tư/công cụ, nhân sự, phân quyền
theo vai trò và thông báo real-time.

## Tech stack

- **Java 17**, **Spring Boot 3.5** (Web, Data JPA, Security, Validation, Mail, Actuator, WebSocket)
- **MySQL 8** + **Flyway** migration
- **JWT** cho xác thực, RBAC theo role (xem [`docs/ROLE_CODES.md`](docs/ROLE_CODES.md), [`docs/PERMISSION_MATRIX.md`](docs/PERMISSION_MATRIX.md))
- **WebSocket** (STOMP) cho realtime, **Lombok**
- Đóng gói **Docker**, CI/CD qua **Jenkins**

## Cấu trúc thư mục

```
src/main/java/.../m6_thermal_power_plant_api/
├── config/       # cấu hình Spring, Security, WebSocket...
├── controller/   # REST endpoint theo domain (auth, equipment, work_order, repair, ...)
├── dto/          # request/response object
├── entity/       # JPA entity
├── exception/    # xử lý lỗi tập trung
├── repository/   # Spring Data JPA repository
├── security/     # JWT filter, RBAC
├── service/      # business logic theo domain
└── util/
```

## Chạy lần đầu

1. `cp .env.example .env`
2. Mở `.env`, điền `DB_PASSWORD` = mật khẩu MySQL trên máy bạn
3. Bấm **Run** trong IntelliJ (hoặc `./gradlew bootRun`)

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

Quy ước đặt tên, cách khai báo biến mới, xử lý sự cố: xem
[`docs/BIEN_MOI_TRUONG.md`](docs/BIEN_MOI_TRUONG.md) — áp dụng cho cả frontend.

## Ghi chú DB

Nhớ sau khi chạy dự án thì phải copy 2 file MySQL script ở thư mục resources/db
và chạy để tạo cột active_flag để làm composite unique code cho entity.

## Build & test

```
./gradlew build       # build + chạy test
./gradlew bootRun      # chạy app
```

## Tài liệu

| File | Nội dung |
|------|----------|
| [`docs/BIEN_MOI_TRUONG.md`](docs/BIEN_MOI_TRUONG.md) | Quy ước biến môi trường (dùng chung 2 repo) |
| [`docs/API_AUTH.md`](docs/API_AUTH.md) | Xác thực API |
| [`docs/PERMISSION_MATRIX.md`](docs/PERMISSION_MATRIX.md) | Ma trận phân quyền theo role |
| [`docs/ROLE_CODES.md`](docs/ROLE_CODES.md) | Danh sách mã role |
| [`docs/SCMS.md`](docs/SCMS.md) | Phân tích luồng nghiệp vụ hệ thống |
| [`docs/userstory_SCMS.md`](docs/userstory_SCMS.md) | User story |
| [`docs/phieu_cong_tac.md`](docs/phieu_cong_tac.md) | Mẫu phiếu công tác |
| [`docs/CHANGELOG.md`](docs/CHANGELOG.md) | Lịch sử thay đổi |
