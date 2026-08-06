package com.example.m6_thermal_power_plant_api.controller.work_order;

import com.example.m6_thermal_power_plant_api.dto.maintenance.CreateWorkOrderRequest;
import com.example.m6_thermal_power_plant_api.dto.maintenance.WorkOrderDTO;
import com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderType;
import com.example.m6_thermal_power_plant_api.service.maintenance.IMaintenanceService;
import com.example.m6_thermal_power_plant_api.service.pdf.WorkOrderPdfService;
import com.example.m6_thermal_power_plant_api.util.UniqueCodeRetryExecutor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Dispatch của POST /api/v1/work-orders: chọn đúng service theo chế độ tạo phiếu.
 *
 * Chế độ bôi trơn (equipmentLines, KHÔNG có equipmentIds) từng bị rơi nhầm vào
 * {@code createWorkOrderFromRequest} → {@code findById(null)} → "The given id
 * must not be null". Ba test dưới khoá cả ba chế độ.
 */
@ExtendWith(MockitoExtension.class)
class WorkOrderControllerCreateDispatchTest {

    @Mock
    private IMaintenanceService maintenanceService;
    @Mock
    private WorkOrderPdfService workOrderPdfService;

    private WorkOrderController controller() {
        return new WorkOrderController(maintenanceService, new UniqueCodeRetryExecutor(), workOrderPdfService);
    }

    @Test
    void taoTuYeuCauSuaChua_dungCreateWorkOrderFromRequest() {
        CreateWorkOrderRequest request = new CreateWorkOrderRequest();
        request.setRepairRequestId(7);
        when(maintenanceService.createWorkOrderFromRequest(any(), isNull())).thenReturn(new WorkOrderDTO());

        controller().createWorkOrder(request, null);

        verify(maintenanceService).createWorkOrderFromRequest(request, null);
        verify(maintenanceService, never()).createManualWorkOrder(any(), any());
    }

    @Test
    void taoThuCongNhieuThietBi_dungCreateManualWorkOrder() {
        CreateWorkOrderRequest request = new CreateWorkOrderRequest();
        request.setEquipmentIds(List.of(1, 2));
        when(maintenanceService.createManualWorkOrder(any(), isNull())).thenReturn(new WorkOrderDTO());

        controller().createWorkOrder(request, null);

        verify(maintenanceService).createManualWorkOrder(request, null);
        verify(maintenanceService, never()).createWorkOrderFromRequest(any(), any());
    }

    @Test
    void taoPhieuBoiTron_dungCreateManualWorkOrder() {
        CreateWorkOrderRequest.EquipmentLineInput line = new CreateWorkOrderRequest.EquipmentLineInput();
        line.setEquipmentId(3);
        line.setLubricationPlanId(9);
        CreateWorkOrderRequest request = new CreateWorkOrderRequest();
        request.setType(WorkOrderType.LUBRICATION);
        request.setEquipmentLines(List.of(line));
        when(maintenanceService.createManualWorkOrder(any(), isNull())).thenReturn(new WorkOrderDTO());

        controller().createWorkOrder(request, null);

        verify(maintenanceService).createManualWorkOrder(request, null);
        verify(maintenanceService, never()).createWorkOrderFromRequest(any(), any());
    }
}
