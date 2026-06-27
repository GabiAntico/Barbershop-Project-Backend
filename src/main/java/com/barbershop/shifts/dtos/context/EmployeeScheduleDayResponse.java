package com.barbershop.shifts.dtos.context;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
public class EmployeeScheduleDayResponse {
    private DayOfWeek dayOfWeek;
    private Boolean enabled;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;
}
