package com.example.m6_thermal_power_plant_api.service.position;

import com.example.m6_thermal_power_plant_api.dto.employee.PositionDTO;
import com.example.m6_thermal_power_plant_api.entity.Position;
import com.example.m6_thermal_power_plant_api.exception.DuplicateResourceException;
import com.example.m6_thermal_power_plant_api.repository.position.IPositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PositionService implements IPositionService {

    private final IPositionRepository positionRepository;

    @Override
    public List<PositionDTO> getAllPositions() {
        return positionRepository.findAll().stream()
                .filter(p -> !Boolean.TRUE.equals(p.getIsDeleted()))
                .map(p -> PositionDTO.builder()
                        .id(p.getId())
                        .positionCode(p.getPositionCode())
                        .name(p.getName())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PositionDTO createPosition(PositionDTO dto) {
        String code = dto.getPositionCode().trim().toUpperCase();
        if (positionRepository.existsByPositionCode(code)) {
            throw new DuplicateResourceException("Mã chức vụ '" + code + "' đã tồn tại.");
        }
        String name = dto.getName().trim();
        if (positionRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Tên chức vụ '" + name + "' đã tồn tại.");
        }

        Position pos = Position.builder()
                .positionCode(code)
                .name(name)
                .build();
        Position saved = positionRepository.save(pos);

        return PositionDTO.builder()
                .id(saved.getId())
                .positionCode(saved.getPositionCode())
                .name(saved.getName())
                .build();
    }
}
