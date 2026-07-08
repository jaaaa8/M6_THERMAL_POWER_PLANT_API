package com.example.m6_thermal_power_plant_api.service.expertise;

import com.example.m6_thermal_power_plant_api.dto.employee.ExpertiseDTO;
import com.example.m6_thermal_power_plant_api.repository.expertise.IExpertiseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpertiseService implements IExpertiseService {

    private final IExpertiseRepository expertiseRepository;

    @Override
    public List<ExpertiseDTO> getAllExpertises() {
        return expertiseRepository.findAll().stream()
                .filter(e -> !Boolean.TRUE.equals(e.getIsDeleted()))
                .map(e -> ExpertiseDTO.builder()
                        .id(e.getId())
                        .expertiseCode(e.getExpertiseCode())
                        .name(e.getName())
                        .build())
                .collect(Collectors.toList());
    }
}
