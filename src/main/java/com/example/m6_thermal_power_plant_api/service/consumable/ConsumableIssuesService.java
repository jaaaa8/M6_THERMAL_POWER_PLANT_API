package com.example.m6_thermal_power_plant_api.service.consumable;

import com.example.m6_thermal_power_plant_api.dto.consumables.ConsumableIssueDTO;
import com.example.m6_thermal_power_plant_api.dto.consumables.CreateConsumableIssueRequest;
import com.example.m6_thermal_power_plant_api.entity.Account;
import com.example.m6_thermal_power_plant_api.entity.Consumable;
import com.example.m6_thermal_power_plant_api.entity.ConsumableIssue;
import com.example.m6_thermal_power_plant_api.entity.ConsumableIssueDetail;
import com.example.m6_thermal_power_plant_api.entity.ConsumableInventory;
import com.example.m6_thermal_power_plant_api.entity.ConsumableExport;
import com.example.m6_thermal_power_plant_api.entity.Equipment;
import com.example.m6_thermal_power_plant_api.entity.WorkOrder;
import com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderStatus;
import com.example.m6_thermal_power_plant_api.entity.enums.ConsumableIssueStatus;
import com.example.m6_thermal_power_plant_api.entity.enums.TransactionType;
import com.example.m6_thermal_power_plant_api.exception.ObjectNotFoundException;
import com.example.m6_thermal_power_plant_api.repository.AccountRepository;
import com.example.m6_thermal_power_plant_api.repository.IConsumableIssueDetailRepository;
import com.example.m6_thermal_power_plant_api.repository.IConsumableIssueRepository;
import com.example.m6_thermal_power_plant_api.repository.IConsumableRepository;
import com.example.m6_thermal_power_plant_api.repository.IConsumableInventoryRepository;
import com.example.m6_thermal_power_plant_api.repository.IConsumableExportRepository;
import com.example.m6_thermal_power_plant_api.repository.WorkOrderRepository;
import com.example.m6_thermal_power_plant_api.util.TimeStampCodeGenerator;
import com.example.m6_thermal_power_plant_api.dto.file.FileUploadResult;
import com.example.m6_thermal_power_plant_api.service.util.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
@Transactional
public class ConsumableIssuesService implements IConsumableIssuesService {

    private final IConsumableIssueRepository issueRepository;
    private final IConsumableIssueDetailRepository detailRepository;
    private final IConsumableRepository consumableRepository;
    private final WorkOrderRepository workOrderRepository;
    private final AccountRepository accountRepository;
    private final IConsumableInventoryRepository consumableInventoryRepository;
    private final IConsumableExportRepository consumableExportRepository;
    private final FileUploadService fileUploadService;

