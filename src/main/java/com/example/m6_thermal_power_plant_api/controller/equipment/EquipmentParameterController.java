package com.example.m6_thermal_power_plant_api.controller.equipment;

import com.example.m6_thermal_power_plant_api.dto.equipment.request.ParameterDTO;
import com.example.m6_thermal_power_plant_api.service.equipment.IEquipmentParameterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/parameter")
@RequiredArgsConstructor
@CrossOrigin("*")
public class EquipmentParameterController {
    private final IEquipmentParameterService parameterService;

    @GetMapping("/equipment/{equipmentId}")
    public ResponseEntity<List<ParameterDTO>> getByEquipment(
            @PathVariable Integer equipmentId
    ){
        return ResponseEntity.ok(parameterService.getByEquipment(equipmentId));
    }

    @PreAuthorize("hasAnyRole('WORKSHOP_FOREMAN')")
    @PostMapping
    public ResponseEntity<List<ParameterDTO>> create(
            @RequestBody List<ParameterDTO> dtos
    ){
        return ResponseEntity.ok(parameterService.create(dtos));
    }

    @PreAuthorize("hasAnyRole('WORKSHOP_FOREMAN')")
    @PutMapping("/{id}")
    public  ResponseEntity<ParameterDTO> update(
            @PathVariable Integer id,
            @RequestBody ParameterDTO dto
    ){
        return  ResponseEntity.ok(parameterService.update(id,dto));
    }
    @PreAuthorize("hasAnyRole('WORKSHOP_FOREMAN')")
    @DeleteMapping("/{id}")
    public  ResponseEntity<Void> delete(
            @PathVariable Integer id
    )
    {
        parameterService.delete(id);
        return  ResponseEntity.ok().build();
    }
}
