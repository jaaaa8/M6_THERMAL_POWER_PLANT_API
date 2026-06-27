package com.example.m6_thermal_power_plant_api.controller.maintenance;

import com.example.m6_thermal_power_plant_api.dto.maintenance.CreateWorkOrderRequest;
import com.example.m6_thermal_power_plant_api.dto.maintenance.RepairRequestDTO;
import com.example.m6_thermal_power_plant_api.dto.maintenance.WorkOrderDTO;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API cho Quản đốc sửa chữa / Tổ trưởng — Sprint 1 :
 *  - Xem danh sách yêu cầu sửa chữa đang chờ xử lý (phân trang).
 *  - Tạo phiếu công tác (PCT) từ một yêu cầu.
 */
@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceController {

    private final IMaintenanceService maintenanceService;
    private final UniqueCodeRetryExecutor codeRetryExecutor;

    public MaintenanceController(IMaintenanceService maintenanceService,
                                 UniqueCodeRetryExecutor codeRetryExecutor) {
        this.maintenanceService = maintenanceService;
        this.codeRetryExecutor = codeRetryExecutor;
    }

    /**
     * Danh sách request đang chờ xử lý (status = PENDING), CÓ PHÂN TRANG.
     * Tham số query chuẩn của Spring: {@code ?page=0&size=20&sort=createdAt,desc}.
     * Mặc định trang 20 dòng, sắp xếp createdAt giảm dần (mới nhất lên trước).
     *
     * Trả {@link PagedModel} (bọc quanh Page) để JSON phân trang ổn định, tránh
     * cảnh báo "serializing PageImpl is not supported" của Spring Boot 3.x. JSON
     * gồm {@code content[]} + khối {@code page} (size, number, totalElements,
     * totalPages).
     */
    @GetMapping("/repair-requests/pending")
    public PagedModel<RepairRequestDTO> getPendingRepairRequests(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return new PagedModel<>(maintenanceService.getPendingRepairRequests(pageable));
    }

    /**
     * Tạo phiếu công tác từ một yêu cầu sửa chữa.
     *
     * Bọc bằng {@link UniqueCodeRetryExecutor}: vì controller KHÔNG @Transactional
     * nên mỗi lần gọi {@code createWorkOrderFromRequest} (vốn @Transactional) mở
     * một transaction riêng. Nếu orderCode trùng (hiếm) → constraint DB ném lỗi,
     * transaction rollback sạch, executor sinh lại mã + chạy lại toàn bộ thao tác.
     */
    @PostMapping("/work-orders")
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
    @PatchMapping("/work-orders/{id}/cancel")
    public WorkOrderDTO cancelWorkOrder(@PathVariable Integer id) {
        return maintenanceService.cancelWorkOrder(id);
    }
}
