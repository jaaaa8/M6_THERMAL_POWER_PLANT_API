package com.example.m6_thermal_power_plant_api.service.department;

import com.example.m6_thermal_power_plant_api.dto.employee.DepartmentDTO;
import com.example.m6_thermal_power_plant_api.dto.employee.DepartmentCreateDTO;
import com.example.m6_thermal_power_plant_api.entity.Department;
import com.example.m6_thermal_power_plant_api.repository.department.IDepartmentRepository;
import com.example.m6_thermal_power_plant_api.service.soft_delete.SoftDeleteCascadeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private IDepartmentRepository departmentRepository;

    @Mock
    private SoftDeleteCascadeService softDeleteCascadeService;

    @InjectMocks
    private DepartmentService departmentService;

    @Test
    void createDepartment_savesAndReturnsDepartmentDTO() {
        DepartmentCreateDTO createDTO = DepartmentCreateDTO.builder()
                .name("Technical Department")
                .description("Handles technical issues")
                .build();

        Department department = Department.builder()
                .id(1)
                .departmentCode("DEPT123456789")
                .name("Technical Department")
                .description("Handles technical issues")
                .build();

        when(departmentRepository.existsByDepartmentCode(anyString())).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenReturn(department);

        DepartmentDTO result = departmentService.createDepartment(createDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getDepartmentCode()).isEqualTo("DEPT123456789");
        assertThat(result.getName()).isEqualTo("Technical Department");
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void deleteDepartment_callsSoftDelete() {
        Department department = Department.builder()
                .id(1)
                .departmentCode("DEPT01")
                .name("Technical Department")
                .build();

        when(departmentRepository.findById(1)).thenReturn(Optional.of(department));

        departmentService.deleteDepartment(1);

        verify(softDeleteCascadeService).softDelete(department);
    }

    @Test
    void deleteDepartment_whenNotFound_throwsException() {
        when(departmentRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.deleteDepartment(1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Department not found");

        verify(softDeleteCascadeService, never()).softDelete(any(Department.class));
    }
}
