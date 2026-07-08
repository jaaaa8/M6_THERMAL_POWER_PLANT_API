package com.example.m6_thermal_power_plant_api.service.account;

import com.example.m6_thermal_power_plant_api.dto.accounts.AccountResponseDTO;
import com.example.m6_thermal_power_plant_api.dto.accounts.AccountSearchRequestDTO;
import com.example.m6_thermal_power_plant_api.entity.Account;
import com.example.m6_thermal_power_plant_api.entity.Employee;
import com.example.m6_thermal_power_plant_api.entity.Role;
import com.example.m6_thermal_power_plant_api.entity.enums.AccountStatus;
import com.example.m6_thermal_power_plant_api.repository.account.IAccountRepository;
import com.example.m6_thermal_power_plant_api.repository.account.IRoleRepository;
import com.example.m6_thermal_power_plant_api.repository.employee.IEmployeeRepository;
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
public class AccountSearchServiceDbTest {

    @MockBean
    private JavaMailSender javaMailSender;

    @Autowired
    private IAccountService accountService;

    @Autowired
    private IAccountRepository accountRepository;

    @Autowired
    private IEmployeeRepository employeeRepository;

    @Autowired
    private IRoleRepository roleRepository;

    @Autowired
    private EntityManager entityManager;

    private Role roleUser;
    private Role roleAdmin;

    private Employee emp1;
    private Employee emp2;

    private Account acc1;
    private Account acc2;
    private Account acc3;
    private Account acc4;

    @BeforeEach
    void setUp() {
        accountRepository.deleteAllInBatch();
        roleRepository.deleteAllInBatch();
        employeeRepository.deleteAllInBatch();

        // Create roles
        roleUser = Role.builder().name("ROLE_USER").build();
        roleUser = roleRepository.save(roleUser);

        roleAdmin = Role.builder().name("ROLE_ADMIN").build();
        roleAdmin = roleRepository.save(roleAdmin);

        // Create employees
        emp1 = Employee.builder()
                .employeeCode("TEST_EMP01")
                .fullName("Nguyen Van An")
                .gmail("an@gmail.com")
                .phone("0912345678")
                .isActive(true)
                .build();
        emp1 = employeeRepository.save(emp1);

        emp2 = Employee.builder()
                .employeeCode("TEST_EMP02")
                .fullName("Tran Thi Binh")
                .gmail("binh@yahoo.com")
                .phone("0987654321")
                .isActive(true)
                .build();
        emp2 = employeeRepository.save(emp2);

        // Create accounts
        acc1 = Account.builder()
                .username("user_an")
                .passwordHash("hash1")
                .email("an@gmail.com")
                .status(AccountStatus.ACTIVE)
                .employee(emp1)
                .roles(List.of(roleUser))
                .build();
        acc1.setIsDeleted(false);
        acc1 = accountRepository.save(acc1);

        acc2 = Account.builder()
                .username("admin_binh")
                .passwordHash("hash2")
                .email("binh@yahoo.com")
                .status(AccountStatus.ACTIVE)
                .employee(emp2)
                .roles(List.of(roleAdmin))
                .build();
        acc2.setIsDeleted(false);
        acc2 = accountRepository.save(acc2);

        acc3 = Account.builder()
                .username("locked_user")
                .passwordHash("hash3")
                .email("locked@gmail.com")
                .status(AccountStatus.LOCKED)
                .roles(List.of(roleUser))
                .build();
        acc3.setIsDeleted(false);
        acc3 = accountRepository.save(acc3);

        acc4 = Account.builder()
                .username("deleted_user")
                .passwordHash("hash4")
                .email("deleted@gmail.com")
                .status(AccountStatus.ACTIVE)
                .roles(List.of(roleUser))
                .build();
        acc4.softDelete();
        acc4 = accountRepository.save(acc4);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void search_byUsernameFuzzy() {
        AccountSearchRequestDTO request = AccountSearchRequestDTO.builder()
                .username("user")
                .build();
        Page<AccountResponseDTO> result = accountService.searchAccounts(request, PageRequest.of(0, 10));

        // Should return acc1 ("user_an") and acc3 ("locked_user") but not acc4 (deleted)
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(AccountResponseDTO::getUsername)
                .containsExactlyInAnyOrder("user_an", "locked_user");
    }

    @Test
    void search_byEmailFuzzy() {
        AccountSearchRequestDTO request = AccountSearchRequestDTO.builder()
                .email("gmail.com")
                .build();
        Page<AccountResponseDTO> result = accountService.searchAccounts(request, PageRequest.of(0, 10));

        // Should return acc1 and acc3
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(AccountResponseDTO::getUsername)
                .containsExactlyInAnyOrder("user_an", "locked_user");
    }

    @Test
    void search_byStatus() {
        AccountSearchRequestDTO request = AccountSearchRequestDTO.builder()
                .status(AccountStatus.LOCKED)
                .build();
        Page<AccountResponseDTO> result = accountService.searchAccounts(request, PageRequest.of(0, 10));

        // Should return acc3
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("locked_user");
    }

    @Test
    void search_byRoleId() {
        AccountSearchRequestDTO request = AccountSearchRequestDTO.builder()
                .roleId(roleAdmin.getId())
                .build();
        Page<AccountResponseDTO> result = accountService.searchAccounts(request, PageRequest.of(0, 10));

        // Should return acc2
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("admin_binh");
    }

    @Test
    void search_byEmployeeNameFuzzy() {
        AccountSearchRequestDTO request = AccountSearchRequestDTO.builder()
                .employeeName("binh")
                .build();
        Page<AccountResponseDTO> result = accountService.searchAccounts(request, PageRequest.of(0, 10));

        // Should return acc2 (linked to Tran Thi Binh)
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("admin_binh");
    }

    @Test
    void search_combinedCriteria() {
        AccountSearchRequestDTO request = AccountSearchRequestDTO.builder()
                .status(AccountStatus.ACTIVE)
                .roleId(roleUser.getId())
                .build();
        Page<AccountResponseDTO> result = accountService.searchAccounts(request, PageRequest.of(0, 10));

        // Should return acc1 only
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("user_an");
    }

    @Test
    void getAccountById_success() {
        AccountResponseDTO dto = accountService.getAccountById(acc1.getId());
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(acc1.getId());
        assertThat(dto.getUsername()).isEqualTo("user_an");
        assertThat(dto.getEmail()).isEqualTo("an@gmail.com");
        assertThat(dto.getEmployee()).isNotNull();
        assertThat(dto.getEmployee().getFullName()).isEqualTo("Nguyen Van An");
    }

    @Test
    void getAccountById_notFoundOrDeleted() {
        // Deleted account (acc4) should throw exception
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> accountService.getAccountById(acc4.getId()))
                .isInstanceOf(com.example.m6_thermal_power_plant_api.exception.ResourceNotFoundException.class);

        // Non-existent ID should throw exception
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> accountService.getAccountById(99999))
                .isInstanceOf(com.example.m6_thermal_power_plant_api.exception.ResourceNotFoundException.class);
    }
}
