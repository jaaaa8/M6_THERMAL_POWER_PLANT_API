# Biến môi trường — quy ước dùng chung cho cả 2 repo

Áp dụng cho `M6_THERMAL_POWER_PLANT_API` (backend) và `M6_THERMAL_POWER_PLANT`
(frontend).

**Vấn đề cần giải quyết:** mật khẩu MySQL mỗi máy một khác, nhưng trước đây nó
nằm thẳng trong `application.properties`. Hệ quả: mỗi lần merge với `main` là
mật khẩu bị đổi về máy người khác, phải sửa tay lại; và mọi secret (Cloudinary,
Gmail) đều nằm trong lịch sử git, ai clone repo cũng đọc được.

**Cách giải quyết:** giá trị thật chuyển sang file `.env` — file này **không
commit**, nên mỗi máy giữ giá trị riêng mà không đụng nhau.

---

## 1. Chạy lần đầu (dev mới vào team)

### Backend

```bash
cd M6_THERMAL_POWER_PLANT_API
cp .env.example .env
```

Mở `.env`, điền đúng **một dòng**:

```
DB_PASSWORD=<mật khẩu MySQL trên máy bạn>
```

Rồi bấm **Run** trong IntelliJ. Không cần cài thêm gì, không cần sửa Run
Configuration.

### Frontend

```bash
cd M6_THERMAL_POWER_PLANT/m6-thermal-power-plant
cp .env.example .env
npm install
npm run dev
```

Để `VITE_API_BASE_URL` **trống** là chạy được. Chỉ điền nếu backend máy bạn
chạy cổng khác 8080.

---

## 2. File `.env` đặt ở đâu

Đặt ở **thư mục gốc của mỗi repo** — nơi bạn gõ lệnh chạy app:

| Repo | Đường dẫn | Nằm cạnh file |
|---|---|---|
| Backend | `M6_THERMAL_POWER_PLANT_API/.env` | `build.gradle` |
| Frontend | `M6_THERMAL_POWER_PLANT/m6-thermal-power-plant/.env` | `package.json` |

**Không** đặt trong `src/main/resources/`. Spring tìm `.env` theo *working
directory* (thư mục app đang chạy), không theo vị trí file `.jar`. Đặt sai chỗ
thì Spring không thấy file và không báo lỗi gì — chỉ im lặng bỏ qua.

Cả hai đường dẫn đều đã nằm trong `.gitignore`. **Đừng bao giờ dùng
`git add -f .env`** để ép commit.

---

## 3. Quy ước đặt tên

**`UPPER_SNAKE_CASE`** — chữ in hoa, ngăn cách bằng gạch dưới. Không dùng
`camelCase`, không dùng gạch ngang.

Đặt theo mẫu **`<NHÓM>_<THUỘC_TÍNH>`**, nhóm trước, thuộc tính sau:

```
DB_PASSWORD              đúng
CLOUDINARY_API_SECRET    đúng
MAIL_USERNAME            đúng

passwordDb               sai — không phải UPPER_SNAKE_CASE
DB-PASSWORD              sai — dùng gạch ngang
PASSWORD                 sai — không biết mật khẩu của cái gì
PASSWORD_FOR_LOGIN_PAGE  sai — tên theo NƠI DÙNG, đổi chỗ dùng là tên vô nghĩa
```

Tên mô tả **giá trị đó là gì**, không mô tả nơi nó được dùng.

### Ba quy ước riêng, dễ sai

**Frontend bắt buộc có tiền tố `VITE_`.** Vite cố tình bỏ qua mọi biến không có
tiền tố này. Đặt `API_BASE_URL` thì code đọc ra `undefined`, không có cảnh báo.

**Đừng đặt tên bắt đầu bằng `SPRING_`.** Spring Boot tự map biến môi trường
`SPRING_DATASOURCE_PASSWORD` vào property `spring.datasource.password`. Tự đặt
tên kiểu đó dễ vô tình ghi đè cấu hình khung mà không biết.

