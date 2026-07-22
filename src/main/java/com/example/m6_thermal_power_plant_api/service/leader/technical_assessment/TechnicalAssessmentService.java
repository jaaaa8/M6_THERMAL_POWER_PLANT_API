package com.example.m6_thermal_power_plant_api.service.leader.technical_assessment;

import com.example.m6_thermal_power_plant_api.dto.Leader.req.*;
import com.example.m6_thermal_power_plant_api.dto.file.FileUploadResult;
import com.example.m6_thermal_power_plant_api.entity.Account;
import com.example.m6_thermal_power_plant_api.entity.Equipment;
import com.example.m6_thermal_power_plant_api.entity.TechnicalAssessment;
import com.example.m6_thermal_power_plant_api.entity.enums.TechnicalAssessmentStatus;
import com.example.m6_thermal_power_plant_api.repository.account.IAccountRepository;
import com.example.m6_thermal_power_plant_api.repository.ITechnicalAssessmentRepository;
import com.example.m6_thermal_power_plant_api.repository.equipment.IEquipmentRepository;
import com.example.m6_thermal_power_plant_api.service.util.FileUploadService;
import com.example.m6_thermal_power_plant_api.util.TimeStampCodeGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TechnicalAssessmentService implements ITechnicalAssessmentService {
    private final ITechnicalAssessmentRepository technicalAssessmentRepository;
    private final IAccountRepository accountRepository;
    private final IEquipmentRepository equipmentRepository;
    private final FileUploadService fileUploadService;
    private final ObjectMapper objectMapper;
    public  TechnicalAssessmentService(ITechnicalAssessmentRepository technicalAssessmentRepository,
                                       IAccountRepository accountRepository,
                                       IEquipmentRepository equipmentRepository,
                                       FileUploadService fileUploadService,
                                       ObjectMapper objectMapper) {
        this.accountRepository = accountRepository;
        this.technicalAssessmentRepository = technicalAssessmentRepository;
        this.equipmentRepository = equipmentRepository;
        this.fileUploadService = fileUploadService;
        this.objectMapper = objectMapper;
    }
    @Override
    public List<TechnicalAssessmentUpdateRequestDto> findAll() {
        List<TechnicalAssessment> technicalAssessments = technicalAssessmentRepository.findAll();
        return technicalAssessments.stream()
                .map(ta -> new TechnicalAssessmentUpdateRequestDto(
                        ta.getId(),
                        ta.getTechnicalCode(),
                        getSafeAccountDto(ta.getAssessor()),
                        new EquipmentDto(
                                ta.getEquipment().getId(),
                                ta.getEquipment().getKksCode(),
                                ta.getEquipment().getName(),
                                new SystemDto(
                                        ta.getEquipment().getSystem().getId(),
                                        ta.getEquipment().getSystem().getCode(),
                                        ta.getEquipment().getSystem().getName()
                                )
                        ),
                        ta.getAttachmentPath(),
                        ta.getImgPath() != null
                                ? ta.getImgPath().split(";")
                                : new String[0],
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
        return new AccountDto(assessor.getUsername(), assessor.getEmail(),
                assessor.getEmployee() != null ? new EmployeeDto(
                        assessor.getEmployee().getId(),
                        assessor.getEmployee().getEmployeeCode(),
                        fullName
                ) : null);
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

            try {
                List<FileUploadResult> uploadedImages = fileUploadService.uploadImages(imageFiles);

                String imgPath = uploadedImages.stream()
                        .map(FileUploadResult::secureUrl)
                        .collect(Collectors.joining(";"));

                technicalAssessment.setImgPath(imgPath);

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

        Equipment equipment = equipmentRepository.findById(dto.getEquipmentId())
                .orElseThrow(() -> new IllegalArgumentException("Equipment not found with id: " + dto.getEquipmentId()));

        technicalAssessment.setEquipment(equipment);

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
                new EquipmentDto(
                        technicalAssessment.getEquipment().getId(),
                        technicalAssessment.getEquipment().getKksCode(),
                        technicalAssessment.getEquipment().getName(),
                        new SystemDto(
                                technicalAssessment.getEquipment().getSystem().getId(),
                                technicalAssessment.getEquipment().getSystem().getCode(),
                                technicalAssessment.getEquipment().getSystem().getName()
                        )
                ),
                technicalAssessment.getAttachmentPath(),
                technicalAssessment.getImgPath() != null
                        ? technicalAssessment.getImgPath().split(";")
                        : new String[0],
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

                FileUploadResult uploadResult = fileUploadService.uploadPdf(
                        pdfFile.getBytes(),
                        "technical-assessments",
                        dto.getTechnicalCode()
                );

                dto.setAttachmentPath(
                        uploadResult.secureUrl()
                );
            } else {
                dto.setAttachmentPath(
                        entity.getAttachmentPath()
                );
            }



            entity.setAttachmentPath(dto.getAttachmentPath());
            entity.setStatus(TechnicalAssessmentStatus.COMPLETED);

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
                new EquipmentDto(
                        technicalAssessment.getEquipment().getId(),
                        technicalAssessment.getEquipment().getKksCode(),
                        technicalAssessment.getEquipment().getName(),
                        new SystemDto(
                                technicalAssessment.getEquipment().getSystem().getId(),
                                technicalAssessment.getEquipment().getSystem().getCode(),
                                technicalAssessment.getEquipment().getSystem().getName()
                        )
                ),
                technicalAssessment.getAttachmentPath(),
                technicalAssessment.getImgPath() != null
                        ? technicalAssessment.getImgPath().split(";")
                        : new String[0],
                technicalAssessment.getResult(),
                technicalAssessment.getDescription(),
                technicalAssessment.getCreatedAt(),
                technicalAssessment.getStatus()
        );
    }

    @Override
    public Page<TechnicalAssessmentUpdateRequestDto> search(
            String technicalCode,
            Integer equipmentId,
            TechnicalAssessmentStatus status,
            Pageable pageable
    ) {


        Page<TechnicalAssessment> assessments =
                technicalAssessmentRepository.search(
                        technicalCode,
                        equipmentId,
                        status,
                        pageable
                );


        return assessments.map(
                this::convertToUpdateDto
        );
    }

    @Override
    public TechnicalAssessmentUpdateRequestDto deletePdfAttachment(TechnicalAssessmentUpdateRequestDto dto) {
        TechnicalAssessment entity = technicalAssessmentRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Technical assessment not found for id: " + dto.getId()));

        try {
            if (entity.getAttachmentPublicId() != null && entity.getAttachmentResourceType() != null) {
                fileUploadService.deleteFile(entity.getAttachmentPublicId(), entity.getAttachmentResourceType());
            }
            if (entity.getImgPublicIds() != null && entity.getImgResourceType() != null) {
                String[] imgPublicIds = entity.getImgPublicIds().split(";");
                for (String imgPublicId : imgPublicIds) {
                    fileUploadService.deleteFile(imgPublicId, entity.getImgResourceType());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(
                    "Error deleting PDF attachment",
                    e
            );
        }

        entity.setAttachmentPath(null);
        entity.setAttachmentPublicId(null);
        entity.setAttachmentResourceType(null);
        entity.setImgPublicIds(null);
        entity.setImgPath(null);
        entity.setImgResourceType(null);
        entity.setStatus(TechnicalAssessmentStatus.PENDING);

        technicalAssessmentRepository.save(entity);

        return dto;
    }

    private TechnicalAssessmentUpdateRequestDto convertToUpdateDto(
            TechnicalAssessment entity
    ) {
        TechnicalAssessmentUpdateRequestDto dto =
                new TechnicalAssessmentUpdateRequestDto();

        dto.setId(entity.getId());
        dto.setTechnicalCode(entity.getTechnicalCode());
        dto.setAttachmentPath(entity.getAttachmentPath());
        if(entity.getImgPath() != null && !entity.getImgPath().isEmpty()) {
            dto.setImgPath(entity.getImgPath().split(";"));
        } else {
            dto.setImgPath(new String[0]);
        }
        dto.setResult(entity.getResult());
        dto.setDescription(entity.getDescription());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setStatus(entity.getStatus());
        dto.setEquipment(
                new EquipmentDto(
                        entity.getEquipment().getId(),
                        entity.getEquipment().getKksCode(),
                        entity.getEquipment().getName(),
                        new SystemDto(
                                entity.getEquipment().getSystem().getId(),
                                entity.getEquipment().getSystem().getCode(),
                                entity.getEquipment().getSystem().getName()
                        )
                )
        );

        if (entity.getAssessor() != null) {
            AccountDto accountDto = new AccountDto();

            accountDto.setUsername(entity.getAssessor().getUsername());
            accountDto.setEmail(entity.getAssessor().getEmail());
            accountDto.setEmployee(
                    new  EmployeeDto(
                            entity.getAssessor().getEmployee().getId(),
                            entity.getAssessor().getEmployee().getEmployeeCode(),
                            entity.getAssessor().getEmployee().getFullName()
                    )
            );

            dto.setAssessor(accountDto);
        }

        return dto;
    }
}
