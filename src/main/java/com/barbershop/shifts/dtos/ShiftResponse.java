package com.barbershop.shifts.dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShiftResponse {
    private Long id;
    private LocalDateTime dateTime;
    private Long clientId;
}
