package com.example.m6_thermal_power_plant_api.service.position;

import com.example.m6_thermal_power_plant_api.dto.employee.PositionDTO;
import com.example.m6_thermal_power_plant_api.repository.position.IPositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PositionService implements IPositionService {

    private final IPositionRepository positionRepository;

    @Override
    public List<PositionDTO> getAllPositions() {
        return positionRepository.findAll().stream()
                .map(p -> PositionDTO.builder()
                        .positionCode(p.getPositionCode())
                        .name(p.getName())
                        .build())
                .collect(Collectors.toList());
    }
}
