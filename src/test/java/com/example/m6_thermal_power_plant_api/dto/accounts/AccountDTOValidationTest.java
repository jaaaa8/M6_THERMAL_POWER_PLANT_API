package com.example.m6_thermal_power_plant_api.dto.accounts;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountDTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testUsernameWithSpecialCharacters_IsValid() {
        AccountDTO dto = AccountDTO.builder()
                .username("user_test_01@company.com")
                .email("test@company.com")
                .roleIds(List.of(1))
                .build();

        Set<ConstraintViolation<AccountDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Username containing special characters (@, .) should be valid");
    }

    @Test
    void testUsernameWithUnderscore_IsValid() {
        AccountDTO dto = AccountDTO.builder()
                .username("user_test_01")
                .email("test@company.com")
                .roleIds(List.of(1))
                .build();

        Set<ConstraintViolation<AccountDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Username containing underscore should be valid");
    }

    @Test
    void testUsernameWithoutDigit_IsInvalid() {
        AccountDTO dto = AccountDTO.builder()
                .username("username_no_digit")
                .email("test@company.com")
                .roleIds(List.of(1))
                .build();

        Set<ConstraintViolation<AccountDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Username without digit should be invalid due to regex constraints");
    }

    @Test
    void testUsernameWithoutLowercase_IsInvalid() {
        AccountDTO dto = AccountDTO.builder()
                .username("1234567890")
                .email("test@company.com")
                .roleIds(List.of(1))
                .build();

        Set<ConstraintViolation<AccountDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Username without lowercase letters should be invalid");
    }
}
