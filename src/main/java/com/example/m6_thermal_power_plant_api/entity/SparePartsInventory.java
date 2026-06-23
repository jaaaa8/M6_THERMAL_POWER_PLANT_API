package com.example.m6_thermal_power_plant_api.entity;

import com.example.m6_thermal_power_plant_api.entity.base.BaseSoftDeleteEntity;
import com.example.m6_thermal_power_plant_api.entity.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Nhật ký nhập / xuất kho vật tư THAY THẾ.
 * Mỗi dòng = 1 giao dịch (không cộng dồn).
 * Table: spare_parts_inventory
 *
 * Không soft-delete: là nhật ký giao dịch — xoá (kể cả ẩn) sẽ làm sai số tồn
 * kho. Nhập nhầm thì tạo giao dịch đảo chiều, không xoá dòng cũ.
 * (SparePart / Account đều đã @SQLRestriction("is_deleted = false") nên 2
 * quan hệ dưới đây KHÔNG cần khai báo lại restriction.)
 */
@Entity
@Table(name = "spare_parts_inventory")
@SQLRestriction("is_deleted = false")
@Getter @Setter
@SuperBuilder
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = false, of = "id")
public class SparePartsInventory extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spare_part_id")
    private SparePart sparePart;

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
