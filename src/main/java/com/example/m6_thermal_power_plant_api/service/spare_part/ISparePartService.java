package com.example.m6_thermal_power_plant_api.service.spare_part;

import com.example.m6_thermal_power_plant_api.dto.spare_parts.SparePartDTO;
import com.example.m6_thermal_power_plant_api.entity.enums.PartStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface ISparePartService {
    SparePartDTO create(SparePartDTO dto);

    SparePartDTO update(Integer id, SparePartDTO dto);

    SparePartDTO getById(Integer id);

    SparePartDTO getByCode(String code);

    Page<SparePartDTO> search(
            String code,
            String name,
            String manufacturer,
            BigDecimal price,
            PartStatus status,
            Pageable pageable
    );

    void delete(Integer id);
}
