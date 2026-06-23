package com.example.m6_thermal_power_plant_api.entity;

import com.example.m6_thermal_power_plant_api.entity.base.BaseSoftDeleteEntity;
import com.example.m6_thermal_power_plant_api.entity.base.CascadeSoftDelete;
import com.example.m6_thermal_power_plant_api.entity.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Nhật ký nhập / xuất kho vật tư TIÊU HAO.
 * Table: consumable_inventory
 *
 * Không soft-delete (lý do tương tự SparePartsInventory). Consumable / Account
 * đều đã @SQLRestriction nên không cần khai báo lại ở quan hệ dưới đây.
 */
@Entity
@Table(name = "consumable_inventory")
@SQLRestriction("is_deleted = false")
@Getter @Setter
@SuperBuilder
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = false, of = "id")
public class ConsumableInventory extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumable_id")
    @CascadeSoftDelete
    private Consumable consumable;

    @Column(length = 255)
    private String supplier;

    /** Nhân viên thực hiện giao dịch (đăng nhập bằng tài khoản) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @Builder.Default
    @Column(precision = 10, scale = 2)
    private BigDecimal quantity = BigDecimal.ZERO;

    /** IMPORT = Nhập kho | EXPORT = Xuất kho */
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 10)
    private TransactionType transactionType;

    @CreationTimestamp
    @Column(name = "transaction_date", updatable = false)
    private LocalDateTime transactionDate;
}