**Đừng dùng dấu nháy trong `.env`.** Spring coi dấu nháy là một phần của giá
trị.

```
DB_PASSWORD=matkhau123        đúng
DB_PASSWORD="matkhau123"      sai — mật khẩu thành 12 ký tự, có cả 2 dấu nháy
```

---

## 4. Cách khai báo biến mới trong code

### Backend — `application.properties`

Cú pháp `${TÊN_BIẾN:giá_trị_mặc_định}`. Chọn một trong ba nhóm:

| Loại giá trị | Cách viết | Khi nào dùng |
|---|---|---|
| Vô hại, giống nhau mọi máy | `${DB_URL:jdbc:mysql://localhost:3306/...}` | Clone về là chạy ngay, không bắt ai phải điền |
| Secret **bắt buộc** để app chạy | `${DB_PASSWORD}` — **không** dấu `:` | Thiếu thì app phải chết, không được chạy nửa vời |
| Secret của **tính năng phụ** | `${CLOUDINARY_API_SECRET:}` — mặc định rỗng | Ai không làm tính năng đó vẫn boot được app |

Đọc giá trị trong code Java như property bình thường:

```java
@Value("${cloudinary.api-key}")
private String apiKey;
```

Bạn đọc `cloudinary.api-key`, **không** đọc `CLOUDINARY_API_KEY`. Việc nối biến
môi trường vào property là do `application.properties` làm.

### Frontend — `import.meta.env`

```js
const BASE_URL = import.meta.env.VITE_API_BASE_URL || ''
```

**Không dùng `process.env`.** Vite nạp `.env` vào `import.meta.env`, không nạp
vào `process.env` — dùng nhầm thì luôn nhận `undefined` mà không có lỗi. Lỗi
này đã từng xảy ra thật ở `vite.config.js`, khiến proxy luôn trỏ về cổng 8080 dù
`.env` khai cổng khác.

Riêng trong `vite.config.js` (chạy ở Node, không có `import.meta.env`) thì dùng
`loadEnv`:

```js
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  return { /* ... env.VITE_API_BASE_URL ... */ }
})
```

---

## 5. ⚠️ Biến `VITE_*` là CÔNG KHAI

Vite **nhúng thẳng giá trị vào file `dist/*.js`** lúc build. Bất kỳ ai mở
DevTools trên trang production đều đọc được. Đây không phải lỗ hổng cấu hình —
đây là cách Vite hoạt động, không có cách nào giấu.

**Tuyệt đối không đặt vào `VITE_*`:** mật khẩu, api-secret, token, khoá riêng,
connection string database.

Secret chỉ được nằm ở backend. Frontend cần dữ liệu bảo mật thì gọi API backend,
để backend giữ khoá.

---

## 6. Quy trình thêm một biến mới

Làm đủ 4 bước, **trong cùng một PR**:

1. **Khai trong code** — `application.properties` (backend) hoặc chỗ dùng
   `import.meta.env` (frontend), theo mục 4.
2. **Thêm vào `.env.example`** — kèm comment giải thích biến này để làm gì và
   lấy giá trị ở đâu. Ghi **giá trị giả hoặc để trống**, không bao giờ ghi giá
   trị thật.
3. **Thêm giá trị thật vào `.env` của máy bạn** — file này không đi theo PR.
4. **Báo team trong nhóm chat**: "vừa thêm biến `X`, pull về nhớ thêm dòng
   `X=...` vào `.env`". Người khác pull về mà không đọc `.env.example` sẽ gặp
   lỗi khó hiểu.

Nếu biến mới là secret **production cần dùng** → báo thêm cho người phụ trách
CI/CD để thêm vào AWS SSM. Chỉ sửa `.env.example` là production không có giá trị
đó.

---

## 7. Danh sách biến hiện có (backend)

