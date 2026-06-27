package com.barbershop.shifts.dtos.shifts;

import lombok.Data;

import java.util.List;

@Data
public class AgendaSlotResponse {
    private String time;
    private boolean available;
    private int availableCount;
    private int totalCapacity;
    private ShiftCompleteResponse shift;
    private List<ShiftCompleteResponse> shifts;
}
