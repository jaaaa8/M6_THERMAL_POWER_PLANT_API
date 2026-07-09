package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.SuppliesIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SuppliesIssueRepository extends JpaRepository<SuppliesIssue, Integer> {

    /** Các lần cấp vật tư của một phiếu công tác, cũ nhất trước (để đánh số #1, #2...). */
    List<SuppliesIssue> findByWorkOrder_IdOrderByIssuedAtAscIdAsc(Integer workOrderId);

    /** Lần cấp theo id nhưng PHẢI thuộc đúng phiếu công tác chỉ định (chặn đọc chéo phiếu). */
    Optional<SuppliesIssue> findByIdAndWorkOrder_Id(Integer id, Integer workOrderId);
}
