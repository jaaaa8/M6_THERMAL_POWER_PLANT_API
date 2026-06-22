package com.example.m6_thermal_power_plant_api.entity.enums;

/**
 * Trạng thái kinh doanh của 1 loại vật tư trong danh mục
 * (KHÁC với is_deleted — status là trạng thái nghiệp vụ "còn dùng/ngừng dùng",
 * is_deleted là cờ xoá mềm hành chính). Dùng cho SparePart, Consumable.
 */
public enum PartStatus {
    ACTIVE,
    INACTIVE
}
