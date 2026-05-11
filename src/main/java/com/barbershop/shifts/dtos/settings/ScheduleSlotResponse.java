package com.barbershop.shifts.dtos.settings;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ScheduleSlotResponse {
    private String time;
    private boolean occupied;
    private String presence;
}
