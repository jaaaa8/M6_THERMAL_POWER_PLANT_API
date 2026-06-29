# CHƯƠNG X. PHÂN TÍCH LUỒNG NGHIỆP VỤ HỆ THỐNG SCMS

## 1\. Luồng quản lý nhân sự và phân quyền

### Mô tả nghiệp vụ

Hệ thống quản lý thông tin nhân sự tham gia vào quá trình vận hành và sửa chữa trong nhà máy. Tuy nhiên, không phải tất cả nhân sự đều được phép truy cập hệ thống. Chỉ những nhân sự đảm nhận vai trò quản lý hoặc thực hiện các nghiệp vụ trên phần mềm mới được cấp tài khoản đăng nhập.

Mỗi tài khoản có thể được gán một hoặc nhiều vai trò khác nhau nhằm xác định phạm vi chức năng được sử dụng.

### Luồng xử lý

1. Quản trị viên tạo phòng ban.  
2. Quản trị viên khai báo thông tin nhân sự.  
3. Xác định nhân sự có cần sử dụng hệ thống hay không.  
4. Nếu có, tạo tài khoản đăng nhập.  
5. Gán vai trò phù hợp cho tài khoản.  
6. Người dùng đăng nhập và sử dụng các chức năng được phân quyền.

### Bảng dữ liệu liên quan

* phong\_ban  
* nhan\_su  
* tai\_khoan  
* vai\_tro  
* phan\_quyen\_tai\_khoan

## 2\. Luồng quản lý hệ thống và thiết bị

### Mô tả nghiệp vụ

Nhà máy được chia thành nhiều hệ thống khác nhau như: lò hơi, tua-bin, xử lý nước, nhiên liệu,...

Mỗi hệ thống bao gồm nhiều thiết bị. Thiết bị có thể thuộc nhóm cơ khí, điện hoặc CI (Control & Instrument).

Mỗi thiết bị được định danh duy nhất bằng mã KKS và có thể có nhiều thông số kỹ thuật khác nhau.

### Luồng xử lý

1. Khai báo hệ thống.  
2. Khai báo loại thiết bị.  
3. Khai báo thiết bị thuộc hệ thống.  
4. Khai báo các loại thông số kỹ thuật.  
5. Gán thông số tương ứng cho từng thiết bị.  
6. Người dùng tìm kiếm thiết bị theo:  
   * Tên thiết bị;  
   * Mã KKS;  
   * Loại thiết bị;  
   * Hệ thống.

### Bảng dữ liệu liên quan

* he\_thong  
* loai\_thiet\_bi  
* thiet\_bi  
* danh\_muc\_thong\_so  
* thong\_so\_thiet\_bi

## 3\. Luồng tạo phiếu yêu cầu sửa chữa

### Mô tả nghiệp vụ

Khi bộ phận vận hành phát hiện thiết bị gặp sự cố hoặc có dấu hiệu bất thường, nhân sự vận hành sẽ tạo phiếu yêu cầu sửa chữa để gửi sang bộ phận sửa chữa xử lý.

Phiếu yêu cầu chỉ mang tính chất ghi nhận sự cố, chưa phải là lệnh sửa chữa.

### Luồng xử lý

1. Người vận hành xác định thiết bị gặp sự cố.  
2. Chọn thiết bị cần sửa chữa.  
3. Nhập mô tả sự cố.  
4. Chọn mức độ ưu tiên.  
5. Gửi phiếu yêu cầu.  
6. Bộ phận sửa chữa tiếp nhận yêu cầu.

### Bảng dữ liệu liên quan

* phieu\_yeu\_cau\_sua\_chua  
* thiet\_bi  
* nhan\_su

## 4\. Luồng lập phiếu công tác

### Mô tả nghiệp vụ

Sau khi tiếp nhận yêu cầu sửa chữa, tổ trưởng hoặc quản đốc sửa chữa sẽ tạo phiếu công tác.

Phiếu công tác là căn cứ để bố trí nhân lực, triển khai công việc, thực hiện ký duyệt và kiểm soát an toàn.

### Luồng xử lý

1. Tiếp nhận phiếu yêu cầu sửa chữa.  
2. Xác định tổ thực hiện.  
3. Tạo phiếu công tác.  
4. Chỉ định:  
   * Người lãnh đạo công việc;  
   * Người chỉ huy trực tiếp;  
   * Người giám sát an toàn.  
5. Bổ sung danh sách nhân sự tham gia.  
6. Xuất phiếu công tác dưới dạng PDF.  
7. Thực hiện ký duyệt theo quy định.

### Bảng dữ liệu liên quan

* phieu\_cong\_tac  
* thanh\_vien\_phieu\_cong\_tac  
* phieu\_yeu\_cau\_sua\_chua  
* nhan\_su

## 5\. Luồng thay đổi nhân sự và gia hạn phiếu công tác

### Mô tả nghiệp vụ

Trong quá trình thực hiện công việc, phiếu công tác có thể kéo dài sang ngày tiếp theo hoặc cần thay đổi nhân sự do hết ca làm việc.

Hệ thống cần lưu lại toàn bộ lịch sử thay đổi nhằm phục vụ truy xuất sau này.

