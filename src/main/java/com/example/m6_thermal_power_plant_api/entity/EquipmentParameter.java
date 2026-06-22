package com.example.m6_thermal_power_plant_api.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Giá trị thông số kỹ thuật cụ thể của một thiết bị.
 * Table: equipment_parameters
 *
 * Không soft-delete: là dữ liệu phụ thuộc hoàn toàn vào Equipment (entity cha)
 * — khi Equipment bị xoá mềm, nghiệp vụ truy vấn luôn đi qua Equipment trước
 * nên dữ liệu con tự nhiên không còn truy cập được qua đường bình thường.
 * (Equipment / ParameterCatalog đều đã @SQLRestriction("is_deleted = false")
 * nên 2 quan hệ dưới đây KHÔNG cần khai báo lại restriction.)
 */
@Entity
@Table(name = "equipment_parameters")
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class EquipmentParameter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id")
    private Equipment equipment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parameter_id")
    private ParameterCatalog parameter;

    /** Giá trị thực tế của thông số */
    @Column(length = 255)
    private String value;

    @Column(columnDefinition = "TEXT")
    private String description;
}
