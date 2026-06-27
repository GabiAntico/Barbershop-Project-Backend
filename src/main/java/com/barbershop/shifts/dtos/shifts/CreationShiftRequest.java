package com.barbershop.shifts.dtos.shifts;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreationShiftRequest {
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime datetime;

    @NotNull
    private Long clientId;

    private Long assignedEmployeeId;

    private BigDecimal estimatedAmount;
}
