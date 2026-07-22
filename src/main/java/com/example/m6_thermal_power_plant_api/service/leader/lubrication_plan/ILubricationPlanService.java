package com.example.m6_thermal_power_plant_api.service.leader.lubrication_plan;


import com.example.m6_thermal_power_plant_api.dto.Leader.req.LubricationPlanDto;
import com.example.m6_thermal_power_plant_api.entity.enums.LubricationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ILubricationPlanService {
    Page<LubricationPlanDto> search(String keyword, LubricationStatus status, Pageable pageable);
    LubricationPlanDto create(
            LubricationPlanDto dto
    );
    void deleteById(Integer id);
}
