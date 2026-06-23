package com.example.m6_thermal_power_plant_api.entity;

import com.example.m6_thermal_power_plant_api.entity.base.BaseSoftDeleteEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Phiếu cấp vật tư TIÊU HAO gắn với Phiếu Công Tác.
 * Table: consumable_issues
 *
 * Không soft-delete (lý do tương tự SparePartsIssue). Consumable / Account
 * đều đã @SQLRestriction nên không cần khai báo lại ở các quan hệ dưới.
 */
@Entity
@Table(name = "consumable_issues")
@SQLRestriction("is_deleted = false")
@Getter @Setter
@SuperBuilder
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = false, of = "id")
public class ConsumableIssue extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Mã của chính phiếu cấp vật tư này (khác mã vật tư trong danh mục) */
    @Column(name = "consumable_code", unique = true, nullable = false, length = 50)
    private String consumableCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id")
    private WorkOrder workOrder;

    /** Vật tư tiêu hao được cấp (tham chiếu danh mục) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumable_id")
    private Consumable consumable;

    /** Giá trị hợp lệ theo DB: 'export' | 'import' */
    @Column(name = "transaction_type", length = 50)
    private String transactionType;

    @Column(precision = 10, scale = 2)
    private BigDecimal quantity;

    /** Người thực hiện cấp phát (đăng nhập bằng tài khoản) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issued_by")
    private Account issuedBy;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;
}
