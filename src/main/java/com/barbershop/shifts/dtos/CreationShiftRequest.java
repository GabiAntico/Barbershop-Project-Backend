package com.barbershop.shifts.dtos;

import com.barbershop.shifts.entities.ShiftStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreationShiftRequest {
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime datetime;

    @NotNull
    private Long clientId;

    @NotNull
    private ShiftStatus status;
}
