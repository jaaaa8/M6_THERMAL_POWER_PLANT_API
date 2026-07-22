package com.example.m6_thermal_power_plant_api.service.equipment;

import com.example.m6_thermal_power_plant_api.dto.equipment.request.ParameterCatalogDTO;
import com.example.m6_thermal_power_plant_api.dto.equipment.response.UnitListDTO;
import com.example.m6_thermal_power_plant_api.entity.ParameterCatalog;
import com.example.m6_thermal_power_plant_api.entity.Unit;
import com.example.m6_thermal_power_plant_api.repository.equipment.IParameterCatalogRepository;
import com.example.m6_thermal_power_plant_api.repository.equipment.IUnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParameterCatalogService implements IParameterCatalogService{
    private final IParameterCatalogRepository parameterCatalogRepository;
    private final IUnitRepository unitRepository;

    private ParameterCatalogDTO convertDTO(ParameterCatalog entity) {
        return ParameterCatalogDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .units(
                        entity.getUnits()
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
    public Page<ParameterCatalogDTO> getAll(Pageable pageable) {

        return parameterCatalogRepository
                .findAll(PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "id")
                ))
                .map(this::convertDTO);
    }

    @Override
    public ParameterCatalogDTO getById(Integer id) {

        ParameterCatalog entity = parameterCatalogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Parameter catalog not found"));

        return convertDTO(entity);
    }
    @Override
    public ParameterCatalogDTO create(ParameterCatalogDTO dto) {
            ParameterCatalog entity = new ParameterCatalog();

            entity.setName(dto.getName());
            entity.setDescription(dto.getDescription());

            List<Integer> unitIds = dto.getUnits()
                    .stream()
                    .map(UnitListDTO::getId)
                    .toList();

            entity.setUnits(unitRepository.findAllById(unitIds));

            return convertDTO(parameterCatalogRepository.save(entity));
    }

    @Override
    public ParameterCatalogDTO update(Integer id, ParameterCatalogDTO dto) {
        ParameterCatalog entity = parameterCatalogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Parameter catalog not found"));

        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());

        List<Integer> unitIds = dto.getUnits()
                .stream()
                .map(UnitListDTO::getId)
                .toList();

        entity.setUnits(unitRepository.findAllById(unitIds));

        return convertDTO(parameterCatalogRepository.save(entity));
    }

    @Override
    public void delete(Integer id) {
        ParameterCatalog entity = parameterCatalogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Parameter catalog not found"));

        parameterCatalogRepository.delete(entity);
    }
}
