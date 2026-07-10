package com.example.m6_thermal_power_plant_api.service.leader.repair_history;

import com.example.m6_thermal_power_plant_api.dto.Leader.req.RepairHistoryCreateRequestDto;
import com.example.m6_thermal_power_plant_api.dto.Leader.res.RepairHistoryDetailResponseDto;
import com.example.m6_thermal_power_plant_api.dto.Leader.res.RepairHistoryResponseDto;
import com.example.m6_thermal_power_plant_api.entity.*;
import com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderStatus;
import com.example.m6_thermal_power_plant_api.repository.IRepairHistoryRepository;
import com.example.m6_thermal_power_plant_api.repository.ISparePartRepository;
import com.example.m6_thermal_power_plant_api.repository.WorkOrderRepository;
import com.example.m6_thermal_power_plant_api.repository.equipment.IEquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class RepairHistoryService implements IRepairHistoryService {
    private final IRepairHistoryRepository repairHistoryRepository;
    private final WorkOrderRepository workOrderRepository;
    private final IEquipmentRepository equipmentRepository;
    private final ISparePartRepository sparePartRepository;

    @Override
    public RepairHistoryResponseDto create(
            RepairHistoryCreateRequestDto dto
    ) {

        WorkOrder workOrder =
                workOrderRepository.findById(dto.getWorkOrderId())
                        .orElseThrow(() ->
                                new RuntimeException("Work order not found"));

        Equipment equipment =
                equipmentRepository.findById(dto.getEquipmentId())
                        .orElseThrow(() ->
                                new RuntimeException("Equipment not found"));

        RepairHistory history = new RepairHistory();

        history.setWorkOrder(workOrder);
        history.setEquipment(equipment);
        history.setRepairDate(dto.getRepairDate());
        history.setRepairContent(dto.getRepairContent());
        history.setRepairResult(dto.getRepairResult());

        List<RepairHistoryDetail> detailList = dto.getDetails()
                .stream()
                .map(item -> {

                    SparePart sparePart =
                            sparePartRepository
                                    .findById(item.getSparePartId())
                                    .orElseThrow(() ->
                                            new RuntimeException("Spare part not found"));

                    RepairHistoryDetail detail =
                            new RepairHistoryDetail();

                    detail.setRepairHistory(history);
                    detail.setSparePart(sparePart);
                    detail.setQuantity(item.getQuantity());

                    return detail;
                })
                .toList();

        history.setDetails(detailList);

        RepairHistory saved =
                repairHistoryRepository.save(history);

        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepairHistoryResponseDto> findAll() {

        return repairHistoryRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RepairHistoryResponseDto findById(Integer id) {

        RepairHistory history =
                repairHistoryRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException("Repair history not found"));

        return mapToDto(history);
    }

    private RepairHistoryResponseDto mapToDto(
            RepairHistory history
    ) {

        RepairHistoryResponseDto dto =
                new RepairHistoryResponseDto();

        dto.setId(history.getId());

        dto.setWorkOrderId(
                history.getWorkOrder().getId()
        );

        dto.setOrderCode(
                history.getWorkOrder().getOrderCode()
        );

        dto.setLeaderName(
                history.getWorkOrder()
                        .getLeader()
                        .getFullName()
        );

        dto.setEquipmentId(
                history.getEquipment().getId()
        );

        dto.setKksCode(
                history.getEquipment().getKksCode()
        );

        dto.setEquipmentName(
                history.getEquipment().getName()
        );

        dto.setEquipmentImg(
                history.getEquipment().getImgPath()
        );

        dto.setRepairDate(
                history.getRepairDate()
        );

        dto.setRepairContent(
                history.getRepairContent()
        );

        dto.setRepairResult(
                history.getRepairResult()
        );

        dto.setDetails(
                history.getDetails()
                        .stream()
                        .map(detail -> {

                            RepairHistoryDetailResponseDto d =
                                    new RepairHistoryDetailResponseDto();

                            d.setId(detail.getId());

                            d.setSparePartId(
                                    detail.getSparePart().getId()
                            );

                            d.setSparePartCode(
                                    detail.getSparePart()
                                            .getSparePartCode()
                            );

                            d.setSparePartName(
                                    detail.getSparePart()
                                            .getName()
                            );

                            d.setImgPath(
                                    detail.getSparePart()
                                            .getImgPath()
                            );

                            if (detail.getSparePart().getUnit() != null) {

                                d.setUnitName(
                                        detail.getSparePart()
                                                .getUnit()
                                                .getName()
                                );
                            }

                            d.setQuantity(
                                    detail.getQuantity()
                            );

                            return d;

                        })
                        .toList()
        );

        return dto;
    }

    public void createRepairHistory(
            WorkOrder workOrder
    ) {

        if (repairHistoryRepository
                .existsByWorkOrderId(workOrder.getId())) {
        }

        RepairHistory history =
                new RepairHistory();

        history.setWorkOrder(workOrder);

        history.setEquipment(
                workOrder.getRepairRequest()
                        .getEquipment()
        );

        history.setRepairDate(
                LocalDate.now()
        );

        history.setRepairContent(
                workOrder.getRepairDescription()
        );

        history.setRepairResult(
                "Hoàn thành sửa chữa"
        );

        List<RepairHistoryDetail> details =
                new ArrayList<>();

        for (SparePartsIssue issue :
                workOrder.getSparePartsIssues()) {

            for (SparePartsIssueDetail detail :
                    issue.getDetails()) {

                RepairHistoryDetail historyDetail =
                        new RepairHistoryDetail();

                historyDetail.setRepairHistory(history);

                historyDetail.setSparePart(
                        detail.getSparePart()
                );

                historyDetail.setQuantity(
                        detail.getQuantity()
                );

                details.add(historyDetail);
            }
        }

        history.setDetails(details);

        repairHistoryRepository.save(history);
    }

    @Transactional
    public WorkOrder complete(Integer id){

        WorkOrder workOrder =
                workOrderRepository.findById(id)
                        .orElseThrow();

        workOrder.setStatus(
                WorkOrderStatus.COMPLETED
        );

        createRepairHistory(workOrder);

        return workOrderRepository.save(workOrder);
    }
}
