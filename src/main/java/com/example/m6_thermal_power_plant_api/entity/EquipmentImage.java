package com.example.m6_thermal_power_plant_api.entity;

import com.example.m6_thermal_power_plant_api.entity.base.BaseSoftDeleteEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

@Entity
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = {"parameters", "repairRequests", "lubricationPlans"})
@EqualsAndHashCode(callSuper = false, of = "id")
@Table(name = "equipment_image")
public class EquipmentImage extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id")
    private Equipment equipment;
}