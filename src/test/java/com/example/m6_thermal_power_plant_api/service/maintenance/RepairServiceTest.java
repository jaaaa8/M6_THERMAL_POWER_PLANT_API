package com.example.m6_thermal_power_plant_api.service.maintenance;

import com.example.m6_thermal_power_plant_api.dto.maintenance.CreateRepairRequestDTO;
import com.example.m6_thermal_power_plant_api.entity.Account;
import com.example.m6_thermal_power_plant_api.entity.Equipment;
import com.example.m6_thermal_power_plant_api.entity.RepairRequest;
import com.example.m6_thermal_power_plant_api.entity.enums.RepairPriority;
import com.example.m6_thermal_power_plant_api.repository.AccountRepository;
import com.example.m6_thermal_power_plant_api.repository.RepairRequestRepository;
import com.example.m6_thermal_power_plant_api.repository.equipment.IEquipmentRepository;
import com.example.m6_thermal_power_plant_api.service.tool.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepairServiceTest {

    @Mock
    private RepairRequestRepository repairRequestRepository;
    @Mock
    private IEquipmentRepository equipmentRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private RepairService repairService;

    @Test
    void createRepairRequest_notifiesLeaderRolesExcludingRequester() {
        Equipment equipment = Equipment.builder().id(1).kksCode("10LAC10AP001").name("Boiler Feed Pump A").build();
        Account requester = Account.builder().id(5).build();
        when(equipmentRepository.findById(1)).thenReturn(Optional.of(equipment));
        when(accountRepository.findAccountByUsername("shift.leader")).thenReturn(Optional.of(requester));
        when(repairRequestRepository.save(any(RepairRequest.class))).thenAnswer(inv -> {
            RepairRequest rr = inv.getArgument(0);
            rr.setId(100);
            return rr;
        });

        CreateRepairRequestDTO dto = new CreateRepairRequestDTO();
        dto.setEquipmentId(1);
        dto.setIncidentDescription("Abnormal vibration.");
        dto.setPriority(RepairPriority.HIGH);

        repairService.createRepairRequest(dto, "shift.leader");

        ArgumentCaptor<List<String>> rolesCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificationService).sendToRoles(
                rolesCaptor.capture(),
                eq("Yêu cầu sửa chữa mới"),
                messageCaptor.capture(),
                eq("/repair/yeu-cau"),
                eq(5));

        assertThat(rolesCaptor.getValue())
                .containsExactly("SHIFT_LEADER", "CREW_LEADER", "MAINTENANCE_FOREMAN", "ADMIN");
        assertThat(messageCaptor.getValue())
                .startsWith("Yêu cầu RR-")
                .contains("Boiler Feed Pump A (10LAC10AP001) vừa được tạo");
    }
}