package com.example.m6_thermal_power_plant_api.service.leader.technical_assessment;

import com.example.m6_thermal_power_plant_api.dto.Leader.req.AccountDto;
import com.example.m6_thermal_power_plant_api.dto.Leader.req.TechnicalAssessmentCreateRequestDto;
import com.example.m6_thermal_power_plant_api.dto.Leader.req.TechnicalAssessmentUpdateRequestDto;
import com.example.m6_thermal_power_plant_api.dto.Leader.res.TechnicalAssessmentResponseDto;
import com.example.m6_thermal_power_plant_api.dto.accounts.AccountDTO;
import com.example.m6_thermal_power_plant_api.entity.Account;
import com.example.m6_thermal_power_plant_api.entity.TechnicalAssessment;
import com.example.m6_thermal_power_plant_api.repository.account.IAccountRepository;
import com.example.m6_thermal_power_plant_api.repository.ITechnicalAssessmentRepository;
import com.example.m6_thermal_power_plant_api.util.TimeStampCodeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
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
                        getSafeAccountDto(ta.getAssessor()),
                        ta.getAttachmentPath(),
                        ta.getImgPath(),
                        ta.getResult(),
                        ta.getDescription(),
                        ta.getCreatedAt(),
                        ta.getStatus()
                ))
                .toList();
    }

    private AccountDto getSafeAccountDto(Account assessor) {
        if (assessor == null) return null;
        String fullName = assessor.getUsername();
        try {
            if (assessor.getEmployee() != null) {
                fullName = assessor.getEmployee().getFullName();
            }
        } catch (jakarta.persistence.EntityNotFoundException e) {
            fullName = assessor.getUsername() + " (Đã xóa)";
        }
        return new AccountDto(assessor.getUsername(), assessor.getEmail(), fullName);
    }

    @Override
    public TechnicalAssessmentCreateRequestDto save(
            TechnicalAssessmentCreateRequestDto dto,
            MultipartFile[] imageFiles) {

        if (dto == null) {
            throw new IllegalArgumentException(
                    "TechnicalAssessmentRequestDto cannot be null"
            );
        }

        TechnicalAssessment technicalAssessment =
                new TechnicalAssessment();

        Account assessor =
                accountRepository.findByUsername(dto.getAssessor().getUsername())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Assessor not found with username: "
                                                + dto.getAssessor().getUsername()
                                ));

        technicalAssessment.setAssessor(
                assessor
        );
        String technicalCode = TimeStampCodeGenerator.generate(TechnicalAssessment.class);
        technicalAssessment.setTechnicalCode(technicalCode);

        if (dto.getAttachmentPath() != null
                && !dto.getAttachmentPath().isEmpty()) {

            technicalAssessment.setAttachmentPath(
                    dto.getAttachmentPath()
            );
        }

        // Upload nhiều ảnh
        if (imageFiles != null && imageFiles.length > 0) {

            String uploadDir =
                    "src/main/resources/img/technical-assessment";

            Path uploadPath =
                    Paths.get(uploadDir);

            try {

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                List<String> imagePaths = new ArrayList<>();

                int index = 1;

                for (MultipartFile file : imageFiles) {

                    if (file.isEmpty()) {
                        continue;
                    }

                    String originalFileName = file.getOriginalFilename();

                    String extension = "";

                    if (originalFileName != null
                            && originalFileName.contains(".")) {

                        extension = originalFileName.substring(
                                originalFileName.lastIndexOf(".")
                        );
                    }

                    String fileName = String.format(
                            "%s-%03d%s",
                            technicalCode,
                            index++,
                            extension
                    );

                    Path filePath = uploadPath.resolve(fileName);

                    Files.copy(
                            file.getInputStream(),
                            filePath,
                            StandardCopyOption.REPLACE_EXISTING
                    );

                    imagePaths.add(
                            "/img/technical-assessment/" + fileName
                    );
                }

                technicalAssessment.setImgPath(
                        String.join(";", imagePaths)
                );

            } catch (Exception e) {
                throw new RuntimeException(
                        "Upload image failed",
                        e
                );
            }
        }

        technicalAssessment.setResult(
                dto.getResult()
        );

        technicalAssessment.setDescription(
                dto.getDescription()
        );

        technicalAssessment.setCreatedAt(
                dto.getCreatedAt()
        );

        technicalAssessment.setStatus(
                dto.getStatus()
        );

        TechnicalAssessment saved =
                technicalAssessmentRepository.save(technicalAssessment);

        TechnicalAssessmentCreateRequestDto responseDto =
                new TechnicalAssessmentCreateRequestDto();

        responseDto.setTechnicalCode(saved.getTechnicalCode());
        responseDto.setAssessor(dto.getAssessor());
        responseDto.setResult(saved.getResult());
        responseDto.setDescription(saved.getDescription());
        responseDto.setStatus(saved.getStatus());
        responseDto.setCreatedAt(saved.getCreatedAt());

        return responseDto;
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
                getSafeAccountDto(technicalAssessment.getAssessor()),
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
                String fileName = pdfFile.getOriginalFilename();

                assert fileName != null;
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
                getSafeAccountDto(technicalAssessment.getAssessor()),
                technicalAssessment.getAttachmentPath(),
                technicalAssessment.getImgPath(),
                technicalAssessment.getResult(),
                technicalAssessment.getDescription(),
                technicalAssessment.getCreatedAt(),
                technicalAssessment.getStatus()
        );
    }
}
