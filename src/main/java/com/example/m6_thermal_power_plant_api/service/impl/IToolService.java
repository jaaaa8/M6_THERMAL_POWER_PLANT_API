package com.example.m6_thermal_power_plant_api.service.impl;


import com.example.m6_thermal_power_plant_api.dto.tool.ToolDamageRequest;
import com.example.m6_thermal_power_plant_api.dto.tool.ToolQuantityUpdateRequest;
import com.example.m6_thermal_power_plant_api.dto.tool.ToolRequest;
import com.example.m6_thermal_power_plant_api.dto.tool.ToolResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IToolService {

    /** Tạo mới CCDC (nhập kho lần đầu) */
    ToolResponse create(ToolRequest request);

    /** Cập nhật thông tin CCDC (không đổi số lượng) */
    ToolResponse update(Integer id, ToolRequest request);

    void delete(Integer id);

    ToolResponse getById(Integer id);

    /** Tìm kiếm theo tên (keyword) và/hoặc chủng loại (categoryId) */
    Page<ToolResponse> search(String keyword, Integer categoryId, Pageable pageable);

    /** Thêm số lượng CCDC vào kho (nhập kho bổ sung) */
    ToolResponse addQuantity(Integer id, ToolQuantityUpdateRequest request);

    /** Huỷ (loại bỏ) số lượng CCDC bị hư hỏng khỏi sử dụng */
    ToolResponse markDamaged(Integer id, ToolDamageRequest request);
}