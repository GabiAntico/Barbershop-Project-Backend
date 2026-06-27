package com.barbershop.shifts.dtos.shifts;

import lombok.Data;

@Data
public class ShiftEmployeeResponse {
    private Long id;
    private String displayName;
    private String email;
}
