package com.barbershop.shifts.dtos.settings;

import jakarta.validation.constraints.NotEmpty;
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

    @NotEmpty
    private List<String> slots;
}
