package com.example.m6_thermal_power_plant_api.service.leader.spare_parts_issue;

import com.example.m6_thermal_power_plant_api.dto.Leader.req.SparePartsIssueDetailRequestDto;
import com.example.m6_thermal_power_plant_api.dto.Leader.req.SparePartsIssueRequestDto;
import com.example.m6_thermal_power_plant_api.entity.*;
import com.example.m6_thermal_power_plant_api.repository.ISparePartRepository;
import com.example.m6_thermal_power_plant_api.repository.ISparePartsIssueDetailRepository;
import com.example.m6_thermal_power_plant_api.repository.ISparePartsIssueRepository;
import com.example.m6_thermal_power_plant_api.repository.WorkOrderRepository;
import com.example.m6_thermal_power_plant_api.repository.account.IAccountRepository;
import com.example.m6_thermal_power_plant_api.util.TimeStampCodeGenerator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SparePartsIssueService implements ISparePartsIssueService {
    private final ISparePartsIssueRepository sparePartsIssueRepository;
    private final WorkOrderRepository workOrderRepository;
    private final IAccountRepository accountRepository;
    private final ISparePartRepository sparePartRepository;
    private final ISparePartsIssueDetailRepository sparePartsIssueDetailRepository;

    public SparePartsIssueService(ISparePartsIssueRepository sparePartsIssueRepository,
                                  WorkOrderRepository workOrderRepository,
                                  IAccountRepository accountRepository,
                                  ISparePartRepository sparePartRepository,
                                  ISparePartsIssueDetailRepository sparePartsIssueDetailRepository) {
        this.sparePartsIssueRepository = sparePartsIssueRepository;
        this.workOrderRepository = workOrderRepository;
        this.accountRepository = accountRepository;
        this.sparePartRepository = sparePartRepository;
        this.sparePartsIssueDetailRepository = sparePartsIssueDetailRepository;
    }
    @Override
    public List<SparePartsIssueRequestDto> findAll() {
        List<SparePartsIssue> sparePartsIssues = sparePartsIssueRepository.findAll();
        return sparePartsIssues.stream().map(sparePartsIssue -> new SparePartsIssueRequestDto(
                sparePartsIssue.getId(),
                sparePartsIssue.getSparePartCode(),
                sparePartsIssue.getWorkOrder().getId(),
                sparePartsIssue.getIssuedBy().getId(),
                sparePartsIssue.getIssuedAt(),
                sparePartsIssue.getDetails().stream().map(detail -> new SparePartsIssueDetailRequestDto(
                        detail.getSparePart().getId(),
                        detail.getQuantity()
                )).toList()
        )).toList();
    }

    @Override
    public SparePartsIssueRequestDto save(
            SparePartsIssueRequestDto dto) {

        SparePartsIssue issue = new SparePartsIssue();

        issue.setSparePartCode(
                TimeStampCodeGenerator.generate(SparePartsIssue.class));

        issue.setWorkOrder(
                workOrderRepository.findById(dto.getWorkOrderId())
                        .orElseThrow(() ->
                                new RuntimeException("Work order not found")));

        issue.setIssuedBy(
                accountRepository.findById(dto.getIssuedById())
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

        dto.setIssueCode(issue.getSparePartCode());

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
        Account account = accountRepository.findById(sparePartsIssueRequestDto.getIssuedById())
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
                sparePartsIssue.getSparePartCode(),
                sparePartsIssue.getWorkOrder().getId(),
                sparePartsIssue.getIssuedBy().getId(),
                sparePartsIssue.getIssuedAt(),
                sparePartsIssue.getDetails().stream().map(detail -> new SparePartsIssueDetailRequestDto(
                        detail.getSparePart().getId(),
                        detail.getQuantity()
                )).toList());
    }
}
