package com.example.m6_thermal_power_plant_api.dto.maintenance;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CreateWorkOrderRequestValidationTest {

    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    private CreateWorkOrderRequest baseRequest() {
        CreateWorkOrderRequest req = new CreateWorkOrderRequest();
        req.setLeaderId(1);
        req.setDirectSupervisorId(2);
        req.setSafetySupervisorId(3);
        req.setStartTime(LocalDateTime.now());
        return req;
    }

    @Test
    void valid_whenOnlyRepairRequestId() {
        CreateWorkOrderRequest req = baseRequest();
        req.setRepairRequestId(1);

        assertThat(validator.validate(req)).isEmpty();
    }

    @Test
    void valid_whenOnlyEquipmentIds() {
        CreateWorkOrderRequest req = baseRequest();
        req.setEquipmentIds(List.of(10, 11, 12));

        assertThat(validator.validate(req)).isEmpty();
    }

    @Test
    void invalid_whenBothRepairRequestIdAndEquipmentIds() {
        CreateWorkOrderRequest req = baseRequest();
        req.setRepairRequestId(1);
        req.setEquipmentIds(List.of(10));

        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void invalid_whenNeitherProvided() {
        CreateWorkOrderRequest req = baseRequest();

        assertThat(validator.validate(req)).isNotEmpty();
    }
}