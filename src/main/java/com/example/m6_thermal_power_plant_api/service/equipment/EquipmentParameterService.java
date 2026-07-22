package com.example.m6_thermal_power_plant_api.service.equipment;

import com.example.m6_thermal_power_plant_api.dto.equipment.request.ParameterDTO;
import com.example.m6_thermal_power_plant_api.dto.equipment.response.UnitListDTO;
import com.example.m6_thermal_power_plant_api.entity.Equipment;
import com.example.m6_thermal_power_plant_api.entity.EquipmentParameter;
import com.example.m6_thermal_power_plant_api.entity.ParameterCatalog;
import com.example.m6_thermal_power_plant_api.entity.Unit;
import com.example.m6_thermal_power_plant_api.repository.equipment.IEquipmentParameterRepository;
import com.example.m6_thermal_power_plant_api.repository.equipment.IEquipmentRepository;
import com.example.m6_thermal_power_plant_api.repository.equipment.IParameterCatalogRepository;
import com.example.m6_thermal_power_plant_api.repository.equipment.IUnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EquipmentParameterService implements IEquipmentParameterService{
    private  final IEquipmentParameterRepository parameterRepository;
    private  final IEquipmentRepository equipmentRepository;
    private  final IParameterCatalogRepository parameterCatalogRepository;
    private  final IUnitRepository unitRepository;
    @Override
    public List<ParameterDTO> getByEquipment(Integer equipmentId) {
        return parameterRepository.findByEquipmentId(equipmentId)
                .stream()
                .map(this:: convertDTO)
                .toList();
    }

    private ParameterDTO convertDTO(EquipmentParameter equipmentParameter) {
        return ParameterDTO.builder()
                .id(equipmentParameter.getId())
                .equipmentId(equipmentParameter.getEquipment().getId())
                .parameterId(equipmentParameter.getParameter().getId())
                .name(equipmentParameter.getParameter().getName())
                .value(equipmentParameter.getValue())
                .description(equipmentParameter.getDescription())
                .unit(
                        equipmentParameter.getParameter()
                                .getUnits()
                                .stream()
                                .map(unit -> UnitListDTO.builder()
                                        .id(unit.getId())
                                        .name(unit.getName())
                                        .description(unit.getDescription())
                                        .build())
                                .toList()
                )
                .build();
    }

    @Override
    public   List<ParameterDTO> create(  List<ParameterDTO> dtos) {
        List<ParameterDTO> result = new ArrayList<>();

        for (ParameterDTO dto : dtos) {

            Equipment equipment = equipmentRepository.findById(dto.getEquipmentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thiết bị"));

            ParameterCatalog parameter = parameterCatalogRepository.findById(dto.getParameterId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông số"));

            EquipmentParameter entity;

            // update
            if (dto.getId() != null) {

                entity = parameterRepository.findById(dto.getId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy thông số kỹ thuật"));

            } else {

                entity = new EquipmentParameter();

            }

            entity.setEquipment(equipment);
            entity.setParameter(parameter);
            entity.setValue(dto.getValue());
            entity.setDescription(dto.getDescription());

            result.add(convertDTO(parameterRepository.save(entity)));

        }

        return result;
    }

    @Override
    public ParameterDTO update(Integer id, ParameterDTO dto) {
        EquipmentParameter entity =
                parameterRepository.findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Không tìm thấy thông số"
                                )
                        );
        ParameterCatalog parameter = parameterCatalogRepository
                .findById(dto.getParameterId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông số"));

        entity.setValue(dto.getValue());
        entity.setParameter(parameter);
        entity.setDescription(dto.getDescription());


        return convertDTO(
                parameterRepository.save(entity)
        );
    }

    @Override
    public void delete(Integer id) {
        EquipmentParameter entity =
                parameterRepository.findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Không tìm thấy thông số"
                                )
                        );


        entity.setIsDeleted(true);
        entity.setDeletedAt(LocalDateTime.now());
        parameterRepository.save(entity);

    }
}
