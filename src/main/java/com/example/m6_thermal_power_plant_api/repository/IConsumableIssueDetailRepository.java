package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.ConsumableIssueDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IConsumableIssueDetailRepository extends JpaRepository<ConsumableIssueDetail, Integer> {

    /** Các dòng chi tiết của một phiếu cấp vật tư. */
    List<ConsumableIssueDetail> findByIssue_Id(Integer issueId);
}