| Biến | Bắt buộc? | Dùng để làm gì |
|---|---|---|
| `DB_PASSWORD` | **Có** | Mật khẩu MySQL máy bạn |
| `DB_URL` | Không | Chỉ khi MySQL máy bạn khác cổng / tên DB mặc định |
| `DB_USERNAME` | Không | Mặc định `root` |
| `CLOUDINARY_CLOUD_NAME` | Không | Xuất / lưu PDF phiếu công tác |
| `CLOUDINARY_API_KEY` | Không | như trên |
| `CLOUDINARY_API_SECRET` | Không | như trên |
| `MAIL_USERNAME` | Không | Nhắc trả CCDC quá hạn qua email |
| `MAIL_PASSWORD` | Không | **App Password** của Google, không phải mật khẩu đăng nhập |
| `JWT_SECRET` | Không | Có mặc định sẵn cho dev; production dùng giá trị riêng |

Frontend chỉ có `VITE_API_BASE_URL` (để trống là được).

Chi tiết và giá trị mẫu: xem `.env.example` của từng repo — đó mới là nguồn
chuẩn, bảng này chỉ để nhìn nhanh.

---

## 8. Xử lý sự cố

### App dừng với `Access denied for user 'root'@'localhost' (using password: YES)`

Nhiều khả năng **chưa có file `.env`**, hoặc `DB_PASSWORD` sai.

Cần biết một điều dễ gây hiểu nhầm: **Spring không báo tên biến bị thiếu.**
`spring.datasource.password` đi qua *relaxed binder*, đường này không ném lỗi
khi không giải được `${DB_PASSWORD}` — nó truyền nguyên chuỗi ký tự
`${DB_PASSWORD}` xuống làm mật khẩu, và MySQL từ chối. Nên thông báo lỗi trông
như sai mật khẩu chứ không như thiếu cấu hình.

Kiểm tra theo thứ tự:

1. File `M6_THERMAL_POWER_PLANT_API/.env` có tồn tại không
2. Working directory trong Run Configuration của IntelliJ có trỏ đúng thư mục
   gốc repo không
3. `DB_PASSWORD` có đúng mật khẩu MySQL máy bạn không, có lỡ để dấu nháy không

### App dừng với `Could not resolve placeholder 'CLOUDINARY_...'`

Ngược lại với trên: `cloudinary.*` inject bằng `@Value` nên đường này **có** ném
lỗi rõ tên biến. Điền 3 biến `CLOUDINARY_*` vào `.env`.

### `gradlew test` PASS nhưng app không chạy được

Bình thường, không phải phép thử hợp lệ. Test dùng profile `test` → chạy H2 chứ
không phải MySQL, và `application-test.properties` ghi đè `spring.datasource.password`.
Nên test **luôn PASS dù không có `.env`**. Muốn kiểm chứng `.env` phải chạy
`bootRun` hoặc bấm Run trong IntelliJ.

### Frontend: sửa `.env` mà không thấy tác dụng

Vite chỉ đọc `.env` lúc khởi động. Dừng `npm run dev` rồi chạy lại.

---

## 9. Production

Production **không dùng file `.env`**. Giá trị được tiêm vào container lúc chạy:
secret nằm trong AWS SSM Parameter Store (kiểu `SecureString`), ECS đọc ra và
đưa vào biến môi trường của container. Phần này do người phụ trách CI/CD quản,
dev thường không cần đụng tới.

Điều dev cần nhớ: **thêm secret mới thì phải báo người phụ trách CI/CD.** Sửa
`.env.example` chỉ giúp đồng đội chạy máy cá nhân, không tự động làm production
có giá trị đó.

---

## 10. Nếu lỡ commit secret

Secret đã lộ — **`git rm` là không đủ**, giá trị vẫn nằm trong lịch sử git và ai
clone repo cũng lấy lại được.

Việc phải làm là **đổi (rotate) secret đó**: tạo khoá mới ở nhà cung cấp
(Google, Cloudinary...), thu hồi khoá cũ.

**Báo người phụ trách CI/CD trước khi đổi.** Giá trị cũ đang nằm trong AWS SSM;
đổi mà không cập nhật SSM là production hỏng đúng chức năng đó.
