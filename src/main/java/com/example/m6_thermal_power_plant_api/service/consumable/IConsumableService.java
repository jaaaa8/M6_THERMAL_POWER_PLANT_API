package com.example.m6_thermal_power_plant_api.service.consumable;

import com.example.m6_thermal_power_plant_api.dto.consumables.ConsumableDTO;
import com.example.m6_thermal_power_plant_api.entity.enums.PartStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface IConsumableService {
    ConsumableDTO create(ConsumableDTO dto);

    ConsumableDTO update(Integer id, ConsumableDTO dto);

    ConsumableDTO getById(Integer id);

    ConsumableDTO getByCode(String code);

    Page<ConsumableDTO> search(
            String code,
            String name,
            String manufacturer,
            BigDecimal price,
            PartStatus status,
            Pageable pageable
    );

    void delete(Integer id);
}
