package com.example.m6_thermal_power_plant_api.service.leader.lubrication;

import com.example.m6_thermal_power_plant_api.dto.equipment.response.LubricationHistoryDTO;
import com.example.m6_thermal_power_plant_api.entity.LubricationHistory;
import com.example.m6_thermal_power_plant_api.repository.ILubricationHistoryRepository;
import com.example.m6_thermal_power_plant_api.repository.equipment.IEquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LubricationHistoryService implements ILubricationHistoryService {
    private final ILubricationHistoryRepository lubricationHistoryRepository;
    private final IEquipmentRepository equipmentRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LubricationHistoryDTO> findByEquipment(Integer equipmentId) {
        return lubricationHistoryRepository
                .findByEquipmentIdOrderByPerformedDateDesc(equipmentId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public void create(LubricationHistoryDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("LubricationHistoryDTO cannot be null");
        }
        lubricationHistoryRepository.create(mapToLubricationHistory(dto));
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

    private  LubricationHistory mapToLubricationHistory(LubricationHistoryDTO dto){
        LubricationHistory history = new LubricationHistory();
        history.setId(dto.getId());
        history.setEquipment(equipmentRepository.findById(dto.getEquipmentId()).get());

        history.setPerformedDate(dto.getPerformedDate());
        history.setNotes(dto.getNotes());
        return history;
    }
}
