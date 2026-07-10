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
 * Phiếu cấp vật tư TIÊU HAO gắn với Phiếu Công Tác.
 * Table: consumable_issues
 *
 * MỨC ĐỘ AN TOÀN SOFT-DELETE: ❌ KHÔNG NÊN (lý do tương tự {@link SparePartsIssue})
 *  - Đây là chứng từ cấp vật tư đã ký các bên.
 *  - Nếu soft-delete:
 *    (1) Cascade ẩn toàn bộ ConsumableIssueDetail + ConsumableExport đính kèm.
 *    (2) KHÔNG hoàn lại tồn kho — vì kho hiện hành dựa trên
 *        ConsumableInventory (sổ giao dịch, không soft-delete). Hậu quả:
 *        tổng actual_quantity từ Export khác tổng EXPORT từ Inventory →
 *        mất khớp, không đối chiếu được.
 *  - QUY TẮC: cấp nhầm → tạo phiếu trả/giao dịch đảo chiều trong Inventory,
 *    KHÔNG xoá phiếu cũ. Service tầng trên KHÔNG gọi softDelete trực tiếp
 *    lên phiếu này; chỉ tồn tại đường cascade từ Equipment/Consumable (cả
 *    hai đều ❌ ở cấp gốc — coi như không xảy ra trong nghiệp vụ thường ngày).
 *
 * (Consumable / Account đều đã @SQLRestriction nên không cần khai báo lại
 * restriction ở các quan hệ dưới.)
 */
@Entity
@Table(name = "consumable_issues")
@SQLRestriction("is_deleted = false")
@Getter @Setter
@SuperBuilder
@NoArgsConstructor @AllArgsConstructor
@ToString(callSuper = true, exclude = "exports")
@EqualsAndHashCode(callSuper = false, of = "id")
public class ConsumableIssue extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Mã của chính phiếu cấp vật tư này (khác mã vật tư trong danh mục) */
    // composite voi cot active_flag de tao unique sau khi run sql script o thu muc db
    @Column(name = "consumable_code", nullable = false, length = 50)
    private String consumableCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id")
    @CascadeSoftDelete
    private WorkOrder workOrder;

    /**
     * LẦN cấp vật tư (bảng cha supplies_issues, V9) mà phiếu này thuộc về — gom
     * cặp phiếu thay thế + tiêu hao tạo trong cùng một hành động. NULL không xảy
     * ra sau backfill V9, nhưng code đọc vẫn nên phòng thủ.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplies_issue_id")
    private SuppliesIssue suppliesIssue;

    /** Vật tư tiêu hao được cấp (tham chiếu danh mục) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumable_id")
    @CascadeSoftDelete
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

    @OneToMany(mappedBy = "issue")
    private List<ConsumableIssueDetail> details;

    @JsonIgnore
    @OneToMany(mappedBy = "consumableIssue", fetch = FetchType.LAZY)
    private List<ConsumableExport> exports;
}
