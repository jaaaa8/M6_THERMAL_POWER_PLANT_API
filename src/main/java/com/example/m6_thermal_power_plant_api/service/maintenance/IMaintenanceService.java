package com.example.m6_thermal_power_plant_api.service.maintenance;

import com.example.m6_thermal_power_plant_api.dto.maintenance.CreateWorkOrderRequest;
import com.example.m6_thermal_power_plant_api.dto.maintenance.RepairRequestDTO;
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
     */
    WorkOrderDTO createWorkOrderFromRequest(CreateWorkOrderRequest request);

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
}