package com.example.m6_thermal_power_plant_api.service.leader.spare_parts_issue;

import com.example.m6_thermal_power_plant_api.dto.Leader.req.SparePartsIssueRequestDto;
import com.example.m6_thermal_power_plant_api.entity.enums.SparePartsIssueStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ISparePartsIssueService {
    SparePartsIssueRequestDto save(SparePartsIssueRequestDto sparePartsIssueRequestDto);

    SparePartsIssueRequestDto update(SparePartsIssueRequestDto sparePartsIssueRequestDto);

    SparePartsIssueRequestDto findById(Integer id);

    Page<SparePartsIssueRequestDto> search(
            String keyword,
            SparePartsIssueStatus status,
            Pageable pageable
    );

    SparePartsIssueRequestDto uploadSignedPdf(Integer id, MultipartFile file);
}
