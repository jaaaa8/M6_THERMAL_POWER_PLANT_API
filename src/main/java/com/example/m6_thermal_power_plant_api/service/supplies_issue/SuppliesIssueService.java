package com.example.m6_thermal_power_plant_api.service.supplies_issue;

import com.example.m6_thermal_power_plant_api.dto.consumables.ConsumableIssueDTO;
import com.example.m6_thermal_power_plant_api.dto.consumables.CreateConsumableIssueRequest;
import com.example.m6_thermal_power_plant_api.dto.spare_parts.CreateSparePartsIssueRequest;
import com.example.m6_thermal_power_plant_api.dto.spare_parts.SparePartsIssueDTO;
import com.example.m6_thermal_power_plant_api.dto.supplies_issue.CreateSuppliesIssueRequest;
import com.example.m6_thermal_power_plant_api.dto.supplies_issue.SuppliesIssueBatchDTO;
import com.example.m6_thermal_power_plant_api.dto.supplies_issue.SuppliesIssueDTO;
import com.example.m6_thermal_power_plant_api.dto.supplies_issue.SuppliesIssueHistoryDTO;
import com.example.m6_thermal_power_plant_api.entity.Account;
import com.example.m6_thermal_power_plant_api.entity.SuppliesIssue;
import com.example.m6_thermal_power_plant_api.entity.WorkOrder;
import com.example.m6_thermal_power_plant_api.exception.ObjectNotFoundException;
import com.example.m6_thermal_power_plant_api.repository.AccountRepository;
import com.example.m6_thermal_power_plant_api.repository.SuppliesIssueRepository;
import com.example.m6_thermal_power_plant_api.repository.WorkOrderRepository;
import com.example.m6_thermal_power_plant_api.service.consumable.IConsumableIssuesService;
import com.example.m6_thermal_power_plant_api.service.spare_part.ISparePartIssuesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Phiếu cấp vật tư GỘP của một phiếu công tác (PCT) — điều phối 2 nghiệp vụ con
 * (vật tư thay thế / vật tư tiêu hao) trong CÙNG một hành động, vì 1 PCT thường
 * cần cả hai loại vật tư cùng lúc.
 *
 * Từ V9 mỗi hành động tạo còn ghi 1 dòng cha {@link SuppliesIssue} — 2 phiếu con
 * tạo trong cùng lần trỏ về nó, nhờ vậy lịch sử gom được theo "lần cấp #N" và
 * xuất PDF theo từng lần. Vẫn tái sử dụng {@link ISparePartIssuesService} và
 * {@link IConsumableIssuesService}; @Transactional ở đây đảm bảo cha + cả hai
 * phần con được tạo/rollback cùng nhau.
 */
@Service
@RequiredArgsConstructor
public class SuppliesIssueService implements ISuppliesIssueService {

    private final ISparePartIssuesService sparePartIssuesService;
    private final IConsumableIssuesService consumableIssuesService;
    private final SuppliesIssueRepository suppliesIssueRepository;
    private final WorkOrderRepository workOrderRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public SuppliesIssueDTO createForWorkOrder(Integer workOrderId, CreateSuppliesIssueRequest request,
                                               String issuedByUsername) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Khong tim thay phieu cong tac voi id: " + workOrderId));
        Account issuedBy = accountRepository.findAccountByUsername(issuedByUsername)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Khong tim thay tai khoan dang nhap: " + issuedByUsername));

        // Dòng cha của LẦN cấp này — nếu phiếu con bên dưới ném lỗi (VD phiếu công
        // tác đã kết thúc) thì transaction rollback, dòng cha không bị bỏ mồ côi.
        SuppliesIssue batch = suppliesIssueRepository.save(SuppliesIssue.builder()
                .workOrder(workOrder)
                .issuedBy(issuedBy)
                .issuedAt(LocalDateTime.now())
                .build());

        SuppliesIssueDTO.SuppliesIssueDTOBuilder result = SuppliesIssueDTO.builder()
                .id(batch.getId())
                .workOrderId(workOrderId)
                .orderCode(workOrder.getOrderCode())
                .issuedAt(batch.getIssuedAt());

        if (request.getSpareParts() != null && !request.getSpareParts().isEmpty()) {
            CreateSparePartsIssueRequest sparePartsRequest = new CreateSparePartsIssueRequest();
            sparePartsRequest.setItems(request.getSpareParts());
            var sparePartsIssue = sparePartIssuesService.createForWorkOrder(
                    workOrderId, sparePartsRequest, issuedByUsername, batch);
            result.sparePartsIssue(sparePartsIssue);
        }

