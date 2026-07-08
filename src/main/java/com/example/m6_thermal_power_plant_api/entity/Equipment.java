package com.example.m6_thermal_power_plant_api.entity;

import com.example.m6_thermal_power_plant_api.entity.base.BaseSoftDeleteEntity;
import com.example.m6_thermal_power_plant_api.entity.base.CascadeSoftDelete;
import com.example.m6_thermal_power_plant_api.entity.enums.EquipmentStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

/**
 * Thiết bị trong nhà máy.
 * Table: equipment
 *
 * MỨC ĐỘ AN TOÀN SOFT-DELETE: ❌ RỦI RO CAO — đây là GỐC của cây cascade lớn
 * nhất hệ thống. Soft-delete Equipment kéo theo TOÀN BỘ:
 *   - EquipmentParameter (thông số chi tiết)
 *   - LubricationPlan + LubricationHistory (kế hoạch & lịch sử bôi trơn)
 *   - SparePartExport, ConsumableExport (qua cột equipment_id)
 *   - RepairRequest → WorkOrder → WorkOrderMember, WorkOrderExtension,
 *     SparePart/ConsumableIssue → details & exports
 * Tức là toàn bộ lịch sử sửa chữa & chứng từ pháp lý của thiết bị bị ẩn khỏi
 * UI. DB vẫn còn (cùng deleted_at) nên restore được, nhưng restore sẽ "hồi
 * sinh" cả chứng từ đã ký — cẩn trọng.
 *
 * QUY TẮC NGHIỆP VỤ:
 *   - CHỈ thanh lý thiết bị khi không còn PCT/chứng từ "đang dở" tham chiếu.
 *   - Service phải cảnh báo người dùng rằng toàn bộ lịch sử liên quan sẽ bị
 *     ẩn khỏi màn hình mặc định trước khi xác nhận.
 *   - Nếu chỉ muốn đánh dấu thiết bị "ngừng vận hành" → dùng
 *     {@code EquipmentStatus} (đang vận hành / đang sửa chữa / dự phòng /
 *     ngừng vận hành), KHÔNG soft-delete.
 */
@Entity
@Table(name = "equipment")
@SQLRestriction("is_deleted = false")
@Getter @Setter
@SuperBuilder
@NoArgsConstructor @AllArgsConstructor
@ToString(callSuper = true, exclude = {"parameters", "repairRequests", "lubricationPlans"})
@EqualsAndHashCode(callSuper = false, of = "id")
public class Equipment extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Mã KKS - định danh duy nhất cho mỗi thiết bị */
    @Column(name = "kks_code", unique = true, nullable = false, length = 50)
    private String kksCode;

    @Column(nullable = false, length = 255)
    private String name;

    /** EquipmentSystem / EquipmentType đều @SQLRestriction("is_deleted = false")
     *  nên không cần khai báo lại restriction ở 2 quan hệ dưới đây. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "system_id", nullable=false)
    @CascadeSoftDelete
    private EquipmentSystem system;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_type_id", nullable=false)
    @CascadeSoftDelete
    private EquipmentType equipmentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipmentStatus status;

    @Column(name = "installation_year")
    private Integer installationYear;

    @Column(length = 255)
    private String model;

    @Column(length = 255)
    private String manufacturer;


    @Column(columnDefinition = "TEXT")
    private String description;


    @Lob
    @Column(name="img_path", columnDefinition = "LONGTEXT")
    private String imgPath;

    @JsonIgnore
    @OneToMany(mappedBy = "equipment", fetch = FetchType.LAZY)
    private List<EquipmentParameter> parameters;

    @JsonIgnore
    @OneToMany(mappedBy = "equipment", fetch = FetchType.LAZY)
    private List<RepairRequest> repairRequests;

    @JsonIgnore
    @OneToMany(mappedBy = "equipment", fetch = FetchType.LAZY)
    private List<LubricationPlan> lubricationPlans;

    @JsonIgnore
    @OneToMany(mappedBy = "equipment", fetch = FetchType.LAZY)
    private List<LubricationHistory> histories;
}
