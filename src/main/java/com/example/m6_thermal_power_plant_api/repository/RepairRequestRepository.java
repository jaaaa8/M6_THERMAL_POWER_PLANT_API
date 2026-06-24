package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.RepairRequest;
import com.example.m6_thermal_power_plant_api.entity.enums.RepairRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepairRequestRepository extends JpaRepository<RepairRequest, Integer> {

    /**
     * Danh sách yêu cầu sửa chữa theo trạng thái (VD PENDING — đang chờ xử lý),
     * mới nhất lên trước. @SQLRestriction đã tự loại các bản ghi đã xoá mềm.
     */
    List<RepairRequest> findByStatusOrderByCreatedAtDesc(RepairRequestStatus status);
}