        if (request.getConsumables() != null && !request.getConsumables().isEmpty()) {
            CreateConsumableIssueRequest consumablesRequest = new CreateConsumableIssueRequest();
            consumablesRequest.setItems(request.getConsumables());
            var consumableIssue = consumableIssuesService.createForWorkOrder(
                    workOrderId, consumablesRequest, issuedByUsername, batch);
            result.consumableIssue(consumableIssue);
        }

        return result.build();
    }

    @Override
    @Transactional(readOnly = true)
    public SuppliesIssueHistoryDTO getByWorkOrder(Integer workOrderId) {
        List<SparePartsIssueDTO> spareParts = sparePartIssuesService.getByWorkOrder(workOrderId);
        List<ConsumableIssueDTO> consumables = consumableIssuesService.getByWorkOrder(workOrderId);
        return SuppliesIssueHistoryDTO.builder()
                .sparePartsIssues(spareParts)
                .consumableIssues(consumables)
                .issues(groupIntoBatches(workOrderId, spareParts, consumables))
                .build();
    }

    /**
     * Gom 2 danh sách phiếu con theo LẦN cấp (supplies_issues), đánh số #1, #2...
     * theo thời gian tăng dần. Phiếu con mồ côi (tạo qua endpoint con cũ / dữ liệu
     * không backfill được) vẫn hiện: mỗi phiếu một nhóm với id = null (không xuất
     * PDF riêng được), chen theo đúng thời gian.
     */
    private List<SuppliesIssueBatchDTO> groupIntoBatches(Integer workOrderId,
                                                         List<SparePartsIssueDTO> spareParts,
                                                         List<ConsumableIssueDTO> consumables) {
        Map<Integer, SparePartsIssueDTO> spByBatch = new HashMap<>();
        List<SparePartsIssueDTO> orphanSp = new ArrayList<>();
        for (SparePartsIssueDTO sp : spareParts) {
            if (sp.getSuppliesIssueId() != null) {
                spByBatch.put(sp.getSuppliesIssueId(), sp);
            } else {
                orphanSp.add(sp);
            }
        }
        Map<Integer, ConsumableIssueDTO> csByBatch = new HashMap<>();
        List<ConsumableIssueDTO> orphanCs = new ArrayList<>();
        for (ConsumableIssueDTO cs : consumables) {
            if (cs.getSuppliesIssueId() != null) {
                csByBatch.put(cs.getSuppliesIssueId(), cs);
            } else {
                orphanCs.add(cs);
            }
        }

        List<SuppliesIssueBatchDTO> result = new ArrayList<>();
        for (SuppliesIssue batch : suppliesIssueRepository.findByWorkOrder_IdOrderByIssuedAtAscIdAsc(workOrderId)) {
            SparePartsIssueDTO sp = spByBatch.remove(batch.getId());
            ConsumableIssueDTO cs = csByBatch.remove(batch.getId());
            if (sp == null && cs == null) {
                continue; // lần cấp rỗng (không xảy ra bình thường) — bỏ qua
            }
            result.add(SuppliesIssueBatchDTO.builder()
                    .id(batch.getId())
                    .issuedAt(batch.getIssuedAt())
                    .issuedById(batch.getIssuedBy() != null ? batch.getIssuedBy().getId() : null)
                    .issuedByName(nameOf(batch.getIssuedBy()))
                    .sparePartsIssue(sp)
                    .consumableIssue(cs)
                    .build());
        }

        for (SparePartsIssueDTO sp : orphanSp) {
            result.add(SuppliesIssueBatchDTO.builder()
                    .issuedAt(sp.getIssuedAt())
                    .issuedById(sp.getIssuedById())
                    .issuedByName(sp.getIssuedByName())
                    .sparePartsIssue(sp)
                    .build());
        }
        for (ConsumableIssueDTO cs : orphanCs) {
            result.add(SuppliesIssueBatchDTO.builder()
                    .issuedAt(cs.getIssuedAt())
                    .issuedById(cs.getIssuedById())
                    .issuedByName(cs.getIssuedByName())
                    .consumableIssue(cs)
                    .build());
        }

        result.sort(Comparator.comparing(SuppliesIssueBatchDTO::getIssuedAt,
                Comparator.nullsFirst(Comparator.naturalOrder())));
        for (int i = 0; i < result.size(); i++) {
            result.get(i).setSeq(i + 1);
        }
        return result;
    }

    private static String nameOf(Account account) {
        if (account == null) {
            return null;
        }
        return account.getEmployee() != null && account.getEmployee().getFullName() != null
                ? account.getEmployee().getFullName()
                : account.getUsername();
    }
}
