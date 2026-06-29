package com.example.m6_thermal_power_plant_api.service.leader.technical_assessment;

import com.example.m6_thermal_power_plant_api.dto.Leader.req.TechnicalAssessmentCreateRequestDto;
import com.example.m6_thermal_power_plant_api.dto.Leader.req.TechnicalAssessmentUpdateRequestDto;
import com.example.m6_thermal_power_plant_api.dto.Leader.res.TechnicalAssessmentResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ITechnicalAssessmentService {
    List<TechnicalAssessmentUpdateRequestDto> findAll();
    TechnicalAssessmentCreateRequestDto save(TechnicalAssessmentCreateRequestDto dto);
    TechnicalAssessmentUpdateRequestDto findByTechnicalCode(String technicalCode);
    TechnicalAssessmentUpdateRequestDto update(TechnicalAssessmentUpdateRequestDto dto, MultipartFile pdfFile);

    TechnicalAssessmentUpdateRequestDto findById(Integer id);
}
