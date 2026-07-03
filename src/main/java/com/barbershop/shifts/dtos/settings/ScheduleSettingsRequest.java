package com.barbershop.shifts.dtos.settings;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ScheduleSettingsRequest {
    @NotNull
    private String mode;

    private LocalDate date;
    private LocalDate startDate;
    private LocalDate endDate;

    private List<String> slots;
    private List<ScheduleWeeklyDayRequest> weeklySlots;
}
