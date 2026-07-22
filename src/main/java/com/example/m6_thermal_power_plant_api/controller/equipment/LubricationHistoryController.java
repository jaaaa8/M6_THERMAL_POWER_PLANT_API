package com.example.m6_thermal_power_plant_api.controller.equipment;

import com.example.m6_thermal_power_plant_api.dto.equipment.response.LubricationHistoryDTO;
import com.example.m6_thermal_power_plant_api.service.leader.lubrication.ILubricationHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/lubrication")
@RequiredArgsConstructor
@CrossOrigin("*")
public class LubricationHistoryController {
    private final ILubricationHistoryService service;

    @GetMapping("/equipment/{equipmentId}")
    public ResponseEntity<List<LubricationHistoryDTO>>
    getByEquipment(
            @PathVariable Integer equipmentId
    ){

        return ResponseEntity.ok(
                service.findByEquipment(equipmentId)
        );
    }
}
