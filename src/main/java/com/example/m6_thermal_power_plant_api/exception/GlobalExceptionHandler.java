package com.example.m6_thermal_power_plant_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    /** Không tìm thấy bản ghi nghiệp vụ (do service chủ động ném). */
    @ExceptionHandler(com.example.m6_thermal_power_plant_api.exception.ObjectNotFoundException.class)
    public ResponseEntity<String> handleObjectNotFound(
            com.example.m6_thermal_power_plant_api.exception.ObjectNotFoundException ex) {
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
