package com.example.m6_thermal_power_plant_api.service.leader.technical_assessment;

import com.example.m6_thermal_power_plant_api.dto.Leader.req.TechnicalAssessmentCreateRequestDto;
import com.example.m6_thermal_power_plant_api.dto.Leader.req.TechnicalAssessmentUpdateRequestDto;
import com.example.m6_thermal_power_plant_api.entity.enums.TechnicalAssessmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ITechnicalAssessmentService {
    List<TechnicalAssessmentUpdateRequestDto> findAll();
    TechnicalAssessmentCreateRequestDto save(
            TechnicalAssessmentCreateRequestDto dto,
            MultipartFile[] imageFiles
    );
    TechnicalAssessmentUpdateRequestDto findByTechnicalCode(String technicalCode);
    TechnicalAssessmentUpdateRequestDto update(TechnicalAssessmentUpdateRequestDto dto, MultipartFile pdfFile);

    TechnicalAssessmentUpdateRequestDto findById(Integer id);

    Page<TechnicalAssessmentUpdateRequestDto> search(
            String technicalCode,
            Integer equipmentId,
            TechnicalAssessmentStatus status,
            Pageable pageable
    );

    TechnicalAssessmentUpdateRequestDto deletePdfAttachment(TechnicalAssessmentUpdateRequestDto dto);
}
