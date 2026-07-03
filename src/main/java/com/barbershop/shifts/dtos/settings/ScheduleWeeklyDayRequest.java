package com.barbershop.shifts.dtos.settings;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.DayOfWeek;
import java.util.List;

@Data
public class ScheduleWeeklyDayRequest {
    @NotNull
    private DayOfWeek dayOfWeek;

    private List<String> slots;
}
