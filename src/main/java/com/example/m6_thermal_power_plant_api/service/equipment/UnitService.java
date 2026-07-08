package com.example.m6_thermal_power_plant_api.service.equipment;

import com.example.m6_thermal_power_plant_api.dto.equipment.request.UnitDTO;
import com.example.m6_thermal_power_plant_api.dto.equipment.response.UnitListDTO;
import com.example.m6_thermal_power_plant_api.entity.Unit;
import com.example.m6_thermal_power_plant_api.repository.equipment.IUnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UnitService implements IUnitService{
    private final IUnitRepository unitRepository;

    @Override
    public UnitListDTO getById(int id) {
        Unit unit= unitRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy đơn vị"));
        return convertDTO(unit);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UnitListDTO> getAll(Pageable pageable) {
       return unitRepository
                .findAll(PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "id")
                ))
                .map(unit -> UnitListDTO.builder()
                        .id(unit.getId())
                        .name(unit.getName())
                        .description(unit.getDescription())
                        .build());
    }

    @Override
    public UnitListDTO createUnit(UnitDTO dto) {
        if(unitRepository.existsByNameIgnoreCase(dto.getName().trim())){
            throw new RuntimeException("Đơn vị đo lường đã tồn tại!");
        }
        Unit unit =Unit.builder()
                .name(dto.getName().trim())
                .description(dto.getDescription())
                .build();
        unit = unitRepository.save(unit);
        return UnitListDTO.builder()
                .id(unit.getId())
                .name(unit.getName())
                .description(unit.getDescription())
                .build();
    }

    @Override
    public UnitListDTO updateUnit(Integer id, UnitDTO dto) {
       Unit unit = unitRepository.findById(id)
               .orElseThrow(()-> new RuntimeException("Đơn vị đo lường không tồn tại!"));
       unit.setName(dto.getName().trim());
       unit.setDescription(dto.getDescription());
       unit= unitRepository.save(unit);
        return UnitListDTO.builder()
                .id(unit.getId())
                .name(unit.getName())
                .description(unit.getDescription())
                .build();
    }

    @Override
    public void deleteUnit(int id) {
        Unit unit = unitRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy đơn vị đo lường "));
        unit.setIsDeleted(true);
        unitRepository.save(unit);
    }

    private UnitListDTO convertDTO(Unit unit) {
        return UnitListDTO.builder()
                .id(unit.getId())
                .name(unit.getName())
                .description(unit.getDescription())
                .build();
    }
}
