package com.example.m6_thermal_power_plant_api.repository;


import com.example.m6_thermal_power_plant_api.entity.tool.ToolBorrowLog;
import com.example.m6_thermal_power_plant_api.entity.enums.BorrowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface IToolBorrowLogRepository extends JpaRepository<ToolBorrowLog, Integer> {

    /**
     * Lọc lịch sử mượn/trả theo accountId, toolId, status — truyền null cho tham số không cần lọc.
     */
    @Query("""
            SELECT l FROM ToolBorrowLog l
            WHERE (:accountId IS NULL OR l.account.id = :accountId)
              AND (:toolId IS NULL OR l.tool.id = :toolId)
              AND (:status IS NULL OR l.status = :status)
            ORDER BY l.transactionDate DESC
            """)
    Page<ToolBorrowLog> search(@Param("accountId") Integer accountId,
                               @Param("toolId") Integer toolId,
                               @Param("status") BorrowStatus status,
                               Pageable pageable);

    /** Dùng cho job quét quá hạn: đã APPROVED, chưa trả, đã qua hạn, chưa gửi email */
    List<ToolBorrowLog> findByStatusAndDueDateBeforeAndActualReturnDateIsNullAndOverdueNotifiedFalse(
            BorrowStatus status, LocalDateTime now);

    /** Dùng cho job nhắc sắp đến hạn: đã APPROVED, chưa trả, dueDate trong khoảng [from, to], chưa gửi email nhắc */
    List<ToolBorrowLog> findByStatusAndDueDateBetweenAndActualReturnDateIsNullAndDueSoonNotifiedFalse(
            BorrowStatus status, LocalDateTime from, LocalDateTime to);

    /** Dashboard: CCDC quá hạn — đã duyệt, chưa trả, quá ngày hẹn trả */
    @Query("SELECT COUNT(b) FROM ToolBorrowLog b WHERE b.status = com.example.m6_thermal_power_plant_api.entity.enums.BorrowStatus.APPROVED AND b.dueDate < CURRENT_TIMESTAMP AND b.actualReturnDate IS NULL")
    long countOverdue();
}