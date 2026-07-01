package com.example.m6_thermal_power_plant_api.service.leader.technical_assessment;

import com.example.m6_thermal_power_plant_api.dto.Leader.req.TechnicalAssessmentCreateRequestDto;
import com.example.m6_thermal_power_plant_api.dto.Leader.req.TechnicalAssessmentUpdateRequestDto;
import com.example.m6_thermal_power_plant_api.dto.Leader.res.TechnicalAssessmentResponseDto;
import com.example.m6_thermal_power_plant_api.entity.Account;
import com.example.m6_thermal_power_plant_api.entity.TechnicalAssessment;
import com.example.m6_thermal_power_plant_api.repository.account.IAccountRepository;
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
    private final IAccountRepository accountRepository;
    public  TechnicalAssessmentService(ITechnicalAssessmentRepository technicalAssessmentRepository,
                                       IAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
        this.technicalAssessmentRepository = technicalAssessmentRepository;
    }
    @Override
    public List<TechnicalAssessmentUpdateRequestDto> findAll() {
        List<TechnicalAssessment> technicalAssessments = technicalAssessmentRepository.findAll();
        return technicalAssessments.stream()
                .map(ta -> new TechnicalAssessmentUpdateRequestDto(
                        ta.getId(),
                        ta.getTechnicalCode(),
                        ta.getAssessor().getId(),
                        ta.getAttachmentPath(),
                        ta.getImgPath(),
                        ta.getResult(),
                        ta.getDescription(),
                        ta.getCreatedAt(),
                        ta.getStatus()
                ))
                .toList();
    }

    @Override
    public TechnicalAssessmentCreateRequestDto save(TechnicalAssessmentCreateRequestDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("TechnicalAssessmentRequestDto cannot be null");
        }
        TechnicalAssessment technicalAssessment = new TechnicalAssessment();
        Account assessor = accountRepository.findById(dto.getAssessorId())
                .orElseThrow(() -> new IllegalArgumentException("Assessor not found with id: " + dto.getAssessorId()));
        technicalAssessment.setTechnicalCode(dto.getTechnicalCode());
        if (assessor != null) {
            technicalAssessment.setAssessor(assessor);
        }
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
    public TechnicalAssessmentUpdateRequestDto findByTechnicalCode(String technicalCode) {
        TechnicalAssessment technicalAssessment = technicalAssessmentRepository.findByTechnicalCode(technicalCode);
        if (technicalAssessment == null) {
            throw new IllegalArgumentException("Technical assessment not found for code: " + technicalCode);
        }
        return new TechnicalAssessmentUpdateRequestDto(
                technicalAssessment.getId(),
                technicalAssessment.getTechnicalCode(),
                technicalAssessment.getAssessor().getId(),
                technicalAssessment.getAttachmentPath(),
                technicalAssessment.getImgPath(),
                technicalAssessment.getResult(),
                technicalAssessment.getDescription(),
                technicalAssessment.getCreatedAt(),
                technicalAssessment.getStatus()
        );
    }

    @Override
    public TechnicalAssessmentUpdateRequestDto update(
            TechnicalAssessmentUpdateRequestDto dto,
            MultipartFile pdfFile) {

        try {
            TechnicalAssessment entity =
                    technicalAssessmentRepository.findById(dto.getId())
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Technical assessment not found"
                                    ));

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
                        entity.getAttachmentPath()
                );
            }



            entity.setAttachmentPath(dto.getAttachmentPath());

            technicalAssessmentRepository.save(entity);
            return dto;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error updating technical assessment",
                    e
            );
        }
    }

    @Override
    public TechnicalAssessmentUpdateRequestDto findById(Integer id) {
        TechnicalAssessment technicalAssessment = technicalAssessmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Technical assessment not found for id: " + id));
        return new TechnicalAssessmentUpdateRequestDto(
                technicalAssessment.getId(),
                technicalAssessment.getTechnicalCode(),
                technicalAssessment.getAssessor().getId(),
                technicalAssessment.getAttachmentPath(),
                technicalAssessment.getImgPath(),
                technicalAssessment.getResult(),
                technicalAssessment.getDescription(),
                technicalAssessment.getCreatedAt(),
                technicalAssessment.getStatus()
        );
    }
}
