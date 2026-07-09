package com.example.m6_thermal_power_plant_api.service.spare_part;

import com.example.m6_thermal_power_plant_api.dto.spare_parts.CreateSparePartsIssueRequest;
import com.example.m6_thermal_power_plant_api.dto.spare_parts.SparePartsIssueDTO;
import com.example.m6_thermal_power_plant_api.entity.Account;
import com.example.m6_thermal_power_plant_api.entity.SparePart;
import com.example.m6_thermal_power_plant_api.entity.SparePartsIssue;
import com.example.m6_thermal_power_plant_api.entity.SparePartsIssueDetail;
import com.example.m6_thermal_power_plant_api.entity.SuppliesIssue;
import com.example.m6_thermal_power_plant_api.entity.WorkOrder;
import com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderStatus;
import com.example.m6_thermal_power_plant_api.exception.ObjectNotFoundException;
import com.example.m6_thermal_power_plant_api.repository.AccountRepository;
import com.example.m6_thermal_power_plant_api.repository.ISparePartRepository;
import com.example.m6_thermal_power_plant_api.repository.ISparePartsIssueDetailRepository;
import com.example.m6_thermal_power_plant_api.repository.ISparePartsIssueRepository;
import com.example.m6_thermal_power_plant_api.repository.WorkOrderRepository;
import com.example.m6_thermal_power_plant_api.util.TimeStampCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Phiếu cấp vật tư THAY THẾ gắn với phiếu công tác (PCT).
 *
 * Phiếu chỉ là YÊU CẦU cấp vật tư — tạo phiếu KHÔNG trừ tồn kho. Xuất kho thật
 * (SparePartExport + giao dịch EXPORT trong spare_parts_inventory) là bước sau
 * do thủ kho thực hiện — vì vậy KHÔNG kiểm tra tồn kho ở đây.
 */
@Service
@RequiredArgsConstructor
public class SparePartIssuesService implements ISparePartIssuesService {

    private final ISparePartsIssueRepository issueRepository;
    private final ISparePartsIssueDetailRepository detailRepository;
    private final ISparePartRepository sparePartRepository;
    private final WorkOrderRepository workOrderRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public SparePartsIssueDTO createForWorkOrder(Integer workOrderId, CreateSparePartsIssueRequest request,
                                                 String issuedByUsername, SuppliesIssue suppliesIssue) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Khong tim thay phieu cong tac voi id: " + workOrderId));

        // Chỉ cấp vật tư cho phiếu đang "sống" — phiếu đã huỷ/hoàn thành không nhận thêm vật tư.
        if (workOrder.getStatus() == WorkOrderStatus.CANCELLED
                || workOrder.getStatus() == WorkOrderStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Phieu cong tac (" + workOrder.getOrderCode() + ") da " + workOrder.getStatus()
                            + " — khong the tao phieu cap vat tu.");
        }

        Account issuedBy = accountRepository.findAccountByUsername(issuedByUsername)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Khong tim thay tai khoan dang nhap: " + issuedByUsername));

        LocalDateTime now = LocalDateTime.now();
        BigDecimal total = request.getItems().stream()
                .map(CreateSparePartsIssueRequest.Line::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        SparePartsIssue issue = issueRepository.save(SparePartsIssue.builder()
                .sparePartCode(TimeStampCodeGenerator.generate(SparePartsIssue.class))
                .workOrder(workOrder)
                .suppliesIssue(suppliesIssue)
                .transactionType("export")
                .quantity(total)
                .issuedBy(issuedBy)
                .issuedAt(now)
                .build());

        List<SparePartsIssueDetail> details = new ArrayList<>();
        for (CreateSparePartsIssueRequest.Line line : request.getItems()) {
            SparePart sparePart = sparePartRepository.findById(line.getSparePartId())
                    .orElseThrow(() -> new ObjectNotFoundException(
                            "Khong tim thay vat tu thay the voi id: " + line.getSparePartId()));
            details.add(detailRepository.save(SparePartsIssueDetail.builder()
                    .issue(issue)
                    .sparePart(sparePart)
                    .quantity(line.getQuantity())
                    .build()));
        }

        return SparePartsIssueDTO.from(issue, details);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SparePartsIssueDTO> getByWorkOrder(Integer workOrderId) {
        if (!workOrderRepository.existsById(workOrderId)) {
            throw new ObjectNotFoundException("Khong tim thay phieu cong tac voi id: " + workOrderId);
        }
        return issueRepository.findByWorkOrder_IdOrderByIssuedAtDesc(workOrderId).stream()
                .map(issue -> SparePartsIssueDTO.from(issue, detailRepository.findByIssue_Id(issue.getId())))
                .toList();
    }
}
