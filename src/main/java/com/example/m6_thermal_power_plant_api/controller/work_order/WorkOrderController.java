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
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;

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
    @PreAuthorize("hasAnyRole('MAINTENANCE_FOREMAN','TEAM_LEADER')")
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
     * chứng từ pháp lý). Dùng khi phiếu không còn cần thiết nữa. Sau khi huỷ, nếu
     * yêu cầu không còn phiếu nào đang hoạt động thì quay lại trạng thái PENDING.
     *
     * Ba điều kiện chồng lên nhau: đúng role (chặn ở đây), ĐÚNG NGƯỜI TẠO phiếu
     * và phiếu CHƯA chạy ngày công tác nào (cả hai chặn ở service — role thôi là
     * chưa đủ vì mọi Tổ trưởng đều có cùng role).
     */
    @PreAuthorize("hasAnyRole('TEAM_LEADER','MAINTENANCE_FOREMAN')")
    @PatchMapping("/{id}/cancel")
    public WorkOrderDTO cancelWorkOrder(@PathVariable Integer id,
                                        java.security.Principal principal) {
        return maintenanceService.cancelWorkOrder(id, principal != null ? principal.getName() : null);
    }

    /**
     * "Khoá phiếu hoàn thành" — endpoint cập nhật status DUY NHẤT sang COMPLETED,
     * không sửa trường nào khác. Đóng nốt ngày công tác còn đang mở. Idempotent
     * nếu đã COMPLETED; 409 nếu CANCELLED.
     */
    @PreAuthorize("hasAnyRole('SHIFT_LEADER','CREW_LEADER')")
    @PatchMapping("/{id}/complete")
    public WorkOrderDTO completeWorkOrder(@PathVariable Integer id) {
        return maintenanceService.completeWorkOrder(id);
    }

    /**
     * "Khoá phiếu ngày" khi hết ngày mà chưa xong việc: đóng dòng nhật ký ngày
     * đang mở (ghi chú tuỳ chọn) + status → STOPPED để hôm sau mở lại.
     * 409 nếu phiếu không đang IN_PROGRESS.
     */
    @PreAuthorize("hasAnyRole('SHIFT_LEADER','CREW_LEADER')")
    @PatchMapping("/{id}/close-day")
    public WorkOrderDTO closeWorkDay(@PathVariable Integer id,
                                     @RequestBody(required = false) StopWorkOrderRequest request) {
        return maintenanceService.closeWorkDay(id, request);
    }

    /**
     * Sửa thông tin phiếu đang sống (partial update — chỉ trường khác null được
     * ghi đè): nhân sự phụ trách, thời gian, mô tả. Hiện trường thay đổi liên
     * tục nên KHÔNG áp ràng buộc lúc tạo; phiếu COMPLETED/CANCELLED trả 409.
     */
    @PreAuthorize("hasAnyRole('MAINTENANCE_FOREMAN','TEAM_LEADER')")
    @PatchMapping("/{id}")
    public WorkOrderDTO updateWorkOrder(@PathVariable Integer id,
                                        @RequestBody UpdateWorkOrderRequest request) {
        return maintenanceService.updateWorkOrder(id, request);
    }

    /**
     * Cập nhật trạng thái phiếu cho modal "Cập nhật trạng thái": mở phiếu ngày,
     * khoá phiếu ngày, khoá phiếu hoàn thành. Bước chuyển không hợp lệ trả 409.
     *
     * KHÔNG dùng cho HUỶ phiếu, dù switch trong service có nhánh CANCELLED:
     * huỷ thuộc về người CẤP phiếu (Tổ trưởng / Quản đốc SC) nên đi qua
     * {@code PATCH /{id}/cancel} với @PreAuthorize riêng. Đừng thêm 2 role đó
     * vào đây để "cho tiện" — họ sẽ mở/khoá được cả phiếu ngày, vì các nhánh
     * đó không kiểm role lại ở tầng service.
     */
    @PreAuthorize("hasAnyRole('SHIFT_LEADER','CREW_LEADER')")
    @PatchMapping("/{id}/status")
    public WorkOrderDTO updateWorkOrderStatus(@PathVariable Integer id,
                                              @Valid @RequestBody UpdateWorkOrderStatusRequest request,
                                              java.security.Principal principal) {
        return maintenanceService.updateWorkOrderStatus(id, request,
                principal != null ? principal.getName() : null);
    }

    /**
     * "Mở phiếu ngày": ghi một dòng nhật ký ngày công tác + status → IN_PROGRESS.
     * Lần mở ĐẦU TIÊN chính là bắt đầu phiếu. 409 nếu phiếu không đang STOPPED.
     */
    @PreAuthorize("hasAnyRole('SHIFT_LEADER','CREW_LEADER')")
    @PatchMapping("/{id}/open-day")
    public WorkOrderDTO openWorkDay(@PathVariable Integer id) {
        return maintenanceService.openWorkDay(id);
    }

    /**
     * Danh sach phieu cong tac, CO PHAN TRANG + TIM KIEM theo 4 bo loc doc lap
     * ket hop AND (bo trong = khong loc).
     * Tham so query: {@code ?page=0&size=20&code=...&description=...&fromDate=2026-07-01&toDate=2026-07-31}
     * Mac dinh trang 20 dong, sap xep theo TIEN DO: dang song (STOPPED /
     * IN_PROGRESS) → COMPLETED → CANCELLED; cung nhom thi phieu moi tao dung truoc.
     *
     * @param code        tu khoa tim theo id phieu (khi la so) / orderCode / ma
     *                    nhan vien cua nguoi lanh dao — KHONG tim theo
     *                    requestCode / noi dung su co cua yeu cau.
     * @param description tu khoa tim theo mo ta sua chua (repairDescription).
     * @param fromDate    chi lay phieu co startTime tu ngay nay (yyyy-MM-dd).
     * @param toDate      chi lay phieu co startTime den HET ngay nay (yyyy-MM-dd).
     */
    @GetMapping
    public PagedModel<WorkOrderDTO> listWorkOrders(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @PageableDefault(size = 20) Pageable pageable) {
        return new PagedModel<>(maintenanceService.listWorkOrders(code, description, fromDate, toDate, pageable));
    }

    /**
     * Id nhân viên ĐANG BẬN ở một phiếu công tác sống bất kỳ (giữ vai trò phụ
     * trách hoặc là thành viên chưa rời) — UI dùng để ẩn khỏi gợi ý khi thêm
     * nhân sự. Chỉ là bộ lọc hiển thị, backend KHÔNG chặn thêm (permissive).
     *
     * @param excludeWorkOrderId bỏ qua phiếu này khi xét (thao tác trên chính nó).
     * @param statuses chỉ xét phiếu có status thuộc danh sách (VD
     *                 {@code ?statuses=IN_PROGRESS} cho ô Người giám sát an toàn);
     *                 không truyền = mọi trạng thái sống.
     */
    @GetMapping("/busy-employees")
    public java.util.List<Integer> getBusyEmployees(
            @RequestParam(required = false) Integer excludeWorkOrderId,
            @RequestParam(required = false) java.util.List<com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderStatus> statuses) {
        return maintenanceService.getBusyEmployeeIds(excludeWorkOrderId, statuses);
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
    @PreAuthorize("hasAnyRole('MAINTENANCE_FOREMAN','TEAM_LEADER')")
    @PostMapping("/{id}/members")
    public ResponseEntity<WorkOrderMemberDTO> addMember(
            @PathVariable Integer id,
            @Valid @RequestBody CreateWorkOrderRequest.MemberInput input) {
        return ResponseEntity.status(HttpStatus.CREATED).body(maintenanceService.addMember(id, input));
    }

    /** Đánh dấu thành viên rời khu vực làm việc (set leftAt = now, idempotent). */
    @PreAuthorize("hasAnyRole('MAINTENANCE_FOREMAN','TEAM_LEADER')")
    @PatchMapping("/{id}/members/{memberId}/leave")
    public WorkOrderMemberDTO leaveMember(@PathVariable Integer id, @PathVariable Integer memberId) {
        return maintenanceService.leaveMember(id, memberId);
    }
}