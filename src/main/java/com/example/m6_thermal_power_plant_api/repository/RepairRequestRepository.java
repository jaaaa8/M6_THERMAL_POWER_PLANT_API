package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.RepairRequest;
import com.example.m6_thermal_power_plant_api.entity.enums.RepairRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepairRequestRepository extends JpaRepository<RepairRequest, Integer> {

    /**
     * Trang yêu cầu sửa chữa theo trạng thái (VD PENDING — đang chờ xử lý).
     * Sắp xếp do {@link Pageable} quyết định (controller mặc định createdAt desc).
     * @SQLRestriction đã tự loại các bản ghi đã xoá mềm.
     */
    Page<RepairRequest> findByStatus(RepairRequestStatus status, Pageable pageable);
}
