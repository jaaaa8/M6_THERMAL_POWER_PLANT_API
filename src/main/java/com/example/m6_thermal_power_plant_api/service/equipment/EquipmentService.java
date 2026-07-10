package com.example.m6_thermal_power_plant_api.service.equipment;

import com.example.m6_thermal_power_plant_api.dto.equipment.response.ListEquipmentDTO;
import com.example.m6_thermal_power_plant_api.entity.Equipment;
import com.example.m6_thermal_power_plant_api.entity.enums.EquipmentStatus;
import com.example.m6_thermal_power_plant_api.repository.equipment.IEquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EquipmentService implements IEquipmentService {
    private  final IEquipmentRepository equipmentRepository;

    @Override
    public Page<ListEquipmentDTO> getEquipmentList(String kks, String name, Integer typeId, EquipmentStatus status, Pageable pageable) {


        Pageable page= PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC,"id")
        );
        Page<Equipment> equipment= equipmentRepository.getEquipment(
                kks,
                name,
                typeId,
                status,
                page
        );
      return  equipment.map(this ::convertEquipment);
    }

    @Override
    public Page<ListEquipmentDTO> getBySystem(Integer systemId, Pageable pageable) {

        return equipmentRepository
                .findBySystemId(systemId, pageable)
                .map(this::convertEquipment);
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
    private String getFirstImage(String imgPath) {
        if (imgPath == null || imgPath.isBlank()) {
            return null;
        }

        return imgPath.split("\\|")[0];
    }
}
