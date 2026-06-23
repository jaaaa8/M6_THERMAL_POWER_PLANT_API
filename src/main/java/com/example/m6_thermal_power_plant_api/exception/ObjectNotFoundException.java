package com.example.m6_thermal_power_plant_api.exception;

public class ObjectNotFoundException extends RuntimeException {
    public ObjectNotFoundException() {
        super("Đối tượng không tồn tại trong hệ thống.");
    }
    public ObjectNotFoundException(String message) {
        super(message);
    }
}
