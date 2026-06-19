package com.example.m6_thermal_power_plant_api.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Giá trị thông số kỹ thuật cụ thể của một thiết bị.
 * Table: equipment_parameters
 *
 * Không áp dụng @SoftDelete: là dữ liệu phụ thuộc hoàn toàn vào Equipment
 * (entity cha) — khi Equipment bị soft-delete, nghiệp vụ truy vấn luôn đi
 * qua Equipment trước nên dữ liệu con tự nhiên không còn truy cập được.
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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "equipment_id")
    private Equipment equipment;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parameter_id")
    private ParameterCatalog parameter;

    /** Giá trị thực tế của thông số */
    @Column(length = 255)
    private String value;

    @Column(columnDefinition = "TEXT")
    private String description;
}
