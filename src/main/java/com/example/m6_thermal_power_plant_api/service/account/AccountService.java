package com.example.m6_thermal_power_plant_api.service.account;

import com.example.m6_thermal_power_plant_api.dto.accounts.AccountDTO;
import com.example.m6_thermal_power_plant_api.dto.accounts.AccountResponseDTO;
import com.example.m6_thermal_power_plant_api.dto.accounts.WorkerAccountRequest;
import com.example.m6_thermal_power_plant_api.dto.accounts.WorkerAccountResponse;
import com.example.m6_thermal_power_plant_api.entity.Account;
import com.example.m6_thermal_power_plant_api.entity.Employee;
import com.example.m6_thermal_power_plant_api.entity.Role;
import com.example.m6_thermal_power_plant_api.repository.account.IAccountRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService implements IAccountService {

    private final IAccountRepository accountRepository;
    private final com.example.m6_thermal_power_plant_api.repository.employee.IEmployeeRepository employeeRepository;
    private final com.example.m6_thermal_power_plant_api.repository.RoleRepository roleRepository;
    private final EntityManager entityManager;
    private final org.springframework.mail.javamail.JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private AccountResponseDTO mapToResponseDTO(Account a) {
        if (a == null) return null;

        java.util.List<com.example.m6_thermal_power_plant_api.dto.accounts.RoleDTO> roleDTOs = new java.util.ArrayList<>();
        if (a.getRoles() != null) {
            for (com.example.m6_thermal_power_plant_api.entity.Role r : a.getRoles()) {
                try {
                    roleDTOs.add(com.example.m6_thermal_power_plant_api.dto.accounts.RoleDTO.builder()
                            .id(r.getId())
                            .name(r.getName())
                            .build());
                } catch (jakarta.persistence.EntityNotFoundException ex) {
                    // ignore soft-deleted role
                }
            }
        }

        AccountResponseDTO.EmployeeInfo employeeInfo = null;
        try {
            Employee emp = a.getEmployee();
            if (emp != null) {
                emp.getFullName(); // trigger lazy load
                employeeInfo = AccountResponseDTO.EmployeeInfo.builder()
                        .id(emp.getId())
                        .fullName(emp.getFullName())
                        .gmail(emp.getGmail())
                        .build();
            }
        } catch (jakarta.persistence.EntityNotFoundException ex) {
            // ignore soft-deleted employee
        }

        return AccountResponseDTO.builder()
                .id(a.getId())
                .username(a.getUsername())
                .email(a.getEmail())
                .status(a.getStatus())
                .roles(roleDTOs)
                .employee(employeeInfo)
                .build();
    }

    public List<AccountResponseDTO> getAllAccounts() {
        return accountRepository.findAll().stream()
                .filter(a -> !Boolean.TRUE.equals(a.getIsDeleted()))
                .map(this::mapToResponseDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    public AccountResponseDTO createAccount(AccountDTO dto) {
        boolean hasEmployeeId = dto.getEmployeeId() != null;
        boolean hasEmail = dto.getEmail() != null && !dto.getEmail().trim().isEmpty();

        if (!hasEmployeeId && !hasEmail) {
            throw new IllegalArgumentException("Must provide either employeeId or email.");
        }

        if (hasEmployeeId && hasEmail) {
            throw new IllegalArgumentException("Cannot provide both employeeId and email.");
        }

        if (accountRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + dto.getUsername());
        }

        if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
            if (accountRepository.existsByEmail(dto.getEmail().trim())) {
                throw new IllegalArgumentException("Email already exists in another account.");
            }
            if (employeeRepository.existsByGmail(dto.getEmail().trim())) {
                throw new IllegalArgumentException("Email already exists in employee records.");
            }
        }

        Account account = new Account();
        account.setUsername(dto.getUsername());
        account.setEmail(dto.getEmail());
        // Mật khẩu sẽ được hệ thống tạo ngẫu nhiên
        String plainPassword = generateRandomPassword(8);
        account.setPasswordHash(passwordEncoder.encode(plainPassword));
        
        if (dto.getEmployeeId() != null) {
            if (accountRepository.existsByEmployeeId(dto.getEmployeeId())) {
                throw new IllegalArgumentException("Employee already has an account: " + dto.getEmployeeId());
            }
            Employee emp = employeeRepository.findById(dto.getEmployeeId())
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
            account.setEmployee(emp);
            
            // Gán email bằng email của employee
            if (emp.getGmail() != null && !emp.getGmail().isEmpty()) {
                account.setEmail(emp.getGmail());
            }
        }

        if (dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {
            java.util.Set<com.example.m6_thermal_power_plant_api.entity.Role> roles = dto.getRoleIds().stream()
                    .map(id -> entityManager.getReference(com.example.m6_thermal_power_plant_api.entity.Role.class, id))
                    .collect(java.util.stream.Collectors.toSet());
            account.setRoles(roles);
        }

        Account savedAccount = accountRepository.save(account);
        accountRepository.flush();

        if (account.getEmail() != null && !account.getEmail().isEmpty()) {
            sendAccountInfoEmailAsync(account.getEmail(), dto.getUsername(), plainPassword);
        }

        return mapToResponseDTO(savedAccount);
    }

    @Transactional
    public AccountResponseDTO grantAccount(com.example.m6_thermal_power_plant_api.dto.accounts.AccountGrantRequestDTO request) {
        if (accountRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new IllegalArgumentException("Employee already has an account: " + request.getEmployeeId());
        }

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found for id: " + request.getEmployeeId()));
        
        String username = employee.getGmail();
        if (accountRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username (email) already exists: " + username);
        }

        com.example.m6_thermal_power_plant_api.entity.Role role = entityManager.getReference(com.example.m6_thermal_power_plant_api.entity.Role.class, request.getRoleId());
        
        Account account = new Account();
        account.setUsername(username);
        account.setEmail(username); // employee.getGmail()
        String plainPassword = generateRandomPassword(8);
        account.setPasswordHash(passwordEncoder.encode(plainPassword));
        account.setEmployee(employee);
        account.setStatus(com.example.m6_thermal_power_plant_api.entity.enums.AccountStatus.ACTIVE);
        account.setRoles(java.util.Collections.singleton(role));

        Account savedAccount = accountRepository.save(account);
        accountRepository.flush();

        if (username != null && !username.isEmpty()) {
            sendAccountInfoEmailAsync(username, username, plainPassword);
        }

        return mapToResponseDTO(savedAccount);
    }

    private String generateRandomPassword(int length) {
        java.security.SecureRandom random = new java.security.SecureRandom();
        String letters = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String all = letters + digits;
        
        StringBuilder sb = new StringBuilder(length);
        // Đảm bảo có ít nhất 1 chữ thường và 1 số
        sb.append(letters.charAt(random.nextInt(letters.length())));
        sb.append(digits.charAt(random.nextInt(digits.length())));
        
        for (int i = 2; i < length; i++) {
            sb.append(all.charAt(random.nextInt(all.length())));
        }
        
        // Trộn ngẫu nhiên
        char[] chars = sb.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            char temp = chars[index];
            chars[index] = chars[i];
            chars[i] = temp;
        }
        return new String(chars);
    }

    private void sendAccountInfoEmailAsync(String to, String username, String password) {
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                org.springframework.mail.SimpleMailMessage message = new org.springframework.mail.SimpleMailMessage();
                message.setTo(to);
                message.setSubject("Thông tin tài khoản hệ thống");
                message.setText("Xin chào,\n\n"
                        + "Tài khoản của bạn đã được tạo/cấp thành công.\n"
                        + "Tên đăng nhập (Username): " + username + "\n"
                        + "Mật khẩu (Password): " + password + "\n\n"
                        + "Vui lòng đổi mật khẩu sau khi đăng nhập để bảo đảm an toàn.\n"
                        + "Trân trọng,\nBan Quản Trị");
                mailSender.send(message);
            } catch (Exception e) {
                // Ignore errors for now so it doesn't crash the transaction
                System.err.println("Failed to send email to " + to + ": " + e.getMessage());
            }
        });
    }

    @Transactional
    public WorkerAccountResponse createWorkerAccount(WorkerAccountRequest req) {
        if (accountRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("Username đã tồn tại: " + req.getUsername());
        }

        // Tạo employee record
        long empCount = employeeRepository.count();
        Employee emp = new Employee();
        emp.setFullName(req.getFullName());
        String gmail = (req.getEmail() != null && !req.getEmail().isBlank())
            ? req.getEmail() : req.getUsername() + "@demo.local";
        emp.setGmail(gmail);
        emp.setIsActive(true);
        emp.setEmployeeCode("NS" + String.format("%03d", empCount + 1));
        Employee savedEmp = employeeRepository.save(emp);

        // Tìm hoặc tạo role WORKER
        Role workerRole = roleRepository.findByName("WORKER").orElseGet(() -> {
            Role r = new Role();
            r.setName("WORKER");
            r.setIsDeleted(false);
            return roleRepository.save(r);
        });

        // Tạo account
        Account account = new Account();
        account.setUsername(req.getUsername());
        account.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        account.setEmployee(savedEmp);
        account.setStatus(com.example.m6_thermal_power_plant_api.entity.enums.AccountStatus.ACTIVE);
        account.setIsDeleted(false);
        account.setRoles(new java.util.HashSet<>(java.util.List.of(workerRole)));
        Account savedAccount = accountRepository.save(account);
        accountRepository.flush();

        return WorkerAccountResponse.builder()
            .accountId(savedAccount.getId())
            .username(req.getUsername())
            .password(req.getPassword())
            .fullName(req.getFullName())
            .email(gmail)
            .build();
    }

    @Transactional
    public AccountResponseDTO updateStatus(com.example.m6_thermal_power_plant_api.dto.accounts.AccountStatusUpdateRequestDTO request) {
        Account account = accountRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Account not found with username: " + request.getUsername()));
        
        account.setStatus(request.getStatus());
        Account saved = accountRepository.save(account);
        accountRepository.flush();
        return mapToResponseDTO(saved);
    }

    public org.springframework.data.domain.Page<AccountResponseDTO> searchAccounts(
            com.example.m6_thermal_power_plant_api.dto.accounts.AccountSearchRequestDTO searchRequest,
            org.springframework.data.domain.Pageable pageable
    ) {
        org.springframework.data.jpa.domain.Specification<Account> spec = (root, query, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();

            predicates.add(cb.equal(root.get("isDeleted"), false));

            if (searchRequest.getUsername() != null && !searchRequest.getUsername().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("username")), "%" + searchRequest.getUsername().trim().toLowerCase() + "%"));
            }

            if (searchRequest.getEmail() != null && !searchRequest.getEmail().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("email")), "%" + searchRequest.getEmail().trim().toLowerCase() + "%"));
            }

            if (searchRequest.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), searchRequest.getStatus()));
            }

            if (searchRequest.getRoleId() != null) {
                jakarta.persistence.criteria.Join<Account, com.example.m6_thermal_power_plant_api.entity.Role> roleJoin = root.join("roles");
                predicates.add(cb.equal(roleJoin.get("id"), searchRequest.getRoleId()));
            }

            if (searchRequest.getEmployeeName() != null && !searchRequest.getEmployeeName().trim().isEmpty()) {
                jakarta.persistence.criteria.Join<Account, Employee> employeeJoin = root.join("employee");
                predicates.add(cb.like(cb.lower(employeeJoin.get("fullName")), "%" + searchRequest.getEmployeeName().trim().toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return accountRepository.findAll(spec, pageable).map(this::mapToResponseDTO);
    }

    public AccountResponseDTO getAccountById(Integer id) {
        Account account = accountRepository.findById(id)
                .filter(a -> !Boolean.TRUE.equals(a.getIsDeleted()))
                .orElseThrow(() -> new com.example.m6_thermal_power_plant_api.exception.ResourceNotFoundException("Account not found with id: " + id));
        return mapToResponseDTO(account);
    }

    @Transactional
    @Override
    public AccountResponseDTO updateAccount(Integer id, AccountDTO dto) {
        Account account = accountRepository.findById(id)
                .filter(a -> !Boolean.TRUE.equals(a.getIsDeleted()))
                .orElseThrow(() -> new com.example.m6_thermal_power_plant_api.exception.ResourceNotFoundException("Account not found with id: " + id));

        // If it's an external account (employee is null), we allow editing the email
        if (account.getEmployee() == null) {
            String newEmail = dto.getEmail();
            if (newEmail != null && !newEmail.trim().isEmpty()) {
                newEmail = newEmail.trim();
                if (!newEmail.equalsIgnoreCase(account.getEmail())) {
                    if (accountRepository.existsByEmail(newEmail)) {
                        throw new IllegalArgumentException("Email already exists in another account.");
                    }
                    if (employeeRepository.existsByGmail(newEmail)) {
                        throw new IllegalArgumentException("Email already exists in employee records.");
                    }
                    account.setEmail(newEmail);
                }
            }
        }

        // Update roles
        if (dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {
            java.util.List<com.example.m6_thermal_power_plant_api.entity.Role> roles = dto.getRoleIds().stream()
                    .map(roleId -> entityManager.getReference(com.example.m6_thermal_power_plant_api.entity.Role.class, roleId))
                    .collect(java.util.stream.Collectors.toList());
            account.setRoles(roles);
        }

        Account saved = accountRepository.save(account);
        accountRepository.flush();
        return mapToResponseDTO(saved);
    }

    @Transactional
    @Override
    public void resetPassword(Integer id) {
        Account account = accountRepository.findById(id)
                .filter(a -> !Boolean.TRUE.equals(a.getIsDeleted()))
                .orElseThrow(() -> new com.example.m6_thermal_power_plant_api.exception.ResourceNotFoundException("Account not found with id: " + id));

        String plainPassword = generateRandomPassword(8);
        account.setPasswordHash(passwordEncoder.encode(plainPassword));
        accountRepository.save(account);
        accountRepository.flush();

        if (account.getEmail() != null && !account.getEmail().isEmpty()) {
            sendAccountInfoEmailAsync(account.getEmail(), account.getUsername(), plainPassword);
        }
    }
}
