package com.example.m6_thermal_power_plant_api.controller.equipment;

import com.example.m6_thermal_power_plant_api.dto.equipment.response.UnitListDTO;
import com.example.m6_thermal_power_plant_api.service.equipment.IEquipmentSystemService;
import com.example.m6_thermal_power_plant_api.service.equipment.IUnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/units")
@RequiredArgsConstructor
@CrossOrigin("*")
public class UnitController {
    private  final IUnitService unitService;

    @GetMapping("/{id}")
    public ResponseEntity<UnitListDTO> getById(
            @PathVariable (name= "id") int id
    ){
        return  ResponseEntity.ok(unitService.getById(id));
    }
}
