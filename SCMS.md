# **1\. Luồng quản lý thiết bị**

Hệ thống cho phép quản lý toàn bộ thiết bị trong nhà máy theo cấu trúc phân cấp từ hệ thống lớn đến từng thiết bị cụ thể. Mỗi hệ thống có thể bao gồm nhiều thiết bị thuộc các nhóm chuyên môn khác nhau như Cơ khí, Điện và Điều khiển \- Đo lường (CI).

Đối với mỗi thiết bị, hệ thống lưu trữ đầy đủ các thông tin nhận dạng bao gồm mã KKS, tên thiết bị, hệ thống trực thuộc, loại thiết bị và trạng thái hoạt động hiện tại. Ngoài ra, hệ thống hỗ trợ quản lý các thông số kỹ thuật động của thiết bị như lưu lượng, áp suất, điện áp, công suất,... thông qua cơ chế khai báo thông số linh hoạt, giúp đáp ứng được sự đa dạng của các loại thiết bị trong nhà máy.

Người dùng có thể tìm kiếm thiết bị theo mã KKS, tên thiết bị hoặc loại thiết bị. Khi truy cập vào thông tin chi tiết, hệ thống hiển thị toàn bộ hồ sơ kỹ thuật của thiết bị phục vụ cho quá trình vận hành và sửa chữa.

# **2\. Luồng xử lý yêu cầu sửa chữa và phiếu công tác**

Khi nhân viên vận hành phát hiện thiết bị xảy ra sự cố hoặc cần được kiểm tra, bảo dưỡng, nhân viên sẽ tạo một Phiếu yêu cầu sửa chữa trên hệ thống. Nội dung yêu cầu bao gồm thiết bị gặp sự cố, mô tả tình trạng, mức độ ưu tiên và các thông tin liên quan.

Sau khi được tiếp nhận, bộ phận sửa chữa sẽ xem xét và xác nhận yêu cầu. Từ Phiếu yêu cầu sửa chữa, Tổ trưởng hoặc người có thẩm quyền sẽ tạo Phiếu công tác để triển khai thực hiện công việc.

Phiếu công tác là trung tâm của toàn bộ quy trình sửa chữa. Trong phiếu công tác, người quản lý sẽ phân công các vai trò cần thiết như Người lãnh đạo công việc, Người chỉ huy trực tiếp, Người giám sát an toàn và các nhân viên thực hiện. Hệ thống cho phép cập nhật danh sách nhân sự tham gia theo thời gian thực nhằm đáp ứng các trường hợp thay đổi nhân sự hoặc đổi ca làm việc.

Sau khi hoàn tất việc bố trí nhân lực, hệ thống sẽ sinh Phiếu công tác dưới dạng tệp PDF để phục vụ việc in ấn, ký xác nhận và lưu trữ theo quy định an toàn của nhà máy. Phiếu công tác sau đó được trình lên bộ phận vận hành để thực hiện các thao tác cô lập thiết bị trước khi tiến hành sửa chữa.

Trong quá trình thực hiện, nếu công việc chưa thể hoàn thành trong thời gian dự kiến, người phụ trách có thể đề nghị gia hạn Phiếu công tác. Mọi thay đổi liên quan đến phiếu đều được ghi nhận vào nhật ký hệ thống nhằm phục vụ công tác kiểm tra và truy vết sau này.

Sau khi công việc kết thúc, Phiếu công tác được đóng lại và lưu vào lịch sử sửa chữa của thiết bị.

# **3\. Luồng quản lý vật tư, công cụ và bảo dưỡng định kỳ**

Trong quá trình sửa chữa, nếu cần sử dụng vật tư tiêu hao như dầu mỡ, hóa chất, vật liệu phụ trợ,... nhân viên có thể thực hiện yêu cầu xuất kho trực tiếp thông qua Phiếu công tác. Hệ thống sẽ ghi nhận đầy đủ các giao dịch nhập, xuất và điều chỉnh tồn kho để đảm bảo tính chính xác của số liệu vật tư.

Đối với các vật tư thay thế có giá trị lớn hoặc ảnh hưởng trực tiếp đến hoạt động của thiết bị, bộ phận sửa chữa phải lập Biên bản đánh giá kỹ thuật để xác nhận tình trạng hư hỏng và đề xuất phương án thay thế. Sau khi được phê duyệt, hệ thống sẽ tạo yêu cầu xuất kho tương ứng và cập nhật số lượng tồn kho.

Bên cạnh vật tư, hệ thống còn quản lý Công cụ dụng cụ phục vụ công tác sửa chữa. Các công cụ này được theo dõi theo hình thức mượn và trả, giúp kiểm soát số lượng hiện có cũng như trách nhiệm của người sử dụng.

Ngoài hoạt động sửa chữa khi phát sinh sự cố, hệ thống còn hỗ trợ quản lý công tác bảo dưỡng định kỳ. Đối với các thiết bị cần thay dầu, bôi mỡ hoặc kiểm tra theo chu kỳ, người quản lý có thể thiết lập kế hoạch bảo dưỡng với tần suất cụ thể. Hệ thống sẽ tự động nhắc lịch khi đến hạn thực hiện. Sau khi hoàn thành công việc bảo dưỡng, thông tin thực hiện sẽ được lưu vào lịch sử bảo dưỡng của thiết bị nhằm phục vụ công tác theo dõi và đánh giá độ tin cậy vận hành.

