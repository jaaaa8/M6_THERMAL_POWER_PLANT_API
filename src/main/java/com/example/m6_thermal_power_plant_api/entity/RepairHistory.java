package com.example.m6_thermal_power_plant_api.entity;

import com.example.m6_thermal_power_plant_api.entity.base.BaseSoftDeleteEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "repair_history")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RepairHistory extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Phiếu công tác thực hiện sửa chữa
    @ManyToOne
    @JoinColumn(name = "work_order_id")
    private WorkOrder workOrder;

    // Thiết bị được sửa
    @ManyToOne
    @JoinColumn(name = "equipment_id")
    private Equipment equipment;

    // Ngày sửa chữa thực tế
    private LocalDate repairDate;

    // Nội dung sửa chữa
    @Column(columnDefinition = "TEXT")
    private String repairContent;

    // Kết quả sau sửa chữa
    @Column(columnDefinition = "TEXT")
    private String repairResult;

    // Vật tư đã thay thế
    @OneToMany(mappedBy = "repairHistory",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<RepairHistoryDetail> details;
}