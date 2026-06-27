package com.barbershop.shifts.dtos.shifts;

import com.barbershop.shifts.dtos.clients.ClientResponse;
import com.barbershop.shifts.entities.ShiftStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ShiftCompleteResponse {
    private Long id;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime datetime;
    private ClientResponse client;
    private ShiftEmployeeResponse assignedEmployee;
    private ShiftStatus status;
    private BigDecimal estimatedAmount;
}
