package com.example.m6_thermal_power_plant_api.controller.equipment;

import com.example.m6_thermal_power_plant_api.dto.equipment.response.ListEquipmentDTO;
import com.example.m6_thermal_power_plant_api.entity.enums.EquipmentStatus;
import com.example.m6_thermal_power_plant_api.service.equipment.EquipmentService;
import com.example.m6_thermal_power_plant_api.service.equipment.IEquipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/equipments")
@RequiredArgsConstructor
@CrossOrigin("*")
public class EquipmentController {
    private final IEquipmentService equipmentService;

    @GetMapping
    public ResponseEntity<Page<ListEquipmentDTO>> getEquipmentList(
            @RequestParam (required = false) String kks,
            @RequestParam (required = false) String name,
            @RequestParam (required = false) Integer typeId,
            @RequestParam (required = false)EquipmentStatus status,
            Pageable pageable
    ){
        return  ResponseEntity.ok(equipmentService.getEquipmentList(
                kks,
                name,
                typeId,
                status ,
                pageable
        ));

    }
}
