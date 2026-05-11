package com.barbershop.shifts.services.implementations;

import com.barbershop.shifts.dtos.settings.ScheduleSettingsRequest;
import com.barbershop.shifts.dtos.settings.ScheduleSettingsResponse;
import com.barbershop.shifts.dtos.settings.ScheduleSlotResponse;
import com.barbershop.shifts.entities.AppSettings;
import com.barbershop.shifts.entities.ScheduleOverride;
import com.barbershop.shifts.entities.Shift;
import com.barbershop.shifts.entities.ShiftStatus;
import com.barbershop.shifts.repositories.AppSettingsRepositoryJpa;
import com.barbershop.shifts.repositories.ScheduleOverrideRepositoryJpa;
import com.barbershop.shifts.repositories.ShiftRepositoryJpa;
import com.barbershop.shifts.services.ScheduleSettingsService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ScheduleSettingsServiceImpl implements ScheduleSettingsService {

    private static final Long SETTINGS_ID = 1L;
    private static final int MIN_SLOT_DISTANCE_MINUTES = 30;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final List<ShiftStatus> BLOCKING_STATUSES = List.of(ShiftStatus.PENDING, ShiftStatus.COMPLETED);

    private final AppSettingsRepositoryJpa appSettingsRepository;
    private final ScheduleOverrideRepositoryJpa scheduleOverrideRepository;
    private final ShiftRepositoryJpa shiftRepository;

    public ScheduleSettingsServiceImpl(
            AppSettingsRepositoryJpa appSettingsRepository,
            ScheduleOverrideRepositoryJpa scheduleOverrideRepository,
            ShiftRepositoryJpa shiftRepository
    ) {
        this.appSettingsRepository = appSettingsRepository;
        this.scheduleOverrideRepository = scheduleOverrideRepository;
        this.shiftRepository = shiftRepository;
    }

    @Override
    public ScheduleSettingsResponse getSchedule(LocalDate date) {
        List<LocalTime> slots = getSlotsForDate(date);
        Set<LocalTime> occupied = getOccupiedTimes(date);
        List<LocalTime> displaySlots = mergeSlotsWithOccupied(slots, occupied);

        ScheduleSettingsResponse response = new ScheduleSettingsResponse();
        response.setDate(date);
        response.setSlots(displaySlots.stream()
                .map(slot -> new ScheduleSlotResponse(slot.format(TIME_FORMATTER), occupied.contains(slot), "ALL"))
                .toList());

        return response;
    }

    @Override
    public ScheduleSettingsResponse getScheduleRange(LocalDate startDate, LocalDate endDate) {
        validateRange(startDate, endDate);

        List<LocalDate> dates = getDatesBetween(startDate, endDate);
        java.util.Map<LocalTime, Integer> slotCounts = new java.util.HashMap<>();
        for (LocalDate date : dates) {
            for (LocalTime slot : getSlotsForDate(date)) {
                slotCounts.merge(slot, 1, Integer::sum);
            }
        }

        Set<LocalTime> occupied = getOccupiedTimesBetween(startDate, endDate);
        Set<LocalTime> unionSlots = new HashSet<>(slotCounts.keySet());
        unionSlots.addAll(occupied);
        List<LocalTime> displaySlots = unionSlots.stream().sorted(Comparator.naturalOrder()).toList();

        ScheduleSettingsResponse response = new ScheduleSettingsResponse();
        response.setDate(startDate);
        response.setSlots(displaySlots.stream()
                .map(slot -> new ScheduleSlotResponse(
                        slot.format(TIME_FORMATTER),
                        occupied.contains(slot),
                        slotCounts.getOrDefault(slot, 0) == dates.size() ? "ALL" : "PARTIAL"
                ))
                .toList());

        return response;
    }

    @Override
    public ScheduleSettingsResponse getDefaultSchedule() {
        ScheduleSettingsResponse response = new ScheduleSettingsResponse();
        response.setDate(LocalDate.now());
        response.setSlots(parseSlots(getSettings().getDefaultScheduleSlots()).stream()
                .map(slot -> new ScheduleSlotResponse(slot.format(TIME_FORMATTER), false, "ALL"))
                .toList());

        return response;
    }

    @Override
    public ScheduleSettingsResponse updateSchedule(ScheduleSettingsRequest request) {
        List<LocalTime> slots = normalizeSlots(request.getSlots());
        String mode = request.getMode().trim().toUpperCase();

        switch (mode) {
            case "DATE" -> updateSingleDate(request.getDate(), slots);
            case "RANGE" -> updateRange(request.getStartDate(), request.getEndDate(), slots);
            case "DEFAULT" -> updateDefault(slots);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid schedule mode");
        }

        LocalDate responseDate = switch (mode) {
            case "DATE" -> request.getDate();
            case "RANGE" -> request.getStartDate();
            default -> LocalDate.now();
        };

        return getSchedule(responseDate);
    }

    @Override
    public List<LocalTime> getSlotsForDate(LocalDate date) {
        return scheduleOverrideRepository.findByDate(date)
                .map(override -> parseSlots(override.getSlots()))
                .orElseGet(() -> parseSlots(getSettings().getDefaultScheduleSlots()));
    }

    @Override
    public boolean isValidSlot(LocalDate date, LocalTime time) {
        return getSlotsForDate(date).contains(time.withSecond(0).withNano(0));
    }

    private void updateSingleDate(LocalDate date, List<LocalTime> slots) {
        if (date == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date is required");
        }

        validateNotPast(date);
        ensureOccupiedSlotsAreKept(date, slots);
        saveOverride(date, slots);
    }

    private void updateRange(LocalDate startDate, LocalDate endDate, List<LocalTime> slots) {
        validateRange(startDate, endDate);

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (hasBlockingShifts(date)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The selected date range has shifts");
            }
        }

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            saveOverride(date, slots);
        }
    }

    private void updateDefault(List<LocalTime> slots) {
        AppSettings settings = getSettings();
        String previousDefault = settings.getDefaultScheduleSlots();

        Set<LocalDate> datesWithShifts = shiftRepository.findAll().stream()
                .filter(shift -> BLOCKING_STATUSES.contains(shift.getStatus()))
                .map(shift -> shift.getDatetime().toLocalDate())
                .collect(Collectors.toSet());

        for (LocalDate date : datesWithShifts) {
            if (scheduleOverrideRepository.findByDate(date).isEmpty()) {
                saveOverride(date, parseSlots(previousDefault));
            }
        }

        settings.setDefaultScheduleSlots(formatSlots(slots));
        appSettingsRepository.save(settings);
    }

    private void saveOverride(LocalDate date, List<LocalTime> slots) {
        ScheduleOverride override = scheduleOverrideRepository.findByDate(date).orElseGet(ScheduleOverride::new);
        override.setDate(date);
        override.setSlots(formatSlots(slots));
        scheduleOverrideRepository.save(override);
    }

    private void ensureOccupiedSlotsAreKept(LocalDate date, List<LocalTime> slots) {
        Set<LocalTime> occupiedTimes = getOccupiedTimes(date);

        if (!new HashSet<>(slots).containsAll(occupiedTimes)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Occupied slots can't be removed");
        }
    }

    private boolean hasBlockingShifts(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay().minusNanos(1);

        return shiftRepository.existsByDatetimeBetweenAndStatusIn(start, end, BLOCKING_STATUSES);
    }

    private Set<LocalTime> getOccupiedTimes(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay().minusNanos(1);

        return shiftRepository.findByDatetimeBetweenAndStatusIn(start, end, BLOCKING_STATUSES)
                .stream()
                .map(Shift::getDatetime)
                .map(LocalDateTime::toLocalTime)
                .map(time -> time.withSecond(0).withNano(0))
                .collect(Collectors.toSet());
    }

    private Set<LocalTime> getOccupiedTimesBetween(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay().minusNanos(1);

        return shiftRepository.findByDatetimeBetweenAndStatusIn(start, end, BLOCKING_STATUSES)
                .stream()
                .map(Shift::getDatetime)
                .map(LocalDateTime::toLocalTime)
                .map(time -> time.withSecond(0).withNano(0))
                .collect(Collectors.toSet());
    }

    private List<LocalTime> mergeSlotsWithOccupied(List<LocalTime> slots, Set<LocalTime> occupied) {
        Set<LocalTime> merged = new HashSet<>(slots);
        merged.addAll(occupied);

        return merged.stream().sorted(Comparator.naturalOrder()).toList();
    }

    private List<LocalDate> getDatesBetween(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> dates = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            dates.add(date);
        }

        return dates;
    }

    private void validateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date and end date are required");
        }

        validateNotPast(startDate);
        validateNotPast(endDate);

        if (endDate.isBefore(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date can't be before start date");
        }
    }

    private void validateNotPast(LocalDate date) {
        if (date.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Past dates can't be configured");
        }
    }

    private List<LocalTime> normalizeSlots(List<String> rawSlots) {
        List<LocalTime> slots = rawSlots.stream()
                .map(this::parseSlot)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();

        if (slots.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one slot is required");
        }

        for (int i = 1; i < slots.size(); i++) {
            long minutes = java.time.Duration.between(slots.get(i - 1), slots.get(i)).toMinutes();
            if (minutes < MIN_SLOT_DISTANCE_MINUTES) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slots must be at least 30 minutes apart");
            }
        }

        return slots;
    }

    private LocalTime parseSlot(String slot) {
        try {
            return LocalTime.parse(slot, TIME_FORMATTER).withSecond(0).withNano(0);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid slot format");
        }
    }

    private List<LocalTime> parseSlots(String slots) {
        if (slots == null || slots.trim().isEmpty()) {
            return parseSlots(AppSettingsServiceImpl.FALLBACK_DEFAULT_SLOTS);
        }

        List<LocalTime> parsed = new ArrayList<>();
        for (String slot : slots.split(",")) {
            parsed.add(parseSlot(slot));
        }

        return parsed.stream().distinct().sorted(Comparator.naturalOrder()).toList();
    }

    private String formatSlots(List<LocalTime> slots) {
        return slots.stream()
                .sorted(Comparator.naturalOrder())
                .map(slot -> slot.format(TIME_FORMATTER))
                .collect(Collectors.joining(","));
    }

    private AppSettings getSettings() {
        AppSettings settings = appSettingsRepository.findById(SETTINGS_ID).orElseGet(() -> {
            AppSettings created = new AppSettings();
            created.setId(SETTINGS_ID);
            created.setDefaultEstimatedAmount(java.math.BigDecimal.ZERO);
            created.setDefaultScheduleSlots(AppSettingsServiceImpl.FALLBACK_DEFAULT_SLOTS);
            return appSettingsRepository.save(created);
        });

        if (settings.getDefaultScheduleSlots() == null || settings.getDefaultScheduleSlots().trim().isEmpty()) {
            settings.setDefaultScheduleSlots(AppSettingsServiceImpl.FALLBACK_DEFAULT_SLOTS);
            return appSettingsRepository.save(settings);
        }

        return settings;
    }
}
