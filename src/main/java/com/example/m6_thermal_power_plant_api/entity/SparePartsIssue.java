package com.example.m6_thermal_power_plant_api.entity;

import com.example.m6_thermal_power_plant_api.entity.base.BaseSoftDeleteEntity;
import com.example.m6_thermal_power_plant_api.entity.base.CascadeSoftDelete;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

/**
 * Phiếu cấp vật tư THAY THẾ gắn với Phiếu Công Tác.
 * Table: spare_parts_issues
 *
 * Không soft-delete: là chứng từ cấp/xuất vật tư, không xoá. SparePart /
 * Account đều đã @SQLRestriction nên không cần khai báo lại ở các quan hệ dưới.
 */
@Entity
@Table(name = "spare_parts_issues")
@SQLRestriction("is_deleted = false")
@Getter @Setter
@SuperBuilder
@NoArgsConstructor @AllArgsConstructor
@ToString(callSuper = true, exclude = "exports")
@EqualsAndHashCode(callSuper = false, of = "id")
public class SparePartsIssue extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Mã của chính phiếu cấp vật tư này (khác mã vật tư trong danh mục) */
    // composite voi cot active_flag de tao unique sau khi run sql script o thu muc db
    @Column(name = "spare_part_code", nullable = false, length = 50)
    private String sparePartCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id")
    @CascadeSoftDelete
    private WorkOrder workOrder;

    /** Người thực hiện cấp phát (đăng nhập bằng tài khoản) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issued_by")
    private Account issuedBy;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    @OneToMany(mappedBy = "issue")
    private List<SparePartsIssueDetail> details;

    @JsonIgnore
    @OneToMany(mappedBy = "sparePartsIssue", fetch = FetchType.LAZY)
    private List<SparePartExport> exports;
}
