package com.example.m6_thermal_power_plant_api.dto.Leader.req;

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
public class TechnicalAssessmentUpdateRequestDto {
    private int id;
    private String technicalCode;
    private int assessorId;
    private String attachmentPath;
    private String imgPath;
    private String result;
    private String description;
    private LocalDateTime createdAt;
    private TechnicalAssessmentStatus status = TechnicalAssessmentStatus.PENDING;

    public TechnicalAssessmentUpdateRequestDto(String technicalCode, int assessorId, String imgPath, String result, String description) {
        this.technicalCode = technicalCode;
        this.assessorId = assessorId;
        this.imgPath = imgPath;
        this.result = result;
        this.description = description;
    }
}
