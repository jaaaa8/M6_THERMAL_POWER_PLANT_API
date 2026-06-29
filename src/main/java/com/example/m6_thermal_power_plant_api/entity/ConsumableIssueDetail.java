package com.example.m6_thermal_power_plant_api.entity;

import com.example.m6_thermal_power_plant_api.entity.base.BaseSoftDeleteEntity;
import com.example.m6_thermal_power_plant_api.entity.base.CascadeSoftDelete;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

/**
 * Dòng chi tiết của 1 phiếu cấp vật tư TIÊU HAO.
 * Table: consumable_issue_details
 *
 * Soft delete: xem {@link BaseSoftDeleteEntity}. ConsumableIssue / Consumable đều
 * đã @SQLRestriction nên 2 quan hệ dưới không cần khai báo lại restriction; cả hai
 * gắn @CascadeSoftDelete để dòng chi tiết bị ẩn cùng phiếu cấp / danh mục vật tư.
 */
@Entity
@Table(name = "consumable_issue_details")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, of = "id")
public class ConsumableIssueDetail extends BaseSoftDeleteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id")
    @CascadeSoftDelete
    private ConsumableIssue issue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumable_id")
    @CascadeSoftDelete
    private Consumable consumable;

    private BigDecimal quantity;
}
