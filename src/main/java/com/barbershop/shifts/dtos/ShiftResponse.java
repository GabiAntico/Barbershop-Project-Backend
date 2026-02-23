package com.barbershop.shifts.dtos;

import com.barbershop.shifts.entities.ShiftStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShiftResponse {
    private Long id;
    private LocalDateTime datetime;
    private Long clientId;
    private ShiftStatus status;
}
