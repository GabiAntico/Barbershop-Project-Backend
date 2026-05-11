package com.barbershop.shifts.services;

import com.barbershop.shifts.dtos.settings.ScheduleSettingsRequest;
import com.barbershop.shifts.dtos.settings.ScheduleSettingsResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ScheduleSettingsService {
    ScheduleSettingsResponse getSchedule(LocalDate date);
    ScheduleSettingsResponse getScheduleRange(LocalDate startDate, LocalDate endDate);
    ScheduleSettingsResponse getDefaultSchedule();
    ScheduleSettingsResponse updateSchedule(ScheduleSettingsRequest request);
    List<LocalTime> getSlotsForDate(LocalDate date);
    boolean isValidSlot(LocalDate date, LocalTime time);
}
