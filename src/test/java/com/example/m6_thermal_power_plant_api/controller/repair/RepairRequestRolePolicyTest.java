package com.example.m6_thermal_power_plant_api.controller.repair;

import com.example.m6_thermal_power_plant_api.controller.work_order.WorkOrderController;
import com.example.m6_thermal_power_plant_api.dto.maintenance.CreateRepairRequestDTO;
import com.example.m6_thermal_power_plant_api.dto.maintenance.CreateWorkOrderRequest;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import java.security.Principal;

import static org.assertj.core.api.Assertions.assertThat;

class RepairRequestRolePolicyTest {

    @Test
    void teamLeader_canCreateAndDeleteRepairRequests() throws Exception {
        assertThat(rule(RepairRequestController.class, "createRepairRequest",
                CreateRepairRequestDTO.class, Authentication.class))
                .isEqualTo("hasAnyRole('SHIFT_LEADER', 'CREW_LEADER', 'TEAM_LEADER')");
        assertThat(rule(RepairRequestController.class, "deleteRepairRequest",
                Integer.class, Authentication.class))
                .isEqualTo("hasAnyRole('SHIFT_LEADER', 'CREW_LEADER', 'TEAM_LEADER')");
    }

    @Test
    void teamLeader_workOrderRule_blocksOnlyRepairRequestMode() throws Exception {
        assertThat(rule(WorkOrderController.class, "createWorkOrder",
                CreateWorkOrderRequest.class, Principal.class))
                .isEqualTo("hasRole('MAINTENANCE_FOREMAN') or "
                        + "(hasRole('TEAM_LEADER') and #p0.repairRequestId == null)");
    }

    private static String rule(Class<?> controller, String method, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        return controller.getMethod(method, parameterTypes)
                .getAnnotation(PreAuthorize.class)
                .value();
    }
}