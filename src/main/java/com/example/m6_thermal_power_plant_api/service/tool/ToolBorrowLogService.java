package com.example.m6_thermal_power_plant_api.service.tool;

import com.example.m6_thermal_power_plant_api.dto.tool.ToolBorrowLogResponse;
import com.example.m6_thermal_power_plant_api.dto.tool.ToolBorrowRejectRequest;
import com.example.m6_thermal_power_plant_api.dto.tool.ToolBorrowRequest;
import com.example.m6_thermal_power_plant_api.dto.tool.ToolBorrowReturnRequest;
import com.example.m6_thermal_power_plant_api.entity.Account;
import com.example.m6_thermal_power_plant_api.entity.enums.BorrowStatus;
import com.example.m6_thermal_power_plant_api.entity.tool.Tool;
import com.example.m6_thermal_power_plant_api.entity.tool.ToolBorrowLog;
import com.example.m6_thermal_power_plant_api.exception.BadRequestException;
import com.example.m6_thermal_power_plant_api.exception.ResourceNotFoundException;
import com.example.m6_thermal_power_plant_api.repository.account.IAccountRepository;
import com.example.m6_thermal_power_plant_api.repository.IToolBorrowLogRepository;
import com.example.m6_thermal_power_plant_api.repository.IToolRepository;
import com.example.m6_thermal_power_plant_api.service.impl.IToolBorrowLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ToolBorrowLogService implements IToolBorrowLogService {

    private final IToolBorrowLogRepository toolBorrowLogRepository;
    private final IToolRepository toolRepository;
    private final IAccountRepository accountRepository;
    private final NotificationService notificationService;

    @Override
    public ToolBorrowLogResponse createBorrowRequest(Integer accountId, ToolBorrowRequest request) {
        Tool tool = getToolOrThrow(request.getToolId());
        Account account = getAccountOrThrow(accountId);

        if (request.getQuantity() > tool.getQuantityAvailable()) {
            throw new BadRequestException("Số lượng mượn vượt quá số lượng khả dụng hiện có");
        }

        ToolBorrowLog log = ToolBorrowLog.builder()
                .tool(tool)
                .account(account)
                .quantity(request.getQuantity())
                .borrowPurpose(request.getBorrowPurpose())
                .status(BorrowStatus.PENDING)
                .transactionDate(LocalDateTime.now())
                .dueDate(request.getDueDate())
                .build();

        ToolBorrowLogResponse response = toResponse(toolBorrowLogRepository.save(log));

        // Thông báo cho thủ kho / admin khi có yêu cầu mới
        String borrowerName = account.getEmployee() != null ? account.getEmployee().getFullName() : account.getUsername();
        notificationService.sendToAdmins(
                "Yêu cầu mượn CCDC mới",
                borrowerName + " muốn mượn " + tool.getName() + " (SL: " + request.getQuantity() + ")",
                "/ccdc/muon-tra"
        );

        return response;
    }

    @Override
    public ToolBorrowLogResponse approve(Integer id, Integer approvedByAccountId) {
        ToolBorrowLog log = getLogOrThrow(id);
        if (log.getStatus() != BorrowStatus.PENDING) {
            throw new BadRequestException("Chỉ có thể duyệt phiếu đang ở trạng thái chờ duyệt");
        }

        Tool tool = log.getTool();
        if (log.getQuantity() > tool.getQuantityAvailable()) {
            throw new BadRequestException("Số lượng khả dụng không còn đủ để duyệt phiếu mượn này");
        }

        tool.setQuantityBorrowed(tool.getQuantityBorrowed() + log.getQuantity());
        toolRepository.save(tool);

        log.setStatus(BorrowStatus.APPROVED);
        log.setApprovedBy(getAccountOrThrow(approvedByAccountId));
        log.setDeliveredDate(LocalDateTime.now());

        ToolBorrowLogResponse response = toResponse(toolBorrowLogRepository.save(log));

        // Thông báo cho người mượn khi được duyệt
        notificationService.send(
                log.getAccount().getId(),
                "Yêu cầu mượn CCDC đã được duyệt",
                "Phiếu mượn " + log.getTool().getName() + " của bạn đã được duyệt. Vui lòng đến kho nhận.",
                "/employee"
        );

        return response;
    }

    @Override
    public ToolBorrowLogResponse reject(Integer id, Integer approvedByAccountId, ToolBorrowRejectRequest request) {
        ToolBorrowLog log = getLogOrThrow(id);
        if (log.getStatus() != BorrowStatus.PENDING) {
            throw new BadRequestException("Chỉ có thể từ chối phiếu đang ở trạng thái chờ duyệt");
        }

        log.setStatus(BorrowStatus.REJECTED);
        log.setApprovedBy(getAccountOrThrow(approvedByAccountId));
        log.setReturnNote(request.getReason());

        ToolBorrowLogResponse response = toResponse(toolBorrowLogRepository.save(log));

        // Thông báo cho người mượn khi bị từ chối
        notificationService.send(
                log.getAccount().getId(),
                "Yêu cầu mượn CCDC bị từ chối",
                "Phiếu mượn " + log.getTool().getName() + " đã bị từ chối. Lý do: " + request.getReason(),
                "/employee"
        );

        return response;
    }

    @Override
    public ToolBorrowLogResponse returnTool(Integer id, ToolBorrowReturnRequest request) {
        ToolBorrowLog log = getLogOrThrow(id);
        if (log.getStatus() != BorrowStatus.APPROVED) {
            throw new BadRequestException("Chỉ có thể trả CCDC đang ở trạng thái đã duyệt và chưa trả");
        }

        int alreadyReturned = log.getReturnedQuantity() == null ? 0 : log.getReturnedQuantity();
        int remaining = log.getQuantity() - alreadyReturned;

        int returnQuantity = request.getReturnQuantity() == null ? remaining : request.getReturnQuantity();
        if (returnQuantity > remaining) {
            throw new BadRequestException("Số lượng trả không thể vượt quá số còn đang mượn (" + remaining + ")");
        }

        Integer damagedQuantity = request.getDamagedQuantity() == null ? 0 : request.getDamagedQuantity();
        if (damagedQuantity > returnQuantity) {
            throw new BadRequestException("Số lượng hư hỏng không thể vượt quá số lượng trả");
        }

        Tool tool = log.getTool();
        tool.setQuantityBorrowed(tool.getQuantityBorrowed() - returnQuantity);
        tool.setQuantityDamaged(tool.getQuantityDamaged() + damagedQuantity);
        toolRepository.save(tool);

        log.setReturnedQuantity(alreadyReturned + returnQuantity);
        log.setReturnNote(request.getReturnNote());

        // Chỉ đóng phiếu (RETURNED) khi đã trả đủ; còn thiếu thì vẫn "đang mượn"
        if (log.getReturnedQuantity() >= log.getQuantity()) {
            log.setStatus(BorrowStatus.RETURNED);
            log.setActualReturnDate(LocalDateTime.now());
        }

        return toResponse(toolBorrowLogRepository.save(log));
    }

    @Override
    public ToolBorrowLogResponse getById(Integer id) {
        return toResponse(getLogOrThrow(id));
    }

    @Override
    public Page<ToolBorrowLogResponse> search(Integer accountId, Integer toolId, BorrowStatus status, Pageable pageable) {
        return toolBorrowLogRepository.search(accountId, toolId, status, pageable).map(this::toResponse);
    }

    private Tool getToolOrThrow(Integer id) {
        return toolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy CCDC với id: " + id));
    }

    private Account getAccountOrThrow(Integer id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với id: " + id));
    }

    private ToolBorrowLog getLogOrThrow(Integer id) {
        return toolBorrowLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu mượn với id: " + id));
    }

    private String accountDisplayName(Account account) {
        return account.getEmployee() == null ? account.getUsername() : account.getEmployee().getFullName();
    }

    private ToolBorrowLogResponse toResponse(ToolBorrowLog log) {
        boolean overdue = log.getStatus() == BorrowStatus.APPROVED
                && log.getActualReturnDate() == null
                && log.getDueDate() != null
                && log.getDueDate().isBefore(LocalDateTime.now());

        return ToolBorrowLogResponse.builder()
                .id(log.getId())
                .toolId(log.getTool().getId())
                .toolCode(log.getTool().getToolCode())
                .toolName(log.getTool().getName())
                .accountId(log.getAccount().getId())
                .accountName(accountDisplayName(log.getAccount()))
                .quantity(log.getQuantity())
                .returnedQuantity(log.getReturnedQuantity())
                .borrowPurpose(log.getBorrowPurpose())
                .status(log.getStatus())
                .transactionDate(log.getTransactionDate())
                .deliveredDate(log.getDeliveredDate())
                .dueDate(log.getDueDate())
                .actualReturnDate(log.getActualReturnDate())
                .returnNote(log.getReturnNote())
                .approvedById(log.getApprovedBy() == null ? null : log.getApprovedBy().getId())
                .approvedByName(log.getApprovedBy() == null ? null : log.getApprovedBy().getUsername())
                .overdueNotified(log.getOverdueNotified())
                .overdue(overdue)
                .build();
    }
}
