package com.example.m6_thermal_power_plant_api.dto.Leader.req;

import com.example.m6_thermal_power_plant_api.entity.enums.TechnicalAssessmentStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TechnicalAssessmentCreateRequestDto {

    private String technicalCode;


    @NotNull(message = "Người đánh giá không được để trống")
    @Valid
    private AccountDto assessor;


    @NotNull(message = "Thiết bị đánh giá không được để trống")
    private Integer equipmentId;


    private String attachmentPath;


    private String imgPath;


    @NotBlank(message = "Kết quả đánh giá không được để trống")
    @Size(
            max = 2000,
            message = "Kết quả đánh giá không được vượt quá 2000 ký tự"
    )
    private String result;


    @NotBlank(message = "Nội dung mô tả không được để trống")
    @Size(
            max = 2000,
            message = "Mô tả không được vượt quá 2000 ký tự"
    )
    private String description;


    private LocalDateTime createdAt;


    @NotNull(message = "Trạng thái không được để trống")
    private TechnicalAssessmentStatus status = TechnicalAssessmentStatus.PENDING;



    public TechnicalAssessmentCreateRequestDto(
            String technicalCode,
            AccountDto assessor,
            Integer equipmentId,
            String imgPath,
            String result,
            String description
    ) {
        this.technicalCode = technicalCode;
        this.assessor = assessor;
        this.equipmentId = equipmentId;
        this.imgPath = imgPath;
        this.result = result;
        this.description = description;
    }
}