package com.example.m6_thermal_power_plant_api.controller.equipment;

import com.example.m6_thermal_power_plant_api.service.equipment.IEquipmentSystemService;
import com.example.m6_thermal_power_plant_api.service.equipment.IUnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/units")
@RequiredArgsConstructor
@CrossOrigin("*")
public class UnitController {
    private  final IUnitService unitService;
}
