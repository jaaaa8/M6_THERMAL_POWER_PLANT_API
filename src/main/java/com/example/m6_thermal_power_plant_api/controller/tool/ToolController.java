package com.example.m6_thermal_power_plant_api.controller.tool;

import com.example.m6_thermal_power_plant_api.dto.tool.ApiResponse;
import com.example.m6_thermal_power_plant_api.dto.tool.ToolDamageRequest;
import com.example.m6_thermal_power_plant_api.dto.tool.ToolQuantityUpdateRequest;
import com.example.m6_thermal_power_plant_api.dto.tool.ToolRequest;
import com.example.m6_thermal_power_plant_api.dto.tool.ToolResponse;
import com.example.m6_thermal_power_plant_api.service.impl.IToolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tools")
@RequiredArgsConstructor
public class ToolController {

    private final IToolService toolService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ToolResponse> create(@Valid @RequestBody ToolRequest request) {
        return ApiResponse.success("Tạo CCDC thành công", toolService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ToolResponse> update(@PathVariable Integer id, @Valid @RequestBody ToolRequest request) {
        return ApiResponse.success("Cập nhật CCDC thành công", toolService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Integer id) {
        toolService.delete(id);
        return ApiResponse.success("Xoá CCDC thành công", null);
    }

    @GetMapping("/{id}")
    public ApiResponse<ToolResponse> getById(@PathVariable Integer id) {
        return ApiResponse.success(toolService.getById(id));
    }

    @GetMapping
    public ApiResponse<Page<ToolResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            Pageable pageable) {
        return ApiResponse.success(toolService.search(keyword, categoryId, pageable));
    }

    @PatchMapping("/{id}/quantity")
    public ApiResponse<ToolResponse> addQuantity(@PathVariable Integer id,
                                                  @Valid @RequestBody ToolQuantityUpdateRequest request) {
        return ApiResponse.success("Nhập kho thành công", toolService.addQuantity(id, request));
    }

    @PatchMapping("/{id}/damage")
    public ApiResponse<ToolResponse> markDamaged(@PathVariable Integer id,
                                                  @Valid @RequestBody ToolDamageRequest request) {
        return ApiResponse.success("Đã huỷ CCDC hư hỏng", toolService.markDamaged(id, request));
    }

}
