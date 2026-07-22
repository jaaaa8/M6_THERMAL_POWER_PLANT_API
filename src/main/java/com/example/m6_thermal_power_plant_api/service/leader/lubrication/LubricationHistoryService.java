package com.example.m6_thermal_power_plant_api.service.leader.lubrication;

import com.example.m6_thermal_power_plant_api.dto.equipment.response.LubricationHistoryDTO;
import com.example.m6_thermal_power_plant_api.entity.LubricationHistory;
import com.example.m6_thermal_power_plant_api.repository.ILubricationHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LubricationHistoryService implements ILubricationHistoryService {
    private final ILubricationHistoryRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<LubricationHistoryDTO> findByEquipment(Integer equipmentId) {
        return repository
                .findByEquipmentIdOrderByPerformedDateDesc(equipmentId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    private LubricationHistoryDTO mapToDto(
            LubricationHistory history

    ){
        LubricationHistoryDTO dto =
                new LubricationHistoryDTO();
        dto.setId(history.getId());
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
        dto.setPerformedDate(
                history.getPerformedDate()
        );
        dto.setNotes(
                history.getNotes()
        );
        return dto;
    }
}
