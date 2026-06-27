package com.example.m6_thermal_power_plant_api.exception;

public class ObjectNotFoundException extends RuntimeException {
    public ObjectNotFoundException() {
        super("Doi tuong khong ton tai trong he thong!");
    }
    public ObjectNotFoundException(String message) {
        super(message);
    }
}
