package com.example.m6_thermal_power_plant_api.service.equipment;

import com.example.m6_thermal_power_plant_api.dto.equipment.request.AddEquipmentDTO;
import com.example.m6_thermal_power_plant_api.dto.equipment.request.EquipmentUpdateDTO;
import com.example.m6_thermal_power_plant_api.dto.equipment.response.*;
import com.example.m6_thermal_power_plant_api.dto.file.FileUploadResult;
import com.example.m6_thermal_power_plant_api.entity.*;
import com.example.m6_thermal_power_plant_api.entity.enums.EquipmentStatus;
import com.example.m6_thermal_power_plant_api.exception.ObjectNotFoundException;
import com.example.m6_thermal_power_plant_api.repository.ILubricationHistoryRepository;
import com.example.m6_thermal_power_plant_api.repository.ILubricationPlanRepository;
import com.example.m6_thermal_power_plant_api.repository.IRepairHistoryRepository;
import com.example.m6_thermal_power_plant_api.repository.equipment.IEquipmentRepository;
import com.example.m6_thermal_power_plant_api.repository.equipment.IEquipmentSystemRepository;
import com.example.m6_thermal_power_plant_api.repository.equipment.IEquipmentTypeRepository;
import com.example.m6_thermal_power_plant_api.service.util.FileUploadService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EquipmentService implements IEquipmentService {
    private  final IEquipmentRepository equipmentRepository;
    private  final IEquipmentTypeRepository equipmentTypeRepository;
    private  final IEquipmentSystemRepository equipmentSystemRepository;
    private  final FileUploadService fileUploadService;
    private  final ObjectMapper objectMapper;
    private  final IRepairHistoryRepository repairHistoryRepository;
    private  final ILubricationHistoryRepository lubricationHistoryRepository;
    @Override
    public Page<ListEquipmentDTO> getEquipmentList(Integer systemId,String kks, String name, Integer typeId, String status, Pageable pageable) {

        Pageable page= PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC,"id")
        );
        Page<Equipment> equipment= equipmentRepository.getEquipment(
                systemId,
                kks,
                name,
                typeId,
                status,
                page
        );
      return  equipment.map(this ::convertEquipment);
    }


    public Page<ListEquipmentDTO> getBySystem(Integer systemId, Pageable pageable) {

        return equipmentRepository
                .findBySystemId(systemId, pageable)
                .map(this::convertEquipment);
    }

    // Add equipment
    @Override
    public EquipmentAddDTO addEquipment(Integer systemId, AddEquipmentDTO dto, List<MultipartFile> images) throws IOException {
        EquipmentSystem system= validateSystem(systemId);
        EquipmentType type= validateEquipmentType(dto.getEquipmentTypeId());
        String kksCode= generateKks(system,dto.getName());
        String imagePath= uploadImages(images);
        Equipment equipment = Equipment.builder()
                .kksCode(kksCode)
                .name(dto.getName())
                .system(system)
                .equipmentType(type)
                .status(dto.getStatus())
                .manufacturer(dto.getManufacturer())
                .model(dto.getModel())
                .installationYear(dto.getInstallationYear())
                .description(dto.getDescription())
                .imgPath(imagePath)
                .build();

        equipmentRepository.save(equipment);
        return EquipmentAddDTO.builder()
                .id(equipment.getId())
                .kksCode(equipment.getKksCode())
                .name(equipment.getName())
                .build();
    }

    @Override
    public EquipmentDetailDTO getEquipmentDetail(Integer id) {

        Equipment equipment = equipmentRepository.findWithDetailById(id)
                .orElseThrow(() ->
                        new ObjectNotFoundException("Không tìm thấy thiết bị"));

        return convertToDetailDTO(
                equipment
        );
    }

    @Override
    public EquipmentDetailDTO update(
            Integer id,
            EquipmentUpdateDTO dto,
            List<MultipartFile> images
    ) throws IOException {

        Equipment equipment =
                equipmentRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException("Không tìm thấy thiết bị"));

        EquipmentType type =
                validateEquipmentType(dto.getEquipmentTypeId());

        equipment.setName(dto.getName());
        equipment.setEquipmentType(type);
        equipment.setStatus(dto.getStatus());
        equipment.setManufacturer(dto.getManufacturer());
        equipment.setModel(dto.getModel());
        equipment.setInstallationYear(dto.getInstallationYear());
        equipment.setDescription(dto.getDescription());

        equipment.setImgPath(
                updateImages(
                        dto.getImageUrls(),
                        images
                )
        );

        equipmentRepository.save(equipment);

        return getEquipmentDetail(id);
    }


    @Override
    @Transactional
    public void deleteById(Integer id) {
        Equipment equipment= equipmentRepository.findById(id).orElseThrow(()-> new ObjectNotFoundException("Không tìm thấy thiết bị."));

        equipment.setStatus(EquipmentStatus.RETIRED);
        equipment.setIsDeleted(true);
        equipment.setDeletedAt(LocalDateTime.now());
        equipmentRepository.save(equipment);
    }

    private EquipmentDetailDTO convertToDetailDTO(Equipment equipment
                                                  ) {
        List<EquipmentParamerDTO> technicalParameters =
                equipment.getParameters()
                        .stream()
                        .map(this::convertParameter)
                        .toList();
        return EquipmentDetailDTO.builder()
                .id(equipment.getId())
                .kksCode(equipment.getKksCode())
                .name(equipment.getName())

                .systemId(equipment.getSystem().getId())
                .systemName(equipment.getSystem().getName())

                .equipmentTypeId(equipment.getEquipmentType().getId())
                .equipmentTypeName(equipment.getEquipmentType().getName())

                .status(equipment.getStatus())

                .installationYear(equipment.getInstallationYear())
                .manufacturer(equipment.getManufacturer())
                .model(equipment.getModel())
                .description(equipment.getDescription())

                .imageUrls(getImageUrls(equipment.getImgPath()))
                .build();
    }

    private EquipmentParamerDTO convertParameter(EquipmentParameter parameter) {
        List<UnitListDTO> units =
                parameter.getParameter()
                        .getUnits()
                        .stream()
                        .map(unit -> UnitListDTO.builder()
                                .id(unit.getId())
                                .name(unit.getName())
                                .description(unit.getDescription())
                                .build())
                        .toList();

        return EquipmentParamerDTO.builder()
                .id(parameter.getId())
                .parameterId(parameter.getParameter().getId())
                .name(parameter.getParameter().getName())
                .value(parameter.getValue())
                .description(parameter.getDescription())
                .units(units)
                .build();
    }

    // Hàm convert ảnh
    private List<String> getImageUrls(String imgPath) {

        if (imgPath == null || imgPath.isBlank()) {
            return Collections.emptyList();
        }

        try {
            return objectMapper.readValue(
                    imgPath,
                    new TypeReference<List<String>>() {}
            );
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private ListEquipmentDTO convertEquipment(Equipment equipment) {

        return ListEquipmentDTO.builder()
                .id(equipment.getId())
                .imageUrl(getFirstImage(equipment.getImgPath()))
                .kksCode(equipment.getKksCode())
                .name(equipment.getName())
                .equipmentType(
                        equipment.getEquipmentType() != null
                        ? equipment.getEquipmentType().getName() : null
                )
                .equipmentStatus(equipment.getStatus())
                .build();
    }

    // Lấy ảnh làm avatar
    private String getFirstImage(String imgPath) {

        List<String> images = getImageUrls(imgPath);

        return images.isEmpty() ? null : images.get(0);
    }

    private EquipmentSystem validateSystem(Integer systemId){
        return equipmentSystemRepository.findById(systemId).orElseThrow(()->
                new IllegalArgumentException("Hệ thống không tồn tại !"));
    }

    private EquipmentType validateEquipmentType(Integer typeId){

        return equipmentTypeRepository.findById(typeId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Loại thiết bị không tồn tại."));
    }


    // Viết tắt
    private String getAbbreviation(String text){

        return Arrays.stream(text.trim().split("\\s+"))
                .filter(s -> !s.isBlank())
                .map(s -> String.valueOf(
                        Character.toUpperCase(s.charAt(0))
                ))
                .collect(Collectors.joining());

    }


    // Sinh mã KKS
    private String generateKks(
            EquipmentSystem system,
            String equipmentName){

        String equipmentPrefix =
                "TB" + getAbbreviation(equipmentName);

        String systemPrefix =
                getAbbreviation(system.getName());

        String prefix =
                equipmentPrefix + "-" + systemPrefix + "-";

        List<Equipment> latest =
                equipmentRepository.findLatestEquipmentByPrefix(
                        system.getId(),
                        prefix,
                        PageRequest.of(0,1)
                );

        int next = 1;

        if(!latest.isEmpty()){

            String lastKks =
                    latest.get(0).getKksCode();

            String lastNumber =
                    lastKks.substring(
                            lastKks.lastIndexOf("-")+1
                    );

            next = Integer.parseInt(lastNumber)+1;

        }

        return prefix + String.format("%03d",next);

    }

    private String uploadImages(List<MultipartFile> images)
            throws IOException {

        if (images == null || images.isEmpty()) {
            return null;
        }

        List<String> urls = new ArrayList<>();

        for (MultipartFile image : images) {

            FileUploadResult result =
                    fileUploadService.uploadImage(image);

            urls.add(result.secureUrl());
        }

        return objectMapper.writeValueAsString(urls);
    }


    private String updateImages(
            List<String> remainImages,
            List<MultipartFile> newImages
    ) throws IOException {

        List<String> result = new ArrayList<>();

        // giữ ảnh cũ
        if (remainImages != null) {
            result.addAll(remainImages);
        }

        // upload thêm
        if (newImages != null) {

            for (MultipartFile file : newImages) {

                if (file.isEmpty()) continue;

                FileUploadResult upload =
                        fileUploadService.uploadImage(file);

                result.add(upload.secureUrl());
            }
        }

        return objectMapper.writeValueAsString(result);
    }
}
