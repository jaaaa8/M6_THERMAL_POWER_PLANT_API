package com.example.m6_thermal_power_plant_api.util;

import com.example.m6_thermal_power_plant_api.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Đặc tả hành vi của {@link UniqueCodeRetryExecutor}: chạy operation, thử lại
 * khi đụng {@link DataIntegrityViolationException}, bỏ cuộc sau MAX_ATTEMPTS.
 *
 * Operation truyền vào chính là "ranh giới" hợp lệ để kiểm chứng — nên việc
 * đếm số lần nó được gọi là khẳng định trên HỢP ĐỒNG, không phải nội bộ.
 */
class UniqueCodeRetryExecutorTest {

    /** Số lần thử tối đa kỳ vọng (khớp hằng số nội bộ của executor). */
    private static final int MAX_ATTEMPTS = 5;

    private final UniqueCodeRetryExecutor executor = new UniqueCodeRetryExecutor();

    @Test
    void execute_operationSucceedsFirstTry_returnsResultAndRunsOnce() {
        AtomicInteger attempts = new AtomicInteger(0);

        String result = executor.execute(() -> {
            attempts.incrementAndGet();
            return "WO-260627153045-000";
        });

        assertThat(result).isEqualTo("WO-260627153045-000");
        assertThat(attempts).hasValue(1);
    }

    @Test
    void execute_failsTwiceThenSucceeds_retriesAndReturnsResult() {
        AtomicInteger attempts = new AtomicInteger(0);

        String result = executor.execute(() -> {
            int attempt = attempts.incrementAndGet();
            if (attempt < 3) {
                throw new DataIntegrityViolationException("duplicate code");
            }
            return "OK-on-attempt-" + attempt;
        });

        assertThat(result).isEqualTo("OK-on-attempt-3");
        assertThat(attempts).hasValue(3);
    }

    @Test
    void execute_succeedsExactlyOnLastAttempt_stillReturnsResult() {
        AtomicInteger attempts = new AtomicInteger(0);

        String result = executor.execute(() -> {
            int attempt = attempts.incrementAndGet();
            if (attempt < MAX_ATTEMPTS) {
                throw new DataIntegrityViolationException("duplicate code");
            }
            return "OK-on-attempt-" + attempt;
        });

        assertThat(result).isEqualTo("OK-on-attempt-" + MAX_ATTEMPTS);
        assertThat(attempts).hasValue(MAX_ATTEMPTS);
    }

    @Test
    void execute_alwaysDuplicate_throwsBadRequestAfterMaxAttemptsWithCause() {
        AtomicInteger attempts = new AtomicInteger(0);
        DataIntegrityViolationException duplicate =
                new DataIntegrityViolationException("duplicate code");

        Supplier<String> alwaysDuplicate = () -> {
            attempts.incrementAndGet();
            throw duplicate;
        };

        assertThatThrownBy(() -> executor.execute(alwaysDuplicate))
                .isInstanceOf(BadRequestException.class)
                .hasCause(duplicate);

        // Thử đúng MAX_ATTEMPTS lần rồi mới chịu thua — không hơn, không kém.
        assertThat(attempts).hasValue(MAX_ATTEMPTS);
    }

    @Test
    void execute_nonDuplicateException_propagatesImmediatelyWithoutRetry() {
        AtomicInteger attempts = new AtomicInteger(0);

        Supplier<String> failsHard = () -> {
            attempts.incrementAndGet();
            throw new IllegalStateException("unrelated failure");
        };

        assertThatThrownBy(() -> executor.execute(failsHard))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("unrelated failure");

        // Chỉ DataIntegrityViolationException mới được retry; lỗi khác văng ra ngay.
        assertThat(attempts).hasValue(1);
    }
}
