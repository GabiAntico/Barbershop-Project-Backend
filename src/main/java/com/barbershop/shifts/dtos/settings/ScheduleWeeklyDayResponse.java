package com.barbershop.shifts.dtos.settings;

import lombok.Data;

import java.time.DayOfWeek;
import java.util.List;

@Data
public class ScheduleWeeklyDayResponse {
    private DayOfWeek dayOfWeek;
    private List<ScheduleSlotResponse> slots;
}
