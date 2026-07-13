package com.example.m6_thermal_power_plant_api.service.consumable;

import com.example.m6_thermal_power_plant_api.dto.consumables.ConsumableDTO;
import com.example.m6_thermal_power_plant_api.entity.Consumable;
import com.example.m6_thermal_power_plant_api.entity.Unit;
import com.example.m6_thermal_power_plant_api.entity.enums.PartStatus;
import com.example.m6_thermal_power_plant_api.repository.IConsumableRepository;
import com.example.m6_thermal_power_plant_api.repository.equipment.IUnitRepository;
import com.example.m6_thermal_power_plant_api.service.soft_delete.SoftDeleteCascadeService;
import com.example.m6_thermal_power_plant_api.service.util.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.example.m6_thermal_power_plant_api.util.TimeStampCodeGenerator;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ConsumableService implements IConsumableService{

    private final IConsumableRepository consumableRepository;
    private final IUnitRepository unitRepository;
    private final SoftDeleteCascadeService softDeleteCascadeService;
    private final FileUploadService fileUploadService;


    @Override
    public ConsumableDTO create(ConsumableDTO dto) {
        String code = normalize(dto.getConsumableCode());

        if(code == null || code.isBlank()){
            code = TimeStampCodeGenerator.generate(Consumable.class);
        }
        if(consumableRepository.existsByConsumableCode(code)){
              throw new IllegalStateException("Mã vật tư tiêu hao đã tồn tại");
        }

        Consumable consumable = toEntity(dto);
        consumable.setId(null);
        consumable.setConsumableCode(code);
        if(consumable.getStatus() == null){
            consumable.setStatus(PartStatus.ACTIVE);
        }
        return toDto(consumableRepository.save(consumable));
    }

    @Override
    public ConsumableDTO update(Integer id, ConsumableDTO dto) {
        Consumable consumable = consumableRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Không tìm thấy vật tư tiêu hao")
        );

        String code = normalize(dto.getConsumableCode());
        if (consumableRepository.existsByConsumableCodeAndIdNot(code, id)){
            throw new IllegalStateException("Mã vật tư tiêu hao đã tồn tại");
        }

        Unit unit = null;
        if (dto.getUnitId() != null) {
            unit = unitRepository.findById(dto.getUnitId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn vị tính với id = " + dto.getUnitId()));
        }

        consumable.setConsumableCode(code);
        consumable.setName(dto.getName());
        consumable.setPrice(dto.getPrice());
        consumable.setImgPath(dto.getImgPath());
        consumable.setManufacturer(dto.getManufacturer());
        consumable.setImgPath(dto.getImgPath());
        consumable.setUnit(unit);
        consumable.setStatus(dto.getStatus() != null ? dto.getStatus() : consumable.getStatus());

        return toDto(consumableRepository.save(consumable));
    }

    @Override
    @Transactional(readOnly = true)
    public ConsumableDTO getById(Integer id) {
        return consumableRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(
                () -> new IllegalArgumentException("Không tìm thấy vật tư tiêu hao với id = " + id)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ConsumableDTO getByCode(String code) {
        String normalizeCode = normalize(code);
        return consumableRepository.findByConsumableCodeIgnoreCase(normalizeCode)
                .map(this::toDto)
                .orElseThrow(
                        () -> new IllegalArgumentException("Không tìm thấy vật tư tiêu hao với mã = " + normalizeCode)
                );
    }

    @Override
    public Page<ConsumableDTO> search(String code, String name, String manufacturer, BigDecimal price, PartStatus status, Pageable pageable) {
        return consumableRepository.searchByFields(
                normalize(code),
                normalize(name),
                normalize(manufacturer),
                price,
                status,
                pageable
        ).map(this::toDto);
    }

    @Override
    public void delete(Integer id) {
        Consumable consumable = consumableRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vật tư tiêu hao với id = " + id));

        // Tu dong don dep anh tren Cloudinary truoc khi xoa mem
        if (consumable.getImgPath() != null && !consumable.getImgPath().isBlank()) {
            String[] urls = consumable.getImgPath().split("[|;]");
            for (String url : urls) {
                String trimmedUrl = url.trim();
                if (!trimmedUrl.isEmpty()) {
                    String publicId = FileUploadService.extractPublicIdFromUrl(trimmedUrl);
                    if (publicId != null) {
                        try {
                            fileUploadService.deleteFile(publicId, "image");
                        } catch (Exception e) {
                            log.error("Loi khi xoa anh tren Cloudinary cho vat tu tieu hao id=" + id + ", url=" + trimmedUrl, e);
                        }
                    }
                }
            }
        }

        softDeleteCascadeService.softDelete(consumable);
    }

    private String normalize(String value){
        return value == null ? null : value.trim();
    }

    private Consumable toEntity(ConsumableDTO consumableDTO){
        Unit unit = null;
        if(consumableDTO.getUnitId() != null){
            unit = unitRepository.findById(consumableDTO.getUnitId()).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn vị với id = " + consumableDTO.getUnitId()));
        }

        return Consumable.builder()
                .consumableCode(consumableDTO.getConsumableCode())
                .name(consumableDTO.getName())
                .price(consumableDTO.getPrice())
                .manufacturer(consumableDTO.getManufacturer())
                .imgPath(consumableDTO.getImgPath())
                .status(consumableDTO.getStatus() != null ? consumableDTO.getStatus() : PartStatus.ACTIVE)
                .unit(unit)
                .build();
    }

    private ConsumableDTO toDto(Consumable consumable){
        return ConsumableDTO.builder()
                .id(consumable.getId())
                .consumableCode(consumable.getConsumableCode())
                .name(consumable.getName())
                .price(consumable.getPrice())
                .manufacturer(consumable.getManufacturer())
                .imgPath(consumable.getImgPath())
                .unitId(consumable.getUnit() != null ? consumable.getUnit().getId() : null)
                .unitName(consumable.getUnit() != null ? consumable.getUnit().getName() : null)
                .status(consumable.getStatus())
                .build();
    }
}
