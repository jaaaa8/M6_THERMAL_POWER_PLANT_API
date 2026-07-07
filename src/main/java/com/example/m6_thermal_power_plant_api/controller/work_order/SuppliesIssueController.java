package com.example.m6_thermal_power_plant_api.controller.work_order;

import com.example.m6_thermal_power_plant_api.dto.supplies_issue.CreateSuppliesIssueRequest;
import com.example.m6_thermal_power_plant_api.dto.supplies_issue.SuppliesIssueDTO;
import com.example.m6_thermal_power_plant_api.dto.supplies_issue.SuppliesIssueHistoryDTO;
import com.example.m6_thermal_power_plant_api.service.supplies_issue.ISuppliesIssueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * Phiếu cấp vật tư GỘP của MỘT phiếu công tác (nested resource) — 1 phiếu công
 * tác thường cần cả vật tư thay thế lẫn vật tư tiêu hao, nên action tạo phiếu
 * nhận cả 2 loại dòng vật tư trong cùng 1 request.
 *
 * Người cấp phát (issuedBy) lấy từ tài khoản đăng nhập (JWT principal = username),
 * client KHÔNG tự truyền để tránh giả mạo người cấp.
 */
@RestController
@RequestMapping("/api/v1/work-orders/{workOrderId}/supplies-issues")
@RequiredArgsConstructor
public class SuppliesIssueController {

    private final ISuppliesIssueService suppliesIssueService;

    @PostMapping
    public ResponseEntity<SuppliesIssueDTO> create(@PathVariable Integer workOrderId,
                                                    @Valid @RequestBody CreateSuppliesIssueRequest request,
                                                    Principal principal) {
        SuppliesIssueDTO created =
                suppliesIssueService.createForWorkOrder(workOrderId, request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public SuppliesIssueHistoryDTO list(@PathVariable Integer workOrderId) {
        return suppliesIssueService.getByWorkOrder(workOrderId);
    }
}
