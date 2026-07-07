package com.example.m6_thermal_power_plant_api.service.supplies_issue;

import com.example.m6_thermal_power_plant_api.dto.consumables.CreateConsumableIssueRequest;
import com.example.m6_thermal_power_plant_api.dto.spare_parts.CreateSparePartsIssueRequest;
import com.example.m6_thermal_power_plant_api.dto.supplies_issue.CreateSuppliesIssueRequest;
import com.example.m6_thermal_power_plant_api.dto.supplies_issue.SuppliesIssueDTO;
import com.example.m6_thermal_power_plant_api.dto.supplies_issue.SuppliesIssueHistoryDTO;
import com.example.m6_thermal_power_plant_api.service.consumable.IConsumableIssuesService;
import com.example.m6_thermal_power_plant_api.service.spare_part.ISparePartIssuesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Phiếu cấp vật tư GỘP của một phiếu công tác (PCT) — điều phối 2 nghiệp vụ con
 * (vật tư thay thế / vật tư tiêu hao) trong CÙNG một hành động, vì 1 PCT thường
 * cần cả hai loại vật tư cùng lúc.
 *
 * Vẫn tái sử dụng {@link ISparePartIssuesService} và {@link IConsumableIssuesService}
 * hiện có (không nhân bản logic tạo phiếu) — @Transactional ở đây đảm bảo cả hai
 * phần được tạo/rollback cùng nhau khi request gửi kèm cả 2 loại dòng vật tư.
 */
@Service
@RequiredArgsConstructor
public class SuppliesIssueService implements ISuppliesIssueService {

    private final ISparePartIssuesService sparePartIssuesService;
    private final IConsumableIssuesService consumableIssuesService;

    @Override
    @Transactional
    public SuppliesIssueDTO createForWorkOrder(Integer workOrderId, CreateSuppliesIssueRequest request,
                                               String issuedByUsername) {
        SuppliesIssueDTO.SuppliesIssueDTOBuilder result = SuppliesIssueDTO.builder().workOrderId(workOrderId);

        if (request.getSpareParts() != null && !request.getSpareParts().isEmpty()) {
            CreateSparePartsIssueRequest sparePartsRequest = new CreateSparePartsIssueRequest();
            sparePartsRequest.setItems(request.getSpareParts());
            var sparePartsIssue = sparePartIssuesService.createForWorkOrder(
                    workOrderId, sparePartsRequest, issuedByUsername);
            result.sparePartsIssue(sparePartsIssue).orderCode(sparePartsIssue.getOrderCode());
        }

        if (request.getConsumables() != null && !request.getConsumables().isEmpty()) {
            CreateConsumableIssueRequest consumablesRequest = new CreateConsumableIssueRequest();
            consumablesRequest.setItems(request.getConsumables());
            var consumableIssue = consumableIssuesService.createForWorkOrder(
                    workOrderId, consumablesRequest, issuedByUsername);
            result.consumableIssue(consumableIssue).orderCode(consumableIssue.getOrderCode());
        }

        return result.build();
    }

    @Override
    @Transactional(readOnly = true)
    public SuppliesIssueHistoryDTO getByWorkOrder(Integer workOrderId) {
        return SuppliesIssueHistoryDTO.builder()
                .sparePartsIssues(sparePartIssuesService.getByWorkOrder(workOrderId))
                .consumableIssues(consumableIssuesService.getByWorkOrder(workOrderId))
                .build();
    }
}
