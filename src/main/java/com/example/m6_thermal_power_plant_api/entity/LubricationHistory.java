package com.example.m6_thermal_power_plant_api.entity;

import com.example.m6_thermal_power_plant_api.entity.base.BaseSoftDeleteEntity;
import com.example.m6_thermal_power_plant_api.entity.base.CascadeSoftDelete;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;

/**
 * Lịch sử thực hiện bảo dưỡng dầu mỡ.
 * Table: lubrication_history
 *
 * Không soft-delete: là lịch sử thực hiện, không xoá.
 */
@Entity
@Table(name = "lubrication_history")
@SQLRestriction("is_deleted = false")
@Getter @Setter
@SuperBuilder
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = false, of = "id")
public class LubricationHistory extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    @CascadeSoftDelete
    private LubricationPlan plan;

    @Column(name = "performed_date")
    private LocalDate performedDate;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
