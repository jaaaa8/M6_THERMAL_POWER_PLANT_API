package com.example.m6_thermal_power_plant_api.repository.employee;

import com.example.m6_thermal_power_plant_api.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IEmployeeRepository extends JpaRepository<Employee, Integer> {
    boolean existsByEmployeeCode(String employeeCode);
    boolean existsByGmail(String gmail);
    boolean existsByPhone(String phone);
}
