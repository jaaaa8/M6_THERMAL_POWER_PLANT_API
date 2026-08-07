package com.example.m6_thermal_power_plant_api.entity;

import com.example.m6_thermal_power_plant_api.entity.base.BaseSoftDeleteEntity;
import com.example.m6_thermal_power_plant_api.entity.base.CascadeSoftDelete;
import com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderEquipmentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

/**
 * Một thiết bị trong phạm vi PCT thủ công (WO không có RepairRequest), kèm
 * trạng thái làm việc RIÊNG của thiết bị đó. WO từ yêu cầu không có dòng nào.
 *
 * Là entity xoá mềm + @CascadeSoftDelete trên cả 2 FK: xoá mềm Equipment (hoặc
 * WorkOrder) sẽ ẩn luôn dòng join, nên không bao giờ tồn tại dòng "sống" trỏ tới
 * bản ghi đã bị @SQLRestriction lọc → không có EntityNotFoundException, và giữ
 * nguyên được LAZY (xem Quyết định 1).
 */
@Entity
@Table(name = "work_order_equipments")
@SQLRestriction("is_deleted = false")
@Getter @Setter
@SuperBuilder
@NoArgsConstructor @AllArgsConstructor
public class WorkOrderEquipment extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id", nullable = false)
    @CascadeSoftDelete
    private WorkOrder workOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    @CascadeSoftDelete
    private Equipment equipment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WorkOrderEquipmentStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lubrication_plan_id")
    private LubricationPlan lubricationPlan;
}