### Luồng xử lý

1. Kiểm tra tình trạng thực hiện phiếu.  
2. Xác định nhu cầu gia hạn hoặc đổi người.  
3. Thực hiện cập nhật nhân sự mới.  
4. Ghi nhận thời điểm tham gia và rút khỏi công việc.  
5. Nếu cần, tạo yêu cầu gia hạn phiếu.  
6. Người có thẩm quyền phê duyệt gia hạn.

### Bảng dữ liệu liên quan

* thanh\_vien\_phieu\_cong\_tac  
* gia\_han\_phieu\_cong\_tac  
* nhat\_ky\_phieu\_cong\_tac

## 6\. Luồng đánh giá kỹ thuật

### Mô tả nghiệp vụ

Sau khi kiểm tra thực tế thiết bị, tổ sửa chữa có thể phát hiện cần thay thế linh kiện hoặc vật tư.

Khi đó phải lập biên bản đánh giá kỹ thuật để làm căn cứ xuất vật tư thay thế.

### Luồng xử lý

1. Tiến hành kiểm tra thiết bị.  
2. Đánh giá nguyên nhân hư hỏng.  
3. Lập biên bản đánh giá kỹ thuật.  
4. Đính kèm tài liệu minh chứng.  
5. Đề xuất vật tư cần thay thế.  
6. Trình cấp có thẩm quyền xem xét.

### Bảng dữ liệu liên quan

* bien\_ban\_danh\_gia\_ky\_thuat  
* chi\_tiet\_vat\_tu\_de\_xuat  
* vat\_tu

## 7\. Luồng quản lý vật tư

### Mô tả nghiệp vụ

Hệ thống quản lý hai nhóm vật tư:

* Vật tư tiêu hao;  
* Vật tư thay thế.

Mọi hoạt động nhập kho, xuất kho hoặc điều chỉnh đều phải được ghi nhận.

### Luồng xử lý

1. Nhập vật tư vào kho.  
2. Cập nhật số lượng tồn.  
3. Khi có nhu cầu sử dụng:  
   * Xuất vật tư tiêu hao trực tiếp;  
   * Xuất vật tư thay thế dựa trên biên bản kỹ thuật.  
4. Ghi nhận lịch sử giao dịch kho.

### Bảng dữ liệu liên quan

* loai\_vat\_tu  
* vat\_tu  
* giao\_dich\_kho  
* bien\_ban\_danh\_gia\_ky\_thuat

## 8\. Luồng quản lý công cụ dụng cụ

### Mô tả nghiệp vụ

Công cụ dụng cụ là tài sản dùng chung, có thể được cấp phát tạm thời và hoàn trả sau khi sử dụng.

Khác với vật tư tiêu hao, công cụ dụng cụ không bị mất đi sau mỗi lần sử dụng.

### Luồng xử lý

1. Nhập công cụ vào kho.

2. Nhân sự đăng ký mượn công cụ.  
3. Thủ kho xác nhận giao công cụ.  
4. Sau khi hoàn thành công việc, nhân sự trả lại công cụ.  
5. Hệ thống cập nhật lịch sử mượn trả.

### Bảng dữ liệu liên quan

* cong\_cu\_dung\_cu  
* nhat\_ky\_muon\_tra\_cong\_cu  
* nhan\_su

## 9\. Luồng bảo dưỡng định kỳ

### Mô tả nghiệp vụ

Bên cạnh sửa chữa sự cố, hệ thống còn hỗ trợ quản lý bảo dưỡng định kỳ cho các thiết bị cần thay dầu hoặc bôi trơn theo chu kỳ.

Hệ thống sẽ tự động nhắc lịch khi đến hạn thực hiện.

### Luồng xử lý

1. Khai báo chu kỳ bảo dưỡng.  
2. Xác định ngày thực hiện tiếp theo.  
3. Hệ thống gửi thông báo khi đến hạn.  
4. Nhân sự thực hiện bảo dưỡng.  
5. Cập nhật kết quả thực hiện.  
6. Sinh lịch bảo dưỡng kế tiếp.

### Bảng dữ liệu liên quan

* ke\_hoach\_bao\_duong\_dau\_mo  
* lich\_su\_bao\_duong\_dau\_mo  
* thiet\_bi

## 10\. Luồng nghiệp vụ tổng thể của hệ thống

Thiết bị trong nhà máy được quản lý theo từng hệ thống. Khi phát sinh sự cố, bộ phận vận hành sẽ tạo phiếu yêu cầu sửa chữa.

Bộ phận sửa chữa tiếp nhận yêu cầu và lập phiếu công tác để tổ chức thực hiện. Trong quá trình sửa chữa có thể phát sinh thay đổi nhân sự, gia hạn phiếu hoặc đánh giá kỹ thuật để đề xuất vật tư thay thế.

Sau khi hoàn thành công việc, vật tư sử dụng được ghi nhận, phiếu được đóng lại và toàn bộ lịch sử được lưu trữ phục vụ tra cứu.

Song song với đó, hệ thống quản lý các hoạt động bảo dưỡng định kỳ nhằm hạn chế sự cố phát sinh trong quá trình vận hành nhà máy.

