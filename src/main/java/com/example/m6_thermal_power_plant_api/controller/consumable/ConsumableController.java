package com.example.m6_thermal_power_plant_api.controller.consumable;


import com.example.m6_thermal_power_plant_api.dto.consumables.ConsumableDTO;
import com.example.m6_thermal_power_plant_api.dto.consumables.ConsumableReceiptCreateDTO;
import com.example.m6_thermal_power_plant_api.dto.consumables.ConsumableReceiptResponseDTO;
import com.example.m6_thermal_power_plant_api.dto.consumables.ConsumableStockDTO;
import com.example.m6_thermal_power_plant_api.entity.enums.PartStatus;
import com.example.m6_thermal_power_plant_api.security.CustomUserDetails;
import com.example.m6_thermal_power_plant_api.service.consumable.IConsumableInventoryService;
import com.example.m6_thermal_power_plant_api.service.consumable.IConsumableService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/consumables")
public class ConsumableController {

    private final IConsumableService consumableService;
    private final IConsumableInventoryService consumableInventoryService;



    @PostMapping
    public ConsumableDTO create(@Valid @RequestBody ConsumableDTO dto){
        return consumableService.create(dto);
    }

    @GetMapping("/code/{code}")
    public ConsumableDTO getByCode(@PathVariable String code){
        return consumableService.getByCode(code);
    }

    @GetMapping("/{id}")
    public ConsumableDTO getById(@PathVariable Integer id){
        return consumableService.getById(id);
    }

    @GetMapping("")
    public Page<ConsumableDTO> search(@RequestParam(required = false) String code,
                                      @RequestParam(required = false) String name,
                                      @RequestParam(required = false) String manufacturer,
                                      @RequestParam(required = false)BigDecimal price,
                                      @RequestParam(required = false)PartStatus status,
                                      Pageable pageable){
        return consumableService.search(code, name, manufacturer, price, status, pageable);
    }

    @PutMapping("/{id}")
    public ConsumableDTO update(@PathVariable Integer id,@Valid @RequestBody ConsumableDTO consumableDTO){
        return consumableService.update(id, consumableDTO);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id){
        consumableService.delete(id);
    }

    @GetMapping("/stock")
    public Page<ConsumableStockDTO> searchStock(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) PartStatus status,
            Pageable pageable) {
        return consumableInventoryService.searchStock(code, name, manufacturer, status, pageable);
    }

    @PostMapping("/receipts")
    public ResponseEntity<?> importConsumable(
            @Valid @RequestBody ConsumableReceiptCreateDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            ConsumableReceiptResponseDTO response = consumableInventoryService.importConsumable(dto, userDetails.getAccountId());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/receipts")
    public Page<ConsumableReceiptResponseDTO> getReceiptHistory(Pageable pageable) {
        return consumableInventoryService.getReceiptHistory(pageable);
    }





}
