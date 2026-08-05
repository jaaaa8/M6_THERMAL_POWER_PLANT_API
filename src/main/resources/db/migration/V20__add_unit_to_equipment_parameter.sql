ALTER TABLE equipment_parameters
    ADD COLUMN unit_id INT NULL AFTER parameter_id;

ALTER TABLE equipment_parameters
    ADD CONSTRAINT FK_equipment_parameter_unit
        FOREIGN KEY (unit_id)
            REFERENCES units(id);

CREATE INDEX IDX_equipment_parameter_unit
    ON equipment_parameters(unit_id);