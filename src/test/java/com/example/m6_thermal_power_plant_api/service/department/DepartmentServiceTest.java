package com.example.m6_thermal_power_plant_api.service.department;

import com.example.m6_thermal_power_plant_api.dto.employee.DepartmentDTO;
import com.example.m6_thermal_power_plant_api.dto.employee.DepartmentCreateDTO;
import com.example.m6_thermal_power_plant_api.dto.employee.DepartmentUpdateDTO;
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
                .departmentCode("DEPT")
                .name("Technical Department")
                .description("Handles technical issues")
                .build();

        Department department = Department.builder()
                .id(1)
                .departmentCode("DEPT")
                .name("Technical Department")
                .description("Handles technical issues")
                .build();

        when(departmentRepository.existsByDepartmentCode(anyString())).thenReturn(false);
        when(departmentRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenReturn(department);

        DepartmentDTO result = departmentService.createDepartment(createDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getDepartmentCode()).isEqualTo("DEPT");
        assertThat(result.getName()).isEqualTo("Technical Department");
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void createDepartment_duplicateCode_throwsDuplicateResourceException() {
        DepartmentCreateDTO createDTO = DepartmentCreateDTO.builder()
                .departmentCode("DEPT")
                .name("Technical Department")
                .description("Handles technical issues")
                .build();

        when(departmentRepository.existsByDepartmentCode("DEPT")).thenReturn(true);

        assertThatThrownBy(() -> departmentService.createDepartment(createDTO))
                .isInstanceOf(com.example.m6_thermal_power_plant_api.exception.DuplicateResourceException.class)
                .hasMessageContaining("Mã phòng ban 'DEPT' đã tồn tại.");

        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    void createDepartment_duplicateName_throwsDuplicateResourceException() {
        DepartmentCreateDTO createDTO = DepartmentCreateDTO.builder()
                .departmentCode("DEPT")
                .name("Technical Department")
                .description("Handles technical issues")
                .build();

        when(departmentRepository.existsByDepartmentCode("DEPT")).thenReturn(false);
        when(departmentRepository.existsByNameIgnoreCase("Technical Department")).thenReturn(true);

        assertThatThrownBy(() -> departmentService.createDepartment(createDTO))
                .isInstanceOf(com.example.m6_thermal_power_plant_api.exception.DuplicateResourceException.class)
                .hasMessageContaining("Tên phòng ban 'Technical Department' đã tồn tại.");

        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    void updateDepartment_duplicateName_throwsDuplicateResourceException() {
        DepartmentUpdateDTO updateDTO = DepartmentUpdateDTO.builder()
                .name("Technical Department")
                .description("Updated description")
                .build();

        Department existingDepartment = Department.builder()
                .id(1)
                .departmentCode("DEPT01")
                .name("Old Department")
                .description("Handles technical issues")
                .build();

        when(departmentRepository.findById(1)).thenReturn(Optional.of(existingDepartment));
        when(departmentRepository.existsByNameIgnoreCaseAndIdNot("Technical Department", 1)).thenReturn(true);

        assertThatThrownBy(() -> departmentService.updateDepartment(1, updateDTO))
                .isInstanceOf(com.example.m6_thermal_power_plant_api.exception.DuplicateResourceException.class)
                .hasMessageContaining("Tên phòng ban 'Technical Department' đã tồn tại.");

        verify(departmentRepository, never()).save(any(Department.class));
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

    @Test
    void updateDepartment_whenExists_updatesAndReturnsDTO() {
        DepartmentUpdateDTO updateDTO = DepartmentUpdateDTO.builder()
                .name("Updated Department")
                .description("Updated description")
                .build();

        Department existingDepartment = Department.builder()
                .id(1)
                .departmentCode("DEPT01")
                .name("Technical Department")
                .description("Handles technical issues")
                .build();

        Department updatedDepartment = Department.builder()
                .id(1)
                .departmentCode("DEPT01")
                .name("Updated Department")
                .description("Updated description")
                .build();

        when(departmentRepository.findById(1)).thenReturn(Optional.of(existingDepartment));
        when(departmentRepository.save(any(Department.class))).thenReturn(updatedDepartment);

        DepartmentDTO result = departmentService.updateDepartment(1, updateDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("Updated Department");
        assertThat(result.getDescription()).isEqualTo("Updated description");
        verify(departmentRepository).save(existingDepartment);
    }

    @Test
    void updateDepartment_whenNotFound_throwsException() {
        DepartmentUpdateDTO updateDTO = DepartmentUpdateDTO.builder()
                .name("Updated Department")
                .build();

        when(departmentRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.updateDepartment(1, updateDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Department not found");

        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    void getDepartmentById_whenExists_returnsDTO() {
        Department department = Department.builder()
                .id(1)
                .departmentCode("DEPT01")
                .name("Technical Department")
                .description("Handles technical issues")
                .build();

        when(departmentRepository.findById(1)).thenReturn(Optional.of(department));

        DepartmentDTO result = departmentService.getDepartmentById(1);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("Technical Department");
        assertThat(result.getDescription()).isEqualTo("Handles technical issues");
    }

    @Test
    void getDepartmentById_whenNotFound_throwsException() {
        when(departmentRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.getDepartmentById(1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Department not found");
    }
}
