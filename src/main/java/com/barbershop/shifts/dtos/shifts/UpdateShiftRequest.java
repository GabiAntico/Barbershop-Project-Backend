package com.barbershop.shifts.dtos.shifts;

import com.barbershop.shifts.entities.ShiftStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateShiftRequest {
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime datetime;

    @NotNull
    private Long clientId;

    @NotNull
    private ShiftStatus status;

    private BigDecimal estimatedAmount;
}
