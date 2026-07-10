package com.example.m6_thermal_power_plant_api.service.leader.spare_parts_issue;

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
    public List<SparePartsIssueRequestDto> findAll() {

        List<SparePartsIssue> sparePartsIssues =
                sparePartsIssueRepository.findAllOrderByStatusAndIssuedAt();

        return sparePartsIssues.stream()
                .map(sparePartsIssue -> new SparePartsIssueRequestDto(
                        sparePartsIssue.getId(),
                        sparePartsIssue.getIssueCode(),
                        sparePartsIssue.getWorkOrder().getId(),
                        sparePartsIssue.getIssuedBy().getUsername(),
                        sparePartsIssue.getIssuedAt(),
                        sparePartsIssue.getAttachmentPath(),
                        sparePartsIssue.getStatus().name(),
                        sparePartsIssue.getDetails().stream()
                                .map(detail -> new SparePartsIssueDetailRequestDto(
                                        detail.getSparePart().getId(),
                                        detail.getQuantity()
                                ))
                                .toList()
                ))
                .toList();
    }

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
                        accountRepository.findByUsername(dto.getIssueUsername())
                                .orElseThrow(() ->
                                        new RuntimeException("Account not found")));

        issue.setIssuedAt(dto.getIssuedAt());

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
        Account account = accountRepository.findByUsername(sparePartsIssueRequestDto.getIssueUsername())
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
                sparePartsIssue.getIssuedBy().getUsername(),
                sparePartsIssue.getIssuedAt(),
                sparePartsIssue.getAttachmentPath(),
                sparePartsIssue.getStatus().name(),
                sparePartsIssue.getDetails().stream().map(detail -> new SparePartsIssueDetailRequestDto(
                        detail.getSparePart().getId(),
                        detail.getQuantity()
                )).toList());
    }

    @Override
    public SparePartsIssueRequestDto upload(
            Integer id,
            MultipartFile[] pdfFiles
    ) {
        SparePartsIssue sparePartsIssue = sparePartsIssueRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy phiếu xuất vật tư"));

        if (pdfFiles == null || pdfFiles.length == 0) {
            throw new RuntimeException("File PDF không được để trống");
        }

        try {
            String uploadDir = System.getProperty("user.dir")
                    + "/src/main/resources/pdf/spare-parts-issue/";

            File directory = new File(uploadDir);

            if (!directory.exists()) {
                directory.mkdirs();
            }

            List<String> filePaths = new ArrayList<>();

            for (MultipartFile file : pdfFiles) {

                if (file.isEmpty()) {
                    continue;
                }

                String originalName = file.getOriginalFilename();

                String extension = originalName.substring(
                        originalName.lastIndexOf(".")
                );

                String fileName = sparePartsIssue.getIssueCode();

                Path targetPath = Paths.get(uploadDir, fileName);

                Files.copy(
                        file.getInputStream(),
                        targetPath,
                        StandardCopyOption.REPLACE_EXISTING
                );

                filePaths.add(
                        "/pdf/spare-parts-issue/" + fileName
                );
            }

            if (!filePaths.isEmpty()) {
                sparePartsIssue.setAttachmentPath(
                        String.join(",", filePaths)
                );
            }

            sparePartsIssue.setStatus(SparePartsIssueStatus.COMPLETED);

            sparePartsIssueRepository.save(sparePartsIssue);

            return convertToDto(sparePartsIssue);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Lỗi upload PDF: " + e.getMessage(),
                    e
            );
        }
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

        if(entity.getIssuedBy() != null){
            dto.setIssueUsername(
                    entity.getIssuedBy().getUsername()
            );
        }

        return dto;
    }
}
