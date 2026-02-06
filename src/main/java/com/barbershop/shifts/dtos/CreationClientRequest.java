package com.barbershop.shifts.dtos;

import lombok.Data;

@Data
public class CreationClientRequest {

    private String firstName;
    private String lastName;
    private String documentNumber;
}
