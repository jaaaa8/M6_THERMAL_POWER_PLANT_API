package com.example.m6_thermal_power_plant_api.exception;

public class TimeOverlapException extends RuntimeException {
    public TimeOverlapException() {
        super("Thoi gian overlap");
    }
}
