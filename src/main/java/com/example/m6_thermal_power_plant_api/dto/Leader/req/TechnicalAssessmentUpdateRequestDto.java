package com.example.m6_thermal_power_plant_api.dto.Leader.req;

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
    private AccountDto assessor;
    private EquipmentDto equipment;
    private String attachmentPath;
    private String[] imgPath;
    private String result;
    private String description;
    private LocalDateTime createdAt;
    private TechnicalAssessmentStatus status = TechnicalAssessmentStatus.PENDING;

    public TechnicalAssessmentUpdateRequestDto(String technicalCode, AccountDto assessor, EquipmentDto equipment, String[] imgPath,
                                                String attachmentPath,
                                               String result, String description) {
        this.technicalCode = technicalCode;
        this.assessor = assessor;
        this.equipment = equipment;
        this.imgPath = imgPath;
        this.attachmentPath = attachmentPath;
        this.result = result;
        this.description = description;
    }
}
