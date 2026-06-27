package com.example.m6_thermal_power_plant_api.dto.Leader.res;

import com.example.m6_thermal_power_plant_api.entity.Account;
import com.example.m6_thermal_power_plant_api.entity.enums.TechnicalAssessmentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TechnicalAssessmentResponseDto {
    private String technicalCode;
    private Account assessor;
    private String result;
    private String attachmentPath;
    private String imgPath;
    private String resultDescription;
    private String description;
    private LocalDateTime createdAt;
    private TechnicalAssessmentStatus status;
}
