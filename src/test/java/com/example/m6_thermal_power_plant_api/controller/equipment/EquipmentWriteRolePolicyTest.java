package com.example.m6_thermal_power_plant_api.controller.equipment;

import com.example.m6_thermal_power_plant_api.dto.equipment.request.AddEquipmentDTO;
import com.example.m6_thermal_power_plant_api.dto.equipment.request.EquipmentUpdateDTO;
import com.example.m6_thermal_power_plant_api.dto.equipment.request.ParameterCatalogDTO;
import com.example.m6_thermal_power_plant_api.dto.equipment.request.ParameterDTO;
import com.example.m6_thermal_power_plant_api.dto.equipment.request.TypeEquipmentDTO;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EquipmentWriteRolePolicyTest {

    private static final String FOREMAN_ONLY = "hasAnyRole('WORKSHOP_FOREMAN')";

    private static String rule(Class<?> controller, String method, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        return controller.getMethod(method, parameterTypes)
                .getAnnotation(PreAuthorize.class)
                .value();
    }

    @Test
    void equipmentWriteEndpoints_areForemanOnly() throws Exception {
        assertThat(rule(EquipmentController.class, "addEquipment",
                Integer.class, AddEquipmentDTO.class, List.class)).isEqualTo(FOREMAN_ONLY);
        assertThat(rule(EquipmentController.class, "deleteById", Integer.class)).isEqualTo(FOREMAN_ONLY);
        assertThat(rule(EquipmentController.class, "update",
                Integer.class, EquipmentUpdateDTO.class, List.class)).isEqualTo(FOREMAN_ONLY);
    }

    @Test
    void typeCreateEndpoint_isForemanOnly() throws Exception {
        assertThat(rule(EquipmentTypeController.class, "create", TypeEquipmentDTO.class))
                .isEqualTo(FOREMAN_ONLY);
    }

    @Test
    void parameterWriteEndpoints_areForemanOnly() throws Exception {
        assertThat(rule(EquipmentParameterController.class, "create", List.class)).isEqualTo(FOREMAN_ONLY);
        assertThat(rule(EquipmentParameterController.class, "update", Integer.class, ParameterDTO.class))
                .isEqualTo(FOREMAN_ONLY);
        assertThat(rule(EquipmentParameterController.class, "delete", Integer.class)).isEqualTo(FOREMAN_ONLY);
    }

    @Test
    void catalogWriteEndpoints_areForemanOnly() throws Exception {
        assertThat(rule(ParameterCatalogController.class, "create", ParameterCatalogDTO.class))
                .isEqualTo(FOREMAN_ONLY);
        assertThat(rule(ParameterCatalogController.class, "update", Integer.class, ParameterCatalogDTO.class))
                .isEqualTo(FOREMAN_ONLY);
        assertThat(rule(ParameterCatalogController.class, "delete", Integer.class)).isEqualTo(FOREMAN_ONLY);
    }
}
