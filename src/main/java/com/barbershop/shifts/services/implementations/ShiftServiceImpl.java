package com.barbershop.shifts.services.implementations;

import com.barbershop.shifts.dtos.clients.ClientResponse;
import com.barbershop.shifts.dtos.shifts.AgendaSlotResponse;
import com.barbershop.shifts.dtos.shifts.CreationShiftRequest;
import com.barbershop.shifts.dtos.shifts.ShiftCompleteResponse;
import com.barbershop.shifts.dtos.shifts.ShiftResponse;
import com.barbershop.shifts.dtos.shifts.TimeSlotAvailabilityResponse;
import com.barbershop.shifts.dtos.shifts.UpdateShiftRequest;
import com.barbershop.shifts.entities.Client;
import com.barbershop.shifts.entities.Shift;
import com.barbershop.shifts.entities.ShiftStatus;
import com.barbershop.shifts.repositories.ShiftRepositoryJpa;
import com.barbershop.shifts.services.ClientService;
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
import java.util.Objects;

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

    @Override
    public List<ShiftResponse> getAllShifts() {
        List<Shift> shifts = shiftRepository.findAll();

        List<ShiftResponse> shiftsDtos = new ArrayList();
        for(Shift shift : shifts){
            shiftsDtos.add(convertEntityIntoDto(shift));
        }

        return shiftsDtos;
    }

    @Override
    public List<ShiftCompleteResponse> getAllCompleteShifts(){

        List<Shift> shifts = shiftRepository.findAll();

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

        return shiftRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity not found"));
    }

    @Override
    public List<TimeSlotAvailabilityResponse> getAvailabilityByDate(LocalDate date, Long excludeShiftId) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay().minusNanos(1);
        List<ShiftStatus> blocking = List.of(ShiftStatus.PENDING, ShiftStatus.COMPLETED);
        List<Shift> blockingShifts = shiftRepository.findByDatetimeBetweenAndStatusIn(dayStart, dayEnd, blocking)
                .stream()
                .filter(shift -> !Objects.equals(shift.getId(), excludeShiftId))
                .toList();
        Shift excludedShift = excludeShiftId == null ? null : getShiftByIdRaw(excludeShiftId);
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);

        List<TimeSlotAvailabilityResponse> availability = new ArrayList<>();
        for (LocalTime time : scheduleSettingsService.getSlotsForDate(date)) {
            LocalDateTime slotDateTime = LocalDateTime.of(date, time);
            boolean isExcludedShiftSlot = excludedShift != null && Objects.equals(excludedShift.getDatetime(), slotDateTime);
            boolean available = (isExcludedShiftSlot || !slotDateTime.isBefore(now)) && !isSlotBlocked(slotDateTime, blockingShifts);
            availability.add(new TimeSlotAvailabilityResponse(time.format(TIME_FORMATTER), available));
        }

        return availability;
    }

    @Override
    public List<AgendaSlotResponse> getAgendaByDate(LocalDate date) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay().minusNanos(1);
        List<ShiftStatus> blocking = List.of(ShiftStatus.PENDING, ShiftStatus.COMPLETED);
        List<Shift> blockingShifts = shiftRepository.findByDatetimeBetweenAndStatusIn(dayStart, dayEnd, blocking);
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);

        List<AgendaSlotResponse> agenda = new ArrayList<>();
        for (LocalTime time : scheduleSettingsService.getSlotsForDate(date)) {
            LocalDateTime slotDateTime = LocalDateTime.of(date, time);
            Shift shift = findShiftForSlot(slotDateTime, blockingShifts);

            AgendaSlotResponse slot = new AgendaSlotResponse();
            slot.setTime(time.format(TIME_FORMATTER));
            slot.setAvailable(shift == null && !slotDateTime.isBefore(now));
            slot.setShift(shift == null ? null : convertEntityIntoCompleteDto(shift));
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

        List<ShiftStatus> blocking = List.of(ShiftStatus.PENDING, ShiftStatus.COMPLETED);

        if (shiftRepository.existsByDatetimeAfterAndDatetimeBeforeAndStatusIn(start, end, blocking)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "There is already a shift within 30 minutes of this time"
            );
        }

        Shift shift = new Shift();
        shift.setDatetime(dt);
        shift.setClient(client);
        shift.setStatus(ShiftStatus.PENDING);
        shift.setEstimatedAmount(shiftRequest.getEstimatedAmount());

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

        LocalDateTime dt = shiftRequest.getDatetime().withSecond(0).withNano(0);
        validateScheduleSlot(dt);
        LocalDateTime start = dt.minusMinutes(30);
        LocalDateTime end = dt.plusMinutes(30);

        List<ShiftStatus> blocking = List.of(ShiftStatus.PENDING, ShiftStatus.COMPLETED);

        if (shiftRepository.existsByDatetimeAfterAndDatetimeBeforeAndIdNotAndStatusIn(start, end, id, blocking)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "There is already a shift within 30 minutes of this time"
            );
        }

        if (Objects.equals(actualShift.getDatetime(), dt) &&
                Objects.equals(actualShift.getClient().getId(), shiftRequest.getClientId()) &&
                Objects.equals(actualShift.getStatus(), shiftRequest.getStatus())
        ) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The new information is the same as the previous one");
        }

        actualShift.setDatetime(dt);
        actualShift.setClient(newClient);
        actualShift.setStatus(shiftRequest.getStatus());
        actualShift.setEstimatedAmount(shiftRequest.getEstimatedAmount());

        Shift shiftSaved = shiftRepository.save(actualShift);

        return convertEntityIntoDto(shiftSaved);
    }

    @Override
    public ShiftResponse completeShift(Long shiftId){
        Shift shift = shiftRepository.findById(shiftId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity not found"));

        shift.setStatus(ShiftStatus.COMPLETED);

        shiftRepository.save(shift);

        return convertEntityIntoDto(shift);
    }

    private ShiftResponse convertEntityIntoDto(Shift shift){
        ShiftResponse shiftResponse = new ShiftResponse();
        shiftResponse.setId(shift.getId());
        shiftResponse.setDatetime(shift.getDatetime());
        shiftResponse.setClientId(shift.getClient().getId());
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

    private boolean isSlotBlocked(LocalDateTime slotDateTime, List<Shift> blockingShifts) {
        LocalDateTime start = slotDateTime.minusMinutes(DEFAULT_SLOT_MINUTES);
        LocalDateTime end = slotDateTime.plusMinutes(DEFAULT_SLOT_MINUTES);

        return blockingShifts.stream()
                .map(Shift::getDatetime)
                .anyMatch(datetime -> datetime.isAfter(start) && datetime.isBefore(end));
    }

    private Shift findShiftForSlot(LocalDateTime slotDateTime, List<Shift> blockingShifts) {
        LocalDateTime start = slotDateTime.minusMinutes(DEFAULT_SLOT_MINUTES);
        LocalDateTime end = slotDateTime.plusMinutes(DEFAULT_SLOT_MINUTES);

        return blockingShifts.stream()
                .filter(shift -> shift.getDatetime().isAfter(start) && shift.getDatetime().isBefore(end))
                .findFirst()
                .orElse(null);
    }
}
