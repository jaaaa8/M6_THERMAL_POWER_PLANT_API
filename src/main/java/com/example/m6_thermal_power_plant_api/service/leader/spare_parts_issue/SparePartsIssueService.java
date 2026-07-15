package com.example.m6_thermal_power_plant_api.service.leader.spare_parts_issue;

import com.example.m6_thermal_power_plant_api.dto.Leader.req.AccountDto;
import com.example.m6_thermal_power_plant_api.dto.Leader.req.EmployeeDto;
import com.example.m6_thermal_power_plant_api.dto.Leader.req.SparePartsIssueDetailRequestDto;
import com.example.m6_thermal_power_plant_api.dto.Leader.req.SparePartsIssueRequestDto;
import com.example.m6_thermal_power_plant_api.entity.*;
import com.example.m6_thermal_power_plant_api.entity.enums.SparePartsIssueStatus;
import com.example.m6_thermal_power_plant_api.repository.ISparePartRepository;
import com.example.m6_thermal_power_plant_api.repository.ISparePartsIssueDetailRepository;
import com.example.m6_thermal_power_plant_api.repository.ISparePartsIssueRepository;
import com.example.m6_thermal_power_plant_api.repository.WorkOrderRepository;
import com.example.m6_thermal_power_plant_api.repository.account.IAccountRepository;
import com.example.m6_thermal_power_plant_api.repository.employee.IEmployeeRepository;
import com.example.m6_thermal_power_plant_api.util.TimeStampCodeGenerator;
import com.example.m6_thermal_power_plant_api.dto.file.FileUploadResult;
import com.example.m6_thermal_power_plant_api.entity.enums.TransactionType;
import com.example.m6_thermal_power_plant_api.repository.ISparePartInventoryRepository;
import com.example.m6_thermal_power_plant_api.repository.ISparePartExportRepository;
import com.example.m6_thermal_power_plant_api.service.util.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SparePartsIssueService implements ISparePartsIssueService {
    private final ISparePartsIssueRepository sparePartsIssueRepository;
    private final WorkOrderRepository workOrderRepository;
    private final IAccountRepository accountRepository;
    private final ISparePartRepository sparePartRepository;
    private final ISparePartsIssueDetailRepository sparePartsIssueDetailRepository;
    private final ISparePartInventoryRepository sparePartInventoryRepository;
    private final ISparePartExportRepository sparePartExportRepository;
    private final FileUploadService fileUploadService;


    @Override
    @Transactional
    public SparePartsIssueRequestDto save(
            SparePartsIssueRequestDto dto) {

        SparePartsIssue issue = new SparePartsIssue();

        issue.setIssueCode(
                TimeStampCodeGenerator.generate(SparePartsIssue.class));

        issue.setWorkOrder(
                workOrderRepository.findById(dto.getWorkOrderId())
                        .orElseThrow(() ->
                                new RuntimeException("Work order not found")));

        issue.setIssuedBy(
                        accountRepository.findByUsername(dto.getIssuedBy().getUsername())
                                .orElseThrow(() ->
                                        new RuntimeException("Account not found")));

        issue.setIssuedAt(dto.getIssuedAt());
        issue.setStatus(SparePartsIssueStatus.PENDING);

        List<SparePartsIssueDetail> details =

                dto.getDetails().stream()
                        .map(detailDto -> {

                            SparePart sparePart =
                                    sparePartRepository.findById(
                                                    detailDto.getSparePartId())
                                            .orElseThrow(() ->
                                                    new RuntimeException(
                                                            "Spare part not found"));

                            SparePartsIssueDetail detail =
                                    new SparePartsIssueDetail();

                            detail.setIssue(issue);
                            detail.setSparePart(sparePart);
                            detail.setQuantity(detailDto.getQuantity());

                            return detail;
                        })
                        .toList();

        issue.setDetails(details);
        sparePartsIssueRepository.save(issue);

        for(SparePartsIssueDetail detail : details) {
            SparePartsIssueDetail save = sparePartsIssueDetailRepository.save(detail);
        }

        dto.setIssueCode(issue.getIssueCode());
        dto.setStatus(issue.getStatus().name());
        dto.setIssuedBy(
                new AccountDto(
                        issue.getIssuedBy().getUsername(),
                        issue.getIssuedBy().getEmail(),
                        new EmployeeDto(
                                issue.getIssuedBy().getEmployee().getId(),
                                issue.getIssuedBy().getEmployee().getEmployeeCode(),
                                issue.getIssuedBy().getEmployee().getFullName()
                        )
                )
        );

        return dto;
    }

    @Override
    @Transactional
    public SparePartsIssueRequestDto update(SparePartsIssueRequestDto sparePartsIssueRequestDto) {
        SparePartsIssue issue = sparePartsIssueRepository.findById(sparePartsIssueRequestDto.getId())
                .orElseThrow(() -> new RuntimeException("Spare parts issue not found"));
        WorkOrder workOrder = workOrderRepository.findById(sparePartsIssueRequestDto.getWorkOrderId())
                .orElseThrow(() -> new RuntimeException("Work order not found"));
        
        SparePartsIssueStatus newStatus = SparePartsIssueStatus.valueOf(sparePartsIssueRequestDto.getStatus());
        SparePartsIssueStatus oldStatus = issue.getStatus();

        Account account = accountRepository.findByUsername(sparePartsIssueRequestDto.getIssuedBy().getUsername())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (newStatus == SparePartsIssueStatus.COMPLETED && oldStatus != SparePartsIssueStatus.COMPLETED) {
            handleCompletion(issue, account);
        }

        //Cập nhật các trường thông tin của issues
        updateIssueFields(issue, workOrder, sparePartsIssueRequestDto, account, newStatus);

        // 4. Cập nhật details nếu danh sách có thay đổi
        updateDetailsIfChanged(issue, sparePartsIssueRequestDto, newStatus, oldStatus);

        sparePartsIssueRepository.save(issue);
        return convertToDto(issue);
    }

    @Override
    public SparePartsIssueRequestDto findById(Integer id) {
        SparePartsIssue sparePartsIssue = sparePartsIssueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Spare parts issue not found"));
        return new SparePartsIssueRequestDto(
                sparePartsIssue.getId(),
                sparePartsIssue.getIssueCode(),
                sparePartsIssue.getWorkOrder().getId(),
                new AccountDto(
                        sparePartsIssue.getIssuedBy().getUsername(),
                        sparePartsIssue.getIssuedBy().getEmail(),
                        new EmployeeDto(
                                sparePartsIssue.getIssuedBy().getEmployee().getId(),
                                sparePartsIssue.getIssuedBy().getEmployee().getEmployeeCode(),
                                sparePartsIssue.getIssuedBy().getEmployee().getFullName()
                        )
                ),
                sparePartsIssue.getIssuedAt(),
                sparePartsIssue.getAttachmentPath(),
                sparePartsIssue.getStatus().name(),
                sparePartsIssue.getDetails().stream().map(detail -> new SparePartsIssueDetailRequestDto(
                        detail.getSparePart().getId(),
                        detail.getSparePart().getSparePartCode(),
                        detail.getSparePart().getName(),
                        detail.getQuantity(),
                        detail.getSparePart().getUnit().getName(),
                        detail.getSparePart().getImgPath(),
                        sparePartRepository.getStockQuantity(detail.getSparePart().getId())
                )).toList());
    }

    @Override
    public Page<SparePartsIssueRequestDto> search(
            String keyword,
            SparePartsIssueStatus status,
            Pageable pageable
    ) {

        Page<SparePartsIssue> page =
                sparePartsIssueRepository.search(
                        keyword,
                        status,
                        pageable
                );

        return page.map(this::convertToDto);
    }

    private SparePartsIssueRequestDto convertToDto(
            SparePartsIssue entity
    ) {
        SparePartsIssueRequestDto dto =
                new SparePartsIssueRequestDto();

        dto.setId(entity.getId());
        dto.setIssueCode(entity.getIssueCode());
        dto.setAttachmentPath(entity.getAttachmentPath());
        dto.setIssuedAt(entity.getIssuedAt());
        dto.setWorkOrderId(entity.getWorkOrder().getId());
        dto.setStatus(entity.getStatus().name());

        if (entity.getIssuedBy() != null) {
            dto.setIssuedBy(
                    new AccountDto(
                            entity.getIssuedBy().getUsername(),
                            entity.getIssuedBy().getEmail(),
                            new EmployeeDto(
                                    entity.getIssuedBy().getEmployee().getId(),
                                    entity.getIssuedBy().getEmployee().getEmployeeCode(),
                                    entity.getIssuedBy().getEmployee().getFullName()
                            )
                    )
            );
        }

        List<SparePartsIssueDetailRequestDto> details = new ArrayList<>();
        if (entity.getDetails() != null) {
            for (SparePartsIssueDetail detail : entity.getDetails()) {
                SparePartsIssueDetailRequestDto detailDto = new SparePartsIssueDetailRequestDto();
                detailDto.setSparePartId(detail.getSparePart().getId());
                detailDto.setSparePartCode(detail.getSparePart().getSparePartCode());
                detailDto.setSparePartName(detail.getSparePart().getName());
                detailDto.setQuantity(detail.getQuantity());
                detailDto.setUnit(detail.getSparePart().getUnit().getName());
                detailDto.setImgPath(detail.getSparePart().getImgPath());
                detailDto.setCurrentStock(sparePartRepository.getStockQuantity(detail.getSparePart().getId()));
                details.add(detailDto);
            }
        }
        dto.setDetails(details);
        return dto;
    }

    @Override
    @Transactional
    public SparePartsIssueRequestDto uploadSignedPdf(Integer id, MultipartFile file) {
        SparePartsIssue issue = sparePartsIssueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu xuất vật tư thay thế với id: " + id));

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File PDF không được rỗng");
        }

        try {
            FileUploadResult uploadResult = fileUploadService.uploadPdf(
                    file.getBytes(),
                    "spare-parts-issues",
                    issue.getIssueCode()
            );
            issue.setAttachmentPath(uploadResult.secureUrl());
            sparePartsIssueRepository.save(issue);
        } catch (Exception e) {
            throw new RuntimeException("Tải lên file PDF thất bại: " + e.getMessage(), e);
        }

        return convertToDto(issue);
    }

    private void handleCompletion(SparePartsIssue issue, Account account) {
        validateAttachment(issue);
        validateStock(issue);
        exportSpareParts(issue, account);
    }

    private void validateAttachment(SparePartsIssue issue) {
        if (issue.getAttachmentPath() == null || issue.getAttachmentPath().isBlank()) {
            throw new IllegalStateException("Yêu cầu tải lên phiếu cấp vật tư bản cứng (file PDF) đã có chữ ký của thủ kho trước khi hoàn thành.");
        }
    }
    private void validateStock(SparePartsIssue issue) {
        for (SparePartsIssueDetail detail : issue.getDetails()) {
            BigDecimal stock = sparePartRepository.getStockQuantity(detail.getSparePart().getId());
            BigDecimal reqQty = BigDecimal.valueOf(detail.getQuantity());
            if (stock.compareTo(reqQty) < 0) {
                throw new IllegalStateException("Không đủ số lượng tồn kho cho vật tư: "
                        + detail.getSparePart().getName() + " (Yêu cầu: " + reqQty + ", Tồn hiện tại: " + stock + ")");
            }
        }
    }
    private void exportSpareParts(SparePartsIssue issue, Account account) {
        Equipment equipment = null;
        if (issue.getWorkOrder() != null
                && issue.getWorkOrder().getRepairRequest() != null
                && issue.getWorkOrder().getRepairRequest().getEquipment() != null) {
            equipment = issue.getWorkOrder().getRepairRequest().getEquipment();
        }
        for (SparePartsIssueDetail detail : issue.getDetails()) {
            BigDecimal reqQty = BigDecimal.valueOf(detail.getQuantity());
            createInventoryLedgerEntry(detail, account, reqQty);
            createExportRecord(issue, detail, account, equipment, reqQty);
        }
    }

    private void createInventoryLedgerEntry(SparePartsIssueDetail detail, Account account, BigDecimal reqQty) {
        SparePartsInventory inventory = SparePartsInventory.builder()
                .sparePart(detail.getSparePart())
                .account(account)
                .quantity(reqQty)
                .transactionType(TransactionType.EXPORT)
                .transactionDate(LocalDateTime.now())
                .build();
        sparePartInventoryRepository.save(inventory);
    }
    private void createExportRecord(SparePartsIssue issue, SparePartsIssueDetail detail, Account account, Equipment equipment, BigDecimal reqQty) {
        SparePartExport export = SparePartExport.builder()
                .exportCode(TimeStampCodeGenerator.generate(SparePartExport.class))
                .sparePartsIssue(issue)
                .sparePart(detail.getSparePart())
                .requestedQuantity(reqQty)
                .actualQuantity(reqQty)
                .equipment(equipment)
                .exportedBy(account)
                .exportedAt(LocalDateTime.now())
                .status("COMPLETED")
                .build();
        sparePartExportRepository.save(export);
    }
    private void updateIssueFields(SparePartsIssue issue, WorkOrder workOrder, SparePartsIssueRequestDto dto, Account account, SparePartsIssueStatus newStatus) {
        issue.setWorkOrder(workOrder);
        issue.setIssuedAt(dto.getIssuedAt());
        issue.setStatus(newStatus);
        issue.setIssuedBy(account);
    }
    private void updateDetailsIfChanged(SparePartsIssue issue, SparePartsIssueRequestDto dto, SparePartsIssueStatus newStatus, SparePartsIssueStatus oldStatus) {
        boolean statusChangeOnly = (newStatus != oldStatus) &&
                dto.getDetails() != null &&
                issue.getDetails() != null &&
                dto.getDetails().size() == issue.getDetails().size();

        if (!statusChangeOnly) {
            softDeleteOldDetails(issue);
            createNewDetails(issue, dto);
        }
    }
    private void softDeleteOldDetails(SparePartsIssue issue) {
        if (issue.getDetails() != null && !issue.getDetails().isEmpty()) {
            for (SparePartsIssueDetail oldDetail : issue.getDetails()) {
                oldDetail.softDelete();
            }
            sparePartsIssueDetailRepository.saveAll(issue.getDetails());
        }
    }
    private void createNewDetails(SparePartsIssue issue, SparePartsIssueRequestDto dto) {
        List<SparePartsIssueDetail> details = dto.getDetails().stream()
                .map(detailDto -> {
                    SparePart sparePart = sparePartRepository.findById(detailDto.getSparePartId())
                            .orElseThrow(() -> new RuntimeException("Spare part not found"));
                    SparePartsIssueDetail detail = new SparePartsIssueDetail();
                    detail.setIssue(issue);
                    detail.setSparePart(sparePart);
                    detail.setQuantity(detailDto.getQuantity());
                    return detail;
                }).toList();
        sparePartsIssueDetailRepository.saveAll(details);
        issue.setDetails(details);
    }
}
