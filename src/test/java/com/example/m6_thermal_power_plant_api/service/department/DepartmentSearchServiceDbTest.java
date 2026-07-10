package com.example.m6_thermal_power_plant_api.service.department;

import com.example.m6_thermal_power_plant_api.dto.employee.DepartmentDTO;
import com.example.m6_thermal_power_plant_api.dto.employee.DepartmentSearchRequestDTO;
import com.example.m6_thermal_power_plant_api.entity.Department;
import com.example.m6_thermal_power_plant_api.repository.department.IDepartmentRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class DepartmentSearchServiceDbTest {

    @MockBean
    private JavaMailSender javaMailSender;

    @Autowired
    private IDepartmentService departmentService;

    @Autowired
    private IDepartmentRepository departmentRepository;

    @Autowired
    private EntityManager entityManager;

    private Department dept1;
    private Department dept2;
    private Department dept3;
    private Department dept4;

    @BeforeEach
    void setUp() {
        departmentRepository.deleteAllInBatch();

        dept1 = Department.builder()
                .departmentCode("TEST_DEPT01")
                .name("Technical Department")
                .description("Desc 1")
                .build();
        dept1.setIsDeleted(false);
        dept1 = departmentRepository.save(dept1);

        dept2 = Department.builder()
                .departmentCode("TEST_DEPT02")
                .name("Human Resources")
                .description("Desc 2")
                .build();
        dept2.setIsDeleted(false);
        dept2 = departmentRepository.save(dept2);

        dept3 = Department.builder()
                .departmentCode("DEPT_ANOTHER")
                .name("Technical operations")
                .description("Desc 3")
                .build();
        dept3.setIsDeleted(false);
        dept3 = departmentRepository.save(dept3);

        dept4 = Department.builder()
                .departmentCode("DEPT_DELETED")
                .name("Deleted Department")
                .description("Desc 4")
                .build();
        dept4.softDelete();
        dept4 = departmentRepository.save(dept4);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void search_byDepartmentCodeFuzzy() {
        DepartmentSearchRequestDTO request = DepartmentSearchRequestDTO.builder()
                .departmentCode("TEST")
                .build();
        Page<DepartmentDTO> result = departmentService.searchDepartments(request, PageRequest.of(0, 10));

        // Should return dept1 ("TEST_DEPT01") and dept2 ("TEST_DEPT02") but not dept4 (deleted)
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(DepartmentDTO::getDepartmentCode)
                .containsExactlyInAnyOrder("TEST_DEPT01", "TEST_DEPT02");
    }

    @Test
    void search_byNameFuzzy() {
        DepartmentSearchRequestDTO request = DepartmentSearchRequestDTO.builder()
                .name("Technical")
                .build();
        Page<DepartmentDTO> result = departmentService.searchDepartments(request, PageRequest.of(0, 10));

        // Should return dept1 and dept3
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(DepartmentDTO::getName)
                .containsExactlyInAnyOrder("Technical Department", "Technical operations");
    }

    @Test
    void search_combinedCriteria() {
        DepartmentSearchRequestDTO request = DepartmentSearchRequestDTO.builder()
                .departmentCode("ANOTHER")
                .name("technical")
                .build();
        Page<DepartmentDTO> result = departmentService.searchDepartments(request, PageRequest.of(0, 10));

        // Should return dept3 only ("DEPT_ANOTHER", "Technical operations")
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getDepartmentCode()).isEqualTo("DEPT_ANOTHER");
    }
}
