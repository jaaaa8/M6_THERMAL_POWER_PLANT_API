package com.example.m6_thermal_power_plant_api.controller.tool;

import com.example.m6_thermal_power_plant_api.dto.CreateToolDTO;
import com.example.m6_thermal_power_plant_api.entity.Tool;
import com.example.m6_thermal_power_plant_api.service.impl.IToolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tools")
@RequiredArgsConstructor
public class ToolController {

    private  IToolService toolService;


    @PostMapping
    public Tool createTool(
            @Valid @RequestBody CreateToolDTO dto
    ) {
        return toolService.createTool(dto);
    }


    @GetMapping
    public List<Tool> getAllTools() {
        return toolService.getAllTools();
    }

    @GetMapping("/search")
    public List<Tool> searchTools(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId
    ) {
        return toolService.search(keyword, categoryId);
    }
}