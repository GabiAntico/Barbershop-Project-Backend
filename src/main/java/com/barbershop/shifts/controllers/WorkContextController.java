package com.barbershop.shifts.controllers;

import com.barbershop.shifts.dtos.context.BranchResponse;
import com.barbershop.shifts.dtos.context.CreateBranchRequest;
import com.barbershop.shifts.dtos.context.CreateEmployeeRequest;
import com.barbershop.shifts.dtos.context.EmployeeResponse;
import com.barbershop.shifts.dtos.context.UpdateEmployeeBranchesRequest;
import com.barbershop.shifts.dtos.context.WorkContextResponse;
import com.barbershop.shifts.entities.Barbershop;
import com.barbershop.shifts.entities.Branch;
import com.barbershop.shifts.entities.User;
import com.barbershop.shifts.repositories.BranchRepositoryJpa;
import com.barbershop.shifts.repositories.UserRepository;
import com.barbershop.shifts.services.CurrentUserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping("/api/work-context")
public class WorkContextController {

    private final CurrentUserService currentUserService;
    private final BranchRepositoryJpa branchRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public WorkContextController(
            CurrentUserService currentUserService,
            BranchRepositoryJpa branchRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.currentUserService = currentUserService;
        this.branchRepository = branchRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public WorkContextResponse getWorkContext() {
        User user = currentUserService.getCurrentUser();
        WorkContextResponse response = new WorkContextResponse();
        response.setBarbershopId(user.getBarbershop().getId());
        response.setBarbershopName(user.getBarbershop().getName());
        response.setUserRole(user.getRole());
        response.setTemporaryPassword(Boolean.TRUE.equals(user.getTemporaryPassword()));
        response.setBranches(getAvailableBranches(user).stream().map(this::toBranchResponse).toList());
        return response;
    }

    @GetMapping("/branches")
    public List<BranchResponse> getBranches() {
        User user = requireAdmin();
        return branchRepository.findAllByBarbershop(user.getBarbershop()).stream()
                .map(this::toBranchResponse)
                .toList();
    }

    @PostMapping("/branches")
    @ResponseStatus(HttpStatus.CREATED)
    public BranchResponse createBranch(@RequestBody CreateBranchRequest request) {
        User user = requireAdmin();
        Branch branch = new Branch();
        branch.setName(normalizeRequired(request.getName(), "Branch name is required"));
        branch.setAddress(normalize(request.getAddress()));
        branch.setBarbershop(user.getBarbershop());
        branch = branchRepository.save(branch);
        user.getBranches().add(branch);
        userRepository.save(user);
        return toBranchResponse(branch);
    }

    @GetMapping("/employees")
    public List<EmployeeResponse> getEmployees() {
        User user = requireAdmin();
        return userRepository.findAllByBarbershop(user.getBarbershop()).stream()
                .map(this::toEmployeeResponse)
                .toList();
    }

    @PostMapping("/employees")
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse createEmployee(@RequestBody CreateEmployeeRequest request) {
        User admin = requireAdmin();
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }

        User employee = new User();
        employee.setEmail(normalizeRequired(request.getEmail(), "Email is required"));
        employee.setDisplayName(normalize(request.getDisplayName()));
        employee.setRole("EMPLOYEE");
        employee.setTemporaryPassword(true);
        employee.setPassword(passwordEncoder.encode(normalizeRequired(request.getTemporaryPassword(), "Temporary password is required")));
        employee.setBarbershop(admin.getBarbershop());
        employee.setBranches(new HashSet<>(getBranchesByIds(admin.getBarbershop(), request.getBranchIds())));
        return toEmployeeResponse(userRepository.save(employee));
    }

    @PutMapping("/employees/{id}/branches")
    public EmployeeResponse updateEmployeeBranches(
            @PathVariable Long id,
            @RequestBody UpdateEmployeeBranchesRequest request
    ) {
        User admin = requireAdmin();
        User employee = userRepository.findByIdAndBarbershop(id, admin.getBarbershop())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        employee.setBranches(new HashSet<>(getBranchesByIds(admin.getBarbershop(), request.getBranchIds())));
        return toEmployeeResponse(userRepository.save(employee));
    }

    private List<Branch> getAvailableBranches(User user) {
        if (currentUserService.isAdmin(user)) {
            return branchRepository.findAllByBarbershop(user.getBarbershop());
        }
        return user.getBranches().stream().sorted(java.util.Comparator.comparing(Branch::getId)).toList();
    }

    private List<Branch> getBranchesByIds(Barbershop barbershop, List<Long> branchIds) {
        if (branchIds == null || branchIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one branch is required");
        }

        return branchIds.stream()
                .map(id -> branchRepository.findByIdAndBarbershop(id, barbershop)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid branch")))
                .toList();
    }

    private User requireAdmin() {
        User user = currentUserService.getCurrentUser();
        if (!currentUserService.isAdmin(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can manage this resource");
        }
        return user;
    }

    private BranchResponse toBranchResponse(Branch branch) {
        BranchResponse response = new BranchResponse();
        response.setId(branch.getId());
        response.setName(branch.getName());
        response.setAddress(branch.getAddress());
        return response;
    }

    private EmployeeResponse toEmployeeResponse(User user) {
        EmployeeResponse response = new EmployeeResponse();
        response.setId(user.getId());
        response.setDisplayName(user.getDisplayName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setTemporaryPassword(Boolean.TRUE.equals(user.getTemporaryPassword()));
        response.setBranches(user.getBranches().stream().map(this::toBranchResponse).toList());
        return response;
    }

    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeRequired(String value, String message) {
        String normalized = normalize(value);
        if (normalized == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return normalized;
    }
}
