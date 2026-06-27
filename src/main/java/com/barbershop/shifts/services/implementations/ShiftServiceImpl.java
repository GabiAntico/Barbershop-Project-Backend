package com.barbershop.shifts.services.implementations;

import com.barbershop.shifts.dtos.clients.ClientResponse;
import com.barbershop.shifts.dtos.shifts.AgendaSlotResponse;
import com.barbershop.shifts.dtos.shifts.CreationShiftRequest;
import com.barbershop.shifts.dtos.shifts.ShiftCompleteResponse;
import com.barbershop.shifts.dtos.shifts.ShiftEmployeeResponse;
import com.barbershop.shifts.dtos.shifts.ShiftResponse;
import com.barbershop.shifts.dtos.shifts.TimeSlotAvailabilityResponse;
import com.barbershop.shifts.dtos.shifts.UpdateShiftRequest;
import com.barbershop.shifts.entities.Branch;
import com.barbershop.shifts.entities.Client;
import com.barbershop.shifts.entities.EmployeeSchedule;
import com.barbershop.shifts.entities.Shift;
import com.barbershop.shifts.entities.ShiftStatus;
import com.barbershop.shifts.entities.User;
import com.barbershop.shifts.repositories.EmployeeScheduleRepository;
import com.barbershop.shifts.repositories.ShiftRepositoryJpa;
import com.barbershop.shifts.repositories.UserRepository;
import com.barbershop.shifts.services.ClientService;
import com.barbershop.shifts.services.CurrentUserService;
import com.barbershop.shifts.services.ScheduleSettingsService;
import com.barbershop.shifts.services.ShiftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ShiftServiceImpl implements ShiftService {

    private static final int DEFAULT_SLOT_MINUTES = 30;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Autowired
    private ShiftRepositoryJpa shiftRepository;

    @Autowired
    private ClientService clientService;

    @Autowired
    private ScheduleSettingsService scheduleSettingsService;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeScheduleRepository employeeScheduleRepository;

    @Override
    public List<ShiftResponse> getAllShifts() {
        Branch branch = currentUserService.getCurrentBranch();
        List<Shift> shifts = shiftRepository.findAllByBranch(branch);

        List<ShiftResponse> shiftsDtos = new ArrayList();
        for(Shift shift : shifts){
            shiftsDtos.add(convertEntityIntoDto(shift));
        }

        return shiftsDtos;
    }

    @Override
    public List<ShiftCompleteResponse> getAllCompleteShifts(){

        Branch branch = currentUserService.getCurrentBranch();
        List<Shift> shifts = shiftRepository.findAllByBranch(branch);

        List<ShiftCompleteResponse> shiftsConverted = new ArrayList();
        for(Shift shift : shifts){
            shiftsConverted.add(convertEntityIntoCompleteDto(shift));
        }

        return shiftsConverted;
    }

    @Override
    public ShiftResponse getShiftById(Long id) {
        Shift shift = getShiftByIdRaw(id);

        return convertEntityIntoDto(shift);
    }

    @Override
    public Shift getShiftByIdRaw(Long id) {
        if(id <= 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parameter");
        }

        Branch branch = currentUserService.getCurrentBranch();

        return shiftRepository.findByIdAndBranch(id, branch).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity not found"));
    }

    @Override
    public List<TimeSlotAvailabilityResponse> getAvailabilityByDate(LocalDate date, Long excludeShiftId) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay().minusNanos(1);
        List<ShiftStatus> blocking = List.of(ShiftStatus.PENDING, ShiftStatus.COMPLETED);
        Branch branch = currentUserService.getCurrentBranch();
        List<Shift> blockingShifts = shiftRepository.findByDatetimeBetweenAndStatusInAndBranch(dayStart, dayEnd, blocking, branch)
                .stream()
                .filter(shift -> !Objects.equals(shift.getId(), excludeShiftId))
                .toList();
        Shift excludedShift = excludeShiftId == null ? null : getShiftByIdRaw(excludeShiftId);
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        List<User> employees = getEmployeesForBranch(branch);
        Map<Long, List<EmployeeSchedule>> schedulesByEmployee = getSchedulesByEmployee(employees, branch);

        List<TimeSlotAvailabilityResponse> availability = new ArrayList<>();
        for (LocalTime time : scheduleSettingsService.getSlotsForDate(date)) {
            LocalDateTime slotDateTime = LocalDateTime.of(date, time);
            boolean isExcludedShiftSlot = excludedShift != null && Objects.equals(excludedShift.getDatetime(), slotDateTime);
            List<User> workingEmployees = getWorkingEmployeesForSlot(slotDateTime, employees, schedulesByEmployee);
            List<User> availableEmployees = getAvailableEmployeesForSlot(slotDateTime, workingEmployees, blockingShifts);
            if (isExcludedShiftSlot && excludedShift.getAssignedEmployee() != null) {
                availableEmployees = ensureEmployeeIncluded(availableEmployees, excludedShift.getAssignedEmployee());
                workingEmployees = ensureEmployeeIncluded(workingEmployees, excludedShift.getAssignedEmployee());
            }
            boolean available = (isExcludedShiftSlot || !slotDateTime.isBefore(now)) && !availableEmployees.isEmpty();
            availability.add(new TimeSlotAvailabilityResponse(
                    time.format(TIME_FORMATTER),
                    available,
                    availableEmployees.size(),
                    workingEmployees.size(),
                    availableEmployees.stream().map(User::getId).toList()
            ));
        }

        return availability;
    }

    @Override
    public List<AgendaSlotResponse> getAgendaByDate(LocalDate date) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay().minusNanos(1);
        List<ShiftStatus> blocking = List.of(ShiftStatus.PENDING, ShiftStatus.COMPLETED);
        Branch branch = currentUserService.getCurrentBranch();
        List<Shift> blockingShifts = shiftRepository.findByDatetimeBetweenAndStatusInAndBranch(dayStart, dayEnd, blocking, branch);
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        List<User> employees = getEmployeesForBranch(branch);
        Map<Long, List<EmployeeSchedule>> schedulesByEmployee = getSchedulesByEmployee(employees, branch);

        List<AgendaSlotResponse> agenda = new ArrayList<>();
        for (LocalTime time : scheduleSettingsService.getSlotsForDate(date)) {
            LocalDateTime slotDateTime = LocalDateTime.of(date, time);
            List<Shift> shifts = findShiftsForSlot(slotDateTime, blockingShifts);
            List<ShiftCompleteResponse> shiftResponses = shifts.stream()
                    .map(this::convertEntityIntoCompleteDto)
                    .toList();
            List<User> workingEmployees = getWorkingEmployeesForSlot(slotDateTime, employees, schedulesByEmployee);
            List<User> availableEmployees = getAvailableEmployeesForSlot(slotDateTime, workingEmployees, blockingShifts);

            AgendaSlotResponse slot = new AgendaSlotResponse();
            slot.setTime(time.format(TIME_FORMATTER));
            slot.setAvailable(!availableEmployees.isEmpty() && !slotDateTime.isBefore(now));
            slot.setAvailableCount(availableEmployees.size());
            slot.setTotalCapacity(workingEmployees.size());
            slot.setShift(shiftResponses.isEmpty() ? null : shiftResponses.get(0));
            slot.setShifts(shiftResponses);
            agenda.add(slot);
        }

        return agenda;
    }

    @Override
    public ShiftResponse createShift(CreationShiftRequest shiftRequest) {

        validateAmount(shiftRequest.getEstimatedAmount());

        LocalDateTime dt = shiftRequest.getDatetime().withSecond(0).withNano(0);
        validateScheduleSlot(dt);
        LocalDateTime start = dt.minusMinutes(30);
        LocalDateTime end = dt.plusMinutes(30);

        Client client = clientService.getClientByIdRaw(shiftRequest.getClientId());
        User owner = currentUserService.getCurrentUser();
        Branch branch = currentUserService.getCurrentBranch();

        List<ShiftStatus> blocking = List.of(ShiftStatus.PENDING, ShiftStatus.COMPLETED);
        List<Shift> blockingShifts = shiftRepository.findByDatetimeBetweenAndStatusInAndBranch(start, end, blocking, branch);
        User assignedEmployee = resolveAssignedEmployee(shiftRequest.getAssignedEmployeeId(), branch, dt, blockingShifts, null);

        Shift shift = new Shift();
        shift.setDatetime(dt);
        shift.setClient(client);
        shift.setStatus(ShiftStatus.PENDING);
        shift.setEstimatedAmount(shiftRequest.getEstimatedAmount());
        shift.setOwner(owner);
        shift.setBranch(branch);
        shift.setAssignedEmployee(assignedEmployee);

        Shift shiftSaved = shiftRepository.save(shift);

        return convertEntityIntoDto(shiftSaved);
    }

    @Override
    public ShiftResponse updateShift(Long id, UpdateShiftRequest shiftRequest){

        validateAmount(shiftRequest.getEstimatedAmount());

        if(id == null || id <= 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parameter");
        }

        Shift actualShift = getShiftByIdRaw(id);
        Client newClient = clientService.getClientByIdRaw(shiftRequest.getClientId());
        Branch branch = currentUserService.getCurrentBranch();

        LocalDateTime dt = shiftRequest.getDatetime().withSecond(0).withNano(0);
        validateScheduleSlot(dt);
        LocalDateTime start = dt.minusMinutes(30);
        LocalDateTime end = dt.plusMinutes(30);

        List<ShiftStatus> blocking = List.of(ShiftStatus.PENDING, ShiftStatus.COMPLETED);
        List<Shift> blockingShifts = shiftRepository.findByDatetimeBetweenAndStatusInAndBranch(start, end, blocking, branch)
                .stream()
                .filter(shift -> !Objects.equals(shift.getId(), id))
                .toList();
        User assignedEmployee = resolveAssignedEmployee(shiftRequest.getAssignedEmployeeId(), branch, dt, blockingShifts, actualShift);

        if (Objects.equals(actualShift.getDatetime(), dt) &&
                Objects.equals(actualShift.getClient().getId(), shiftRequest.getClientId()) &&
                Objects.equals(actualShift.getStatus(), shiftRequest.getStatus()) &&
                Objects.equals(actualShift.getAssignedEmployee() == null ? null : actualShift.getAssignedEmployee().getId(), assignedEmployee.getId()) &&
                Objects.equals(actualShift.getEstimatedAmount(), shiftRequest.getEstimatedAmount())
        ) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The new information is the same as the previous one");
        }

        actualShift.setDatetime(dt);
        actualShift.setClient(newClient);
        actualShift.setStatus(shiftRequest.getStatus());
        actualShift.setEstimatedAmount(shiftRequest.getEstimatedAmount());
        actualShift.setAssignedEmployee(assignedEmployee);

        Shift shiftSaved = shiftRepository.save(actualShift);

        return convertEntityIntoDto(shiftSaved);
    }

    @Override
    public ShiftResponse completeShift(Long shiftId){
        Shift shift = getShiftByIdRaw(shiftId);

        shift.setStatus(ShiftStatus.COMPLETED);

        shiftRepository.save(shift);

        return convertEntityIntoDto(shift);
    }

    private ShiftResponse convertEntityIntoDto(Shift shift){
        ShiftResponse shiftResponse = new ShiftResponse();
        shiftResponse.setId(shift.getId());
        shiftResponse.setDatetime(shift.getDatetime());
        shiftResponse.setClientId(shift.getClient().getId());
        shiftResponse.setAssignedEmployeeId(shift.getAssignedEmployee() == null ? null : shift.getAssignedEmployee().getId());
        shiftResponse.setAssignedEmployee(convertEmployeeIntoDto(shift.getAssignedEmployee()));
        shiftResponse.setStatus(shift.getStatus());
        shiftResponse.setEstimatedAmount(shift.getEstimatedAmount());

        return shiftResponse;
    }

    private ShiftCompleteResponse convertEntityIntoCompleteDto(Shift shift){
        ClientResponse clientResponse = new ClientResponse();
        clientResponse.setId(shift.getClient().getId());
        clientResponse.setFirstName(shift.getClient().getFirstName());
        clientResponse.setLastName(shift.getClient().getLastName());
        clientResponse.setDocumentNumber(shift.getClient().getDocumentNumber());
        clientResponse.setEmail(shift.getClient().getEmail());
        clientResponse.setPhoneNumber(shift.getClient().getPhoneNumber());

        ShiftCompleteResponse shiftCompleteResponse = new ShiftCompleteResponse();
        shiftCompleteResponse.setId(shift.getId());
        shiftCompleteResponse.setDatetime(shift.getDatetime());
        shiftCompleteResponse.setClient(clientResponse);
        shiftCompleteResponse.setAssignedEmployee(convertEmployeeIntoDto(shift.getAssignedEmployee()));
        shiftCompleteResponse.setStatus(shift.getStatus());
        shiftCompleteResponse.setEstimatedAmount(shift.getEstimatedAmount());

        return shiftCompleteResponse;
    }

    private void validateAmount(BigDecimal amount){
        if (amount != null && amount.compareTo(BigDecimal.ZERO) < 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The amount can't be negative.");
        }
    }

    private void validateScheduleSlot(LocalDateTime datetime) {
        if (!scheduleSettingsService.isValidSlot(datetime.toLocalDate(), datetime.toLocalTime())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The shift time is not available for this date"
            );
        }
    }

    private boolean isEmployeeBlocked(LocalDateTime slotDateTime, User employee, List<Shift> blockingShifts) {
        LocalDateTime start = slotDateTime.minusMinutes(DEFAULT_SLOT_MINUTES);
        LocalDateTime end = slotDateTime.plusMinutes(DEFAULT_SLOT_MINUTES);

        return blockingShifts.stream()
                .filter(shift -> shift.getAssignedEmployee() != null)
                .filter(shift -> Objects.equals(shift.getAssignedEmployee().getId(), employee.getId()))
                .map(Shift::getDatetime)
                .anyMatch(datetime -> datetime.isAfter(start) && datetime.isBefore(end));
    }

    private List<Shift> findShiftsForSlot(LocalDateTime slotDateTime, List<Shift> blockingShifts) {
        LocalDateTime start = slotDateTime.minusMinutes(DEFAULT_SLOT_MINUTES);
        LocalDateTime end = slotDateTime.plusMinutes(DEFAULT_SLOT_MINUTES);

        return blockingShifts.stream()
                .filter(shift -> shift.getDatetime().isAfter(start) && shift.getDatetime().isBefore(end))
                .toList();
    }

    private List<User> getEmployeesForBranch(Branch branch) {
        return userRepository.findAllByBarbershopAndBranchesContaining(branch.getBarbershop(), branch);
    }

    private Map<Long, List<EmployeeSchedule>> getSchedulesByEmployee(List<User> employees, Branch branch) {
        if (employees.isEmpty()) {
            return Map.of();
        }

        return employeeScheduleRepository.findAllByEmployeeInAndBranch(employees, branch)
                .stream()
                .collect(Collectors.groupingBy(schedule -> schedule.getEmployee().getId()));
    }

    private List<User> getWorkingEmployeesForSlot(
            LocalDateTime slotDateTime,
            List<User> employees,
            Map<Long, List<EmployeeSchedule>> schedulesByEmployee
    ) {
        return employees.stream()
                .filter(employee -> isEmployeeWorking(slotDateTime, employee, schedulesByEmployee.get(employee.getId())))
                .toList();
    }

    private boolean isEmployeeWorking(LocalDateTime slotDateTime, User employee, List<EmployeeSchedule> schedules) {
        if (schedules == null || schedules.isEmpty()) {
            return true;
        }

        LocalTime slotTime = slotDateTime.toLocalTime();

        return schedules.stream()
                .filter(schedule -> schedule.getDayOfWeek() == slotDateTime.getDayOfWeek())
                .anyMatch(schedule ->
                        !slotTime.isBefore(schedule.getStartTime())
                                && !slotTime.isAfter(schedule.getEndTime())
                );
    }

    private List<User> getAvailableEmployeesForSlot(LocalDateTime slotDateTime, List<User> employees, List<Shift> blockingShifts) {
        return employees.stream()
                .filter(employee -> !isEmployeeBlocked(slotDateTime, employee, blockingShifts))
                .toList();
    }

    private List<User> ensureEmployeeIncluded(List<User> employees, User employee) {
        if (employees.stream().anyMatch(item -> Objects.equals(item.getId(), employee.getId()))) {
            return employees;
        }

        List<User> result = new ArrayList<>(employees);
        result.add(employee);
        return result;
    }

    private User resolveAssignedEmployee(Long requestedEmployeeId, Branch branch, LocalDateTime datetime, List<Shift> blockingShifts, Shift actualShift) {
        List<User> employees = getEmployeesForBranch(branch);
        Map<Long, List<EmployeeSchedule>> schedulesByEmployee = getSchedulesByEmployee(employees, branch);
        List<User> workingEmployees = getWorkingEmployeesForSlot(datetime, employees, schedulesByEmployee);

        if (employees.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There are no employees assigned to this branch");
        }

        if (requestedEmployeeId != null) {
            User employee = employees.stream()
                    .filter(item -> Objects.equals(item.getId(), requestedEmployeeId))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "The selected employee is not assigned to this branch"));

            boolean keepsExistingEmployeeAtExistingTime = actualShift != null
                    && actualShift.getAssignedEmployee() != null
                    && Objects.equals(actualShift.getAssignedEmployee().getId(), employee.getId())
                    && Objects.equals(actualShift.getDatetime(), datetime);
            boolean worksAtTime = workingEmployees.stream().anyMatch(item -> Objects.equals(item.getId(), employee.getId()));

            if (!worksAtTime && !keepsExistingEmployeeAtExistingTime) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The selected employee is not working at this time");
            }

            if (isEmployeeBlocked(datetime, employee, blockingShifts)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The selected employee is not available for this time");
            }

            return employee;
        }

        if (actualShift != null && actualShift.getAssignedEmployee() != null) {
            User currentEmployee = actualShift.getAssignedEmployee();
            boolean worksAtTime = workingEmployees.stream().anyMatch(employee -> Objects.equals(employee.getId(), currentEmployee.getId()));
            if (worksAtTime && !isEmployeeBlocked(datetime, currentEmployee, blockingShifts)) {
                return currentEmployee;
            }
        }

        return workingEmployees.stream()
                .filter(employee -> !isEmployeeBlocked(datetime, employee, blockingShifts))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "There is no employee available for this time"));
    }

    private ShiftEmployeeResponse convertEmployeeIntoDto(User employee) {
        if (employee == null) return null;

        ShiftEmployeeResponse response = new ShiftEmployeeResponse();
        response.setId(employee.getId());
        response.setDisplayName(employee.getDisplayName());
        response.setEmail(employee.getEmail());

        return response;
    }
}
