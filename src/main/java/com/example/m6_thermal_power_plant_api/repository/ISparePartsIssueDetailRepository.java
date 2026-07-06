package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.SparePartsIssueDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ISparePartsIssueDetailRepository extends JpaRepository<SparePartsIssueDetail, Integer> {

    /** Các dòng chi tiết của một phiếu cấp vật tư. */
    List<SparePartsIssueDetail> findByIssue_Id(Integer issueId);
}
