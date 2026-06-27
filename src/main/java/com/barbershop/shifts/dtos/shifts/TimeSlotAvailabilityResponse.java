package com.barbershop.shifts.dtos.shifts;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TimeSlotAvailabilityResponse {
    private String time;
    private boolean available;
    private int availableCount;
    private int totalCapacity;
    private List<Long> availableEmployeeIds;
}
