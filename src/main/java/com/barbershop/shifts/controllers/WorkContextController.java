package com.barbershop.shifts.controllers;

import com.barbershop.shifts.dtos.context.BranchResponse;
import com.barbershop.shifts.dtos.context.CreateBranchRequest;
import com.barbershop.shifts.dtos.context.CreateEmployeeRequest;
import com.barbershop.shifts.dtos.context.EmployeeResponse;
import com.barbershop.shifts.dtos.context.EmployeeScheduleDayRequest;
import com.barbershop.shifts.dtos.context.EmployeeScheduleDayResponse;
import com.barbershop.shifts.dtos.context.EmployeeScheduleRequest;
import com.barbershop.shifts.dtos.context.EmployeeScheduleResponse;
import com.barbershop.shifts.dtos.context.UpdateEmployeeBranchesRequest;
import com.barbershop.shifts.dtos.context.WorkContextResponse;
import com.barbershop.shifts.entities.Barbershop;
import com.barbershop.shifts.entities.Branch;
import com.barbershop.shifts.entities.EmployeeSchedule;
import com.barbershop.shifts.entities.User;
import com.barbershop.shifts.repositories.BranchRepositoryJpa;
import com.barbershop.shifts.repositories.EmployeeScheduleRepository;
import com.barbershop.shifts.repositories.UserRepository;
import com.barbershop.shifts.services.CurrentUserService;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/work-context")
public class WorkContextController {

    private final CurrentUserService currentUserService;
    private final BranchRepositoryJpa branchRepository;
    private final UserRepository userRepository;
    private final EmployeeScheduleRepository employeeScheduleRepository;
    private final PasswordEncoder passwordEncoder;

    public WorkContextController(
            CurrentUserService currentUserService,
            BranchRepositoryJpa branchRepository,
            UserRepository userRepository,
            EmployeeScheduleRepository employeeScheduleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.currentUserService = currentUserService;
        this.branchRepository = branchRepository;
        this.userRepository = userRepository;
        this.employeeScheduleRepository = employeeScheduleRepository;
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
        branch.setTimeZone(normalizeTimeZone(request.getTimeZone()));
        branch.setBarbershop(user.getBarbershop());
        branch = branchRepository.save(branch);
        user.getBranches().add(branch);
        userRepository.save(user);
        return toBranchResponse(branch);
    }

    @GetMapping("/employees")
    public List<EmployeeResponse> getEmployees() {
        User user = currentUserService.getCurrentUser();
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

    @GetMapping("/employees/{id}/schedule")
    public EmployeeScheduleResponse getEmployeeSchedule(
            @PathVariable Long id,
            @RequestParam Long branchId
    ) {
        User admin = requireAdmin();
        User employee = getEmployee(admin.getBarbershop(), id);
        Branch branch = getBranch(admin.getBarbershop(), branchId);
        validateEmployeeBranch(employee, branch);

        return toScheduleResponse(employee, branch);
    }

    @PutMapping("/employees/{id}/schedule")
    @Transactional
    public EmployeeScheduleResponse updateEmployeeSchedule(
            @PathVariable Long id,
            @RequestBody EmployeeScheduleRequest request
    ) {
        User admin = requireAdmin();
        User employee = getEmployee(admin.getBarbershop(), id);
        Branch branch = getBranch(admin.getBarbershop(), request.getBranchId());
        validateEmployeeBranch(employee, branch);
        validateScheduleRequest(request);

        employeeScheduleRepository.deleteAllByEmployeeAndBranch(employee, branch);

        List<EmployeeSchedule> schedules = request.getDays().stream()
                .filter(day -> Boolean.TRUE.equals(day.getEnabled()))
                .map(day -> {
                    EmployeeSchedule schedule = new EmployeeSchedule();
                    schedule.setEmployee(employee);
                    schedule.setBranch(branch);
                    schedule.setDayOfWeek(day.getDayOfWeek());
                    schedule.setStartTime(day.getStartTime());
                    schedule.setEndTime(day.getEndTime());
                    return schedule;
                })
                .toList();

        employeeScheduleRepository.saveAll(schedules);

        return toScheduleResponse(employee, branch);
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

    private User getEmployee(Barbershop barbershop, Long id) {
        return userRepository.findByIdAndBarbershop(id, barbershop)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
    }

    private Branch getBranch(Barbershop barbershop, Long id) {
        return branchRepository.findByIdAndBarbershop(id, barbershop)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid branch"));
    }

    private void validateEmployeeBranch(User employee, Branch branch) {
        boolean assigned = employee.getBranches().stream().anyMatch(item -> item.getId().equals(branch.getId()));
        if (!assigned) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee is not assigned to this branch");
        }
    }

    private void validateScheduleRequest(EmployeeScheduleRequest request) {
        if (request.getDays() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Schedule days are required");
        }

        boolean hasEnabledDay = false;
        for (EmployeeScheduleDayRequest day : request.getDays()) {
            if (!Boolean.TRUE.equals(day.getEnabled())) continue;
            hasEnabledDay = true;
            if (day.getDayOfWeek() == null || day.getStartTime() == null || day.getEndTime() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enabled days require start and end time");
            }
            if (!day.getEndTime().isAfter(day.getStartTime())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be after start time");
            }
        }

        if (!hasEnabledDay) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one working day is required");
        }
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
        response.setTimeZone(branch.getTimeZone());
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

    private EmployeeScheduleResponse toScheduleResponse(User employee, Branch branch) {
        List<EmployeeSchedule> schedules = employeeScheduleRepository
                .findAllByEmployeeAndBranchOrderByDayOfWeekAscStartTimeAsc(employee, branch);
        boolean hasCustomSchedule = !schedules.isEmpty();
        Map<DayOfWeek, EmployeeSchedule> schedulesByDay = schedules.stream()
                .collect(Collectors.toMap(EmployeeSchedule::getDayOfWeek, Function.identity(), (first, second) -> first));

        EmployeeScheduleResponse response = new EmployeeScheduleResponse();
        response.setEmployeeId(employee.getId());
        response.setBranchId(branch.getId());
        response.setDays(List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
                .stream()
                .map(day -> {
                    EmployeeSchedule schedule = schedulesByDay.get(day);
                    EmployeeScheduleDayResponse dayResponse = new EmployeeScheduleDayResponse();
                    dayResponse.setDayOfWeek(day);
                    dayResponse.setEnabled(hasCustomSchedule ? schedule != null : true);
                    dayResponse.setStartTime(schedule == null ? LocalTime.of(10, 0) : schedule.getStartTime());
                    dayResponse.setEndTime(schedule == null ? LocalTime.of(20, 0) : schedule.getEndTime());
                    return dayResponse;
                })
                .toList());
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

    private String normalizeTimeZone(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return Branch.DEFAULT_TIME_ZONE;
        }

        try {
            return ZoneId.of(normalized).getId();
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid branch time zone");
        }
    }
}
