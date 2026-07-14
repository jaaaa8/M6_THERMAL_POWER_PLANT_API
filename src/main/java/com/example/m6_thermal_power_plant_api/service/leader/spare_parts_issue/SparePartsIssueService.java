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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class SparePartsIssueService implements ISparePartsIssueService {
    private final ISparePartsIssueRepository sparePartsIssueRepository;
    private final WorkOrderRepository workOrderRepository;
    private final IAccountRepository accountRepository;
    private final ISparePartRepository sparePartRepository;
    private final ISparePartsIssueDetailRepository sparePartsIssueDetailRepository;


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
//            if (save != null) {
//                // Update the spare part quantity
//                SparePart sparePart = save.getSparePart();
//                Integer newQuantity = sparePart.getQuantity() - save.getQuantity();
//                if (newQuantity < 0) {
//                    throw new RuntimeException("Not enough spare parts in stock");
//                }
//                sparePart.s(newQuantity);
//                sparePartRepository.save(sparePart);
//            }
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
    public SparePartsIssueRequestDto update(SparePartsIssueRequestDto sparePartsIssueRequestDto) {
        SparePartsIssue issue = sparePartsIssueRepository.findById(sparePartsIssueRequestDto.getId())
                .orElseThrow(() -> new RuntimeException("Spare parts issue not found"));
        WorkOrder workOrder = workOrderRepository.findById(sparePartsIssueRequestDto.getWorkOrderId())
                .orElseThrow(() -> new RuntimeException("Work order not found"));
        issue.setWorkOrder(workOrder);
        issue.setIssuedAt(sparePartsIssueRequestDto.getIssuedAt());
        issue.setStatus(SparePartsIssueStatus.valueOf(sparePartsIssueRequestDto.getStatus()));
        Account account = accountRepository.findByUsername(sparePartsIssueRequestDto.getIssuedBy().getUsername())
                .orElseThrow(() -> new RuntimeException("Account not found"));
        issue.setIssuedBy(account);
        List<SparePartsIssueDetail> details = (List<SparePartsIssueDetail>) sparePartsIssueRequestDto.getDetails().stream()
                .map(detailDto -> {
                    SparePart sparePart = sparePartRepository.findById(detailDto.getSparePartId())
                            .orElseThrow(() -> new RuntimeException("Spare part not found"));
                    SparePartsIssueDetail detail = new SparePartsIssueDetail();
                    detail.setIssue(issue);
                    detail.setSparePart(sparePart);
                    detail.setQuantity(detailDto.getQuantity());
                    return detail;
                }).toList();
        issue.setDetails(details);
        sparePartsIssueRepository.save(issue);
        return sparePartsIssueRequestDto;

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
                        detail.getSparePart().getImgPath()
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
                details.add(detailDto);
            }
        }
        dto.setDetails(details);
        return dto;
    }
}
