package com.example.m6_thermal_power_plant_api.service.maintenance;

import com.example.m6_thermal_power_plant_api.dto.maintenance.CreateRepairRequestDTO;
import com.example.m6_thermal_power_plant_api.dto.maintenance.CreateWorkOrderRequest;
import com.example.m6_thermal_power_plant_api.dto.maintenance.RepairRequestDTO;
import com.example.m6_thermal_power_plant_api.dto.maintenance.StopWorkOrderRequest;
import com.example.m6_thermal_power_plant_api.dto.maintenance.UpdateWorkOrderRequest;
import com.example.m6_thermal_power_plant_api.dto.maintenance.UpdateWorkOrderStatusRequest;
import com.example.m6_thermal_power_plant_api.dto.maintenance.WorkOrderDTO;
import com.example.m6_thermal_power_plant_api.dto.maintenance.WorkOrderDetailDTO;
import com.example.m6_thermal_power_plant_api.dto.maintenance.WorkOrderMemberDTO;
import com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderEquipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Nghiệp vụ cho Quản đốc sửa chữa / Tổ trưởng.
 */
public interface IMaintenanceService {

    Page<RepairRequestDTO> getPendingRepairRequests(Pageable pageable);

    /**
     * User Story #40 (row 44): tạo một phiếu công tác (PCT) từ 1 yêu cầu sửa chữa.
     * Thông tin thiết bị lấy từ request; gắn người lãnh đạo công việc, chỉ huy
     * trực tiếp, người giám sát an toàn và các nhân viên làm việc.
     *
     * @param createdByUsername username tài khoản đăng nhập đang thao tác — lưu vào
     *                          created_by làm "Người cấp phiếu" trên bản in PCT
     *                          (null = không ghi nhận người cấp).
     */
    WorkOrderDTO createWorkOrderFromRequest(CreateWorkOrderRequest request, String createdByUsername);

    /** Như trên nhưng KHÔNG ghi nhận người cấp phiếu (giữ tương thích test/luồng cũ). */
    default WorkOrderDTO createWorkOrderFromRequest(CreateWorkOrderRequest request) {
        return createWorkOrderFromRequest(request, null);
    }

    /**
     * Tạo phiếu công tác (PCT) thủ công nhiều thiết bị — KHÔNG cần RepairRequest.
     * Body truyền equipmentIds (danh sách id thiết bị) thay vì repairRequestId.
     * Validation: XOR giữa repairRequestId và equipmentIds (xem {@link CreateWorkOrderRequest}).
     *
     * @param createdByUsername username tài khoản đăng nhập đang thao tác — lưu vào
     *                          created_by làm "Người cấp phiếu" trên bản in PCT
     *                          (null = không ghi nhận người cấp).
     */
    WorkOrderDTO createManualWorkOrder(CreateWorkOrderRequest request, String createdByUsername);

    /** Như trên nhưng KHÔNG ghi nhận người cấp phiếu (giữ tương thích test/luồng cũ). */
    default WorkOrderDTO createManualWorkOrder(CreateWorkOrderRequest request) {
        return createManualWorkOrder(request, null);
    }

    /**
     * Huỷ một phiếu công tác: đặt status = CANCELLED (KHÔNG hard-delete vì PCT là
     * chứng từ pháp lý). Dùng cho luồng "kho không cấp được vật tư → tạm đóng phiếu,
     * chờ rồi tạo phiếu mới".
     *
     * Quy tắc:
     *  - Không huỷ được phiếu đã COMPLETED (ném xung đột 409).
     *  - Không huỷ được phiếu ĐÃ CHẠY ít nhất một ngày công tác (409) — đã có công
     *    tác thực tế tại hiện trường thì phải đóng bằng "hoàn thành".
     *  - Chỉ NGƯỜI TẠO phiếu được huỷ (403), không miễn trừ cho ADMIN.
     *  - Phiếu đã CANCELLED: idempotent (trả về nguyên trạng, không lỗi).
     *  - Sau khi huỷ, nếu yêu cầu không còn phiếu nào "sống" thì đưa yêu cầu về
     *    PENDING để quay lại hàng chờ xử lý.
     *
     * @param username tài khoản đang đăng nhập — phải khớp người tạo phiếu.
     */
    WorkOrderDTO cancelWorkOrder(Integer workOrderId, String username);

