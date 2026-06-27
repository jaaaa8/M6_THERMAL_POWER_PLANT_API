package com.example.m6_thermal_power_plant_api.service.leader.technical_assessment;

import com.example.m6_thermal_power_plant_api.dto.Leader.req.TechnicalAssessmentRequestDto;
import com.example.m6_thermal_power_plant_api.dto.Leader.res.TechnicalAssessmentResponseDto;
import com.example.m6_thermal_power_plant_api.entity.TechnicalAssessment;
import com.example.m6_thermal_power_plant_api.repository.ITechnicalAssessmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;


import java.util.List;

@Service
public class TechnicalAssessmentService implements ITechnicalAssessmentService {
    private final ITechnicalAssessmentRepository technicalAssessmentRepository;
    public  TechnicalAssessmentService(ITechnicalAssessmentRepository technicalAssessmentRepository) {
        this.technicalAssessmentRepository = technicalAssessmentRepository;
    }
    @Override
    public List<TechnicalAssessmentResponseDto> findAll() {
        return technicalAssessmentRepository.findAll().stream()
                .map(technicalAssessment -> new TechnicalAssessmentResponseDto(
                        technicalAssessment.getTechnicalCode(),
                        technicalAssessment.getAssessor(),
                        technicalAssessment.getAttachmentPath(),
                        technicalAssessment.getImgPath(),
                        technicalAssessment.getResult(),
                        technicalAssessment.getDescription(),
                        technicalAssessment.getCreatedAt()
                ))
                .toList();
    }

    @Override
    public TechnicalAssessmentRequestDto save(TechnicalAssessmentRequestDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("TechnicalAssessmentRequestDto cannot be null");
        }
        TechnicalAssessment technicalAssessment = new TechnicalAssessment();
        technicalAssessment.setTechnicalCode(dto.getTechnicalCode());
        technicalAssessment.setAssessor(dto.getAssessor());
        if(dto.getAttachmentPath() != null && !dto.getAttachmentPath().isEmpty()) {
            technicalAssessment.setAttachmentPath(dto.getAttachmentPath());
        }
        technicalAssessment.setImgPath(dto.getImgPath());
        technicalAssessment.setResult(dto.getResult());
        technicalAssessment.setDescription(dto.getDescription());
        technicalAssessment.setCreatedAt(dto.getCreatedAt());
        technicalAssessmentRepository.save(technicalAssessment);
        return dto;
    }

    @Override
    public TechnicalAssessmentResponseDto findByTechnicalCode(String technicalCode) {
        TechnicalAssessment technicalAssessment = technicalAssessmentRepository.findByTechnicalCode(technicalCode);
        if (technicalAssessment == null) {
            throw new IllegalArgumentException("Technical assessment not found for code: " + technicalCode);
        }
        return new TechnicalAssessmentResponseDto(
                technicalAssessment.getTechnicalCode(),
                technicalAssessment.getAssessor(),
                technicalAssessment.getAttachmentPath(),
                technicalAssessment.getImgPath(),
                technicalAssessment.getResult(),
                technicalAssessment.getDescription(),
                technicalAssessment.getCreatedAt()
        );
    }

    @Override
    public TechnicalAssessmentRequestDto update(
            TechnicalAssessmentRequestDto dto,
            MultipartFile pdfFile) {

        try {

            TechnicalAssessmentResponseDto existing =
                    findByTechnicalCode(dto.getTechnicalCode());

            if (existing == null) {
                throw new RuntimeException(
                        "Technical Assessment not found: "
                                + dto.getTechnicalCode()
                );
            }

            dto.setTechnicalCode(existing.getTechnicalCode());
            dto.setCreatedAt(existing.getCreatedAt());

            if (pdfFile != null && !pdfFile.isEmpty()) {

                String uploadDir =
                        "src/main/resources/pdf/technical-assessment";

                Path uploadPath = Paths.get(uploadDir);

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                String originalName =
                        pdfFile.getOriginalFilename();

                String fileName =
                        UUID.randomUUID() + "_" + originalName;

                Path filePath =
                        uploadPath.resolve(fileName);

                Files.copy(
                        pdfFile.getInputStream(),
                        filePath,
                        StandardCopyOption.REPLACE_EXISTING
                );

                dto.setAttachmentPath(
                        "/pdf/technical-assessment/" + fileName
                );
            } else {
                dto.setAttachmentPath(
                        existing.getAttachmentPath()
                );
            }

            save(dto);

            return dto;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error updating technical assessment",
                    e
            );
        }
    }
}
