package com.example.m6_thermal_power_plant_api.controller.equipment;

import com.example.m6_thermal_power_plant_api.dto.equipment.response.UnitListDTO;
import com.example.m6_thermal_power_plant_api.entity.EquipmentType;
import com.example.m6_thermal_power_plant_api.service.equipment.IEquipmentTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/types")
@RequiredArgsConstructor
@CrossOrigin("*")
public class EquipmentTypeController {
    private final IEquipmentTypeService equipmentTypeService;

    @GetMapping
    public ResponseEntity<List<EquipmentType>> getAll() {
        return ResponseEntity.ok(equipmentTypeService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipmentType> getById(
            @PathVariable(name= "id") int id
    ){
        return  ResponseEntity.ok(equipmentTypeService.getById(id));
    }
}
