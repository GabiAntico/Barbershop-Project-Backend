package com.barbershop.shifts.dtos.shifts;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TimeSlotAvailabilityResponse {
    private String time;
    private boolean available;
}