    @Override
    @Transactional
    public ConsumableIssueDTO createForWorkOrder(Integer workOrderId, CreateConsumableIssueRequest request,
                                                  String issuedByUsername) {
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

        return mapToDto(issue);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsumableIssueDTO> getByWorkOrder(Integer workOrderId) {
        if (!workOrderRepository.existsById(workOrderId)) {
            throw new ObjectNotFoundException("Khong tim thay phieu cong tac voi id: " + workOrderId);
        }
        return issueRepository.findByWorkOrder_IdOrderByIssuedAtDesc(workOrderId).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConsumableIssueDTO> search(String keyword, ConsumableIssueStatus status, Pageable pageable) {
        Page<ConsumableIssue> page = issueRepository.search(keyword, status, pageable);
        return page.map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ConsumableIssueDTO findById(Integer id) {
        ConsumableIssue issue = issueRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Không tìm thấy phiếu yêu cầu cấp vật tư tiêu hao với id: " + id));
        return mapToDto(issue);
    }

    @Override
    @Transactional
    public ConsumableIssueDTO update(ConsumableIssueDTO dto) {
        ConsumableIssue issue = issueRepository.findById(dto.getId())
                .orElseThrow(() -> new ObjectNotFoundException("Không tìm thấy phiếu yêu cầu cấp vật tư tiêu hao với id: " + dto.getId()));

        ConsumableIssueStatus newStatus = ConsumableIssueStatus.valueOf(dto.getStatus());
        ConsumableIssueStatus oldStatus = issue.getStatus();

        if (newStatus == ConsumableIssueStatus.REJECTED) {
            throw new IllegalArgumentException("Thủ kho vật tư không có quyền từ chối cấp phát vật tư.");
        }

        Account account = accountRepository.findAccountByUsername(dto.getIssuedByName())
                .orElseThrow(() -> new ObjectNotFoundException("Không tìm thấy tài khoản: " + dto.getIssuedByName()));

        if (newStatus == ConsumableIssueStatus.COMPLETED && oldStatus != ConsumableIssueStatus.COMPLETED) {
            handleCompletion(issue, account);
        }

        issue.setStatus(newStatus);
        issue.setIssuedBy(account);

        ConsumableIssue saved = issueRepository.save(issue);
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public ConsumableIssueDTO uploadSignedPdf(Integer id, MultipartFile file) {
        ConsumableIssue issue = issueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu xuất vật tư tiêu hao với id: " + id));

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File PDF không được rỗng");
        }

        try {
            FileUploadResult uploadResult = fileUploadService.uploadPdf(
                    file.getBytes(),
                    "consumable-issues",
                    issue.getConsumableCode()
            );
            issue.setAttachmentPath(uploadResult.secureUrl());
            ConsumableIssue saved = issueRepository.save(issue);
            return mapToDto(saved);
        } catch (Exception e) {
            throw new RuntimeException("Tải lên file PDF thất bại: " + e.getMessage(), e);
        }
    }

    private ConsumableIssueDTO mapToDto(ConsumableIssue issue) {
        List<ConsumableIssueDetail> details = detailRepository.findByIssue_Id(issue.getId());
        ConsumableIssueDTO dto = ConsumableIssueDTO.from(issue, details);
        if (dto.getDetails() != null) {
            for (ConsumableIssueDTO.LineDTO line : dto.getDetails()) {
                line.setCurrentStock(consumableRepository.getStockQuantity(line.getConsumableId()));
            }
        }
        return dto;
    }

    private void handleCompletion(ConsumableIssue issue, Account account) {
        validateAttachment(issue);
        validateStock(issue);
        exportConsumables(issue, account);
    }

    private void validateAttachment(ConsumableIssue issue) {
        if (issue.getAttachmentPath() == null || issue.getAttachmentPath().isBlank()) {
            throw new IllegalStateException("Yêu cầu tải lên phiếu cấp vật tư bản cứng (file PDF) đã có chữ ký của thủ kho trước khi hoàn thành.");
        }
    }

    private void validateStock(ConsumableIssue issue) {
        List<ConsumableIssueDetail> details = detailRepository.findByIssue_Id(issue.getId());
        for (ConsumableIssueDetail detail : details) {
            BigDecimal stock = consumableRepository.getStockQuantity(detail.getConsumable().getId());
            BigDecimal reqQty = detail.getQuantity();
            if (stock.compareTo(reqQty) < 0) {
                throw new IllegalStateException("Không đủ số lượng tồn kho cho vật tư tiêu hao: "
                        + detail.getConsumable().getName() + " (Yêu cầu: " + reqQty + ", Tồn hiện tại: " + stock + ")");
            }
        }
    }

    private void exportConsumables(ConsumableIssue issue, Account account) {
        Equipment equipment = null;
        if (issue.getWorkOrder() != null
                && issue.getWorkOrder().getRepairRequest() != null
                && issue.getWorkOrder().getRepairRequest().getEquipment() != null) {
            equipment = issue.getWorkOrder().getRepairRequest().getEquipment();
        }
        List<ConsumableIssueDetail> details = detailRepository.findByIssue_Id(issue.getId());
        for (ConsumableIssueDetail detail : details) {
            BigDecimal reqQty = detail.getQuantity();
            createInventoryLedgerEntry(detail, account, reqQty);
            createExportRecord(issue, detail, account, equipment, reqQty);
        }
    }

    private void createInventoryLedgerEntry(ConsumableIssueDetail detail, Account account, BigDecimal reqQty) {
        ConsumableInventory inventory = ConsumableInventory.builder()
                .consumable(detail.getConsumable())
                .account(account)
                .quantity(reqQty)
                .transactionType(TransactionType.EXPORT)
                .transactionDate(LocalDateTime.now())
                .build();
        consumableInventoryRepository.save(inventory);
    }

    private void createExportRecord(ConsumableIssue issue, ConsumableIssueDetail detail, Account account, Equipment equipment, BigDecimal reqQty) {
        ConsumableExport export = ConsumableExport.builder()
                .exportCode(TimeStampCodeGenerator.generate(ConsumableExport.class))
                .consumableIssue(issue)
                .consumable(detail.getConsumable())
                .requestedQuantity(reqQty)
                .actualQuantity(reqQty)
                .equipment(equipment)
                .exportedBy(account)
                .exportedAt(LocalDateTime.now())
                .status("COMPLETED")
                .build();
        consumableExportRepository.save(export);
    }
}
