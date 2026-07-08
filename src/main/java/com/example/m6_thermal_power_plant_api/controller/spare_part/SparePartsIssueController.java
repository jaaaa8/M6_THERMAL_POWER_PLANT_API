package com.example.m6_thermal_power_plant_api.controller.spare_part;

import com.example.m6_thermal_power_plant_api.dto.spare_parts.CreateSparePartsIssueRequest;
import com.example.m6_thermal_power_plant_api.dto.spare_parts.SparePartsIssueDTO;
import com.example.m6_thermal_power_plant_api.service.spare_part.ISparePartIssuesService;
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
import java.util.List;

/**
 * Phiếu cấp vật tư thay thế của MỘT phiếu công tác (nested resource).
 *
 * Người cấp phát (issuedBy) lấy từ tài khoản đăng nhập (JWT principal = username),
 * client KHÔNG tự truyền để tránh giả mạo người cấp.
 */
@RestController
@RequestMapping("/api/v1/work-orders/{workOrderId}/spare-parts-issues")
@RequiredArgsConstructor
public class SparePartsIssueController {

    private final ISparePartIssuesService sparePartIssuesService;

    @PostMapping
    public ResponseEntity<SparePartsIssueDTO> create(@PathVariable Integer workOrderId,
                                                     @Valid @RequestBody CreateSparePartsIssueRequest request,
                                                     Principal principal) {
        SparePartsIssueDTO created =
                sparePartIssuesService.createForWorkOrder(workOrderId, request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public List<SparePartsIssueDTO> list(@PathVariable Integer workOrderId) {
        return sparePartIssuesService.getByWorkOrder(workOrderId);
    }
}
