package com.example.m6_thermal_power_plant_api.controller.leader;

import com.example.m6_thermal_power_plant_api.dto.Leader.req.LubricationPlanDto;
import com.example.m6_thermal_power_plant_api.entity.enums.LubricationStatus;
import com.example.m6_thermal_power_plant_api.service.leader.lubrication_plan.ILubricationPlanService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/lubrication-plan")
public class LubricationPlanController {
    private ILubricationPlanService lubricationPlanService;

    public LubricationPlanController(ILubricationPlanService lubricationPlanService) {
        this.lubricationPlanService = lubricationPlanService;
    }

    @GetMapping("")
    public ResponseEntity<Page<LubricationPlanDto>> getAllLubricationPlans(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) LubricationStatus status,
            Pageable pageable
    ) {
        return ResponseEntity.ok(lubricationPlanService.search(keyword,status,pageable));
    }

    @PostMapping("/add")
    public ResponseEntity<?> create(
            @RequestBody LubricationPlanDto dto
    ){

        try {

            return ResponseEntity.ok(
                    lubricationPlanService.create(dto)
            );

        } catch(Exception e){

            e.printStackTrace();

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}
