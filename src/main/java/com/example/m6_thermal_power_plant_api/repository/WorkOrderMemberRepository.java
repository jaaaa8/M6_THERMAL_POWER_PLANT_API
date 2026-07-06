package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.WorkOrderMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkOrderMemberRepository extends JpaRepository<WorkOrderMember, Integer> {

    /** Danh sách thành viên (đang active) của một phiếu công tác. */
    List<WorkOrderMember> findByWorkOrder_Id(Integer workOrderId);

    /** Member theo id nhưng PHẢI thuộc đúng phiếu công tác chỉ định (chặn sửa chéo phiếu). */
    Optional<WorkOrderMember> findByIdAndWorkOrder_Id(Integer id, Integer workOrderId);

    /** Nhân viên đang là thành viên CHƯA RỜI (left_at IS NULL) của phiếu? Dùng chặn join trùng. */
    boolean existsByWorkOrder_IdAndEmployees_IdAndLeftAtIsNull(Integer workOrderId, Integer employeeId);
}
