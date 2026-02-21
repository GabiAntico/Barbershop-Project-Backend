package com.barbershop.shifts.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShiftCompleteResponse {
    private Long id;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime datetime;
    private ClientResponse client;
}
