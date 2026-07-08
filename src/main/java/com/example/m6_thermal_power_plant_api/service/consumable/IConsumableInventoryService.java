package com.example.m6_thermal_power_plant_api.service.consumable;

import com.example.m6_thermal_power_plant_api.dto.consumables.ConsumableReceiptCreateDTO;
import com.example.m6_thermal_power_plant_api.dto.consumables.ConsumableReceiptResponseDTO;
import com.example.m6_thermal_power_plant_api.dto.consumables.ConsumableStockDTO;
import com.example.m6_thermal_power_plant_api.entity.enums.PartStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IConsumableInventoryService {
    ConsumableReceiptResponseDTO importConsumable(ConsumableReceiptCreateDTO dto, Integer accountId);

    Page<ConsumableStockDTO> searchStock(
            String code,
            String name,
            String manufacturer,
            PartStatus status,
            Pageable pageable
    );

    Page<ConsumableReceiptResponseDTO> getReceiptHistory(Pageable pageable);
}
