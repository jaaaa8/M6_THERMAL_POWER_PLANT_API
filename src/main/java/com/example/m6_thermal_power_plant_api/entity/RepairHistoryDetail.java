package com.example.m6_thermal_power_plant_api.entity;

import com.example.m6_thermal_power_plant_api.entity.base.BaseSoftDeleteEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;


@Entity
@Table(name = "repair_history_details")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, of = "id")
public class RepairHistoryDetail extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repair_history_id", nullable = false)
    private RepairHistory repairHistory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spare_part_id", nullable = false)
    private SparePart sparePart;

    private Integer quantity;
}