package com.example.m6_thermal_power_plant_api.controller.work_order;

import com.example.m6_thermal_power_plant_api.dto.maintenance.CreateWorkOrderRequest;
import com.example.m6_thermal_power_plant_api.dto.maintenance.RepairRequestDTO;
import com.example.m6_thermal_power_plant_api.dto.maintenance.StopWorkOrderRequest;
import com.example.m6_thermal_power_plant_api.dto.maintenance.UpdateWorkOrderRequest;
import com.example.m6_thermal_power_plant_api.dto.maintenance.UpdateWorkOrderStatusRequest;
import com.example.m6_thermal_power_plant_api.dto.maintenance.WorkOrderDTO;
import com.example.m6_thermal_power_plant_api.dto.maintenance.WorkOrderDetailDTO;
import com.example.m6_thermal_power_plant_api.dto.maintenance.WorkOrderMemberDTO;
import com.example.m6_thermal_power_plant_api.service.maintenance.IMaintenanceService;
import com.example.m6_thermal_power_plant_api.service.pdf.WorkOrderPdfService;
import com.example.m6_thermal_power_plant_api.util.UniqueCodeRetryExecutor;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
 *
 * Phân vai (chốt 2026-07-20, đúng user story #37/#38):
 *  - MAINTENANCE_FOREMAN/TEAM_LEADER (class-level mặc định): HỒ SƠ phiếu —
 *    tạo PCT từ request, sửa thông tin, thành viên, PDF, cấp vật tư.
 *  - SHIFT_LEADER/CREW_LEADER (override method-level): MỌI bước chuyển trạng
 *    thái — duyệt, mở/đóng hằng ngày, gia hạn, khoá khi hoàn thành, huỷ
 *    (cancel/complete/stop/reopen/approve-extension/status; service còn chặn
 *    thêm lớp nữa qua requireWorkOrderStatusRole cho đường gọi gián tiếp).
 *  - GET list/detail: mở cho cả 2 nhóm (đều cần xem).
 */
@RestController
@RequestMapping("/api/v1/work-orders")
@PreAuthorize("hasAnyRole('MAINTENANCE_FOREMAN', 'TEAM_LEADER')")
public class WorkOrderController {

    private final IMaintenanceService maintenanceService;
    private final UniqueCodeRetryExecutor codeRetryExecutor;
    private final WorkOrderPdfService workOrderPdfService;

    public WorkOrderController(IMaintenanceService maintenanceService,
                               UniqueCodeRetryExecutor codeRetryExecutor,
                               WorkOrderPdfService workOrderPdfService) {
        this.maintenanceService = maintenanceService;
        this.codeRetryExecutor = codeRetryExecutor;
        this.workOrderPdfService = workOrderPdfService;
    }



    /**
     * Tạo phiếu công tác từ một yêu cầu sửa chữa.
     *
     * Bọc bằng {@link UniqueCodeRetryExecutor}: vì controller KHÔNG @Transactional
     * nên mỗi lần gọi {@code createWorkOrderFromRequest} (vốn @Transactional) mở
     * một transaction riêng. Nếu orderCode trùng (hiếm) → constraint DB ném lỗi,
     * transaction rollback sạch, executor sinh lại mã + chạy lại toàn bộ thao tác.
     */
    @PreAuthorize("hasAnyRole('MAINTENANCE_FOREMAN', 'TEAM_LEADER', 'SHIFT_LEADER', 'CREW_LEADER')")
    @PostMapping
    public ResponseEntity<WorkOrderDTO> createWorkOrder(@Valid @RequestBody CreateWorkOrderRequest request,
                                                        java.security.Principal principal) {
        String createdByUsername = principal != null ? principal.getName() : null;
        WorkOrderDTO created = codeRetryExecutor.execute(
                () -> maintenanceService.createWorkOrderFromRequest(request, createdByUsername));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Xuất bản in PDF của phiếu công tác theo mẫu giấy (snapshot dữ liệu hiện
     * tại: nhân sự, vào/ra vị trí, gia hạn). Đồng thời upload lên Cloudinary
     * (đè bản cũ cùng orderCode) và lưu URL vào pdf_path.
     */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> exportPdf(@PathVariable Integer id) {
        WorkOrderPdfService.WorkOrderPdf pdf = workOrderPdfService.render(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header("Content-Disposition", ContentDisposition.inline()
                        .filename(pdf.orderCode() + ".pdf")
                        .build()
                        .toString())
                .body(pdf.content());
    }

    /**
     * Huỷ một phiếu công tác (đặt status = CANCELLED, KHÔNG xoá dòng — PCT là
     * chứng từ pháp lý). Dùng khi kho không cấp được vật tư, cần tạm đóng phiếu
     * để sau tạo phiếu mới. Sau khi huỷ, nếu yêu cầu không còn phiếu nào đang
     * hoạt động thì yêu cầu quay lại trạng thái PENDING.
     */
    @PreAuthorize("hasAnyRole('SHIFT_LEADER', 'CREW_LEADER')")
    @PatchMapping("/{id}/cancel")
    public WorkOrderDTO cancelWorkOrder(@PathVariable Integer id) {
        return maintenanceService.cancelWorkOrder(id);
    }

    /**
     * Hoàn thành phiếu công tác — endpoint cập nhật status DUY NHẤT sang
     * COMPLETED, không sửa trường nào khác. Idempotent nếu đã COMPLETED;
     * 409 nếu CANCELLED hoặc đang chờ duyệt gia hạn.
     */
    @PreAuthorize("hasAnyRole('SHIFT_LEADER', 'CREW_LEADER')")
    @PatchMapping("/{id}/complete")
    public WorkOrderDTO completeWorkOrder(@PathVariable Integer id) {
        return maintenanceService.completeWorkOrder(id);
    }

    /**
     * Tổ trưởng gửi duyệt / tạm dừng phiếu (từ mọi trạng thái đang sống): tạo
     * dòng gia hạn chờ duyệt + status → WAITING_FOR_APPROVAL. Bước duyệt diễn ra
     * NGOÀI hệ thống: bản giấy PCT được đưa tận tay Trưởng ca ký.
     */
    @PreAuthorize("hasAnyRole('SHIFT_LEADER', 'CREW_LEADER')")
    @PatchMapping("/{id}/stop")
    public WorkOrderDTO stopWorkOrder(@PathVariable Integer id,
                                      @Valid @RequestBody StopWorkOrderRequest request) {
        return maintenanceService.stopWorkOrder(id, request);
    }

    /**
     * Sửa thông tin phiếu đang sống (partial update — chỉ trường khác null được
     * ghi đè): nhân sự phụ trách, thời gian, mô tả. Hiện trường thay đổi liên
     * tục nên KHÔNG áp ràng buộc lúc tạo; phiếu COMPLETED/CANCELLED trả 409.
     */
    @PatchMapping("/{id}")
    public WorkOrderDTO updateWorkOrder(@PathVariable Integer id,
                                        @RequestBody UpdateWorkOrderRequest request) {
        return maintenanceService.updateWorkOrder(id, request);
    }

    /**
     * Cập nhật trạng thái phiếu — endpoint DUY NHẤT cho modal "Cập nhật trạng
     * thái": duyệt phiếu, bắt đầu, tạm dừng, gửi duyệt gia hạn, duyệt gia hạn,
     * hoàn thành, huỷ. Bước chuyển không hợp lệ trả 409.
     */
    @PreAuthorize("hasAnyRole('SHIFT_LEADER', 'CREW_LEADER')")
    @PatchMapping("/{id}/status")
    public WorkOrderDTO updateWorkOrderStatus(@PathVariable Integer id,
                                              @Valid @RequestBody UpdateWorkOrderStatusRequest request,
                                              java.security.Principal principal) {
        return maintenanceService.updateWorkOrderStatus(id, request,
                principal != null ? principal.getName() : null);
    }

    /**
     * Ghi nhận online việc Trưởng ca ĐÃ ký duyệt bản giấy: tài khoản đang đăng
     * nhập được lưu vào approvedBy (người bấm chịu trách nhiệm nhập đúng theo
     * bản giấy) + status → APPROVED.
     */
    @PreAuthorize("hasAnyRole('SHIFT_LEADER', 'CREW_LEADER')")
    @PatchMapping("/{id}/approve-extension")
    public WorkOrderDTO approveExtension(@PathVariable Integer id, java.security.Principal principal) {
        return maintenanceService.approveExtension(id, principal.getName());
    }

    /**
     * Mở (lại) phiếu để làm việc: OPEN → IN_PROGRESS (bắt đầu lần đầu) hoặc
     * APPROVED → IN_PROGRESS (bật lại nút đã tắt hôm trước, sau khi duyệt).
     */
    @PreAuthorize("hasAnyRole('SHIFT_LEADER', 'CREW_LEADER')")
    @PatchMapping("/{id}/reopen")
    public WorkOrderDTO reopenWorkOrder(@PathVariable Integer id) {
        return maintenanceService.reopenWorkOrder(id);
    }

    /**
     * Danh sach phieu cong tac, CO PHAN TRANG + TIM KIEM.
     * Tham so query: {@code ?page=0&size=20&sort=createdAt,desc&search=...}
     * Mac dinh trang 20 dong, sap xep createdAt giam dan.
     *
     * @param search tu khoa tim trong orderCode / requestCode / noi dung.
     *
     * Mở cho cả role "duyệt" (SHIFT_LEADER/CREW_LEADER) — họ cần xem danh
     * sách để biết phiếu nào đang chờ duyệt (khác class-level mặc định chỉ
     * cho vận hành, vì đây thuần là xem, không phải thao tác trạng thái).
     */
    @PreAuthorize("hasAnyRole('MAINTENANCE_FOREMAN', 'TEAM_LEADER', 'SHIFT_LEADER', 'CREW_LEADER')")
    @GetMapping
    public PagedModel<WorkOrderDTO> listWorkOrders(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        return new PagedModel<>(maintenanceService.listWorkOrders(search, pageable));
    }

    /**
     * Id nhân viên ĐANG BẬN ở một phiếu công tác sống bất kỳ (giữ vai trò phụ
     * trách hoặc là thành viên chưa rời) — UI dùng để ẩn khỏi gợi ý khi thêm
     * nhân sự. Chỉ là bộ lọc hiển thị, backend KHÔNG chặn thêm (permissive).
     *
     * @param excludeWorkOrderId bỏ qua phiếu này khi xét (thao tác trên chính nó).
     */
    @PreAuthorize("hasAnyRole('MAINTENANCE_FOREMAN', 'TEAM_LEADER', 'SHIFT_LEADER', 'CREW_LEADER')")
    @GetMapping("/busy-employees")
    public java.util.List<Integer> getBusyEmployees(
            @RequestParam(required = false) Integer excludeWorkOrderId) {
        return maintenanceService.getBusyEmployeeIds(excludeWorkOrderId);
    }

    /**
     * Chi tiết một phiếu công tác: thông tin chung + thành viên hiện tại
     * (leftAt null = đang trong khu vực làm việc) + DÒNG THỜI GIAN ra/vào
     * (JOINED/LEFT tăng dần theo thời gian) + các phiếu cấp vật tư thay thế.
     *
     * Mở cho cả role "duyệt" — xem chi tiết trước khi bấm duyệt/duyệt gia hạn.
     */
    @PreAuthorize("hasAnyRole('MAINTENANCE_FOREMAN', 'TEAM_LEADER', 'SHIFT_LEADER', 'CREW_LEADER')")
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