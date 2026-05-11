package com.barbershop.shifts.dtos.settings;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ScheduleSettingsResponse {
    private LocalDate date;
    private List<ScheduleSlotResponse> slots;
}
