package com.example.m6_thermal_power_plant_api.util;

import com.example.m6_thermal_power_plant_api.exception.BadRequestException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * Lưới an toàn cho mã sinh bởi {@link TimeStampCodeGenerator}: chạy một thao
 * tác lưu, nếu đụng UNIQUE constraint (mã trùng) thì THỬ LẠI — mỗi lần thử
 * {@code TimeStampCodeGenerator.generate(...)} sẽ cho hậu tố SEQ mới nên mã
 * khác đi, gần như chắc chắn thoát trùng ở lần kế tiếp.
 *
 * VÌ SAO CẦN HELPER NÀY?
 *  Bộ đếm AtomicInteger trong generator khử trùng trong-tiến-trình, NHƯNG vẫn
 *  còn khe hở hiếm: JVM restart làm bộ đếm về 0, hoặc chạy nhiều instance, hoặc
 *  > 1000 mã/giây. Khi đó DB (unique theo active_flag) sẽ ném
 *  {@link DataIntegrityViolationException}. Thay vì để lỗi nổ ra ngoài, ta sinh
 *  lại mã và thử lại vài lần.
 *
 * ⚠️ HỢP ĐỒNG VỀ GIAO DỊCH (QUAN TRỌNG — ĐỌC KỸ):
 *  {@code operation} truyền vào PHẢI là MỘT ĐƠN VỊ GIAO DỊCH ĐỘC LẬP — tức là
 *  gọi tới một method {@code @Transactional} (mở transaction riêng cho mỗi lần
 *  thử). Lý do: khi một insert vi phạm constraint, transaction hiện tại bị đánh
 *  dấu rollback-only, MỌI thao tác sau đó trong cùng transaction sẽ hỏng. Phải
 *  để mỗi lần thử nằm trong transaction MỚI thì việc sinh-lại-rồi-lưu mới sạch.
 *
 *  ✅ ĐÚNG — bọc ở ranh giới ngoài, operation tự mở transaction riêng:
 *  <pre>{@code
 *  // trong controller (KHÔNG nằm trong @Transactional):
 *  return retry.execute(() -> maintenanceService.createWorkOrderFromRequest(req));
 *  // với createWorkOrderFromRequest là @Transactional → mỗi retry = 1 tx mới,
 *  // toàn bộ thao tác rollback sạch rồi chạy lại với orderCode mới.
 *  }</pre>
 *
 *  ❌ SAI — gọi retry quanh một câu save NẰM TRONG một @Transactional lớn:
 *  transaction ngoài đã rollback-only sau lần đụng constraint đầu tiên, retry
 *  vô nghĩa.
 */
@Component
public class UniqueCodeRetryExecutor {

    /** Số lần thử tối đa trước khi chịu thua. */
    private static final int MAX_ATTEMPTS = 5;

    /**
     * Chạy {@code operation}; nếu đụng {@link DataIntegrityViolationException}
     * (nghi do mã trùng) thì thử lại tối đa {@link #MAX_ATTEMPTS} lần.
     *
     * @param operation đơn vị giao dịch độc lập có sinh + lưu mã (xem hợp đồng
     *                  về giao dịch ở Javadoc class)
     * @return kết quả của lần chạy thành công
     * @throws BadRequestException nếu vẫn trùng sau {@link #MAX_ATTEMPTS} lần
     */
    public <T> T execute(Supplier<T> operation) {
        DataIntegrityViolationException last = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return operation.get();
            } catch (DataIntegrityViolationException ex) {
                // Khả năng cao là trùng mã: lần thử sau generator cho SEQ mới.
                last = ex;
            }
        }
        throw new BadRequestException(
                "Khong the sinh ma duy nhat sau " + MAX_ATTEMPTS + " lan thu — vui long thu lai.",
                last);
    }
}
