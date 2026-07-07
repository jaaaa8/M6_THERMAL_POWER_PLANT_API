package com.example.m6_thermal_power_plant_api.service.leader.spare_parts_issue;

import com.example.m6_thermal_power_plant_api.dto.Leader.req.SparePartsIssueRequestDto;

import java.util.List;

public interface ISparePartsIssueService {
    List<SparePartsIssueRequestDto> findAll();
    SparePartsIssueRequestDto save(SparePartsIssueRequestDto sparePartsIssueRequestDto);
    SparePartsIssueRequestDto update(SparePartsIssueRequestDto sparePartsIssueRequestDto);
    SparePartsIssueRequestDto findById(Integer id);
}
