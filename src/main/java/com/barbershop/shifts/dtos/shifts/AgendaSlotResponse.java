package com.barbershop.shifts.dtos.shifts;

import lombok.Data;

@Data
public class AgendaSlotResponse {
    private String time;
    private boolean available;
    private ShiftCompleteResponse shift;
}