    /**
     * User Story #42 (row 46): xem danh sách các phiếu công tác, tìm kiếm theo
     * BỐN bộ lọc độc lập kết hợp AND (null/rỗng = bỏ qua bộ lọc đó). Trả về
     * danh sách CÓ PHÂN TRANG.
     *
     * @param code        từ khoá tìm theo id phiếu (khi là số) / orderCode / mã
     *                    nhân viên của người lãnh đạo — KHÔNG tìm theo
     *                    requestCode/incidentDescription.
     * @param description từ khoá tìm theo mô tả sửa chữa (repairDescription).
     * @param fromDate    chỉ lấy phiếu có startTime từ NGÀY này trở đi.
     * @param toDate      chỉ lấy phiếu có startTime đến HẾT ngày này.
     * @param pageable    phân trang; sắp xếp mặc định theo tiến độ (OPEN → đang
     *                    làm → chờ duyệt gia hạn → hoàn thành → huỷ), cùng nhóm
     *                    thì mới tạo đứng trước.
     */
    Page<WorkOrderDTO> listWorkOrders(String code, String description,
                                      java.time.LocalDate fromDate, java.time.LocalDate toDate,
                                      Pageable pageable);

    /**
     * Chi tiết đầy đủ một phiếu công tác: thông tin chung + danh sách thành viên
     * + DÒNG THỜI GIAN ra/vào khu vực làm việc (JOINED/LEFT, tăng dần theo thời
     * gian) + các lần tạm dừng / gia hạn của phiếu.
     */
    WorkOrderDetailDTO getWorkOrderDetail(Integer workOrderId);

    /**
     * Id các nhân viên ĐANG BẬN — giữ vai trò leader / chỉ huy trực tiếp / giám
     * sát an toàn của một phiếu công tác đang sống, hoặc đang là thành viên CHƯA
     * RỜI (leftAt = null) của phiếu sống. Dùng cho UI lọc gợi ý khi thêm nhân sự
     * (CHỈ là bộ lọc hiển thị — backend không chặn thêm, giữ triết lý permissive).
     *
     * @param excludeWorkOrderId bỏ qua phiếu này khi xét (để thao tác nhân sự trên
     *                           chính phiếu đang mở không tự loại người của nó);
     *                           null = xét mọi phiếu sống.
     * @param statuses           chỉ xét phiếu có status thuộc danh sách này
     *                           (VD chỉ IN_PROGRESS cho ô Người giám sát an toàn);
     *                           null/rỗng = mọi trạng thái sống như trước.
     */
    java.util.List<Integer> getBusyEmployeeIds(
            Integer excludeWorkOrderId,
            java.util.List<com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderStatus> statuses);

    /**
     * Thêm nhân viên vào phiếu công tác đang chạy (joinedAt = now, leftAt = null).
     * Từ chối (409) nếu phiếu đã COMPLETED/CANCELLED hoặc nhân viên đang là thành
     * viên CHƯA RỜI của chính phiếu này. Nhân viên đã rời trước đó vào lại được —
     * tạo dòng member MỚI để lịch sử giữ đủ các cặp JOINED/LEFT.
     */
    WorkOrderMemberDTO addMember(Integer workOrderId, CreateWorkOrderRequest.MemberInput input);

    /**
     * Đánh dấu thành viên rời khu vực làm việc (leftAt = now). Idempotent: member
     * đã rời rồi thì trả về nguyên trạng. 404 nếu member không thuộc phiếu này.
     */
    WorkOrderMemberDTO leaveMember(Integer workOrderId, Integer memberId);

