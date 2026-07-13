package com.example.m6_thermal_power_plant_api.controller.spare_part;

import java.math.BigDecimal;

import com.example.m6_thermal_power_plant_api.dto.consumables.ConsumableReceiptCreateDTO;
import com.example.m6_thermal_power_plant_api.dto.consumables.ConsumableReceiptResponseDTO;
import com.example.m6_thermal_power_plant_api.dto.consumables.ConsumableStockDTO;
import com.example.m6_thermal_power_plant_api.dto.spare_parts.SparePartReceiptCreateDTO;
import com.example.m6_thermal_power_plant_api.dto.spare_parts.SparePartReceiptResponseDTO;
import com.example.m6_thermal_power_plant_api.dto.spare_parts.SparePartStockDTO;
import com.example.m6_thermal_power_plant_api.security.CustomUserDetails;
import com.example.m6_thermal_power_plant_api.service.spare_part.ISparePartInventoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.m6_thermal_power_plant_api.dto.spare_parts.SparePartDTO;
import com.example.m6_thermal_power_plant_api.entity.enums.PartStatus;
import com.example.m6_thermal_power_plant_api.service.spare_part.ISparePartService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/api/v1/spare-parts")
@RequiredArgsConstructor
public class SparePartController {
    private final ISparePartService sparePartService;
    private final ISparePartInventoryService sparePartInventoryService;

    @PostMapping
    public SparePartDTO create(@Valid @RequestBody SparePartDTO dto) {
        return sparePartService.create(dto);
    }

    @GetMapping("/code/{code}")
    public SparePartDTO getByCode(@PathVariable String code) {
        return sparePartService.getByCode(code);
    }

    @GetMapping("/{id}")
    public SparePartDTO getById(@PathVariable Integer id) {
        return sparePartService.getById(id);
    }

    @GetMapping
    public Page<SparePartDTO> search( @RequestParam(required = false) String code,
                                      @RequestParam(required = false) String name,
                                      @RequestParam(required = false) String manufacturer,
                                      @RequestParam(required = false) BigDecimal price,
                                      @RequestParam(required = false) PartStatus status,
                                      Pageable pageable) {
        return sparePartService.search(code, name, manufacturer, price, status, pageable);
    }

    @PutMapping("/{id}")
    public SparePartDTO updateSparePartDTO(@PathVariable Integer id, @Valid @RequestBody SparePartDTO dto) {
        return sparePartService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id){
        sparePartService.delete(id);
    }

    @GetMapping("/stock")
    public Page<SparePartStockDTO> searchStock(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) PartStatus status,
            Pageable pageable) {
        return sparePartInventoryService.searchStock(code, name, manufacturer, status, pageable);
    }

    @PostMapping("/receipts")
    public ResponseEntity<?> importConsumable(
            @Valid @RequestBody SparePartReceiptCreateDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            SparePartReceiptResponseDTO response = sparePartInventoryService.importSparePart(dto, userDetails.getAccountId());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/receipts")
    public Page<SparePartReceiptResponseDTO> getReceiptHistory(Pageable pageable) {
        return sparePartInventoryService.getReceiptHistory(pageable);
    }
}
