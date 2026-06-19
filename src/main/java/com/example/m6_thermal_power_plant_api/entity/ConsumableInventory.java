package com.example.m6_thermal_power_plant_api.entity;

import com.example.m6_thermal_power_plant_api.entity.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Nhật ký nhập / xuất kho vật tư TIÊU HAO.
 * Table: consumable_inventory
 *
 * Không áp dụng @SoftDelete (lý do tương tự SparePartsInventory).
 */
@Entity
@Table(name = "consumable_inventory")
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class ConsumableInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "consumable_id")
    private Consumable consumable;

    @Column(length = 255)
    private String supplier;

    /** Nhân viên thực hiện giao dịch (đăng nhập bằng tài khoản) */
    @ManyToOne(fetch = FetchType.EAGER)
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
