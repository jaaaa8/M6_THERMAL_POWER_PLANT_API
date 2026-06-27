package com.example.m6_thermal_power_plant_api.dto.Leader.req;

import com.example.m6_thermal_power_plant_api.entity.Account;
import com.example.m6_thermal_power_plant_api.entity.enums.TechnicalAssessmentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TechnicalAssessmentRequestDto {
    private String technicalCode;
    private Account assessor;
    private String attachmentPath;
    private String imgPath;
    private String result;
    private String description;
    private LocalDateTime createdAt;
    private TechnicalAssessmentStatus status = TechnicalAssessmentStatus.PENDING;

    public TechnicalAssessmentRequestDto(String technicalCode, Account assessor, String imgPath, String result, String description) {
        this.technicalCode = technicalCode;
        this.assessor = assessor;
        this.imgPath = imgPath;
        this.result = result;
        this.description = description;
    }
}
