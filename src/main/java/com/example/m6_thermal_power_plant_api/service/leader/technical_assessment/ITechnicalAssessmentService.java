package com.example.m6_thermal_power_plant_api.service.leader.technical_assessment;

import com.example.m6_thermal_power_plant_api.dto.Leader.req.TechnicalAssessmentRequestDto;
import com.example.m6_thermal_power_plant_api.dto.Leader.res.TechnicalAssessmentResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ITechnicalAssessmentService {
    List<TechnicalAssessmentResponseDto> findAll();
    TechnicalAssessmentRequestDto save(TechnicalAssessmentRequestDto dto);
    TechnicalAssessmentResponseDto findByTechnicalCode(String technicalCode);
    TechnicalAssessmentRequestDto update(TechnicalAssessmentRequestDto dto, MultipartFile pdfFile);
}
