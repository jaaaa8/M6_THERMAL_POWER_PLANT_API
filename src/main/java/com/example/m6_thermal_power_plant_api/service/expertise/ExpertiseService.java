package com.example.m6_thermal_power_plant_api.service.expertise;

import com.example.m6_thermal_power_plant_api.dto.employee.ExpertiseDTO;
import com.example.m6_thermal_power_plant_api.entity.Expertise;
import com.example.m6_thermal_power_plant_api.exception.DuplicateResourceException;
import com.example.m6_thermal_power_plant_api.repository.expertise.IExpertiseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional
    public ExpertiseDTO createExpertise(ExpertiseDTO dto) {
        String code = dto.getExpertiseCode().trim().toUpperCase();
        if (expertiseRepository.existsByExpertiseCode(code)) {
            throw new DuplicateResourceException("Mã chuyên môn '" + code + "' đã tồn tại.");
        }
        String name = dto.getName().trim();
        if (expertiseRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Tên chuyên môn '" + name + "' đã tồn tại.");
        }

        Expertise exp = Expertise.builder()
                .expertiseCode(code)
                .name(name)
                .build();
        Expertise saved = expertiseRepository.save(exp);

        return ExpertiseDTO.builder()
                .id(saved.getId())
                .expertiseCode(saved.getExpertiseCode())
                .name(saved.getName())
                .build();
    }
}