    /**
     * "Khoá phiếu hoàn thành": status → COMPLETED, đóng dấu giờ kết thúc thực tế
     * và đóng nốt ngày công tác còn đang mở. Không sửa trường nào khác.
     * Idempotent nếu đã COMPLETED; từ chối (409) nếu CANCELLED.
     */
    WorkOrderDTO completeWorkOrder(Integer workOrderId);

    /**
     * KHOÁ PHIẾU NGÀY khi hết ngày mà chưa xong việc: đóng dòng nhật ký ngày đang
     * mở (ghi closedAt + ghi chú tuỳ chọn) và đưa status về STOPPED để hôm sau mở
     * lại. Chỉ cho phép khi phiếu đang IN_PROGRESS (409 nếu không).
     */
    WorkOrderDTO closeWorkDay(Integer workOrderId, StopWorkOrderRequest request);

    /**
     * Sửa thông tin phiếu công tác đang sống: leader / chỉ huy trực tiếp / giám
     * sát an toàn / thời gian / mô tả. Partial update — chỉ trường khác null
     * được ghi đè. KHÔNG áp ràng buộc trùng vai trò / chồng lấn giờ (hiện trường
     * thay đổi liên tục); chỉ từ chối (409) phiếu đã COMPLETED/CANCELLED.
     */
    WorkOrderDTO updateWorkOrder(Integer workOrderId, UpdateWorkOrderRequest request);

    /**
     * Cập nhật trạng thái phiếu theo máy trạng thái (modal "Cập nhật trạng thái"):
     * STOPPED ─mở phiếu ngày─► IN_PROGRESS ─khoá phiếu ngày─► STOPPED
     *                               └─khoá phiếu hoàn thành─► COMPLETED;
     * STOPPED ─huỷ (chưa chạy ngày nào, đúng người tạo)─► CANCELLED.
     * Mỗi nhánh uỷ quyền cho method chuyên trách nên guard + side effect không bị
     * nhân bản. Idempotent khi target = trạng thái hiện tại; 409 cho bước chuyển
     * không hợp lệ.
     */
    WorkOrderDTO updateWorkOrderStatus(Integer workOrderId, UpdateWorkOrderStatusRequest request, String username);

    /**
     * MỞ PHIẾU NGÀY: ghi một dòng nhật ký ngày công tác (ngày = hôm nay, giờ mở =
     * bây giờ) và chuyển status → IN_PROGRESS. Lần mở ĐẦU TIÊN chính là bắt đầu
     * phiếu — không có thao tác "bắt đầu" riêng. Chỉ cho phép khi phiếu đang
     * STOPPED (409 nếu không). Idempotent với dòng ngày còn bỏ ngỏ chưa khoá.
     */

    /**
     * Cập nhật trạng thái làm việc của MỘT thiết bị trong PCT thủ công
     * (IN_PROGRESS ↔ COMPLETED). Chỉ áp dụng cho WO KHÔNG có RepairRequest.
     * 404 nếu WO/thiết bị không tồn tại hoặc thiết bị không thuộc phiếu;
     * 409 nếu phiếu đã kết thúc, WO từ yêu cầu, hoặc status = CANCELED.
     */
    WorkOrderDTO updateWorkOrderEquipmentStatus(Integer workOrderId, Integer equipmentId,
                                                WorkOrderEquipmentStatus status);

    /**
     * Ghi nhận online việc Trưởng ca ĐÃ ký duyệt bản giấy: gắn tài khoản đang
     * đăng nhập vào approvedBy của dòng gia hạn đang chờ (người bấm chịu trách
     * nhiệm nhập đúng theo bản giấy) và chuyển status → APPROVED.
     * Chỉ cho phép khi phiếu đang WAITING_FOR_APPROVAL.
     *
     * @param allowedDate NGÀY Trưởng ca cho phép làm tiếp (in vào cột "Ngày cho
     *                    phép tiếp tục làm việc" của bản PDF); null = hôm sau
     *                    ngày Tổ trưởng gửi duyệt.
     */
    WorkOrderDTO openWorkDay(Integer workOrderId);
}