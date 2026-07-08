package com.example.m6_thermal_power_plant_api.service.employee;

import com.example.m6_thermal_power_plant_api.dto.employee.EmployeeResponseDTO;
import com.example.m6_thermal_power_plant_api.dto.employee.EmployeeSearchRequestDTO;
import com.example.m6_thermal_power_plant_api.entity.Department;
import com.example.m6_thermal_power_plant_api.entity.Employee;
import com.example.m6_thermal_power_plant_api.repository.department.IDepartmentRepository;
import com.example.m6_thermal_power_plant_api.repository.employee.IEmployeeRepository;
import com.example.m6_thermal_power_plant_api.repository.account.IAccountRepository;
import com.example.m6_thermal_power_plant_api.repository.account.IRoleRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class EmployeeSearchServiceDbTest {

    @MockBean
    private JavaMailSender javaMailSender;

    @Autowired
    private IEmployeeService employeeService;

    @Autowired
    private IEmployeeRepository employeeRepository;

    @Autowired
    private IDepartmentRepository departmentRepository;

    @Autowired
    private IAccountRepository accountRepository;

    @Autowired
    private IRoleRepository roleRepository;

    @Autowired
    private EntityManager entityManager;

    private Department dept1;
    private Department dept2;

    private Employee emp1;
    private Employee emp2;
    private Employee emp3;
    private Employee emp4;

    @BeforeEach
    void setUp() {
        // Clear database before test in dependency order
        accountRepository.deleteAllInBatch();
        roleRepository.deleteAllInBatch();
        employeeRepository.deleteAllInBatch();
        departmentRepository.deleteAllInBatch();

        // Create departments
        dept1 = Department.builder()
                .departmentCode("TEST_DEPT01")
                .name("Technical Department")
                .build();
        dept1 = departmentRepository.save(dept1);

        dept2 = Department.builder()
                .departmentCode("TEST_DEPT02")
                .name("HR Department")
                .build();
        dept2 = departmentRepository.save(dept2);

        // Create employees
        emp1 = Employee.builder()
                .employeeCode("TEST_EMP01")
                .fullName("Nguyen Van An")
                .gmail("an@gmail.com")
                .phone("0912345678")
                .department(dept1)
                .isActive(true)
                .build();
        emp1.setIsDeleted(false);
        emp1 = employeeRepository.save(emp1);

        emp2 = Employee.builder()
                .employeeCode("TEST_EMP02")
                .fullName("Tran Thi Binh")
                .gmail("binh@yahoo.com")
                .phone("0987654321")
                .department(dept1)
                .isActive(false)
                .build();
        emp2.setIsDeleted(false);
        emp2 = employeeRepository.save(emp2);

        emp3 = Employee.builder()
                .employeeCode("TEST_EMP03")
                .fullName("Le Van Cuong")
                .gmail("cuong@gmail.com")
                .phone("0900000000")
                .department(dept2)
                .isActive(true)
                .build();
        emp3.setIsDeleted(false);
        emp3 = employeeRepository.save(emp3);

        emp4 = Employee.builder()
                .employeeCode("TEST_EMP04")
                .fullName("Nguyen Van An Deleted")
                .gmail("an@gmail.com")
                .phone("0912345678")
                .department(dept1)
                .isActive(true)
                .build();
        emp4.softDelete(); // Marks isDeleted = true
        emp4 = employeeRepository.save(emp4);
    }

    @Test
    void search_byNameFuzzyCaseInsensitive() {
        EmployeeSearchRequestDTO request = EmployeeSearchRequestDTO.builder()
                .name("an")
                .build();
        Page<EmployeeResponseDTO> result = employeeService.searchEmployees(request, PageRequest.of(0, 10));

        // Should return emp1 and emp3 ("Le Van Cuong" has "Van") but not emp4 (deleted)
        List<EmployeeResponseDTO> content = result.getContent();
        assertThat(content).hasSize(3);
        assertThat(content).extracting(EmployeeResponseDTO::getFullName)
                .containsExactlyInAnyOrder("Nguyen Van An", "Le Van Cuong", "Tran Thi Binh");
        
        // Exact name test
        request = EmployeeSearchRequestDTO.builder()
                .name("Nguyen Van An")
                .build();
        result = employeeService.searchEmployees(request, PageRequest.of(0, 10));
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFullName()).isEqualTo("Nguyen Van An");
    }

    @Test
    void search_byPhoneFuzzy() {
        EmployeeSearchRequestDTO request = EmployeeSearchRequestDTO.builder()
                .phone("123")
                .build();
        Page<EmployeeResponseDTO> result = employeeService.searchEmployees(request, PageRequest.of(0, 10));

        // Should return emp1 (0912345678)
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFullName()).isEqualTo("Nguyen Van An");
    }

    @Test
    void search_byGmailFuzzy() {
        EmployeeSearchRequestDTO request = EmployeeSearchRequestDTO.builder()
                .gmail("gmail.com")
                .build();
        Page<EmployeeResponseDTO> result = employeeService.searchEmployees(request, PageRequest.of(0, 10));

        // Should return emp1 and emp3
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(EmployeeResponseDTO::getFullName)
                .containsExactlyInAnyOrder("Nguyen Van An", "Le Van Cuong");
    }

    @Test
    void search_byDepartmentId() {
        EmployeeSearchRequestDTO request = EmployeeSearchRequestDTO.builder()
                .departmentId(dept1.getId())
                .build();
        Page<EmployeeResponseDTO> result = employeeService.searchEmployees(request, PageRequest.of(0, 10));

        // Should return emp1 and emp2
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(EmployeeResponseDTO::getFullName)
                .containsExactlyInAnyOrder("Nguyen Van An", "Tran Thi Binh");
    }

    @Test
    void search_byIsActive() {
        EmployeeSearchRequestDTO request = EmployeeSearchRequestDTO.builder()
                .isActive(false)
                .build();
        Page<EmployeeResponseDTO> result = employeeService.searchEmployees(request, PageRequest.of(0, 10));

        // Should return emp2
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFullName()).isEqualTo("Tran Thi Binh");
    }

    @Test
    void search_byMultipleCriteriaCombined() {
        EmployeeSearchRequestDTO request = EmployeeSearchRequestDTO.builder()
                .name("Van")
                .departmentId(dept1.getId())
                .isActive(true)
                .build();
        Page<EmployeeResponseDTO> result = employeeService.searchEmployees(request, PageRequest.of(0, 10));

        // Should return emp1 only
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFullName()).isEqualTo("Nguyen Van An");
    }

    @Test
    void search_emptyCriteria_returnsAllActive() {
        EmployeeSearchRequestDTO request = EmployeeSearchRequestDTO.builder().build();
        Page<EmployeeResponseDTO> result = employeeService.searchEmployees(request, PageRequest.of(0, 10));

        // Should return all active non-deleted employees (emp1, emp2, emp3)
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent()).extracting(EmployeeResponseDTO::getFullName)
                .containsExactlyInAnyOrder("Nguyen Van An", "Tran Thi Binh", "Le Van Cuong");
    }

    @Test
    void search_returnsAccountInfo() {
        // Create a role
        com.example.m6_thermal_power_plant_api.entity.Role role = com.example.m6_thermal_power_plant_api.entity.Role.builder()
                .name("ROLE_USER")
                .build();
        role = roleRepository.save(role);

        // Create an account for emp1
        com.example.m6_thermal_power_plant_api.entity.Account account = com.example.m6_thermal_power_plant_api.entity.Account.builder()
                .employee(emp1)
                .username("testuser")
                .passwordHash("hashedpassword")
                .email("an@gmail.com")
                .status(com.example.m6_thermal_power_plant_api.entity.enums.AccountStatus.ACTIVE)
                .roles(java.util.Set.of(role))
                .build();
        account = accountRepository.save(account);

        entityManager.flush();
        entityManager.clear();

        // Search for emp1
        EmployeeSearchRequestDTO request = EmployeeSearchRequestDTO.builder()
                .name("Nguyen Van An")
                .build();
        Page<EmployeeResponseDTO> result = employeeService.searchEmployees(request, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        EmployeeResponseDTO responseDTO = result.getContent().get(0);
        assertThat(responseDTO.getFullName()).isEqualTo("Nguyen Van An");

        // Verify account mapping
        assertThat(responseDTO.getAccount()).isNotNull();
        assertThat(responseDTO.getAccount().getUsername()).isEqualTo("testuser");
        assertThat(responseDTO.getAccount().getEmail()).isEqualTo("an@gmail.com");
        assertThat(responseDTO.getAccount().getStatus()).isEqualTo(com.example.m6_thermal_power_plant_api.entity.enums.AccountStatus.ACTIVE);
        assertThat(responseDTO.getAccount().getRoles()).hasSize(1);
        assertThat(responseDTO.getAccount().getRoles().get(0).getName()).isEqualTo("ROLE_USER");

        // Verify that emp2 search returns null account
        request = EmployeeSearchRequestDTO.builder()
                .name("Tran Thi Binh")
                .build();
        result = employeeService.searchEmployees(request, PageRequest.of(0, 10));
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAccount()).isNull();
    }

    @Test
    void getEmployeeById_returnsDetailsAndAccountInfo() {
        // Create a role and account for emp1
        com.example.m6_thermal_power_plant_api.entity.Role role = com.example.m6_thermal_power_plant_api.entity.Role.builder()
                .name("ROLE_ADMIN")
                .build();
        role = roleRepository.save(role);

        com.example.m6_thermal_power_plant_api.entity.Account account = com.example.m6_thermal_power_plant_api.entity.Account.builder()
                .employee(emp1)
                .username("adminuser")
                .passwordHash("hashedpwd")
                .email("admin@gmail.com")
                .status(com.example.m6_thermal_power_plant_api.entity.enums.AccountStatus.ACTIVE)
                .roles(java.util.Set.of(role))
                .build();
        account = accountRepository.save(account);

        entityManager.flush();
        entityManager.clear();

        // Get details by ID
        EmployeeResponseDTO response = employeeService.getEmployeeById(emp1.getId());
        assertThat(response).isNotNull();
        assertThat(response.getFullName()).isEqualTo("Nguyen Van An");

        // Verify account is populated
        assertThat(response.getAccount()).isNotNull();
        assertThat(response.getAccount().getUsername()).isEqualTo("adminuser");
        assertThat(response.getAccount().getRoles()).hasSize(1);
        assertThat(response.getAccount().getRoles().get(0).getName()).isEqualTo("ROLE_ADMIN");

        // Get details of employee without account
        EmployeeResponseDTO responseNoAccount = employeeService.getEmployeeById(emp2.getId());
        assertThat(responseNoAccount).isNotNull();
        assertThat(responseNoAccount.getAccount()).isNull();

        // Expect ResourceNotFoundException for non-existent or soft-deleted ID
        assertThat(emp4.getIsDeleted()).isTrue();
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> employeeService.getEmployeeById(emp4.getId()))
                .isInstanceOf(com.example.m6_thermal_power_plant_api.exception.ResourceNotFoundException.class);
    }
}
