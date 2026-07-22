package com.example.m6_thermal_power_plant_api.service.leader.lubrication_plan;

import com.example.m6_thermal_power_plant_api.dto.Leader.req.ConsumableDto;
import com.example.m6_thermal_power_plant_api.dto.Leader.req.EquipmentDto;
import com.example.m6_thermal_power_plant_api.dto.Leader.req.LubricationPlanDto;
import com.example.m6_thermal_power_plant_api.dto.Leader.req.SystemDto;
import com.example.m6_thermal_power_plant_api.entity.LubricationPlan;
import com.example.m6_thermal_power_plant_api.entity.enums.LubricationStatus;
import com.example.m6_thermal_power_plant_api.repository.IConsumableRepository;
import com.example.m6_thermal_power_plant_api.repository.ILubricationPlanRepository;
import com.example.m6_thermal_power_plant_api.repository.equipment.IEquipmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class LubricationPlanService implements ILubricationPlanService {
    private final ILubricationPlanRepository lubricationPlanRepository;
    private final IEquipmentRepository equipmentRepository;
    private final IConsumableRepository consumableRepository;

    public LubricationPlanService(
            ILubricationPlanRepository lubricationPlanRepository,
            IEquipmentRepository equipmentRepository,
            IConsumableRepository consumableRepository
    ) {
        this.lubricationPlanRepository = lubricationPlanRepository;
        this.equipmentRepository = equipmentRepository;
        this.consumableRepository = consumableRepository;
    }

    @Override
    public Page<LubricationPlanDto> search(
            String keyword,
            LubricationStatus status,
            Pageable pageable
    ) {

        return lubricationPlanRepository
                .search(keyword, status, pageable)
                .map(this::convertToDto);
    }

    private LubricationPlanDto convertToDto(
            LubricationPlan entity
    ) {

        EquipmentDto equipmentDto = null;

        if (entity.getEquipment() != null) {

            equipmentDto = new EquipmentDto(
                    entity.getEquipment().getId(),
                    entity.getEquipment().getKksCode(),
                    entity.getEquipment().getName(),
                    new SystemDto(
                            entity.getEquipment()
                                    .getSystem()
                                    .getId(),
                            entity.getEquipment()
                                    .getSystem()
                                    .getCode(),
                            entity.getEquipment()
                                    .getSystem()
                                    .getName()
                    )
            );
        }

        ConsumableDto consumableDto = null;

        if (entity.getConsumable() != null) {

            consumableDto = new ConsumableDto(
                    entity.getConsumable().getId(),
                    entity.getConsumable().getConsumableCode(),
                    entity.getConsumable().getName(),
                    entity.getConsumable().getImgPath(),
                    entity.getConsumable().getUnit().getName(),
                    entity.getConsumable().getStatus()
            );
        }

        return new LubricationPlanDto(
                entity.getId(),
                entity.getLubricationCode(),
                equipmentDto,
                entity.getCycleDays(),
                entity.getNextDueDate(),
                entity.getStatus(),
                consumableDto,
                entity.getQuantity()
        );
    }
    @Override
    public LubricationPlanDto create(
            LubricationPlanDto dto
    ) {

        LubricationPlan entity = new LubricationPlan();


        entity.setLubricationCode(
                dto.getLubricationCode()
        );


        if(dto.getEquipment() != null){

            entity.setEquipment(
                    equipmentRepository.findById(
                            dto.getEquipment().getId()
                    ).orElseThrow(
                            () -> new RuntimeException(
                                    "Không tìm thấy thiết bị"
                            )
                    )
            );
        }


        entity.setCycleDays(
                dto.getCycleDays()
        );


        entity.setNextDueDate(
                dto.getNextDueDate()
        );


        entity.setStatus(
                dto.getStatus()
        );


        if(dto.getConsumable() != null){

            entity.setConsumable(
                    consumableRepository.findById(
                            dto.getConsumable().getId()
                    ).orElseThrow(
                            () -> new RuntimeException(
                                    "Không tìm thấy vật tư"
                            )
                    )
            );
        }


        entity.setQuantity(
                dto.getQuantity()
        );


        LubricationPlan saved =
                lubricationPlanRepository.save(entity);


        return convertToDto(saved);
    }

    @Override
    public void deleteById(Integer id) {

        LubricationPlan plan = lubricationPlanRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy kế hoạch"));

        plan.setIsDeleted(true);

        lubricationPlanRepository.save(plan);
    }
}
