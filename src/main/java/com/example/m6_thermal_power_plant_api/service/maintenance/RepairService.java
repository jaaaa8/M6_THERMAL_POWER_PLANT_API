package com.example.m6_thermal_power_plant_api.service.maintenance;

import com.example.m6_thermal_power_plant_api.dto.maintenance.CreateRepairRequestDTO;
import com.example.m6_thermal_power_plant_api.dto.maintenance.RepairRequestDTO;
import com.example.m6_thermal_power_plant_api.dto.maintenance.RepairRequestStatsDTO;
import com.example.m6_thermal_power_plant_api.entity.enums.EquipmentStatus;
import com.example.m6_thermal_power_plant_api.entity.enums.RepairPriority;
import com.example.m6_thermal_power_plant_api.entity.enums.RepairRequestStatus;
import com.example.m6_thermal_power_plant_api.exception.ObjectNotFoundException;
import com.example.m6_thermal_power_plant_api.repository.AccountRepository;
import com.example.m6_thermal_power_plant_api.repository.RepairRequestRepository;
import com.example.m6_thermal_power_plant_api.repository.equipment.IEquipmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RepairService implements IRepairService {

    private final RepairRequestRepository repairRequestRepository;
    private final IEquipmentRepository equipmentRepository;
    private final AccountRepository accountRepository;

    public RepairService(RepairRequestRepository repairRequestRepository,
                         IEquipmentRepository equipmentRepository,
                         AccountRepository accountRepository) {
        this.repairRequestRepository = repairRequestRepository;
        this.equipmentRepository = equipmentRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RepairRequestDTO> getAllRepairRequests(RepairRequestStatus status,
                                                       RepairPriority priority,
                                                       String search,
                                                       Pageable pageable) {
        String kw = (search == null || search.isBlank()) ? null : search.trim();
        return repairRequestRepository.search(status, priority, kw, pageable)
                .map(RepairRequestDTO::from);
    }

    @Override
    @Transactional(readOnly = true)
    public RepairRequestStatsDTO getStats() {
        return RepairRequestStatsDTO.builder()
                .total(repairRequestRepository.count())
                .pending(repairRequestRepository.countByStatus(RepairRequestStatus.PENDING))
                .approved(repairRequestRepository.countByStatus(RepairRequestStatus.APPROVED))
                .inProgress(repairRequestRepository.countByStatus(RepairRequestStatus.IN_PROGRESS))
                .completed(repairRequestRepository.countByStatus(RepairRequestStatus.COMPLETED))
                .emergencyPending(repairRequestRepository.countByStatusAndPriority(
                        RepairRequestStatus.PENDING, RepairPriority.EMERGENCY))
                .build();
    }

    @Override
    @Transactional
    public RepairRequestDTO createRepairRequest(CreateRepairRequestDTO dto, String requesterUsername) {
        com.example.m6_thermal_power_plant_api.entity.Equipment equipment = equipmentRepository.findById(dto.getEquipmentId())
                .orElseThrow(() -> new ObjectNotFoundException("Không tìm thấy thiết bị với ID: " + dto.getEquipmentId()));

        // Thiết bị có yêu cầu sửa chữa → đánh dấu Sự cố. equipment là managed entity trong
        // method @Transactional nên dirty-checking tự flush, không cần gọi save riêng.
        equipment.setStatus(EquipmentStatus.FAILURE);

        com.example.m6_thermal_power_plant_api.entity.Account requester = accountRepository.findAccountByUsername(requesterUsername)
                .orElseThrow(() -> new ObjectNotFoundException("Không tìm thấy tài khoản người tạo: " + requesterUsername));

        com.example.m6_thermal_power_plant_api.entity.RepairRequest req = new com.example.m6_thermal_power_plant_api.entity.RepairRequest();
        req.setRequestCode(com.example.m6_thermal_power_plant_api.util.TimeStampCodeGenerator.generate("RepairRequest"));
        req.setEquipment(equipment);
        req.setRequester(requester);
        req.setIncidentDescription(dto.getIncidentDescription());
        req.setPriority(dto.getPriority());
        req.setStatus(RepairRequestStatus.PENDING);

        return RepairRequestDTO.from(repairRequestRepository.save(req));
    }

    @Override
    @Transactional
    public void deleteRepairRequest(Integer id, String requesterUsername) {
        com.example.m6_thermal_power_plant_api.entity.RepairRequest req = repairRequestRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Không tìm thấy yêu cầu sửa chữa với ID: " + id));

        if (req.getWorkOrders() != null && !req.getWorkOrders().isEmpty()) {
            throw new IllegalStateException("Không thể xoá yêu cầu đã có phiếu công tác — hãy huỷ Phiếu công tác (PCT) trước.");
        }

        req.setIsDeleted(true);
        repairRequestRepository.save(req);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RepairRequestDTO> getPendingRepairRequests(Pageable pageable) {
        // Page.map giữ nguyên metadata phân trang; RepairRequestDTO.from chạy
        // TRONG transaction readOnly nên các quan hệ LAZY map được an toàn.
        return repairRequestRepository
                .findByStatus(RepairRequestStatus.PENDING, pageable)
                .map(RepairRequestDTO::from);
    }
}
