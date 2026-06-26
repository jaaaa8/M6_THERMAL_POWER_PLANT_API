package com.example.m6_thermal_power_plant_api.controller.equipment;

import com.example.m6_thermal_power_plant_api.dto.equipment.response.SystemListDTO;
import com.example.m6_thermal_power_plant_api.service.equipment.IEquipmentSystemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/systems")
@RequiredArgsConstructor
public class EquipmentSystemController {
    private  final IEquipmentSystemService equipmentSystemService;

    @GetMapping
    public ResponseEntity<Page<SystemListDTO>> getSystem(
            @RequestParam (defaultValue = "") String keyword,
            @RequestParam (defaultValue = "0") int page,
            @RequestParam (defaultValue = "10") int size)
    {
        return ResponseEntity.ok(equipmentSystemService.getSystem(keyword,page,size));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id") Integer id){
        equipmentSystemService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
