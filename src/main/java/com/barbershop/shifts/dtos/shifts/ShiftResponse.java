package com.barbershop.shifts.dtos.shifts;

import com.barbershop.shifts.entities.ShiftStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ShiftResponse {
    private Long id;
    private LocalDateTime datetime;
    private Long clientId;
    private Long assignedEmployeeId;
    private ShiftEmployeeResponse assignedEmployee;
    private ShiftStatus status;
    private BigDecimal estimatedAmount;
}
