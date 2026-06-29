package com.example.m6_thermal_power_plant_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class M6ThermalPowerPlantApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(M6ThermalPowerPlantApiApplication.class, args);
        System.out.println(
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
                        .encode("123456")
        );
    }

}
