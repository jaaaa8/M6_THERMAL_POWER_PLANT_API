package com.example.m6_thermal_power_plant_api.entity.base;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Đánh dấu field tham chiếu (FK) cần được cascade soft-delete
 * khi entity cha bị xóa mềm.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CascadeSoftDelete {
}
