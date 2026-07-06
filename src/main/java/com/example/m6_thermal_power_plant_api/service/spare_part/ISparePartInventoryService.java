package com.example.m6_thermal_power_plant_api.service.spare_part;

import com.example.m6_thermal_power_plant_api.dto.spare_parts.SparePartReceiptCreateDTO;
import com.example.m6_thermal_power_plant_api.dto.spare_parts.SparePartReceiptResponseDTO;
import com.example.m6_thermal_power_plant_api.dto.spare_parts.SparePartStockDTO;
import com.example.m6_thermal_power_plant_api.entity.enums.PartStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ISparePartInventoryService {
    SparePartReceiptResponseDTO importSparePart(SparePartReceiptCreateDTO dto, Integer accountId);

    Page<SparePartStockDTO> searchStock(
            String code,
            String name,
            String manufacturer,
            PartStatus status,
            Pageable pageable
    );

    Page<SparePartReceiptResponseDTO> getReceiptHistory(Pageable pageable);
}
