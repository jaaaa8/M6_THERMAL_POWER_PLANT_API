package com.example.m6_thermal_power_plant_api.controller.leader;

import com.example.m6_thermal_power_plant_api.dto.Leader.req.RepairHistoryCreateRequestDto;
import com.example.m6_thermal_power_plant_api.dto.Leader.res.RepairHistoryResponseDto;
import com.example.m6_thermal_power_plant_api.service.leader.repair_history.RepairHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/repair-histories")
@RequiredArgsConstructor
public class RepairHistoryController {

    private final RepairHistoryService repairHistoryService;

    @GetMapping
    public ResponseEntity<List<RepairHistoryResponseDto>>
    getAll() {

        return ResponseEntity.ok(
                repairHistoryService.findAll()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<RepairHistoryResponseDto>
    getById(@PathVariable Integer id) {

        return ResponseEntity.ok(
                repairHistoryService.findById(id)
        );
    }

    @PostMapping
    public ResponseEntity<RepairHistoryResponseDto>
    create(
            @RequestBody
            RepairHistoryCreateRequestDto dto
    ) {

        return ResponseEntity.ok(
                repairHistoryService.create(dto)
        );
    }
}
