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
 * Dòng chi tiết của 1 phiếu cấp vật tư THAY THẾ.
 * Table: spare_parts_issue_details
 *
 * Soft delete: xem {@link BaseSoftDeleteEntity}. SparePartsIssue / SparePart đều
 * đã @SQLRestriction nên 2 quan hệ dưới không cần khai báo lại restriction; cả hai
 * gắn @CascadeSoftDelete để dòng chi tiết bị ẩn cùng phiếu cấp / danh mục vật tư.
 */
@Entity
@Table(name = "spare_parts_issue_details")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, of = "id")
public class SparePartsIssueDetail extends BaseSoftDeleteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id")
    @CascadeSoftDelete
    private SparePartsIssue issue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spare_part_id")
    @CascadeSoftDelete
    private SparePart sparePart;

    private Integer quantity;
}
