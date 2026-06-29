package com.example.m6_thermal_power_plant_api.exception;

import org.hibernate.ObjectNotFoundException;
import com.example.m6_thermal_power_plant_api.dto.tool.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errorList = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());
        String errors = String.join("; ", errorList);
        return ApiResponse.error(HttpStatus.BAD_REQUEST, errors, "VALIDATION_ERROR");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler({EntityNotFoundException.class, ResourceNotFoundException.class})
    public ResponseEntity<ApiResponse<Object>> handleNotFound(RuntimeException ex) {
        log.warn("Lỗi không tìm thấy: {}", ex.getMessage());
        return ApiResponse.error(HttpStatus.NOT_FOUND, ex.getMessage(), "NOT_FOUND");
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidCredentials(InvalidCredentialsException ex) {
        log.warn("Sai thông tin đăng nhập: {}", ex.getMessage());
        return ApiResponse.error(HttpStatus.UNAUTHORIZED, ex.getMessage(), "INVALID_CREDENTIALS");
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidToken(InvalidTokenException ex) {
        log.warn("Token không hợp lệ: {}", ex.getMessage());
        return ApiResponse.error(HttpStatus.UNAUTHORIZED, ex.getMessage(), "INVALID_TOKEN");
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicate(DuplicateResourceException ex) {
        log.warn("Tài nguyên trùng lặp: {}", ex.getMessage());
        return ApiResponse.error(HttpStatus.CONFLICT, ex.getMessage(), "DUPLICATE_RESOURCE");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Truy cập bị từ chối: {}", ex.getMessage());
        return ApiResponse.error(HttpStatus.FORBIDDEN,
                "Bạn không có quyền thực hiện hành động này", "FORBIDDEN");
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Object>> handleOptimisticLock(OptimisticLockingFailureException ex) {
        log.warn("Xung đột đồng thời: {}", ex.getMessage());
        return ApiResponse.error(HttpStatus.CONFLICT,
                "Yêu cầu xung đột — đã có thao tác khác xử lý trước. Vui lòng thử lại.", "CONCURRENT_CONFLICT");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeExceptions(RuntimeException ex) {
        log.warn("Lỗi nghiệp vụ: {}", ex.getMessage());
        return ApiResponse.error(HttpStatus.BAD_REQUEST, ex.getMessage(), "BAD_REQUEST");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String errorMessage = String.format("Tham số '%s' không đúng định dạng.", ex.getName());
        return ApiResponse.error(HttpStatus.BAD_REQUEST, errorMessage, "TYPE_MISMATCH");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleAllExceptions(Exception ex) {
        log.error("Lỗi hệ thống nghiêm trọng: ", ex);
        return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "Hệ thống đang bảo trì hoặc gặp sự cố!", "SERVER_ERROR");
    }


    /**
     * Exception nghiệp vụ tự định nghĩa (cùng package): service ném khi không tìm thấy đối tượng.
     * KHÔNG import org.hibernate.ObjectNotFoundException ở đầu file — nếu import, tên đơn ở đây sẽ
     * trỏ nhầm sang kiểu của Hibernate và đụng độ với {@link #handleHibernateObjectNotFound}.
     */
    @ExceptionHandler(ObjectNotFoundException.class)
    public ResponseEntity<String> handleObjectNotFound(ObjectNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /**
     * Quan hệ LAZY trỏ tới bản ghi đã bị soft-delete (bị lọc bởi @SQLRestriction)
     * khiến Hibernate không tìm thấy dòng — coi là 404.
     */
    @ExceptionHandler(org.hibernate.ObjectNotFoundException.class)
    public ResponseEntity<String> handleHibernateObjectNotFound(org.hibernate.ObjectNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /** Xung đột nghiệp vụ (VD: yêu cầu đã có phiếu công tác). */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalState(IllegalStateException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    /** Dữ liệu đầu vào không hợp lệ. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
