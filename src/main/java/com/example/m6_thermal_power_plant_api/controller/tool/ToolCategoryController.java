package com.example.m6_thermal_power_plant_api.controller.tool;

import com.example.m6_thermal_power_plant_api.dto.tool.ApiResponse;
import com.example.m6_thermal_power_plant_api.dto.tool.ToolCategoryRequest;
import com.example.m6_thermal_power_plant_api.dto.tool.ToolCategoryResponse;
import com.example.m6_thermal_power_plant_api.service.impl.IToolCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tool-categories")
@RequiredArgsConstructor
public class ToolCategoryController {

    private final IToolCategoryService toolCategoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ToolCategoryResponse> create(@Valid @RequestBody ToolCategoryRequest request) {
        return ApiResponse.success("Tạo chủng loại thành công", toolCategoryService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ToolCategoryResponse> update(@PathVariable Integer id,
                                                    @Valid @RequestBody ToolCategoryRequest request) {
        return ApiResponse.success("Cập nhật chủng loại thành công", toolCategoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Integer id) {
        toolCategoryService.delete(id);
        return ApiResponse.success("Xoá chủng loại thành công", null);
    }

    @GetMapping("/{id}")
    public ApiResponse<ToolCategoryResponse> getById(@PathVariable Integer id) {
        return ApiResponse.success(toolCategoryService.getById(id));
    }

    @GetMapping("/search")
    public ApiResponse<List<ToolCategoryResponse>> search(
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) String categoryCode) {
        return ApiResponse.success(toolCategoryService.search(categoryName, categoryCode));
    }

    @GetMapping
    public ApiResponse<List<ToolCategoryResponse>> getAll() {
        return ApiResponse.success(toolCategoryService.getAll());
    }
}