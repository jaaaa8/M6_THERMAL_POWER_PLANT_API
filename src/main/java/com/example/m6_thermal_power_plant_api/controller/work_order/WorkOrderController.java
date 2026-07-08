package com.example.m6_thermal_power_plant_api.controller.work_order;

import com.example.m6_thermal_power_plant_api.dto.maintenance.CreateWorkOrderRequest;
import com.example.m6_thermal_power_plant_api.dto.maintenance.RepairRequestDTO;
import com.example.m6_thermal_power_plant_api.dto.maintenance.WorkOrderDTO;
import com.example.m6_thermal_power_plant_api.dto.maintenance.WorkOrderDetailDTO;
import com.example.m6_thermal_power_plant_api.dto.maintenance.WorkOrderMemberDTO;
import com.example.m6_thermal_power_plant_api.service.maintenance.IMaintenanceService;
import com.example.m6_thermal_power_plant_api.util.UniqueCodeRetryExecutor;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API cho Quản đốc sửa chữa / Tổ trưởng — Sprint 1 :
 *  - Xem danh sách yêu cầu sửa chữa đang chờ xử lý (phân trang).
 *  - Tạo phiếu công tác (PCT) từ một yêu cầu.
 */
@RestController
@RequestMapping("/api/v1/work-orders")
public class WorkOrderController {

    private final IMaintenanceService maintenanceService;
    private final UniqueCodeRetryExecutor codeRetryExecutor;

    public WorkOrderController(IMaintenanceService maintenanceService,
                               UniqueCodeRetryExecutor codeRetryExecutor) {
        this.maintenanceService = maintenanceService;
        this.codeRetryExecutor = codeRetryExecutor;
    }



    /**
     * Tạo phiếu công tác từ một yêu cầu sửa chữa.
     *
     * Bọc bằng {@link UniqueCodeRetryExecutor}: vì controller KHÔNG @Transactional
     * nên mỗi lần gọi {@code createWorkOrderFromRequest} (vốn @Transactional) mở
     * một transaction riêng. Nếu orderCode trùng (hiếm) → constraint DB ném lỗi,
     * transaction rollback sạch, executor sinh lại mã + chạy lại toàn bộ thao tác.
     */
    @PostMapping
    public ResponseEntity<WorkOrderDTO> createWorkOrder(@Valid @RequestBody CreateWorkOrderRequest request) {
        WorkOrderDTO created = codeRetryExecutor.execute(
                () -> maintenanceService.createWorkOrderFromRequest(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Huỷ một phiếu công tác (đặt status = CANCELLED, KHÔNG xoá dòng — PCT là
     * chứng từ pháp lý). Dùng khi kho không cấp được vật tư, cần tạm đóng phiếu
     * để sau tạo phiếu mới. Sau khi huỷ, nếu yêu cầu không còn phiếu nào đang
     * hoạt động thì yêu cầu quay lại trạng thái PENDING.
     */
    @PatchMapping("/{id}/cancel")
    public WorkOrderDTO cancelWorkOrder(@PathVariable Integer id) {
        return maintenanceService.cancelWorkOrder(id);
    }

    /**
     * Danh sach phieu cong tac, CO PHAN TRANG + TIM KIEM.
     * Tham so query: {@code ?page=0&size=20&sort=createdAt,desc&search=...}
     * Mac dinh trang 20 dong, sap xep createdAt giam dan.
     *
     * @param search tu khoa tim trong orderCode / requestCode / noi dung.
     */
    @GetMapping
    public PagedModel<WorkOrderDTO> listWorkOrders(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        return new PagedModel<>(maintenanceService.listWorkOrders(search, pageable));
    }

    /**
     * Chi tiết một phiếu công tác: thông tin chung + thành viên hiện tại
     * (leftAt null = đang trong khu vực làm việc) + DÒNG THỜI GIAN ra/vào
     * (JOINED/LEFT tăng dần theo thời gian) + các phiếu cấp vật tư thay thế.
     */
    @GetMapping("/{id}")
    public WorkOrderDetailDTO getWorkOrderDetail(@PathVariable Integer id) {
        return maintenanceService.getWorkOrderDetail(id);
    }

    /**
     * Thêm nhân viên vào phiếu đang chạy (join). Nhân viên từng rời có thể vào
     * lại — tạo dòng member mới nên lịch sử giữ đủ các cặp JOINED/LEFT.
     */
    @PostMapping("/{id}/members")
    public ResponseEntity<WorkOrderMemberDTO> addMember(
            @PathVariable Integer id,
            @Valid @RequestBody CreateWorkOrderRequest.MemberInput input) {
        return ResponseEntity.status(HttpStatus.CREATED).body(maintenanceService.addMember(id, input));
    }

    /** Đánh dấu thành viên rời khu vực làm việc (set leftAt = now, idempotent). */
    @PatchMapping("/{id}/members/{memberId}/leave")
    public WorkOrderMemberDTO leaveMember(@PathVariable Integer id, @PathVariable Integer memberId) {
        return maintenanceService.leaveMember(id, memberId);
    }
}