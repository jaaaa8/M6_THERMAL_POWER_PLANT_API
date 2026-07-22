package com.example.m6_thermal_power_plant_api.controller.work_order;

import com.example.m6_thermal_power_plant_api.dto.consumables.ConsumableIssueDTO;
import com.example.m6_thermal_power_plant_api.dto.consumables.CreateConsumableIssueRequest;
import com.example.m6_thermal_power_plant_api.service.consumable.IConsumableIssuesService;
import com.example.m6_thermal_power_plant_api.service.pdf.ConsumableIssuePdfService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
 * Phiếu cấp vật tư TIÊU HAO của MỘT phiếu công tác (nested resource)
 *
 * Người cấp phát (issuedBy) lấy từ tài khoản đăng nhập (JWT principal = username),
 * KHÔNG nhận từ client.
 */
@RestController
@RequestMapping("/api/v1/work-orders/{workOrderId}/consumable-issues")
@RequiredArgsConstructor
public class WorkOrderConsumableIssueController {

    private final IConsumableIssuesService consumableIssuesService;
    private final ConsumableIssuePdfService consumableIssuePdfService;

    @PostMapping
    public ResponseEntity<ConsumableIssueDTO> create(@PathVariable Integer workOrderId,
                                                       @Valid @RequestBody CreateConsumableIssueRequest request,
                                                       Principal principal) {
        ConsumableIssueDTO created = consumableIssuesService.createForWorkOrder(
                workOrderId, request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<ConsumableIssueDTO>> list(@PathVariable Integer workOrderId) {
        return ResponseEntity.ok(consumableIssuesService.getByWorkOrder(workOrderId));
    }

    /**
     * Xuất bản in PDF "Phiếu đề nghị cấp phát vật tư, trang thiết bị" theo mẫu
     * giấy. Đồng thời upload lên Cloudinary (đè bản cũ cùng mã phiếu) và lưu URL
     * vào pdf_path — phiếu cấp vật tư bất biến nên bản lưu luôn khớp bản in.
     */
    @GetMapping("/{issueId}/pdf")
    public ResponseEntity<byte[]> exportPdf(@PathVariable Integer workOrderId,
                                            @PathVariable Integer issueId) {
        ConsumableIssuePdfService.ConsumableIssuePdf pdf =
                consumableIssuePdfService.render(workOrderId, issueId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header("Content-Disposition", ContentDisposition.inline()
                        .filename(pdf.issueCode() + ".pdf")
                        .build()
                        .toString())
                .body(pdf.content());
    }
}
