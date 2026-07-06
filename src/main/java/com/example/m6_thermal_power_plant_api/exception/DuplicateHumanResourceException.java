package com.example.m6_thermal_power_plant_api.exception;

public class DuplicateHumanResourceException extends RuntimeException {
    public DuplicateHumanResourceException() {
        super("Nhan su nay khong duoc trung lap");
    }

    public DuplicateHumanResourceException(String message) {
        super(message);
    }
}
