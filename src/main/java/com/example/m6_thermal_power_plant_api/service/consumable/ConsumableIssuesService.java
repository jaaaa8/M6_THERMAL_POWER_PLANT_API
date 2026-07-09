package com.example.m6_thermal_power_plant_api.service.consumable;

import com.example.m6_thermal_power_plant_api.dto.consumables.ConsumableIssueDTO;
import com.example.m6_thermal_power_plant_api.dto.consumables.CreateConsumableIssueRequest;
import com.example.m6_thermal_power_plant_api.entity.Account;
import com.example.m6_thermal_power_plant_api.entity.Consumable;
import com.example.m6_thermal_power_plant_api.entity.ConsumableIssue;
import com.example.m6_thermal_power_plant_api.entity.ConsumableIssueDetail;
import com.example.m6_thermal_power_plant_api.entity.SuppliesIssue;
import com.example.m6_thermal_power_plant_api.entity.WorkOrder;
import com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderStatus;
import com.example.m6_thermal_power_plant_api.exception.ObjectNotFoundException;
import com.example.m6_thermal_power_plant_api.repository.AccountRepository;
import com.example.m6_thermal_power_plant_api.repository.IConsumableIssueDetailRepository;
import com.example.m6_thermal_power_plant_api.repository.IConsumableIssueRepository;
import com.example.m6_thermal_power_plant_api.repository.IConsumableRepository;
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
 * Phiếu cấp vật tư TIÊU HAO gắn với phiếu công tác (PCT).
 *
 * Phiếu chỉ là YÊU CẦU cấp vật tư — tạo phiếu KHÔNG trừ tồn kho. Xuất kho thật
 * (ConsumableExport + giao dịch EXPORT trong consumable_inventory) là bước sau
 * do thủ kho thực hiện — vì vậy KHÔNG kiểm tra tồn kho ở đây.
 */
@Service
@RequiredArgsConstructor
public class ConsumableIssuesService implements IConsumableIssuesService {

    private final IConsumableIssueRepository issueRepository;
    private final IConsumableIssueDetailRepository detailRepository;
    private final IConsumableRepository consumableRepository;
    private final WorkOrderRepository workOrderRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public ConsumableIssueDTO createForWorkOrder(Integer workOrderId, CreateConsumableIssueRequest request,
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
                .map(CreateConsumableIssueRequest.Line::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ConsumableIssue issue = issueRepository.save(ConsumableIssue.builder()
                .consumableCode(TimeStampCodeGenerator.generate(ConsumableIssue.class))
                .workOrder(workOrder)
                .suppliesIssue(suppliesIssue)
                .transactionType("export")
                .quantity(total)
                .issuedBy(issuedBy)
                .issuedAt(now)
                .build());

        List<ConsumableIssueDetail> details = new ArrayList<>();
        for (CreateConsumableIssueRequest.Line line : request.getItems()) {
            Consumable consumable = consumableRepository.findById(line.getConsumableId())
                    .orElseThrow(() -> new ObjectNotFoundException(
                            "Khong tim thay vat tu tieu hao voi id: " + line.getConsumableId()));
            details.add(detailRepository.save(ConsumableIssueDetail.builder()
                    .issue(issue)
                    .consumable(consumable)
                    .quantity(line.getQuantity())
                    .build()));
        }

        return ConsumableIssueDTO.from(issue, details);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsumableIssueDTO> getByWorkOrder(Integer workOrderId) {
        if (!workOrderRepository.existsById(workOrderId)) {
            throw new ObjectNotFoundException("Khong tim thay phieu cong tac voi id: " + workOrderId);
        }
        return issueRepository.findByWorkOrder_IdOrderByIssuedAtDesc(workOrderId).stream()
                .map(issue -> ConsumableIssueDTO.from(issue, detailRepository.findByIssue_Id(issue.getId())))
                .toList();
    }
}
