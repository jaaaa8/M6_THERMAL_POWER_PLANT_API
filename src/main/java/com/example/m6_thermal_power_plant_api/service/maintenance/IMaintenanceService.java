package com.example.m6_thermal_power_plant_api.service.maintenance;

import com.example.m6_thermal_power_plant_api.dto.maintenance.CreateWorkOrderRequest;
import com.example.m6_thermal_power_plant_api.dto.maintenance.RepairRequestDTO;
import com.example.m6_thermal_power_plant_api.dto.maintenance.StopWorkOrderRequest;
import com.example.m6_thermal_power_plant_api.dto.maintenance.UpdateWorkOrderRequest;
import com.example.m6_thermal_power_plant_api.dto.maintenance.UpdateWorkOrderStatusRequest;
import com.example.m6_thermal_power_plant_api.dto.maintenance.WorkOrderDTO;
import com.example.m6_thermal_power_plant_api.dto.maintenance.WorkOrderDetailDTO;
import com.example.m6_thermal_power_plant_api.dto.maintenance.WorkOrderMemberDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Nghiệp vụ cho Quản đốc sửa chữa / Tổ trưởng.
 */
public interface IMaintenanceService {

    /**
     * User Story #39 (row 43): xem danh sách các yêu cầu sửa chữa đang chờ xử lý
     * (status = PENDING), CÓ PHÂN TRANG. Thứ tự sắp xếp lấy từ {@code pageable}
     * (controller mặc định createdAt giảm dần — mới nhất lên trước).
     */
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
     * Huỷ một phiếu công tác: đặt status = CANCELLED (KHÔNG hard-delete vì PCT là
     * chứng từ pháp lý). Dùng cho luồng "kho không cấp được vật tư → tạm đóng phiếu,
     * chờ rồi tạo phiếu mới".
     *
     * Quy tắc:
     *  - Không huỷ được phiếu đã COMPLETED (ném xung đột 409).
     *  - Phiếu đã CANCELLED: idempotent (trả về nguyên trạng, không lỗi).
     *  - Sau khi huỷ, nếu yêu cầu không còn phiếu nào "sống" (OPEN/IN_PROGRESS) thì
     *    đưa yêu cầu về PENDING để quay lại hàng chờ xử lý.
     */
    WorkOrderDTO cancelWorkOrder(Integer workOrderId);

    /**
     * User Story #42 (row 46): xem danh sách các phiếu công tác, tìm kiếm theo
     * nội dung hoặc số phiếu. Trả về danh sách CÓ PHÂN TRANG.
     *
     * @param search   từ khoá tìm trong orderCode, requestCode
     *                 (null hoặc rỗng = không lọc).
     * @param pageable phân trang + sắp xếp (mặc định createdAt giảm dần).
     */
    Page<WorkOrderDTO> listWorkOrders(String search, Pageable pageable);

    /**
     * Chi tiết đầy đủ một phiếu công tác: thông tin chung + danh sách thành viên
     * + DÒNG THỜI GIAN ra/vào khu vực làm việc (JOINED/LEFT, tăng dần theo thời
     * gian) + các phiếu cấp vật tư thay thế đã tạo cho phiếu.
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
     */
    java.util.List<Integer> getBusyEmployeeIds(Integer excludeWorkOrderId);

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
     * Cập nhật trạng thái phiếu sang COMPLETED — endpoint cập nhật status DUY NHẤT
     * cho việc hoàn thành, không sửa trường nào khác.
     * Idempotent nếu đã COMPLETED; từ chối (409) nếu CANCELLED hoặc đang
     * WAITING_FOR_APPROVAL (phải xử lý xong thủ tục gia hạn trước).
     */
    WorkOrderDTO completeWorkOrder(Integer workOrderId);

    /**
     * Tổ trưởng GỬI DUYỆT / TẠM DỪNG phiếu: tạo một dòng work_order_extensions
     * (reason + extendedUntil, CHƯA có người duyệt) và chuyển status →
     * WAITING_FOR_APPROVAL. Dùng cho cả 2 luồng: phiếu MỚI TẠO (OPEN) xin
     * Trưởng ca duyệt trước khi làm, và phiếu đang chạy tạm dừng cuối ngày.
     *
     * Bước duyệt là THỦ CÔNG NGOÀI HỆ THỐNG: bản giấy PCT (mục "Cho phép làm việc
     * và kết thúc công tác hàng ngày") được đưa tận tay Trưởng ca ký — môi trường
     * làm việc nguy hiểm nên người duyệt phải chịu trách nhiệm bằng chữ ký thật.
     * Cho phép từ mọi trạng thái đang sống trừ WAITING_FOR_APPROVAL (đang có
     * dòng gia hạn treo).
     */
    WorkOrderDTO stopWorkOrder(Integer workOrderId, StopWorkOrderRequest request);

    /**
     * Sửa thông tin phiếu công tác đang sống: leader / chỉ huy trực tiếp / giám
     * sát an toàn / thời gian / mô tả. Partial update — chỉ trường khác null
     * được ghi đè. KHÔNG áp ràng buộc trùng vai trò / chồng lấn giờ (hiện trường
     * thay đổi liên tục); chỉ từ chối (409) phiếu đã COMPLETED/CANCELLED.
     */
    WorkOrderDTO updateWorkOrder(Integer workOrderId, UpdateWorkOrderRequest request);

    /**
     * Cập nhật trạng thái phiếu theo máy trạng thái (modal "Cập nhật trạng thái"):
     * OPEN ─duyệt phiếu─► APPROVED ─bắt đầu─► IN_PROGRESS ─không kịp─► STOPPED
     * ─gửi duyệt lại (reason+extendedUntil, tạo dòng gia hạn)─► WAITING_FOR_APPROVAL
     * ─duyệt gia hạn (approvedBy = username)─► APPROVED ─► ... ─► COMPLETED;
     * mọi trạng thái sống ─► CANCELLED (side effect huỷ giữ nguyên).
     * Idempotent khi target = trạng thái hiện tại; 409 cho bước chuyển không hợp lệ.
     */
    WorkOrderDTO updateWorkOrderStatus(Integer workOrderId, UpdateWorkOrderStatusRequest request, String username);

    /**
     * Ghi nhận online việc Trưởng ca ĐÃ ký duyệt bản giấy: gắn tài khoản đang
     * đăng nhập vào approvedBy của dòng gia hạn đang chờ (người bấm chịu trách
     * nhiệm nhập đúng theo bản giấy) và chuyển status → APPROVED.
     * Chỉ cho phép khi phiếu đang WAITING_FOR_APPROVAL.
     */
    WorkOrderDTO approveExtension(Integer workOrderId, String approvedByUsername);

    /**
     * Mở (lại) phiếu để làm việc: OPEN → IN_PROGRESS (bắt đầu lần đầu) hoặc
     * APPROVED → IN_PROGRESS (Tổ trưởng bật lại nút đã tắt hôm trước, sau khi
     * gia hạn được duyệt).
     */
    WorkOrderDTO reopenWorkOrder(Integer workOrderId);
}